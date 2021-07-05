#!/bin/sh
# Define versions
if [ -z "${1}" ] ; then
    NEW_VERSION=1.47.18
else
    NEW_VERSION=$1
fi
VF="./../VERSION-desktop"
# set versions in parent pom and in all childs
echo "New version is: $NEW_VERSION"
echo $NEW_VERSION > $VF
echo "Changing Maven's POM files"
mvn versions:set -DnewVersion=${NEW_VERSION}

PKG_PATH=packaging/pkg-apollo-desktop.json
echo "Changing pkg-apollo-desktop.json"
sed -i -e "s/\ \"version\".*/ \"version\": \"$NEW_VERSION\",/g" ${PKG_PATH}
