#!/bin/sh

mvn --offline package
cp restx launch.sh  target/dist/
mkdir target/dist/plugins/

cp -R target/dist target/restx
cd target/restx
tar czvf ../restx.tgz .
cd -
