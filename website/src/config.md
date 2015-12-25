### Configuration Options

Servlex is a standard WAR file, and can be deployed in any compliant Java EE
Servlet container.  It also comes with an automatic installer, pre-bundeled with
Tomcat.  The purpose of this document is to document Servlex own configuration
options, but it gives also a few recipes about Tomcat, e.g. how to change the
port number.

#### Servlex options

The following options are Java system properties.  The way to set them to
specific values is depending on the product you use.  Please see the
documentation of your servlet container.  For instance, one way of achieving
this with Tomcat is to add a line like the following in the config file
`conf/catalina.properties`:

```shell
# this line is a comment
# below line is the property name and its value
org.expath.servlex.profile.dir=/some/place/to/save/profiling
```

The options are:

##### org.expath.servlex.repo.dir

This is the absolute directory identifying the root of the repository to be used
by Servlex.  The repository contains all the webapps, as well as the libraries
they (might) depend on.  The directory must exist.  This option is exlusive with
the option `org.expath.servlex.repo.dir`.

##### org.expath.servlex.repo.classpath

This is the classpath prefix to be used by Servlex if the repository is provided
within the classpath.  This is a very special use, for instance to use in Google
App Engine.  Repositories in the classpath are read-only.  This option is
exlusive with the option `org.expath.servlex.repo.dir`.

##### org.expath.servlex.profile.dir

Directory where to save profiling information.  When set, Servlex saves the XML
representation of the incoming HTTP requests, as well as the Calabash's profile
files (if you use XProc).

##### org.expath.servlex.trace.content

If this option is set to `true`, Servlex logs the entire entity content (that
is, the "payload" or "body") of the requests, not only its XML representation.

##### org.expath.servlex.default.charset

The default charset to use if it is not explicitly defined on the request.  The
valid charsets are the same as the charsets accepted by the Java platform.

#### Options for the Saxon processor

If you use Saxon as the XSLT and XQuery processor (which is so far the only
implementation in Servlex), you can use the following options to configure its
behaviour.

##### org.expath.servlex.saxon.xslt.version

The XSLT and XQuery processor based on Saxon generates an XSLT stylesheet to
implement XSLT function and named template components.  The value of this
option is the version to use for those wrapper XSLT stylesheets.  By default
it is `2.0`.

##### org.expath.servlex.saxon.config.file

Saxon defines its own configuration file format.  The value of this option, if
any, must be the path to such a Saxon config file.  The file is used when
instantiating Saxon, when starting Servlex.  By default, Servlex does not
provide Saxon with any config file.

#### Useful recipes for Tomcat

If you use Tomcat (e.g. if you installed the Servlex installer, which contains
Tomcat), you might be used by the following configuration tips.

##### Changing the port number

You can change the default port number in `conf/server.xml`.  By default, the
Servlex installer uses the port number 19757.  Tomcat also uses a few other
ports for its own purposes.  Just change all occurrences of the prefix "197"
with a prefix of your own, to avoid any clash between two instances of Servlex.

##### Using Saxon-PE or Saxon-EE

Saxon-HE is free and open-source, but the versions PE and EE require a license,
as well as their own JAR file.  If you are the happy owner of Saxon PE or EE,
you can replace the Saxon JAR file in `webapps/ROOT/WEB-INF/lib/`, as well as
copying you license file in th same directory.  And that's it, Servlex will
detect the licensed version and will use it.

##### Increase log level

In `[servlex]/webapps/ROOT/WEB-INF/classes/logging.properties`, save
the following content:

```
handlers = org.apache.juli.FileHandler, java.util.logging.ConsoleHandler

org.expath.servlex.level = FINE

org.apache.juli.FileHandler.directory = ${catalina.base}/logs
org.apache.juli.FileHandler.prefix = ${classloader.webappName}.

java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
```

Change `FINE` (debug) to `FINEST` or `ALL`, for trace-level logging.
