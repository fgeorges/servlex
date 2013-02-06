<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://expath.org/ns/test/servlex/mapping-webapp"
                exclude-result-prefixes="#all"
                version="2.0">

   <xsl:import href="servlets.xsl"/>

   <pkg:import-uri>http://expath.org/ns/test/servlex/mapping-webapp/echo.xsl</pkg:import-uri>

   <xsl:param name="web:input" as="item()+"/>

   <xsl:template match="/">
      <!-- both component have the same implem -->
      <xsl:sequence select="app:echo($web:input)"/>
   </xsl:template>

</xsl:stylesheet>
