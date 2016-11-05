#!/bin/bash -e

# Generates different AppIcon images with correct dimensions
# (brew package: imagemagick)
SIZES="76 120 152 167 180"
SRCFILE=../android/src/main/res/drawable-xhdpi/ic_launcher.png
DSTDIR=MultiCraft/MultiCraft/Assets.xcassets/AppIcon.appiconset

for sz in $SIZES; do
	echo "Creating ${sz}x${sz} icon"
	convert -resize ${sz}x${sz} $SRCFILE $DSTDIR/AppIcon-${sz}.png
done
