#!/bin/bash

# This script sets necessary properties to build rosapl
gradle_prefix="ORG_GRADLE_PROJECT_";
apl_jar="apl_release";
apl_lib="apl_lib";
target="/home/$USER/.bashrc"

jar="export $gradle_prefix$apl_jar=$1"
lib="export $gradle_prefix$apl_lib=${1}/lib"
if grep -q $gradle_prefix "$target"; then
   sed -i "/$gradle_prefix/d" $target 
fi

echo $jar >> $target
echo $lib >> $target


