<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
 <!-- Global parsers, always included in the source when compiling a test suite -->
<ctl:package 
 xmlns:parsers="http://www.occamlab.com/te/parsers"
 xmlns:ctl="http://www.occamlab.com/ctl">
 
	<ctl:parser name="parsers:CDataParser">
		<ctl:java class="com.occamlab.te.parsers.CDataParser" method="parse"/>
	</ctl:parser>

	<ctl:parser name="parsers:HTTPParser">
		<ctl:java class="com.occamlab.te.parsers.HTTPParser" method="parse"/>
	</ctl:parser>

	<ctl:parser name="parsers:ImageParser">
		<ctl:java class="com.occamlab.te.parsers.ImageParser" method="parse"/>
	</ctl:parser>

	<ctl:parser name="parsers:NullParser">
		<ctl:java class="com.occamlab.te.parsers.NullParser" method="parse"/>
	</ctl:parser>

	<ctl:parser name="parsers:ZipParser">
		<ctl:java class="com.occamlab.te.parsers.ZipParser" method="parse"/>
	</ctl:parser>
	
	<ctl:parser name="parsers:XMLValidatingParser">
		<ctl:java class="com.occamlab.te.parsers.XMLValidatingParser" method="parse" initialized="true"/>
	</ctl:parser>

	<ctl:parser name="parsers:XSLTransformationParser">
		<ctl:java class="com.occamlab.te.parsers.XSLTransformationParser" method="parse" initialized="true"/>
	</ctl:parser>

	<ctl:parser name="parsers:SchematronValidatingParser">
		<ctl:java class="com.occamlab.te.parsers.SchematronValidatingParser" method="parse" initialized="true"/>
	</ctl:parser>

	<ctl:parser name="parsers:SOAPParser">
		<ctl:java class="com.occamlab.te.parsers.SoapParser" method="parse" initialized="true"/>
	</ctl:parser>

	
</ctl:package>
