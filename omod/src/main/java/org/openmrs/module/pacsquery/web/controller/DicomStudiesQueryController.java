package org.openmrs.module.pacsquery.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.pacsquery.PacsQueryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Writer;

@RequestMapping(value = "/rest/v1/pacs")
@Controller
public class DicomStudiesQueryController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/studies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	ResponseEntity<? extends Object> fetchStudies(Writer responseWriter,
	        @RequestParam(value = "patientId", defaultValue = "") String patientId,
	        @RequestParam(value = "date", defaultValue = "") String date) {
		PacsQueryService ps = new PacsQueryService();
		try {
			Object studies = ps.dicomQuery(patientId, date);
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(studies);
		}
		catch (Exception e) {
			log.error("Error occurred while trying to query PACS server", e);
			return ResponseEntity.unprocessableEntity().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			        .body(e.getMessage());
		}
	}
}
