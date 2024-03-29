/************************************************************
 *
 * Contributor(s):
 *     C. Heazel (WiSC): Moved vocabulary package from spi to core
 *
 ************************************************************/
package com.occamlab.te.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * Provides an RDF vocabulary to represent the content of resource representations. The
 * dct:hasFormat and dct:isFormatOf properties may be used to interrelate alternative
 * representations.
 *
 * @see <a href="https://www.w3.org/TR/Content-in-RDF10/" target= "_blank">Representing
 * Content in RDF 1.0</a>
 */
public class CONTENT {

	/** Model that holds the Content vocabulary terms. */
	private static final Model model = ModelFactory.createDefaultModel();

	public static final String NS_URI = "http://www.w3.org/2011/content#";

	public static final Property characterEncoding = model.createProperty(NS_URI + "characterEncoding");

	public static final Resource ContentAsBase64 = model.createResource(NS_URI + "ContentAsBase64");

	/** The Base64 encoded byte sequence (xsd:base64Binary). */
	public static final Property bytes = model.createProperty(NS_URI + "bytes");

	public static final Resource ContentAsText = model.createResource(NS_URI + "ContentAsText");

	/** The character sequence (xsd:string). */
	public static final Property chars = model.createProperty(NS_URI + "chars");

	public static final Resource ContentAsXML = model.createResource(NS_URI + "ContentAsXML");

	/** The XML content following the prolog (document element). */
	public static final Property rest = model.createProperty(NS_URI + "rest");

}
