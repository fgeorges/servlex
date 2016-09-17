#! /bin/bash

# The version number to build a release for.  To edit when changing
# the version number.  Don't forget to keep the following file in sync
# too: samples/hello-world/xproject/project.xml, as well as in the
# IZpack file: build/izpack-tomcat.xml
DIST_VER=0.11.0pre3
DIR=servlex-${DIST_VER}
BIN="${DIR}/bin"
SAMPLES="${DIR}/samples"
REVISION=`git show-ref --hash --abbrev HEAD`

WAR=../servlex/dist/servlex.war
JAR=../servlex/dist/servlex.jar
LOADER=../servlex-loader/dist/servlex-loader.jar

SHIP_SERVLEX_SAXON=true
SHIP_SERVLEX_SAXABASH=false
SAXON_PROC=../servlex-saxon/dist/servlex-saxon.jar
SAXON_DEPS=../servlex-saxon/lib
SAXABASH_PROC=../servlex-saxabash/dist/servlex-saxabash.jar
SAXABASH_DEPS=../servlex-saxabash/lib

HELLO=../samples/hello-world
HELLO_dist=$HELLO/dist
HELLO_src=$HELLO/src
HELLO_proj=$HELLO/xproject
HELLO_xaw=$HELLO_dist/hello-world-${DIST_VER}.xaw

MANAGER_xaw=../samples/webapp-manager/dist/webapp-manager-0.3.1.xaw

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
echo "Git revision: #${REVISION}" >> ${DIR}/VERSION
VERSION_PROP=../servlex/src/java/org/expath/servlex/tools/version.properties
echo "org.expath.servlex.version=${DIST_VER}" > ${VERSION_PROP}
echo "org.expath.servlex.revision=${REVISION}" >> ${VERSION_PROP}

# build servlex
( cd ../servlex/ && ant ) || die "Servlex build failed"
if test \! -f "$WAR"; then
    die "$WAR does not exist"
fi
if test \! -f "$JAR"; then
    die "$JAR does not exist"
fi

# servlex WAR and JAR
cp "$WAR" ${DIR}/
cp "$JAR" ${DIR}/

# build servlex-loader
( cd ../servlex-loader/ && ant ) || die "Servlex Loader build failed"
if test \! -f "$LOADER"; then
    die "$LOADER does not exist"
fi

# servlex loader JAR
cp "$LOADER" ${DIR}/

if "$SHIP_SERVLEX_SAXON" = "true"; then
    # build servlex-saxon
    ( cd ../servlex-saxon/ && ant ) || die "Servlex Saxon processor build failed"
    if test \! -f "$SAXON_PROC"; then
        die "$SAXON_PROC does not exist"
    fi

    # servlex saxon JAR
    mkdir ${DIR}/saxon
    cp "$SAXON_PROC"       ${DIR}/saxon/
    cp "$SAXON_DEPS"/*.jar ${DIR}/saxon/
fi

if "$SHIP_SERVLEX_SAXABASH" = "true"; then
    # build servlex-saxabash
    ( cd ../servlex-saxabash/ && ant ) || die "Servlex Saxabash processor build failed"
    if test \! -f "$SAXABASH_PROC"; then
        die "$SAXABASH_PROC does not exist"
    fi

    # servlex saxabash JAR
    mkdir ${DIR}/saxabash
    cp "$SAXABASH_PROC"       ${DIR}/saxabash/
    cp "$SAXABASH_DEPS"/*.jar ${DIR}/saxabash/
fi

# the bin dir
mkdir ${BIN}

cp xrepo-tomcat.sh ${BIN}/xrepo.sh
cp ../../pkg-java/bin/saxon ${BIN}/saxon.sh
chmod u+x ${BIN}/*

# the samples dir
mkdir ${SAMPLES}

# hello world: the XAW...
( cd ../samples/hello-world && xproj build ) || die "Hello world build failed"
if test \! -f "$HELLO_xaw"; then
    die "$HELLO_xaw does not exist"
fi
cp "$HELLO_xaw" ${SAMPLES}/

# ...and the project sources
HW=${SAMPLES}/hello-world
mkdir ${HW}
mkdir ${HW}/xproject
cp $HELLO_proj/*.xml ${HW}/xproject/
mkdir ${HW}/src
cp $HELLO_src/hello.* ${HW}/src/

# webapp-manager: the XAW
if test \! -f "$MANAGER_xaw"; then
    die "$MANAGER_xaw does not exist"
fi
cp "$MANAGER_xaw" ${SAMPLES}/

# zip up the whole thing
zip -r ${DIR}.zip ${DIR}/

# create the IzPack installer
./bundle-tomcat.sh "${DIST_VER}"
