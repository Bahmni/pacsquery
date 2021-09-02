package org.bahmni.module.pacsquery.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.pacsquery.service.PacsService;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Priority;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ValidationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Dcm4cheServiceImpl implements PacsService {
	
	private static final String AE_TITLE = "PACSQUERY";
	
	private static final String DEVICE_NAME = "findscu";
	
	private AdministrationService administrationService;
	
	private final Log log = LogFactory.getLog(getClass());
	
	public Dcm4cheServiceImpl(AdministrationService administrationService) {
		this.administrationService = administrationService;
	}
	
	private HashMap<String, Object> loadConfig() {
		String pacsConfig = administrationService.getGlobalProperty("pacsquery.pacsConfig");
		HashMap<String, Object> configParams = new HashMap<>();
		if (pacsConfig == null || "".equals(pacsConfig)) {
			return configParams;
		}

		// DCM4CHEE@localhost:11112
		String[] parts = pacsConfig.split("@");
		String[] addr = parts[1].split(":");

		configParams.put("aetitle", parts[0]);
		configParams.put("host", addr[0]);
		configParams.put("port", Integer.parseInt(addr[1]));

		String retrieveTags = administrationService.getGlobalProperty("pacsquery.retrieveTags");
		//00000000,34323431
		String[] tagsString = retrieveTags.split(",");
		int[] tags = new int[tagsString.length];
		for (int i = 0; i < tagsString.length; i++) {
			tags[i] = (int) Long.parseLong(tagsString[i], 16);
		}
		configParams.put("tags", tags);
		return configParams;
	}
	
	@Override
	public List<Object> findStudies(String patientId, String date) {
		HashMap<String, Object> config = loadConfig();
		if (config.isEmpty()) {
			throw new UnsupportedOperationException("Pacs Server configuration not setup");
		}
		verifyParameters(patientId, date);
		Device device = new Device(DEVICE_NAME);
		Connection remoteConnection = new Connection("pacs", (String) config.get("host"), (Integer) config.get("port"));
		Connection aeConnection = getAEConnection();
		remoteConnection.setTlsProtocols(aeConnection.getTlsProtocols());
		remoteConnection.setTlsCipherSuites(aeConnection.getTlsCipherSuites());
		
		// Create Application Entity
		ApplicationEntity ae = new ApplicationEntity(AE_TITLE);
		ae.addConnection(aeConnection);
		device.addConnection(aeConnection);
		device.addApplicationEntity(ae);
		
		// Create Association Request
		AAssociateRQ request = new AAssociateRQ();
		request.setCalledAET((String) config.get("aetitle"));
		// pulled from CLIUtils.java
		String[] IVR_LE_FIRST = { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian };
		request.addPresentationContext(new PresentationContext(1, UID.StudyRootQueryRetrieveInformationModelFind,
		        IVR_LE_FIRST));
		
		// Create Attributes
		Attributes attr = new Attributes();
		// Add study level
		attr.setString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
		// request params
		int[] tags = (int[]) config.get("tags");
		for (int tag : tags) {
			attr.setNull(tag, ElementDictionary.vrOf(tag, null));
		}
		// Match query params
		if (!patientId.isEmpty()) {
			attr.setString(0x00100020, VR.LO, patientId);
		}
		if (!date.isEmpty()) {
			attr.setString(0x00080020, VR.DA, date); // date-date
		}
		
		// Create Executor Service
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		device.setExecutor(executorService);
		device.setScheduledExecutor(scheduledExecutorService);
		
		// Run the query and write the result
		Association association = null;
		try {
			association = ae.connect(remoteConnection, request);
			Dcm4cheStudyDimseRSPHandler rspHandler = new Dcm4cheStudyDimseRSPHandler(association.nextMessageID());
			association.cfind(UID.StudyRootQueryRetrieveInformationModelFind, Priority.NORMAL, attr, null, rspHandler);
			return rspHandler.getStudies();
		} catch (Exception e) {
			throw new APIException("Query failed: " + e.getMessage());
		} finally {
			if (association != null && association.isReadyForDataTransfer()) {
				try {
					association.waitForOutstandingRSP();
					association.release();
				} catch (InterruptedException | IOException e) {
					log.error("Error occurred while trying to release AE Association", e);
				}
			}
			executorService.shutdown();
			scheduledExecutorService.shutdown();
		}
	}
	
	private void verifyParameters(String patientId, String date) {
		if (patientId.isEmpty() && date.isEmpty()) {
			throw new ValidationException("At least one of patientId, date required.");
		}
		if (!patientId.isEmpty() && !patientId.matches("^[A-Za-z]{0,3}[0-9]+$")) {
			throw new ValidationException("patientId must be numeric");
		}
		if (!date.isEmpty() && !date.matches("^[0-9]{4}[0-1][0-9][0-3][0-9]$")) {
			throw new ValidationException("date must be of format YYYYMMDD");
		}
	}
	
	private Connection getAEConnection() {
		Connection conn = new Connection();
		// Set connection properties
		conn.setReceivePDULength(Connection.DEF_MAX_PDU_LENGTH);
		conn.setSendPDULength(Connection.DEF_MAX_PDU_LENGTH);
		conn.setMaxOpsInvoked(0);
		conn.setMaxOpsPerformed(0);
		conn.setPackPDV(true);
		conn.setConnectTimeout(10000); // 10 sec
		conn.setRequestTimeout(10000); // 10 sec
		conn.setAcceptTimeout(0);
		conn.setReleaseTimeout(0);
		conn.setResponseTimeout(0);
		conn.setRetrieveTimeout(0);
		conn.setIdleTimeout(0);
		conn.setSocketCloseDelay(Connection.DEF_SOCKETDELAY);
		conn.setSendBufferSize(0);
		conn.setReceiveBufferSize(0);
		conn.setTcpNoDelay(true);
		return conn;
	}
}
