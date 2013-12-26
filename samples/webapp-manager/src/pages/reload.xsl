<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:desc="http://expath.org/ns/webapp/descriptor"
                xmlns:zip="http://expath.org/ns/zip"
                xmlns:app="http://servlex.net/ns/webapp-manager"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://servlex.net/app/manager/pages/reload.xsl</pkg:import-uri>

   <xsl:param name="web:input" required="yes"/>

   <xsl:template match="document-node()">
      <xsl:message terminate="yes">
         <xsl:text>Unexpected document?!?: </xsl:text>
         <xsl:value-of select="name(*)"/>
         <xsl:text> (expect a web:request).</xsl:text>
      </xsl:message>
   </xsl:template>

   <xsl:template match="document-node(element(web:request))">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="web:request">
      <xsl:variable name="repo"                  select="web:repository()"/>
      <xsl:variable name="method" as="xs:string" select="@method"/>
      <page menu="webapps">
         <title>Reload</title>
         <xsl:choose>
            <xsl:when test="not($method eq 'post')">
               <xsl:sequence select="
                   error(
                     xs:QName('app:method-not-allowed'),
                     concat('Method not allowed, need POST, got ', $method))"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:apply-templates select="." mode="reload">
                  <xsl:with-param name="repo" select="$repo"/>
               </xsl:apply-templates>
            </xsl:otherwise>
         </xsl:choose>
      </page>
   </xsl:template>

   <xsl:template match="web:request" mode="reload">
      <xsl:param name="repo" required="yes"/>
      <xsl:variable name="apps" select="web:reload-webapps($repo)"/>
      <xsl:choose>
         <xsl:when test="exists($apps)">
            <para>
               <xsl:text>Cache properly reloaded, with the following apps: </xsl:text>
               <xsl:for-each select="$apps">
                  <xsl:sort select="."/>
                  <link href="../{ encode-for-uri(.) }/">
                     <xsl:value-of select="."/>
                  </link>
                  <xsl:if test="position() ne last()">
                     <xsl:text>, </xsl:text>
                  </xsl:if>
               </xsl:for-each>
               <xsl:text>.</xsl:text>
            </para>
         </xsl:when>
         <xsl:otherwise>
            <para>
               <xsl:text>Error reloading the cache. Please see logs for more information.</xsl:text>
            </para>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
