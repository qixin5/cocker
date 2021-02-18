#! /bin/csh -f

set COCKER = /research/people/spr/cocker

set CP = $COCKER/java:/pro/ivy/java
set FIRJAR = $COCKER/firewall.jar
set FIRFLAGS = "-source 1.8 -target 1.8 -bootclasspath /pro/java/linux/jdk1.8.0/jre/lib/rt.jar"
set HOST = conifer2
set DIR = /vol/cocker

ant



javac -d $COCKER/java $FIRFLAGS $COCKER/server/src/ServerFirewall.java
taigabuildimpljar -cp $CP -j $FIRJAR -x edu.brown.cs.cocker.server.ServerFirewall

scp $FIRJAR ${HOST}:$DIR


