<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron" defaultPhase="DefaultPhase" version="1.5">

  <sch:title>Rules for CSW-2.0.2 GetCapabilities response entities.</sch:title>

  <sch:ns prefix="csw" uri="http://www.opengis.net/cat/csw/2.0.2"/>
  <sch:ns prefix="ows" uri="http://www.opengis.net/ows"/>
  <sch:ns prefix="ogc" uri="http://www.opengis.net/ogc"/>
  <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>

  <sch:phase id="DefaultPhase">
    <sch:active pattern="CapabilitiesPattern"/>
    <sch:active pattern="ServiceInfoPattern"/>
  </sch:phase>

  <sch:phase id="CapabilitiesDocPhase">
    <sch:active pattern="CapabilitiesPattern"/>
  </sch:phase>

  <sch:phase id="RequiredElementsPhase">
    <sch:active pattern="CapabilitiesPattern"/>
    <sch:active pattern="RequiredBindingsPattern"/>
    <sch:active pattern="RequiredOperatorsPattern"/>
    <sch:active pattern="RequiredConstraintsPattern"/>
    <sch:active pattern="RequiredParametersPattern"/>
  </sch:phase>

  <sch:phase id="AbbreviatedContentPhase">
    <sch:active pattern="CapabilitiesPattern"/>
    <sch:active pattern="MinimalContentPattern"/>
  </sch:phase>

  <sch:pattern id="CapabilitiesPattern" name="CapabilitiesPattern">
    <sch:p xml:lang="en">Checks that the document is a CSW v2.0.2 capabilities document.</sch:p>
    <sch:rule id="docElement" context="/">
      <sch:assert id="docElement.infoset" test="csw:Capabilities" diagnostics="includedDocElem"> The
        document element must have [local name] = "Capabilities" and [namespace name] =
        "http://www.opengis.net/cat/csw/2.0.2". </sch:assert>
      <sch:assert id="docElement.version" test="csw:Capabilities/@version='2.0.2'"> The @version
        attribute must have the value "2.0.2". </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="ServiceInfoPattern" name="ServiceInfoPattern">
    <sch:p xml:lang="en">Checks for the presence of all optional child elements.</sch:p>
    <sch:rule id="docElement.children" context="/csw:Capabilities">
      <sch:assert id="ServiceIdentification" test="ows:ServiceIdentification"> Document is
        incomplete: the ows:ServiceIdentification element is missing. </sch:assert>
      <sch:assert id="ServiceProvider" test="ows:ServiceProvider"> Document is incomplete: the
        ows:ServiceProvider element is missing. </sch:assert>
      <sch:assert id="OperationsMetadata" test="ows:OperationsMetadata"> Document is incomplete: the
        ows:OperationsMetadata element is missing. </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="RequiredBindingsPattern" name="RequiredBindingsPattern">
    <sch:p xml:lang="en"> Checks that all HTTP method bindings required for CSW implementations are
      present. </sch:p>
    <sch:rule id="RequiredBindings" context="/csw:Capabilities">
      <sch:assert id="GetCapabilities-GET"
        test="ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"
        > Missing mandatory binding for GetCapabilities using the GET method. </sch:assert>
      <sch:assert id="DescribeRecord-POST"
        test="ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"
        > Missing mandatory binding for DescribeRecord using the POST method. </sch:assert>
      <sch:assert id="GetRecords-POST"
        test="ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"
        > Missing mandatory binding for GetRecords request using the POST method. </sch:assert>
      <sch:assert id="GetRecordById-GET"
        test="ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"
        > Missing mandatory binding for GetRecordById using the GET method. </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="RequiredConstraintsPattern" name="RequiredConstraintsPattern">
    <sch:p xml:lang="en"> Checks that all required CSW operational constraints are present. </sch:p>
    <sch:rule id="RequiredConstraints" context="/csw:Capabilities/ows:OperationsMetadata">
      <sch:assert id="GetRecordsPostEncoding"
        test="(ows:Constraint[@name='PostEncoding']/ows:Value = 'XML') or 
        (ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post/ows:Constraint[@name='PostEncoding']/ows:Value = 'XML')"
        > Missing mandatory PostEncoding constraint for GetRecords POST, "XML". </sch:assert>
      <sch:assert id="DescribeRecordPostEncoding"
        test="(ows:Constraint[@name='PostEncoding']/ows:Value = 'XML') or 
        (ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post/ows:Constraint[@name='PostEncoding']/ows:Value = 'XML')"
        > Missing mandatory PostEncoding constraint for DescribeRecord POST, "XML". </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="RequiredParametersPattern" name="RequiredParametersPattern">
    <sch:p xml:lang="en"> Checks that all required CSW operational parameters are present. </sch:p>
    <sch:rule id="RequiredCommonParameters" context="//ows:OperationsMetadata">
      <sch:assert id="ServiceParameter"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='service']/ows:Value = 'CSW'"
        > Missing mandatory service parameter, "CSW". </sch:assert>
      <sch:assert id="VersionParameter"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='version']/ows:Value = '2.0.2'"
        > Missing mandatory version parameter, "2.0.2". </sch:assert>
    </sch:rule>
    <sch:rule id="RequiredGetCapabilitiesParameters"
      context="//ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']">
      <sch:assert id="GetCapabilitiesSectionsServiceIdentification"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='sections']/ows:Value = 'ServiceIdentification'"
        > Missing mandatory sections parameter for GetCapabilities, "ServiceIdentification". </sch:assert>
      <sch:assert id="GetCapabilitiesSectionsServiceProvider"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='sections']/ows:Value = 'ServiceProvider'"
        > Missing mandatory sections parameter for GetCapabilities, "ServiceProvider". </sch:assert>
      <sch:assert id="GetCapabilitiesSectionsOperationsMetadata"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='sections']/ows:Value = 'OperationsMetadata'"
        > Missing mandatory sections parameter for GetCapabilities, "OperationsMetadata". </sch:assert>
      <sch:assert id="GetCapabilitiesSectionsFilter_Capabilities"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='sections']/ows:Value = 'Filter_Capabilities'"
        > Missing mandatory sections parameter for GetCapabilities, "Filter_Capabilities".
      </sch:assert>
    </sch:rule>
    <sch:rule id="RequiredDescribeRecordParameters"
      context="//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']">
      <sch:assert id="DescribeRecordTypeName"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='typename']/ows:Value = 'csw:Record'"
        > Missing mandatory TypeName parameter for DescribeRecord, "csw:Record". </sch:assert>
      <sch:assert id="DescribeRecordOutputFormat"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='outputformat']/ows:Value = 'application/xml'"
        > Missing mandatory outputFormat parameter for DescribeRecord, "application/xml". </sch:assert>
      <sch:assert id="DescribeRecordSchemaLanguage"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='schemalanguage']/ows:Value = 'http://www.w3.org/XML/Schema'"
        > Missing mandatory schemaLanguage parameter for DescribeRecord,
        "http://www.w3.org/XML/Schema". </sch:assert>
    </sch:rule>
    <sch:rule id="RequiredGetRecordsParameters"
      context="//ows:OperationsMetadata/ows:Operation[@name='GetRecords']">
      <sch:assert id="GetRecordsTypeNames"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='typenames']/ows:Value = 'csw:Record'"
        > Missing mandatory typeNames parameter for GetRecords, "csw:Record". </sch:assert>
      <sch:assert id="GetRecordsOutputSchema"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='outputschema']/ows:Value = 'http://www.opengis.net/cat/csw/2.0.2'"
        > Missing mandatory outputSchema parameter for GetRecords,
        "http://www.opengis.net/cat/csw/2.0.2". </sch:assert>
      <sch:assert id="GetRecordsOutputFormat"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='outputformat']/ows:Value = 'application/xml'"
        > Missing mandatory outputFormat parameter for GetRecords, "application/xml". </sch:assert>
      <sch:assert id="GetRecordsResultTypeHits"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='resulttype']/ows:Value = 'hits'"
        > Missing mandatory resultType parameter for GetRecords, "hits". </sch:assert>
      <sch:assert id="GetRecordsResultTypeResults"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='resulttype']/ows:Value = 'results'"
        > Missing mandatory resultType parameter for GetRecords, "results". </sch:assert>
      <sch:assert id="GetRecordsResultTypeValidate"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='resulttype']/ows:Value = 'validate'"
        > Missing mandatory resultType parameter for GetRecords, "validate". </sch:assert>
    </sch:rule>
    <sch:rule id="RequiredGetRecordByIdParameters"
      context="//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']">
      <sch:assert id="GetRecordByIdOutputSchema"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='outputschema']/ows:Value = 'http://www.opengis.net/cat/csw/2.0.2'"
        > Missing mandatory outputSchema parameter for GetRecordById,
        "http://www.opengis.net/cat/csw/2.0.2". </sch:assert>
      <sch:assert id="GetRecordByIdOutputFormat"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='outputformat']/ows:Value = 'application/xml'"
        > Missing mandatory outputFormat parameter for GetRecordById, "application/xml". </sch:assert>
      <sch:assert id="GetRecordByIdElementSetNameBrief"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='elementsetname']/ows:Value = 'brief'"
        > Missing mandatory ElementSetName parameter for GetRecordById, "brief". </sch:assert>
      <sch:assert id="GetRecordByIdElementSetNameSummary"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='elementsetname']/ows:Value = 'summary'"
        > Missing mandatory ElementSetName parameter for GetRecordById, "summary". </sch:assert>
      <sch:assert id="GetRecordByIdElementSetNameFull"
        test="ows:Parameter[translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='elementsetname']/ows:Value = 'full'"
        > Missing mandatory ElementSetName parameter for GetRecordById, "full". </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="RequiredOperatorsPattern" name="RequiredOperatorsPattern">
    <sch:p xml:lang="en"> Checks that the mandatory set of filter predicates are supported. </sch:p>
    <sch:rule id="RequiredOperators" context="//ogc:Filter_Capabilities">
      <sch:assert id="SpatialOperators.BBOX"
        test="ogc:Spatial_Capabilities/ogc:SpatialOperators/ogc:SpatialOperator[@name='BBOX']"
        >Missing mandatory spatial operator: BBOX.</sch:assert>
      <sch:assert id="ComparisonOperators.EqualTo"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'EqualTo'"
        >Missing mandatory comparison operator: EqualTo.</sch:assert>
      <sch:assert id="ComparisonOperators.NotEqualTo"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'NotEqualTo'"
        >Missing mandatory comparison operator: NotEqualTo.</sch:assert>
      <sch:assert id="ComparisonOperators.LessThan"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'LessThan'"
        >Missing mandatory comparison operator: LessThan.</sch:assert>
      <sch:assert id="ComparisonOperators.GreaterThan"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'GreaterThan'"
        >Missing mandatory comparison operator: GreaterThan.</sch:assert>
      <sch:assert id="ComparisonOperators.LessThanEqualTo"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'LessThanEqualTo'"
        >Missing mandatory comparison operator: LessThanEqualTo.</sch:assert>
      <sch:assert id="ComparisonOperators.GreaterThanEqualTo"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'GreaterThanEqualTo'"
        >Missing mandatory comparison operator: GreaterThanEqualTo.</sch:assert>
      <sch:assert id="ComparisonOperators.Like"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'Like'"
        >Missing mandatory comparison operator: Like.</sch:assert>
      <sch:assert id="LogicalOperators" test="ogc:Scalar_Capabilities/ogc:LogicalOperators">Missing
        mandatory logical operators (And, Or, Not).</sch:assert>
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

  <sch:diagnostics>
    <sch:diagnostic id="includedDocElem"> The included document element has [local name] =
        <sch:value-of select="local-name(/*[1])"/> and [namespace name] = <sch:value-of
        select="namespace-uri(/*[1])"/>. </sch:diagnostic>
  </sch:diagnostics>
</sch:schema>
