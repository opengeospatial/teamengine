<?xml version="1.0" encoding="UTF-8"?>
<ctl:package xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:tns="http://www.opengis.net/cite/cat30"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:tec="java:com.occamlab.te.TECore"
  xmlns:tng="java:org.opengis.cite.cat30.TestNGController">

  <ctl:function name="tns:run-ets-cat30">
    <ctl:param name="testRunArgs">A Document node containing test run arguments (as XML properties).</ctl:param>
    <ctl:param name="outputDir">The directory in which the test results will be written.</ctl:param>
    <ctl:return>The test results as a Source object (root node).</ctl:return>
    <ctl:description>Runs the ets-cat30-0.4 test suite.</ctl:description>
    <ctl:code>
      <xsl:variable name="controller" select="tng:new($outputDir)" />
      <xsl:copy-of select="tng:doTestRun($controller, $testRunArgs)" />
    </ctl:code>
  </ctl:function>

  <ctl:suite name="tns:ets-cat30-0.4">
    <ctl:title>OGC Catalogue 3.0 Conformance Test Suite</ctl:title>
    <ctl:description>Describe scope of testing.</ctl:description>
    <ctl:starting-test>tns:Main</ctl:starting-test>
  </ctl:suite>

  <ctl:test name="tns:Main">
    <ctl:assertion>The test subject satisfies all applicable constraints.</ctl:assertion>
    <ctl:code>
      <xsl:variable name="form-data">
        <ctl:form method="POST" width="800" height="600" xmlns="http://www.w3.org/1999/xhtml">
          <h2>OGC Catalogue 3.0 Conformance Test Suite</h2>
          <div style="background:#F0F8FF" bgcolor="#F0F8FF">
            <p>The behavior of the implementation under test is checked 
               against the following specifications:</p>
            <ul>
              <li><a target="_blank" href="https://portal.opengeospatial.org/files/?artifact_id=61521&amp;version=1">
                 OGC 12-176r5</a> OGC Catalogue Services 3.0 Specification - HTTP Protocol Binding</li>
              <li><a target="_blank" href="https://portal.opengeospatial.org/files/?artifact_id=61520&amp;version=1">
                 OGC 14-014r3</a> OGC Catalogue Services 3.0 Specification - HTTP Protocol 
                 Binding - Abstract Test Suite</li>
              <li><a target="_blank" href="https://portal.opengeospatial.org/files/?artifact_id=56866&amp;version=2">
                 OGC 10-032r8</a> OGC OpenSearch Geo and Time Extensions, Version 1.0</li>
              <li><a target="_blank" href="http://www.opensearch.org/Specifications/OpenSearch/1.1">OpenSearch</a>
                 OpenSearch 1.1, Draft 5</li>
              <li><a target="_blank" href="http://tools.ietf.org/html/rfc4287">RFC 4287</a>
                 The Atom Syndication Format</li>
            </ul>
            <p>The test suite currently covers the following conformance classes:</p>
            <ul>
              <li>Basic-Catalogue (mandatory)</li>
              <li>OpenSearch</li>
            </ul>
          </div>
          <fieldset style="background:#ccffff">
            <legend style="font-family: sans-serif; color: #000099; 
			                 background-color:#F0F8FF; border-style: solid; 
                       border-width: medium; padding:4px">Implementation under test</legend>
            <p>
              <label for="uri">
                <h4 style="margin-bottom: 0.5em">Location of capabilities document (absolute http: or file: URI)</h4>
              </label>
              <input id="uri" name="uri" size="128" type="text" value="" />
            </p>
            <p>
              <label for="doc">
                <h4 style="margin-bottom: 0.5em">Upload capabilities document</h4>
              </label>
              <input name="doc" id="doc" size="128" type="file" />
            </p>
          </fieldset>
          <p><strong>Note:</strong> The content of the capabilities document 
             determines which tests will be run. If an optional conformance class 
             is not supported then the applicable tests will be skipped.
          </p>
          <p>
            <input class="form-button" type="submit" value="Start"/> | 
            <input class="form-button" type="reset" value="Clear"/>
          </p>
        </ctl:form>
      </xsl:variable>
      <xsl:variable name="iut-file" select="$form-data//value[@key='doc']/ctl:file-entry/@full-path" />
      <xsl:variable name="test-run-props">
        <properties version="1.0">
          <entry key="iut">
            <xsl:choose>
              <xsl:when test="empty($iut-file)">
                <xsl:value-of select="normalize-space($form-data/values/value[@key='uri'])"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:copy-of select="concat('file:///', $iut-file)" />
              </xsl:otherwise>
            </xsl:choose>
          </entry>
          <entry key="ics"><xsl:value-of select="$form-data/values/value[@key='level']"/></entry>
        </properties>
      </xsl:variable>
      <xsl:variable name="testRunDir">
        <xsl:value-of select="tec:getTestRunDirectory($te:core)"/>
      </xsl:variable>
      <xsl:variable name="test-results">
        <ctl:call-function name="tns:run-ets-cat30">
          <ctl:with-param name="testRunArgs" select="$test-run-props"/>
          <ctl:with-param name="outputDir" select="$testRunDir" />
        </ctl:call-function>
      </xsl:variable>
      <xsl:call-template name="tns:testng-report">
        <xsl:with-param name="results" select="$test-results" />
        <xsl:with-param name="outputDir" select="$testRunDir" />
      </xsl:call-template>
      <xsl:variable name="summary-xsl" select="tec:findXMLResource($te:core, '/testng-summary.xsl')" />
      <ctl:message>
        <xsl:value-of select="saxon:transform(saxon:compile-stylesheet($summary-xsl), $test-results)"/>
See detailed test report in the TE_BASE/users/<xsl:value-of 
select="concat(substring-after($testRunDir, 'users/'), '/html/')" /> directory.
      </ctl:message>
      <xsl:if test="xs:integer($test-results/testng-results/@failed) gt 0">
        <xsl:for-each select="$test-results//test-method[@status='FAIL' and not(@is-config='true')]">
          <ctl:message>
Test method <xsl:value-of select="./@name"/>: <xsl:value-of select=".//message"/>
          </ctl:message>
        </xsl:for-each>
        <ctl:fail />
      </xsl:if>
      <xsl:if test="xs:integer($test-results/testng-results/@skipped) eq xs:integer($test-results/testng-results/@total)">
        <ctl:message>All tests were skipped. One or more preconditions were not satisfied.</ctl:message>
        <xsl:for-each select="$test-results//test-method[@status='FAIL' and @is-config='true']">
          <ctl:message>
            <xsl:value-of select="./@name"/>: <xsl:value-of select=".//message"/>
          </ctl:message>
        </xsl:for-each>
        <ctl:skipped />
      </xsl:if>
    </ctl:code>
  </ctl:test>

  <xsl:template name="tns:testng-report">
    <xsl:param name="results" />
    <xsl:param name="outputDir" />
    <xsl:variable name="stylesheet" select="tec:findXMLResource($te:core, '/testng-report.xsl')" />
    <xsl:variable name="reporter" select="saxon:compile-stylesheet($stylesheet)" />
    <xsl:variable name="report-params" as="node()*">
      <xsl:element name="testNgXslt.outputDir">
        <xsl:value-of select="concat($outputDir, '/html')" />
      </xsl:element>
    </xsl:variable>
    <xsl:copy-of select="saxon:transform($reporter, $results, $report-params)" />
  </xsl:template>
</ctl:package>
