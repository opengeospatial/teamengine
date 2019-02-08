package com.occamlab.te.spi.jaxrs;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Dynamically searches for root resource and provider classes. An instance of
 * this class defines the components of a JAX-RS application and is used to
 * configure a runtime environment such as a web container.
 * 
 * @see org.glassfish.jersey.server.ResourceConfig
 * @see javax.ws.rs.core.Application
 */

@ApplicationPath("rest")
public class ApplicationComponents extends ResourceConfig {

    /**
     * Default constructor scans for root resource classes in the
     * <code>com.occamlab.te.spi.jaxrs.resources</code> package (or a
     * sub-package).
     */
    public ApplicationComponents() {
   // Register resources and providers using package-scanning.
      packages("com.occamlab.te.spi.jaxrs.resources");
    }

}
