<?xml version="1.0" encoding="UTF-8"?>
<ctl:package xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:ex="http://example.org/">

  <ctl:suite name="ex:num-parity">
    <ctl:title>Sample test suite</ctl:title>
    <ctl:description>Checks the parity of a number.</ctl:description>
    <ctl:starting-test>ex:num-parity-main</ctl:starting-test>
  </ctl:suite>

  <ctl:test name="ex:num-parity-main">
    <ctl:param name="input"/>
    <ctl:assertion>The input number satisfies all constraints.</ctl:assertion>
    <ctl:code>
      <xsl:choose>
        <xsl:when test="$input castable as xsd:integer">
          <ctl:call-test name="ex:is-even">
            <ctl:with-param name="num" select="$input"/>
          </ctl:call-test>
        </xsl:when>
        <xsl:otherwise>
          <ctl:message>Non-integer input: <xsl:value-of select="$input"/></ctl:message>
          <ctl:fail />
        </xsl:otherwise>
      </xsl:choose>
    </ctl:code>
  </ctl:test>

  <ctl:test name="ex:is-even">
    <ctl:param name="num"/>
    <ctl:assertion>The integer is even.</ctl:assertion>
    <ctl:code>
      <xsl:if test="(xsd:integer($num) mod 2) != 0">
        <ctl:message>[FAIL] Not an even number: <xsl:value-of select="$num"/></ctl:message>
        <ctl:fail />
      </xsl:if>
    </ctl:code>
  </ctl:test>

</ctl:package>
