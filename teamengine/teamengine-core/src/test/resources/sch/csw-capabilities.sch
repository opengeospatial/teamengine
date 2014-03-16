<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron"
  defaultPhase="DefaultPhase" version="1.5">

  <sch:title>Rules for CSW-2.0.2 GetCapabilities response entities.</sch:title>

  <sch:ns prefix="csw" uri="http://www.opengis.net/cat/csw/2.0.2" />
  <sch:ns prefix="ows" uri="http://www.opengis.net/ows" />
  <sch:ns prefix="ogc" uri="http://www.opengis.net/ogc" />
  <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink" />

  <sch:phase id="DefaultPhase">
    <sch:active pattern="CapabilitiesPattern" />
    <sch:active pattern="ServiceInfoPattern" />
  </sch:phase>

  <sch:phase id="CapabilitiesDocPhase">
    <sch:active pattern="CapabilitiesPattern" />
  </sch:phase>
  
  <sch:phase id="AbbreviatedContentPhase">
    <sch:active pattern="MinimalContentPattern"/>
  </sch:phase>

  <sch:pattern id="CapabilitiesPattern" name="CapabilitiesPattern">
    <sch:p xml:lang="en">Checks that the document is a CSW v2.0.2 capabilities
      document.</sch:p>
    <sch:rule id="docElement" context="/">
      <sch:assert id="docElement.infoset" test="csw:Capabilities"
        diagnostics="includedDocElem"> The
        document element must have [local name] = "Capabilities"
        and [namespace name] =
        "http://www.opengis.net/cat/csw/2.0.2".
      </sch:assert>
      <sch:assert id="docElement.version" test="csw:Capabilities/@version='2.0.2'"> The @version
        attribute must have the value "2.0.2".
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="MinimalContentPattern" name="MinimalContentPattern">
    <sch:p xml:lang="en">Checks that all optional child elements are not included.</sch:p>
    <sch:rule id="optional.content" context="/csw:Capabilities">
      <sch:report id="NoServiceIdentification" test="ows:ServiceIdentification"> The
        ows:ServiceIdentification element is included. </sch:report>
      <sch:report id="NoServiceProvider" test="ows:ServiceProvider"> The ows:ServiceProvider element
        is included. </sch:report>
      <sch:report id="NoOperationsMetadata" test="ows:OperationsMetadata"> The
        ows:OperationsMetadata element is included. </sch:report>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="ServiceInfoPattern" name="ServiceInfoPattern">
    <sch:p xml:lang="en">Checks for the presence of all optional child
      elements.</sch:p>
    <sch:rule id="docElement.children" context="/csw:Capabilities">
      <sch:assert id="ServiceIdentification" test="ows:ServiceIdentification"> Document is
        incomplete: the ows:ServiceIdentification element is missing.
      </sch:assert>
      <sch:assert id="ServiceProvider" test="ows:ServiceProvider"> Document is incomplete:
        the
        ows:ServiceProvider element is missing.
      </sch:assert>
      <sch:assert id="OperationsMetadata" test="ows:OperationsMetadata"> Document is
        incomplete: the
        ows:OperationsMetadata element is missing.
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:diagnostics>
    <sch:diagnostic id="includedDocElem">
      The included document element has [local name] =
      <sch:value-of select="local-name(/*[1])" />
      and [namespace name] =
      <sch:value-of select="namespace-uri(/*[1])" />.
    </sch:diagnostic>
  </sch:diagnostics>
</sch:schema>
