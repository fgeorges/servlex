<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                xmlns:app="http://servlex.net/ns/webapp-manager"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">

   <pkg:import-uri>http://servlex.net/ns/webapp-manager/view.xsl</pkg:import-uri>

   <xsl:template match="document-node()[empty(page)]">
      <xsl:message terminate="yes">
         <xsl:text>Unexpected document?!?: </xsl:text>
         <xsl:value-of select="name(*)"/>
      </xsl:message>
   </xsl:template>

   <xsl:template match="document-node()[exists(page)]">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="*" priority="-1">
      <xsl:message terminate="yes">
         <xsl:text>Unexpected element: </xsl:text>
         <xsl:value-of select="name(.)"/>
         <xsl:text>&#10;</xsl:text>
         <xsl:copy-of select="."/>
      </xsl:message>
   </xsl:template>

   <xsl:template match="page">
      <web:response status="200" message="Ok">
         <web:body content-type="application/xhtml+xml" method="xhtml"/>
      </web:response>
      <xsl:variable name="root" as="xs:string" select="( @root, '.' )[1]"/>
      <html>
         <head>
            <xsl:apply-templates select="title" mode="head"/>
            <link rel="stylesheet"    type="text/css"  href="{ $root }/style/manager.css"/>
            <link rel="shortcut icon" type="image/png" href="{ $root }/images/expath-icon.png"/>
         </head>
         <body>
            <div id="upbg"> </div>
            <div id="outer">
               <div id="header">
                  <div id="headercontent">
                     <h1>Servlex manager</h1>
                  </div>
               </div>
               <xsl:call-template name="menu">
                  <xsl:with-param name="root" select="$root"/>
               </xsl:call-template>
               <div id="menubottom"> </div>
               <div id="content">
                  <div class="normalcontent">
                     <xsl:apply-templates select="title"/>
                     <div class="contentarea">
                        <xsl:apply-templates select="* except title"/>
                     </div>
                  </div>
               </div>
               <div id="footer">
                  <div class="right">
                     <!-- TODO: Have a HTML version of this, with links... -->
                     <xsl:value-of select="web:get-container-field('web:product')"/>
                     <br/>
                     <xsl:text>By </xsl:text>
                     <xsl:value-of select="web:get-container-field('web:vendor')"/>
                     
                     <!-- TODO: Parse the values... (WTF, why would we need to parse standard
                          properties...?!?) -->
                     <!--xsl:sequence select="web:get-container-field('web:product-html')"/>
                     <br/>
                     <xsl:text>By </xsl:text>
                     <xsl:sequence select="web:get-container-field('web:vendor-html')"/-->
                  </div>
               </div>
            </div>
         </body>
      </html>
   </xsl:template>

   <xsl:variable name="menu-items">
      <menu name="home"    title="Servlex manager home"    label="Home"/>
      <menu name="webapps" title="Installed webapps list"  label="Webapps"/>
      <menu name="deploy"  title="Deploy a webapp"         label="Deploy"/>
      <menu name="reload"  title="Reload the webapp cache" label="Reload"/>
   </xsl:variable>

   <xsl:template name="menu">
      <xsl:param name="root" as="xs:string" required="yes"/>
      <xsl:variable name="menu" select="@menu" as="xs:string"/>
      <div id="menu">
         <ul>
            <xsl:for-each select="$menu-items/*">
               <li>
                  <a href="{ $root }/{ @name }" title="{ @title }">
                     <xsl:if test="$menu eq @name">
                        <xsl:attribute name="class" select="'active'"/>
                     </xsl:if>
                     <xsl:value-of select="@label"/>
                  </a>
               </li>
            </xsl:for-each>
         </ul>
      </div>
   </xsl:template>

   <xsl:template match="title" mode="head">
      <title>
         <xsl:apply-templates/>
      </title>
   </xsl:template>

   <xsl:template match="title">
      <h3>
         <strong>
            <xsl:apply-templates/>
         </strong>
      </h3>
   </xsl:template>

   <xsl:template match="para">
      <p>
         <xsl:apply-templates/>
      </p>
   </xsl:template>

   <xsl:template match="link">
      <a href="{ @href }">
         <xsl:apply-templates/>
      </a>
   </xsl:template>

   <xsl:template match="form">
      <form action="{ @href }" method="post">
         <xsl:if test="@type">
            <xsl:attribute name="enctype" select="@type"/>
         </xsl:if>
         <xsl:apply-templates/>
      </form>
   </xsl:template>

   <xsl:template match="button">
      <input type="submit" value="{ @label }"/>
   </xsl:template>

   <xsl:template match="file">
      <input type="file" name="{ @name }" size="{ @size }"/>
   </xsl:template>

   <xsl:template match="br">
      <br/>
   </xsl:template>

   <xsl:template match="fields">
      <table>
         <xsl:apply-templates/>
      </table>
   </xsl:template>

   <xsl:template match="field">
      <tr>
         <td class="right">
            <xsl:value-of select="@label"/>
            <xsl:text>:</xsl:text>
         </td>
         <td>
            <xsl:apply-templates/>
         </td>
      </tr>
   </xsl:template>

   <xsl:template match="choices">
      <select name="{ @name }">
         <xsl:apply-templates/>
      </select>
   </xsl:template>

   <xsl:template match="choice">
      <option value="{ @value }">
         <xsl:if test="@select/xs:boolean(.)">
            <xsl:attribute name="selected" select="'selected'"/>
         </xsl:if>
         <xsl:apply-templates/>
      </option>
   </xsl:template>

   <xsl:template match="text">
      <input type="text" value="{ . }">
         <xsl:copy-of select="@name|@size|@title"/>
         <xsl:if test="@hidden/xs:boolean(.)">
            <xsl:attribute name="type" select="'hidden'"/>
         </xsl:if>
      </input>
   </xsl:template>

   <xsl:template match="subtitle">
      <p>
         <b>
            <xsl:apply-templates/>
         </b>
      </p>
   </xsl:template>

   <xsl:template match="bold">
      <b>
         <xsl:apply-templates/>
      </b>
   </xsl:template>

   <xsl:template match="emphasis">
      <em>
         <xsl:apply-templates/>
      </em>
   </xsl:template>

   <xsl:template match="list">
      <ul>
         <xsl:apply-templates/>
      </ul>
   </xsl:template>

   <xsl:template match="item">
      <li>
         <xsl:apply-templates/>
      </li>
   </xsl:template>

   <xsl:template match="debug">
      <xsl:comment>
         <xsl:copy-of select="node()"/>
      </xsl:comment>
   </xsl:template>

</xsl:stylesheet>
