package org.bahmni.module.pacsquery.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimpleImagingStudy implements Serializable {
	
	private String resourceType = "ImagingStudy";
	
	private String id;
	
	private ArrayList identifier = new ArrayList();
	
	private Map subject;
	
	private String started;
	
	private String status;
	
	public void setPatientReference(String patientId) {
        HashMap<String, String> ref = new HashMap<>();
        ref.put("reference", "Patient/".concat(patientId));
        this.subject = ref;
    }
	
	public void setAccessionNumber(String accessionNumber) {
        HashMap<String, String> id = new HashMap<>();
        id.put("system", "urn:bahmni:accession");
        id.put("value", "urn:oid:" + accessionNumber);
        this.identifier.add(id);
    }
	
	public void setStudyUID(String studyUID) {
        this.id = studyUID;
        HashMap<String, String> dicomStudyId = new HashMap<>();
        dicomStudyId.put("system", "urn:dicom:uid");
        dicomStudyId.put("value", "urn:oid:" + studyUID);
        this.identifier.add(dicomStudyId);
    }
	
	public void setStarted(String started) {
		this.started = started;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getResourceType() {
		return resourceType;
	}
	
	public String getId() {
		return id;
	}
	
	public ArrayList getIdentifier() {
		return identifier;
	}
	
	public Map getSubject() {
		return subject;
	}
	
	public String getStarted() {
		return started;
	}
	
	public String getStatus() {
		return status;
	}
}
