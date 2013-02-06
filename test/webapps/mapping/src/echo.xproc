<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:app="http://expath.org/ns/test/servlex/mapping-webapp"
                pkg:import-uri="http://expath.org/ns/test/servlex/mapping-webapp/echo.xproc"
                version="1.0">

   <p:input  port="source" sequence="true"/>
   <p:output port="result" sequence="true"/>

   <p:import href="servlets.xpl"/>

   <app:echo/>

</p:declare-step>
