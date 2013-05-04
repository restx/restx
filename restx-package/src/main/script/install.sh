#!/bin/sh

# wget 
mkdir ~/.restx
tar xzf restx.tgz -C ~/.restx/
ln -s ~/.restx/restx ~/bin/
rm -f restx.tgz
