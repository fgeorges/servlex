### User Guide

Other places of interest:

- [Installation Guide](install)
- [Configuration Guide](config)
- [Download Area](download)
- [Web Applications](http://expath.org/spec/webapp) specification
- [Servlex Project](https://github.com/fgeorges/servlex) on GitHub

<p></p>

The [dump tool](http://h2oconsulting.be/tools/dump) tool provided by
[H2O Consulting](http://h2oconsulting.be/) is also useful to display the XML
representation of the HTTP request which is sent to the XSLT, XQuery and XProc
components in a web application.

#### Introduction

Servlex is a container for EXPath Web Applications (or webapps for short).  It
enables you to install and run such webapps, written in XML technologies (using
XQuery, XSLT and/or XProc components).  Servlex handles all the networking, and
connects the components to the HTTP layer.

Day-to-day, the way you will interact most with Servlex is via the Webapp
Manager and/or the REST API.

#### Webapp Manager

The purpose of the Webapp Manager is to, well, let you manager webapps.  The
Webapp Manager is itself a webapp.  It is not built-in Servlex, but uses its
APIs to present the user with a web interface.

Because the Webapp Manager is itself a webapp, it needs to be installed before
you can use it.  If you used the Servlex Installer, bundled with Apache Tomcat,
then the Webapp Manager is already pre-installed and configured in the
repository created during the installation.  Details are convered in the
[Installation Guide](install).

You can access the Servlex Admin Console at http://localhost:19757/manager/.
The Console lets you manage the webapps in a Servlex instance, by listing them,
removing them, deploying a webapp from a XAW file, or directly from CXAN.

<p style="text-align: center">
<img src="images/console-screenshot.png" style="border: 0"></img>
</p>

If you are eager to test Servlex, you can deploy the sample web application
available in the installation dir, as `hello-world-n.n-n.xaw`.  Go to the
Console, to the "*Deploy*" tab, fill in the form by choosing the file and press
`deploy`.  Check the abstract you get on the next page looks OK, keep the
default context root suggested, and press `deploy` again.  You can now access
the example application by visiting http://localhost:19757/hello-world/.  The
sources are available in the dir `hello-world/` in the installation dir.

This sample application is just a few forms, each one-field, sending a string to
either an XSLT function, an XQuery function or an XProc step, which in turn
responds with a page based on this parameter.

#### REST API

In order to deploy a webapp, you can POST the XAW file to the endpoint
`~rest/deploy/[my-app]`, where `[my-app]` must be replaced by the context root
under which you want the webapp to be acessible.  For instance, to install a
webapp to be accessible at:

```text
http://localhost:19757/servlex/foobar/
```

then send the XAW file as a HTTP POST request to:

```
http://localhost:19757/servlex/~rest/deploy/foobar
```

In order to install a library package, do not include a context root at the end
of the endpoint URL, and POST the package to:

```
http://localhost:19757/servlex/~rest/deploy
```

For instance, if you want to use CURL from the command line, or the excellent
[HTTPie](http://httpie.org/):

```shell
# using CURL
curl --request POST --data-binary @../path/to/my-app.xaw \
    http://localhost:19757/servlex/~rest/deploy/my-app

# using HTTPie
http POST :19757/servlex/~rest/deploy/my-app \
    @../path/to/my-app.xaw
```

You can set webapp config parameters by simply passing them as URL parameters.
For instance, if the webapp `my-app.xaw` provides the parameter `content-dir`,
the following command will install the webapp, and provide a value for that
parameter at the same time:

```shell
# note the endpoint, with a parameter
http POST :19757/servlex/~rest/deploy/my-app?content-dir=/some/server/dir/ \
    @../path/to/my-app.xaw
```

To install a library package, do not include a context root, and use the fix
deploy endpoint:

```shell
# note the endpoint, with no context root
http POST :19757/servlex/~rest/deploy @../to/lib.xar
```

A new REST endpoint will be added, to remove libraries and webapps once they
have been installed.
