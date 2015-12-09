#! /bin/bash

die() {
    echo
    echo "*** $@" 1>&2;
    exit 1;
}

if test -z "${JAVA_HOME}"; then
    JAVA=java
else
    JAVA="${JAVA_HOME}/bin/java"
fi

SERVLEX=`dirname $0`/..
if test \! -d "${SERVLEX}"; then
    die "INTERNAL ERROR: The Servlex directory is not a directory?!? ($SERVLEX)"
fi

LIB="${SERVLEX}/webapps/ROOT/WEB-INF/lib"
if test \! -d "${LIB}"; then
    die "INTERNAL ERROR: The lib directory is not a directory?!? ($LIB)"
fi

# tools-java
tools_java="$LIB/expath-tools-java-0.1.0.jar"
if test \! -f "$tools_java"; then
    die "Cannot find tools-java JAR: $tools_java"
fi

# tools-saxon
tools_saxon="$LIB/expath-tools-saxon-0.1.0.jar"
if test \! -f "$tools_saxon"; then
    die "Cannot find tools-saxon JAR: $tools_saxon"
fi

# pkg-java
pkg_java="$LIB/pkg-java-0.13.1.jar"
if test \! -f "$pkg_java"; then
    die "Cannot find pkg-java JAR: $pkg_java"
fi

# pkg-saxon
pkg_saxon="$LIB/pkg-saxon-0.13.1.jar"
if test \! -f "$pkg_saxon"; then
    die "Cannot find pkg-saxon JAR: $pkg_saxon"
fi

# pkg-calabash
pkg_calabash="$LIB/pkg-calabash-0.13.1.jar"
if test \! -f "$pkg_calabash"; then
    die "Cannot find pkg-calabash JAR: $pkg_calabash"
fi

# saxon.jar
saxon="$LIB/saxon9he.jar"
if test \! -f "$saxon"; then
    die "Cannot find Saxon: $saxon"
fi

# the classpath
CP=$tools_java:$tools_saxon:$pkg_java:$pkg_saxon:$pkg_calabash:$saxon

# do it!
"$JAVA" -cp "$CP" org.expath.pkg.repo.tui.Main "$@"
