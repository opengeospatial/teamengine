<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:txsl="http://www.w3.org/1999/XSL/Transform/target"
  xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:te="java:com.occamlab.te.TECore"
  xmlns:saxon="http://saxon.sf.net/"
  version="2.0">

	<xsl:strip-space elements="*"/>
	<xsl:output indent="yes"/>
	<xsl:namespace-alias stylesheet-prefix="txsl" result-prefix="xsl"/>

	<xsl:template match="ctl:async-request">
		<xsl:variable name="request-id" select="generate-id()"/>
		<xsl:variable name="port">
			<xsl:choose>
			<xsl:when test="@port">
				<xsl:value-of select="@port"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'80'"/>
			</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="timeout">
			<xsl:choose>
			<xsl:when test="@timeout">
				<xsl:value-of select="@timeout"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'10'"/>
			</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="xpointer-id" select="@xpointer-id"/>
		<txsl:variable name="te:web-call-id">
			<xsl:call-template name="loc"/>
			<xsl:attribute name="select"><xsl:value-of select="concat('concat(', $apos, $request-id, '_', $apos, ', position())')"/></xsl:attribute>
		</txsl:variable>		
		<txsl:variable name="te:async-request">
			<txsl:choose>
				<txsl:when test="$te:mode='2' and boolean($te:log/log/async-request[@id = $te:web-call-id])">
					<txsl:copy-of select="$te:log/log/async-request[@id = $te:web-call-id]"/>
				</txsl:when>
				<txsl:otherwise>
					<async-request>
						<txsl:attribute name="id"><txsl:value-of select="$te:web-call-id"/></txsl:attribute>
						<txsl:attribute name="port"><txsl:value-of select="{$port}"/></txsl:attribute>
						<txsl:attribute name="timeout"><txsl:value-of select="{$timeout}"/></txsl:attribute>
						<txsl:attribute name="xpointer-id"><txsl:value-of select="{$xpointer-id}"/></txsl:attribute>
						<xsl:apply-templates select="ctl:url" mode="drop-namespace"/>
						<xsl:apply-templates select="ctl:method" mode="drop-namespace"/>
						<xsl:apply-templates select="ctl:header" mode="drop-namespace"/>
						<xsl:apply-templates select="ctl:param" mode="drop-namespace"/>
						<xsl:apply-templates select="ctl:body" mode="drop-namespace"/>
						<xsl:apply-templates select="ctl:part" mode="drop-namespace"/>
					</async-request>
				</txsl:otherwise>
			</txsl:choose>
		</txsl:variable>
		<txsl:value-of select="te:log_xml($te:core, $te:async-request)"/>
		<txsl:variable name="te:async-request-responses" select="te:build_async_request($te:async-request/async-request)">
			<xsl:call-template name="loc"/>
		</txsl:variable>					
		<xsl:variable name="parser">
			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="self::ctl:url"/>
					<xsl:when test="self::ctl:method"/>
					<xsl:when test="self::ctl:header"/>
					<xsl:when test="self::ctl:param"/>
					<xsl:when test="self::ctl:body"/>
					<xsl:when test="self::ctl:part"/>
					<xsl:otherwise>
						<xsl:apply-templates select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:variable>
		<txsl:variable name="te:parser">
			<xsl:copy-of select="$parser"/>
		</txsl:variable>
		<xsl:call-template name="loc-element"/>
		<txsl:variable name="te:responses">
			<txsl:choose>
				<txsl:when test="$te:mode='2' and boolean($te:log/log/responses[@id = $te:web-call-id])">
					<txsl:copy-of select="$te:log/log/responses[@id = $te:web-call-id]"/>
				</txsl:when>
				<txsl:otherwise>
					<xsl:choose>
						<xsl:when test="boolean($parser/*)">
							<xsl:variable name="parser-prefix" select="substring-before(name($parser/*), ':')"/>
							<txsl:copy-of select="te:parse_async($te:core, $te:async-request-responses, $te:web-call-id, $te:parser)">
								<xsl:copy-of select="namespace::*[name()=$parser-prefix]"/>
							</txsl:copy-of>
						</xsl:when>
						<xsl:otherwise>
							<txsl:copy-of select="te:parse_async($te:core, $te:async-request-responses, $te:web-call-id)"/>
						</xsl:otherwise>
					</xsl:choose>
				</txsl:otherwise>
			</txsl:choose>
		</txsl:variable>
		<txsl:if test="string-length($te:responses/responses/parser) &gt; 0">
			<txsl:value-of select="te:message($te:core, $te:call-depth + 1, $te:responses/responses/parser)"/>
		</txsl:if>
		<txsl:value-of select="te:log_xml($te:core, $te:responses)"/>
		<txsl:for-each select="$te:responses/responses/content">
			<content>
				<txsl:copy-of select="*|text()"/>
			</content>
		</txsl:for-each>
	</xsl:template>
	
</xsl:transform>

