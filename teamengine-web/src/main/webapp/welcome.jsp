<div>
		<p>
			The Test, Evaluation, And Measurement (TEAM) Engine is a test script interpreter.
			It executes test scripts written using Compliance Test Language (CTL)
			to verify that an implementation of a specification complies with the specification.
		</p>
<%--
		<p>
			<span>The following test suites are available:</span>
			<ul>
<%
  com.occamlab.te.web.Config conf = new com.occamlab.te.web.Config();
  for (com.occamlab.te.index.SuiteEntry suite : conf.getSuites().values()) {
	out.print("<li>");
	String link = suite.getLink();
	if (link == null) {
		out.print(suite.getTitle());
	} else {
		out.print("<a href=\"" + link + "\">" + suite.getTitle() + "</a>");
	}
	out.println("<br/>");
	String desc = suite.getDescription();
	if (desc != null) {
		out.println(desc);
	}
    out.println("<br/>");
    if (null != suite.getDataLink()) {
		out.print("<a href=\"" + suite.getDataLink() + "\">" + "Test data</a>");
	}
	out.print("</li>");
  }
%>
			</ul>
		</p>
        <p>
        <img alt="WARNING!" src="images/warn.png" align="bottom" hspace="4" />
        It may be necessary to load test data before running a test suite!
        </p>
--%>
 </div>
