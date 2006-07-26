package com.occamlab.te;

import org.xml.sax.*;
import java.io.PrintWriter;

public class ErrorHandlerImpl implements ErrorHandler {
	private int ErrorCount = 0;
	private int WarningCount = 0;
	private PrintWriter Logger;
	private String Prefix = "";
	
	public ErrorHandlerImpl() {
		this(null, new PrintWriter(System.out));
	}
	
	public ErrorHandlerImpl(String role, PrintWriter logger) {
		setRole(role);
		setLogger(logger);
	}
	
	public void setRole(String role) {
		if (role != null) {
		  Prefix = role + " ";
	  }
	}
	
	public void setLogger(PrintWriter logger) {
		Logger = logger;
	}

  public String getErrorCounts() {
    String msg = "";
    if (ErrorCount > 0 || WarningCount > 0) {
      if (ErrorCount > 0) {
        msg += ErrorCount + " error" + (ErrorCount == 1 ? "" : "s");
        if (WarningCount > 0) msg += " and ";
      }
      if (WarningCount > 0) {
        msg += WarningCount + " warning" + (WarningCount == 1 ? "" : "s");
      }
    } else {
      msg = "No errors or warnings";
    }
    msg += " detected.";
    return msg;
  }

  private void error(String type, SAXParseException e) {
		Logger.print(type);
    if (e.getLineNumber() >= 0) {
      Logger.print(" at line " + e.getLineNumber());
      if (e.getColumnNumber() >= 0) {
        Logger.print(", column " + e.getColumnNumber());
      }
      if (e.getSystemId() != null) {
        Logger.print(" of " + e.getSystemId());
      }
    } else {
      if (e.getSystemId() != null) {
        Logger.print(" in " + e.getSystemId());
      }
    }
    Logger.println(":");
		Logger.println("  " + e.getMessage());
		Logger.flush();
//		System.err.println(type + " at line " +  + e.getLineNumber() + ", column " + e.getColumnNumber() + " of " + e.getSystemId() + ":");
//		System.err.println("  " + e.getMessage());
	}
	
	public int getErrorCount() {
		return ErrorCount;
	}

	public int getWarningCount() {
		return WarningCount;
	}

	public void error(SAXParseException exception) {
		error(Prefix + "error", exception);
    ErrorCount++;
	}
	public void fatalError(SAXParseException exception) {
		error("Fatal " + Prefix + "error", exception);
    ErrorCount++;
  }
	public void warning(SAXParseException exception) {
		error(Prefix + "warning", exception);
    WarningCount++;
  }
}
