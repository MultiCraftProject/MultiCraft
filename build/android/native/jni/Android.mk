LOCAL_PATH := $(call my-dir)/..

#LOCAL_ADDRESS_SANITIZER:=true

include $(CLEAR_VARS)
LOCAL_MODULE := Curl
LOCAL_SRC_FILES := deps/Android/Curl/${NDK_TOOLCHAIN_VERSION}_nossl/$(APP_ABI)/libcurl.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := Freetype
LOCAL_SRC_FILES := deps/Android/Freetype/${NDK_TOOLCHAIN_VERSION}/$(APP_ABI)/libfreetype.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := Irrlicht
LOCAL_SRC_FILES := deps/Android/Irrlicht/${NDK_TOOLCHAIN_VERSION}/$(APP_ABI)/libIrrlicht.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := LevelDB
LOCAL_SRC_FILES := deps/Android/LevelDB/${NDK_TOOLCHAIN_VERSION}/$(APP_ABI)/libleveldb.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := LuaJIT
LOCAL_SRC_FILES := deps/Android/LuaJIT/${NDK_TOOLCHAIN_VERSION}/$(APP_ABI)/libluajit.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := OpenAL
LOCAL_SRC_FILES := deps/Android/OpenAL-Soft/${NDK_TOOLCHAIN_VERSION}/$(APP_ABI)/libopenal.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := Gettext
LOCAL_SRC_FILES := deps/Android/Gettext/${NDK_TOOLCHAIN_VERSION}/$(APP_ABI)/libintl.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := Vorbis
LOCAL_SRC_FILES := deps/Android/Vorbis/${NDK_TOOLCHAIN_VERSION}/$(APP_ABI)/libvorbis.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := MultiCraft

LOCAL_CFLAGS += \
	-DJSONCPP_NO_LOCALE_SUPPORT     \
	-DHAVE_TOUCHSCREENGUI           \
	-DUSE_CURL=1                    \
	-DUSE_SOUND=1                   \
	-DUSE_FREETYPE=1                \
	-DUSE_LEVELDB=1                 \
	-DUSE_LUAJIT=1                  \
	-DUSE_GETTEXT=1                 \
	-DENABLE_GLES=1                 \
	$(GPROF_DEF)

ifdef NDEBUG
	LOCAL_CFLAGS += -DNDEBUG=1
endif

ifdef GPROF
	GPROF_DEF := -DGPROF
	PROFILER_LIBS := android-ndk-profiler
	LOCAL_CFLAGS += -pg
endif

LOCAL_C_INCLUDES := \
	../../../src                                   \
	../../../src/cguittfont                        \
	../../../src/script                            \
	../../../lib/gmp                               \
	../../../lib/jsoncpp                           \
	deps/Android/Curl/include                      \
	deps/Android/Freetype/include                  \
	deps/Android/Irrlicht/include                  \
	deps/Android/LevelDB/include                   \
	deps/Android/Gettext/include                   \
	deps/Android/ndk_iconv                         \
	deps/Android/LuaJIT/src                        \
	deps/Android/OpenAL-Soft/include               \
	deps/Android/Vorbis/include

LOCAL_SRC_FILES := \
	../../../src/ban.cpp                           \
	../../../src/camera.cpp                        \
	../../../src/cavegen.cpp                       \
	../../../src/cguittfont/xCGUITTFont.cpp        \
	../../../src/chat.cpp                          \
	../../../src/client.cpp                        \
	../../../src/clientenvironment.cpp             \
	../../../src/clientiface.cpp                   \
	../../../src/clientmap.cpp                     \
	../../../src/clientmedia.cpp                   \
	../../../src/clientobject.cpp                  \
	../../../src/clouds.cpp                        \
	../../../src/collision.cpp                     \
	../../../src/content_abm.cpp                   \
	../../../src/content_cao.cpp                   \
	../../../src/content_mapblock.cpp              \
	../../../src/content_mapnode.cpp               \
	../../../src/content_nodemeta.cpp              \
	../../../src/content_sao.cpp                   \
	../../../src/convert_json.cpp                  \
	../../../src/craftdef.cpp                      \
	../../../src/database.cpp                      \
	../../../src/database-dummy.cpp                \
	../../../src/database-files.cpp                \
	../../../src/database-leveldb.cpp              \
	../../../src/debug.cpp                         \
	../../../src/defaultsettings.cpp               \
	../../../src/drawscene.cpp                     \
	../../../src/dungeongen.cpp                    \
	../../../src/emerge.cpp                        \
	../../../src/environment.cpp                   \
	../../../src/face_position_cache.cpp           \
	../../../src/filecache.cpp                     \
	../../../src/filesys.cpp                       \
	../../../src/fontengine.cpp                    \
	../../../src/game.cpp                          \
	../../../src/genericobject.cpp                 \
	../../../src/gettext.cpp                       \
	../../../src/guiChatConsole.cpp                \
	../../../src/guiEngine.cpp                     \
	../../../src/guiFileSelectMenu.cpp             \
	../../../src/guiFormSpecMenu.cpp               \
	../../../src/guiKeyChangeMenu.cpp              \
	../../../src/guiPasswordChange.cpp             \
	../../../src/guiTable.cpp                      \
	../../../src/guiscalingfilter.cpp              \
	../../../src/guiVolumeChange.cpp               \
	../../../src/httpfetch.cpp                     \
	../../../src/hud.cpp                           \
	../../../src/imagefilters.cpp                  \
	../../../src/intlGUIEditBox.cpp                \
	../../../src/inventory.cpp                     \
	../../../src/inventorymanager.cpp              \
	../../../src/irrlicht_changes/static_text.cpp  \
	../../../src/itemdef.cpp                       \
	../../../src/itemstackmetadata.cpp             \
	../../../src/keycode.cpp                       \
	../../../src/light.cpp                         \
	../../../src/localplayer.cpp                   \
	../../../src/log.cpp                           \
	../../../src/main.cpp                          \
	../../../src/map.cpp                           \
	../../../src/map_settings_manager.cpp          \
	../../../src/mapblock.cpp                      \
	../../../src/mapblock_mesh.cpp                 \
	../../../src/mapgen.cpp                        \
	../../../src/mapgen_flat.cpp                   \
	../../../src/mapgen_v6.cpp                     \
	../../../src/mapgen_v7.cpp                     \
	../../../src/mapgen_v7p.cpp                    \
	../../../src/mapgen_valleys.cpp                \
	../../../src/mapnode.cpp                       \
	../../../src/mapsector.cpp                     \
	../../../src/mesh.cpp                          \
	../../../src/mesh_generator_thread.cpp         \
	../../../src/metadata.cpp                      \
	../../../src/mg_biome.cpp                      \
	../../../src/mg_decoration.cpp                 \
	../../../src/mg_ore.cpp                        \
	../../../src/mg_schematic.cpp                  \
	../../../src/minimap.cpp                       \
	../../../src/mods.cpp                          \
	../../../src/nameidmapping.cpp                 \
	../../../src/nodedef.cpp                       \
	../../../src/nodemetadata.cpp                  \
	../../../src/nodetimer.cpp                     \
	../../../src/noise.cpp                         \
	../../../src/objdef.cpp                        \
	../../../src/object_properties.cpp             \
	../../../src/particles.cpp                     \
	../../../src/pathfinder.cpp                    \
	../../../src/player.cpp                        \
	../../../src/porting_android.cpp               \
	../../../src/porting.cpp                       \
	../../../src/profiler.cpp                      \
	../../../src/quicktune.cpp                     \
	../../../src/raycast.cpp                       \
	../../../src/reflowscan.cpp                    \
	../../../src/remoteplayer.cpp                  \
	../../../src/rollback_interface.cpp            \
	../../../src/serialization.cpp                 \
	../../../src/server.cpp                        \
	../../../src/serverenvironment.cpp             \
	../../../src/serverlist.cpp                    \
	../../../src/serverobject.cpp                  \
	../../../src/settings.cpp                      \
	../../../src/shader.cpp                        \
	../../../src/sky.cpp                           \
	../../../src/socket.cpp                        \
	../../../src/sound.cpp                         \
	../../../src/sound_openal.cpp                  \
	../../../src/staticobject.cpp                  \
	../../../src/subgame.cpp                       \
	../../../src/tileanimation.cpp                 \
	../../../src/tool.cpp                          \
	../../../src/touchscreengui.cpp                \
	../../../src/treegen.cpp                       \
	../../../src/version.cpp                       \
	../../../src/voxel.cpp                         \
	../../../src/voxelalgorithms.cpp               \
	../../../src/wieldmesh.cpp

# Client
LOCAL_SRC_FILES += $(wildcard ../../../src/client/*.cpp)

# Network
LOCAL_SRC_FILES += $(wildcard ../../../src/network/*.cpp)

# Lua API
LOCAL_SRC_FILES += $(wildcard ../../../src/script/*.cpp)
LOCAL_SRC_FILES += $(wildcard ../../../src/script/*/*.cpp)

# Threading
LOCAL_SRC_FILES += $(wildcard ../../../src/threading/*.cpp)

# Util
LOCAL_SRC_FILES += $(wildcard ../../../src/util/*.c)
LOCAL_SRC_FILES += $(wildcard ../../../src/util/*.cpp)

# GMP
LOCAL_SRC_FILES += ../../../lib/gmp/mini-gmp.c

# JSONCPP
LOCAL_SRC_FILES += ../../../lib/jsoncpp/jsoncpp.cpp

# Lua UTF-8 Lib
LOCAL_SRC_FILES += ../../../lib/luautf8/lutf8lib.c

LOCAL_STATIC_LIBRARIES += Curl Gettext Freetype Irrlicht LevelDB OpenAL Vorbis LuaJIT android_native_app_glue $(PROFILER_LIBS)

LOCAL_LDLIBS := -lEGL -lGLESv1_CM -lGLESv2 -landroid -lOpenSLES

include $(BUILD_SHARED_LIBRARY)

ifdef GPROF
$(call import-module,android-ndk-profiler)
endif
$(call import-module,android/native_app_glue)
