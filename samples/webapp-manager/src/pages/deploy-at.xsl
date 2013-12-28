<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:desc="http://expath.org/ns/webapp/descriptor"
                xmlns:app="http://servlex.net/ns/webapp-manager"
                exclude-result-prefixes="#all"
                version="2.0">

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
      <xsl:variable name="repo"                      select="web:repository()"/>
      <xsl:variable name="method"    as="xs:string"  select="@method"/>
      <xsl:variable name="root"      as="xs:string?" select="web:param[@name eq 'root']/@value/string(.)[.]"/>
      <xsl:variable name="cname-1"   as="xs:string?" select="web:param[@name eq 'config-name-1']/@value/string(.)"/>
      <xsl:variable name="cvalue-1"  as="xs:string?" select="web:param[@name eq 'config-value-1']/@value/string(.)"/>
      <xsl:variable name="cname-2"   as="xs:string?" select="web:param[@name eq 'config-name-2']/@value/string(.)"/>
      <xsl:variable name="cvalue-2"  as="xs:string?" select="web:param[@name eq 'config-value-2']/@value/string(.)"/>
      <xsl:variable name="cname-3"   as="xs:string?" select="web:param[@name eq 'config-name-3']/@value/string(.)"/>
      <xsl:variable name="cvalue-3"  as="xs:string?" select="web:param[@name eq 'config-value-3']/@value/string(.)"/>
      <xsl:variable name="cname-4"   as="xs:string?" select="web:param[@name eq 'config-name-4']/@value/string(.)"/>
      <xsl:variable name="cvalue-4"  as="xs:string?" select="web:param[@name eq 'config-value-4']/@value/string(.)"/>
      <xsl:variable name="cname-5"   as="xs:string?" select="web:param[@name eq 'config-name-5']/@value/string(.)"/>
      <xsl:variable name="cvalue-5"  as="xs:string?" select="web:param[@name eq 'config-value-5']/@value/string(.)"/>
      <xsl:variable name="cname-6"   as="xs:string?" select="web:param[@name eq 'config-name-6']/@value/string(.)"/>
      <xsl:variable name="cvalue-6"  as="xs:string?" select="web:param[@name eq 'config-value-6']/@value/string(.)"/>
      <xsl:variable name="cname-7"   as="xs:string?" select="web:param[@name eq 'config-name-7']/@value/string(.)"/>
      <xsl:variable name="cvalue-7"  as="xs:string?" select="web:param[@name eq 'config-value-7']/@value/string(.)"/>
      <xsl:variable name="cname-8"   as="xs:string?" select="web:param[@name eq 'config-name-8']/@value/string(.)"/>
      <xsl:variable name="cvalue-8"  as="xs:string?" select="web:param[@name eq 'config-value-8']/@value/string(.)"/>
      <xsl:variable name="cname-9"   as="xs:string?" select="web:param[@name eq 'config-name-9']/@value/string(.)"/>
      <xsl:variable name="cvalue-9"  as="xs:string?" select="web:param[@name eq 'config-value-9']/@value/string(.)"/>
      <xsl:variable name="cname-10"  as="xs:string?" select="web:param[@name eq 'config-name-10']/@value/string(.)"/>
      <xsl:variable name="cvalue-10" as="xs:string?" select="web:param[@name eq 'config-value-10']/@value/string(.)"/>
      <xsl:variable name="cname-11"  as="xs:string?" select="web:param[@name eq 'config-name-11']/@value/string(.)"/>
      <xsl:variable name="cvalue-11" as="xs:string?" select="web:param[@name eq 'config-value-11']/@value/string(.)"/>
      <xsl:variable name="cname-12"  as="xs:string?" select="web:param[@name eq 'config-name-12']/@value/string(.)"/>
      <xsl:variable name="cvalue-12" as="xs:string?" select="web:param[@name eq 'config-value-12']/@value/string(.)"/>
      <xsl:variable name="cname-13"  as="xs:string?" select="web:param[@name eq 'config-name-13']/@value/string(.)"/>
      <xsl:variable name="cvalue-13" as="xs:string?" select="web:param[@name eq 'config-value-13']/@value/string(.)"/>
      <xsl:variable name="cname-14"  as="xs:string?" select="web:param[@name eq 'config-name-14']/@value/string(.)"/>
      <xsl:variable name="cvalue-14" as="xs:string?" select="web:param[@name eq 'config-value-14']/@value/string(.)"/>
      <xsl:variable name="cname-15"  as="xs:string?" select="web:param[@name eq 'config-name-15']/@value/string(.)"/>
      <xsl:variable name="cvalue-15" as="xs:string?" select="web:param[@name eq 'config-value-15']/@value/string(.)"/>
      <xsl:variable name="cname-16"  as="xs:string?" select="web:param[@name eq 'config-name-16']/@value/string(.)"/>
      <xsl:variable name="cvalue-16" as="xs:string?" select="web:param[@name eq 'config-value-16']/@value/string(.)"/>
      <xsl:variable name="cname-17"  as="xs:string?" select="web:param[@name eq 'config-name-17']/@value/string(.)"/>
      <xsl:variable name="cvalue-17" as="xs:string?" select="web:param[@name eq 'config-value-17']/@value/string(.)"/>
      <xsl:variable name="cname-18"  as="xs:string?" select="web:param[@name eq 'config-name-18']/@value/string(.)"/>
      <xsl:variable name="cvalue-18" as="xs:string?" select="web:param[@name eq 'config-value-18']/@value/string(.)"/>
      <xsl:variable name="cname-19"  as="xs:string?" select="web:param[@name eq 'config-name-19']/@value/string(.)"/>
      <xsl:variable name="cvalue-19" as="xs:string?" select="web:param[@name eq 'config-value-19']/@value/string(.)"/>
      <xsl:variable name="cname-20"  as="xs:string?" select="web:param[@name eq 'config-name-20']/@value/string(.)"/>
      <xsl:variable name="cvalue-20" as="xs:string?" select="web:param[@name eq 'config-value-20']/@value/string(.)"/>
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
               <xsl:variable name="xar"    select="web:get-session-field('manager:xar-to-deploy')"/>
               <xsl:variable name="config" select="
                  $cname-1,  $cvalue-1,  $cname-2,  $cvalue-2,  $cname-3,  $cvalue-3,  $cname-4,  $cvalue-4,  $cname-5,  $cvalue-5,
                  $cname-6,  $cvalue-6,  $cname-7,  $cvalue-7,  $cname-8,  $cvalue-8,  $cname-9,  $cvalue-9,  $cname-10, $cvalue-10,
                  $cname-11, $cvalue-11, $cname-12, $cvalue-12, $cname-13, $cvalue-13, $cname-14, $cvalue-14, $cname-15, $cvalue-15,
                  $cname-16, $cvalue-16, $cname-17, $cvalue-17, $cname-18, $cvalue-18, $cname-19, $cvalue-19, $cname-20, $cvalue-20"/>
               <xsl:variable name="res"    select="
                  if ( exists($root) ) then
                     web:install-webapp($repo, $xar, $root, $config)
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
            </xsl:otherwise>
         </xsl:choose>
      </page>
   </xsl:template>

</xsl:stylesheet>
