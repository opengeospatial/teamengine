<?xml version="1.0" encoding="UTF-8"?>
<!-- Global functions, always included in the source when compiling a test suite -->
<ctl:package
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:ctl="http://www.occamlab.com/ctl">
   
	<ctl:function name="ctl:getImageType">
		<ctl:param name="image.uri"/>
		<ctl:description>Returns the image type name (null if invalid).</ctl:description>
		<ctl:java class="com.occamlab.te.parsers.ImageParser" 
                  method="getImageType"/>
	</ctl:function>   
   
	<ctl:function name="ctl:dateTimeConverter">
		<ctl:param name="dateTime"/>
		<ctl:description>Converts a string to a correct dateTime representation.</ctl:description>
		<ctl:java class="com.occamlab.te.util.DateTimeConverter" 
			method="getDateTimeValue"
			initialized="true"/>
	</ctl:function>   
   
</ctl:package>