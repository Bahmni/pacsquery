package org.bahmni.module.pacsquery.service.impl;

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
import java.util.Optional;
import java.util.function.Function;

public class Dcm4cheStudyDimseRSPHandler extends DimseRSPHandler {
	
	private int cancelAfter = 50;
	
	private int numMatches = 0;
	
	private List<Map<String, Object>> studies = new ArrayList();
	
	public Dcm4cheStudyDimseRSPHandler(int msgId) {
		super(msgId);
	}
	
	@Override
	public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
		super.onDimseRSP(as, cmd, data);
		int status = cmd.getInt(Tag.Status, -1);
		if (Status.isPending(status)) {
			try {
				HashMap<String, Object> record = new HashMap<String, Object>();
				Function<Integer, Optional<String>> attrCheck = tag -> {
					if (!data.contains(tag)) {
						return Optional.empty();
					}
					return Optional.ofNullable(data.getString(tag));
				};
				attrCheck.apply(Tag.PatientID).ifPresent(value -> record.put("patientId", value));
				attrCheck.apply(Tag.PatientName).ifPresent(value -> record.put("patientName", value));
				attrCheck.apply(Tag.CreationDate).ifPresent(value -> record.put("creationDate", value));
				attrCheck.apply(Tag.StudyDate).ifPresent(value -> record.put("studyDate", value));
				attrCheck.apply(Tag.StudyTime).ifPresent(value -> record.put("studyTime", value));
				attrCheck.apply(Tag.StudyInstanceUID).ifPresent(value -> record.put("studyInstanceUID", value));
				attrCheck.apply(Tag.StudyID).ifPresent(value -> record.put("studyID", value));
				attrCheck.apply(Tag.SeriesDate).ifPresent(value -> record.put("seriesDate", value));
				attrCheck.apply(Tag.SeriesTime).ifPresent(value -> record.put("seriesTime", value));
				attrCheck.apply(Tag.SeriesDescription).ifPresent(value -> record.put("seriesDescription", value));
				attrCheck.apply(Tag.SeriesInstanceUID).ifPresent(value -> record.put("seriesInstanceUID", value));
				attrCheck.apply(Tag.AccessionNumber).ifPresent(value -> record.put("accessionNumber", value));
				attrCheck.apply(Tag.RequestedProcedureDescription).ifPresent(value -> record.put("requestedProcedureDescription", value));
				attrCheck.apply(Tag.RequestedProcedureComments).ifPresent(value -> record.put("requestedProcedureComments", value));
				attrCheck.apply(Tag.ScheduledProcedureStepDescription).ifPresent(value -> record.put("scheduledProcedureStepDescription", value));
				studies.add(record);
			} catch (Exception e) {
				System.out.println("Failed to write JSON : " + e.getMessage());
			}
			++numMatches;
			if (cancelAfter != 0 && numMatches >= cancelAfter) {
				try {
					cancel(as);
					cancelAfter = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public List<Map<String, Object>> getStudies() {
		return this.studies;
	}
}
