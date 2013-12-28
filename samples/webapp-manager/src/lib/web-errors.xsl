<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://servlex.net/ns/webapp-manager"
                exclude-result-prefixes="#all"
                version="2.0">

   <xsl:import href="view.xsl"/>

   <pkg:import-uri>http://servlex.net/ns/webapp-manager/web-errors.xsl</pkg:import-uri>

   <xsl:param name="web:input"         required="yes"/>
   <xsl:param name="web:error-data"    required="yes"/>
   <xsl:param name="web:error-code"    required="yes"/>
   <xsl:param name="web:error-message" required="yes"/>

<!--xsl:template match="/">
   <xsl:message>
      <xsl:text>CONTEXT ITEM: </xsl:text>
      <xsl:copy-of select="."/>
      <xsl:text>&#10;$WEB:INPUT: </xsl:text>
      <xsl:copy-of select="$web:input"/>
      <xsl:text>&#10;$WEB:ERROR-DATA: </xsl:text>
      <xsl:copy-of select="$web:error-data"/>
      <xsl:text>&#10;$WEB:ERROR-CODE: </xsl:text>
      <xsl:copy-of select="$web:error-code"/>
      <xsl:text>&#10;$WEB:ERROR-MESSAGE: </xsl:text>
      <xsl:copy-of select="$web:error-message"/>
   </xsl:message>
</xsl:template-->

   <xsl:template match="document-node()[empty(web:request)]">
      <xsl:message terminate="yes">
         <xsl:text>Unexpected document?!?: </xsl:text>
         <xsl:value-of select="name(*)"/>
         <xsl:text> (expect a web:request).</xsl:text>
      </xsl:message>
   </xsl:template>

   <xsl:template match="document-node()[exists(web:request)]">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="web:request">
      <xsl:variable name="page" as="element()">
         <page menu="none">
            <title>Oops</title>
            <para>
               <xsl:text>Error: </xsl:text>
               <xsl:value-of select="$web:error-message"/>
            </para>
         </page>
      </xsl:variable>
      <xsl:apply-templates select="$page"/>
   </xsl:template>

</xsl:stylesheet>
