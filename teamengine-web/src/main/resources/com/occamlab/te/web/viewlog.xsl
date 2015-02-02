<?xml version="1.0" encoding="UTF-8"?>
<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The contents of this file are subject to the Mozilla Public License
  Version 1.1 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://www.mozilla.org/MPL/ 

  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  the specific language governing rights and limitations under the License. 

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:transform
    xmlns:viewlog="viewlog"
    xmlns:te="java:com.occamlab.te.TECore"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:encoder="java:java.net.URLEncoder"
    xmlns:file="java:java.io.File"
    xmlns:ctl="http://www.occamlab.com/ctl"
    xmlns:saxon="http://saxon.sf.net/"
    exclude-result-prefixes="viewlog encoder file te ctl"
    version="2.0">
    <xsl:import href="../logstyles/default.xsl"/>
    <xsl:import href="../logstyles/result-log.xsl"/>
    
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>
    <xsl:output name="xml" omit-xml-declaration="yes" indent="yes"/>

    <xsl:param name="logdir"/>
    <xsl:param name="sessionDir"/>
    <xsl:param name="TESTNAME"/>
    <xsl:param name="index"/>
    <xsl:param name="testnum">1</xsl:param>

    <xsl:template name="result-filename">
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
        <xsl:text>images/</xsl:text>        
        <xsl:choose>
            <xsl:when test="$result-code=$fail">fail</xsl:when>
            <xsl:when test="not($complete)">incomplete</xsl:when>
            <xsl:when test="$result-code=$inheritedFailure">inheritedFailure</xsl:when>
            <xsl:when test="$result-code=$warning">warn</xsl:when>
            <xsl:when test="$result-code=$skipped">skipped</xsl:when>
            <xsl:when test="$result-code=$notTested">notTested</xsl:when>
            <xsl:when test="$result-code=$pass">pass</xsl:when>
            <xsl:when test="$result-code=$continue">continue</xsl:when>
            <xsl:otherwise>bestPractice</xsl:otherwise>
        </xsl:choose>
        <xsl:text>.png</xsl:text>
    </xsl:template>

    <xsl:function name="viewlog:encode">
        <xsl:param name="str"/>
        <xsl:if test="string-length($str) &gt; 0">
            <xsl:value-of select="encoder:encode($str, 'UTF-8')"/>
        </xsl:if>
    </xsl:function>

    <xsl:template name="test" match="test">
        <xsl:param name="testnum" select="$testnum"/>
        <xsl:variable name="file" select="file:new(string(@file))"/>
        <xsl:variable name="pdir1" select="file:getName(file:getParentFile($file))"/>
        <xsl:variable name="pdir2" select="file:getName(file:getParentFile(file:getParentFile($file)))"/>
        <xsl:variable name="dir">
            <xsl:choose>
                <xsl:when test="contains($pdir1, '.ctl')">
                    <xsl:value-of select="substring($pdir1, 1, string-length($pdir1) - 4)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat($pdir2, $pdir1)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!-- Create link to CTL script in web app context -->
        <xsl:if test="not($TESTNAME='WMS Client Test Suite')">
            <xsl:text>&#xa;</xsl:text>
            <xsl:if test="test">
                <img src="images/minus.png" name="image{$testnum}" onclick="toggle('{$testnum}', event)" title="Click to toggle.  Ctrl+Click for a deep toggle."/>
                <xsl:text>&#xa;</xsl:text>
            </xsl:if>
            <img>
                <xsl:attribute name="src">
                    <xsl:call-template name="result-filename"/>
                </xsl:attribute>
            </img>
            <xsl:text>&#xa;</xsl:text>        
            <a href="listings/{$dir}.html#{@prefix}:{@local-name}" target="_blank">
                <xsl:value-of select="concat('Test ', @prefix, ':', @local-name)"/>
            </a>

            <xsl:text>&#xa;(</xsl:text>
            <a href="viewTestLog.jsp?test={@path}">View Details</a>
            <xsl:text>): </xsl:text>
            <xsl:call-template name="result-text"/>
            <br/>
            <xsl:if test="test">
                <div style="display:block; margin-left:30px" id="test{$testnum}">
                    <xsl:for-each select="test">
                        <xsl:call-template name="test">
                            <xsl:with-param name="testnum" select="concat($testnum, '.', position())"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </div>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template match="log">
        <xsl:variable name="result">
            <xsl:for-each select="$index/test">
                <xsl:call-template name="result-text"/>
            </xsl:for-each>
        </xsl:variable>
        <pre>
            <xsl:apply-templates select="*"/>
            <xsl:value-of select="concat('Result: ', $result, '&#xa;')"/>
        </pre>
    </xsl:template>
    
    <xsl:template match="/">
        <xsl:param name="continue">-1</xsl:param>
        <xsl:param name="bestPractice">0</xsl:param>
        <xsl:param name="pass">1</xsl:param>
        <xsl:param name="notTested">2</xsl:param>
        <xsl:param name="skipped">3</xsl:param>
        <xsl:param name="warning">4</xsl:param>
        <xsl:param name="inheritedFailure">5</xsl:param>
        <xsl:param name="fail">6</xsl:param>
        <xsl:apply-templates/>
        <xsl:if test="$TESTNAME='WMS Client Test Suite'">
            <xsl:call-template name="Client-Result"/>  
        </xsl:if>
        <xsl:if test="not($TESTNAME='WMS Client Test Suite')">
            <xsl:if test="test">
                <br/>
                <table id="summary" border="0" cellpadding="4">
                    <tr>
                        <th align="left" colspan="8" 
                            style="font-family: sans-serif; color: #000099; background:#ccffff">Summary of results</th>
                    </tr>
                    <tr>
                        <td>
                            <img src="images/bestPractice.png" /> Best Practice</td>
                        <td>
                            <img src="images/pass.png" /> Passed</td>
                        <td>
                            <img src="images/continue.png" /> Continue</td>
                        <td>
                            <img src="images/notTested.png" /> Not Tested</td>
                        <td>
                            <img src="images/warn.png" /> Warning</td>
                        <td>
                            <img src="images/skipped.png" /> Skipped</td>
                        <td>
                            <img src="images/fail.png" /> Failed</td>
                        <td>
                            <img src="images/inheritedFailure.png" /> Failed (Inherited)</td>
                    </tr>
                    <tr>
                        <td id="nBestPractice" align="center" bgcolor="#00FF00">
                            <xsl:value-of select="count(//test[@result=$bestPractice and @complete='yes'])"/>
                        </td>
                        <td id="nPass" align="center" bgcolor="#00FF00">
                            <xsl:value-of select="count(//test[@result=$pass and @complete='yes'])"/>
                        </td>
                        <td id="nContinue" align="center" bgcolor="#FFFF00">
                            <xsl:value-of select="count(//test[@result=$continue or @complete='no'])"/>
                        </td>
                        <td id="nNotTested" align="center" bgcolor="#FFFF00">
                            <xsl:value-of select="count(//test[@result=$notTested and @complete='yes'])"/>
                        </td>
                        <td id="nWarn" align="center" bgcolor="#FFFF00">
                            <xsl:value-of select="count(//test[@result=$warning and @complete='yes'])"/>
                        </td>
                        <td id="nSkipped" align="center" bgcolor="#FFFF00">
                            <xsl:value-of select="count(//test[@result=$skipped and @complete='yes'])"/>
                        </td>
                        <td id="nFail" align="center" bgcolor="#FF0000">
                            <xsl:value-of select="count(//test[@result=$fail and @complete='yes'])"/>
                        </td>
                        <td id="nInheritedFail" align="center" bgcolor="#FF0000">
                            <xsl:value-of select="count(//test[@result=$inheritedFailure and @complete='yes'])"/>
                        </td>
                    </tr>
                </table>
            </xsl:if>
        </xsl:if>
    </xsl:template>
</xsl:transform>
