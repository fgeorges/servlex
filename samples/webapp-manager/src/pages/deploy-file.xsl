<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://servlex.net/ns/webapp-manager"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://servlex.net/app/manager/pages/deploy-file.xsl</pkg:import-uri>

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
      <xsl:variable name="repo"                    select="web:repository()"/>
      <xsl:variable name="method"  as="xs:string"  select="@method"/>
      <xsl:variable name="body"    as="element()?" select="web:body"/>
      <xsl:variable name="mpart"   as="element()?" select="web:multipart"/>
      <xsl:variable name="content" as="item()?"    select="$web:input[2]"/>
      <xsl:variable name="ctype"   as="xs:string?" select="
          $mpart/web:body[xs:integer(@position) eq 1]/@content-type"/>
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
            <xsl:when test="exists($body)">
               <xsl:sequence select="
                   error(
                     xs:QName('app:bad-request'),
                     concat('Need multipart, got single part: ', $body/@content-type))"/>
            </xsl:when>
            <xsl:when test="empty($mpart)">
               <xsl:sequence select="
                   error(
                     xs:QName('app:bad-request'),
                     'Need multipart, got nothing')"/>
            </xsl:when>
            <xsl:when test="empty($content)">
               <xsl:sequence select="
                   error(
                     xs:QName('app:bad-request'),
                     'Need a file part, got nothing')"/>
            </xsl:when>
            <xsl:when test="exists(subsequence($web:input, 3))">
               <xsl:sequence select="
                   error(
                     xs:QName('app:bad-request'),
                     'Need exactly one file part, got more than one part',
                     $mpart)"/>
            </xsl:when>
            <xsl:when test="not($ctype eq 'application/octet-stream')">
               <xsl:sequence select="
                   error(
                     xs:QName('app:bad-request'),
                     concat('Need a binary part, got: ', $ctype))"/>
            </xsl:when>
            <xsl:when test="not($content instance of xs:base64Binary)">
               <xsl:sequence select="
                   error(
                     xs:QName('app:bad-request'),
                     'Need a binary part, got something else')"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:apply-templates select="." mode="install">
                  <xsl:with-param name="repo" select="$repo"/>
                  <xsl:with-param name="body" select="$content"/>
               </xsl:apply-templates>
            </xsl:otherwise>
         </xsl:choose>
      </page>
   </xsl:template>

   <xsl:template match="web:request" mode="install">
      <xsl:param name="repo" required="yes"/>
      <xsl:param name="body" required="yes" as="xs:base64Binary"/>
      <xsl:variable name="root" select="web:install-webapp($repo, $body)"/>
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
