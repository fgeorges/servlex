module namespace h = "http://expath.org/ns/test/servlex/filters-app-webapp";

declare namespace web = "http://expath.org/ns/webapp";

declare function h:in-filter-one($request as element(web:request))
{
  let $who := $request/web:param[@name eq 'who']
  return
    <web:request> {
      $request/@*,
      $request/* except $who,
      $who/<web:param name="{ @name }" value="(in 1) { @value }"/>
    }
    </web:request>
};

declare function h:in-filter-two($request as element(web:request))
{
  let $who := $request/web:param[@name eq 'who']
  return
    <web:request> {
      $request/@*,
      $request/* except $who,
      $who/<web:param name="{ @name }" value="(in 2) { @value }"/>
    }
    </web:request>
};

declare function h:in-filter-three($request as element(web:request))
{
  let $who := $request/web:param[@name eq 'who']
  return
    <web:request> {
      $request/@*,
      $request/* except $who,
      $who/<web:param name="{ @name }" value="(in 3) { @value }"/>
    }
    </web:request>
};
