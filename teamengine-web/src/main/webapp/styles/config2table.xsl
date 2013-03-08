<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:saxon="http://saxon.sf.net/"
  exclude-result-prefixes="saxon">

  <db:abstract xmlns:db="http://docbook.org/ns/docbook">
    <db:para>
    Transforms the TEAM-Engine configuration file to an HTML table representation.
    </db:para>
  </db:abstract>

  <xsl:output method="xml" omit-xml-declaration="yes" indent="yes" />

  <xsl:variable name="BASE_URL" select="'about/'"/>

  <xsl:template match="/config">
    <xsl:for-each select="scripts/organization">
      <table border="1">
        <caption>
          <xsl:value-of select="name" />
        </caption>
        <thead>
          <tr>
            <th>Specification</th>
            <th>Version</th>
            <th>Test Suite Revision</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <xsl:call-template name="standards" />
        </tbody>
      </table>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="standards">
    <xsl:for-each select="standard/version">
      <xsl:sort select="../name" order="ascending" />
      <xsl:sort select="name" order="ascending" />
      <tr>
        <td><xsl:value-of select="../name" /></td>
        <td><xsl:value-of select="name" /></td>
        <td>
          <xsl:choose>
            <xsl:when test="revision/webdir">
              <xsl:element name="a">
                <xsl:attribute name="href">
                  <xsl:value-of select="concat($BASE_URL, revision/webdir)"/>
                </xsl:attribute>
                <xsl:attribute name="target" select="'_blank'" />
                <xsl:value-of select="revision/name" />
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="revision/name" />
            </xsl:otherwise>
          </xsl:choose> 
        </td>
        <td><xsl:value-of 
        select="if (revision/status) then revision/status else 'Final'" />
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
