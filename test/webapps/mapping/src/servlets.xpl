<p:library xmlns:p="http://www.w3.org/ns/xproc"
           xmlns:pkg="http://expath.org/ns/pkg"
           xmlns:web="http://expath.org/ns/webapp"
           xmlns:app="http://expath.org/ns/test/servlex/mapping-webapp"
           pkg:import-uri="http://expath.org/ns/test/servlex/mapping-webapp/servlets.xpl"
           version="1.0">

   <p:declare-step type="app:echo" name="this">
      <p:input  port="source" sequence="true"/>
      <p:output port="result" sequence="true">
         <p:pipe step="desc"    port="result"/>
         <p:pipe step="content" port="result"/>
      </p:output>
      <!-- the response descriptor -->
      <p:identity name="desc">
         <p:input port="source">
            <p:inline>
               <web:response status="200" message="Ok">
                  <web:body content-type="application/xml"/>
               </web:response>
            </p:inline>
         </p:input>
      </p:identity>
      <!-- split web:request from the bodies -->
      <p:split-sequence test="position() eq 1" name="split">
         <p:input port="source">
            <p:pipe step="this" port="source"/>
         </p:input>
      </p:split-sequence>
      <!-- wrap web:response into app:http -->
      <p:wrap match="/*" wrapper="app:http" name="http">
         <p:input port="source">
            <p:pipe step="split" port="matched"/>
         </p:input>
      </p:wrap>
      <!-- wrap each body into app:body -->
      <p:for-each name="bodies">
         <p:iteration-source>
            <p:pipe step="split" port="not-matched"/>
         </p:iteration-source>
         <p:output port="result"/>
         <p:wrap match="/*" wrapper="app:body"/>
         <p:add-attribute match="/app:body" attribute-name="position">
            <p:with-option name="attribute-value" select="position()"/>
         </p:add-attribute>
      </p:for-each>
      <!-- wrap app:http and every app:body into app:request -->
      <p:wrap-sequence wrapper="app:request" name="content">
         <p:input port="source">
            <p:pipe step="http"   port="result"/>
            <p:pipe step="bodies" port="result"/>
         </p:input>
      </p:wrap-sequence>
   </p:declare-step>

</p:library>
