#!/bin/sh
# This script adds the JARS that do not exist in remote Maven repositories to the local repository
# version and the path to the files are configurable
# author: engin, mesbah
LIBDIR=lib
###versions
CONCURRENTVER=1.3.4
WEBSCARABVER=20091209

#files
CONCURRENT=concurrent-$CONCURRENTVER.jar
WEBSCARAB=webscarab-$WEBSCARABVER.jar

mvn install:install-file -DgroupId=oswego -DartifactId=concurrent -Dversion=$CONCURRENTVER -Dpackaging=jar -Dfile=$LIBDIR/$CONCURRENT -DgeneratePom=true
mvn install:install-file -DgroupId=owasp -DartifactId=webscarab -Dversion=$WEBSCARABVER -Dpackaging=jar -Dfile=$LIBDIR/$WEBSCARAB -DgeneratePom=true
