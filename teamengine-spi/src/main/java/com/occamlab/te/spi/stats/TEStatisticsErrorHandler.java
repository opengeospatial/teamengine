package com.occamlab.te.spi.stats;

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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TEStatisticsErrorHandler implements ErrorHandler {

	static Logger logger = Logger.getLogger(TEStatisticsErrorHandler.class.getName());

	public void warning(SAXParseException e) throws SAXException {
		logger.log(Level.SEVERE, e.getMessage());
	}

	public void error(SAXParseException e) throws SAXException {
		logger.log(Level.SEVERE, e.getMessage());
	}

	public void fatalError(SAXParseException e) throws SAXException {
		logger.log(Level.SEVERE, e.getMessage());
	}

}
