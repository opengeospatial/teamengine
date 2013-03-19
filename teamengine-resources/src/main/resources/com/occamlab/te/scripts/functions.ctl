<?xml version="1.0" encoding="UTF-8"?>
<!-- Global functions - these are always included when processing a test suite -->
<ctl:package
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:ctl="http://www.occamlab.com/ctl"
 xmlns:te="http://www.occamlab.com/te">

  <ctl:function name="ctl:getImageType">
    <ctl:param name="image.uri"/>
    <ctl:description>Returns the image type name (empty string if invalid).</ctl:description>
    <ctl:java class="com.occamlab.te.parsers.ImageParser" 
                  method="getImageType"/>
  </ctl:function>   

  <ctl:function name="ctl:ParseBase64ImageData">
    <ctl:param name="base64Data">Base 64 image data, e.g. from wmts:BinaryPayload/wmts:BinaryContent 
    returned by SOAPParser as unwrapped content from SOAP GetTile response, or from ImageParser 
    parsers:base64Data instruction from a GetTile image. </ctl:param>
    <ctl:param name="instruction">parsers:ImageParser element containing a list of parsers:* instruction 
    child elements (any or all of type, height, width, metadata) for the ImageParser</ctl:param>
    <ctl:return>Requested parsers:* elements with requested data</ctl:return>
    <ctl:description>Invokes the ImageParser on the specified base64Data using the parsers:* elements 
    specified in the instruction.</ctl:description>
    <ctl:java class="com.occamlab.te.parsers.ImageParser" method="parseBase64" initialized="false"/>
  </ctl:function>

  <ctl:function name="ctl:getBeginningDateTime">
    <ctl:param name="timestamp"/>
    <ctl:description>
        Returns the time instant (a dateTime value) at which a given time period 
        begins. The period may be expressed as a year, month, or date according 
        to the Gregorian calendar.
    </ctl:description>
    <ctl:java class="com.occamlab.te.util.DateTimeUtils" 
			method="getBeginningInstant" />
  </ctl:function>

  <ctl:function name="ctl:startStopwatch">
    <ctl:param name="watchName"/>
    <ctl:description>Starts a stopwatch with the supplied name (identifier)</ctl:description>
    <ctl:java class="com.occamlab.te.util.Stopwatch" method="start"/>
  </ctl:function>

  <ctl:function name="ctl:elapsedTime">
    <ctl:param name="watchName"/>
    <ctl:description>Returns elapsed time in milliseconds for stopwatich with the supplied name 
    (identifier) if it was started, or 0 if not.</ctl:description>	
    <ctl:java class="com.occamlab.te.util.Stopwatch" method="elapsedTime"/>
  </ctl:function>

  <ctl:function name="ctl:putLogCache">
    <ctl:param name="id"/>
    <ctl:param name="xmlToCache"/>
    <ctl:description>puts the xmlToCache in the current log file with the specified id</ctl:description>
    <ctl:code>
      <xsl:variable name="xmlToCacheDoc">
        <xsl:copy-of select="$xmlToCache"/>
      </xsl:variable>
      <xsl:copy-of select="tec:putLogCache($te:core, $id, $xmlToCache)" xmlns:tec="java:com.occamlab.te.TECore"/>
    </ctl:code>
  </ctl:function>

  <ctl:function name="ctl:getLogCache">
    <ctl:param name="id"/>
    <ctl:description>returns the contents of the previous log cache element with the specified id</ctl:description>
    <ctl:code>
      <xsl:copy-of select="tec:getLogCache($te:core, $id)" xmlns:tec="java:com.occamlab.te.TECore"/>
    </ctl:code>
  </ctl:function>

  <ctl:function name="ctl:addDomAttr">
    <ctl:param name="doc"/>
    <ctl:param name="tag.name"/>
    <ctl:param name="tag.namespace"/>
    <ctl:param name="attr.name"/>
    <ctl:param name="attr.value"/>
    <ctl:description>Adds a given attribute to the nodes of a document, retrieved by the xpath expression.</ctl:description>
    <ctl:java class="com.occamlab.te.util.DomUtils" 
					method="addDomAttr"/>
  </ctl:function>   

  <ctl:function name="ctl:getPathFromString">
    <ctl:param name="filepath"/>
    <ctl:description>Extracts the path portion of a filepath (minus the filename).</ctl:description>
    <ctl:java class="com.occamlab.te.util.StringUtils" 
					method="getPathFromString"/>
  </ctl:function>

  <ctl:function name="ctl:getResourceURL">
    <ctl:param name="resourcepath"/>
    <ctl:description>Returns the URL for the resource at a given resource path.</ctl:description>
    <ctl:java class="com.occamlab.te.util.Misc" method="getResourceURL"/>
  </ctl:function>

  <ctl:function name="ctl:encode">
    <ctl:param name="s">String to encode</ctl:param>
    <ctl:description>Returns the URL encoded form of a string.</ctl:description>
    <ctl:java class="java.net.URLEncoder" method="encode"/>
  </ctl:function>

  <ctl:function name="te:isWeb">
    <ctl:description>Determines whether TEAM Engine is running as a web application.  Returns 'true' or 'false'.</ctl:description>
    <ctl:code>
      <xsl:value-of select="string(tec:isWeb($te:core))" xmlns:tec="java:com.occamlab.te.TECore"/>
    </ctl:code>
  </ctl:function>

  <ctl:function name="ctl:getSessionDir">
    <ctl:description>Returns the location of the current test session directory.</ctl:description>
    <ctl:code>
      <xsl:value-of select="tec:getTestRunDirectory($te:core)" xmlns:tec="java:com.occamlab.te.TECore"/>
    </ctl:code>
  </ctl:function>
</ctl:package>
