#!/bin/bash

die() {
    echo
    echo "*** $@" 1>&2;
    exit 1;
}

deploy_pkg() {
    pkg="$1"
    if [[ ! -f "$pkg" ]]; then
        die "Package file does not exist: $pkg"
    fi
    if [[ -n "$2" ]]; then
        die "Extra param to deploy_pkg(): $2 (all: $@)"
    fi
    curl --form "xawfile=@$pkg" "$DEPLOY" 2>/dev/null
}

start_tomcat() {
    # start Tomcat up
    # TODO: Start it only if not already (and shut it down at the end only if we started it here)
    "${TOMCAT}/bin/startup.sh" \
        || die "Tomcat failed to startup."
    # wait for Tomcat to be up
    until curl --silent --show-error --connect-timeout 1 -I $MANAGER | grep "Coyote" >/dev/null; do
        echo Waiting for Tomcat to be up...
        sleep 2
    done
}

stop_tomcat() {
    "${TOMCAT}/bin/shutdown.sh" \
        || die "Tomcat failed to shutdown."
}

# the dir containing this script
BASEDIR=`dirname $0`
if [[ ! -d "${BASEDIR}" ]]; then
    die "INTERNAL ERROR: The install directory is not a directory?!?";
fi

# the Tomcat dir
TOMCAT=${BASEDIR}/apache-tomcat-7.0.27
if [[ ! -d "${TOMCAT}" ]]; then
    die "INTERNAL ERROR: The install directory does not look to be correct?!?";
fi

# the repo to use (first param)
REPO=$1
shift
if [[ -z "${REPO}" ]]; then
    die "You must provide the target repo dir as first param";
fi
if [[ "/" != `echo ${REPO} | cut -c 1` ]]; then
    REPO=`pwd`/${REPO}
fi
echo "Repo is set to: ${REPO}"
if [[ -d "${REPO}" ]]; then
    echo "Repo exists: remove it all"
    rm -rf "${REPO}";
fi
echo "Create repo dir"
mkdir "${REPO}"

# the context config (specific to Tomcat, contains the context root and the repo dir)
CTXT=$1
shift
if [[ -z "${CTXT}" ]]; then
    die "You must provide the context config file as second param";
fi
if [[ "/" != `echo ${CTXT} | cut -c 1` ]]; then
    CTXT=`pwd`/${CTXT}
fi
echo "Context is set to: ${CTXT}"

# the context root (where to install this Servlex instance)
ROOT=$1
shift
if [[ -z "${ROOT}" ]]; then
    die "You must provide the context root as third param";
fi
echo "Context root is set to: ${ROOT}"

# the test dir (containing XSpec test suites as $TESTDIR/*.xspec)
# TODO: Check there is at least one $TESTDIR/*.xspec
TESTDIR=$1
shift
if [[ -z "${TESTDIR}" ]]; then
    die "You must provide the test dir as fourth param";
fi
echo "Test dir is set to: ${TESTDIR}"
if [[ ! -d "${TESTDIR}" ]]; then
    die "The test dir does not exist or is not a dir: ${TESTDIR}"
fi

echo "REPO: $REPO"
echo "CTXT: $CTXT"
echo "ROOT: $ROOT"
echo "TEST: $TESTDIR"

# generate the context config file
echo "<Context path='/${ROOT}'>" > $CTXT;
echo "   <Parameter name='org.expath.servlex.repo.dir'" >> $CTXT;
echo "              value='${REPO}'" >> $CTXT;
echo "              override='false'/>" >> $CTXT;
echo "</Context>" >> $CTXT;

# the text manager URI from Tomcat
MANAGER=http://localhost:9090/manager/text
# the deploy URI from the Servlex manager
DEPLOY=http://localhost:9090/${ROOT}/manager/deploy

# start Tomcat up
start_tomcat

tmpfile=`mktemp /tmp/servlex-test.XXXXXX`

# get the list of deployed Tomcat webapps
curl -u servlex:servlex $MANAGER/list 2> /dev/null > "$tmpfile"
if [[ "OK - " != `head -n 1 < "$tmpfile" | cut -c 1-5` ]]; then
   die "Tomcat manager failed to return the webapp list
Output: $list"
fi

# is Servlex one of them? (if yes, undeploy it)
if [[ "${ROOT}" = `sed 's/.*://' < "$tmpfile" | grep ${ROOT}` ]]; then
    echo "Servlex deployed (at ${ROOT}), undeploying it..."
    # undeploy Servlex
    curl -u servlex:servlex "$MANAGER/undeploy?path=/${ROOT}" 2> /dev/null > "$tmpfile"
    if [[ ! "OK - " = `head -n 1 < "$tmpfile" | cut -c 1-5` ]]; then
        res=`cat $tmpfile`
        die "Tomcat manager failed to undeploy Servlex
Output: $res"
    fi
else
    echo "Servlex not deployed (at ${ROOT})"
fi

# the Servlex WAR file
# TODO: FIXME: How to make the path absolute without hard-coding it?
# (it is passed over HTTP, so has to be absolute)
# WAR="${BASEDIR}/../servlex/dist/servlex.war"
# if [[ ! -f "${WAR}" ]]; then
#     die "The WAR file does not exist at: ${WAR}"
# fi
WAR="$BASEDIR/../servlex/dist/servlex.war"

# deplpoying it
# TODO: Seems Tomcat needs path= even with config=!  Report it...
echo "Deploying Servlex (at ${ROOT})..."
echo "  using: curl -u servlex:servlex $MANAGER/deploy?path=/${ROOT}&config=${CTXT}&war=${WAR}"
res=`curl -u servlex:servlex "$MANAGER/deploy?path=/${ROOT}&config=${CTXT}&war=${WAR}" 2>/dev/null`
if [[ "OK - " != `echo $res | head -n 1 | cut -c 1-5` ]]; then
   die "Tomcat manager failed to deploy Servlex (at ${ROOT})
Output: $res"
fi

# deploy the hello-world EXPath webapp
# TODO: Adapt it to one specific test instance... (that is, unit tests...)
# HELLO_XAW=../samples/hello-world/dist/hello-world-0.6.0dev.xaw
# res=`curl --form xawfile=@$HELLO_XAW $DEPLOY 2>/dev/null`
# echo $res | grep "has been successfully installed" >/dev/null \
#     || die "Servlex failed to deploy $HELLO_XAW
# Output: $res"

# deploy the test webapps (each dir in webapps/)
# TODO: Adapt it to one specific test instance... (that is, unit tests...)
# for d in webapps/*; do
#     echo Building and deploying $d
#     ( cd $d && xproj build )
#     if [[ 1 != `ls $d/dist/ | wc -l | sed "s/ *//"` ]]; then
#         die "There is not exactly one file in $d/dist/"
#     fi
#     f=`ls $d/dist/*`
#     res=`curl --form xawfile=@$f $DEPLOY 2>/dev/null`
#     echo $res | grep "has been successfully installed" >/dev/null \
#         || die "Servlex failed to deploy $f
#     Output: $res"
# done

while [[ -n "$1" ]]
do
    pkg=$1
    shift
    echo "Deploying $pkg"
    res=`deploy_pkg $pkg`
    #res=`curl --form "xawfile=@$pkg" "$DEPLOY" 2>/dev/null`
    echo $res | grep "has been successfully installed" >/dev/null \
        || die "Servlex failed to deploy $pkg
Output: $res"
done

# restart Tomcat in case some Java extension packages have been installed
# (e.g. the EXPath ZIP and HTTP Client for Saxon, used in the H2O website...)
stop_tomcat
start_tomcat

# run the tests
HARNESS=http://www.jenitennison.com/xslt/xspec/saxon/harness/xslt.xproc
for suite in "${TESTDIR}"/*.xspec
do
    echo "Running suite $suite"
    report=`echo "$suite" | sed 's/.xspec$/.html/'`
    calabash -i "source="$suite"" \
        "$HARNESS" \
        > $report
done

# shut Tomcat down
stop_tomcat
