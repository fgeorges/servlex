module namespace h = "http://expath.org/ns/samples/servlex/hello";

declare namespace web = "http://expath.org/ns/webapp";

declare function h:hello-xquery($input as item()+)
{
  <web:response status="200" message="Ok">
     <web:body content-type="application/xhtml+xml" method="xhtml"/>
  </web:response>
  ,
  let $who := xs:string($input[1]/web:param[@name eq 'who']/@value)
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
