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

  Contributor(s): Paul Daisey (Image Matters LLC): 
		2011-04-06 Added support for:test status: Best Practice, Not Tested, Skipped, Continue.
		2011-12-20 Added testnum transform param for use by test template to disambiguate tests from multiple profiles

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:transform
 xmlns:viewlog="viewlog"
 xmlns:te="java:com.occamlab.te.TECore"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:encoder="java:java.net.URLEncoder"
 xmlns:file="java:java.io.File"
 xmlns:ctl="http://www.occamlab.com/ctl"
 exclude-result-prefixes="viewlog encoder file te ctl"
 version="2.0">
 	<xsl:import href="../logstyles/default.xsl"/>
 
	<xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>
	<xsl:output name="xml" omit-xml-declaration="yes" indent="yes"/>

	<xsl:param name="logdir"/>
	<xsl:param name="index"/>
	<xsl:param name="testnum">1</xsl:param>  <!-- 2011-12-20 PwD -->

<!-- 2011-04-06 PwD
	<xsl:template name="result-filename">
		<xsl:param name="result-code" select="@result"/>
		<xsl:param name="complete" select="not(@complete='no')"/>
		<xsl:text>images/</xsl:text>
		<xsl:choose>
			<xsl:when test="$result-code=3">fail</xsl:when>
			<xsl:when test="not($complete)">incomplete</xsl:when>
			<xsl:when test="$result-code=2">fail</xsl:when>
			<xsl:when test="$result-code=1">warn</xsl:when>
			<xsl:otherwise>pass</xsl:otherwise>
		</xsl:choose>
		<xsl:text>.png</xsl:text>
	</xsl:template>
replaced by the following, which returns the name of the status icon to display: -->

	<xsl:template name="result-filename">
		<xsl:param name="result-code" select="@result"/>
		<xsl:param name="complete" select="not(@complete='no')"/>
		<!-- Following values from java.com.occamlab.te.TECore.java -->
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
		<!-- <xsl:param name="testnum" select="1"/>  -->
		<xsl:param name="testnum" select="$testnum"/> <!-- 2011-12-20 PwD -->
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
		<xsl:variable name="file" select="file:new(string(@file))"/>
		<xsl:variable name="pdir1" select="file:getName(file:getParentFile($file))"/>
		<xsl:variable name="pdir2" select="file:getName(file:getParentFile(file:getParentFile($file)))"/>
		<xsl:variable name="separator">
			<xsl:choose>
				<xsl:when test="contains(@file, '/')">~2F</xsl:when>
				<xsl:otherwise>~5C</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="dir">
			<xsl:choose>
				<xsl:when test="contains($pdir1, $separator)">
					<xsl:value-of select="substring($pdir1, 1, string-length($pdir1) - 4)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat($pdir2, $separator, $pdir1)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- begin 2011-04-06 PwD replace next line with following to find listings with ~ instead of % in filenames 
		<a href="listings/{$dir}.html#{@prefix}:{@local-name}"> -->		
		<xsl:variable name="dirTilde" select="replace($dir,'%', '~')"/>
		<a href="listings/{$dirTilde}.html#{@prefix}:{@local-name}">
		<!-- end 2011-04-06 PwD -->
			<xsl:value-of select="concat('Test ', @prefix, ':', @local-name)"/>
		</a>
<!--
		<xsl:choose>
		<xsl:when test="contains(@path,'/')">
			<xsl:variable name="sessionid" select="substring-before(@path,'/')"/>
			<a href="viewTest.jsp?file={viewlog:encode(@file)}&amp;namespace={viewlog:encode(@namespace-uri)}&amp;name={@local-name}&amp;sessionid={$sessionid}">
				<xsl:value-of select="concat('Test ', @prefix, ':', @local-name)"/>
			</a>
		</xsl:when>
		<xsl:otherwise>
			<xsl:variable name="sessionid" select="@path"/>
			<a href="viewTest.jsp?file={viewlog:encode(@file)}&amp;namespace={viewlog:encode(@namespace-uri)}&amp;name={@local-name}&amp;sessionid={$sessionid}">
				<xsl:value-of select="concat('Test ', @prefix, ':', @local-name)"/>
			</a>		
		</xsl:otherwise>
		</xsl:choose>				
 -->
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
		<!-- begin 2011-04-06 PwD -->
		<xsl:param name="continue">-1</xsl:param>
		<xsl:param name="bestPractice">0</xsl:param>
		<xsl:param name="pass">1</xsl:param>
		<xsl:param name="notTested">2</xsl:param>
		<xsl:param name="skipped">3</xsl:param>
		<xsl:param name="warning">4</xsl:param>
		<xsl:param name="inheritedFailure">5</xsl:param>
		<xsl:param name="fail">6</xsl:param>		
		<!-- end 2011-04-06 PwD -->
		<xsl:apply-templates/>
		<xsl:if test="test">
			<br/>
<!-- 2011-04-06 PwD			
			<table id="summary" border="0" bgcolor="#EEEEEE" width="410">
				<tr>
					<th align="left">
						<font color="#000099">Summary</font>
					</th>
					<td align="right"><img src="images/pass.png" hspace="4"/>Pass:</td>
					<td id="nPass" align="center" bgcolor="#00FF00">
						<xsl:value-of select="count(//test[@result=0 and @complete='yes'])"/>
					</td>
					<td align="right"><img src="images/warn.png" hspace="4"/>Warning:</td>
					<td id="nWarn" align="center" bgcolor="#FFFF00">
						<xsl:value-of select="count(//test[@result=1 and @complete='yes'])"/>
					</td>
					<td align="right"><img src="images/fail.png" hspace="4"/>Fail:</td>
					<td id="nFail" align="center" bgcolor="#FF0000">
						<xsl:value-of select="count(//test[@result &gt; 1 and @complete='yes'])"/>
					</td>
				</tr>
			</table>
	replaced by the following: -->
	
			<table id="summary" border="0" bgcolor="#EEEEEE">
				<tr>
					<th align="left">
						<font color="#000099">Summary</font>
					</th>
					<td align="right"><img src="images/bestPractice.png" hspace="4"/>Best Practice:</td>
					<td id="nPass" align="center" bgcolor="#00FF00">
						<xsl:value-of select="count(//test[@result=$bestPractice and @complete='yes'])"/>
					</td>
					<td align="right"><img src="images/pass.png" hspace="4"/>Pass:</td>
					<td id="nPass" align="center" bgcolor="#00FF00">
						<xsl:value-of select="count(//test[@result=$pass and @complete='yes'])"/>
					</td>
					<td align="right"><img src="images/continue.png" hspace="4"/>Continue:</td>
					<td id="nWarn" align="center" bgcolor="#FFFF00">
						<xsl:value-of select="count(//test[@result=$continue or @complete='no'])"/>
					</td>
					<td align="right"><img src="images/notTested.png" hspace="4"/>Not Tested:</td>
					<td id="nWarn" align="center" bgcolor="#FFFF00">
						<xsl:value-of select="count(//test[@result=$notTested and @complete='yes'])"/>
					</td>
					<td align="right"><img src="images/warn.png" hspace="4"/>Warning:</td>
					<td id="nWarn" align="center" bgcolor="#FFFF00">
						<xsl:value-of select="count(//test[@result=$warning and @complete='yes'])"/>
					</td>
					<td align="right"><img src="images/skipped.png" hspace="4"/>Skipped:</td>
					<td id="nWarn" align="center" bgcolor="#FFFF00">
						<xsl:value-of select="count(//test[@result=$skipped and @complete='yes'])"/>
					</td>
					<td align="right"><img src="images/inheritedFailure.png" hspace="4"/>Inherited Failure:</td>
					<td id="nFail" align="center" bgcolor="#FF0000">
						<xsl:value-of select="count(//test[@result=$inheritedFailure and @complete='yes'])"/>
					</td>
					<td align="right"><img src="images/fail.png" hspace="4"/>Fail:</td>
					<td id="nFail" align="center" bgcolor="#FF0000">
						<xsl:value-of select="count(//test[@result=$fail and @complete='yes'])"/>
					</td>
				</tr>
			</table>
			
		</xsl:if>
	</xsl:template>
</xsl:transform>
