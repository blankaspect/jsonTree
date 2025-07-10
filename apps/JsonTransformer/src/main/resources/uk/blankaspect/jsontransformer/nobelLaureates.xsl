<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
   version="2.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
   xmlns:fn="http://www.w3.org/2005/xpath-functions"
   exclude-result-prefixes="xsd fn">

  <xsl:variable name="fields" as="xsd:string*">
    <xsl:for-each select="//array">
      <xsl:sequence select="@name"/>
    </xsl:for-each>
  </xsl:variable>

  <xsl:variable name="years" as="xsd:integer*">
    <xsl:for-each select="//object/number[@name eq 'year']/..">
      <xsl:sort data-type="number" select="number/@value"/>
      <xsl:sequence select="number/@value"/>
    </xsl:for-each>
  </xsl:variable>

  <xsl:variable name="root" select="/"/>

  <xsl:template match="/">
    <array>
      <xsl:for-each select="fn:distinct-values($years)">
        <xsl:variable name="year" select="."/>
        <object>
          <number name="year" value="{$year}"/>
          <xsl:for-each select="$fields">
            <xsl:variable name="field" select="."/>
            <xsl:variable
               name="winner"
               select="$root//array[@name eq $field]/object/number[@value eq xsd:string($year)]/../object"/>
            <xsl:if test="$winner">
              <object name="{$field}">
                <string name="primaryName"   value="{$winner/string[@name eq 'primaryName']/@value}"/>
                <string name="secondaryName" value="{$winner/string[@name eq 'secondaryName']/@value}"/>
              </object>
            </xsl:if>
          </xsl:for-each>
        </object>
      </xsl:for-each>
    </array>
  </xsl:template>

</xsl:stylesheet>
