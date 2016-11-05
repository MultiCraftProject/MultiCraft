#!/bin/bash -e

. ../sdk.sh
CURL_VERSION=7.50.3

if [ ! -d libcurl-src ]; then
	wget https://curl.haxx.se/download/curl-$CURL_VERSION.tar.gz
	tar -xzvf curl-$CURL_VERSION.tar.gz
	mv curl-$CURL_VERSION libcurl-src
	rm curl-$CURL_VERSION.tar.gz
fi

cd libcurl-src

CC=$IOS_CC CFLAGS=$IOS_FLAGS \
./configure --host=arm-apple-darwin --prefix=/ \
	--disable-shared --enable-static \
	--with-darwinssl
make -j$(sysctl -n hw.ncpu)

mkdir -p ../libcurl
make DESTDIR=$PWD/../libcurl install

echo "libcurl build successful"
