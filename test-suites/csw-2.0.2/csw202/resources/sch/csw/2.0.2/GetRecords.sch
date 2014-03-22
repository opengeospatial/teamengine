<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron" 
  defaultPhase="DefaultPhase" 
  version="1.5">
  
  <sch:title>Rules for CSW-2.0.2 GetRecords response entities.</sch:title>
  
  <sch:ns prefix="csw" uri="http://www.opengis.net/cat/csw/2.0.2"/>
  <sch:ns prefix="dc" uri="http://purl.org/dc/elements/1.1/"/>
  <sch:ns prefix="dct" uri="http://purl.org/dc/terms/"/>
  
  <sch:phase id="DefaultPhase">
    <sch:active pattern="GetRecordsResponsePattern"/>
    <sch:active pattern="NoRecordsPattern"/>
    <sch:active pattern="OneOrMoreHitsPattern"/>
  </sch:phase>
  
  <sch:phase id="ZeroHitsPhase">
    <sch:active pattern="GetRecordsResponsePattern"/>
    <sch:active pattern="NoRecordsPattern"/>
    <sch:active pattern="ZeroHitsPattern"/>
  </sch:phase>
  
  <sch:phase id="TenRecordsPhase">
    <sch:active pattern="GetRecordsResponsePattern"/>
    <sch:active pattern="TenRecordsPattern"/>
  </sch:phase>
  
  <sch:phase id="OneToTenRecordsPhase">
    <sch:active pattern="GetRecordsResponsePattern"/>
    <sch:active pattern="OneOrMoreHitsPattern"/>
    <sch:active pattern="OneToTenRecordsPattern"/>
  </sch:phase>
  
  <sch:phase id="OneToTenAdHocRecordsPhase">
    <sch:active pattern="GetRecordsResponsePattern"/>
    <sch:active pattern="OneOrMoreHitsPattern"/>
    <sch:active pattern="OneToTenRecordsPattern"/>
    <sch:active pattern="IdTypeDateElementsPattern"/>
  </sch:phase>
  
  <sch:phase id="IdTypeDateElementsPhase">
    <sch:active pattern="GetRecordsResponsePattern"/>
    <sch:active pattern="TenRecordsPattern"/>
    <sch:active pattern="CSWRecordsPattern"/>
    <sch:active pattern="IdTypeDateElementsPattern"/>
  </sch:phase>
  
  <sch:phase id="TypeFormatElementsPhase">
    <sch:active pattern="GetRecordsResponsePattern"/>
    <sch:active pattern="OneToTenRecordsPattern"/>
    <sch:active pattern="TypeFormatElementsPattern"/>
  </sch:phase>
  
  <sch:phase id="BoxDateElementsPhase">
    <sch:active pattern="GetRecordsResponsePattern"/>
    <sch:active pattern="OneToTenRecordsPattern"/>
    <sch:active pattern="BoxDateElementsPattern"/>
  </sch:phase>
  
  <sch:pattern id="GetRecordsResponsePattern" name="GetRecordsResponsePattern">
    <sch:p xml:lang="en">Checks that the document element is csw:GetRecordsResponse.</sch:p>
    <sch:rule id="docElement" context="/">
      <sch:assert id="docElement.infoset" 
        test="csw:GetRecordsResponse"
        diagnostics="includedDocElem">
	The document element must have [local name] = "GetRecordsResponse" and [namespace name] = "http://www.opengis.net/cat/csw/2.0.2".
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="NoRecordsPattern" name="NoRecordsPattern">
    <sch:p xml:lang="en">Checks that no records are included in the response.</sch:p>
    <sch:rule id="HitsOnly" context="/csw:GetRecordsResponse">
      <sch:assert id="SearchResults.empty" 
        test="count(csw:SearchResults/*) = 0"
        diagnostics="recordCount">
	The csw:SearchResults element must be empty.
      </sch:assert>
      <sch:assert id="ZeroRecordsReturned" 
        test="csw:SearchResults/@numberOfRecordsReturned = 0">
	csw:SearchResults/@numberOfRecordsReturned must have the value zero.
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="ZeroHitsPattern" name="ZeroHitsPattern">
    <sch:p xml:lang="en">Checks that no hits are reported.</sch:p>
    <sch:rule id="ZeroHits" context="/csw:GetRecordsResponse">
      <sch:assert id="NoRecordsMatched" 
        test="csw:SearchResults/@numberOfRecordsMatched = 0">
	csw:SearchResults/@numberOfRecordsMatched != 0.
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="OneOrMoreHitsPattern" name="OneOrMoreHitsPattern">
    <sch:p xml:lang="en">Checks that one or more hits are reported.</sch:p>
    <sch:rule id="OneOrMoreHits" context="/csw:GetRecordsResponse">
      <sch:assert id="OneOrMoreRecordsMatched" 
        test="csw:SearchResults/@numberOfRecordsMatched > 0">
	csw:SearchResults/@numberOfRecordsMatched must be greater than zero.
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="TenRecordsPattern" name="TenRecordsPattern">
    <sch:p xml:lang="en">Checks that the number of hits exceeds 10 and that only 10 records are included in the response.</sch:p>
    <sch:rule id="TenRecords" context="/csw:GetRecordsResponse">
      <sch:assert id="SearchResults.10" 
        test="count(csw:SearchResults/*) = 10"
        diagnostics="recordCount">
	The csw:SearchResults element must contain 10 records.
      </sch:assert>
      <sch:assert id="RecordsReturnedeq10" 
        test="csw:SearchResults/@numberOfRecordsReturned = 10">
	csw:SearchResults/@numberOfRecordsReturned does not equal 10.
      </sch:assert>
      <sch:assert id="RecordsMatchedgt10" 
        test="csw:SearchResults/@numberOfRecordsMatched > 10"
        diagnostics="hitCount">
	csw:SearchResults/@numberOfRecordsMatched must exceed 10.
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="OneToTenRecordsPattern" name="OneToTenRecordsPattern">
    <sch:p xml:lang="en">
    Checks that 1-10 records are included in the response.
    </sch:p>
    <sch:rule id="OneToTenRecords" context="/csw:GetRecordsResponse">
      <sch:assert id="SearchResults.1-10" 
        test="count(csw:SearchResults/*) &gt; 0 and count(csw:SearchResults/*) &lt;= 10"
        diagnostics="recordCount">
	The csw:SearchResults element must contain 1-10 records.
      </sch:assert>
      <sch:assert id="RecordsReturned.correct" 
        test="csw:SearchResults/@numberOfRecordsReturned = count(csw:SearchResults/*)">
	csw:SearchResults/@numberOfRecordsReturned does not equal the number of records included.
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="CSWRecordsPattern" name="CSWRecordsPattern">
    <sch:p xml:lang="en">Checks that the included records include only csw:Record elements.</sch:p>
    <sch:p xml:lang="en">Checks that each csw:Record includes required elements 
    (dc:identifier, dc:title) and does not include csw:AnyText elements.</sch:p>
    <sch:rule id="CSWRecords" context="/csw:GetRecordsResponse/csw:SearchResults">
      <sch:assert id="onlyCSWRecords" 
        test="count(*) = count(csw:Record)">Only csw:Record elements are expected in the response.</sch:assert>
    </sch:rule>
    <sch:rule id="FullRecordElements" context="//csw:Record">
      <sch:assert id="FullRecordElements.identifier" 
        test="dc:identifier">Missing required dc:identifier element.</sch:assert>
      <sch:assert id="FullRecordElements.title" 
        test="dc:title">Missing required dc:title element.</sch:assert>
      <sch:assert id="FullRecordElements.AnyText" 
        test="csw:AnyText">csw:AnyText elements are prohibited in csw:Record.</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="IdTypeDateElementsPattern" name="IdTypeDateElementsPattern">
    <sch:p xml:lang="en">Checks that csw:Record instances contain only the following three child elements: dc:identifier, dc:type, dc:date</sch:p>
    <sch:rule id="IdTypeDateElements" context="/csw:GetRecordsResponse/csw:SearchResults/csw:Record">
      <sch:assert id="children3" 
        test="count(*) = 3">
	The csw:Record elements must have three child elements.
      </sch:assert>
      <sch:assert id="dc.identifier" 
        test="dc:identifier">Missing dc:identifier element.</sch:assert>
      <sch:assert id="dc.type" 
        test="dc:type">Missing dc:type element.</sch:assert>
      <sch:assert id="dc.date" 
        test="dc:date">Missing dc:date element.</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="TypeFormatElementsPattern" name="TypeFormatElementsPattern">
    <sch:p xml:lang="en">Checks that included records contain the following elements: dc:type, dc:format.</sch:p>
    <sch:rule id="TypeFormatElements" context="/csw:GetRecordsResponse/csw:SearchResults/*">
      <sch:assert id="TypeFormatElements.format" 
        test="dc:format">Missing dc:format element.</sch:assert>
      <sch:assert id="TypeFormatElements.type" 
        test="dc:type">Missing dc:type element.</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="BoxDateElementsPattern" name="BoxDateElementsPattern">
    <sch:p xml:lang="en">Checks that included records contain the following elements: dc:date, ows:BoundingBox.</sch:p>
    <sch:rule id="BoxDateElements" context="/csw:GetRecordsResponse/csw:SearchResults/*">
      <sch:assert id="BoxDateElements.date" 
        test="dc:date">Missing dc:date element.</sch:assert>
      <sch:assert id="BoxDateElements.box" 
        test="ows:BoundingBox">Missing ows:BoundingBox element.</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:diagnostics>
    <sch:diagnostic id="includedDocElem">
    The included document element has [local name] = <sch:value-of select="local-name(/*[1])"/> 
    and [namespace name] = <sch:value-of select="namespace-uri(/*[1])"/>.
    </sch:diagnostic>
    <sch:diagnostic id="recordCount">
    There are <sch:value-of select="count(csw:SearchResults/*)"/> records included in the response.
    </sch:diagnostic>
    <sch:diagnostic id="hitCount">
    There are <sch:value-of select="csw:SearchResults/@numberOfRecordsMatched"/> hits reported.
    </sch:diagnostic>
  </sch:diagnostics>
</sch:schema>
