#!/bin/sh

PLUGINS="core-shell
specs-shell
build-shell"

# shell script to prepare a local install of restx shell
# you need to have built restx-package and the plugins first

rm -rf target/restx
mkdir target/restx
cd target/restx
tar xzvf ../assembly/out/restx-*-SNAPSHOT.tar.gz

cd -

for plugin in $PLUGINS
do
  cd ../restx-$plugin
  mvn -o dependency:copy-dependencies
  cp target/dependency/* ../restx-package/target/restx/plugins/
  cp `ls -1 target/*.jar | grep -v javadoc | grep -v sources`  ../restx-package/target/restx/plugins/
  cd -
done

echo "local restx shell ready in target/restx"
echo "you can run it with:"
echo "  target/restx/restx"
