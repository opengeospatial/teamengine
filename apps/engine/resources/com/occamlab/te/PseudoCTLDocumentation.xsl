<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:ctl="http://www.occamlab.com/ctl">
<!--
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Fabrizio Vitale
 jointly with the Institute of Methodologies for Environmental Analysis 
 (IMAA) part of the Italian National Research Council (CNR). 
 Portions created by Fabrizio Vitale are Copyright (C) 2009. All Rights Reserved.

 Contributor(s): No additional contributors to date
-->

    <xsl:output method="html"  doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/loose.dtd"  indent="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:template match="text()"/>
    <xsl:template match="/">
        <html>
            <head>
                <title>CTL comments extraction</title>
                <meta name="generator" content="pseudocode_test.xsl"/>
                <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            </head>
            <body>
                <xsl:call-template name="orderedParse"/>
            </body>
        </html>      
    </xsl:template>
    
    <xsl:template match="ctl:test">
        <div class="test">
            <h2><xsl:element name="a">
                <xsl:attribute name="name"><xsl:value-of select="./@name"/></xsl:attribute>
                Test Name:  <xsl:value-of select="./@name"/>
            </xsl:element></h2> 
            <div> <p>
                <b>Test Assertion: </b>
                <xsl:value-of select="./ctl:assertion"/>
            </p>                
                <xsl:if test="count(./ctl:comment) &gt; 0">
                    <p>List of parameters
                        <ul>
                            <xsl:apply-templates select="./ctl:param"/>
                        </ul>    
                    </p>
                    <xsl:text>                
                    </xsl:text>
                </xsl:if>
                <xsl:if test="count(./ctl:comment) &gt; 0">
                    <p><b>Test description</b>                        
                        <xsl:value-of select="./ctl:comment"/>
                    </p>
                    <xsl:text>                
                    </xsl:text>    
                </xsl:if>
                <div>
                    <xsl:apply-templates select="./ctl:code"/>
                </div>
                <ul>        
                    <xsl:apply-templates select=".//ctl:call-test"/>
                </ul>    
            </div>
        </div>
    </xsl:template>
    
    <xsl:template match="ctl:call-test">
        <li>Invoke test: <xsl:element name="a">
            <xsl:attribute name="href"><xsl:value-of select="concat('#',string(./@name))"/></xsl:attribute>
            <xsl:value-of select="./@name"/> </xsl:element></li>
        <xsl:text>            
        </xsl:text>
    </xsl:template>
    <xsl:template match="ctl:comment">
        <xsl:value-of select="."/> 
        <xsl:text>            
        </xsl:text>
    </xsl:template>
    
    <xsl:template match="ctl:param">
        <li>
            <b><xsl:value-of select="./@name"/>: </b>        
            <xsl:value-of select="."/>
        </li>        <xsl:text>            
        </xsl:text>
    </xsl:template>
    
    <xsl:template match="ctl:code">
        <xsl:if test="count(.//ctl:comment) &gt; 0">
            <h3>Code comments</h3> 
            <ol>Pseudocode
                <xsl:for-each select=".//ctl:comment">
                    <xsl:text>                
                    </xsl:text>
                    <li><xsl:value-of select="."/></li>            
                </xsl:for-each>
            </ol>    
            <xsl:text>            
            </xsl:text>
        </xsl:if>    
    </xsl:template>
    
    
    <xsl:template name="orderedParse">
        <h1>CTL documentation</h1>
        <div><p>Documentation extracted from sourcecode (CTL files)</p>
        </div>
        <xsl:call-template name="structuredDocument"/>               
    </xsl:template>

    <xsl:template name="structuredDocument">
        <xsl:call-template name="testSuite">
            <xsl:with-param name="content" select="//ctl:suite"/>
            <xsl:with-param name="radice" select="/"/>            
        </xsl:call-template>
    </xsl:template>
    
    
    <xsl:template name="testSuite">
        <xsl:param name="content"/>
        <xsl:param name="radice"/>
        <xsl:value-of select="$content/@name"/>
            <xsl:variable name="testla"><xsl:value-of select="string($content/ctl:starting-test)"/></xsl:variable>
        <xsl:apply-templates select="$radice//ctl:test[@name=$testla]"/>
            <xsl:call-template name="testDocument">                
                <xsl:with-param name="content" select="$radice//ctl:test[@name=$testla]"/>
                <xsl:with-param name="radice" select="$radice"/>
            </xsl:call-template>
    </xsl:template>
    
    <xsl:template name="testDocument">
        <xsl:param name="content"/>
        <xsl:param name="radice"/>
        <!--<xsl:value-of select="$content/@name"/>-->
        <!--<xsl:apply-templates select="//ctl:test[@name='rim:conformance-test']"/>-->
        <xsl:for-each select="$content//ctl:call-test[not(@name= preceding::ctl:call-test/@name)]">
            <xsl:variable name="testla"><xsl:value-of select="string(./@name)"/></xsl:variable>
            <xsl:apply-templates select="$radice//ctl:test[@name=$testla]"/>
            <xsl:call-template name="testDocument">                
                <xsl:with-param name="content" select="$radice//ctl:test[@name=$testla]"/>
                <xsl:with-param name="radice" select="$radice"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>



</xsl:stylesheet>