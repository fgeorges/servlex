### User Guide

For installation instructions, see the [Installation Guide](install).

For configuration options, see the [Reference](config).

For downloading Servlex, go to the [Download Area](download).

#### Introduction

Servlex is a container for EXPath Web Applications (or webapp for short).  It
enables you to install and run such webapps, written in XML technologies (using
XQuery, XSLT and/or XProc components).  Servlex handles all the networking, and
connect the components to the HTTP layer.

Day-to-day, the way you will interact most with Servlex is via the Webapp
Manager and/or the REST API.

#### Webapp Manager

The purpose of the Webapp Manager is to, well, let you manager webapps.  The
Webapp Manager is itself a webapp.  It is not built-in Servlex, but uses its
APIs to present the user with a web UI.

Because the Webapp Manager is itself a webapp, it needs to be installed before
you can use it.  If you used the Servlex Installer, bundled with Apache Tomcat,
then the Webapp Manager is already pre-installed and configured in the
repository created during the installation.  If you installed the WAR file
yourself, you will have to install the Webapp Manager by using the built-in REST
endpoint of Servlex for deploying webapps (see below).  The Webapp Manager XAW
file comes with the Servlex WAR file.

The [...] BLA BLA BLA BLA...

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
# using HTTPie, note the endpoint
http POST :19757/servlex/~rest/deploy/my-app?content-dir=/some/server/dir/ \
    @../path/to/my-app.xaw
```

To install a library package, do not include a context root, and use the fix
deploy endpoint:

```shell
# using HTTPie, note the endpoint
http POST :19757/servlex/~rest/deploy @../to/lib.xar
```

A new REST endpoint will be added, to remove libraries and webapps once they
have been installed.
