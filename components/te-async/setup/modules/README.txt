Make these packages available to the test harness. When run in a webapp
context, put them in the WEB-INF/classes/modules/ directory.

NOTE:
Ensure that the proper web context is specified in the web.xml file
for the callback handler servlet as follows (add additional xpointer
parameters depending on the expected responses in the test suite):

<servlet>
	<servlet-name>CallbackHandlerServlet</servlet-name>
	<servlet-class>net.sf.teamengine.async.CallbackHandlerServlet</servlet-class>
	<!-- XPointer expressions, which the CallbackHandler can use to determine where to store each response -->
	<init-param>
		<param-name>HarvestResponse</param-name>
		<param-value>xmlns(csw=http://www.opengis.net/cat/csw/2.0.2)xpointer(//csw:TransactionResponse/csw:TransactionSummary/@requestId)</param-value>
	</init-param>
	<init-param>
		<param-name>GetRecordsResponse</param-name>
		<param-value>xmlns(csw=http://www.opengis.net/cat/csw/2.0.2)xpointer(//csw:RequestId)</param-value>
	</init-param>
</servlet>
<servlet-mapping>
	<servlet-name>CallbackHandlerServlet</servlet-name>
	<url-pattern>/callback/http</url-pattern>
</servlet-mapping>    