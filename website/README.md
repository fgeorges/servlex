This is the Servlex website.

It is very, very simple for now.  Just a few static pages (home, install,
config, download and news).  Ideas:

- create a "real" doc page, or user guide
- move the "try it" section at the end of the install page to that new doc page
- document how to create a webapp (from a practical point of view, actual
  commands, not theory)
- move the "dump request" tool from H2O website to here
- that last point would need to create a webapp for Servlex website
- that would open the door to an XProj web frontend (first as a tool on
  servlex.net, then one day on xproj.org)

## Edit and build

The content of the pages is written in MarkDown files, in `src/`.  Each source
file corresponds to one HTML page, with the same name.  The HTML template
`src/template.html` is used as a generic template for each page.

The command `marked` is used (see [marked](https://github.com/chjj/marked) lib)
to convert MarkDown to HTML (it replaces `__CONTENT__` in the template).

The `class='__active__'` is also replaced in the template given the name of the
current page (the page `index` is the menu entry `.`).

To edit a page, edit its MarkDown source in `src/`, and invoke `./compile.sh`
from with that directory.  To add a new page, create a new file `src/new.md`,
and add the corresponding entry to the menu in the template (if any).
