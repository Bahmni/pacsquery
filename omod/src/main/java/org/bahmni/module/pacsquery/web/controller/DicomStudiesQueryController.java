package org.bahmni.module.pacsquery.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.pacsquery.service.PacsService;
import org.openmrs.api.APIAuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping(value = "/rest/v1/pacs")
@Controller
public class DicomStudiesQueryController {
	
	private static final String ERROR_OCCURRED_WHILE_QUERYING_PACS_SERVER = "Error occurred while trying to query PACS server";
	
	private static final String PACS_CONFIGURATION_UNDEFINED = "PACS Server configuration is not defined";
	
	private static final String INSUFFICIENT_PRIVILEGE = "Insufficient privilege";
	
	private final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	private PacsService pacsService;
	
	@RequestMapping(value = "/studies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	ResponseEntity<? extends Object> fetchStudies(@RequestParam(value = "patientId", defaultValue = "") String patientId,
	        @RequestParam(value = "date", defaultValue = "") String date) {
		try {
			Object studies = pacsService.findStudies(patientId, date);
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(studies);
		} catch (UnsupportedOperationException uoe) {
			log.error(ERROR_OCCURRED_WHILE_QUERYING_PACS_SERVER, uoe);
			return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, PACS_CONFIGURATION_UNDEFINED), HttpStatus.NOT_IMPLEMENTED);
		} catch (APIAuthenticationException aae) {
			log.error(ERROR_OCCURRED_WHILE_QUERYING_PACS_SERVER, aae);
			return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, INSUFFICIENT_PRIVILEGE), HttpStatus.FORBIDDEN);
		} catch (Exception e) {
			log.error(ERROR_OCCURRED_WHILE_QUERYING_PACS_SERVER, e);
			return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, ERROR_OCCURRED_WHILE_QUERYING_PACS_SERVER), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
