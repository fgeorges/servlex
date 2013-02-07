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

# the Tomcat base name (both .tar.gz and dir must have the same name)
TOMCAT_NAME=apache-tomcat-7.0.35

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
PROPS="${TOMCAT}/conf/catalina.properties"
echo >> "${PROPS}"
echo >> "${PROPS}"
echo "# Added by Servlex bundler for Tomcat" >> "${PROPS}"
echo 'org.expath.servlex.repo.dir=${INSTALL_PATH}/repo' >> "${PROPS}"

# changing the port numbers in conf/server.xml
( cd "${TOMCAT}"; patch -p0 < ../bundle-tomcat-server.patch )

# setting the users and roles
cp "${BASEDIR}/bundle-tomcat-users.xml" "${TOMCAT}/conf/tomcat-users.xml"

# remove existing webapps
rm -r "${TOMCAT}"/webapps/*

# unzip the WAR into webapps/ROOT
mkdir "${TOMCAT}/webapps/ROOT"
( cd "${TOMCAT}/webapps/ROOT"; unzip ../../../../servlex/dist/servlex.war )

# create the installer
# TODO: Substitute version number and such in the IzPack descriptor...
IZPACK=/Applications/IzPack/bin/compile
VERSION=0.7.0pre3
"${IZPACK}" izpack-tomcat.xml -o "servlex-${VERSION}-intaller.jar"

# # the Tomcat manager URI
# MANAGER=http://localhost:19757/manager/text
# # the Servlex WAR file
# WAR=${BASEDIR}/../servlex/dist/servlex.war

# # start Tomcat up
# start_tomcat

# # deploy Servlex on Tomcat, as the root context
# echo "Deploying Servlex..."
# echo "  using: curl -u admin:admin $MANAGER/deploy?path=/&war=${WAR}"
# res=`curl -u admin:admin "$MANAGER/deploy?path=/&war=${WAR}" 2>/dev/null`
# if [[ "OK - " != `echo $res | head -n 1 | cut -c 1-5` ]]; then
#    die "Tomcat manager failed to deploy Servlex
# Output: $res"
# fi

# # stop Tomcat
# stop_tomcat
