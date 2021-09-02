package org.bahmni.module.pacsquery.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;

import java.util.List;

public interface PacsService {
	
	@Authorized({ PrivilegeConstants.GET_ORDERS })
	List<Object> findStudies(String patientId, String date);
}
