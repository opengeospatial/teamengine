<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:ctl="http://www.occamlab.com/ctl">
<!--

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
            <h2><xsl:element name="a"><xsl:attribute name="name">
                    <xsl:value-of select="./@name"/></xsl:attribute>
                Test Name:  <xsl:value-of select="./@name"/>
            </xsl:element></h2> 
            <div> <p>
                <b>Test Assertion: </b>
                <xsl:value-of select="./ctl:assertion"/>
            </p>                
                <xsl:if test="count(./ctl:param) &gt; 0">
                        <ul><b>List of parameters:</b>
                            <xsl:apply-templates select="./ctl:param"/>
                        </ul>    
                    <xsl:text>                
                    </xsl:text>
                </xsl:if>
                <xsl:if test="count(./ctl:comment) &gt; 0">
                    <p><b>Test description </b>                        
                        <xsl:value-of select="./ctl:comment"/>
                    </p>
                    <xsl:text>                
                    </xsl:text>    
                </xsl:if>
                <div>
                    <xsl:apply-templates select="./ctl:code">
                    </xsl:apply-templates>
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
    
    <xsl:template match="ctl:code" >
        <xsl:if test="count(.//ctl:comment) &gt; 0">          
            <ol><b>Pseudocode: </b>
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
        <div class="suite">
            <h1>Test Suite name: <xsl:value-of select="$content/@name"/></h1>
            <h2><xsl:value-of select="$content/ctl:title"/></h2>
            <p><xsl:value-of select="$content/ctl:description"/></p>
            <div> The root test is: <xsl:element name="a">
                <xsl:attribute name="href"><xsl:value-of select="concat('#',string($content/ctl:starting-test))"/></xsl:attribute>
                <xsl:value-of select="string($content/ctl:starting-test)"/> </xsl:element>
            </div>               
        </div>    
        <xsl:variable name="testla"><xsl:value-of select="string($content/ctl:starting-test)"/></xsl:variable>            
        <xsl:apply-templates select="$radice//ctl:test[@name=$testla]">
        </xsl:apply-templates>
            <xsl:call-template name="testDocument">                
                <xsl:with-param name="content" select="$radice//ctl:test[@name=$testla]"/>
                <xsl:with-param name="radice" select="$radice"/>
            </xsl:call-template>
    </xsl:template>
    
    <xsl:template name="testDocument">
        <xsl:param name="content"/>
        <xsl:param name="radice"/>
        <xsl:for-each select="$content//ctl:call-test[not(@name= preceding::ctl:call-test/@name)]">
            <xsl:variable name="testla"><xsl:value-of select="string(./@name)"/></xsl:variable>
            <xsl:apply-templates select="$radice//ctl:test[@name=$testla]">
            </xsl:apply-templates>
            <xsl:call-template name="testDocument">                
                <xsl:with-param name="content" select="$radice//ctl:test[@name=$testla]"/>
                <xsl:with-param name="radice" select="$radice"/>               
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>



</xsl:stylesheet>
