NDK_TOOLCHAIN_VERSION := 4.9
APP_PLATFORM := android-21
APP_CPPFLAGS += -DANDROID
APP_ABI :=  armeabi armeabi-v7a
APP_PROJECT_PATH := $(shell pwd)
APP_BUILD_SCRIPT := $(APP_PROJECT_PATH)/rtmpvedio/Android.mk


