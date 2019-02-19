package com.occamlab.te.spi.jaxrs;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Dynamically searches for root resource and provider classes. An instance of
 * this class defines the components of a JAX-RS application and is used to
 * configure a runtime environment such as a web container.
 * 
 * @see org.glassfish.jersey.server.ResourceConfig
 * @see javax.ws.rs.core.Application
 */

public class ApplicationComponents extends ResourceConfig {

    /**
     * Default constructor scans for root resource classes in the
     * <code>com.occamlab.te.spi.jaxrs.resources</code> package (or a
     * sub-package).
     */
    public ApplicationComponents() {
   // Register resources and providers using package-scanning.
      packages(true, "com.occamlab.te.spi.jaxrs.resources");
      register(MultiPartFeature.class);
    }

}
