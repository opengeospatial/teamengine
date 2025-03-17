/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.jaxrs;

/*-
 * #%L
 * TEAM Engine - Service Providers
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

/**
 * Creates an error response that includes an entity body describing the error condition.
 * The entity conforms to the XHTML Basic 1.1 schema.
 *
 * @see <a href="http://www.w3.org/TR/xhtml-basic/">XHTML Basic (W3C Recommendation)</a>
 */
public class ErrorResponseBuilder {

	/**
	 * Builds a response message that indicates some kind of error has occurred. The error
	 * message is included as the content of the xhtml:body/xhtml:p element.
	 * @param statusCode The relevant HTTP error code (4xx, 5xx).
	 * @param msg A brief description of the error condition.
	 * @return A <code>Response</code> instance containing an XHTML entity.
	 */
	public Response buildErrorResponse(int statusCode, String msg) {
		ResponseBuilder rspBuilder = Response.status(statusCode);
		rspBuilder.type("application/xhtml+xml; charset=UTF-8");
		rspBuilder.entity(createErrorEntityAsString(statusCode, msg));
		return rspBuilder.build();
	}

	private String createErrorEntityAsString(int statusCode, String msg) {
		StringBuilder doc = new StringBuilder();
		doc.append("<?xml version='1.0' encoding='UTF-8'?>\n");
		doc.append(
				"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML Basic 1.1//EN' 'http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd'>\n");
		doc.append("<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en'>\n");
		doc.append("<head><title>").append("Status Code ").append(statusCode);
		doc.append("</title></head>\n");
		doc.append("<body><p>").append(msg).append("</p></body>\n");
		doc.append("</html>");
		return doc.toString();
	}

}
