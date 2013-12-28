<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:desc="http://expath.org/ns/webapp/descriptor"
                xmlns:zip="http://expath.org/ns/zip"
                xmlns:app="http://servlex.net/ns/webapp-manager"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://servlex.net/app/manager/pages/remove.xsl</pkg:import-uri>

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
      <xsl:variable name="root"   as="xs:string" select="web:path/web:match[@name eq 'root']"/>
      <page menu="webapps" root="..">
         <title>Remove</title>
         <xsl:choose>
            <xsl:when test="not(web:install-enabled($repo))">
               <xsl:sequence select="
                   error(
                     xs:QName('app:not-implemented'),
                     'Remove not supported, storage is read-only')"/>
            </xsl:when>
            <xsl:when test="not($method eq 'get')">
               <xsl:sequence select="
                   error(
                     xs:QName('app:method-not-allowed'),
                     concat('Method not allowed, need GET, got ', $method))"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:apply-templates select="." mode="remove">
                  <xsl:with-param name="repo" select="$repo"/>
                  <xsl:with-param name="root" select="$root"/>
               </xsl:apply-templates>
            </xsl:otherwise>
         </xsl:choose>
      </page>
   </xsl:template>

   <xsl:template match="web:request" mode="remove">
      <xsl:param name="repo" required="yes"/>
      <xsl:param name="root" required="yes" as="xs:string"/>
      <xsl:variable name="removed" select="web:remove-webapp($repo, $root)"/>
      <xsl:choose>
         <xsl:when test="$removed">
            <para>
               <xsl:text>Webapp properly un-installed from: </xsl:text>
               <xsl:value-of select="$root"/>
               <xsl:text>.</xsl:text>
            </para>
         </xsl:when>
         <xsl:otherwise>
            <para>
               <xsl:text>Error during un-installation of the webapp from: </xsl:text>
               <xsl:value-of select="$root"/>
               <xsl:text>. Please see logs for more information.</xsl:text>
            </para>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
