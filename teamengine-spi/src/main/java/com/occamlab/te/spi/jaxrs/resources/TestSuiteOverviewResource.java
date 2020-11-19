/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.jaxrs.resources;

import java.io.InputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import com.occamlab.te.spi.jaxrs.ErrorResponseBuilder;
import com.occamlab.te.spi.jaxrs.TestSuiteController;
import com.occamlab.te.spi.jaxrs.TestSuiteRegistry;

/**
 * A document resource that provides an overview of the executable test suite
 * (ETS) and guidance about how to run the tests.
 */
@Path("suites/{etsCode}/")
public class TestSuiteOverviewResource {

    /**
     * Returns a description of the test suite. The representation is a
     * "polyglot" HTML5 document that is also a well-formed XML document.
     *
     * @param etsCode
     *            A code denoting the relevant ETS.
     * @return An InputStream to read the summary document from the classpath (
     *         <code>/doc/{etsCode}/{etsVersion}/overview.html</code>).
     *
     * @see <a href="http://www.w3.org/TR/html-polyglot/">Polyglot Markup:
     *      HTML-Compatible XHTML Documents</a>
     */
    @GET
    @Produces("text/html; charset='utf-8'")
    public InputStream getTestSuiteDescription( @PathParam("etsCode") String etsCode ) {
        StringBuilder docPath = createPathToDoc( etsCode );
        InputStream atsStream = this.getClass().getResourceAsStream( docPath.toString() );
        if ( null == atsStream ) {
            ErrorResponseBuilder builder = new ErrorResponseBuilder();
            Response rsp = builder.buildErrorResponse( 404, "Test suite overview not found." );
            throw new WebApplicationException( rsp );
        }
        return atsStream;
    }

    private StringBuilder createPathToDoc( String etsCode ) {
        StringBuilder docPath = new StringBuilder();
        docPath.append( "/doc/" );
        docPath.append( etsCode );
        docPath.append( "/" );
        docPath.append( findVersion( etsCode ) );
        docPath.append( "/overview.html" );
        return docPath;
    }

    private String findVersion(String code )
            throws WebApplicationException {
        TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
        TestSuiteController controller = registry.getController( code);
        if ( null == controller ) {
            throw new WebApplicationException( 404 );
        }
        return controller.getVersion();
    }

}
