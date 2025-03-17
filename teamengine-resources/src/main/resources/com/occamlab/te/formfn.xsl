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
<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
	<xsl:param name="title"/>
	<xsl:param name="web"/>
	<xsl:param name="files"/>
	<xsl:param name="thread"/>
	<xsl:param name="method"/>
	<xsl:param name="base"/>
	<xsl:param name="action"/>
	<xsl:output method="html"/>

	<xsl:template match="/">
		<html>
			<head>
				<title><xsl:value-of select="$title"/></title>
				<base href="{$base}"/>
			</head>
			<body>
				<xsl:for-each select="*[local-name()='form']">
					<form method="{$method}">
						<xsl:if test="$files = 'yes'">
							<xsl:attribute name="enctype">multipart/form-data</xsl:attribute>
						</xsl:if>
						<xsl:if test="$web = 'yes'">
							<xsl:attribute name="action">
								<xsl:value-of select="$action"/>
							</xsl:attribute>
							<input type="hidden" name="te-operation" value="SubmitForm"/>
							<input type="hidden" name="te-thread" value="{$thread}"/>
						</xsl:if>
						<xsl:apply-templates select="node()"/>
					</form>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="xhtml:*" xmlns:xhtml="http://www.w3.org/1999/xhtml">
		<xsl:element name="{local-name()}">
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="ctl:parse" xmlns:ctl="http://www.occamlab.com/ctl"/>

	<xsl:template match="node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@*">
		<xsl:copy-of select="."/>
	</xsl:template>
</xsl:transform>
