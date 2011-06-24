package com.occamlab.te;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;

public class ConfigFileBuilder {

    public static void main(String[] args) throws Exception {
        String home = "";
        String scriptsDir = "webapps/teamengine/WEB-INF/scripts";
        String workDir = "webapps/teamengine/WEB-INF/work";
        String usersDir = "webapps/teamengine/WEB-INF/users";
        String resourcesDir = null;
        String defaultRevision = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());

        // Parse arguments from command-line
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-home=")) {
                home = args[i].substring(6);
            } else if (args[i].startsWith("-scriptsdir=")) {
                scriptsDir = args[i].substring(12);
            } else if (args[i].startsWith("-resourcesdir=")) {
                scriptsDir = args[i].substring(14);
            } else if (args[i].startsWith("-usersdir=")) {
                usersDir = args[i].substring(10);
            } else if (args[i].startsWith("-workdir=")) {
                workDir = args[i].substring(9);
            } else if (args[i].startsWith("-defaultrev=")) {
                defaultRevision = args[i].substring(12);
            }
        }
        
        if (resourcesDir == null) {
            resourcesDir = scriptsDir;
        }
        
        SortedSet<ConfigEntry> configs = new TreeSet<ConfigEntry>(new ConfigComparator());
        
        File[] scriptDirs = new File(".").listFiles();
        for (File dir : scriptDirs) {
            File file = new File(dir, "config.xml");
            if (file.canRead()) {
                ConfigEntry config = new ConfigEntry(file); 
                File profilesDir = new File(dir, "profiles");
                if (profilesDir.isDirectory()) {
                    File[] profileDirs = profilesDir.listFiles();
                    for (File pdir : profileDirs) {
                        File pfile = new File(pdir, "config.xml");
                        if (pfile.canRead()) {
                            config.add(new ConfigEntry(pfile));
                        }
                    }
                }
                configs.add(config);
            }
        }

        System.out.println("<config>");
        System.out.println("  <home>" + home + "</home>");
        System.out.println("  <scriptsdir>" + scriptsDir + "</scriptsdir>");
        System.out.println("  <resourcesdir>" + resourcesDir + "</resourcesdir>");
        System.out.println("  <usersdir>" + usersDir + "</usersdir>");
        System.out.println("  <workdir>" + workDir + "</workdir>");
        System.out.println("  <scripts>");
        
        Iterator<ConfigEntry> it = configs.iterator();
        ConfigEntry config = it.hasNext() ? it.next() : null;
        while (config != null) {
            String organization = config.organization;
            System.out.println("    <organization>");
            System.out.println("      <name>" + organization + "</name>");
            while (config != null && config.organization.equals(organization)) {
                String standard = config.standard;
                System.out.println("      <standard>");
                System.out.println("        <name>" + standard + "</name>");
                while (config != null && config.organization.equals(organization) && config.standard.equals(standard)) {
                    String version = config.version;
                    System.out.println("        <version>");
                    System.out.println("          <name>" + version + "</name>");
                    System.out.println("          <suite>");
                    System.out.println("            <namespace-uri>" + config.suite.getNamespaceURI() + "</namespace-uri>");
                    System.out.println("            <prefix>" + config.suite.getPrefix() + "</prefix>");
                    System.out.println("            <local-name>" + config.suite.getLocalPart() + "</local-name>");
                    String title = config.title;
                    if (title == null) {
                        title = organization + " " + standard + " " + version + " Test Suite";
                    }
                    System.out.println("            <title>" + title + "</title>");
                    if (config.description != null) {
                        System.out.println("            <description>" + config.description + "</description>");
                    }
                    if (config.link != null) {
                        System.out.println("            <link>" + config.link + "</link>");
                    }
                    if (config.dataLink != null) {
                        System.out.println("            <link type=\"data\">" + config.dataLink + "</link>");
                    }
                    System.out.println("          </suite>");
                    while (config != null && config.organization.equals(organization) && config.standard.equals(standard) && config.version.equals(version)) {
                        List<QName> profiles = new ArrayList<QName>();
                        List<String> profileTitles = new ArrayList<String>();
                        List<String> profileDescriptions = new ArrayList<String>();
                        List<File> sources = new ArrayList<File>();
                        while (config != null && config.suite == null) {
                            profiles.addAll(config.profiles);
                            profileTitles.addAll(config.profileTitles);
                            profileDescriptions.addAll(config.profileDescriptions);
                            sources.addAll(config.sources);
                            config = it.hasNext() ? it.next() : null;
                        }
                        if (config != null) {
                            profiles.addAll(config.profiles);
                            profileTitles.addAll(config.profileTitles);
                            profileDescriptions.addAll(config.profileDescriptions);
                            sources.addAll(config.sources);
                            String revision = config.revision;
                            if (revision == null) {
                                revision = defaultRevision;
                            }
                            System.out.println("          <revision>");
                            System.out.println("            <name>" + revision + "</name>");
                            System.out.println("            <sources>");
                            for (File source : sources) {
                                String path = source.getPath().substring(2).replace('\\', '/');
                                System.out.println("              <source>" + path + "</source>");
                            }
                            System.out.println("            </sources>");
                            if (config.resources != null) {
                                String path = config.resources.getPath().substring(2).replace('\\', '/');
                                System.out.println("            <resources>" + path + "</resources>");
                            }
                            if (config.webdir != null) {
                                System.out.println("            <webdir>" + config.webdir + "</webdir>");
                            }
                            for (int i = 0; i < profiles.size(); i++) {
                                System.out.println("            <profile>");
                                System.out.println("              <namespace-uri>" + profiles.get(i).getNamespaceURI() + "</namespace-uri>");
                                System.out.println("              <prefix>" + profiles.get(i).getPrefix() + "</prefix>");
                                System.out.println("              <local-name>" + profiles.get(i).getLocalPart() + "</local-name>");
                                String profileTitle = profileTitles.get(i);
                                if (profileTitle == null) profileTitle = profiles.get(i).getLocalPart();
                                System.out.println("              <title>" + profileTitle + "</title>");
                                String profileDescription = profileDescriptions.get(i);
                                if (profileDescription != null) {
                                    System.out.println("              <description>" + profileDescription + "</description>");
                                }
                                System.out.println("            </profile>");
                            }
                            System.out.println("          </revision>");
                            config = it.hasNext() ? it.next() : null;
                        }
                    }
                    System.out.println("        </version>");
                }
                System.out.println("      </standard>");
            }
            System.out.println("    </organization>");
        }
        System.out.println("  </scripts>");
        System.out.println("</config>");
    }
}
