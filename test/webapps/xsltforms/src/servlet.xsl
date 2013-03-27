<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://expath.org/ns/test/servlex/xsltforms-webapp"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://expath.org/ns/test/servlex/xsltforms-webapp/servlet.xsl</pkg:import-uri>

   <xsl:function name="app:echo">
      <xsl:param name="input" as="item()+"/>
      <web:response status="200" message="Ok">
         <web:body content-type="application/xml" method="xml"/>
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

</xsl:stylesheet>
