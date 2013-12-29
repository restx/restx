#!/bin/sh

# This script is used to build the front app and put it in the dist directory.
# To make this scripts usable by maven, it must remain very simple, a mere list of commands with no bash expansion

npm install
bower install
grunt build
