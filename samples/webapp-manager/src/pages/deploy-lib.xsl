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

   <!--
      For the 2-phases deploy:
      
      - save $body (the XAR binary) into the session
      - parse $body as a ZIP file, and extract expath-web.xml
      - display a form asking for the context root
        (defaults to what's in expath-web.xml)
      
      TODO: Handle errors with the descriptor(s): do they exist, do they contain
      required information, etc.?
   -->
   <xsl:template match="web:request" mode="install">
      <xsl:param name="repo" required="yes"/>
      <xsl:param name="xar"  required="yes" as="xs:base64Binary"/>
      <xsl:variable name="pkg-desc" select="zip:xml-entry($xar, 'expath-pkg.xml')/*"/>
      <xsl:variable name="web-desc" select="zip:xml-entry($xar, 'expath-web.xml')/*"/>
      <xsl:value-of select="web:set-session-field('manager:xar-to-deploy', $xar)"/>
      <para>
         <xsl:choose>
            <xsl:when test="exists($web-desc)">Webapp: </xsl:when>
            <xsl:otherwise>Library: </xsl:otherwise>
         </xsl:choose>
         <xsl:value-of select="$pkg-desc/pkg:title"/>
         <xsl:text>, version </xsl:text>
         <xsl:value-of select="$pkg-desc/@version"/>
         <xsl:text>, name </xsl:text>
         <xsl:value-of select="$pkg-desc/@name"/>
         <xsl:for-each select="$pkg-desc/pkg:home">
            <xsl:text>, more information at </xsl:text>
            <link href="{ . }">
               <xsl:value-of select="."/>
            </link>
         </xsl:for-each>
         <xsl:text>.</xsl:text>
      </para>
      <form href="deploy-at">
         <xsl:choose>
            <xsl:when test="exists($web-desc)">
               <fields>
                  <field label="Context root">
                     <text name="root" size="20">
                        <xsl:value-of select="$web-desc/@abbrev"/>
                     </text>
                  </field>
               </fields>
               <para>
                  <button label="Deploy"/>
               </para>
            </xsl:when>
            <xsl:otherwise>
               <para>
                  <button label="Install"/>
               </para>
            </xsl:otherwise>
         </xsl:choose>
      </form>
   </xsl:template>

</xsl:stylesheet>
