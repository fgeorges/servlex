<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:desc="http://expath.org/ns/webapp/descriptor"
                xmlns:zip="http://expath.org/ns/zip"
                xmlns:app="http://servlex.net/ns/webapp-manager"
                exclude-result-prefixes="#all"
                version="2.0">

   <xsl:import href="http://expath.org/ns/zip.xsl"/>

   <pkg:import-uri>http://servlex.net/app/manager/pages/deploy-at.xsl</pkg:import-uri>

   <xsl:param name="web:input" required="yes"/>

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
      <xsl:variable name="repo"                   select="web:repository()"/>
      <xsl:variable name="method" as="xs:string"  select="@method"/>
      <xsl:variable name="root"   as="xs:string?" select="web:param[@name eq 'root']/@value/string(.)[.]"/>
      <page menu="deploy">
         <title>Deploy local file</title>
         <xsl:choose>
            <xsl:when test="not(web:install-enabled($repo))">
               <xsl:sequence select="
                   error(
                     xs:QName('app:not-implemented'),
                     'Install not supported, storage is read-only')"/>
            </xsl:when>
            <xsl:when test="not($method eq 'post')">
               <xsl:sequence select="
                   error(
                     xs:QName('app:method-not-allowed'),
                     concat('Method not allowed, need POST, got ', $method))"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:apply-templates select="." mode="deploy">
                  <xsl:with-param name="repo" select="$repo"/>
                  <xsl:with-param name="root" select="$root"/>
               </xsl:apply-templates>
            </xsl:otherwise>
         </xsl:choose>
      </page>
   </xsl:template>

   <!--
      For the 2-phases deploy:
      
      - save the XAR into the session
      - parse the root from expath-web.xml
      - ask for the root, defaulted to the one in expath-web.xml
      - go to the same URL (but GET, with ?root=...)
      - the implem of the GET can be done here or in another component
          (but I think there is no support in expath-web.xml to dispatch based on
          methods, so the same component might be required)
      
      Put another way:
      
      - save $body into the session (a binary)
      - parse $body as a ZIP file, and extract expath-web.xml
      - ...
   -->
   <xsl:template match="web:request" mode="deploy">
      <xsl:param name="repo" required="yes"/>
      <xsl:param name="root" required="yes" as="xs:string?"/>
      <xsl:variable name="xar" select="web:get-session-field('manager:xar-to-deploy')"/>
      <xsl:variable name="res" select="
         if ( exists($root) ) then
            web:install-webapp($repo, $xar, $root)
         else
            web:install-webapp($repo, $xar)"/>
      <para>
         <xsl:choose>
            <xsl:when test="exists($res)">
               <link href="../{ $res }/">
                  <xsl:value-of select="$res"/>
               </link>
            </xsl:when>
            <xsl:otherwise>
               <xsl:text>The package </xsl:text>
            </xsl:otherwise>
         </xsl:choose>
         <xsl:text> has been successfully installed.</xsl:text>
      </para>
   </xsl:template>

</xsl:stylesheet>
