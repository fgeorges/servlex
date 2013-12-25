<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://expath.org/ns/test/servlex/filters-app-webapp"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://expath.org/ns/test/servlex/filters-app-webapp/servlet.xsl</pkg:import-uri>

   <xsl:function name="app:hello-xslt">
      <xsl:param name="request" as="element(web:request)"/>
      <xsl:variable name="who"       select="$request/web:param[@name eq 'who']/@value"/>
      <xsl:variable name="greetings" select="concat('Hello, ', $who, '!')"/>
      <web:response status="200" message="Ok">
         <!-- Depends on the Accept header actually, see
              http://www.w3.org/TR/xhtml-media-types/.  Good candidate
              for a tool function provided by Servlex itself. -->
         <web:body content-type="application/xhtml+xml" method="xhtml"/>
      </web:response>
      <html>
         <head>
            <title>
               <xsl:value-of select="$greetings"/>
            </title>
         </head>
         <body>
            <p>
               <xsl:value-of select="$greetings"/>
            </p>
         </body>
      </html>
   </xsl:function>

</xsl:stylesheet>
