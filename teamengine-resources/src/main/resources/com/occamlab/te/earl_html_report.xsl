<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dct="http://purl.org/dc/terms/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:earl="http://www.w3.org/ns/earl#" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:cite="http://cite.opengeospatial.org/"
	version="2.0">
<xsl:output encoding="UTF-8" indent="yes" method="xml"
		standalone="no" omit-xml-declaration="no" />
	<xsl:template match="/">
		<html>
			<body>
				<h3>
					<font color="black">
						Test Name:
						<xsl:value-of select="rdf:RDF/cite:TestRun/dct:title" />
						<br />
						Time: <xsl:value-of select="rdf:RDF/cite:TestRun/dct:created"/>
						<br />
						Test Artifact:**INPUT:
						http://dummy-input/test-input.xml <!-- <xsl:value-of select="/testng-results/reporter-output[1]/line[3]"/> -->
						<br />
						Result: Pass:  <xsl:value-of select="rdf:RDF/cite:TestRun/cite:testsPassed"/> | Fail: <xsl:value-of select="rdf:RDF/cite:TestRun/cite:testsFailed"/> | Skip: <xsl:value-of select="rdf:RDF/cite:TestRun/cite:testsSkipped"/>
					</font>
				</h3>
				<table border="1">
					<tr>
						<td>Color Legend </td>
						<td bgcolor="#B2F0D1"> Pass </td>
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
	</xsl:template>

	<xsl:template name="test-overview">

		<h2>
			<xsl:value-of select="dct:title" />
		</h2>

		<p>
			<table border="1">
				<tr>
					<td bgcolor="#B2F0D1">
						Pass:
						<!-- <xsl:variable name="passCount">
							<xsl:call-template name="resultCount">
								<xsl:with-param name="result_status" select="'passed'" />
								<xsl:with-param name="count" select="0" />
							</xsl:call-template>
						</xsl:variable> -->
						<xsl:value-of select="cite:testsPassed" />
					</td>
					<td bgcolor="#FFB2B2">
						Fail:
						<!-- <xsl:variable name="failCount">
							<xsl:call-template name="resultCount">
								<xsl:with-param name="result_status" select="'failed'" />
								<xsl:with-param name="count" select="0" />
							</xsl:call-template>
						</xsl:variable> -->
						<xsl:value-of select="cite:testsFailed" />
					</td>
					<td bgcolor="#CCCCCE">
						Skip:
						<!-- <xsl:variable name="skipCount">
							<xsl:call-template name="resultCount">
								<xsl:with-param name="result_status" select="'untested'" />
								<xsl:with-param name="count" select="0" />
							</xsl:call-template>
						</xsl:variable> -->
						<xsl:value-of select="cite:testsSkipped" />
					</td>
				</tr>
			</table>
		</p>
		
		<xsl:call-template name="assertion_info" />

	</xsl:template>

	<!-- Get the count of the test result according to conformace class E.g. Pass: Fail: Skip:   -->
	<xsl:template name="resultCount">

		<xsl:param name="result_status" />
		<xsl:param name="count" />

		<xsl:for-each select="dct:hasPart">

			<xsl:variable name="test_uri">
				<xsl:choose>
					<xsl:when test="earl:TestCase">

						<xsl:value-of select="earl:TestCase/@rdf:about" />

					</xsl:when>
					<xsl:otherwise>

						<xsl:value-of select="@rdf:resource" />

					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>


			<xsl:for-each select="../..//earl:Assertion">

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
					select="earl:result/earl:TestResult/earl:outcome/@rdf:resource" />
				<xsl:if test="$test_uri = $assertion_test_uri">
					<xsl:if test="substring-after($result, '#')=$result_status">
						<xsl:variable name="count" select="$count+1" />
						<xsl:value-of select="$count" />
					</xsl:if>
				</xsl:if>
			</xsl:for-each>

		</xsl:for-each>

	</xsl:template>

	<!-- The assertion_info template get all the test information e.g. TestName, Reason -->
	<xsl:template name="assertion_info">


		<table border="1">
			<tbody>
				<tr>
					<th>Name</th>

					<th>Reason</th>
				</tr>
				
				<xsl:for-each select="dct:hasPart">
					<xsl:choose>
						<xsl:when test="earl:TestCase">
							<xsl:variable name="test_uri" select="earl:TestCase/@rdf:about" />
							<xsl:call-template name="Assertion">
								<xsl:with-param name="test_uris" select="earl:TestCase/@rdf:about" />
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="Assertion">
								<xsl:with-param name="test_uris" select="@rdf:resource" />
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>

			</tbody>
		</table>

	</xsl:template>

	<!-- The Assertion template returns status of the result in HTML format with specific color. 'Pass: Fail: Skip:' -->
	<xsl:template name="Assertion">
		<xsl:param name="test_uris" />

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
			
			<xsl:variable name="result"
				select="earl:result/earl:TestResult/earl:outcome/@rdf:resource" />
				
			<xsl:if test="$test_uris = $assertion_test_uri">

				<xsl:if test="substring-after($result, '#')='passed'">
					<tr bgcolor="#B2F0D1">
						<td>
							<xsl:value-of select="substring-after($test_uris, '#')" />
						</td>
						<td />
					</tr>
				</xsl:if>
				<xsl:if test="substring-after($result, '#')='failed'">
					<tr bgcolor="#FFB2B2">
						<td>
							<xsl:value-of select="substring-after($test_uris, '#')" />
						</td>
						<td>
							<xsl:variable name="message">
								<xsl:call-template name="string-replace-all">
									<xsl:with-param name="text"
										select="earl:result/earl:TestResult/dct:description" />
									<xsl:with-param name="replace" select="'['" />
									<xsl:with-param name="by" select="'&lt;br&gt;&lt;br&gt;['" />
								</xsl:call-template>
							</xsl:variable>
							<p><xsl:value-of select="$message"
								disable-output-escaping="yes" /></p>
						</td>
					</tr>
				</xsl:if>
				<xsl:if test="substring-after($result, '#')='untested'">
					<tr bgcolor="#CCCCCE">
						<td>
							<xsl:value-of select="substring-after($test_uris, '#')" />
						</td>
						<td />
					</tr>
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
							<xsl:with-param name="text"
								select="substring-after($text,$replace)" />
							<xsl:with-param name="replace" select="$replace" />
							<xsl:with-param name="by" select="$by" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>


						<xsl:value-of select="substring-before($text,$replace)" />
						<xsl:text>[</xsl:text>
						<xsl:call-template name="string-replace-all">
							<xsl:with-param name="text"
								select="substring-after($text,$replace)" />
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


</xsl:stylesheet>
