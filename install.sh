#!/bin/sh

# NOTE: this script is largely inspired by Meteor install script, 
# which can be found here: https://install.meteor.com

## NOTE sh NOT bash. This script should be POSIX sh only, since we don't
## know what shell the user has. Debian uses 'dash' for 'sh', for
## example.

# Is RESTX already installed (in /usr/local/bin (engine) or /usr/bin
# (pre-engine)? If so, just ask the user to run the upgrade command

if [ -x /usr/local/bin/restx ]; then
	  cat <<"EOF"

RESTX is already installed, to update it:
  (1) run the 'restx' command
  (2) run 'shell upgrade' command inside restx shell

If you want to reinstall it from scratch:
  (1) run 'rm `which restx`'
  (2) relaunch this script

You can also check the docs at

	http://restx.io/docs/

EOF

  exit 0
fi

if [ -x /usr/bin/restx ]; then
	  cat <<"EOF"

RESTX is already installed, to update it:
  (1) run the 'restx' command
  (2) run 'shell upgrade' command inside restx shell

If you want to reinstall it from scratch:
  (1) run 'rm `which restx`'
  (2) relaunch this script

You can also check the docs at

	http://restx.io/docs/

EOF
  exit 0
fi

PREFIX="/usr/local"

set -e
set -u

# Let's display everything on stderr.
exec 1>&2

UNAME=`uname`
if [ "$UNAME" != "Linux" -a "$UNAME" != "Darwin" ] ; then
    echo "Sorry, this OS is not supported yet."
    exit 1
fi

trap "echo Installation failed." EXIT

# If you already have an existing installation (but don't have restx in PATH), we do a clean
# install here:
[ -e "$HOME/.restx" ] && rm -rf "$HOME/.restx"

if [ "${1:-unset}" = "unset" ] ; then
	# get current version from web site
	VERSION=`curl -s http://restx.io/version | head -n 1`

	# get current tarball URL from web site
	TARBALL_URL=`curl -s http://restx.io/version | tail -n 1`

	# the url has no extension on web site, because it's used for updates where choosing between zip and tar.gz 
	# depends on platform
	TARBALL_URL="$TARBALL_URL.tar.gz"
else
	VERSION=$1
	
	if [ "${2:-unset}" = "unset" ] ; then	
			TARBALL_URL="http://repo1.maven.org/maven2/io/restx/restx-package/${VERSION}/restx-package-${VERSION}.tar.gz"
  else
			TARBALL_URL=$2
	fi
fi

INSTALL_TMPDIR="$HOME/.restx-install-tmp"
rm -rf "$INSTALL_TMPDIR"
mkdir "$INSTALL_TMPDIR"
echo "Downloading RESTX $VERSION distribution"
curl --progress-bar --fail "$TARBALL_URL" | tar -xzf - -C "$INSTALL_TMPDIR"
# bomb out if it didn't work, eg no net
test -x "${INSTALL_TMPDIR}/restx"
mv "${INSTALL_TMPDIR}" "$HOME/.restx"
# just double-checking :)
test -x "$HOME/.restx/restx"

echo
echo "RESTX $VERSION has been installed in your home directory (~/.restx)."

LAUNCHER="$HOME/.restx/restx"

if ln -s "$LAUNCHER" "$PREFIX/bin/restx" >/dev/null 2>&1; then
  echo "Writing a launcher script to $PREFIX/bin/restx for your convenience."
  cat <<"EOF"

RESTX is now properly installed, you can launch it using the restx command.

To get started, see the docs at:

  http://restx.io/docs/

EOF
elif type sudo >/dev/null 2>&1; then
  echo "Writing a launcher script to $PREFIX/bin/restx for your convenience."
  echo "This may prompt for your password."
  if sudo ln -s "$LAUNCHER" "$PREFIX/bin/restx"; then
    cat <<"EOF"

RESTX is now properly installed, you can launch it using the restx command.

To get started, see the docs at:

  http://restx.io/docs/

EOF
  else
    cat <<"EOF"

Couldn't write the launcher script. Please either:

  (1) Add ~/.restx to your path, or
  (2) Rerun this command to try again.

Then to get started, see the docs at

	http://restx.io/docs/

EOF
  fi
else
  cat <<"EOF"

Now you need to do one of the following:

  (1) Add ~/.restx to your path, or
  (2) Run this command as root:
        ln -s ~/.restx/restx /usr/bin/restx

Then to get started, see the docs at

	http://restx.io/docs/

EOF
fi

trap - EXIT