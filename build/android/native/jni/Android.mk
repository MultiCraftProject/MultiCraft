LOCAL_PATH := $(call my-dir)/..

#LOCAL_ADDRESS_SANITIZER:=true

include $(CLEAR_VARS)
LOCAL_MODULE := Irrlicht
LOCAL_SRC_FILES := deps/Android/Irrlicht/${COMPILER_VERSION}/$(APP_ABI)/libIrrlicht.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := LevelDB
LOCAL_SRC_FILES := deps/Android/LevelDB/${COMPILER_VERSION}/$(APP_ABI)/libleveldb.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := Curl
LOCAL_SRC_FILES := deps/Android/Curl/${COMPILER_VERSION}/$(APP_ABI)/libcurl.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := Freetype
LOCAL_SRC_FILES := deps/Android/Freetype/${COMPILER_VERSION}/$(APP_ABI)/libfreetype.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := OpenAL
LOCAL_SRC_FILES := deps/Android/OpenAL-Soft/${COMPILER_VERSION}/$(APP_ABI)/libopenal.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := Vorbis
LOCAL_SRC_FILES := deps/Android/Vorbis/${COMPILER_VERSION}/$(APP_ABI)/libvorbis.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := LuaJIT
LOCAL_SRC_FILES := deps/Android/LuaJIT/${COMPILER_VERSION}/$(APP_ABI)/libluajit.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := gmp
LOCAL_SRC_FILES := deps/Android/gmp/${COMPILER_VERSION}/$(APP_ABI)/libgmp.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := MultiCraft

LOCAL_CFLAGS += \
	-DJSONCPP_NO_LOCALE_SUPPORT      \
	-DHAVE_TOUCHSCREENGUI            \
	-DUSE_CURL=1                     \
	-DUSE_SOUND=1                    \
	-DUSE_FREETYPE=1                 \
	-DUSE_GETTEXT=1                  \
	-DUSE_LEVELDB=1                  \
	-DUSE_LUAJIT=1                   \
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
	../../../src/script                            \
	../../../lib/intl                              \
	../../../lib/jsoncpp                           \
	../../../src/cguittfont                        \
	deps/Android/Irrlicht/include                  \
	deps/Android/Freetype/include                  \
	deps/Android/Curl/include                      \
	deps/Android/OpenAL-Soft/include               \
	deps/Android/Vorbis/include                    \
	deps/Android/LevelDB/include                   \
	deps/Android/LuaJIT/src                        \
	deps/Android/libiconv/include                  \
	deps/Android/libiconv/libcharset/include

LOCAL_SRC_FILES := \
	../../../src/ban.cpp                           \
	../../../src/camera.cpp                        \
	../../../src/cavegen.cpp                       \
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
	../../../src/database-dummy.cpp                \
	../../../src/database-files.cpp                \
	../../../src/database.cpp                      \
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
	../../../src/mapgen_fractal.cpp                \
	../../../src/mapgen_singlenode.cpp             \
	../../../src/mapgen_v5.cpp                     \
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
	../../../src/shader.cpp                        \
	../../../src/sky.cpp                           \
	../../../src/socket.cpp                        \
	../../../src/sound.cpp                         \
	../../../src/sound_openal.cpp                  \
	../../../src/staticobject.cpp                  \
	../../../src/subgame.cpp                       \
	../../../src/tileanimation.cpp                 \
	../../../src/tool.cpp                          \
	../../../src/treegen.cpp                       \
	../../../src/version.cpp                       \
	../../../src/voxel.cpp                         \
	../../../src/voxelalgorithms.cpp               \
	../../../src/util/areastore.cpp                \
	../../../src/util/auth.cpp                     \
	../../../src/util/base64.cpp                   \
	../../../src/util/directiontables.cpp          \
	../../../src/util/enriched_string.cpp          \
	../../../src/util/numeric.cpp                  \
	../../../src/util/pointedthing.cpp             \
	../../../src/util/serialize.cpp                \
	../../../src/util/sha1.cpp                     \
	../../../src/util/string.cpp                   \
	../../../src/util/srp.cpp                      \
	../../../src/util/timetaker.cpp                \
	../../../src/touchscreengui.cpp                \
	../../../src/database-leveldb.cpp              \
	../../../src/settings.cpp                      \
	../../../src/wieldmesh.cpp                     \
	../../../src/client/clientlauncher.cpp         \
	../../../src/client/inputhandler.cpp           \
	../../../src/client/tile.cpp                   \
	../../../src/util/sha256.c                     \
	../../../src/client/joystick_controller.cpp    \
	../../../src/irrlicht_changes/static_text.cpp

LOCAL_CFLAGS += #-Werror=shorten-64-to-32
# Network
LOCAL_SRC_FILES += \
	../../../src/network/connection.cpp            \
	../../../src/network/networkpacket.cpp         \
	../../../src/network/clientopcodes.cpp         \
	../../../src/network/clientpackethandler.cpp   \
	../../../src/network/serveropcodes.cpp         \
	../../../src/network/serverpackethandler.cpp

# Threading
LOCAL_SRC_FILES += \
	../../../src/threading/event.cpp              \
	../../../src/threading/mutex.cpp              \
	../../../src/threading/semaphore.cpp          \
	../../../src/threading/thread.cpp

# lua api
LOCAL_SRC_FILES += \
	../../../src/script/common/c_content.cpp       \
	../../../src/script/common/c_converter.cpp     \
	../../../src/script/common/c_internal.cpp      \
	../../../src/script/common/c_types.cpp         \
	../../../src/script/cpp_api/s_async.cpp        \
	../../../src/script/cpp_api/s_base.cpp         \
	../../../src/script/cpp_api/s_client.cpp       \
	../../../src/script/cpp_api/s_entity.cpp       \
	../../../src/script/cpp_api/s_env.cpp          \
	../../../src/script/cpp_api/s_inventory.cpp    \
	../../../src/script/cpp_api/s_item.cpp         \
	../../../src/script/cpp_api/s_mainmenu.cpp     \
	../../../src/script/cpp_api/s_node.cpp         \
	../../../src/script/cpp_api/s_nodemeta.cpp     \
	../../../src/script/cpp_api/s_player.cpp       \
	../../../src/script/cpp_api/s_security.cpp     \
	../../../src/script/cpp_api/s_server.cpp       \
	../../../src/script/lua_api/l_areastore.cpp    \
	../../../src/script/lua_api/l_base.cpp         \
	../../../src/script/lua_api/l_camera.cpp       \
	../../../src/script/lua_api/l_client.cpp       \
	../../../src/script/lua_api/l_craft.cpp        \
	../../../src/script/lua_api/l_env.cpp          \
	../../../src/script/lua_api/l_inventory.cpp    \
	../../../src/script/lua_api/l_item.cpp         \
	../../../src/script/lua_api/l_itemstackmeta.cpp\
	../../../src/script/lua_api/l_localplayer.cpp  \
	../../../src/script/lua_api/l_mainmenu.cpp     \
	../../../src/script/lua_api/l_mapgen.cpp       \
	../../../src/script/lua_api/l_metadata.cpp     \
	../../../src/script/lua_api/l_minimap.cpp      \
	../../../src/script/lua_api/l_nodemeta.cpp     \
	../../../src/script/lua_api/l_nodetimer.cpp    \
	../../../src/script/lua_api/l_noise.cpp        \
	../../../src/script/lua_api/l_object.cpp       \
	../../../src/script/lua_api/l_particles.cpp    \
	../../../src/script/lua_api/l_rollback.cpp     \
	../../../src/script/lua_api/l_server.cpp       \
	../../../src/script/lua_api/l_settings.cpp     \
	../../../src/script/lua_api/l_sound.cpp        \
	../../../src/script/lua_api/l_http.cpp         \
	../../../src/script/lua_api/l_storage.cpp      \
	../../../src/script/lua_api/l_util.cpp         \
	../../../src/script/lua_api/l_vmanip.cpp       \
	../../../src/script/scripting_client.cpp       \
	../../../src/script/scripting_server.cpp       \
	../../../src/script/scripting_mainmenu.cpp

# Freetype2
LOCAL_SRC_FILES += ../../../src/cguittfont/xCGUITTFont.cpp

# libIntl
LOCAL_SRC_FILES += ../../../lib/intl/libintl.cpp

# JSONCPP
LOCAL_SRC_FILES += ../../../lib/jsoncpp/jsoncpp.cpp

# iconv
LOCAL_SRC_FILES += \
	deps/Android/libiconv/lib/iconv.c              \
	deps/Android/libiconv/libcharset/lib/localcharset.c

# GMP
#ifneq ($(APP_ABI),arm64-v8a)
	LOCAL_C_INCLUDES += ../../../lib/gmp
	LOCAL_SRC_FILES  += ../../../lib/gmp/mini-gmp.c
#else
#	LOCAL_CFLAGS += -DUSE_SYSTEM_GMP=1
#	LOCAL_C_INCLUDES += deps/Android/gmp/include
#	LOCAL_STATIC_LIBRARIES := gmp
#endif

LOCAL_STATIC_LIBRARIES += Irrlicht LevelDB Curl Freetype OpenAL Vorbis LuaJIT android_native_app_glue $(PROFILER_LIBS)

LOCAL_LDLIBS := -lEGL -lGLESv1_CM -lGLESv2 -landroid -lOpenSLES

include $(BUILD_SHARED_LIBRARY)

ifdef GPROF
$(call import-module,android-ndk-profiler)
endif
$(call import-module,android/native_app_glue)
