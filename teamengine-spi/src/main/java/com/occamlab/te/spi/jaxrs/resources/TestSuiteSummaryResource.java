package com.occamlab.te.spi.jaxrs.resources;

import java.io.InputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import com.occamlab.te.spi.jaxrs.ErrorResponseBuilder;

/**
 * A document resource that provides a summary of the executable test suite
 * (ETS) and guidance about how to run the tests.
 */
@Path("suites/{etsCode}/{etsVersion}/")
public class TestSuiteSummaryResource {

    /**
     * Returns a description of the test suite. The representation is a
     * "polyglot" HTML5 document that is also a well-formed XML document.
     * 
     * @param etsCode
     *            A code denoting the relevant ETS.
     * @param etsVersion
     *            A version identifier.
     * @return An InputStream to read the summary document from the classpath (
     *         <code>/doc/{etsCode}/{etsVersion}/index.html</code>).
     * 
     * @see <a href="http://www.w3.org/TR/html-polyglot/">Polyglot Markup:
     *      HTML-Compatible XHTML Documents</a>
     */
    @GET
    @Produces("text/html; charset='utf-8'")
    public InputStream getTestSuiteDescription(
            @PathParam("etsCode") String etsCode,
            @PathParam("etsVersion") String etsVersion) {

        StringBuilder atsPath = new StringBuilder("/doc/");
        atsPath.append(etsCode).append("/").append(etsVersion)
                .append("/index.html");
        InputStream atsStream = this.getClass().getResourceAsStream(
                atsPath.toString());
        if (null == atsStream) {
            ErrorResponseBuilder builder = new ErrorResponseBuilder();
            Response rsp = builder.buildErrorResponse(404,
                    "Test suite description not found.");
            throw new WebApplicationException(rsp);
        }
        return atsStream;
    }
}
