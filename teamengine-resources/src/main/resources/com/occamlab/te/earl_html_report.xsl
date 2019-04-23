<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:cite="http://cite.opengeospatial.org/"
                xmlns:cnt="http://www.w3.org/2011/content#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:earl="http://www.w3.org/ns/earl#"
                xmlns:file="java:java.io.File"
                xmlns:http="http://www.w3.org/2011/http#"
                xmlns:java="http://www.java.com/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:testng="http://testng.org"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="java xs"
                version="2.0">

  <xsl:output encoding="UTF-8" indent="yes" method="html" standalone="no" omit-xml-declaration="yes" />
  <xsl:output name="text" method="text" />
  <xsl:output name="html" method="html" indent="yes" omit-xml-declaration="yes" />
  <xsl:output name="xhtml" method="xhtml" indent="yes" omit-xml-declaration="yes" />
  <xsl:param name="outputDir" />
  <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
  <xsl:variable name="htmlXslt.outputDir" select="$outputDir" />
  <xsl:variable name="tested-instace-terms" select="'iut,gml,wfs-uri,wmts,wfs,wms,capabilities-url'" />
  <xsl:variable name="tested-cc-terms" select="'ics'" />
  <xsl:function name="testng:absolutePath">
    <xsl:param name="fileName" />
    <xsl:value-of select="concat(
              'file:',
              replace(
                iri-to-uri(
                  replace(
                    replace(
                      replace(concat($htmlXslt.outputDir, '/', $fileName), '\\', '/'),
                      '%',
                      '%25'),
                    '#',
                    '%23')),
                '^([A-Za-z]):',
                '///$1:'))" />
    <!-- <xsl:value-of select="concat('file:///',$htmlXslt.outputDir, '/', $fileName)"/> -->
    <!--<xsl:value-of select="concat('file:///', $testNgXslt.outputDir, '/', $fileName)"/>-->
  </xsl:function>

  <xsl:function name="testng:htmlContentFileName">
    <xsl:param name="testName" />
    <xsl:value-of select="concat($testName, '.html')" />
  </xsl:function>

  <!-- Get URL from the test input for testDetails page. -->
  <xsl:template name="testInputs">
    <xsl:choose>
      <xsl:when test="//rdf:RDF/cite:TestRun/cite:inputs/rdf:Bag/rdf:li">
        <xsl:for-each select="//rdf:RDF/cite:TestRun/cite:inputs/rdf:Bag/rdf:li[1]">
          <xsl:value-of select="dct:description" />
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="NULL" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:function name="testng:testSuiteName">
    <xsl:param name="testTitle" />
    <xsl:param name="testName-Version" />

    <xsl:variable name="delimiters">
      <xsl:choose>
        <xsl:when test="contains($testTitle, '_')">
          <xsl:value-of select="'_'" />
        </xsl:when>
        <xsl:when
                test="contains($testTitle, '-') and not(contains($testTitle, '_'))">
          <xsl:value-of select="'-'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'_'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$testName-Version = 'title'">
      	<xsl:variable name="testTitle">
	      <xsl:choose>
	        <xsl:when test="contains($testTitle, 'SNAPSHOT')">
	          <xsl:call-template name="substring-before-after">
	            <xsl:with-param name="getString" select="'before'" />
	            <xsl:with-param name="string" select="$testTitle" />
	            <xsl:with-param name="delimiter" select="$delimiters" />
	          </xsl:call-template>
	        </xsl:when>
	        <xsl:otherwise>
	          <xsl:value-of select="$testTitle" />
	        </xsl:otherwise>
	      </xsl:choose>
	    </xsl:variable>
        <xsl:variable name="testName">
          <xsl:call-template name="substring-before-after">
            <xsl:with-param name="getString" select="'before'" />
            <xsl:with-param name="string" select="$testTitle" />
            <xsl:with-param name="delimiter" select="$delimiters" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="$testName" />
      </xsl:when>
      <xsl:when test="$testName-Version = 'version'">
        <xsl:variable name="testTitle-version">
	      <xsl:choose>
	        <xsl:when test="contains($testTitle, 'SNAPSHOT')">
		          <xsl:call-template name="substring-before-after">
		            <xsl:with-param name="getString" select="'before'" />
		            <xsl:with-param name="string" select="$testTitle" />
		            <xsl:with-param name="delimiter" select="$delimiters" />
		          </xsl:call-template>
	        </xsl:when>
	        <xsl:otherwise>
	          <xsl:value-of select="$testTitle" />
	        </xsl:otherwise>
	      </xsl:choose>
	    </xsl:variable>
        <xsl:variable name="testVersion">
          <xsl:call-template name="substring-before-after">
            <xsl:with-param name="getString" select="'after'" />
            <xsl:with-param name="string" select="$testTitle-version" />
            <xsl:with-param name="delimiter" select="$delimiters" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
	        <xsl:when test="contains($testTitle, 'SNAPSHOT')">
	          <xsl:value-of select="concat($testVersion, '-SNAPSHOT')" />
	        </xsl:when>
	        <xsl:otherwise>
	          <xsl:value-of select="$testVersion" />
	        </xsl:otherwise>
	      </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'NULL'" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:template match="/">
    <xsl:result-document href="{testng:absolutePath('index.html')}" format="xhtml">
      <html>
        <xsl:call-template name="htmlHead" />
        <body>
          <h3>
            <font color="black">
              <xsl:variable name="testTitle" select="rdf:RDF/cite:TestRun/dct:title" />

              Test Name:
              <xsl:value-of select="testng:testSuiteName($testTitle, 'title')" />
              <br />
              Test version:
              <xsl:value-of select="testng:testSuiteName($testTitle, 'version')" />
              <br />
              Time:
              <xsl:value-of select="rdf:RDF/cite:TestRun/dct:created" />
              <br />
              <br />              
              Test INPUT:
              <xsl:choose>
                <xsl:when test="rdf:RDF/cite:TestRun/cite:inputs/rdf:Bag/rdf:li">
                  <xsl:for-each select="rdf:RDF/cite:TestRun/cite:inputs/rdf:Bag/rdf:li">
                    <div style="margin-left:3.7%;">
						<xsl:variable name="test-input-title">
						<xsl:variable name="title" select="dct:title" />
							<xsl:for-each select="tokenize($tested-instace-terms,',')">
		                        <xsl:variable name="term" select="." />
								<xsl:if test="$title=$term" >
									<xsl:value-of select="'Tested Instance'" />
								</xsl:if>
							</xsl:for-each>
							<xsl:for-each select="tokenize($tested-cc-terms,',')">
		                        <xsl:variable name="term" select="." />
								<xsl:if test="$title=$term" >
									<xsl:value-of select="'Tested conformance classes'" />
								</xsl:if>
							</xsl:for-each>
					
						</xsl:variable>
						
					  <span><xsl:value-of select="if (not(empty($test-input-title)) and $test-input-title != '') then $test-input-title else dct:title" /> : </span>
                      
                      <span style="text-intent:0px"> <xsl:value-of select="dct:description" /> </span>
                    </div>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="NULL" />
                </xsl:otherwise>
              </xsl:choose>
              <br />
              Result:
              <br />
              <xsl:variable name="status">
                <xsl:choose>
                  <xsl:when
                          test="count(/rdf:RDF/cite:TestRun/cite:requirements/rdf:Seq/rdf:li/earl:TestRequirement/cite:isBasic[text() = 'true']) &gt; 0 ">
                    <xsl:variable name="core_count">
                      <xsl:for-each select="//earl:TestRequirement">
                        <xsl:if test="cite:isBasic[text() = 'true']">
                          <xsl:choose>
                            <xsl:when test="cite:testsFailed[text() !='0']">
                              <xsl:value-of select="'No'" />
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="'Yes'" />
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:if>
                      </xsl:for-each>
                    </xsl:variable>
                    <xsl:value-of select="if (contains($core_count,'No')) then 'No' else 'Yes'" />
                  </xsl:when>
                  <xsl:otherwise>
							<xsl:text>This test suite does not support evaluation of basic conformance classes. 
									  Please contact the CITE team if you have any questions regarding the basic conformance classes.</xsl:text>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <div style="text-indent:0px; float: left;width: 95%;margin-left:3.7%">
                <span>
                  <label>Passed Core conformance classes (Implementations passing these classes can be certified): </label>
                </span>
                <span style="text-intent:0px">
                  <xsl:value-of select="$status" />
                </span>
              </div>

              <div style="margin-left:3.7%">
                Number of conformance classes tested:
                <xsl:value-of select="count(//earl:TestRequirement)" />
              </div>
              <xsl:variable name="no_of_cc" select="count(//earl:TestRequirement)" />
              <input type="hidden" id="noConformanceClass" name="noConformanceClass" value="{$no_of_cc}" />

              <div style="margin-left:3.7%">
                Number of conformance classes passed:
                <xsl:value-of
                        select="count(//earl:TestRequirement[cite:testsPassed[text() &gt; '0']]/cite:testsFailed[text() = '0'])" />
              </div>
              <div style="margin-left:3.7%">
                Number of conformance classes failed:
                <xsl:value-of select="count(//earl:TestRequirement/cite:testsFailed[text() &gt;'0'])" />
              </div>
              <br />
              <!-- Pass:  <xsl:value-of select="rdf:RDF/cite:TestRun/cite:testsPassed"/> | Fail: <xsl:value-of select="rdf:RDF/cite:TestRun/cite:testsFailed"/> | Skip: <xsl:value-of select="rdf:RDF/cite:TestRun/cite:testsSkipped"/> -->
              Core conformance classes (Pass = Green; Fail = Red; Skip = Grey):
              <xsl:for-each select="/rdf:RDF/cite:TestRun/cite:requirements/rdf:Seq/rdf:li/earl:TestRequirement">
                <xsl:if test="cite:isBasic = 'true'">
                <xsl:variable name="testsInheritedFailure" select="if (cite:testsInheritedFailure >= 0) then cite:testsInheritedFailure else 0 " />
                <xsl:variable name="bgcolor" >
					<xsl:choose>
						<xsl:when test="cite:testsPassed > 0 and cite:testsFailed = 0 and $testsInheritedFailure = 0">
							 <xsl:value-of select="string('#B2F0D1')" />
						</xsl:when>
						<xsl:when test="cite:testsFailed > 0 or $testsInheritedFailure > 0">
							<xsl:value-of select="string('#FFB2B2')" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="string('#CCCCCE')" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>	
                  <div style="margin-left:3.7%">
                  	<span style="background-color:{$bgcolor};"><xsl:value-of select="dct:title" /></span>
                  </div>
                </xsl:if>
              </xsl:for-each>
            </font>
          </h3>
          <table border="1">
            <tr>
              <td>Color Legend</td>
              <td bgcolor="#B2F0D1">Pass</td>
              <td bgcolor="#FFB2B2">Fail</td>
              <td bgcolor="#CCCCCE">Skip</td>
            </tr>
          </table>
          <!--  Starting point of the Report -->
          <xsl:for-each select="/rdf:RDF/cite:TestRun/cite:requirements/rdf:Seq/rdf:li/earl:TestRequirement">
            <!-- <xsl:apply-templates select="/earl:TestRequirement" mode="test-overview" /> -->
            <xsl:call-template name="test-overview" />
          </xsl:for-each>
        </body>
      </html>
    </xsl:result-document>
    <!-- <xsl:call-template name="test-details" /> -->
  </xsl:template>
  <xsl:template name="test-overview">
    <xsl:choose>
      <xsl:when test="cite:isBasic = 'true'">
        <div style="background:#FF8C00; width:25%;">
          <h2>
            <xsl:value-of select="dct:title" />
          </h2>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <h2>
          <xsl:value-of select="dct:title" />
        </h2>
      </xsl:otherwise>
    </xsl:choose>
    <p>
      <table border="1">
        <tr>
          <td bgcolor="#B2F0D1">
            Pass:
            <xsl:element name="span">
              <xsl:attribute name="id">
                <xsl:value-of select="concat('testsPassed_',replace(dct:title, ' ', '_') )" />
              </xsl:attribute>
              <xsl:value-of select="cite:testsPassed" />
            </xsl:element>
          </td> 
          <xsl:variable name="testsInheritedFailure" select="if (cite:testsInheritedFailure >= 0) then cite:testsInheritedFailure else 0 " />
          <td bgcolor="#FFB2B2">
            Fail:
            <xsl:element name="span">
              <xsl:attribute name="id">
                <xsl:value-of select="concat('testsFailed_',replace(dct:title, ' ', '_') )" />
              </xsl:attribute>
              <xsl:value-of select="cite:testsFailed" />
            </xsl:element>
          </td>
          <xsl:if test="not($testsInheritedFailure = 0)">
          <td bgcolor="#f0d831">
            Inherited fail:
            <xsl:element name="span">
              <xsl:attribute name="id">
                <xsl:value-of select="concat('testsFailed_',replace(dct:title, ' ', '_') )" />
              </xsl:attribute>
              <xsl:value-of select=" $testsInheritedFailure" />
            </xsl:element>
          </td>
          </xsl:if>
          <td bgcolor="#CCCCCE">
            Skip:
            <xsl:element name="span">
              <xsl:attribute name="id">
                <xsl:value-of select="concat('testsSkipped_',replace(dct:title, ' ', '_') )" />
              </xsl:attribute>
              <xsl:value-of select="cite:testsSkipped" />
            </xsl:element>
          </td>
          <td>
            Total tests:
            <xsl:variable name="testsInTotal">
              <xsl:value-of select="cite:testsPassed + cite:testsFailed + $testsInheritedFailure + cite:testsSkipped" />
            </xsl:variable>
            <xsl:element name="span">
              <xsl:attribute name="id">
                <xsl:value-of select="concat('testsInTotal_',replace(dct:title, ' ', '_') )" />
              </xsl:attribute>
              <xsl:value-of select="$testsInTotal" />
            </xsl:element>
          </td>
        </tr>
      </table>
    </p>
    <xsl:call-template name="assertion_info" />
  </xsl:template>
  <!-- The assertion_info template get all the test information e.g. TestName, Reason -->
  <xsl:template name="assertion_info">
    <table border="1">
      <xsl:attribute name="id">
        <xsl:value-of select="replace(dct:title, ' ', '_')" />
      </xsl:attribute>
      <tbody>
        <tr>
          <th>Name</th>
          <th>Reason</th>
        </tr>
        <xsl:call-template name="assertion_content">
          <xsl:with-param name="context" select="." />
          <xsl:with-param name="level">0</xsl:with-param>
        </xsl:call-template>
      </tbody>
    </table>
  </xsl:template>
  <xsl:template name="assertion_content">
    <xsl:param name="context" />
    <xsl:param name="level" />
    <xsl:for-each select="$context/dct:hasPart">
      <xsl:choose>
        <xsl:when test="earl:TestCase">
          <xsl:call-template name="Assertion">
            <xsl:with-param name="testUri" select="earl:TestCase/@rdf:about" />
            <xsl:with-param name="testCase" select="earl:TestCase" />
            <xsl:with-param name="level" select="$level" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="testUri" select="@rdf:resource" />
          <xsl:call-template name="Assertion">
            <xsl:with-param name="testUri" select="$testUri" />
            <xsl:with-param name="testCase" select="//earl:TestCase[@rdf:about =$testUri]" />
            <xsl:with-param name="level" select="$level" />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  <!-- The Assertion template returns status of the result in HTML format with specific color. 'Pass: Fail: Skip:' -->
  <xsl:template name="Assertion">
    <xsl:param name="testUri" />
    <xsl:param name="testCase" />
    <xsl:param name="level" />
    <xsl:for-each select="//earl:Assertion">
      <xsl:variable name="assertion_test_uri">
        <xsl:choose>
          <xsl:when test="earl:test/earl:TestCase">
            <xsl:value-of select="earl:test/earl:TestCase/@rdf:about" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="earl:test/@rdf:resource" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="result"
                    select="translate(substring-after(earl:result/earl:TestResult/earl:outcome/@rdf:resource, '#'), $smallcase, $uppercase)" />
      <xsl:variable name="testTitle">
        <xsl:choose>
          <xsl:when test="$testCase">
            <xsl:value-of select="$testCase/dct:title" />
          </xsl:when>
          <xsl:when test="earl:test/earl:TestCase/dct:title">
            <xsl:value-of select="earl:test/earl:TestCase/dct:title" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="substring-after(earl:test/@rdf:resource, '#')" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <!-- Check and get Test description -->
      <xsl:variable name="testDescription">
        <xsl:choose>
          <xsl:when test="$testCase">
            <xsl:value-of select="$testCase/dct:description" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="earl:test/earl:TestCase/dct:description" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <!-- get Test description -->
      <xsl:if test="$testUri = $assertion_test_uri">
        <xsl:variable name="testCaseName">
          <xsl:call-template name="existHtmlPage">
            <xsl:with-param name="testCaseName" select="substring-after($testUri, '#')" />
          </xsl:call-template>
        </xsl:variable>
        <!-- Create test-details html page -->
        <xsl:call-template name="createHtmlPage">
          <xsl:with-param name="testCaseName" select="$testCaseName" />
          <xsl:with-param name="testTitle" select="$testTitle" />
          <xsl:with-param name="test_uris" select="$testUri" />
          <xsl:with-param name="testDescription" select="$testDescription" />
          <xsl:with-param name="result" select="$result" />
        </xsl:call-template>
        <xsl:variable name="indention" select="$level * 15" />
        <xsl:choose>
          <xsl:when test="$result = 'PASSED'">
            <tr bgcolor="#B2F0D1">
              <td>
                <xsl:element name="div">
                  <xsl:attribute name="style">
                    <xsl:value-of select="concat('text-indent:', $indention, 'px;')" />
                  </xsl:attribute>
                  <a href="{testng:htmlContentFileName($testCaseName)}#{$testCaseName}" class="testDetailsLink"
                     id="{testng:htmlContentFileName($testCaseName)}#{$testCaseName}">
                    <xsl:value-of select="$testTitle" />
                  </a>
                </xsl:element>
              </td>
              <td />
            </tr>
          </xsl:when>
          <xsl:when test="$result = 'FAILED' or $result = 'CANTTELL'">
            <tr bgcolor="#FFB2B2">
              <td>
                <xsl:element name="div">
                  <xsl:attribute name="style">
                    <xsl:value-of select="concat('text-indent:', $indention, 'px;')" />
                  </xsl:attribute>
                  <a href="{testng:htmlContentFileName($testCaseName)}#{$testCaseName}" class="testDetailsLink"
                     id="{testng:htmlContentFileName($testCaseName)}#{$testCaseName}">
                    <xsl:value-of select="$testTitle" />
                  </a>
                </xsl:element>
              </td>
              <td>
                <xsl:variable name="message">
                  <xsl:variable name="msg">
                    <xsl:call-template name="string-replace-all">
                      <xsl:with-param name="text" select="earl:result/earl:TestResult/dct:description" />
                      <xsl:with-param name="replace" select="'['" />
                      <xsl:with-param name="by" select="'&lt;br&gt;['" />
                    </xsl:call-template>
                  </xsl:variable>
                  <xsl:choose>
                    <xsl:when test="substring-before($msg,'expected [')">
                      <xsl:value-of select="substring-after(substring-before($msg,'expected ['), ':')" />
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="$msg" />
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <p>
                  <xsl:value-of select="$message" />
                </p>
              </td>
            </tr>
          </xsl:when>
          <xsl:when test="$result = 'INHERITEDFAILURE'">
            <tr bgcolor="#f0d831">
              <td>
                <xsl:element name="div">
                  <xsl:attribute name="style">
                    <xsl:value-of select="concat('text-indent:', $indention, 'px;')" />
                  </xsl:attribute>
                  <a href="{testng:htmlContentFileName($testCaseName)}#{$testCaseName}" class="testDetailsLink"
                     id="{testng:htmlContentFileName($testCaseName)}#{$testCaseName}">
                    <xsl:value-of select="$testTitle" />
                  </a>
                </xsl:element>
              </td>
              <td>
                <xsl:variable name="message">
                  <xsl:variable name="msg">
                    <xsl:call-template name="string-replace-all">
                      <xsl:with-param name="text" select="earl:result/earl:TestResult/dct:description" />
                      <xsl:with-param name="replace" select="'['" />
                      <xsl:with-param name="by" select="'&lt;br&gt;['" />
                    </xsl:call-template>
                  </xsl:variable>
                  <xsl:choose>
                    <xsl:when test="substring-before($msg,'expected [')">
                      <xsl:value-of select="substring-after(substring-before($msg,'expected ['), ':')" />
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="$msg" />
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <p>
                  <xsl:value-of select="$message" />
                </p>
              </td>
            </tr>
          </xsl:when>
          <xsl:when test="$result = 'UNTESTED'">
            <tr bgcolor="#CCCCCE">
              <td>
                <xsl:element name="div">
                  <xsl:attribute name="style">
                    <xsl:value-of select="concat('text-indent:', $indention, 'px;')" />
                  </xsl:attribute>
                  <a href="{testng:htmlContentFileName($testCaseName)}#{$testCaseName}" class="testDetailsLink"
                     id="{testng:htmlContentFileName($testCaseName)}#{$testCaseName}">
                    <xsl:value-of select="$testTitle" />
                  </a>
                </xsl:element>
              </td>
              <td>
                <xsl:variable name="message">
                  <xsl:variable name="msg">
                    <xsl:call-template name="string-replace-all">
                      <xsl:with-param name="text" select="earl:result/earl:TestResult/dct:description" />
                      <xsl:with-param name="replace" select="'['" />
                      <xsl:with-param name="by" select="'&lt;br&gt;['" />
                    </xsl:call-template>
                  </xsl:variable>
                  <xsl:choose>
                    <xsl:when test="substring-before($msg,'expected [')">
                      <xsl:value-of select="substring-after(substring-before($msg,'expected ['), ':')" />
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="$msg" />
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <p>
                  <xsl:value-of select="$message" />
                </p>
              </td>
            </tr>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message>Could not handle assertion with result '<xsl:value-of select="$result" />'. Name is
              '<xsl:value-of select="$testUri" />'.
            </xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$testCase">
          <xsl:call-template name="assertion_content">
            <xsl:with-param name="context" select="$testCase" />
            <xsl:with-param name="level" select="$level + 1" />
          </xsl:call-template>
        </xsl:if>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  <!-- This template replace the special charaters if occured in the exception message. -->
  <xsl:template name="string-replace-all">
    <xsl:param name="text" />
    <xsl:param name="replace" />
    <xsl:param name="by" />
    <xsl:choose>
      <xsl:when test="contains($text, $replace)">
        <xsl:choose>
          <xsl:when
                  test="not(starts-with(substring-after($text,$replace),'false')) and not(starts-with(substring-after($text,$replace),'true')) ">
            <xsl:value-of select="substring-before($text,$replace)" />
            <xsl:value-of select="$by" />
            <xsl:call-template name="string-replace-all">
              <xsl:with-param name="text" select="substring-after($text,$replace)" />
              <xsl:with-param name="replace" select="$replace" />
              <xsl:with-param name="by" select="$by" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="substring-before($text,$replace)" />
            <xsl:text>[</xsl:text>
            <xsl:call-template name="string-replace-all">
              <xsl:with-param name="text" select="substring-after($text,$replace)" />
              <xsl:with-param name="replace" select="$replace" />
              <xsl:with-param name="by" select="$by" />
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="htmlHead">
    <head>
      <meta http-equiv="content-type" content="text/html; charset=utf-8" />
      <meta http-equiv="pragma" content="no-cache" />
      <meta http-equiv="cache-control" content="max-age=0" />
      <meta http-equiv="cache-control" content="no-cache" />
      <meta http-equiv="cache-control" content="no-store" />
      <script type="text/javascript" src="js/shCore.js" />
      <script type="text/javascript" src="js/shBrushXml.js" />
      <link type="text/css" rel="stylesheet" href="styles/shCoreDefault.css" />
      <script type="text/javascript">SyntaxHighlighter.all();</script>
      <style type="text/css">.syntaxhighlighter {
        max-height:200px;
        }
        .syntaxhighlighter table {
        table-layout: fixed;
        }
      </style>
    </head>
  </xsl:template>
  <xsl:template name="test-details">
    <xsl:for-each select="/rdf:RDF/cite:TestRun/cite:requirements/rdf:Seq/rdf:li/earl:TestRequirement">
      <!-- <xsl:apply-templates select="/earl:TestRequirement" mode="test-overview" /> -->
      <xsl:for-each select="dct:hasPart">
        <xsl:variable name="test_uris">
          <xsl:choose>
            <xsl:when test="earl:TestCase">
              <xsl:value-of select="earl:TestCase/@rdf:about" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@rdf:resource" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="testCase" select="earl:TestCase" />
        <xsl:for-each select="../../../../../..//earl:Assertion">
          <xsl:variable name="assertion_test_uri">
            <xsl:choose>
              <xsl:when test="earl:test/earl:TestCase">
                <xsl:value-of select="earl:test/earl:TestCase/@rdf:about" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="earl:test/@rdf:resource" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="result" select="earl:result/earl:TestResult/earl:outcome/@rdf:resource" />
          <!-- Check and get Test Title -->
          <xsl:variable name="testTitle">
            <xsl:choose>
              <xsl:when test="$testCase">
                <xsl:value-of select="$testCase/dct:title" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="earl:test/earl:TestCase/dct:title" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <!-- get Test Title -->
          <!-- Check and get Test description -->
          <xsl:variable name="testDescription">
            <xsl:choose>
              <xsl:when test="$testCase">
                <xsl:value-of select="$testCase/dct:description" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="earl:test/earl:TestCase/dct:description" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <!-- get Test description -->
          <xsl:if test="$test_uris = $assertion_test_uri">
            <xsl:variable name="testCaseName" select="substring-after($test_uris, '#')" />
            <xsl:result-document href="{testng:absolutePath(testng:htmlContentFileName($testCaseName))}" format="xhtml">
              <html>
                <xsl:call-template name="htmlHead" />
                <body>
                  <div id="{$testCaseName}">
                    <table style="width:100%" border="1">
                      <tr>
                        <td colspan="5">
                          <h3>
                            <xsl:value-of select="$testTitle" />
                          </h3>
                        </td>
                      </tr>
                      <xsl:if test="substring-after($test_uris, '#')">
                        <tr>
                          <td>Test Name:</td>
                          <td>
                            <xsl:value-of select="$testTitle" />
                          </td>
                        </tr>
                      </xsl:if>
                      <xsl:if test="$testDescription">
                        <tr>
                          <td>Test Description:</td>
                          <td>
                            <xsl:value-of select="$testDescription" />
                          </td>
                        </tr>
                      </xsl:if>
                      <tr>
                        <td>Test Result:</td>
                        <td>
                          <xsl:value-of select="translate(substring-after($result, '#'), $smallcase, $uppercase)" />
                        </td>
                      </tr>
                      <xsl:if test="earl:result/earl:TestResult/cite:message/http:Request">
                        <tr>
                          <td colspan="5">Inputs :</td>
                        </tr>
                        <tr>
                          <td>Method:</td>
                          <td>
                            <xsl:variable name="requestMethod"
                                          select="earl:result/earl:TestResult/cite:message/http:Request/http:methodName" />
                            <xsl:value-of select="translate($requestMethod, $smallcase, $uppercase)" />
                          </td>
                        </tr>
                        <tr>
                          <td>URI:</td>
                          <td>
                            <xsl:value-of
                                    select="earl:result/earl:TestResult/cite:message/http:Request/http:requestURI" />
                          </td>
                        </tr>
                        <tr>
                          <td>Outputs :</td>
                          <td>
                            <!-- <textarea rows="20" cols="40" style="border:none;"><xsl:value-of select="earl:result/earl:TestResult/cite:message/http:Request/http:resp/http:Response/http:body/cnt:ContentAsXML/cnt:rest" /> </textarea> -->
                            <pre class="brush: xml;">
                              <xsl:copy-of
                                      select="earl:result/earl:TestResult/cite:message/http:Request/http:resp/http:Response/http:body/cnt:ContentAsXML/cnt:rest" />
                            </pre>
                          </td>
                        </tr>
                      </xsl:if>
                      <tr>
                        <td>Reason of Failure:</td>
                        <td>
                          <xsl:variable name="message">
                            <xsl:variable name="msg">
                              <xsl:call-template name="string-replace-all">
                                <xsl:with-param name="text" select="earl:result/earl:TestResult/dct:description" />
                                <xsl:with-param name="replace" select="'['" />
                                <xsl:with-param name="by" select="'&lt;br&gt;['" />
                              </xsl:call-template>
                            </xsl:variable>
                            <xsl:choose>
                              <xsl:when test="substring-before($msg,'expected [')">
                                <xsl:value-of select="substring-after(substring-before($msg,'expected ['), ':')" />
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:value-of select="$msg" />
                              </xsl:otherwise>
                            </xsl:choose>
                          </xsl:variable>
                          <p>
                            <xsl:value-of select="$message" />
                          </p>
                        </td>
                      </tr>
                    </table>
                    <br />
                  </div>
                </body>
              </html>
            </xsl:result-document>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
      <!-- dct:hasPart -->
    </xsl:for-each>
    <!--  TestRequirement -->
  </xsl:template>
  <xsl:template name="substring-before-after">
    <xsl:param name="getString" />
    <xsl:param name="string" />
    <xsl:param name="delimiter" />
    <xsl:choose>
      <xsl:when test="$getString = 'after'">
        <xsl:choose>
          <xsl:when test="contains($string, $delimiter)">
            <xsl:call-template name="substring-before-after">
              <xsl:with-param name="getString" select="$getString" />
              <xsl:with-param name="string" select="substring-after($string, $delimiter)" />
              <xsl:with-param name="delimiter" select="$delimiter" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$string" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$getString = 'before'">
        <xsl:choose>
          <xsl:when test="contains($string, $delimiter)">
            <xsl:variable name="s-tokenized" select="tokenize($string, $delimiter)" />
            <xsl:value-of
                    select="string-join(remove($s-tokenized, count($s-tokenized)),$delimiter)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$string" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- Create test-details html page -->
  <xsl:template name="createHtmlPage">
    <xsl:param name="testCaseName" />
    <xsl:param name="testTitle" />
    <xsl:param name="test_uris" />
    <xsl:param name="testDescription" />
    <xsl:param name="result" />
    <xsl:result-document href="{testng:absolutePath(testng:htmlContentFileName($testCaseName))}" format="xhtml">
      <html>
        <xsl:call-template name="htmlHead" />
        <body>
          <div id="{$testCaseName}">
            <table style="width:100%" border="1">
              <tr>
                <td colspan="5">
                  <h3>
                    <xsl:value-of select="$testTitle" />
                  </h3>
                </td>
              </tr>
              <xsl:if test="substring-after($test_uris, '#')">
                <tr>
                  <td>Test Name:</td>
                  <td>
                    <xsl:value-of select="$testTitle" />
                  </td>
                </tr>
              </xsl:if>
              <xsl:if test="$testDescription">
                <tr>
                  <td>Test Description:</td>
                  <td>
                    <xsl:value-of select="$testDescription" />

                    <xsl:if test="$result='FAILED' or $result = 'CANTTELL'">
                      <xsl:variable name="testSuiteType" select="//rdf:RDF/cite:TestRun/cite:testSuiteType" />
                      <xsl:if test=" $testSuiteType = 'testng'">
                        <xsl:variable name="testName"
                                      select="testng:testSuiteName(//rdf:RDF/cite:TestRun/dct:title, 'title')" />
                        <xsl:call-template name="link-javadoc">
                          <xsl:with-param name="ets-code" select="$testName" />
                          <xsl:with-param name="testClassPath" select="$test_uris" />
                        </xsl:call-template>
                        <hr />
                        <xsl:variable name="testClassName">
                          <xsl:call-template name="substring-before-after">
                            <xsl:with-param name="getString" select="'after'" />
                            <xsl:with-param name="string"
                                            select="substring-before($test_uris, '#')" />
                            <xsl:with-param name="delimiter" select="'/'" />
                          </xsl:call-template>
                        </xsl:variable>
                        <b>Class Name:</b>
                        <xsl:value-of select="$testClassName" />
                        <br />
                        <b>Method Name:</b>
                        <xsl:value-of select="$testCaseName" />
                        <br />
                        <b>Path:</b>
                        <xsl:value-of select="substring-before($test_uris, '#')" />
                      </xsl:if>
                    </xsl:if>
                  </td>
                </tr>
              </xsl:if>
              <tr>
                <td>Test Result:</td>
                <td>
                  <xsl:value-of select="$result" />
                </td>
              </tr>
              <xsl:for-each select="earl:result/earl:TestResult/cite:message">
                <xsl:variable name="requestMethod"
                              select="translate(http:Request/http:methodName, $smallcase, $uppercase)" />
                <xsl:variable name="input_url">
                  <xsl:call-template name="testInputs" />
                </xsl:variable>
                <xsl:variable name="requestURI" select="http:Request/http:requestURI" />
                <tr>
                  <td>Test Requests <xsl:value-of select="position()" />:
                  </td>
                  <td>
                    <table>
                      <tr>
                        <td>Method:</td>
                        <td>
                          <xsl:value-of select="$requestMethod" />
                        </td>
                      </tr>
                      <xsl:choose>
                        <xsl:when test="$requestMethod='POST'">
                          <tr>
                            <td>URL:</td>
                            <td>
                              <xsl:value-of select="substring-before($input_url, '?')" />
                            </td>
                          </tr>
                          <tr>
                            <td>Body:</td>
                            <td>
                              <pre class="brush: xml;">
                                <xsl:value-of select="$requestURI" />
                              </pre>
                            </td>
                          </tr>
                        </xsl:when>
                        <xsl:when test="starts-with($requestURI, 'http')">
                          <tr>
                            <td>URL:</td>
                            <td>
                              <xsl:value-of select="$requestURI" />
                            </td>
                          </tr>
                        </xsl:when>
                        <xsl:otherwise>
                          <tr>
                            <td>URL:</td>
                            <td>
                              <xsl:value-of select="concat(substring-before($input_url, '?'), '?' ,$requestURI)" />
                            </td>
                          </tr>
                        </xsl:otherwise>
                      </xsl:choose>
                      <tr>
                        <td>Outputs:</td>
                        <td>
                          <pre class="brush: xml;">
                            <xsl:copy-of
                                    select="http:Request/http:resp/http:Response/http:body/cnt:ContentAsXML/cnt:rest" />
                          </pre>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </xsl:for-each>
              <tr>
                <td>Reason of Failure:</td>
                <td>
                  <xsl:variable name="message">
                    <xsl:variable name="msg">
                      <xsl:call-template name="string-replace-all">
                        <xsl:with-param name="text" select="earl:result/earl:TestResult/dct:description" />
                        <xsl:with-param name="replace" select="'['" />
                        <xsl:with-param name="by" select="'&lt;br&gt;['" />
                      </xsl:call-template>
                    </xsl:variable>
                    <xsl:choose>
                      <xsl:when test="substring-before($msg,'expected [')">
                        <xsl:value-of select="substring-after(substring-before($msg,'expected ['), ':')" />
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="$msg" />
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
                  <p>
                    <xsl:value-of select="$message" />
                  </p>
                </td>
              </tr>
            </table>
            <br />
          </div>
        </body>
      </html>
    </xsl:result-document>
  </xsl:template>
  <!-- Check HTML page exist or not. -->
  <xsl:template name="existHtmlPage">
    <xsl:param name="testCaseName" />
    <xsl:choose>
      <!-- <xsl:when test="fn:doc-available(string(testng:absolutePath(testng:htmlContentFileName($testCaseName))))"> -->
      <xsl:when test="java:file-exists('', testng:absolutePath(testng:htmlContentFileName($testCaseName)))">
        <xsl:variable name="i" select="position()" />
        <!-- <xsl:value-of select="concat($testCaseName, $i)"/>  -->
        <xsl:call-template name="existHtmlPage">
          <xsl:with-param name="testCaseName" select="concat($testCaseName, $i)" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$testCaseName" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:function xmlns:file="java.io.File" name="java:file-exists" as="xs:boolean">
    <xsl:param name="file" as="xs:string" />
    <xsl:param name="base-uri" as="xs:string" />
    <xsl:variable name="absolute-uri" select="resolve-uri($file, $base-uri)" as="xs:anyURI" />
    <xsl:sequence select="file:exists(file:new($absolute-uri))" />
  </xsl:function>

  <xsl:template name="link-javadoc">
    <xsl:param name="ets-code" />
    <xsl:param name="testClassPath" />
    <xsl:variable name="apidocs" select="concat('http://opengeospatial.github.io/ets-',$ets-code,'/apidocs/')" />
    <xsl:variable name="url" select="concat($apidocs, $testClassPath)" />
    <xsl:text> | </xsl:text>
    <a target="_blank" href="{$url}">Details &#8599;</a>
  </xsl:template>
</xsl:stylesheet>