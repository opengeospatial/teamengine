<?xml version="1.0" encoding="UTF-8"?>
<ctl:package xmlns:ctl="http://www.occamlab.com/ctl"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:note="http://example.net/note-test">

  <ctl:suite name="note:note-test">
    <ctl:title>Sample test suite</ctl:title>
    <ctl:description>Checks the content of a note.</ctl:description>
    <ctl:starting-test>note:main</ctl:starting-test>
  </ctl:suite>

  <ctl:test name="note:main">
    <ctl:assertion>The note is valid.</ctl:assertion>
    <ctl:code>
      <xsl:variable name="form-data">
        <ctl:form width="800" height="600">
          <div id="input-form" xmlns="http://www.w3.org/1999/xhtml">
            <h2>XML Note</h2>
            <fieldset style="background:#ccffff">
              <legend>Instance Under Test</legend>
              <p>
                <label class="form-label" for="uri">
                  <h4 style="margin-bottom: 0.5em">Location of note (absolute URI)</h4>
                </label>
                <input name="uri" size="96" type="text" value="http://www.w3schools.com/xml/note.xml" />
              </p>
              <p>
                <label class="form-label" for="doc">
                  <h4 style="margin-bottom: 0.5em">Upload document</h4>
                </label>
                <input name="doc" size="96" type="file" />
              </p>
            </fieldset>
            <p>
              <input class="form-button" type="submit" value="Start"/>
              <input class="form-button" type="reset" value="Clear"/>
            </p>
          </div>
        </ctl:form>
      </xsl:variable>
      <xsl:variable name="uri" select="$form-data//value[@key='uri']" />
      <xsl:variable name="file" select="$form-data//value[@key='doc']/ctl:file-entry" />
      <xsl:variable name="iut">
        <xsl:choose>
          <xsl:when test="(string-length($uri) gt 0) and empty($file/@full-path)">
            <ctl:request>
              <ctl:url><xsl:value-of select="$uri"/></ctl:url>
            </ctl:request>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="doc(concat('file:///', $file/@full-path))" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="exists($iut)">
          <ctl:call-test name="note:check-heading">
            <ctl:with-param name="heading" select="$iut//heading"/>
          </ctl:call-test>
          <ctl:call-test name="note:check-user">
            <ctl:with-param name="user" select="$iut/note/to" label="The 'to' user"/>
          </ctl:call-test>
          <ctl:call-test name="note:check-user">
            <ctl:with-param name="user" select="$iut/note/from" label="The 'from' user"/>
          </ctl:call-test>
        </xsl:when>
        <xsl:otherwise>
          <ctl:message>Failed to obtain note.</ctl:message>
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