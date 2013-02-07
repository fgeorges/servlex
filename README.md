# Servlex

Servlex, an implementation of the EXPath [Webapp
framework](http://expath.org/modules/webapp/) (see also the [editor
draft](http://expath.org/spec/webapp/editor) of the spec).


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
