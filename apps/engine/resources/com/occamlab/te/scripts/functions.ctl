<?xml version="1.0" encoding="UTF-8"?>
<!-- Global functions--these are always included when processing a test suite -->
<ctl:package
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:ctl="http://www.occamlab.com/ctl">
   
	<ctl:function name="ctl:getImageType">
		<ctl:param name="image.uri"/>
		<ctl:description>Returns the image type name (empty string if invalid).</ctl:description>
		<ctl:java class="com.occamlab.te.parsers.ImageParser" 
                  method="getImageType"/>
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
   
</ctl:package>