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

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Dynamically searches for root resource and provider classes. An instance of this class
 * defines the components of a JAX-RS application and is used to configure a runtime
 * environment such as a web container.
 *
 * @see org.glassfish.jersey.server.ResourceConfig
 */
public class ApplicationComponents extends ResourceConfig {

	/**
	 * Scans for root resource classes in the
	 * <code>com.occamlab.te.spi.jaxrs.resources</code> package (or a sub-package).
	 */
	public ApplicationComponents() {
		packages("com.occamlab.te.spi.jaxrs.resources");
		property("jersey.config.server.redirect.servlet.extension", "false");
	}

}
