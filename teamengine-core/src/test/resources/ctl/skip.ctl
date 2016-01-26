<?xml version="1.0" encoding="UTF-8"?>
<ctl:package xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:ex="http://example.org/">

  <ctl:suite name="ex:skip">
    <ctl:title>Sample test suite</ctl:title>
    <ctl:description>Skip all constituent tests (odd integer greater than 100).</ctl:description>
    <ctl:starting-test>ex:skip-main</ctl:starting-test>
  </ctl:suite>

  <ctl:test name="ex:skip-main">
    <ctl:param name="input"/>
    <ctl:assertion>The input number satisfies all mandatory constraints.</ctl:assertion>
    <ctl:code>
      <xsl:choose>
        <xsl:when test="$input castable as xsd:integer">
          <ctl:call-test name="ex:test-1">
            <ctl:with-param name="int" select="$input"/>
          </ctl:call-test>
          <ctl:call-test name="ex:test-2">
            <ctl:with-param name="int" select="$input"/>
          </ctl:call-test>
        </xsl:when>
        <xsl:otherwise>
          <ctl:message>Non-integer input: <xsl:value-of select="$input"/></ctl:message>
          <ctl:fail />
        </xsl:otherwise>
      </xsl:choose>
    </ctl:code>
  </ctl:test>

  <ctl:test name="ex:test-1">
    <ctl:param name="int"/>
    <ctl:assertion>Accept even integers.</ctl:assertion>
    <ctl:code>
      <xsl:if test="(xsd:integer($int) mod 2) != 0">
        <ctl:message>[SKIP] Not an even number: <xsl:value-of select="$int"/></ctl:message>
        <ctl:skipped />
      </xsl:if>
    </ctl:code>
  </ctl:test>

  <ctl:test name="ex:test-2">
    <ctl:param name="int"/>
    <ctl:assertion>Accept non-negative integers less than 100.</ctl:assertion>
    <ctl:code>
      <xsl:if test="xsd:integer($int) ge 100">
        <ctl:message>[SKIP] <xsl:value-of select="$int"/> is greater than 100.</ctl:message>
        <ctl:skipped />
      </xsl:if>
      <xsl:if test="xsd:integer($int) lt 0">
        <ctl:message>[FAIL] <xsl:value-of select="$int"/> is negative.</ctl:message>
        <ctl:fail />
      </xsl:if>
    </ctl:code>
  </ctl:test>

</ctl:package>
