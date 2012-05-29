<?xml version="1.0" encoding="UTF-8"?>
<ctl:package xmlns:ctl="http://www.occamlab.com/ctl"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:note="http://example.net/note-test">

  <ctl:suite name="note:note-test">
    <ctl:title>Sample test suite</ctl:title>
    <ctl:description>
	Checks the content of a note retrieved from http://www.w3schools.com/xml/note.xml
	</ctl:description>
    <ctl:starting-test>note:main</ctl:starting-test>
  </ctl:suite>

  <ctl:test name="note:main">
    <ctl:assertion>The note is valid.</ctl:assertion>
    <ctl:code>
      <xsl:variable name="response">
        <ctl:request>
          <ctl:url>http://www.w3schools.com/xml/note.xml</ctl:url>
        </ctl:request>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$response/note">
          <ctl:call-test name="note:check-heading">
            <ctl:with-param name="heading" select="$response/note/heading"/>
          </ctl:call-test>
          <ctl:call-test name="note:check-user">
            <ctl:with-param name="user" select="$response/note/to" label="The 'to' user"/>
          </ctl:call-test>
          <ctl:call-test name="note:check-user">
            <ctl:with-param name="user" select="$response/note/from" label="The 'from' user"/>
          </ctl:call-test>
        </xsl:when>
        <xsl:otherwise>
          <ctl:message>Failed to retrieve the note.</ctl:message>
          <ctl:fail/>
        </xsl:otherwise>
      </xsl:choose>
    </ctl:code>
  </ctl:test>

  <ctl:test name="note:check-heading">
    <ctl:param name="heading"/>
    <ctl:assertion>The heading contains more than whitespace.</ctl:assertion>
    <ctl:code>
      <xsl:if test="normalize-space($heading)=''">
        <ctl:fail/>
      </xsl:if>
    </ctl:code>
  </ctl:test>

  <ctl:test name="note:check-user">
    <ctl:param name="user"/>
    <ctl:assertion>{$user} is valid.</ctl:assertion>
    <ctl:code>
      <xsl:choose>
        <xsl:when test="$user='Tove'"/>
        <xsl:when test="$user='Jim'"/>
        <xsl:when test="$user='Jan'"/>
        <xsl:otherwise>
        <ctl:fail/>
        </xsl:otherwise>
      </xsl:choose>
    </ctl:code>
  </ctl:test>
</ctl:package>