package com.occamlab.te.parsers;

import java.io.PrintWriter;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class XSLTransformationErrorHandler implements ErrorListener {
	PrintWriter logger;
	boolean ignoreErrors;
	boolean ignoreWarnings;
	int errorCount;
	int warningCount;
	
	public XSLTransformationErrorHandler(PrintWriter logger, boolean ignoreErrors, boolean ignoreWarnings) {
		this.logger = logger;
		this.ignoreErrors = ignoreErrors;
		this.ignoreWarnings = ignoreWarnings;
	}

	@Override
	public void error(TransformerException e) throws TransformerException {
		if (!ignoreErrors) {
			logger.println("Error: " + e.getMessageAndLocation());
		}
		errorCount++;
	}

	@Override
	public void fatalError(TransformerException e) throws TransformerException {
		logger.println("Fatal Error: " + e.getMessageAndLocation());
	}

	@Override
	public void warning(TransformerException e)	throws TransformerException {
		if (!ignoreWarnings) {
			logger.println("Warning: " + e.getMessageAndLocation());
		}
		warningCount++;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public int getWarningCount() {
		return warningCount;
	}
}
