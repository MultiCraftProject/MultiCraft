#!/bin/bash -e

. ../sdk.sh

if [ ! -d intl-src ]; then
	wget https://github.com/j-jorge/libintl-lite/archive/master.tar.gz
	tar -xzvf master.tar.gz
	mv libintl-lite-master intl-src
	rm master.tar.gz
fi

cd intl-src

cd internal
$IOS_CC $IOS_FLAGS -O3 -Wall -c libintl.cpp -o libintl.o
lipo libintl.o -create -output ../libintl.a
cd ..

mkdir -p ../intl/include
cp -v libintl.a ../intl
cp -v libintl.h ../intl/include

echo "intl build successful"
