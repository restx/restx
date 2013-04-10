#! /usr/bin/env sh

PRG="$0"
while [ -h "$PRG" ] ; do
  PRG=`readlink "$PRG"`
done

dir=`dirname $PRG`

if [ -z "$JAVA_HOME" ]; then
    JAVA="java"
else
    JAVA="$JAVA_HOME/bin/java"
fi

"$JAVA" -Drestx.shell.home="$dir" -cp "$dir/lib/*:$dir/plugins/*:." restx.shell.RestxShell "$@"
