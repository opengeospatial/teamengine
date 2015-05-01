<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron" 
  defaultPhase="DefaultPhase" 
  version="1.5">
  
  <sch:title>Rules for CSW-2.0.2 DescribeRecordResponse entities.</sch:title>
  
  <sch:ns prefix="csw" uri="http://www.opengis.net/cat/csw/2.0.2"/>
  <sch:ns prefix="xsd" uri="http://www.w3.org/2001/XMLSchema"/>
  
  <sch:phase id="DefaultPhase">
    <sch:active pattern="DescribeRecordResponsePattern"/>
    <sch:active pattern="OneOrMoreSchemaComponentsPattern"/>
    <sch:active pattern="DefaultSchemaComponentPattern"/>
  </sch:phase>
  
  <sch:phase id="NoSchemaComponentsPhase">
    <sch:active pattern="DescribeRecordResponsePattern"/>
    <sch:active pattern="NoSchemaComponentsPattern"/>
  </sch:phase>
  
  <sch:pattern id="DescribeRecordResponsePattern" name="DescribeRecordResponsePattern">
    <sch:p xml:lang="en">Checks that the document element is csw:DescribeRecordResponse.</sch:p>
    <sch:rule id="docElement" context="/">
      <sch:assert id="docElement.infoset" 
        test="csw:DescribeRecordResponse"
        diagnostics="includedDocElem">
	The document element must have [local name] = "DescribeRecordResponse" and [namespace name] = "http://www.opengis.net/cat/csw/2.0.2".
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="OneOrMoreSchemaComponentsPattern" name="OneOrMoreSchemaComponentsPattern">
    <sch:p xml:lang="en">Checks for the presence of one or more schema components.</sch:p>
    <sch:rule id="OneOrMoreSchemaComponents" context="/csw:DescribeRecordResponse">
      <sch:assert id="SchemaComponent.OneOrMore" 
        test="count(csw:SchemaComponent) >= 1">
	Document is incomplete: at least one csw:SchemaComponent element must be present.
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="NoSchemaComponentsPattern" name="NoSchemaComponentsPattern">
    <sch:p xml:lang="en">Checks for the absence of schema components.</sch:p>
    <sch:rule id="NoSchemaComponents" context="/csw:DescribeRecordResponse">
      <sch:assert id="SchemaComponent.none" 
        test="count(csw:SchemaComponent) = 0">
	Expected no csw:SchemaComponent elements to be included.
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="DefaultSchemaComponentPattern" name="DefaultSchemaComponentPattern">
    <sch:p xml:lang="en">Checks the values of required SchemaComponent attributes.</sch:p>
    <sch:rule id="DefaultSchemaComponent" context="/csw:DescribeRecordResponse">
      <sch:assert id="DefaultSchemaComponent.targetNamespace" 
        test="csw:SchemaComponent[@targetNamespace = 'http://www.opengis.net/cat/csw/2.0.2']">
	Expected at least one csw:SchemaComponent/@targetNamespace: "http://www.opengis.net/cat/csw/2.0.2"
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:diagnostics>
    <sch:diagnostic id="includedDocElem">
    The included document element has [local name] = <sch:value-of select="local-name(/*[1])"/> 
    and [namespace name] = <sch:value-of select="namespace-uri(/*[1])"/>.
    </sch:diagnostic>
  </sch:diagnostics>
</sch:schema>
