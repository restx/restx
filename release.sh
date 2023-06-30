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

RELVERSION=$1
DEVVERSION=$2

pendingChangesInIndex=`git status -s --untracked-files=no | grep "^[^?]" | wc -l | tr -d '[[:space:]]'`
if [ "$pendingChangesInIndex" != "0" ]
then
  # Stashing current changes
  git stash
fi

./setVersion.sh $RELVERSION
git add . && git commit -m "preparing $RELVERSION"

./mvnw "-DreleaseVersion=$RELVERSION" "-DdevelopmentVersion=$DEVVERSION" -B -Darguments=-DskipTests release:prepare release:perform

./setVersion.sh $DEVVERSION
git add . && git commit -m "re-snapshoted restx.version property"

if [ "$pendingChangesInIndex" != "0" ]
then
  # Stash pop should be made in the end only
  git stash pop
fi

echo ""
echo "  RESTX Release successful !"
