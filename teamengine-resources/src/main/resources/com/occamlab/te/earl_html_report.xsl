<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dct="http://purl.org/dc/terms/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:earl="http://www.w3.org/ns/earl#" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:cite="http://cite.opengeospatial.org/"
	xmlns:cnt="http://www.w3.org/2011/content#"
	xmlns:http="http://www.w3.org/2011/http#"
	xmlns:file="java:java.io.File"
	xmlns:testng="http://testng.org"
	version="2.0">
<xsl:output encoding="UTF-8" indent="yes" method="html"
		standalone="no" omit-xml-declaration="yes" />
	<xsl:output name="text" method="text"/>
	
	<xsl:output name="html" method="html" indent="yes" omit-xml-declaration="yes"/>
	<xsl:output name="xhtml" method="xhtml" indent="yes" omit-xml-declaration="yes"/>
	<xsl:param name="outputDir"/>
    <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'" /> 
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" /> 	
    <xsl:variable name="htmlXslt.outputDir" select="$outputDir" /> 	

	
	<xsl:function name="testng:absolutePath">
    <xsl:param name="fileName"/>
    <xsl:value-of select="concat('file:///',$htmlXslt.outputDir, '/', $fileName)"/>
    <!--<xsl:value-of select="concat('file:///', $testNgXslt.outputDir, '/', $fileName)"/>-->
  </xsl:function>
  
  <xsl:function name="testng:htmlContentFileName">
    <xsl:param name="testName"/>
    <xsl:value-of select="concat($testName, '.html')"/>
  </xsl:function>
	<xsl:template match="/">
	
	<xsl:result-document href="{testng:absolutePath('test.html')}" format="xhtml">
		<html>
			<body>
				<h3>
					<font color="black">
					
						Test Name: <xsl:value-of select="rdf:RDF/cite:TestRun/dct:title" />
						<br />
						Time:
						<xsl:value-of select="rdf:RDF/cite:TestRun/dct:created"/>
						<br />
						Test INPUT: 
						<xsl:choose>
							<xsl:when test="rdf:RDF/cite:TestRun/cite:inputs/rdf:Bag/rdf:li/cnt:ContentAsXML/dct:description">
								<xsl:value-of select="rdf:RDF/cite:TestRun/cite:inputs/rdf:Bag/rdf:li/cnt:ContentAsXML/dct:description"/>
							</xsl:when>
							<xsl:otherwise>
								<br />
								Resource: <xsl:value-of select="rdf:RDF/cite:TestRun/cite:inputs/rdf:Bag/rdf:li/dct:title" />
								<br />
								URI: <xsl:value-of select="rdf:RDF/cite:TestRun/cite:inputs/rdf:Bag/rdf:li/dct:description" />
							</xsl:otherwise>	
						</xsl:choose>						
						<br />
						Result: <br />
					    Number of conformance classes tested: <xsl:value-of select="count(//earl:TestRequirement)"/> <br />
						<xsl:variable name="status">
							<xsl:choose>
								<xsl:when test="//earl:TestRequirement/cite:isBasic[text() = 'true'] and //earl:TestRequirement/cite:testsFailed[text() ='0']">
									<xsl:value-of select="'Yes'"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="'No'" />
								</xsl:otherwise>	
							</xsl:choose>
						</xsl:variable>
						Passed core(Can be certified): <xsl:value-of select="$status"/> <br /> 
						Number of conformance class passed:  <xsl:value-of select="count(//earl:TestRequirement[cite:testsPassed[text() > '0']]/cite:testsFailed[text() = '0'])"/><br />
						Number of conformance class failed:  <xsl:value-of select="count(//earl:TestRequirement/cite:testsFailed[text() >'0'])"/>
						<!-- Pass:  <xsl:value-of select="rdf:RDF/cite:TestRun/cite:testsPassed"/> | Fail: <xsl:value-of select="rdf:RDF/cite:TestRun/cite:testsFailed"/> | Skip: <xsl:value-of select="rdf:RDF/cite:TestRun/cite:testsSkipped"/> -->
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
		</xsl:result-document>
		<xsl:call-template name="test-details" /> 
	</xsl:template>

	<xsl:template name="test-overview">

		<xsl:choose>
			<xsl:when test="cite:isBasic = 'true'">
				<div style="background:#FF8C00; width:25%;"><h2> <xsl:value-of select="dct:title" /> </h2> </div>
			</xsl:when>
			<xsl:otherwise>
				<h2> <xsl:value-of select="dct:title" /> </h2>
			</xsl:otherwise>
		</xsl:choose>
		
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
				<xsl:variable name="testCaseName" select="substring-after($test_uris, '#')"/>
				<xsl:if test="substring-after($result, '#')='passed'">
				
					<tr bgcolor="#B2F0D1">
						<td>
							<a target="_blank" href="{testng:absolutePath(testng:htmlContentFileName($testCaseName))}#{$testCaseName}">	<xsl:value-of select="substring-after($test_uris, '#')" /> </a>
						</td>
						<td />
					</tr>
				</xsl:if>
				<xsl:if test="substring-after($result, '#')='failed'">
					<tr bgcolor="#FFB2B2">
						<td>
							<a target="_blank" href="{testng:absolutePath(testng:htmlContentFileName($testCaseName))}#{$testCaseName}">	<xsl:value-of select="substring-after($test_uris, '#')" /> </a>
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
							<a target="_blank" href="{testng:absolutePath(testng:htmlContentFileName($testCaseName))}#{$testCaseName}">	<xsl:value-of select="substring-after($test_uris, '#')" /> </a>
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
												
											<xsl:if test="$test_uris = $assertion_test_uri">
												<xsl:variable name="testCaseName" select="substring-after($test_uris, '#')"/>											
												<xsl:result-document href="{testng:absolutePath(testng:htmlContentFileName($testCaseName))}" format="xhtml">
													<html>
													<body>
														<div id="{$testCaseName}">
															<table style="width:50%" border="1">
																<tr><td colspan="5"><h3><xsl:value-of select="$testCaseName" /></h3></td></tr>
																<xsl:if test="substring-after($test_uris, '#')">
																	<tr>
																		<td> Test Name: </td>
																		<td>
																			<xsl:value-of select="substring-after($test_uris, '#')" />
																		</td>
																	</tr>
																</xsl:if>
																<xsl:if test="earl:test/earl:TestCase/dct:description">
																	<tr>
																		<td> Test Description: </td>
																		<td>
																			<xsl:value-of select="earl:test/earl:TestCase/dct:description" />
																		</td>
																	</tr>
																</xsl:if>
																	<tr>
																		<td> Test Result: </td>
																		<td>
																			<xsl:value-of select="translate(substring-after($result, '#'), $smallcase, $uppercase)" />
																		</td>
																	</tr>
																<xsl:if test="earl:result/earl:TestResult/cite:message/http:Request">
																	<tr>
																		<td colspan="5"> Test Request: </td>
																	</tr>
																	<tr>
																		<td> Request Method: </td>
																		<td>
																		<xsl:variable name="requestMethod" select="earl:result/earl:TestResult/cite:message/http:Request/http:methodName" />
																			<xsl:value-of select="translate($requestMethod, $smallcase, $uppercase)" />
																		</td>
																	</tr>
																	<tr>
																		<td> Request URI: </td>
																		<td>
																			<xsl:value-of select="earl:result/earl:TestResult/cite:message/http:Request/http:requestURI" />
																		</td>
																	</tr>
																	<tr>
																		<td> Request Response: </td>
																		<td>
																			<textarea rows="20" cols="40" style="border:none;"><xsl:value-of select="earl:result/earl:TestResult/cite:message/http:Request/http:resp/http:Response/http:body/cnt:ContentAsXML/cnt:rest" /> </textarea>
																		</td>
																	</tr>
																</xsl:if>		
																	<tr>
																		<td> Reason of failure: </td>
																		<td>
																			<xsl:variable name="message">
																				<xsl:call-template name="string-replace-all">
																					
																					<xsl:with-param name="text"
																						select="earl:result/earl:TestResult/dct:description" />
																					<xsl:with-param name="replace" select="'['" />
																					<xsl:with-param name="by" select="'&lt;br&gt;&lt;br&gt;['" />
																				</xsl:call-template>
																			</xsl:variable>
																			<p><xsl:value-of select="$message" disable-output-escaping="yes" /></p>
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
					</xsl:for-each> <!-- dct:hasPart -->
		</xsl:for-each> <!--  TestRequirement --> 
	</xsl:template>

</xsl:stylesheet>
