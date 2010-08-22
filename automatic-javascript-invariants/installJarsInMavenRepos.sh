#!/bin/sh
# This script adds the JARS that do not exist in remote Maven repositories to the local repository
# version and the path to the files are configurable
# author: engin, mesbah
LIBDIR=lib
###versions
RHINOVER=20100427
DAIKONVER=custom-20100427

#files
RHINO=rhino-$RHINOVER.jar
DAIKON=daikon-$DAIKONVER.jar

mvn install:install-file -DgroupId=mozilla -DartifactId=rhino -Dversion=$RHINOVER -Dpackaging=jar -Dfile=$LIBDIR/$RHINO -DgeneratePom=true
mvn install:install-file -DgroupId=mit -DartifactId=daikon -Dversion=$DAIKONVER -Dpackaging=jar -Dfile=$LIBDIR/$DAIKON -DgeneratePom=true
