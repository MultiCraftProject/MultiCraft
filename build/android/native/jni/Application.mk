APP_PLATFORM := ${APP_PLATFORM}
APP_ABI := ${TARGET_ABI}
APP_STL := ${APP_STL}
NDK_TOOLCHAIN_VERSION := $(COMPILER_VERSION)
APP_MODULES := MultiCraft

APP_CPPFLAGS := -Ofast -fvisibility=hidden -fexceptions -Wno-deprecated-declarations -Wno-extra-tokens

ifeq ($(COMPILER_VERSION),4.9)
APP_CPPFLAGS += -flto
endif

ifeq ($(APP_ABI),armeabi-v7a)
APP_CPPFLAGS += -march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb
endif

ifeq ($(APP_ABI),x86)
APP_CPPFLAGS += -march=i686 -mtune=intel -mssse3 -mfpmath=sse -m32 -funroll-loops
endif

ifndef NDEBUG
APP_CPPFLAGS := -g -D_DEBUG -O0 -fno-omit-frame-pointer -fexceptions
endif

APP_CFLAGS   := $(APP_CPPFLAGS) #-Werror=shorten-64-to-32
APP_CXXFLAGS := $(APP_CPPFLAGS) -frtti
APP_LDFLAGS  := -Wl,--no-warn-mismatch,--gc-sections,--icf=safe

ifeq ($(COMPILER_VERSION),clang)
APP_CFLAGS   += $(APP_CPPFLAGS) -Wno-parentheses-equality
APP_CXXFLAGS += $(APP_CPPFLAGS) -std=gnu++17
ifeq ($(APP_ABI),arm64-v8a)
APP_LDFLAGS  := -Wl,--no-warn-mismatch,--gc-sections
endif
endif

ifndef NDEBUG
APP_LDFLAGS  :=
endif
