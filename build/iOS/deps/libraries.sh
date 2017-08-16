#!/bin/bash -e

./irrlicht.sh
./libogg.sh
./libvorbis.sh
./leveldb.sh
./freetype.sh
./luajit.sh
./intl.sh
./libcurl.sh

echo
echo "All libraries were built!"
