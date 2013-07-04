package com.occamlab.te.spi.executors.testng;

import java.util.List;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ReporterConfig;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.XMLReporterConfig;
import org.testng.xml.XmlSuite;

/**
 * A basic XML reporter that suppresses stack traces and writes the test results
 * to a single file (testng-results.xml) in the specified output directory.
 * 
 * @see <a
 *      href="http://testng.org/doc/documentation-main.html#logging-xml-reports">
 *      TestNG documentation, 6.2.5</a>
 */
public final class BasicXMLReporter implements IReporter {

    private XMLReporter reporter;

    public BasicXMLReporter() {
        this.reporter = createCustomXMLReporter();
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
            String outputDirectory) {
        this.reporter.generateReport(xmlSuites, suites, outputDirectory);
    }

    /**
     * Creates an XML reporter that suppresses stack traces and includes test
     * result attributes (if set).
     * 
     * @return A customized reporter that generates an XML representation of the
     *         test results. The document element is &lt;testng-results&gt;.
     */
    XMLReporter createCustomXMLReporter() {
        // config data syntax: "class-name:prop1=val1,prop2=val2"
        StringBuilder reporterConfig = new StringBuilder(
                XMLReporter.class.getName() + ":");
        reporterConfig.append("stackTraceOutputMethod=").append(
                XMLReporterConfig.STACKTRACE_NONE);
        reporterConfig.append(",").append("splitClassAndPackageNames=true");
        reporterConfig.append(",").append("generateTestResultAttributes=true");
        reporterConfig.append(",").append("generateGroupsAttribute=true");
        ReporterConfig reporterConf = ReporterConfig.deserialize(reporterConfig
                .toString());
        return (XMLReporter) reporterConf.newReporterInstance();
    }
}
