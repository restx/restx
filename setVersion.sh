#!/bin/bash

# Stop on errors
set -e

display_usage() {
  echo -e "Usage: setVersion.sh <VERSION>"
}

if [ $# -ne 1 ]
then
  display_usage
  exit 1
fi

VERSION=$1

sedi(){
  case "`uname`" in
      Darwin*) sed -i '' "$1" "$2" ;;
      *) sed -i "$1" "$2" ;;
  esac
}

sedi "s/\"restx.version\"[[:space:]]*:[[:space:]]*\"[0-9\\.a-zA-Z\\-]*\",/\"restx.version\": \"$VERSION\",/g" restx.build.properties.json
sedi "s/<restx.version>[0-9\\.a-zA-Z\\-]*</<restx.version>$VERSION</g" pom.xml
restx build generate ivy
mvn versions:set -DnewVersion=$VERSION -DgenerateBackupPoms=false

