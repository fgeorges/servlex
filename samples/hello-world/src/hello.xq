(: import uri: http://expath.org/ns/samples/servlex/hello.xq :)

import module namespace h = "http://expath.org/ns/samples/servlex/hello" at "hello.xql";

declare namespace web = "http://expath.org/ns/webapp";

declare variable $web:input external;

h:hello-xquery($web:input)
