module namespace h = "http://expath.org/ns/test/servlex/filters-webapp";

declare namespace web = "http://expath.org/ns/webapp";

declare function h:hello-xquery($request as element(web:request))
{
  <web:response status="200" message="Ok">
     <web:body content-type="application/xhtml+xml" method="xhtml"/>
  </web:response>
  ,
  let $who := xs:string($request/web:param[@name eq 'who']/@value)
  return
    <html xmlns="http://www.w3.org/1999/xhtml">
       <head>
          <title>Hello, { $who }!</title>
       </head>
       <body>
          <p>Hello, { $who }! (in XQuery)</p>
       </body>
    </html>
};

declare function h:in-filter($request as element(web:request))
{
  let $who := $request/web:param[@name eq 'who']
  return
    <web:request> {
      $request/@*,
      $request/* except $who,
      $who/<web:param name="{ @name }" value="{ @value } (in)"/>
    }
    </web:request>
};
