<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                pkg:import-uri="http://expath.org/ns/test/servlex/filters-webapp/error-handler.xproc"
                version="1.0">

   <p:input  port="source"    primary="true" sequence="true"/>
   <p:input  port="user-data" sequence="true"/>
   <p:output port="result"    primary="true"/>

   <p:option name="web:code-name"      required="true"/>
   <p:option name="web:code-namespace" required="true"/>
   <p:option name="web:message"        required="true"/>

   <p:template>
      <p:input port="source">
         <p:empty/>
      </p:input>
      <p:input port="template">
         <p:inline>
            <web:wrapper>
               <web:response status="400" message="Error">
                  <!-- Depends on the Accept header actually, see
                       http://www.w3.org/TR/xhtml-media-types/.  Good candidate
                       for a tool function provided by Servlex itself. -->
                  <web:body content-type="application/xhtml+xml" method="xhtml"/>
               </web:response>
               <html xmlns="http://www.w3.org/1999/xhtml">
                  <head>
                     <title>Oops</title>
                  </head>
                  <body>
                     <p>{ $code }</p>
                     <p>{ $ns }</p>
                     <p>{ $msg }</p>
                  </body>
               </html>
            </web:wrapper>
         </p:inline>
      </p:input>
      <p:with-param name="code" select="$web:code-name"/>
      <p:with-param name="ns"   select="$web:code-namespace"/>
      <p:with-param name="msg"  select="$web:message"/>
   </p:template>

</p:declare-step>
