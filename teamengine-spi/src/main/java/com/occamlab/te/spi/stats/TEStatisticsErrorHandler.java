package com.occamlab.te.spi.stats;

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
