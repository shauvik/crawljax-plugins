#!/bin/sh
# This script adds the JARS that do not exist in remote Maven repositories to the local repository
# version and the path to the files are configurable
# author: stefan based on work of ali
LIBDIR=lib
###versions
JAVAPLOTVER=0.4.0
CLASSMEXERVER=0.03

#files
JAVAPLOT=javaplot-$JAVAPLOTVER.jar
CLASSMEXER=classmexer-$CLASSMEXERVER.jar

mvn install:install-file -DgroupId=classmexer -DartifactId=classmexer -Dversion=$CLASSMEXERVER -Dpackaging=jar -Dfile=$LIBDIR/$CLASSMEXER -DgeneratePom=true
mvn install:install-file -DgroupId=javaplot -DartifactId=javaplot -Dversion=$JAVAPLOTVER -Dpackaging=jar -Dfile=$LIBDIR/$JAVAPLOT -DgeneratePom=true
