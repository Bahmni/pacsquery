package org.openmrs.module.pacsquery;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientStudyDimseRSPHandler extends DimseRSPHandler {
	
	private int cancelAfter = 50;
	
	private int numMatches = 0;
	
	private List<Map<String, Object>> studies = new ArrayList();
	
	public PatientStudyDimseRSPHandler(int msgId) {
		super(msgId);
	}
	
	@Override
	public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
		super.onDimseRSP(as, cmd, data);
		int status = cmd.getInt(Tag.Status, -1);
		if (Status.isPending(status)) {
			try {
				HashMap<String, Object> record = new HashMap<String, Object>();
				write(record, "patientId", Tag.PatientID, data);
				write(record, "patientName", Tag.PatientName, data);
				write(record, "studyDate", Tag.StudyDate, data);
				write(record, "studyTime", Tag.StudyTime, data);
				write(record, "studyInstanceUID", Tag.StudyInstanceUID, data);
				write(record, "studyID", Tag.StudyID, data);
				write(record, "seriesDate", Tag.SeriesDate, data);
				write(record, "seriesTime", Tag.SeriesTime, data);
				write(record, "seriesDescription", Tag.SeriesDescription, data);
				write(record, "seriesInstanceUID", Tag.SeriesInstanceUID, data);
				write(record, "accessionNumber", Tag.AccessionNumber, data);
				write(record, "requestedProcedureDescription", Tag.RequestedProcedureDescription, data);
				write(record, "requestedProcedureComments", Tag.RequestedProcedureComments, data);
				studies.add(record);
			}
			catch (Exception e) {
				System.out.println("Failed to write JSON : " + e.getMessage());
			}
			++numMatches;
			if (cancelAfter != 0 && numMatches >= cancelAfter)
				try {
					cancel(as);
					cancelAfter = 0;
				}
				catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	private void write(Map<String, Object> record, String name, int tag, Attributes data) throws IOException {
		if (data.contains(tag)) {
			Object tagValue = data.getString(tag);
			record.put(name, tagValue);
		}
	}
	
	public List<Map<String, Object>> getStudies() {
		return this.studies;
	}
}
