<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://servlex.net/ns/webapp-manager"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://servlex.net/app/manager/pages/deploy-cxan.xsl</pkg:import-uri>

   <xsl:param name="web:input" required="yes"/>

   <xsl:template match="document-node()[empty(web:request)]">
      <xsl:message terminate="yes">
         <xsl:text>Unexpected document?!?: </xsl:text>
         <xsl:value-of select="name(*)"/>
         <xsl:text> (expect a web:request).</xsl:text>
      </xsl:message>
   </xsl:template>

   <xsl:template match="document-node()[exists(web:request)]">
<xsl:message>
   REQUEST:
   <xsl:copy-of select="."/>
</xsl:message>
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="web:request">
      <xsl:variable name="repo"                    select="web:repository()"/>
      <xsl:variable name="server"  as="xs:string?" select="web:param[@name eq 'server']/@value/string(.)[.]"/>
      <xsl:variable name="id"      as="xs:string?" select="web:param[@name eq 'id']/@value/string(.)[.]"/>
      <xsl:variable name="name"    as="xs:string?" select="web:param[@name eq 'name']/@value/string(.)[.]"/>
      <xsl:variable name="version" as="xs:string?" select="web:param[@name eq 'version']/@value/string(.)[.]"/>
      <page menu="deploy">
         <title>Deploy from CXAN</title>
         <xsl:choose>
            <xsl:when test="not(web:install-enabled($repo))">
               <xsl:sequence select="
                   error(
                     xs:QName('app:not-implemented'),
                     'Install not supported, storage is read-only')"/>
            </xsl:when>
            <xsl:when test="empty($server)">
               <xsl:sequence select="
                   error(
                     xs:QName('app:missing-param'),
                     'The CXAN server to use has not been provided (param ''server'')')"/>
            </xsl:when>
            <xsl:when test="not($server = ('prod', 'sandbox'))">
               <xsl:sequence select="
                   error(
                     xs:QName('app:bad-request'),
                     concat('The CXAN server to use must be either ''prod'' or ''sandbox'', but is ''', $server, '''.'))"/>
            </xsl:when>
            <xsl:when test="empty($id) and empty($name)">
               <xsl:sequence select="
                   error(
                     xs:QName('app:bad-request'),
                     'Neither CXAN ID or package name provided, at least one is required.')"/>
            </xsl:when>
            <xsl:when test="exists($id) and exists($name)">
               <xsl:sequence select="
                   error(
                     xs:QName('app:bad-request'),
                     concat('Both CXAN ID and package name provided: resp. ''', $id, ''' and ''', $name, '''.'))"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:apply-templates select="." mode="install">
                  <xsl:with-param name="repo"    select="$repo"/>
                  <xsl:with-param name="server"  select="$server"/>
                  <xsl:with-param name="id"      select="$id"/>
                  <xsl:with-param name="name"    select="$name"/>
                  <xsl:with-param name="version" select="$version"/>
               </xsl:apply-templates>
            </xsl:otherwise>
         </xsl:choose>
      </page>
   </xsl:template>

   <xsl:template match="web:request" mode="install">
      <xsl:param name="repo"    required="yes"/>
      <xsl:param name="server"  required="yes" as="xs:string"/>
      <xsl:param name="id"      required="yes" as="xs:string?"/>
      <xsl:param name="name"    required="yes" as="xs:string?"/>
      <xsl:param name="version" required="yes" as="xs:string?"/>
      <xsl:variable name="domain" select="
          if ( $server eq 'prod' ) then 'cxan.org' else 'test.cxan.org'"/>
      <xsl:variable name="root" select="
          web:install-from-cxan($repo, $domain, $id, $name, $version)"/>
      <para>
         <xsl:choose>
            <xsl:when test="exists($root)">
               <link href="../{ $root }/">
                  <xsl:value-of select="$root"/>
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
