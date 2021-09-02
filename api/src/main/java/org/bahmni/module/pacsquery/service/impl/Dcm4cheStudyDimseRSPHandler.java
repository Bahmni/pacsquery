package org.bahmni.module.pacsquery.service.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Dcm4cheStudyDimseRSPHandler extends DimseRSPHandler {
	
	private int cancelAfter = 50;
	
	private int numMatches = 0;
	
	private List<Object> studies = new ArrayList();
	
	public Dcm4cheStudyDimseRSPHandler(int msgId) {
		super(msgId);
	}
	
	@Override
	public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
		super.onDimseRSP(as, cmd, data);
		int status = cmd.getInt(Tag.Status, -1);
		if (Status.isPending(status)) {
			try {
				Function<Integer, Optional<String>> attrCheck = tag -> {
					if (!data.contains(tag)) {
						return Optional.empty();
					}
					return Optional.ofNullable(data.getString(tag));
				};
				SimpleImagingStudy dicomStudy = new SimpleImagingStudy();
				attrCheck.apply(Tag.PatientID).ifPresent(value -> dicomStudy.setPatientReference(value));
				attrCheck.apply(Tag.StudyInstanceUID).ifPresent(value -> dicomStudy.setStudyUID(value));
				attrCheck.apply(Tag.AccessionNumber).ifPresent(value -> dicomStudy.setAccessionNumber(value));
				attrCheck.apply(Tag.StudyDate).ifPresent(value -> dicomStudy.setStarted(value));
				attrCheck.apply(Tag.Status).ifPresent(value -> dicomStudy.setStatus(value));
				studies.add(dicomStudy);
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
	
	public List<Object> getStudies() {
		return this.studies;
	}
}
