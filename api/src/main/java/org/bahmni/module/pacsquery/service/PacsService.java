package org.bahmni.module.pacsquery.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;

import java.util.List;
import java.util.Map;

public interface PacsService {
	
	@Authorized({ PrivilegeConstants.GET_ENCOUNTERS })
	List<Map<String, Object>> findStudies(String patientId, String date);
}
