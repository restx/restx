#!/bin/bash

# Stop on errors
set -e

display_usage() {
  echo -e "Usage: release.sh <RELEASE_VERSION> <NEXT_DEV_VERSION>"
}

if [ $# -ne 2 ]
then
  display_usage
  exit 1
fi

sedi(){
  case "`uname`" in
    Darwin*) sed -i '' "$1" "$2" ;;
    *) sed -i "$1" "$2" ;;
  esac
}

RELVERSION=$1
DEVVERSION=$2

pendingChangesInIndex=`git status -s --untracked-files=no | grep "^[^?]" | wc -l | tr -d '[[:space:]]'`
if [ "$pendingChangesInIndex" != "0" ]
then
  # Stashing current changes
  git stash
fi

sedi "s/\"restx.version\"[[:space:]]*:[[:space:]]*\"[0-9\\.a-zA-Z\\-]*\",/\"restx.version\": \"$RELVERSION\",/g" restx.build.properties.json
restx build generate ivy
git add . && git commit -m "preparing $RELVERSION"

mvn "-DreleaseVersion=$RELVERSION" "-DdevelopmentVersion=$DEVVERSION" -B release:prepare release:perform

sedi "s/\"restx.version\"[[:space:]]*:[[:space:]]*\"[0-9\\.a-zA-Z\\-]*\",/\"restx.version\": \"$DEVVERSION\",/g" restx.build.properties.json
sedi "s/<restx.version>[0-9\\.a-zA-Z\\-]*</<restx.version>$DEVVERSION</g" pom.xml
restx build generate ivy

git add . && git commit -m "re-snapshoted restx.version property"

if [ "$pendingChangesInIndex" != "0" ]
then
  # Stash pop should be made in the end only
  git stash pop
fi

echo ""
echo "  RESTX Release successful !"
