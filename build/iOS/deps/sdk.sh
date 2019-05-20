#!/bin/bash -e

# This file sets the appropiate compiler and flags for compiling for iOS without XCode
sdk=iphoneos
osver=9.0

export IOS_COMPILER=$(xcrun --sdk $sdk --find clang)
export IOS_CC=$IOS_COMPILER
export IOS_CXX=$IOS_COMPILER
export IOS_FLAGS="-isysroot $(xcrun --sdk $sdk --show-sdk-path) -arch armv7 -arch armv7s -arch arm64 -arch arm64e -miphoneos-version-min=$osver -fvisibility=hidden -fdata-sections -ffunction-sections -Ofast"
export IOS_FLAGS_LUA="-isysroot $(xcrun --sdk $sdk --show-sdk-path) -arch armv7 -arch arm64 -miphoneos-version-min=$osver -fvisibility=hidden -fdata-sections -ffunction-sections -Ofast"
