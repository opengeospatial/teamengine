/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): No additional contributors to date
 */

package com.occamlab.te;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;

/**
 * Provides static configuration settings. The {@code TE_BASE} system property
 * or environment variable specifies the location of the main configuration
 * directory that contains several essential sub-directories.
 * 
 * <p>
 * 
 * <pre>
 * TE_BASE
 *  |-- config.xml
 *  |-- resources/
 *  |-- scripts/
 *  |-- work/
 *  +-- users/
 *      |-- {username1}/
 *      +-- {usernameN}/
 * </pre>
 * 
 * </p>
 * 
 */
public class SetupOptions {
    public static final String TE_BASE = "TE_BASE";
    private static File teBaseDir = getBaseConfigDirectory();
    boolean validate = true;
    boolean preload = false;
    File workDir = null;
    String sourcesName = "default";
    ArrayList<File> sources = new ArrayList<File>();

    /**
     * Default constructor. Creates the TE_BASE/scripts directory if it does not
     * exist.
     */
    public SetupOptions() {
        File scriptsDir = new File(teBaseDir, "scripts");
        if (!scriptsDir.exists() && !scriptsDir.mkdirs()) {
            throw new RuntimeException("Failed to create directory at "
                    + scriptsDir.getAbsolutePath());
        }
    }

    /**
     * Determines the location of the TE_BASE directory by looking for either 1)
     * a system property or 2) an environment variable named {@value #TE_BASE}.
     * Finally, if neither is set then the "teamengine" subdirectory is created
     * in the user home directory (${user.home}/teamengine).
     * 
     * @return A File denoting the location of the base configuration directory.
     */
    public static File getBaseConfigDirectory() {
        if (null != teBaseDir) {
            return teBaseDir;
        }
        String basePath = System.getProperty(TE_BASE);
        if (null == basePath) {
            basePath = System.getenv(TE_BASE);
        }
        if (null == basePath) {
            basePath = System.getProperty("user.home")
                    + System.getProperty("file.separator") + "teamengine";
        }
        File baseDir = new File(basePath);
        if (!baseDir.isDirectory()) {
            baseDir.mkdirs();
        }
        Logger.getLogger(SetupOptions.class.getName()).log(Level.CONFIG,
                "Using TE_BASE at " + baseDir);
        return baseDir;
    }

    public String getSourcesName() {
        return sourcesName;
    }

    public void setSourcesName(String sourcesName) {
        this.sourcesName = sourcesName;
    }

    /**
     * Returns the location of the work directory (TE_BASE/work).
     * 
     * @return A File denoting a directory location; it is created if it does
     *         not exist.
     */
    public File getWorkDir() {
        if (null == this.workDir) {
            File dir = new File(teBaseDir, "work");
            if (!dir.exists() && !dir.mkdir()) {
                throw new RuntimeException("Failed to create directory at "
                        + dir.getAbsolutePath());
            }
            this.workDir = dir;
        }
        return workDir;
    }

    /**
     * Returns a list of file system resources (directories and files)
     * containing CTL test scripts.
     * 
     * @return A List containing one or more File references (TE_BASE/scripts is
     *         the default location).
     */
    public List<File> getSources() {
        return sources;
    }

    /**
     * Adds a file system resource to the collection of known scripts.
     * 
     * @param source
     *            A File object representing a file or directory.
     */
    public void addSource(File source) {
        this.sources.add(source);
    }

    public Element getParamsElement() {
        return null;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public boolean isPreload() {
        return preload;
    }

    public void setPreload(boolean preload) {
        this.preload = preload;
    }
}
