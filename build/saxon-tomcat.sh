#! /bin/bash

## saxon [--xsl|--xq]? [--repo=...]? [--add-cp=...]* \
##       [--cp=...]* [--java=...]? [--mem=...]?      \
##       [--proxy=[user:password@]?host:port]?       \
##       <original Saxon options>
##
## Order of options is not significant, but the options to be
## forwarded to Saxon must be at the end.  The special option "--" can
## be used to say "no more option to be used by the script, just
## forward any remaining one".  See below for an explanation of the
## options (in usage(), or the output of the option --help).
##
## The classpath, got from SAXON_CP, must include expath-repo.jar,
## expath-saxon.jar and Apache's resolver.jar, as well as JAR files
## for Saxon itself.  If SAXON_CP is not set, it is constructed by
## using the JAR files in this install directory, except Saxon JAR
## files which must be in the directory SAXON_HOME.
##
## If neither SAXON_CP and SAXON_HOME are set, this is an error.
## SAXON_HOME can be changed here below...
##
## In addition, EXPATH_REPO is used as the default value for --repo,
## and SAXON_SCRIPT_HOME is used to substitude "~" in front of a path
## (this convention is from Unix, and is not understood by Java).
##
## The java command is taken from JAVA_HOME (or just 'java' if not
## set.)


# ==================== utility functions ====================

resolve () {
    if echo "$1" | grep -- '^~' >/dev/null 2>&1; then
        echo "$MY_HOME"`echo $1 | sed s/^~//`;
    else
        echo "$1"
    fi
}

opt_value () {
    echo $1 | sed 's/^--[-a-z]*=//'
}

opt_resolve_value () {
    tmp=`opt_value $1`
    echo `resolve $tmp`;
}

die() {
    echo
    echo "*** $@" 1>&2;
    exit 1;
}

usage () {
    echo
    echo "Usage: saxon <script options> <processor options>"
    echo
    echo "<processor options> are any option accepted by the original command-line"
    echo "Saxon frontend.  Script options are (all are optional, those marked with"
    echo "an * are repeatable):"
    echo
    echo "  --help                    display this help message"
    echo "  --xsl                     invoke Saxon as an XSLT processor (the default)"
    echo "  --xq                      invoke Saxon as an XQuery processor"
    echo "  --repo ...                set the EXPath Packaging repository dir"
    echo "  --add-cp classpath *      add an entry to the classpath"
    echo "  --cp classpath *          set the classpath (override the default classpath)"
    echo "  --java ...                add an option to the Java Virtual Machine"
    echo "  --mem ...                 set the memory (shortcut for --java=-Xmx...)"
    echo "  --proxy [user:password@]host:port"
    echo "                            HTTP and HTTPS proxy information"
    echo "  --show-cp                 display the classpath from packages, then exit"
    echo "  --version                 display the Saxon version number"
    echo
}

set_saxon_cp() {
    if test -z "$SAXON_CP"; then
        # get the install dir
        # TODO: Ask IzPack for substituing it directly at install time...?
        INSTALL_DIR=`dirname $0`/..
        if test \! -d "$INSTALL_DIR"; then
            die "INTERNAL ERROR: The install directory is not a directory?!? ($INSTALL_DIR)"
        fi
        # Set SAXON_HOME in your environment or here, if you don't set SAXON_CP
        if test -z "$SAXON_HOME"; then
            # By default, try saxon/ as SAXON_HOME in the install dir
            if test -d "$INSTALL_DIR/saxon"; then
                SAXON_HOME="$INSTALL_DIR/saxon"
            else
                die "SAXON_CP and SAXON_HOME both not set, and $INSTALL_DIR/saxon/ does not exist!"
            fi
        fi
        if test -f "${SAXON_HOME}/saxon9ee.jar"; then
            SAXON_CP="${SAXON_HOME}/saxon9ee.jar";
        elif test -f "${SAXON_HOME}/saxon9pe.jar"; then
            SAXON_CP="${SAXON_HOME}/saxon9pe.jar";
        elif test -f "${SAXON_HOME}/saxon9he.jar"; then
            SAXON_CP="${SAXON_HOME}/saxon9he.jar";
        elif test -f "${SAXON_HOME}/saxon9sa.jar"; then
            SAXON_CP="${SAXON_HOME}/saxon9sa.jar";
        elif test -f "${SAXON_HOME}/saxon9.jar"; then
            SAXON_CP="${SAXON_HOME}/saxon9.jar";
        elif test -f "${SAXON_HOME}/saxon8sa.jar"; then
            SAXON_CP="${SAXON_HOME}/saxon8sa.jar";
        elif test -f "${SAXON_HOME}/saxon8.jar"; then
            SAXON_CP="${SAXON_HOME}/saxon8.jar";
        else
            die "Saxon JAR cannot be found in SAXON_HOME: $SAXON_HOME"
        fi
        # add the EXPath tools-java.jar to the classpath
        if test -z "$EXPATH_TOOLS_JAVA_JAR"; then
            if test \! -f "$INSTALL_DIR/expath/tools-java.jar"; then
                die "INTERNAL ERROR: The install directory does not contain expath/tools-java.jar?!? ($INSTALL_DIR)"
            fi
            SAXON_CP="$SAXON_CP$CP_DELIM$INSTALL_DIR/expath/tools-java.jar"
        else
            if test \! -f "$EXPATH_TOOLS_JAVA_JAR"; then
                die "\$EXPATH_TOOLS_JAVA_JAR does not exist: $EXPATH_TOOLS_JAVA_JAR"
            fi
            SAXON_CP="$SAXON_CP$CP_DELIM$EXPATH_TOOLS_JAVA_JAR"
        fi
        # add the EXPath tools-saxon.jar to the classpath
        if test -z "$EXPATH_TOOLS_SAXON_JAR"; then
            if test \! -f "$INSTALL_DIR/expath/tools-saxon.jar"; then
                die "INTERNAL ERROR: The install directory does not contain expath/tools-saxon.jar?!? ($INSTALL_DIR)"
            fi
            SAXON_CP="$SAXON_CP$CP_DELIM$INSTALL_DIR/expath/tools-saxon.jar"
        else
            if test \! -f "$EXPATH_TOOLS_SAXON_JAR"; then
                die "\$EXPATH_TOOLS_SAXON_JAR does not exist: $EXPATH_TOOLS_SAXON_JAR"
            fi
            SAXON_CP="$SAXON_CP$CP_DELIM$EXPATH_TOOLS_SAXON_JAR"
        fi
        # add the EXPath pkg-java.jar to the classpath
        if test -z "$EXPATH_PKG_REPO_JAR"; then
            if test \! -f "$INSTALL_DIR/expath/pkg-java.jar"; then
                die "INTERNAL ERROR: The install directory does not contain expath/pkg-java.jar?!? ($INSTALL_DIR)"
            fi
            SAXON_CP="$SAXON_CP$CP_DELIM$INSTALL_DIR/expath/pkg-java.jar"
        else
            if test \! -f "$EXPATH_PKG_REPO_JAR"; then
                die "\$EXPATH_PKG_REPO_JAR does not exist: $EXPATH_PKG_REPO_JAR"
            fi
            SAXON_CP="$SAXON_CP$CP_DELIM$EXPATH_PKG_REPO_JAR"
        fi
        # add the EXPath pkg-saxon.jar to the classpath
        if test -z "$EXPATH_PKG_SAXON_JAR"; then
            if test \! -f "$INSTALL_DIR/expath/pkg-saxon.jar"; then
                die "INTERNAL ERROR: The install directory does not contain expath/pkg-saxon.jar?!? ($INSTALL_DIR)"
            fi
            SAXON_CP="$SAXON_CP$CP_DELIM$INSTALL_DIR/expath/pkg-saxon.jar"
        else
            if test \! -f "$EXPATH_PKG_SAXON_JAR"; then
                die "\$EXPATH_PKG_SAXON_JAR does not exist: $EXPATH_PKG_SAXON_JAR"
            fi
            SAXON_CP="$SAXON_CP$CP_DELIM$EXPATH_PKG_SAXON_JAR"
        fi
    fi
}

# ==================== useful values ====================

if test -z "$JAVA_HOME"; then
    JAVA=java
else
    JAVA=${JAVA_HOME}/bin/java
fi

if uname | grep -i cygwin >/dev/null 2>&1; then
    CP_DELIM=";"
else
    CP_DELIM=":"
fi

# by default 'xslt', can also be 'xquery'
SAXON_KIND=xslt
if test -z "$SAXON_SCRIPT_HOME"; then
    MY_HOME=$HOME
else
    MY_HOME=$SAXON_SCRIPT_HOME
fi
MEMORY=512m
PROXY=$FG_PROXY

CP=
JAVA_OPT=
SAXON_OPT=
# defaults to the environment variable, but can be changed by --repo=...
REPO=$EXPATH_REPO

# ==================== the options ====================

SHOW_CP=false

while echo "$1" | grep -- ^-- >/dev/null 2>&1 && test "$1" != --; do
    case "$1" in
        # XSLT engine
        --xsl)
            SAXON_KIND=xslt;;
        --xslt)
            SAXON_KIND=xslt;;
        # XQuery engine
        --xq)
            SAXON_KIND=xquery;;
        --xquery)
            SAXON_KIND=xquery;;
        # Saxon version
        --version)
            SAXON_KIND=version;;
        # The EXPath Packaging repository
        --repo)
            shift
            REPO=$1;;
        # Add some path to the class path.  May be repeated.
        --add-cp)
            shift
            ADD_CP="${ADD_CP}${CP_DELIM}`resolve $1`";;
        # Set the class path.  May be repeated.
        --cp)
            shift
            CP="${CP}${CP_DELIM}`resolve $1`";;
        # The memory space to give to the JVM
        --mem)
            shift
            MEMORY=$1;;
        # Add support for --proxy=user:password@host:port
        --proxy)
            shift
            PROXY=$1;;
        # Additional option for the JVM
        --java)
            shift
            JAVA_OPT="$JAVA_OPT $1";;
        --help)
            usage
            exit 0;;
        --show-cp)
            SHOW_CP=true;;
        # Unknown option!
        --*)
            die "Unknown option: $1"
    esac
    shift;
done

# ==================== EXPath repo ====================

REPO_CP=

if test -n "$REPO"; then

    # the repo itself
    JAVA_OPT="$JAVA_OPT -Dorg.expath.pkg.saxon.repo=$REPO"

    # loop over each package dir
    for pkg in `resolve "${REPO}"`/*; do
	cp_file="$pkg/.saxon/classpath.txt"
        # if the package has a Saxon classpath file, add its content to REPO_CP
        if test -f "$cp_file"; then
            oldIFS=$IFS
            IFS=$'\n'
            for jar in `cat "$cp_file"`; do
                # if relative, resolve it against the package content dir
                # TODO: Most likely to adapt to Cygwin's definition of "absolute".
                case "$jar" in
                    # absolute $jar
                    /*) jar_abs="$jar";;
                    # relative $jar
                    *)  jar_abs="$pkg/content/$jar";;
                esac
                REPO_CP="${REPO_CP}${CP_DELIM}${jar_abs}";
            done
            IFS=$oldIFS
        fi
    done

fi

if [ "$SHOW_CP" = "true" ]; then
    # remove the first CP_DELIM at the beginning of REPO_CP
    echo "$REPO_CP" | cut -c 2-;
    exit 0;
fi

# ==================== proxy ====================

# TODO: Check the format of the PROXY value
if test -n "$PROXY"; then
    PROXY_HOST=`echo $PROXY | sed "s/^\(\(.*\):\(.*\)@\)\?\(.*\):\([0-9]*\)$/\4/"`
    PROXY_PORT=`echo $PROXY | sed "s/^\(\(.*\):\(.*\)@\)\?\(.*\):\([0-9]*\)$/\5/"`
    JAVA_OPT="$JAVA_OPT -Dhttp.proxyHost=$PROXY_HOST"
    JAVA_OPT="$JAVA_OPT -Dhttp.proxyPort=$PROXY_PORT"
    JAVA_OPT="$JAVA_OPT -Dhttps.proxyHost=$PROXY_HOST"
    JAVA_OPT="$JAVA_OPT -Dhttps.proxyPort=$PROXY_PORT"

    PROXY_USER=`echo $PROXY | sed "s/^\(\(.*\):\(.*\)@\)\?\(.*\):\([0-9]*\)$/\2/"`
    PROXY_PWD=`echo $PROXY | sed "s/^\(\(.*\):\(.*\)@\)\?\(.*\):\([0-9]*\)$/\3/"`
    # if test -n "$PROXY_USER"; then
        # TODO: Move the class to pkg-saxon (adapt from fgeorges.*)
        # JAVA_OPT="$JAVA_OPT -Dfgeorges.httpProxyUser=$PROXY_USER"
        # JAVA_OPT="$JAVA_OPT -Dfgeorges.httpProxyPwd=$PROXY_PWD"
        # SAXON_OPT="$SAXON_OPT -r${OPT_DELIM}org.fgeorges.saxon.HttpProxyUriResolver"
    # fi
fi

# ==================== launch Saxon ====================

# TODO: Add logging configuration facility

if test "$SAXON_KIND" = version; then
    SAXON_CLASS=net.sf.saxon.Version;
elif test "$SAXON_KIND" = xslt; then
    SAXON_CLASS=net.sf.saxon.Transform;
else
    SAXON_CLASS=net.sf.saxon.Query;
fi

set_saxon_cp
CP="${SAXON_CP}${ADD_CP}${REPO_CP}"

# TODO: Include the logging into the big picture...
"$JAVA" "-Xmx$MEMORY" \
    $JAVA_OPT \
    -ea -esa \
    -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog \
    -Dorg.apache.commons.logging.simplelog.showdatetime=true \
    -Dorg.apache.commons.logging.simplelog.log.org=INFO \
    -Dorg.expath.hc.http.version=1.1 \
    -cp "$CP" \
    $SAXON_CLASS \
    -init:org.expath.pkg.saxon.PkgInitializer \
    $SAXON_OPT \
    "$@"
