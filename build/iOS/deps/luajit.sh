#!/bin/bash -e

. ../sdk.sh
LUAJIT_VERSION=2.1.0-beta2

if [ ! -d luajit-src ]; then
	wget http://luajit.org/download/LuaJIT-$LUAJIT_VERSION.tar.gz
	tar -xzvf LuaJIT-$LUAJIT_VERSION.tar.gz
	mv LuaJIT-$LUAJIT_VERSION luajit-src
	rm LuaJIT-$LUAJIT_VERSION.tar.gz
fi

cd luajit-src

# 32-bit
make \
  HOST_CC="clang -m32 -arch i386" CROSS="$(dirname $IOS_CC)/" \
  TARGET_FLAGS="${IOS_FLAGS/-arch arm64/}" TARGET_SYS=iOS \
  -j$(sysctl -n hw.ncpu)
mv src/libluajit.a tmp32.a
make clean
# 64-bit
make \
  HOST_CC=clang CROSS="$(dirname $IOS_CC)/" \
  TARGET_FLAGS="${IOS_FLAGS/-arch armv7/}" TARGET_SYS=iOS \
  -j$(sysctl -n hw.ncpu)
mv src/libluajit.a tmp64.a
make clean
# repack into one .a
lipo tmp32.a tmp64.a -create -output libluajit.a
rm tmp32.a tmp64.a

mkdir -p ../luajit/{lib,include}
cp -v src/*.h ../luajit/include
cp -v libluajit.a ../luajit/lib

echo "LuaJIT build successful"
