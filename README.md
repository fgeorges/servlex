# Servlex

Servlex, an implementation of the EXPath [Webapp
framework](http://expath.org/modules/webapp/) (see also the
[current draft](http://expath.org/spec/webapp) of the spec).


## Installation

Download the latest `servlex-x.y.z-installer.jar` file from the
[download area](http://code.google.com/p/servlex/downloads) and
execute it by double-clicking on it (or from the command-line: `java
-jar servlex-x.y.z-installer.jar`).  Follow the instructions, that's
it!

If you want to install Servlex into the servlet container of your
choice, use the latest `servlex-x.y.z.zip` file instead.  It contains
a suitable WAR file and install instructions.


## Setup

There is nothing to setup if you use the graphical installer.  Just
start Servlex by executing `bin/startup.sh` (resp. `bin/startup.bat`
on Windows) and go to `http://localhost:19757/`.  You can change the
default port number in `conf/server.xml`.

If you install the WAR file yourself, you have to point the property
`org.expath.servlex.repo.dir` to a repository on the disk.  On Tomcat
for instance, you can add the property to `conf/catalina.properties`.

## Try it

Once installed, you can access the Servlex Admin Console at
http://localhost:19757/manager/home. From there you can deploy the
sample web application in the release, `hello-world-x.y-z.xaw`, by
filling in the form. Go to http://localhost:19757/servlex/hello-world/
to access the example once installed. The sources are available in the
dir `hello-world/src/`.

This sample application is just a few forms, each one-field, sending
a string to either an XSLT function, an XQuery function or an XProc
step, which in turn responds with a page based on this parameter. You
can use the tool http://h2oconsulting.be/tools/dump to display the XML
representation of the HTTP request which is send to the XSLT, XQuery
and/or XProc components in a web application.

Some docs about Servlex are also available [here](http://expath.org/wiki/Webapp)
and [there](http://expath.org/wiki/Saxon:Webapp) on the EXPath's wiki.

## Real-world usage

Servlex is used to run [CXAN](http://cxan.org/) (as well as its
[Sandbox](http://test.cxan.org/)), the sources of which are
available on [GitHub](https://github.com/fgeorges/cxan/).  The
[EXPath website](http://expath.org/) runs on Servlex too (see the
[sources](http://code.google.com/p/expath/source/browse/#svn%2Ftrunk%2Fwebsite)).
[H2O Consulting](http://h2oconsulting.be/) website runs on Servlex as well.
