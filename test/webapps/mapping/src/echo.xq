(: import uri: http://expath.org/ns/test/servlex/mapping-webapp/echo.xq :)

import module namespace app = "http://expath.org/ns/test/servlex/mapping-webapp"
  at "servlets.xql";

declare namespace web = "http://expath.org/ns/webapp";

declare variable $web:input as item()+ external;

app:echo($web:input)
