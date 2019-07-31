#!/bin/bash -e

if [ ! -d gradle ]; then
	echo "Run this in build/android"
	exit 1
fi

FOLDER=$(pwd)/app/src/main/assets
DEST=$(mktemp -d)

echo
echo "*** Starting build MultiCraft for Android... ***"

echo
echo "=> Getting precompiled dependencies:"
if [ ! -d native/deps ]
then
  echo
  git clone --depth 1 https://github.com/MultiCraft/deps native/deps
  echo
  echo "* Done!"
else
  echo
  echo "Already available, skipping..."
fi

echo
echo "=> Creating Assets:"

for dir in builtin textures; do
	cp -r ../../$dir $DEST/$dir
done

mkdir -p $DEST/fonts
cp ../../fonts/Retron2000.ttf $DEST/fonts/ # no PNG fonts because freetype

echo
echo "* Converting locale files:"
pushd ../../po
for lang in *; do
	[ ${#lang} -ne 2 ] && continue
	mopath=$DEST/locale/$lang/LC_MESSAGES
	mkdir -p $mopath
	pushd $lang
	for fn in *.po; do
		msgfmt -o $mopath/${fn/.po/.mo} $fn
	done
	popd
done
popd

# remove broken languages
find $DEST -type d -name 'ja' -print0 | xargs -0 -- rm -r
find $DEST -type d -name 'ko' -print0 | xargs -0 -- rm -r
find $DEST -type d -name 'he' -print0 | xargs -0 -- rm -r

mkdir -p $FOLDER

echo
echo "** Creating Files.zip"
ZIPDEST=$FOLDER/Files.zip
rm -f $ZIPDEST
cd $DEST; zip -0qr $ZIPDEST -- *
cd $FOLDER; rm -rf $DEST

###########

cd ../../../../..;

echo "*** Creating games.zip"
ZIPDEST=$FOLDER/games.zip
rm -f $ZIPDEST
zip -0qr $ZIPDEST -- games

echo "**** Creating worlds.zip"
ZIPDEST=$FOLDER/worlds.zip
rm -f $ZIPDEST
zip -0qr $ZIPDEST -- worlds

echo
echo "*** All done! You can continue in Android Studio! ***"
