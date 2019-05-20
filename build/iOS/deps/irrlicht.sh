#!/bin/bash -e

. sdk.sh

[ ! -d irrlicht-src ] && \
	svn co -r 5780 svn://svn.code.sf.net/p/irrlicht/code/branches/ogl-es irrlicht-src

cd irrlicht-src/

if [ ! -f .patched ]; then
	for p in touchcount unscaled dblfreefix viewcontroller proj roundingerror hideindicator; do
		patch -p0 <../../patches/irrlicht-$p.patch
	done
	touch .patched
fi

cd source/Irrlicht
xcodebuild build \
	-configuration Release \
	-project Irrlicht.xcodeproj \
	-scheme Irrlicht_iOS \
	-destination generic/platform=iOS
cd ../..

[ -d ../irrlicht ] && rm -r ../irrlicht
mkdir -p ../irrlicht
cp lib/iOS/libIrrlicht.a ../irrlicht/
cp -r include ../irrlicht/include

echo "Irrlicht build successful"
