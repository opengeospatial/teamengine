package com.occamlab.te.spi.jaxrs.resources;

import java.io.InputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * An image resource referenced by a test suite description. The expected image
 * media type is "image/png".
 */
@Path("suites/{etsCode}/{etsVersion}/img/{image}")
@Produces("image/png")
public class ImageResource {

    /**
     * Returns an image resource from this classpath location:
     * <code>/doc/{etsCode}/{etsVersion}/img/{image}</code>
     * 
     * @param etsCode
     *            The test suite code.
     * @param etsVersion
     *            The test suite version.
     * @param image
     *            The name of the image resource (e.g. figure-1.png).
     * @return An InputStream for reading the image resource from the classpath.
     */
    @GET
    public Response getImage(@PathParam("etsCode") String etsCode,
            @PathParam("etsVersion") String etsVersion,
            @PathParam("image") String image) {
        StringBuilder imgClassPath = new StringBuilder("/doc/");
        imgClassPath.append(etsCode).append("/").append(etsVersion)
                .append("/img/").append(image);
        System.out.println("getImage from " + imgClassPath.toString());
        InputStream imgStream = this.getClass().getResourceAsStream(
                imgClassPath.toString());
        if (null == imgStream) {
            throw new WebApplicationException(404);
        }
        return Response.ok(imgStream, "image/png").build();
    }
}
