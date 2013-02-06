module namespace app = "http://expath.org/ns/test/servlex/mapping-webapp";

declare namespace web = "http://expath.org/ns/webapp";

declare function app:echo($input as item()+)
{
  <web:response status="200" message="Ok">
     <web:body content-type="application/xml"/>
  </web:response>
  ,
  <app:request>
     <app:http> {
        $input[1]
     }
     </app:http>
     {
       for $body at $pos in remove($input, 1)
       return
         <app:body position="{ $pos }"> {
            $body
         }
         </app:body>
     }
  </app:request>
};
