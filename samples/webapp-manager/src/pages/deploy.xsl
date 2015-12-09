<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://servlex.net/ns/webapp-manager"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://servlex.net/app/manager/pages/deploy.xsl</pkg:import-uri>

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
      <xsl:variable name="repo" select="web:repository()"/>
      <page menu="deploy">
         <title>Deploy</title>
         <xsl:choose>
            <xsl:when test="web:install-enabled($repo)">
               <para>Deploy a webapp, either from a local XAW file, or directly
                  from CXAN. If you deploy directly from CXAN, you can use either a
                  CXAN ID <bold>or</bold> a full package name. The version number is
                  optional (if not set, the latest one is picked).</para>
               <subtitle>Local file</subtitle>
               <form href="deploy-file" type="multipart/form-data">
                  <file name="xawfile" size="40"/>
                  <br/><br/>
                  <button label="Deploy"/>
               </form>
               <subtitle>From CXAN</subtitle>
               <form href="deploy-cxan" type="application/x-www-form-urlencoded">
                  <fields>
                     <field label="ID">
                        <text name="id" size="50"/>
                     </field>
                     <field label="Name">
                        <text name="name" size="50"/>
                     </field>
                     <field label="Version">
                        <text name="version" size="50"/>
                     </field>
                     <field label="From">
                        <choices name="server">
                           <choice value="prod" selected="true">Production - http://cxan.org/</choice>
                           <choice value="sandbox">Sandbox - http://test.cxan.org/</choice>
                        </choices>
                     </field>
                  </fields>
                  <para>
                     <button label="Deploy"/>
                  </para>
               </form>
               <subtitle>Using REST</subtitle>
               <para>You can always use the built-in Servlex REST endpoint for webapp
                  installation, at <code>[servlex]/~rest/deploy/[appname]</code>.  See
                  <link href="http://servlex.net/doc">Servlex's User Guide</link> for
                  details.</para>
            </xsl:when>
            <xsl:otherwise>
               <para><emphasis>Installation disabled (read-only storage).</emphasis></para>
            </xsl:otherwise>
         </xsl:choose>
      </page>
   </xsl:template>

</xsl:stylesheet>
