#!/bin/bash

die() {
    echo
    echo "*** $@" 1>&2;
    exit 1;
}

SHIP_SERVLEX_SAXON=true
SHIP_SERVLEX_SAXABASH=true
SAXON_PROC=../servlex-saxon/dist/servlex-saxon.jar
SAXON_DEPS=../servlex-saxon/lib
SAXABASH_PROC=../servlex-saxabash/dist/servlex-saxabash.jar
SAXABASH_DEPS=../servlex-saxabash/lib

VERSION="$1"
# the Servlex version number
if [[ -z "${VERSION}" ]]; then
    die "ERROR: The Servlex version number must be passed as first param!";
fi

# the IzPack "compile" script
IZPACK=/Applications/IzPack/bin/compile

# the Tomcat base name (both .tar.gz and dir must have the same name)
TOMCAT_NAME=apache-tomcat-8.0.33

# the dir containing this script
BASEDIR=`dirname $0`
if [[ ! -d "${BASEDIR}" ]]; then
    die "INTERNAL ERROR: The current directory is not a directory?!?";
fi

# the Tomcat dir
TOMCAT=${BASEDIR}/${TOMCAT_NAME}

# untar the archive
rm -r "${TOMCAT}"
( cd "${BASEDIR}"; tar zxf "${TOMCAT_NAME}.tar.gz" )

# adding properties to conf/catalina.properties
PROPS="${TOMCAT}/conf/catalina.properties"
echo >> "${PROPS}"
echo >> "${PROPS}"
echo "# Added by Servlex bundler for Tomcat" >> "${PROPS}"
echo "# " >> "${PROPS}"
echo "# The processors implementation class to use" >> "${PROPS}"
if "$SHIP_SERVLEX_SAXABASH" = "true"; then
    echo "org.expath.servlex.processors=net.servlex.saxabash.Saxabash" >> "${PROPS}"
elif "$SHIP_SERVLEX_SAXON" = "true"; then
    echo "org.expath.servlex.processors=net.servlex.saxon.Saxon" >> "${PROPS}"
else
    echo "# org.expath.servlex.processors=net.servlex.saxabash.Saxabash" >> "${PROPS}"
fi
echo "# The location of the repository" >> "${PROPS}"
echo 'org.expath.servlex.repo.dir=${INSTALL_PATH}/repo' >> "${PROPS}"
echo "# Uncomment to have Calabash generating profiling data" >> "${PROPS}"
echo '# org.expath.servlex.profile.dir=${INSTALL_PATH}/profiling' >> "${PROPS}"
echo "# Uncomment to log (in trace level) the actual content of requests/responses" >> "${PROPS}"
echo "# org.expath.servlex.trace.content=true" >> "${PROPS}"
echo "# Uncomment to set the default charset of requests (if not set in a request)" >> "${PROPS}"
echo "# org.expath.servlex.default.charset=UTF-8" >> "${PROPS}"

# changing the port numbers in conf/server.xml
# TODO: Set URIEncoding="UTF-8" on the connector as well?
( cd "${TOMCAT}"; patch -p0 < ../bundle-tomcat-server.patch )

# setting the users and roles
cp "${BASEDIR}/bundle-tomcat-users.xml" "${TOMCAT}/conf/tomcat-users.xml"

# remove existing webapps
rm -r "${TOMCAT}"/webapps/*

# unzip the WAR into webapps/ROOT
# TODO: Should we deploy it throught the REST API instead?
# (means starting Tomcat, sending the WAR file over HTTP, etc.)
mkdir "${TOMCAT}/webapps/ROOT"
( cd "${TOMCAT}/webapps/ROOT"; unzip ../../../../servlex/dist/servlex.war )

# copy servlex loader JAR into lib
cp ../servlex-loader/dist/servlex-loader.jar "${TOMCAT}/lib/"

# creating the repo and the profiling directory
mkdir "${TOMCAT}/repo"
mkdir "${TOMCAT}/repo/.expath-web"
mkdir "${TOMCAT}/repo/.servlex"
mkdir "${TOMCAT}/repo/.servlex/lib"
mkdir "${TOMCAT}/profiling"

# empty webapps.xml file
cp "${BASEDIR}/webapps.xml" "${TOMCAT}/repo/.expath-web/"

# the processors JAR files and their dependencies
if "$SHIP_SERVLEX_SAXON" = "true" -o "$SHIP_SERVLEX_SAXABASH" = "true"; then
    cp "${SAXON_PROC}"       "${TOMCAT}/repo/.servlex/lib/"
    cp "${SAXON_DEPS}"/*.jar "${TOMCAT}/repo/.servlex/lib/"
fi
if "$SHIP_SERVLEX_SAXABASH" = "true"; then
    cp "${SAXABASH_PROC}"       "${TOMCAT}/repo/.servlex/lib/"
    cp "${SAXABASH_DEPS}"/*.jar "${TOMCAT}/repo/.servlex/lib/"
fi

# the xrepo.sh script
# TODO: Why did I copy xrepo.sh here?  Why don't I copy the original?
cp "${BASEDIR}/xrepo-tomcat.sh" "${TOMCAT}/bin/xrepo.sh"
chmod u+x "${TOMCAT}/bin/xrepo.sh"

# the saxon script
cp "${BASEDIR}/../../pkg-java/bin/saxon" "${TOMCAT}/bin/saxon.sh"
chmod u+x "${TOMCAT}/bin/saxon.sh"

# deploy the webapp manager
"${TOMCAT}/bin/xrepo.sh" --repo "${TOMCAT}/repo"       \
    install "${BASEDIR}/apps/webapp-manager-0.3.1.xaw" \
    || die "Error deploying webapp manager XAW"
"${TOMCAT}/bin/xrepo.sh" --repo "${TOMCAT}/repo"                  \
    install "${BASEDIR}/apps/expath-http-client-saxon-0.12.0.xar" \
    || die "Error deploying http-client XAW"
"${TOMCAT}/bin/xrepo.sh" --repo "${TOMCAT}/repo"         \
    install "${BASEDIR}/apps/expath-zip-saxon-0.8.0.xar" \
    || die "Error deploying zip XAW"

# replace the Servlex version number
perl -e "s|<appversion>([-.0-9a-z]+)</appversion>|<appversion>${VERSION}</appversion>|g;" \
    -pi izpack-tomcat.xml
perl -e "s|servlex-([-.0-9a-z]+)/hello-world/|servlex-${VERSION}/hello-world/|g;" \
    -pi izpack-tomcat.xml
perl -e "s|servlex-([-.0-9a-z]+)/hello-world-([-.0-9a-z]+).xaw|servlex-${VERSION}/hello-world-${VERSION}.xaw|g;" \
    -pi izpack-tomcat.xml
perl -e "s|apache-tomcat-[.0-9]+/|${TOMCAT_NAME}/|g;" \
    -pi izpack-tomcat.xml

# create the installer
# TODO: IzPack outputs a lot of helpless warnings, hope it will be fixed in a future version...
"${IZPACK}" izpack-tomcat.xml -o "servlex-installer-${VERSION}.jar" 2>&1 \
    | grep -v 'com.sun.java.util.jar.pack.Utils$Pack200Logger warning'   \
    | grep -v "bytes of LocalVariableTable attribute in"                 \
    | grep -v "bytes of LineNumberTable attribute in"
