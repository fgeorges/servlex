<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://servlex.net/ns/webapp-manager"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://servlex.net/app/manager/pages/webapps.xsl</pkg:import-uri>

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
      <page menu="webapps">
         <title>Installed webapps</title>
         <para>Click on the webapp name to access it (its homepage).
            Click on the (x) to delete it.</para>
         <xsl:variable name="repo"    select="web:repository()"/>
         <xsl:variable name="webapps" select="web:installed-webapps($repo)"/>
         <xsl:choose>
            <xsl:when test="empty($webapps)">
               <para>No webapp has been installed yet.</para>
            </xsl:when>
            <xsl:otherwise>
               <list>
                  <xsl:for-each select="$webapps">
                     <xsl:sort select="."/>
                     <item>
                        <xsl:text>(</xsl:text>
                        <link href="remove?webapp={ . }">x</link>
                        <xsl:text>) </xsl:text>
                        <link href="../{ . }/">
                           <xsl:value-of select="."/>
                        </link>
                     </item>
                  </xsl:for-each>
               </list>
            </xsl:otherwise>
         </xsl:choose>
      </page>
   </xsl:template>

</xsl:stylesheet>
