<?xml version="1.0" encoding="UTF-8"?>
<!--
  #%L
  TEAM Engine - Shared Resources
  %%
  Copyright (C) 2006 - 2024 Open Geospatial Consortium
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

	<xsl:output method="html" indent="yes" omit-xml-declaration="yes" />

	<xsl:template match="/">
		<html xmlns="http://www.w3.org/1999/xhtml" lang="en"
			xml:lang="en">
			<head>
				<meta charset="UTF-8" />
				<style type="text/css">
					body {
						color: black;
						background: white;
						font-family: Georgia, serif;
					}
					h1, h2, h3 {
						font-family: Verdana, sans-serif;
						color: #000099;
						text-align: left;
					}
					h1 {
						font-size: 1.4em;
						margin-top: 2em;
					}
					h2 {
						font-size: 1.2em;
					}
					h3 {
						font-size: 1em;
						margin-bottom: 0.5em;
					}
					table {
						margin-top: 0.5em;
						margin-bottom: 1em;
						border-collapse: collapse;
					}
					td, th {
						padding: 0.25em;
						border: 1px solid black;
					}
					caption, figcaption {
						font-weight: bold;
						text-align: left;
						margin: 0.25em;
					}
					th {
						font-weight: bold;
						font-family: Verdana, sans-serif;
						text-align: left;
						vertical-align: top;
						background: #eeeeee;
						color: #000099;
					}
					dt {
						margin-top: 0.5em;
						font-weight: bold;
					}
				</style>
			</head>
			<body>
				<h1>
					<xsl:value-of select="/testsuite/title" />
				</h1>
				<p>
					<xsl:value-of select="/testsuite/description" />
				</p>
				<table border="1" style="border-collapse: collapse;">
					<caption>Test run arguments</caption>
					<thead>
						<tr>
							<th>Name</th>
							<th>Value domain</th>
							<th>Obligation</th>
							<th>Description</th>
						</tr>
					</thead>
					<tbody>
						<xsl:for-each select="/testsuite/testrunarguments/testrunargument">
							<tr>
								<td>
									<xsl:value-of select="name" />
								</td>
								<td>
									<xsl:value-of select="valuedomain" />
								</td>
								<td>
									<xsl:value-of select="obligation" />
								</td>
								<td>
									<xsl:value-of select="description" />
								</td>
							</tr>
						</xsl:for-each>
						<xsl:if test="/testsuite/notes/note">
							<tr>
								<td colspan="4">
									<strong>Notes</strong>
									<xsl:for-each select="/testsuite/notes/note">
										<ul>
											<li>
												<xsl:value-of select="." />
											</li>
										</ul>
									</xsl:for-each>
								</td>
							</tr>
						</xsl:if>
					</tbody>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
