################################################################################

OS := $(shell uname)

# compile with GPROF
# GPROF = 1

# build for build platform
APP_PLATFORM = android-19
COMPILER_VERSION = 4.9

ANDR_ROOT = $(shell pwd)
PROJ_ROOT = $(shell realpath $(ANDR_ROOT)/../..)
APP_ROOT = $(ANDR_ROOT)/src/main

VERSION_MAJOR := $(shell cat $(PROJ_ROOT)/CMakeLists.txt | \
	grep ^set\(VERSION_MAJOR\ | sed 's/)/ /' | cut -f2 -d' ')
VERSION_MINOR := $(shell cat $(PROJ_ROOT)/CMakeLists.txt | \
	grep ^set\(VERSION_MINOR\ | sed 's/)/ /' | cut -f2 -d' ')
VERSION_PATCH := $(shell cat $(PROJ_ROOT)/CMakeLists.txt | \
	grep ^set\(VERSION_PATCH\ | sed 's/)/ /' | cut -f2 -d' ')

################################################################################
# toolchain config for ARMv7
################################################################################

TARGET_HOST = arm-linux
TARGET_ABI = armeabi-v7a
TARGET_LIBDIR = armeabi-v7a
TARGET_TOOLCHAIN = arm-linux-androideabi
TARGET_CFLAGS_ADDON = -mfpu=vfpv3-d16 -march=armv7-a -Ofast -fdata-sections -ffunction-sections -fmodulo-sched -fmodulo-sched-allow-regmoves -fvisibility=hidden
TARGET_CXXFLAGS_ADDON = $(TARGET_CFLAGS_ADDON)
TARGET_LDFLAGS_ADDON = -Wl,--no-warn-mismatch,--gc-sections
CROSS_PREFIX = arm-linux-androideabi-

################################################################################
# toolchain config for x86
################################################################################

# TARGET_HOST = x86-linux
# TARGET_ABI = x86
# TARGET_LIBDIR = x86
# TARGET_TOOLCHAIN = x86
# TARGET_CFLAGS_ADDON = -Ofast -fdata-sections -ffunction-sections -fmodulo-sched -fmodulo-sched-allow-regmoves -fvisibility=hidden
# TARGET_CXXFLAGS_ADDON = $(TARGET_CFLAGS_ADDON)
# TARGET_LDFLAGS_ADDON = -Wl,--no-warn-mismatch,--gc-sections
# TARGET_ARCH = x86
# CROSS_PREFIX = i686-linux-android-

################################################################################

ASSETS_TIMESTAMP = $(ANDR_ROOT)/deps/assets_timestamp

LEVELDB_URL = https://github.com/google/leveldb/archive/v$(LEVELDB_VER).zip
LEVELDB_VER = 1.19
LEVELDB_DIR = $(ANDR_ROOT)/deps/leveldb
LEVELDB_LIB = $(LEVELDB_DIR)/out-static/libleveldb.a
LEVELDB_TIMESTAMP = $(LEVELDB_DIR)/timestamp
LEVELDB_TIMESTAMP_INT = $(ANDR_ROOT)/deps/leveldb_timestamp
#LEVELDB_URL_GIT = https://github.com/google/leveldb

#OPENAL_URL = https://github.com/MoNTE48/openal-soft/releases/download/$(OPENAL_VER)/$(OPENAL_VER).zip
#OPENAL_VER = openal-soft-1.17.2-dev
OPENAL_FOLDER = $(ANDR_ROOT)/deps/openal-soft
OPENAL_DIR = $(ANDR_ROOT)/deps/openal-soft/android
OPENAL_LIB = $(OPENAL_DIR)/libs/$(TARGET_ABI)/libopenal.so
OPENAL_TIMESTAMP = $(OPENAL_DIR)/timestamp
OPENAL_TIMESTAMP_INT = $(ANDR_ROOT)/deps/openal_timestamp
OPENAL_URL_GIT = https://github.com/MoNTE48/openal-soft

VORBIS_DIR = $(ANDR_ROOT)/deps/libvorbis-android
VORBIS_LIB = $(VORBIS_DIR)/obj/local/$(TARGET_ABI)/libvorbis.a
VORBIS_TIMESTAMP = $(VORBIS_DIR)/timestamp
VORBIS_TIMESTAMP_INT = $(ANDR_ROOT)/deps/vorbis_timestamp
VORBIS_URL_GIT = https://github.com/MoNTE48/libvorbis-android

IRRLICHT_COMMIT = 58fa0cf3419c2733159fc3c6253634bedada3bfe
IRRLICHT_DIR = $(ANDR_ROOT)/deps/irrlicht
IRRLICHT_LIB = $(IRRLICHT_DIR)/lib/Android/libIrrlicht.a
IRRLICHT_TIMESTAMP = $(IRRLICHT_DIR)/timestamp
IRRLICHT_TIMESTAMP_INT = $(ANDR_ROOT)/deps/irrlicht_timestamp
IRRLICHT_URL_HTTP = https://github.com/zaki/irrlicht/archive/$(IRRLICHT_COMMIT).zip

# not used now
OPENSSL_VERSION = 1.0.2h
OPENSSL_DIR = $(ANDR_ROOT)/deps/openssl
OPENSSL_LIB = $(OPENSSL_DIR)/libssl.so.1.0.0
OPENSSL_TIMESTAMP = $(OPENSSL_DIR)/timestamp
OPENSSL_TIMESTAMP_INT = $(ANDR_ROOT)/deps/openssl_timestamp
OPENSSL_URL = https://www.openssl.org/source/openssl-$(OPENSSL_VERSION).tar.gz

CURL_VERSION = 7.50.3
CURL_DIR = $(ANDR_ROOT)/deps/curl
CURL_LIB = $(CURL_DIR)/lib/.libs/libcurl.a
CURL_TIMESTAMP = $(CURL_DIR)/timestamp
CURL_TIMESTAMP_INT = $(ANDR_ROOT)/deps/curl_timestamp
CURL_URL_HTTP = http://dl.uxnr.de/mirror/curl/curl-${CURL_VERSION}.tar.bz2

# not used now
MPIR_DIR = $(ANDR_ROOT)/deps/mpir
MPIR_LIB = $(MPIR_DIR)/usr/local/lib/libmpir.a
MPIR_TIMESTAMP = $(MPIR_DIR)/timestamp
MPIR_TIMESTAMP_INT = $(ANDR_ROOT)/deps/mpir_timestamp
MPIR_URL_GIT = https://github.com/wbhart/mpir

FREETYPE_VERSION = 2.7
FREETYPE_DIR = $(ANDR_ROOT)/deps/freetype
FREETYPE_LIB = $(FREETYPE_DIR)/objs/.libs/libfreetype.a
FREETYPE_TIMESTAMP = $(FREETYPE_DIR)/timestamp
FREETYPE_TIMESTAMP_INT = $(ANDR_ROOT)/deps/freetype_timestamp
FREETYPE_URL_HTTP = http://download.savannah.gnu.org/releases/freetype/freetype-$(FREETYPE_VERSION).tar.bz2

ICONV_VERSION = 1.14
ICONV_DIR = $(ANDR_ROOT)/deps/libiconv
ICONV_LIB = $(ICONV_DIR)/lib/.libs/libiconv.a
ICONV_TIMESTAMP = $(ICONV_DIR)/timestamp
ICONV_TIMESTAMP_INT = $(ANDR_ROOT)/deps/iconv_timestamp
ICONV_URL_HTTP = https://ftp.gnu.org/pub/gnu/libiconv/libiconv-$(ICONV_VERSION).tar.gz

INTL_DIR = $(ANDR_ROOT)/deps/libintl/
INTL_LIB = $(INTL_DIR)/libintl.a
INTL_TIMESTAMP = $(INTL_DIR)timestamp
INTL_TIMESTAMP_INT = $(ANDR_ROOT)/deps/intl_timestamp
INTL_URL_HTTP = https://github.com/j-jorge/libintl-lite/archive/master.tar.gz

SQLITE3_VERSION = 3150000
SQLITE3_DIR = $(ANDR_ROOT)/deps/sqlite
SQLITE3_URL = https://www.sqlite.org/2016/sqlite-amalgamation-$(SQLITE3_VERSION).zip

LUAJIT_GIT_BRANCH = v2.1
LUAJIT_DIR = $(ANDR_ROOT)/deps/luajit
LUAJIT_LIB = $(LUAJIT_DIR)src/libluajit.a
LUAJIT_TIMESTAMP = $(LUAJIT_DIR)/timestamp
LUAJIT_TIMESTAMP_INT = $(ANDR_ROOT)/deps/luajit_timestamp
LUAJIT_URL_GIT = https://github.com/LuaJIT/LuaJIT.git

################################################################################

ANDROID_SDK = $(shell grep '^sdk\.dir' local.properties | sed 's/^.*=[[:space:]]*//')
ANDROID_NDK = $(shell grep '^ndk\.dir' local.properties | sed 's/^.*=[[:space:]]*//')
NDK_MODULE_PATH = $(ANDROID_NDK)/toolchains


.PHONY : debug release reconfig delconfig \
	leveldb_download clean_leveldb leveldb\
	irrlicht_download clean_irrlicht irrlicht \
	clean_assets assets sqlite3_download \
	freetype_download clean_freetype freetype \
	luajit_download clean_luajit luajit \
	apk clean_apk \
	clean_all clean prep_srcdir \
	install_debug install_release envpaths all \
	$(ASSETS_TIMESTAMP) $(LUAJIT_TIMESTAMP) \
	$(OPENAL_TIMESTAMP) $(VORBIS_TIMESTAMP) \
	$(IRRLICHT_TIMESTAMP) $(CURL_TIMESTAMP) \
	$(ANDR_ROOT)/jni/src/android_version.h \
	$(ANDR_ROOT)/jni/src/android_version_githash.h
#	$(OPENSSL_TIMESTAMP) \

debug : local.properties
	export NDEBUG=;                                                            \
	export BUILD_TYPE=debug;                                                   \
	$(MAKE) apk

all : debug release

release : local.properties
	@export NDEBUG=1;                                                          \
	export BUILD_TYPE=release;                                                 \
	$(MAKE) apk

reconfig: delconfig
	@$(MAKE) local.properties

delconfig:
	$(RM) local.properties

local.properties:
	@echo "Please specify path of ANDROID NDK";                                    \
	echo "e.g. $$HOME/Android/ndk-r11c/";                                          \
	read ANDROID_NDK ;                                                             \
	if [ ! -d $$ANDROID_NDK ] ; then                                               \
		echo "$$ANDROID_NDK is not a valid folder";                               \
		exit 1;                                                                   \
	fi;                                                                            \
	echo "ndk.dir = $$ANDROID_NDK" > local.properties;                             \
	echo "Please specify path of ANDROID SDK";                                     \
	echo "e.g. $$HOME/Android/sdk/";                                               \
	read SDKFLDR ;                                                                 \
	if [ ! -d $$SDKFLDR ] ; then                                                   \
		echo "$$SDKFLDR is not a valid folder";                                \
		exit 1;                                                                \
	fi;                                                                            \
	echo "sdk.dir = $$SDKFLDR" >> local.properties;


$(OPENAL_TIMESTAMP) : openal_download
	@LAST_MODIF=$$(find ${OPENAL_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${OPENAL_TIMESTAMP};                                             \
	fi

openal_download :
	@if [ ! -d ${OPENAL_DIR} ] ; then                                          \
		echo "openal sources missing, downloading...";                         \
		mkdir -p ${ANDR_ROOT}/deps;                                            \
		cd ${ANDR_ROOT}/deps ;                                                 \
		git clone ${OPENAL_URL_GIT} || exit 1;                                 \
#		wget ${OPENAL_URL} || exit 1;                                          \
#		unzip ${OPENAL_VER}.zip -d openal-soft || exit 1;                      \
#		rm ${OPENAL_VER}.zip || exit 1;                                        \
	fi

openal : $(OPENAL_LIB)

$(OPENAL_LIB): $(OPENAL_TIMESTAMP)
	+ @REFRESH=0;                                                          \
	if [ ! -e ${OPENAL_TIMESTAMP_INT} ] ; then                                 \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ${OPENAL_TIMESTAMP} -nt ${OPENAL_TIMESTAMP_INT} ] ; then              \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	echo "changed timestamp for openal detected building...";                  \
	cd ${OPENAL_DIR};                                                          \
	${ANDROID_NDK}/ndk-build NDEBUG=${NDEBUG}                                  \
		NDK_MODULE_PATH=${NDK_MODULE_PATH}                                     \
		APP_ABI=${TARGET_ABI} APP_PLATFORM=${APP_PLATFORM}                     \
		NDK_TOOLCHAIN_VERSION=${COMPILER_VERSION}                              \
		TARGET_CFLAGS+="${TARGET_CFLAGS_ADDON}"                                \
		TARGET_LDFLAGS+="${TARGET_LDFLAGS_ADDON}"                              \
		TARGET_CXXFLAGS+="${TARGET_CXXFLAGS_ADDON}" || exit 1;                 \
	touch ${OPENAL_TIMESTAMP};                                                 \
	touch ${OPENAL_TIMESTAMP_INT};                                             \
	else                                                                       \
		echo "nothing to be done for openal";                                  \
	fi

clean_openal :
	$(RM) -rf ${OPENAL_FOLDER}

$(VORBIS_TIMESTAMP) : vorbis_download
	@LAST_MODIF=$$(find ${VORBIS_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${VORBIS_TIMESTAMP};                                                \
	fi

vorbis_download :
	@if [ ! -d ${VORBIS_DIR} ] ; then                                          \
		echo "vorbis sources missing, downloading...";                         \
		mkdir -p ${ANDR_ROOT}/deps;                                            \
		cd ${ANDR_ROOT}/deps ;                                                 \
		git clone ${VORBIS_URL_GIT} || exit 1;                                 \
	fi

vorbis : $(VORBIS_LIB)

$(VORBIS_LIB): $(VORBIS_TIMESTAMP)
	+ @REFRESH=0;                                                              \
	if [ ! -e ${VORBIS_TIMESTAMP_INT} ] ; then                                 \
		echo "${VORBIS_TIMESTAMP_INT} doesn't exist";                          \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ${VORBIS_TIMESTAMP} -nt ${VORBIS_TIMESTAMP_INT} ] ; then              \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	echo "changed timestamp for vorbis detected building...";                  \
	cd ${VORBIS_DIR};                                                          \
	${ANDROID_NDK}/ndk-build NDEBUG=${NDEBUG}                                  \
		NDK_MODULE_PATH=${NDK_MODULE_PATH}                                     \
		APP_ABI=${TARGET_ABI} APP_PLATFORM=${APP_PLATFORM}                     \
		NDK_TOOLCHAIN_VERSION=${COMPILER_VERSION}     \
		TARGET_CFLAGS+="${TARGET_CFLAGS_ADDON}"                                \
		TARGET_LDFLAGS+="${TARGET_LDFLAGS_ADDON}"                              \
		TARGET_CXXFLAGS+="${TARGET_CXXFLAGS_ADDON}" || exit 1;                 \
	touch ${VORBIS_TIMESTAMP};                                                 \
	touch ${VORBIS_TIMESTAMP_INT};                                             \
	else                                                                       \
		echo "nothing to be done for libvorbis";                               \
	fi

clean_vorbis :
	$(RM) -rf ${VORBIS_DIR}

$(OPENSSL_TIMESTAMP) : openssl_download
	@LAST_MODIF=$$(find ${OPENSSL_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${OPENSSL_TIMESTAMP};                                            \
	fi

openssl_download :
	@if [ ! -d ${OPENSSL_DIR} ] ; then                                         \
		echo "openssl sources missing, downloading...";                        \
		mkdir -p ${ANDR_ROOT}/deps;                                             \
		cd ${ANDR_ROOT}/deps;                                                  \
		wget ${OPENSSL_URL} || exit 1;                                         \
		tar -xzf openssl-$(OPENSSL_VERSION).tar.gz;                            \
		$(RM) -rf $(ANDR_ROOT)/deps/openssl-$(OPENSSL_VERSION).tar.gz;         \
		mv openssl-$(OPENSSL_VERSION) openssl;                                 \
	fi

openssl : $(OPENSSL_LIB)

$(OPENSSL_LIB): $(OPENSSL_TIMESTAMP) $(MPIR_LIB)
	@REFRESH=0;                                                                \
	if [ ! -e ${OPENSSL_TIMESTAMP_INT} ] ; then                                \
		echo "${OPENSSL_TIMESTAMP_INT} doesn't exist";                         \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ${OPENSSL_TIMESTAMP} -nt ${OPENSSL_TIMESTAMP_INT} ] ; then            \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	echo "changed timestamp for openssl detected building...";                 \
	cd ${OPENSSL_DIR};                                                         \
	export TOOLCHAIN=/tmp/ndk-${TARGET_HOST}-openssl;                          \
	${ANDROID_NDK}/build/tools/make-standalone-toolchain.sh                    \
		--toolchain=${TARGET_TOOLCHAIN}-${COMPILER_VERSION}                        \
		--install-dir=$${TOOLCHAIN} --platform=${APP_PLATFORM};                    \
	export PATH="$${TOOLCHAIN}/bin:$${PATH}";                                  \
	CC=${CROSS_PREFIX}gcc ./Configure no-ssl2 no-ssl3 no-hw enable-gmp         \
	-DL_ENDIAN -I${MPIR_DIR} -L${MPIR_DIR}/usr/lib android-${TARGET_ARCH};     \
	CC=${CROSS_PREFIX}gcc ANDROID_DEV=/tmp/ndk-${TARGET_HOST} make depend;     \
	CC=${CROSS_PREFIX}gcc ANDROID_DEV=/tmp/ndk-${TARGET_HOST} make build_libs; \
	touch ${OPENSSL_TIMESTAMP};                                                \
	touch ${OPENSSL_TIMESTAMP_INT};                                            \
	$(RM) -rf $${TOOLCHAIN};                                                   \
	else                                                                       \
		echo "nothing to be done for openssl";                        \
	fi

clean_openssl :
	$(RM) -rf deps/openssl-$(OPENSSL_VERSION).tar.gz;                          \
	$(RM) -rf deps/openssl

$(LEVELDB_TIMESTAMP) : leveldb_download
	@LAST_MODIF=$$(find ${LEVELDB_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${LEVELDB_TIMESTAMP};                                            \
	fi

leveldb_download :
	@if [ ! -d ${LEVELDB_DIR} ] ; then                                         \
		echo "leveldb sources missing, downloading...";                        \
		mkdir -p ${ANDR_ROOT}/deps;                                            \
		cd ${ANDR_ROOT}/deps ;                                                 \
#		git clone ${LEVELDB_URL_GIT} || exit 1;                                \
		wget ${LEVELDB_URL} || exit 1;                                          \
		unzip v${LEVELDB_VER}.zip  || exit 1;                      \
		rm v${LEVELDB_VER}.zip || exit 1;                                        \
		mv leveldb-${LEVELDB_VER} leveldb || exit 1;                                        \
	fi

leveldb : $(LEVELDB_LIB)

$(LEVELDB_LIB): $(LEVELDB_TIMESTAMP)
	@REFRESH=0;                                                                \
	if [ ! -e ${LEVELDB_TIMESTAMP_INT} ] ; then                                \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ${LEVELDB_TIMESTAMP} -nt ${LEVELDB_TIMESTAMP_INT} ] ; then            \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	echo "changed timestamp for leveldb detected building...";                 \
	cd deps/leveldb;                                                           \
	export CROSS_PREFIX=${CROSS_PREFIX};                                       \
	export TOOLCHAIN=/tmp/ndk-${TARGET_HOST}-leveldb;                          \
	${ANDROID_NDK}/build/tools/make-standalone-toolchain.sh                    \
	--toolchain=${TARGET_TOOLCHAIN}-${COMPILER_VERSION}                        \
	--platform=${APP_PLATFORM} --install-dir=$${TOOLCHAIN};                    \
	export PATH="$${TOOLCHAIN}/bin:$${PATH}";                                  \
	export CC=${CROSS_PREFIX}gcc;                                              \
	export CXX=${CROSS_PREFIX}g++;                                             \
	export TARGET_OS=OS_ANDROID_CROSSCOMPILE;                                  \
	export CPPFLAGS="$${CPPFLAGS} ${TARGET_CXXFLAGS_ADDON}";                   \
	export CFLAGS="$${CFLAGS} ${TARGET_CFLAGS_ADDON}";                         \
	export LDFLAGS="$${LDFLAGS} ${TARGET_LDFLAGS_ADDON}";      \
	$(MAKE) || exit 1;                                                      \
	touch ${LEVELDB_TIMESTAMP};                                                \
	touch ${LEVELDB_TIMESTAMP_INT};                                            \
	$(RM) -rf $${TOOLCHAIN};                                                   \
	else                                                                       \
		echo "nothing to be done for leveldb";                                 \
	fi

clean_leveldb :
	$(RM) -rf deps/leveldb

$(FREETYPE_TIMESTAMP) : freetype_download
	@LAST_MODIF=$$(find ${FREETYPE_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${FREETYPE_TIMESTAMP};                                           \
	fi

freetype_download :
	@if [ ! -d ${FREETYPE_DIR} ] ; then                                        \
		echo "freetype sources missing, downloading...";                       \
		mkdir -p ${ANDR_ROOT}/deps;                                            \
		cd deps;                                                               \
		wget ${FREETYPE_URL_HTTP} || exit 1;                                   \
		tar -xjf freetype-${FREETYPE_VERSION}.tar.bz2 || exit 1;               \
		rm freetype-${FREETYPE_VERSION}.tar.bz2;                               \
		mv freetype-${FREETYPE_VERSION} freetype;                              \
	fi

freetype : $(FREETYPE_LIB)

$(FREETYPE_LIB) : $(FREETYPE_TIMESTAMP)
	+ @REFRESH=0;                                                                \
	if [ ! -e ${FREETYPE_TIMESTAMP_INT} ] ; then                               \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ! -e ${FREETYPE_LIB} ] ; then                                         \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ${FREETYPE_TIMESTAMP} -nt ${FREETYPE_TIMESTAMP_INT} ] ; then          \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	mkdir -p ${FREETYPE_DIR};                                                  \
	export PATH=$$PATH:${ANDROID_NDK};                                         \
	echo "changed timestamp for freetype detected building...";                \
	cd deps/freetype;                                                          \
	export CROSS_PREFIX=${CROSS_PREFIX};                                       \
	export TOOLCHAIN=/tmp/ndk-${TARGET_HOST}-freetype;                         \
	${ANDROID_NDK}/build/tools/make-standalone-toolchain.sh                    \
	--toolchain=${TARGET_TOOLCHAIN}-${COMPILER_VERSION}                        \
	--platform=${APP_PLATFORM} --install-dir=$${TOOLCHAIN};                    \
	export PATH="$${TOOLCHAIN}/bin:$${PATH}";                                  \
	export CXX=${CROSS_PREFIX}g++;                                             \
	export TARGET_OS=OS_ANDROID_CROSSCOMPILE;                                  \
	export CPPFLAGS="$${CPPFLAGS} ${TARGET_CXXFLAGS_ADDON}";                   \
	export CFLAGS="$${CFLAGS} ${TARGET_CFLAGS_ADDON}";                         \
	export LDFLAGS="$${LDFLAGS} ${TARGET_LDFLAGS_ADDON}";                      \
	CC=${CROSS_PREFIX}gcc ./configure  --enable-static --disable-shared --host=${TARGET_HOST} \
		--prefix=${TOOLCHAIN} --with-png=no --with-harfbuzz=no  || exit 1;     \
	CC=${CROSS_PREFIX}gcc ANDROID_DEV=/tmp/ndk-${TARGET_HOST}-freetype $(MAKE) || exit 1; \
	touch ${FREETYPE_TIMESTAMP};                                               \
	touch ${FREETYPE_TIMESTAMP_INT};                                           \
	$(RM) -rf $${TOOLCHAIN};                                                   \
	else                                                                       \
		echo "nothing to be done for freetype";                                \
	fi

clean_freetype :
	$(RM) -rf ${FREETYPE_DIR}

$(ICONV_TIMESTAMP) : iconv_download
	@LAST_MODIF=$$(find ${ICONV_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${ICONV_TIMESTAMP};                                              \
	fi

iconv_download :
	@if [ ! -d ${ICONV_DIR} ] ; then                                           \
		echo "iconv sources missing, downloading...";                      \
		mkdir -p ${ANDR_ROOT}/deps;                                        \
		cd ${ANDR_ROOT}/deps;                                              \
		wget ${ICONV_URL_HTTP} || exit 1;                                  \
		tar -xzf libiconv-${ICONV_VERSION}.tar.gz || exit 1;               \
		rm libiconv-${ICONV_VERSION}.tar.gz;                               \
		mv libiconv-${ICONV_VERSION} libiconv;                             \
		cd libiconv;                                                       \
		patch -p1 < ${ANDR_ROOT}/patches/libiconv_android.patch;           \
		patch -p1 < ${ANDR_ROOT}/patches/libiconv_stdio.patch;             \
	fi

iconv : $(ICONV_LIB)

$(ICONV_LIB) : $(ICONV_TIMESTAMP)
	@REFRESH=0;                                                                \
	if [ ! -e ${ICONV_TIMESTAMP_INT} ] ; then                                  \
		REFRESH=1;                                                         \
	fi;                                                                        \
	if [ ! -e ${ICONV_LIB} ] ; then                                            \
		REFRESH=1;                                                         \
	fi;                                                                        \
	if [ ${ICONV_TIMESTAMP} -nt ${ICONV_TIMESTAMP_INT} ] ; then                \
		REFRESH=1;                                                         \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	mkdir -p ${ICONV_DIR};                                                     \
	echo "changed timestamp for iconv detected building...";                   \
	cd ${ICONV_DIR};                                                           \
	export TOOLCHAIN=/tmp/ndk-${TARGET_HOST}-iconv;                            \
	${ANDROID_NDK}/build/tools/make-standalone-toolchain.sh                    \
		--toolchain=${TARGET_TOOLCHAIN}-${COMPILER_VERSION}                    \
		--platform=${APP_PLATFORM} --install-dir=$${TOOLCHAIN};                \
	export PATH="$${TOOLCHAIN}/bin:$${PATH}";                                  \
	export CC=${CROSS_PREFIX}gcc;                                              \
	export CXX=${CROSS_PREFIX}g++;                                             \
	export TARGET_OS=OS_ANDROID_CROSSCOMPILE;                                  \
	export CPPFLAGS="$${CPPFLAGS} ${TARGET_CXXFLAGS_ADDON}";                   \
	export CFLAGS="$${CFLAGS} ${TARGET_CFLAGS_ADDON}";                         \
	export LDFLAGS="$${LDFLAGS} ${TARGET_LDFLAGS_ADDON}";                      \
	./configure --enable-static --disable-shared --host=${TARGET_HOST} || exit 1; \
	sed -i 's/LIBICONV_VERSION_INFO) /LIBICONV_VERSION_INFO) -avoid-version /g' lib/Makefile; \
	grep "iconv_LDFLAGS" src/Makefile;                                         \
	$(MAKE) -s || exit 1;                                                      \
	touch ${ICONV_TIMESTAMP};                                                  \
	touch ${ICONV_TIMESTAMP_INT};                                              \
	rm -rf ${TOOLCHAIN};                                                       \
	else                                                                       \
		echo "nothing to be done for iconv";                               \
	fi
clean_iconv :
	$(RM) -rf ${ICONV_DIR}

$(INTL_TIMESTAMP) : intl_download
	@LAST_MODIF=$$(find ${INTL_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${INTL_TIMESTAMP};                                           \
	fi

intl_download :
	@if [ ! -d ${INTL_DIR} ] ; then                                        \
		echo "libintl sources missing, downloading...";                    \
		mkdir -p ${ANDR_ROOT}/deps;                                        \
		cd ${ANDR_ROOT}/deps;                                              \
		wget ${INTL_URL_HTTP} -O libintl.tar.gz || exit 1;                 \
		tar -xzf libintl.tar.gz || exit 1;                                 \
		rm libintl.tar.gz;                                                 \
		ln -s libintl-lite-master libintl;                                 \
	fi

intl : $(INTL_LIB)

$(INTL_LIB) : $(INTL_TIMESTAMP)
	@REFRESH=0;                                                                \
	if [ ! -e ${INTL_TIMESTAMP_INT} ] ; then                                   \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ! -e ${INTL_LIB} ] ; then                                             \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ${INTL_TIMESTAMP} -nt ${INTL_TIMESTAMP_INT} ] ; then                  \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	mkdir -p ${INTL_DIR};                                                      \
	echo "changed timestamp for intl detected building...";                    \
	cd ${INTL_DIR};                                                            \
	\
	export TOOLCHAIN=/tmp/ndk-${TARGET_HOST}-intl;                             \
	${ANDROID_NDK}/build/tools/make-standalone-toolchain.sh                    \
		--toolchain=${TARGET_TOOLCHAIN}-${COMPILER_VERSION}                    \
		--platform=${APP_PLATFORM} --install-dir=$${TOOLCHAIN};                \
	export PATH="$${TOOLCHAIN}/bin:$${PATH}";                                  \
	cd internal;                                                               \
	${CROSS_PREFIX}g++ -O3 -c libintl.cpp -o libintl.o || exit 1;              \
	${CROSS_PREFIX}ar rs ../libintl.a libintl.o || exit 1;                     \
	cd ..;                                                                     \
	touch ${INTL_TIMESTAMP};                                                   \
	touch ${INTL_TIMESTAMP_INT};                                               \
	rm -rf ${TOOLCHAIN};                                                       \
	else                                                                       \
		echo "nothing to be done for intl";                                    \
	fi

clean_intl :
	$(RM) -rf ${INTL_DIR}

#Note: Texturehack patch is required for gpu's not supporting color format
#      correctly. Known bad GPU:
#      -geforce on emulator
#      -Vivante Corporation GC1000 core (e.g. Galaxy Tab 3)

irrlicht_download :
	@if [ ! -d "deps/irrlicht" ] ; then                                        \
		echo "irrlicht sources missing, downloading...";                       \
		mkdir -p ${ANDR_ROOT}/deps;                                            \
		cd deps;                                                               \
		wget ${IRRLICHT_URL_HTTP} || exit 1;                                   \
		unzip ${IRRLICHT_COMMIT}.zip || exit 1;                                \
		rm ${IRRLICHT_COMMIT}.zip;                                             \
		mv irrlicht-${IRRLICHT_COMMIT} irrlicht;                               \
		cd irrlicht;                                                           \
		mkdir -p lib/Android;                                                  \
		patch -p1 < ${ANDR_ROOT}/patches/irrlicht-touchcount.patch   || exit 1;\
		patch -p1 < ${ANDR_ROOT}/patches/irrlicht-back_button.patch  || exit 1;\
		patch -p1 < ${ANDR_ROOT}/patches/irrlicht-texturehack.patch  || exit 1;\
		patch -p1 < ${ANDR_ROOT}/patches/irrlicht-optimization.patch || exit 1;\
		patch -p1 < ${ANDR_ROOT}/patches/irrlicht-CEGL.patch         || exit 1;\
	fi

$(IRRLICHT_TIMESTAMP) : irrlicht_download
	@LAST_MODIF=$$(find ${IRRLICHT_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${IRRLICHT_TIMESTAMP};                                           \
	fi

irrlicht : $(IRRLICHT_LIB)

$(IRRLICHT_LIB): $(IRRLICHT_TIMESTAMP) $(FREETYPE_LIB)
	+ @REFRESH=0;                                                                \
	if [ ! -e ${IRRLICHT_TIMESTAMP_INT} ] ; then                               \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ! -e ${IRRLICHT_LIB} ] ; then                                         \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ${IRRLICHT_TIMESTAMP} -nt ${IRRLICHT_TIMESTAMP_INT} ] ; then          \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	mkdir -p ${IRRLICHT_DIR};                                                  \
	echo "changed timestamp for irrlicht detected building...";                \
	cd deps/irrlicht/source/Irrlicht/Android;                                  \
	${ANDROID_NDK}/ndk-build NDEBUG=${NDEBUG}                                  \
		NDK_MODULE_PATH=${NDK_MODULE_PATH}                                     \
		NDK_TOOLCHAIN_VERSION=${COMPILER_VERSION}     \
		APP_ABI=${TARGET_ABI} APP_PLATFORM=${APP_PLATFORM} || exit 1;          \
	touch ${IRRLICHT_TIMESTAMP};                                               \
	touch ${IRRLICHT_TIMESTAMP_INT};                                           \
	else                                                                       \
		echo "nothing to be done for irrlicht";                                \
	fi

clean_irrlicht :
	$(RM) -rf deps/irrlicht

$(CURL_TIMESTAMP) : curl_download
	@LAST_MODIF=$$(find ${CURL_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${CURL_TIMESTAMP};                                               \
	fi

curl_download :
	@if [ ! -d "deps/curl" ] ; then                                            \
		echo "curl sources missing, downloading...";                           \
		mkdir -p ${ANDR_ROOT}/deps;                                            \
		cd deps;                                                               \
		wget ${CURL_URL_HTTP} || exit 1;                                       \
		tar -xjf curl-${CURL_VERSION}.tar.bz2 || exit 1;                       \
		rm curl-${CURL_VERSION}.tar.bz2;                                       \
		mv curl-${CURL_VERSION} curl;                                          \
	fi

curl : $(CURL_LIB)

#$(CURL_LIB): $(CURL_TIMESTAMP) $(OPENSSL_LIB)
$(CURL_LIB): $(CURL_TIMESTAMP)
	@REFRESH=0;                                                                \
	if [ ! -e ${CURL_TIMESTAMP_INT} ] ; then                                   \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ! -e ${CURL_LIB} ] ; then                                             \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ${CURL_TIMESTAMP} -nt ${CURL_TIMESTAMP_INT} ] ; then                  \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	mkdir -p ${CURL_DIR};                                                      \
	echo "changed timestamp for curl detected building...";                    \
	cd deps/curl;                                                              \
	export CROSS_PREFIX=${CROSS_PREFIX};                                       \
	export TOOLCHAIN=/tmp/ndk-${TARGET_HOST}-curl;                             \
	${ANDROID_NDK}/build/tools/make-standalone-toolchain.sh                    \
		--toolchain=${TARGET_TOOLCHAIN}-${COMPILER_VERSION}                        \
		--platform=${APP_PLATFORM} --install-dir=$${TOOLCHAIN};                    \
	export PATH="$${TOOLCHAIN}/bin:$${PATH}";                                  \
	export CC=${CROSS_PREFIX}gcc;                                              \
	export CXX=${CROSS_PREFIX}g++;                                             \
	export TARGET_OS=OS_ANDROID_CROSSCOMPILE;                                  \
	export CPPFLAGS="$${CPPFLAGS} ${TARGET_CXXFLAGS_ADDON}";                   \
	export CFLAGS="$${CFLAGS} ${TARGET_CFLAGS_ADDON}";                         \
	export LDFLAGS="$${LDFLAGS} ${TARGET_LDFLAGS_ADDON}";                      \
#	export CPPFLAGS="$${CPPFLAGS} -I${OPENSSL_DIR}/include                     \
#	export LDFLAGS="$${LDFLAGS} -L${OPENSSL_DIR} ${TARGET_LDFLAGS_ADDON}";     \
#	./configure --host=${TARGET_HOST} --enable-static --disable-shared --with-ssl; \
	./configure --host=${TARGET_HOST} --enable-static --disable-shared;        \
	$(MAKE) -s || exit 1;                                                      \
	touch ${CURL_TIMESTAMP};                                                   \
	touch ${CURL_TIMESTAMP_INT};                                               \
	$(RM) -rf $${TOOLCHAIN};                                                   \
	else                                                                       \
		echo "nothing to be done for curl";                                    \
	fi

clean_curl :
	$(RM) -R ${CURL_DIR}

$(MPIR_TIMESTAMP) : mpir_download
	@LAST_MODIF=$$(find ${MPIR_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${MPIR_TIMESTAMP};                                            \
	fi

mpir_download :
	@if [ ! -d "${MPIR_DIR}" ] ; then                                          \
		echo "mpir sources missing, downloading...";                           \
		mkdir -p ${ANDR_ROOT}/deps;                                            \
		cd deps;                                                               \
		git clone ${MPIR_URL_GIT} || exit 1;                                   \
	fi

mpir : $(MPIR_LIB)

$(MPIR_LIB): $(MPIR_TIMESTAMP)
	@REFRESH=0;                                                                \
	if [ ! -e ${MPIR_TIMESTAMP_INT} ] ; then                                   \
		REFRESH=1;                                                         \
	fi;                                                                        \
	if [ ! -e ${MPIR_LIB} ] ; then                                             \
		REFRESH=1;                                                         \
	fi;                                                                        \
	if [ ${MPIR_TIMESTAMP} -nt ${MPIR_TIMESTAMP_INT} ] ; then                  \
		REFRESH=1;                                                         \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	mkdir -p ${MPIR_DIR};                                                      \
	echo "changed timestamp for mpir detected building...";                    \
	cd deps/mpir;                                                              \
	export CROSS_PREFIX=${CROSS_PREFIX};                                       \
	export TOOLCHAIN=/tmp/ndk-${TARGET_HOST}-mpir;                             \
	${ANDROID_NDK}/build/tools/make-standalone-toolchain.sh                    \
		--toolchain=${TARGET_TOOLCHAIN}-${COMPILER_VERSION}                        \
		--platform=${APP_PLATFORM} --install-dir=$${TOOLCHAIN};                    \
	export PATH="$${TOOLCHAIN}/bin:$${PATH}";                                  \
	export CC=${CROSS_PREFIX}gcc;                                              \
	export CXX=${CROSS_PREFIX}g++;                                             \
	./configure --enable-static --disable-shared --host=${TARGET_HOST};        \
	$(MAKE) install DESTDIR=/${MPIR_DIR} || exit 1;                            \
	touch ${MPIR_TIMESTAMP};                                                   \
	touch ${MPIR_TIMESTAMP_INT};                                               \
	$(RM) -rf $${TOOLCHAIN};                                                   \
	else                                                                       \
		echo "nothing to be done for mpir";                                   \
	fi

clean_mpir:
	$(RM) -R deps/mpir

sqlite3_download: deps/sqlite/sqlite3.c

deps/sqlite/sqlite3.c :
	mkdir -p ${ANDR_ROOT}/deps;                                                     \
	cd ${ANDR_ROOT}/deps;                                                           \
	rm -R sqlite;                                                              \
	wget ${SQLITE3_URL};                                                       \
	unzip sqlite-amalgamation-$(SQLITE3_VERSION).zip || exit 1;                \
	rm sqlite-amalgamation-$(SQLITE3_VERSION).zip;                             \
	mv sqlite-amalgamation-$(SQLITE3_VERSION) sqlite;                          \
fi

clean_sqlite3:
	$(RM) -R sqlite

$(ASSETS_TIMESTAMP) : $(IRRLICHT_LIB)
	@mkdir -p ${ANDR_ROOT}/deps;                                                \
	for DIRNAME in {builtin,client,doc,fonts,games,po,textures}; do        \
		LAST_MODIF=$$(find ${PROJ_ROOT}/$$DIRNAME -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
		if [ $$(basename $$LAST_MODIF) != "timestamp" ]; then               \
			touch ${PROJ_ROOT}/$$DIRNAME/timestamp;                    \
			touch ${ASSETS_TIMESTAMP};                                  \
			echo $$DIRNAME changed $$LAST_MODIF;                       \
		fi;                                                                 \
	done;                                                                       \
	LAST_MODIF=$$(find ${IRRLICHT_DIR}/media -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${IRRLICHT_DIR}/media/timestamp;                             \
		touch ${ASSETS_TIMESTAMP};                                         \
	fi;                                                                        \
	if [ ${PROJ_ROOT}/multicraft.conf.example -nt ${ASSETS_TIMESTAMP} ] ; then \
		echo "conf changed";                                               \
		touch ${ASSETS_TIMESTAMP};                                         \
	fi;                                                                        \
	if [ ${PROJ_ROOT}/README.txt -nt ${ASSETS_TIMESTAMP} ] ; then              \
		touch ${ASSETS_TIMESTAMP};                                         \
	fi;                                                                        \
	if [ ! -e $(ASSETS_TIMESTAMP) ] ; then                                     \
		touch $(ASSETS_TIMESTAMP);                                             \
	fi
	
$(LUAJIT_TIMESTAMP) : luajit_download
	@LAST_MODIF=$$(find ${LUAJIT_DIR} -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -f2- -d" "); \
	if [ $$(basename $$LAST_MODIF) != "timestamp" ] ; then                     \
		touch ${LUAJIT_TIMESTAMP};                                          \
	fi

luajit_download :
	@if [ ! -d ${LUAJIT_DIR} ] ; then                                          \
		echo "luajit sources missing, downloading...";                              \
		mkdir -p ${ANDR_ROOT}/deps;                                                 \
		cd ${ANDR_ROOT}/deps;                                                       \
		git clone --branch ${LUAJIT_GIT_BRANCH} ${LUAJIT_URL_GIT} luajit || exit 1; \
	fi

luajit : $(LUAJIT_LIB)

$(LUAJIT_LIB): $(LUAJIT_TIMESTAMP)
	@REFRESH=0;                                                                \
	if [ ! -e ${LUAJIT_TIMESTAMP_INT} ] ; then                                 \
		REFRESH=1;                                                         \
	fi;                                                                        \
	if [ ${LUAJIT_TIMESTAMP} -nt ${LUAJIT_TIMESTAMP_INT} ] ; then              \
		REFRESH=1;                                                         \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	mkdir -p ${LUAJIT_DIR};                                                    \
	export PATH=$$PATH:${ANDROID_NDK};                                         \
	echo "changed timestamp for luajit detected building...";                  \
	cd ${LUAJIT_DIR};                                                          \
	export CROSS_PREFIX=${CROSS_PREFIX};                                       \
	export TOOLCHAIN=/tmp/ndk-${TARGET_HOST}-luajit;                           \
	${ANDROID_NDK}/build/tools/make-standalone-toolchain.sh                    \
	--toolchain=${TARGET_TOOLCHAIN}-${COMPILER_VERSION}                        \
	--platform=${APP_PLATFORM} --install-dir=$${TOOLCHAIN};                    \
	export PATH="$${TOOLCHAIN}/bin:$${PATH}";                                  \
	export TARGET_FLAGS="${TARGET_CFLAGS_ADDON}";                              \
	export TARGET_LDFLAGS="${TARGET_LDFLAGS_ADDON}";                           \
	$(MAKE) CROSS=${CROSS_PREFIX} XCFLAGS="-DLUAJIT_DISABLE_FFI" HOST_CC="gcc -m32" BUILDMODE=static; \
	touch ${LUAJIT_TIMESTAMP};                                                 \
	touch ${LUAJIT_TIMESTAMP_INT};                                             \
	$(RM) -rf $${TOOLCHAIN};                                                   \
	else                                                                       \
		echo "nothing to be done for luajit";                                  \
	fi

clean_luajit:
	$(RM) -R deps/luajit

assets : $(ASSETS_TIMESTAMP)
	@REFRESH=0;                                                                \
	if [ ! -e ${ASSETS_TIMESTAMP}.old ] ; then                                 \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ${ASSETS_TIMESTAMP} -nt ${ASSETS_TIMESTAMP}.old ] ; then          \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ ! -e ${APP_ROOT}/assets ] ; then                                      \
		REFRESH=1;                                                             \
	fi;                                                                        \
	if [ $$REFRESH -ne 0 ] ; then                                              \
	echo "assets changed, refreshing...";                                      \
	$(MAKE) clean_assets;                                                      \
	mkdir -p ${APP_ROOT}/assets/MultiCraft;                                    \
	cp -r ${PROJ_ROOT}/builtin ${APP_ROOT}/assets/MultiCraft;                  \
	mkdir ${APP_ROOT}/assets/MultiCraft/fonts;                                 \
	cp -r ${PROJ_ROOT}/fonts/retrovillenc.ttf ${APP_ROOT}/assets/MultiCraft/fonts/; \
	cp -r ${PROJ_ROOT}/games ${APP_ROOT}/assets/MultiCraft;                    \
	mkdir -p ${APP_ROOT}/assets/MultiCraft/locale;                             \
	pushd ${PROJ_ROOT}/po;                                                     \
	for lang in *; do                                                          \
		[ $${#lang} -ne 2 ] && continue;                                       \
		MOPATH=${APP_ROOT}/assets/MultiCraft/locale/$$lang/LC_MESSAGES;        \
		mkdir -p $$MOPATH;                                                     \
		pushd $$lang;                                                          \
		for fn in *.po; do                                                     \
			newfn=$${fn/.po/.mo};                                              \
			msgfmt -o $$MOPATH/$$newfn $$fn;                                   \
		done;                                                                  \
		popd;                                                                  \
	done;                                                                      \
	popd;                                                                      \
	cp -r ${PROJ_ROOT}/textures ${APP_ROOT}/assets/MultiCraft;                 \
	cp -r ${PROJ_ROOT}/worlds ${APP_ROOT}/assets/MultiCraft;                   \
	cd ${APP_ROOT}/assets ||  exit 1;                                          \
	find . -name "timestamp" -exec rm {} \; ;                                  \
	find . -name "*.blend" -exec rm {} \; ;                                    \
	find . -name "*~" -exec rm {} \; ;                                         \
	find . -type d -path "*.git" -exec rm -rf {} \; ;                          \
	find . -type d -path "*.svn" -exec rm -rf {} \; ;                          \
	find . -type f -path "*.gitignore" -exec rm -rf {} \; ;                    \
	cd MultiCraft;                                                             \
	ls -R | grep ":$$" | sed -e 's/:$$//' -e 's/\.//' -e 's/^\///' > ../index.txt; \
	find -L . | cut -c 3- > ../filelist.txt;                                   \
	echo "Creating Files.zip";                                                 \
	zip -r ../Files.zip *;                                                     \
	cp ${ASSETS_TIMESTAMP} ${ASSETS_TIMESTAMP}.old;                            \
	else                                                                       \
		echo "nothing to be done for assets";                                  \
	fi

clean_assets :
	@$(RM) -r ${APP_ROOT}/assets

# $(MPIR_LIB)
apk: local.properties $(ICONV_LIB) $(IRRLICHT_LIB) $(CURL_LIB) $(LEVELDB_LIB) $(LUAJIT_LIB) \
	$(OPENAL_LIB) $(VORBIS_LIB) $(INTL_LIB) prep_srcdir $(ANDR_ROOT)/jni/src/android_version.h      \
	$(ANDR_ROOT)/jni/src/android_version_githash.h sqlite3_download assets
	+ @${ANDROID_NDK}/ndk-build NDK_MODULE_PATH=${NDK_MODULE_PATH}                  \
		GPROF=${GPROF} APP_ABI=${TARGET_ABI}                                   \
		APP_PLATFORM=${APP_PLATFORM}                                           \
		NDK_TOOLCHAIN_VERSION=${COMPILER_VERSION}                              \
		TARGET_LIBDIR=${TARGET_LIBDIR} &&                                      \
	if [ ! -e ${APP_ROOT}/jniLibs ]; then                                      \
		ln -s ${ANDR_ROOT}/libs ${APP_ROOT}/jniLibs || exit 1;                 \
	fi;                                                                        \
	export VERSION_STR="${VERSION_MAJOR}.${VERSION_MINOR}.${VERSION_PATCH}" && \
	export BUILD_TYPE_C=$$(echo "$${BUILD_TYPE}" | sed 's/./\U&/') &&          \
	gradle assemble$$BUILD_TYPE_C &&                                           \
	echo "APK stored at: build/outputs/apk/MultiCraft-$$BUILD_TYPE.apk" &&     \
	echo "You can install it with \`make install_$$BUILD_TYPE\`"

# These Intentionally doesn't depend on their respective build steps,
# because it takes a while to verify that everything's up-to-date.
install_debug:
	${ANDROID_SDK}/platform-tools/adb install -r build/outputs/apk/MultiCraft-debug.apk

install_release:
	${ANDROID_SDK}/platform-tools/adb install -r build/outputs/apk/MultiCraft-release.apk

prep_srcdir :
	@if [ ! -e ${ANDR_ROOT}/jni/src ]; then                                    \
		ln -s ${PROJ_ROOT}/src ${ANDR_ROOT}/jni/src;                           \
	fi

clean_apk :
	gradle clean

clean_all :
	@$(MAKE) clean_apk;                                                        \
	$(MAKE) clean_assets;                                                      \
	sleep 1;                                                                   \
	$(RM) -r gen libs obj deps bin Debug and_env jni/src
	
clean_src :
	$(RM) -r gen libs obj bin Debug and_env jni/src

$(ANDR_ROOT)/jni/src/android_version_githash.h : prep_srcdir
	@export VERSION_FILE=${ANDR_ROOT}/jni/src/android_version_githash.h;        \
	export VERSION_FILE_NEW=$${VERSION_FILE}.new;                              \
	{                                                                          \
	echo "#ifndef ANDROID_MT_VERSION_GITHASH_H";                               \
	echo "#define ANDROID_MT_VERSION_GITHASH_H";                               \
	export GITHASH=$$(git rev-parse --short=8 HEAD);                           \
	export VERSION_STR="${VERSION_MAJOR}.${VERSION_MINOR}.${VERSION_PATCH}";   \
	echo "#define VERSION_GITHASH \"$$VERSION_STR-$$GITHASH-Android\"";        \
	echo "#endif";                                                             \
	} > "$${VERSION_FILE_NEW}";                                                \
	if ! cmp -s $${VERSION_FILE} $${VERSION_FILE_NEW}; then                    \
		echo "android_version_githash.h changed, updating...";             \
		mv "$${VERSION_FILE_NEW}" "$${VERSION_FILE}";                      \
	else                                                                       \
		rm "$${VERSION_FILE_NEW}";                                         \
	fi


$(ANDR_ROOT)/jni/src/android_version.h : prep_srcdir
	@export VERSION_FILE=${ANDR_ROOT}/jni/src/android_version.h;                \
	export VERSION_FILE_NEW=$${VERSION_FILE}.new;                              \
	{                                                                          \
	echo "#ifndef ANDROID_MT_VERSION_H";                                       \
	echo "#define ANDROID_MT_VERSION_H";                                       \
	echo "#define VERSION_MAJOR ${VERSION_MAJOR}";                             \
	echo "#define VERSION_MINOR ${VERSION_MINOR}";                             \
	echo "#define VERSION_PATCH ${VERSION_PATCH}";                             \
	echo "#define VERSION_STRING STR(VERSION_MAJOR) \".\" STR(VERSION_MINOR)   \
	\".\" STR(VERSION_PATCH)";                                                 \
	echo "#endif";                                                             \
	} > $${VERSION_FILE_NEW};                                                  \
	if ! cmp -s $${VERSION_FILE} $${VERSION_FILE_NEW}; then                    \
		echo "android_version.h changed, updating...";                     \
		mv "$${VERSION_FILE_NEW}" "$${VERSION_FILE}";                      \
	else                                                                       \
		rm "$${VERSION_FILE_NEW}";                                         \
	fi

clean : clean_apk clean_assets

