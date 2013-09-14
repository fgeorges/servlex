<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:web="http://expath.org/ns/webapp"
                exclude-result-prefixes="web"
                version="1.0">

   <xsl:output indent="yes"/>

   <xsl:param name="root" select="/.."/>
   <xsl:param name="pkg"  select="/.."/>

   <xsl:template match="node()" priority="-1">
      <xsl:copy>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="*">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="web:webapp">
      <xsl:if test="not(@root = $root)">
         <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
         </xsl:copy>
      </xsl:if>
   </xsl:template>

   <xsl:template match="web:webapps">
      <xsl:copy>
         <xsl:apply-templates select="*"/>
         <webapp xmlns="http://expath.org/ns/webapp" root="{ $root }">
            <package name="{ $pkg }"/>
         </webapp>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>
