#! /bin/bash

# The version number to build a release for.  To edit when changing
# the version number.  Don't forget to keep the following file in sync
# too: samples/hello-world/xproject/project.xml
DIST_VER=0.9.0dev
DIR=servlex-${DIST_VER}
REVISION=`git describe --always`

WAR=../servlex/dist/servlex.war
JAR=../servlex/dist/servlex.jar

HELLO=../samples/hello-world
HELLO_dist=$HELLO/dist
HELLO_src=$HELLO/src
HELLO_proj=$HELLO/xproject
HELLO_xaw=$HELLO_dist/hello-world-${DIST_VER}.xaw

die() {
    echo
    echo "*** $@" 1>&2;
    exit 1;
}

PWD=`pwd`
if test `basename $PWD` \!= build; then
    die "You must be in the servlex/build directory (the same as this script)"
fi

# clean up
rm -rf ${DIR}
rm -f ${DIR}.zip
rm -f ${DIR}-installer.jar

# the release dir
mkdir ${DIR}

# README and VERSION
cp README ${DIR}/
echo "Version: ${DIST_VER}" > ${DIR}/VERSION
echo "Subversion revision: ${REVISION}" >> ${DIR}/VERSION
VERSION_PROP=../servlex/src/java/org/expath/servlex/tools/version.properties
echo "org.expath.servlex.version=${DIST_VER}" > ${VERSION_PROP}
echo "org.expath.servlex.revision=${REVISION}" >> ${VERSION_PROP}

# build it
( cd ../servlex/ && ant ) || die "Build failed"
if test \! -f "$WAR"; then
    die "$WAR does not exist"
fi
if test \! -f "$JAR"; then
    die "$JAR does not exist"
fi

# WAR and JAR
cp "$WAR" ${DIR}/
cp "$JAR" ${DIR}/

( cd ../samples/hello-world && xproj build ) || die "Hello world build failed"
if test \! -f "$HELLO_xaw"; then
    die "$HELLO_xaw does not exist"
fi

# hello-world: the XAW...
cp "$HELLO_xaw" ${DIR}/
# ...and the project sources
mkdir ${DIR}/hello-world
mkdir ${DIR}/hello-world/xproject
cp $HELLO_proj/*.xml ${DIR}/hello-world/xproject/
mkdir ${DIR}/hello-world/src
cp $HELLO_src/hello.* ${DIR}/hello-world/src/

# zip up the whole thing
zip -r ${DIR}.zip ${DIR}/

# create the IzPack installer
./bundle-tomcat.sh "${DIST_VER}"
