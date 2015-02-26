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

# # tools-java.jar
# tools_java="$LIB/tools-java.jar"
# if test \! -f "$tools_java"; then
#     die "Cannot find tools-java.jar: $tools_java"
# fi

# # tools-saxon.jar
# tools_saxon="$LIB/tools-saxon.jar"
# if test \! -f "$tools_saxon"; then
#     die "Cannot find tools-saxon.jar: $tools_saxon"
# fi

# pkg-java.jar
pkg_java="$LIB/pkg-java.jar"
if test \! -f "$pkg_java"; then
    die "Cannot find pkg-java.jar: $pkg_java"
fi

# pkg-saxon.jar
pkg_saxon="$LIB/pkg-saxon.jar"
if test \! -f "$pkg_saxon"; then
    die "Cannot find pkg-saxon.jar: $pkg_saxon"
fi

# pkg-calabash.jar
pkg_calabash="$LIB/pkg-calabash.jar"
if test \! -f "$pkg_calabash"; then
    die "Cannot find pkg-calabash.jar: $pkg_calabash"
fi

# saxon.jar
saxon="$LIB/saxon9he.jar"
if test \! -f "$saxon"; then
    die "Cannot find Saxon: $saxon"
fi

# the classpath
# CP=$tools_java:$tools_saxon:$pkg_java:$pkg_saxon:$pkg_calabash:$saxon
CP=$pkg_java:$pkg_saxon:$pkg_calabash:$saxon

# do it!
"$JAVA" -cp "$CP" org.expath.pkg.repo.tui.Main "$@"
