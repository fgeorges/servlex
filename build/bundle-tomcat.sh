#!/bin/bash

die() {
    echo
    echo "*** $@" 1>&2;
    exit 1;
}

start_tomcat() {
    # start Tomcat up
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

VERSION="$1"
# the Servlex version number
if [[ -z "${VERSION}" ]]; then
    die "ERROR: The Servlex version number must be passed as first param!";
fi

# the IzPack "compile" script
IZPACK=/Applications/IzPack/bin/compile

# the Tomcat base name (both .tar.gz and dir must have the same name)
TOMCAT_NAME=apache-tomcat-7.0.37

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

# creating the repo and adding the repo property to conf/catalina.properties
mkdir "${TOMCAT}/repo"
mkdir "${TOMCAT}/profiling"
PROPS="${TOMCAT}/conf/catalina.properties"
echo >> "${PROPS}"
echo >> "${PROPS}"
echo "# Added by Servlex bundler for Tomcat" >> "${PROPS}"
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
mkdir "${TOMCAT}/webapps/ROOT"
( cd "${TOMCAT}/webapps/ROOT"; unzip ../../../../servlex/dist/servlex.war )

# replace the Servlex version number
perl -e "s|<appversion>([-.0-9a-z]+)</appversion>|<appversion>${VERSION}</appversion>|g;" \
    -pi izpack-tomcat.xml
perl -e "s|apache-tomcat-[.0-9]+/|${TOMCAT_NAME}/|g;" \
    -pi izpack-tomcat.xml

# create the installer
"${IZPACK}" izpack-tomcat.xml -o "servlex-${VERSION}-installer.jar"
