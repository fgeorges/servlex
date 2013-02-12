<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://expath.org/ns/samples/servlex/hello"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://expath.org/ns/samples/servlex/hello.xsl</pkg:import-uri>

   <xsl:function name="app:hello-xslt">
      <xsl:param name="input" as="item()+"/>
      <xsl:variable name="request" select="$input[1]" as="element(web:request)"/>
      <xsl:variable name="bodies"  select="$input[2]" as="item()*"/>
      <xsl:variable name="who" select="$request/web:param[@name eq 'who']/@value"/>
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
               <xsl:text> (in XSLT)</xsl:text>
            </p>
         </body>
      </html>
   </xsl:function>

   <xsl:template name="app:hello-template">
      <xsl:param name="web:input" as="item()+"/>
      <xsl:sequence select="app:hello-xslt($web:input)"/>
   </xsl:template>

   <xsl:template match="/">
      <xsl:sequence select="app:hello-xslt(node())"/>
   </xsl:template>

</xsl:stylesheet>
