#!/bin/bash -e

cd deps

./irrlicht.sh
./libogg.sh
./libvorbis.sh # depends on libogg
./leveldb.sh
./freetype.sh
./luajit.sh
./intl.sh

echo
echo "All libraries were built!"
