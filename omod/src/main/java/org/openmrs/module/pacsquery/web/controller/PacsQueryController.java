/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.pacsquery.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.UserService;
import org.openmrs.module.pacsquery.PacsQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Writer;

/**
 * This class configured as controller using annotation and mapped with the URL of
 * 'module/pacsquery/pacsquery.form'.
 */
@Controller("$pacsquery.PacsQueryController")
@RequestMapping(value = "module/pacsquery")
public class PacsQueryController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	UserService userService;
	
	/**
	 * Initially called after the getUsers method to get the landing form name
	 * 
	 * @return String form view name
	 */
	@RequestMapping(value = "query.form", method = RequestMethod.GET, produces = "application/json")
	public void onGet(Writer responseWriter, @RequestParam(value = "patientid", defaultValue = "") String patientid,
	        @RequestParam(value = "date", defaultValue = "") String date) {
		PacsQueryService ps = new PacsQueryService(responseWriter);
		try {
			ps.query(patientid, date);
		}
		catch (Exception e) {
			log.error(e.getMessage());
			// System.out.println(e.getMessage());
			try {
				responseWriter.write("[{\"error\":\"Query failed: " + e.getMessage() + "\"}]");
			}
			catch (Exception e2) {}
		}
	}
	
}
