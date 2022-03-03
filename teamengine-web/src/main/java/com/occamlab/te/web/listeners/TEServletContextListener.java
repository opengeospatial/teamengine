/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.web.listeners;

import com.occamlab.te.SetupOptions;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import java.io.File;

/**
 * Context listener that initializes properties to be used application wide.
 * <p>
 * Currently sets the 'teConfigFile' parameter pointing to the main config.xml
 * configuration file.
 * </p>
 */
@WebListener
public class TEServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setAttribute("teConfigFile",
                SetupOptions.getBaseConfigDirectory().getAbsolutePath() + File.separator + "config.xml");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

    }
}
