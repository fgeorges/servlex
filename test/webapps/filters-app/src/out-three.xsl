<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://expath.org/ns/test/servlex/filters-app-webapp"
                xmlns:h="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://expath.org/ns/test/servlex/filters-app-webapp/out-three.xsl</pkg:import-uri>

   <xsl:param name="web:input" as="node()+"/>

   <xsl:template match="web:response">
      <xsl:sequence select="."/>
      <xsl:apply-templates select="$web:input[2]"/>
   </xsl:template>

   <xsl:template match="node()">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates select="node()"/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="h:title">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates select="node()"/>
         <xsl:text> (out 3)</xsl:text>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>
