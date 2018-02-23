<?xml version="1.0" encoding="UTF-8"?>
<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:transform
 xmlns:te="java:com.occamlab.te.TECore"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:ctl="http://www.occamlab.com/ctl"
 xmlns:saxon="http://saxon.sf.net/"
 exclude-result-prefixes="saxon"
 version="2.0">
	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
	<xsl:output name="xml" omit-xml-declaration="yes" cdata-section-elements="ctl:comment ctl:body" indent="yes"/>

	<xsl:param name="namespace-uri"/>
	<xsl:param name="local-name"/>

	<xsl:template name="viewtest">
		<h2>Test <xsl:value-of select="@name"/></h2>
		<h3>Assertion:</h3>
		<xsl:value-of select="./ctl:assertion"/>
		<br/>
		<xsl:if test="ctl:comment">
			<h3>Comments:</h3>
			<xsl:for-each select="ctl:comment">
				<xsl:value-of select="."/>
				<br/>
			</xsl:for-each>
		</xsl:if>
		<h3>Links:</h3>
		<xsl:if test="count(./ctl:link) gt 0">
			<ul>
			<xsl:for-each select="./ctl:link">
				<xsl:choose>
					<xsl:when test="@title">
						<li><a href="{.}"><xsl:value-of select="@title"/></a></li>
					</xsl:when>
					<xsl:when test="starts-with(., 'http')">
						<li><a href="{.}"><xsl:value-of select="."/></a></li>
					</xsl:when>
					<xsl:otherwise>
						<li><xsl:value-of select="."/></li>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			</ul>
		</xsl:if>
		<xsl:if test="ctl:param">
			<h3>Parameters:</h3>
			<ul>
			<xsl:for-each select="ctl:param">
				<li>
					<b><xsl:value-of select="@name"/></b>
					<xsl:if test="string(.) != ''">
						<xsl:value-of select="concat(': ',.)"/>
					</xsl:if>
				</li>
			</xsl:for-each>
			</ul>
			<br/>
		</xsl:if>
		<xsl:if test="ctl:context">
			<h3>Context:</h3>
			<xsl:value-of select="ctl:context"/>
			<br/>
		</xsl:if>
		<h3>Code:</h3>
		<pre>
			<xsl:value-of select="saxon:serialize(ctl:code, 'xml')"/>
		</pre>
	</xsl:template>
	
	<xsl:template match="/">
		<xsl:for-each select="//ctl:package/ctl:test[substring-after(@name, ':') = $local-name]">
			<xsl:variable name="prefix" select="substring-before(@name, ':')"/>
			<xsl:if test="namespace::*[name()=$prefix] = $namespace-uri">
				<xsl:call-template name="viewtest"/>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="/ctl:test">
			<xsl:call-template name="viewtest"/>
		</xsl:for-each>
	</xsl:template>
</xsl:transform>
