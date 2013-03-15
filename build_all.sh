#!/bin/sh

SRC=src
OUTPUT=bin
LIB=lib/junit-4.10.jar:lib/mockito-all-1.9.5.jar

echo "Building java source code"

find $SRC -name *.java | xargs javac -cp $SRC:$LIB -d $OUTPUT
