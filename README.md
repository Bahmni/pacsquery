Pacs Query
==========================

Description
-----------
This module provides a URL-based query to a PACS system, retrieving studies according to patient ID and returning a list of matched studies with a collection of queried fields.

The connection to the PACS system should be configured via the pacsquery.pacsConfig OpenMRS globalProperty. Studies can be retrieved by going to `http://HOSTIP/openmrs/module/pacsquery/query.form?patientid=204586`. Fields can be configured using the pacsquery.retrieveTags globalProperty.

The response JSON is built using the JSONWriter component of dcm4che. What is returned is a list of objects, each object representing a study. Each study is itself a collect of key value pairs, where keys are 8-character strings of DICOM tags. Corresponding values are an object containing "vr" and "Value".

Example:

	[  
	   {  
	      "00080005":{  
	         "vr":"CS",
	         "Value":[  
	            "ISO_IR 100"
	         ]
	      },
	      "00080020":{  
	         "vr":"DA",
	         "Value":[  
	            "20170614"
	         ]
	      },
	      ...
	   }
	   ...
	]

Building from Source
--------------------
This module was created using the OpenMRS SDK.

Full build instructions
-----------------------

You will need to have Java 1.6+ and Maven 2.x+ installed.  Use the command 'mvn package' to 
compile and package the module.  The .omod file will be in the omod/target folder.

Alternatively you can add the snippet provided in the [Creating Modules](https://wiki.openmrs.org/x/cAEr) page to your 
omod/pom.xml and use the mvn command:

    mvn package -P deploy-web -D deploy.path="../../openmrs-1.8.x/webapp/src/main/webapp"

It will allow you to deploy any changes to your web 
resources such as jsp or js files without re-installing the module. The deploy path says 
where OpenMRS is deployed.

Installation
------------
1. Build the module to produce the .omod file.
2. Use the OpenMRS Administration > Manage Modules screen to upload and install the .omod file.

If uploads are not allowed from the web (changable via a runtime property), you can drop the omod
into the ~/.OpenMRS/modules folder.  (Where ~/.OpenMRS is assumed to be the Application 
Data Directory that the running openmrs is currently using.)  After putting the file in there 
simply restart OpenMRS/tomcat and the module will be loaded and started.
