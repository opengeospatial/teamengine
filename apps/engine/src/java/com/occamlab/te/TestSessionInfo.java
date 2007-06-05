package com.occamlab.te;

import java.io.File;
import java.util.List;

/**
 * Contains session-related metadata.
 *
 * @author rmartell
 */
public class TestSessionInfo {
   
    private String suiteName; 
    private String sessionId;
    private List<File> ctlFiles;
    private File logDir;
    private int mode = Test.TEST_MODE;
    private boolean validationFlag = true;
    
    
    /**
     * Default constructor for TestSessionInfo.
     */
    public TestSessionInfo() {
    }
    
    /**
     * Creates a new instance of TestSessionInfo with a common set of configuration data.
     */
    public TestSessionInfo(String suiteName, String sessionId, List<File> ctlFiles, File logDir, boolean validate) {
        this.suiteName = suiteName;
        this.sessionId = sessionId;
        this.ctlFiles = ctlFiles;
        this.logDir = logDir;
        this.validationFlag = validate;
    }
    
    /**
     * 
     * @return 
     */
    public String getSuiteName() {
        return suiteName;
    }
    
    /**
     * 
     * 
     * @param suiteName
     */
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }
    
    /**
     * 
     * @return 
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * 
     * @param sessionId 
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * Returns the list of files and directories containing CTL packages.
     *
     * @return  a List of File items.
     */
    public List<File> getSources() {
        return ctlFiles;
    }
    
    /**
     * 
     * @param sources a List<File> containing CTL sources.
     */
    public void setSources(List<File> files) {
        this.ctlFiles = files;
    }
    
    /**
     * 
     * @return 
     */
    public File getLogDir() {
        return logDir;
    }
    
    /**
     * 
     * @param logDir 
     */
    public void setLogDir(File logDir) {
        this.logDir = logDir;
    }
    
    /**
     * 
     * @return  the operational mode for the test engine (0 indicates 'test' 
     *          mode).
     */
    public int getMode() {
        return mode;
    }
    
    /**
     * 
     * @param mode  the operational mode for the test engine.
     */
    public void setMode(int mode) {
        this.mode = mode;
    }
    
    /**
     * 
     * @return true if the CTL source files will be validated; false otherwise.
     */
    public boolean hasValidationFlag() {
        return validationFlag;
    }
    
    /**
     * 
     * @param validate indicates whether CTL source files will be validated.
     */
    public void setValidationFlag(boolean validate) {
        this.validationFlag = validate;
    }
    
 /**
  * Presents a summary of test session metadata.
  *
  */
  public String toString() {
      StringBuilder result = new StringBuilder();
      final String newLine = System.getProperty("line.separator");

      result.append(this.getClass().getName() + " {");
      result.append(newLine);
      result.append("Suite name: ");
      result.append(suiteName);
      result.append(newLine);

      result.append(" Session ID: ");
      result.append(sessionId);
      result.append(newLine);

      result.append(" Log directory: ");
      result.append(logDir);
      result.append(newLine);

      result.append(" Validation flag: ");
      result.append(validationFlag);
      result.append(newLine);

      result.append(" Mode: ");
      result.append(mode);
      result.append(newLine);
      
      result.append(" CTL sources: ");
      result.append(ctlFiles);
      result.append(newLine);

      result.append("}");

      return result.toString();
  }
    
}
