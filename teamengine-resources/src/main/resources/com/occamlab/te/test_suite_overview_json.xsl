<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

	<xsl:output method="text" />

	<xsl:template match="/">{
		"testsuite": {
			"title": "<xsl:value-of select="/testsuite/title" />",
			"description": "<xsl:value-of select="/testsuite/description" />",
			"testrunarguments": <xsl:call-template name="testrunargument" />
			<xsl:if test="/testsuite/notes/note">,
			    "notes": <xsl:call-template name="note" />
			</xsl:if>
			}
		}
	</xsl:template>

	<xsl:template name="testrunargument">
		<xsl:choose>
			<xsl:when test="count(/testsuite/testrunarguments/testrunargument) &gt; 1">[
				<xsl:for-each select="/testsuite/testrunarguments/testrunargument">
					{
						"name": "<xsl:value-of select="name" />",
						"valuedomain": "<xsl:value-of select="valuedomain" />",
						"obligation": "<xsl:value-of select="obligation" />",
						"description": "<xsl:value-of select="description" />"
					}<xsl:choose><xsl:when test="position() != last()">,</xsl:when></xsl:choose>
				</xsl:for-each>
				]
			</xsl:when>
			<xsl:otherwise>
				{
					"name": "<xsl:value-of select="/testsuite/testrunarguments/testrunargument/name" />",
					"valuedomain": "<xsl:value-of select="/testsuite/testrunarguments/testrunargument/valuedomain" />",
					"obligation": "<xsl:value-of select="/testsuite/testrunarguments/testrunargument/obligation" />",
					"description": "<xsl:value-of select="/testsuite/testrunarguments/testrunargument/description" />"
				}
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="note">
		<xsl:choose>
			<xsl:when test="count(/testsuite/notes/note) &gt; 1">[
				<xsl:for-each select="/testsuite/notes/note">
					"<xsl:value-of select="." />"<xsl:choose><xsl:when test="position() != last()">,</xsl:when></xsl:choose>
				</xsl:for-each>
				]
			</xsl:when>
			<xsl:otherwise>"<xsl:value-of select="/testsuite/notes/note" />"</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>