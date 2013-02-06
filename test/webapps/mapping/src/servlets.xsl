<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://expath.org/ns/test/servlex/mapping-webapp"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://expath.org/ns/test/servlex/mapping-webapp/servlets.xsl</pkg:import-uri>

   <xsl:function name="app:echo">
      <xsl:param name="input" as="item()+"/>
      <web:response status="200" message="Ok">
         <web:body content-type="application/xml"/>
      </web:response>
      <app:request>
         <app:http>
            <xsl:copy-of select="$input[1]"/>
         </app:http>
         <xsl:for-each select="remove($input, 1)">
            <app:body position="{ position() }">
               <xsl:copy-of select="."/>
            </app:body>
         </xsl:for-each>
      </app:request>
   </xsl:function>

   <xsl:template name="app:echo">
      <xsl:param name="web:input" as="item()+"/>
      <!-- both component have the same implem -->
      <xsl:sequence select="app:echo($web:input)"/>
   </xsl:template>

   <xsl:function name="app:pis-in-body">
      <xsl:param name="request" as="element(web:request)"/>
      <web:response status="200" message="Ok">
         <web:body content-type="application/xml">
            <xsl:processing-instruction name="first-pi"  select="''"/>
            <xsl:processing-instruction name="second-pi" select="'with some data inside'"/>
            <server>data</server>
         </web:body>
      </web:response>
   </xsl:function>

</xsl:stylesheet>
