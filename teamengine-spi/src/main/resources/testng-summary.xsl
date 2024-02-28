<!--
  #%L
  TEAM Engine - Service Providers
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
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema">

  <db:abstract xmlns:db="http://docbook.org/ns/docbook">
    <db:para>Produces a simple text summary from the output of a TestNG XML reporter.</db:para>
  </db:abstract>

  <xsl:output method="text" />	

  <xsl:template match="/testng-results">
    <xsl:text>Test suite: </xsl:text>
    <xsl:value-of select="suite/@name"/>
    <xsl:text>&#10;</xsl:text>
    <xsl:text>======== Test groups ========</xsl:text>
    <xsl:for-each select="suite/test">
      <xsl:text>&#10;</xsl:text>
      <xsl:value-of select="@name"/>
      <xsl:text>&#10;</xsl:text>
      <xsl:text>    Passed: </xsl:text>
      <xsl:value-of select="count(class/test-method[@status='PASS' and not(@is-config)])"/>
      <xsl:text> | Failed: </xsl:text>
      <xsl:value-of select="count(class/test-method[@status='FAIL' and not(@is-config)])"/>
      <xsl:text> | Skipped: </xsl:text>
      <xsl:value-of select="count(class/test-method[@status='SKIP' and not(@is-config)])"/>
    </xsl:for-each>
    <xsl:text>&#10;&#10;</xsl:text>
  </xsl:template>

</xsl:stylesheet>
