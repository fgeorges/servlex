#! /bin/bash

## servlex-appengine [--servlex ...] [--dir ...]? [--desc ...]? [--xrepo ...]? \
##                   [--pkg ...]? [--jar ...]?  [--tmp ...]? [--help] \
##                   <list of XAR and XAW files...>
##
## The only mandatory option is --servlex (as well as at least one package,
## that is, one XAR or XAW file).  Other options default values are:
##
##   --dir          dist/servlex-appengine-war
##   --desc         xproject/appengine-web.xml
##   --xrepo        xrepo
##   --pkg          org.expath.servlex.repo.appengine
##   --jar          servlex-appengine-repo.jar
##   --tmp          <some-tmp-dir>
##
## The special option "--" can be used to say "stop interpreting remainding
## parameters as options, treat them as file names regardless they start by
## '--' or not".  The semantics of the options is as follows:
##
##   --servlex      path to the Servlex WAR file
##   --dir          the target directory to create the exploded WAR into
##   --desc         path to the GAE appengine-web.xml descriptor
##   --xrepo        path to the xrepo command (by default in the PATH)
##   --pkg          Java package name to put the in-classpath-repository into
##   --jar          the name of the JAR file to create for the repository
##   --tmp          a working directory to create files (by default a tmp dir)
##
## The "option" --help is of course different, it displays a help message
## regardless of any other option if any.


# ==================== utility functions ====================

die() {
    echo
    echo "*** $@" 1>&2;
    exit 1;
}

usage () {
    echo
    echo "Usage: servlex-appengine [--servlex ...] [--dir ...]? [--desc ...]?"
    echo "                         [--xrepo ...]?  [--pkg ...]? [--jar ...]?"
    echo "                         [--tmp ...]? [--help]?"
    echo "                         <list of XAR and XAW files...>"
    echo
    echo "  --servlex     path to the Servlex WAR file"
    echo "  --dir         the target directory to create the exploded WAR into"
    echo "  --desc        path to the GAE appengine-web.xml descriptor"
    echo "  --xrepo       path to the xrepo command (by default in the PATH)"
    echo "  --pkg         Java package name to put the in-classpath-repository in"
    echo "  --jar         the name of the JAR file to create for the repository"
    echo "  --tmp         a working directory to create files (by default a tmp dir)"
    echo "  --help        display this help message"
    echo
}

absolute() {
     dirn=`dirname "$1"`
     basen=`basename "$1"`
     abspath="`cd \"$dirn\" 2>/dev/null && pwd || echo \"$dirn\"`/$basen"
     echo $abspath
}


# ==================== the options ====================

OPT_SERVLEX=
OPT_DIR=dist/servlex-appengine-war
OPT_DESC=xproject/appengine-web.xml
OPT_XREPO=xrepo
OPT_PKG=org.expath.servlex.repo.appengine
OPT_JAR=servlex-appengine-repo.jar
OPT_TMP=

# TODO: Parse them...
while echo "$1" | grep -- ^-- >/dev/null 2>&1 && test "$1" != --; do
    case "$1" in
        --servlex)
            shift
            OPT_SERVLEX=$1;;
        --dir)
            shift
            OPT_DIR=$1;;
        --desc)
            shift
            OPT_DESC=$1;;
        --xrepo)
            shift
            OPT_XREPO=$1;;
        --pkg)
            shift
            OPT_PKG=$1;;
        --jar)
            shift
            OPT_JAR=$1;;
        --tmp)
            shift
            OPT_TMP=$1;;
        --help)
            usage
            exit 0;;
        --*)
            die "Unknown option: $1"
    esac
    shift;
done


# ==================== option sanity checks ====================

if test -z "$1"; then
    die "No package provided (that is, XAR and/or XAW files)"
fi

if test -z "$OPT_SERVLEX"; then
    die "--servlex not provided"
fi
if test \! -f "$OPT_SERVLEX"; then
    die "--servlex does not exist or is not a regular file: $OPT_SERVLEX"
fi

# might exist (must be empty), or it will be created (parent dir must exist)
if test \! -e "$OPT_DIR"; then
    mkdir "$OPT_DIR" || die "Cannot create --dir: $OPT_DIR"
fi
if test \! -d "$OPT_DIR"; then
    die "--dir does not exist or is not a directory: $OPT_DIR"
fi
if test -e "$OPT_DIR"/*; then
    die "--dir is not empty: $OPT_DIR"
fi

"$OPT_XREPO" help >/dev/null 2>&1 || die "Cannot execute xrepo: $OPT_XREPO"

# TODO: Create it just before first use?  In order to avoid creating a temp dir
# if another condition makes we won't ever use it (e.g. if we exit before for
# whatever reason)...
if test -z "$OPT_TMP"; then
    OPT_TMP=`mktemp -d -t servlex-appengine-tmp` || die "Cannot create tmp dir"
fi
if test \! -d "$OPT_TMP"; then
    die "--tmp does not exist or is not a directory: $OPT_TMP"
fi

# create the temporary repo
REPO=`echo $OPT_PKG | sed s#\\\.#/#g`
REPO_DIR="$OPT_TMP"/"$REPO"
mkdir -p "$REPO_DIR" || die "Impossible to create the repo dir: $REPO_DIR"

# echo "OPT_SERVLEX:	$OPT_SERVLEX"
# echo "OPT_DIR:	$OPT_DIR"
# echo "OPT_DESC:	$OPT_DESC"
# echo "OPT_XREPO:	$OPT_XREPO"
# echo "OPT_PKG:	$OPT_PKG"
# echo "  REPO:		$REPO"
# echo "  REPO_DIR:	$REPO_DIR"
# echo "OPT_JAR:	$OPT_JAR"
# echo "OPT_TMP:	$OPT_TMP"


# ==================== do it! ====================

# install the packages within the tmp repo
while [ -n "$1" ]; do
    echo "Deploy $1 ..."
    "$OPT_XREPO" --repo "$REPO_DIR" install "$1"
    shift
done

# jar up the repo
(cd "$OPT_TMP" && jar cf "$OPT_JAR" *) \
    || die "Error jaring up the repo in $OPT_TMP to $OPT_JAR"

# unzip the WAR within the destination dir
war=`absolute $OPT_SERVLEX`
(cd "$OPT_DIR" && unzip "$war") \
    || die "Error unziping the Servlex WAR into the destination dir: $OPT_SERVLEX to $OPT_DIR"

# copy the app descriptor and the JAR in the app dir (the decriptor must set
# org.expath.servlex.repo.classpath to $OPT_PKG)
cp "$OPT_DESC"         "$OPT_DIR/WEB-INF/"
cp "$OPT_TMP/$OPT_JAR" "$OPT_DIR/WEB-INF/lib/"

# Saxon-specific: packages for Java extensions for Saxon create a file
# .saxon/classpath (one for each package) when they are installed in
# the repo.  Parse them if any, and move all the listed JARs to the
# WAR dir WEB-INF/lib.
oldIFS=$IFS
IFS=$'\n'
for pkg in "$REPO_DIR"/*; do
    cp="$pkg/.saxon/classpath.txt"
    if test -f "$cp"; then
        for jar in `cat "$cp"`
        #while read jar
        do
            cp "$jar" "$OPT_DIR/WEB-INF/lib/"
        done
    fi
done
IFS=$oldIFS

# now, the user can execute: "appenginecfg update $OPT_DIR"
