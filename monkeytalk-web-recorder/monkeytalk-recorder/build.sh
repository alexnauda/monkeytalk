#!/bin/bash
# build.sh -- builds JAR and XPI files for mozilla extensions
#   by Nickolay Ponomarev <asqueella@gmail.com>
#   (original version based on Nathan Yergler's build script)
# Most recent version is at <http://kb.mozillazine.org/Bash_build_script>

# This script assumes the following directory structure:
# ./
#   chrome.manifest (optional - for newer extensions)
#   install.rdf
#   (other files listed in $ROOT_FILES)
#
#   content/    |
#   locale/     |} these can be named arbitrary and listed in $CHROME_PROVIDERS
#   skin/       |
#
#   defaults/   |
#   components/ |} these must be listed in $ROOT_DIRS in order to be packaged
#   ...         |
#
# It uses a temporary directory ./build when building; don't use that!
# Script's output is:
# ./$APP_NAME.xpi
# ./$APP_NAME.jar  (only if $KEEP_JAR=1)
# ./files -- the list of packaged files
#
# Note: It modifies chrome.manifest when packaging so that it points to 
#       chrome/$APP_NAME.jar!/*

#
# default configuration file is ./config_build.sh, unless another file is 
# specified in command-line. Available config variables:
APP_NAME=          # short-name, jar and xpi files name. Must be lowercase with no spaces
CHROME_PROVIDERS=  # which chrome providers we have (space-separated list)
CLEAN_UP=          # delete the jar / "files" when done?       (1/0)
ROOT_FILES=        # put these files in root of xpi (space separated list of leaf filenames)
ROOT_DIRS=         # ...and these directories       (space separated list)
BEFORE_BUILD=      # run this before building       (bash command)
AFTER_BUILD=       # ...and this after the build    (bash command)

if [ -z $1 ]; then
  . ./config_build.sh
else
  . $1
fi

if [ -z $APP_NAME ]; then
  echo "You need to create build config file first!"
  echo "Read comments at the beginning of this script for more info."
  exit;
fi

if [ ! -f install.rdf ]; then
  echo "You MUST run the outer build.sh first!"
  echo "This build.sh (aka the inner build.sh) cannot be run directly!"
  exit;
fi

ROOT_DIR=`pwd`
TMP_DIR=build

# remove any left-over files from previous build
[ -f "$APP_NAME.jar" ] && rm "$APP_NAME.jar"
[ -f "$APP_NAME.xpi" ] && rm "$APP_NAME.xpi"
[ -f files ] && rm files
[ -d "$TMP_DIR" ] && rm -rf "$TMP_DIR"

$BEFORE_BUILD

mkdir -p "$TMP_DIR/chrome"

# generate the JAR file, excluding CVS, SVN, and temporary files
JAR_FILE=$TMP_DIR/chrome/$APP_NAME.jar
echo "Generating $JAR_FILE..."
for CHROME_SUBDIR in $CHROME_PROVIDERS; do
  find chrome/$CHROME_SUBDIR \( -path '*CVS*' -o -path '*.svn*' \) -prune -o -type f -print | grep -v \~ >> files
done

zip -0 -r $JAR_FILE -@ < files

# prepare components and defaults
echo "Copying various files to $TMP_DIR folder..."
for DIR in $ROOT_DIRS; do
  mkdir $TMP_DIR/$DIR
  FILES="`find $DIR \( -path '*CVS*' -o -path '*.svn*' \) -prune -o -type f -print | grep -v \~`"
  echo $FILES >> files
  cp $FILES $TMP_DIR
done

# Copy other files to the root of future XPI.
for ROOT_FILE in $ROOT_FILES install.rdf chrome.manifest; do
  cp $ROOT_FILE $TMP_DIR
  if [ -f $ROOT_FILE ]; then
    echo $ROOT_FILE >> files
  fi
done

cd "$TMP_DIR"


#JUSTIN - unzip everything
cd chrome
unzip "$APP_NAME.jar"
rm "$APP_NAME.jar"
cd ..

# generate the XPI file
echo "Generating $APP_NAME.xpi..."
zip -r ../$APP_NAME.xpi *

cd "$ROOT_DIR"

echo "Cleanup..."
if [ $CLEAN_UP = 0 ]; then
  # save the jar file
  cp "$TMP_DIR/chrome/$APP_NAME.jar" .
else
  [ -f files ] && rm files
  [ -f readme.txt ] && rm readme.txt
  [ -f install.rdf ] && rm install.rdf
fi

# remove the working files
# [ -d "$TMP_DIR" ] && rm -rf "$TMP_DIR"
echo "Done!"

$AFTER_BUILD