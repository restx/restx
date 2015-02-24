REM This script is used to build the front app and put it in the dist directory.

npm install
bower install
grunt test
grunt build
