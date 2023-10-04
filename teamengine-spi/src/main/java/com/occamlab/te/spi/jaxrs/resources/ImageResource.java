/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.jaxrs.resources;

import java.io.InputStream;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import com.occamlab.te.spi.jaxrs.ErrorResponseBuilder;

/**
 * An image resource referenced by a test suite description. The expected image
 * media type is "image/png".
 */
@Path("suites/{etsCode}/{etsVersion}/resources/{image}")
@Produces("image/png")
public class ImageResource {

    /**
     * Returns an image resource from this classpath location:
     * <code>/doc/{etsCode}/{etsVersion}/resources/{image}</code>
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
                .append("/resources/").append(image);
        InputStream imgStream = this.getClass().getResourceAsStream(
                imgClassPath.toString());
        if (null == imgStream) {
            ErrorResponseBuilder builder = new ErrorResponseBuilder();
            Response rsp = builder.buildErrorResponse(404,
                    "Image resource not found.");
            throw new WebApplicationException(rsp);
        }
        return Response.ok(imgStream, "image/png").build();
    }
}
