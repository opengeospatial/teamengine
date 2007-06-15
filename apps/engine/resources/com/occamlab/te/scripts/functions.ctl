<?xml version="1.0" encoding="UTF-8"?>
<!-- Global functions, always included in the source when compiling a test suite -->
<ctl:package
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:ctl="http://www.occamlab.com/ctl">
   
	<ctl:function name="ctl:get-image-type">
		<ctl:param name="image.uri"/>
		<ctl:description>Returns the image type name (null if invalid).</ctl:description>
		<ctl:java class="com.occamlab.te.parsers.ImageParser" 
                  method="getImageType"/>
	</ctl:function>   
   
</ctl:package>