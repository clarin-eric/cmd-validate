#!/bin/sh
if [[ $# == 0 ]]
then
	echo "Usage: $0 [-s schema url] files..."
	exit 1;
fi

eval "mvn exec:java -Dexec.args=\"$@\""

