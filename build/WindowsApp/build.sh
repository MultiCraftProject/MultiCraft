#!/bin/bash -e

bit=32
#bit=64

mingw=/tmp/mingw${bit}
if [ ! -d $mingw ]; then
	if [ $bit -eq 64 ]; then
		wget http://minetest.kitsunemimi.pw/mingw-w64-x86_64_7.1.1_ubuntu14.04.7z -O mingw.7z
	else
		wget http://minetest.kitsunemimi.pw/mingw-w64-i686_7.1.1_ubuntu14.04.7z -O mingw.7z
	fi
	7z x -y -o$mingw mingw.7z
	rm mingw.7z
fi
export PATH="$mingw/bin:$PATH"

./buildwin${bit}.sh /tmp/build${bit}
cd /tmp/build${bit}/MultiCraft/_build/_CPack_Packages/*/ZIP/
rm *.zip; dir=$(echo *)
if [ $bit -eq 64 ]; then
	base=$mingw/x86_64-w64-mingw32/bin
else
	base=$mingw/i686-w64-mingw32/bin
fi
cp -pv $base/lib{gcc,stdc++,winpthread}*.dll $dir/bin/

rm -f $OLDPWD/multicraft-windows.zip
zip -r $OLDPWD/multicraft-windows.zip $dir
echo "Done"
