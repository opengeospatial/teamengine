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
 version="2.0">
  <xsl:output method="text"/>
  <xsl:output name="xml" omit-xml-declaration="yes" indent="yes"/>

  <xsl:param name="logdir"/>
  <xsl:param name="sessionDir"/>
  <xsl:param name="TESTNAME"/>
  <xsl:param name="index"/>

  <xsl:template name="build-param-string">
    <xsl:param name="param-string" select="''"/>
    <xsl:param name="params"/>
    <xsl:variable name="new-param-string">
      <xsl:value-of select="$param-string"/>
      <xsl:if test="count($params) &gt; 0">
        <xsl:if test="not($param-string = '')">&amp;</xsl:if>
        <xsl:value-of select="concat($params[1]/@name, '=', $params[1])"/>
      </xsl:if>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="count($params) &gt; 1">
        <xsl:call-template name="build-param-string">
          <xsl:with-param name="param-string" select="$new-param-string"/>
          <xsl:with-param name="params" select="$params[position() &gt; 1]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$new-param-string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="build-url">
    <xsl:param name="url"/>
    <xsl:param name="params"/>
    <xsl:value-of select="$url"/>
    <xsl:if test="count($params) &gt; 0">
      <xsl:variable name="last-char" select="substring($url, string-length($url)-1)"/>
      <xsl:if test="not(contains('?&amp;', $last-char))">
        <xsl:choose>
          <xsl:when test="contains($url, '?')">&amp;</xsl:when>
          <xsl:otherwise>?</xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:call-template name="build-param-string">
        <xsl:with-param name="params" select="$params"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="literal">
    <xsl:value-of select="saxon:serialize(., 'xml')" xmlns:saxon="http://saxon.sf.net/"/>
  </xsl:template>

  <xsl:template name="result-text">
    <xsl:param name="result-code" select="@result"/>
    <xsl:param name="complete" select="not(@complete='no')"/>
    <!-- Following values from java.com.occamlab.te.TECore -->
    <xsl:param name="continue">-1</xsl:param>
    <xsl:param name="bestPractice">0</xsl:param>
    <xsl:param name="pass">1</xsl:param>
    <xsl:param name="notTested">2</xsl:param>
    <xsl:param name="skipped">3</xsl:param>
    <xsl:param name="warning">4</xsl:param>
    <xsl:param name="inheritedFailure">5</xsl:param>
    <xsl:param name="fail">6</xsl:param>
    <xsl:choose>
      <xsl:when test="$result-code=$fail and not($complete)">Failed and did not complete</xsl:when>
      <xsl:when test="not($complete)">Did not complete</xsl:when>
      <xsl:when test="$result-code=$fail">Failed</xsl:when>
      <xsl:when test="$result-code=$inheritedFailure">Failed (InheritedFailure)</xsl:when>
      <xsl:when test="$result-code=$warning">Warning</xsl:when>
      <xsl:when test="$result-code=$skipped">Skipped</xsl:when>
      <xsl:when test="$result-code=$notTested">Not Tested</xsl:when>
      <xsl:when test="$result-code=$pass">Passed</xsl:when>
      <xsl:when test="$result-code=$continue">Continue (did not complete)</xsl:when>
      <xsl:otherwise>Passed as Best Practice</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="sessions">
    <xsl:text>Sessions:&#xa;</xsl:text>
    <xsl:for-each select="session">
      <xsl:value-of select="concat('   ', @id, '&#xa;')"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="test" name="viewtest">
    <xsl:param name="indent" select="''"/>
    <xsl:variable name="result">
      <xsl:call-template name="result-text"/>
    </xsl:variable>
    <xsl:value-of select="concat($indent, 'Test ', @prefix, ':', @local-name, ' type ', @type,  ' (', @path, ') ', $result, '&#xa;')"/>
    <xsl:for-each select="test">
      <xsl:call-template name="viewtest">
        <xsl:with-param name="indent" select="concat($indent, '   ')"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="log">
    <xsl:apply-templates select="*"/>
    <xsl:variable name="result">
      <xsl:for-each select="$index/test">
        <xsl:call-template name="result-text"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:value-of select="concat('Result: ', $result, '&#xa;&#xa;')"/>
  </xsl:template>

  <xsl:template match="starttest">
    <xsl:variable name="defaultResultName">
      <xsl:call-template name="result-text">
        <xsl:with-param name="result-code" select="@defaultResult"/>
        <xsl:with-param name="complete" select="true()"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="concat('Test ', @prefix, ':', @local-name, ' type ', @type, ' default result ', $defaultResultName, ' (', @path, ')&#xa;&#xa;')"/> 
    <xsl:value-of select="concat('Assertion: ', assertion, '&#xa;&#xa;')"/>

    <xsl:for-each select="param">
      <xsl:value-of select="concat('Parameter ', @name, ':&#xa;')"/>
      <xsl:value-of select="concat('   Label: ', @label, '&#xa;')"/>
      <xsl:text>   Value: </xsl:text>
      <xsl:for-each select="value">
        <xsl:value-of select="text()|@*"/>
        <xsl:for-each select="*">
          <xsl:call-template name="literal"/>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
    <xsl:if test="param">
      <xsl:text>&#xa;</xsl:text>
    </xsl:if>

    <xsl:for-each select="context[not(value/@te:used='false')]">
      <xsl:text>Context:&#xa;</xsl:text>
      <xsl:value-of select="concat('   Label: ', @label, '&#xa;')"/>
      <xsl:text>   Value: </xsl:text>
      <xsl:choose>
        <xsl:when test="boolean(value)">   
          <xsl:for-each select="value">
            <xsl:value-of select="text()|@*"/>
            <xsl:for-each select="*">
              <xsl:call-template name="literal"/>
            </xsl:for-each>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
    <xsl:if test="context[not(value/@te:used='false')]">
      <xsl:text>&#xa;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="exception">
    <xsl:value-of select="."/>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="request">
    <xsl:text>Request </xsl:text>
    <xsl:value-of select="@id"/>
    <xsl:text>:&#xa;</xsl:text>
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="ctl:request">
    <xsl:text>   Method: </xsl:text>
    <xsl:value-of select="ctl:method"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:text>   URL: </xsl:text>
    <xsl:choose>
      <xsl:when test="translate(ctl:method, 'GET', 'get') = 'get'">
        <xsl:call-template name="build-url">
          <xsl:with-param name="url" select="ctl:url"/>
          <xsl:with-param name="params" select="ctl:param"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="ctl:url"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xa;</xsl:text>
    <xsl:if test="translate(ctl:method, 'POST', 'post') = 'post'">
      <xsl:text>   Body: </xsl:text>
      <xsl:choose>
        <xsl:when test="ctl:body">
          <xsl:text>&#xa;</xsl:text>
          <xsl:for-each select="ctl:body/*">
            <xsl:call-template name="literal"/>
          </xsl:for-each>
          <xsl:if test="not(ctl:body/*)">
            <xsl:value-of select="ctl:body"/>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="build-param-string">
            <xsl:with-param name="params" select="ctl:param"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>&#xa;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="response">
    <xsl:text>   Response</xsl:text>
    <xsl:if test="parser">
      <xsl:value-of select="concat(' from parser ', parser/@prefix, ':', parser/@local-name)"/>
    </xsl:if>
    <xsl:text>:&#xa;      </xsl:text>
    <xsl:for-each select="content/*">
      <xsl:call-template name="literal"/>
    </xsl:for-each>
    <xsl:text>&#xa;</xsl:text>
    <xsl:if test="parser[not(.='')]">
      <xsl:value-of select="concat('   Messages from parser ', parser/@prefix, ':', parser/@local-name, ':&#xa;')"/>
      <xsl:text>        </xsl:text>
      <xsl:value-of select="translate(parser, '&#xa;', '&#xa;      ')"/>
      <xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="parse">
    <xsl:text>Parse </xsl:text>
    <xsl:value-of select="@id"/>
    <xsl:text>:&#xa;</xsl:text>
    <xsl:apply-templates select="response"/>
  </xsl:template>

  <xsl:template match="testcall">
    <xsl:variable name="path" select="@path"/>
    <xsl:variable name="result">
      <xsl:for-each select="$index//test[@path=$path]">
        <xsl:call-template name="result-text"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="nameTypeString">
      <xsl:for-each select="$index//test[@path=$path]">
        <xsl:value-of select="concat('name ', @prefix, ':', @local-name, ' type ', @type)"/>
      </xsl:for-each>     
    </xsl:variable>
    <xsl:value-of select="concat('Subtest ', $nameTypeString, ' path ', @path, ' result ', $result, '&#xa;&#xa;')"/>
  </xsl:template>

  <xsl:template match="formresults">
    <xsl:text>Form </xsl:text>
    <xsl:value-of select="@id"/>
    <xsl:text>:&#xa;</xsl:text>
    <xsl:for-each select="values/value">
      <xsl:value-of select="concat('   ', @key, '=', ., '&#xa;')"/>
    </xsl:for-each>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="message">
    <xsl:value-of select="concat('Message ', @id, ':&#xa;   ', ., '&#xa;&#xa;')"/>
  </xsl:template>

  <xsl:template match="*|@*"/>
</xsl:transform>
