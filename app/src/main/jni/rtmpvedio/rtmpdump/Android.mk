LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := polarssl

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../polarssl-1.2.14/sysroot/include
LOCAL_SRC_FILES := $(LOCAL_PATH)/../polarssl-1.2.14/sysroot/lib/libpolarssl.a
#LOCAL_EXPORT_C_INCLUDES := /home/apadmin/Workspace/android-toolchain/sysroot/include
#LOCAL_SRC_FILES := /home/apadmin/Workspace/android-toolchain/sysroot/lib/libpolarssl.a

include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES += ./librtmp \
    $(LOCAL_PATH)/sysroot/include

LOCAL_SRC_FILES:= \
    librtmp/amf.c \
   librtmp/hashswf.c \
   librtmp/log.c \
   librtmp/parseurl.c \
   librtmp/rtmp.c

LOCAL_STATIC_LIBRARIES = polarssl

LOCAL_CFLAGS += -I$(LOCAL_PATH)/../polarssl-1.2.14/sysroot/include -DUSE_POLARSSL
LOCAL_LDLIBS += -L$(LOCAL_PATH)/../polarssl-1.2.14/sysroot/lib -L$(LOCAL_PATH)/../polarssl-1.2.14/sysroot/lib/usr/lib
#LOCAL_CFLAGS += -I/home/apadmin/Workspace/android-toolchain/sysroot/include -DUSE_POLARSSL
#LOCAL_LDLIBS += -L/home/apadmin/Workspace/android-toolchain/sysroot/lib -L/home/apadmin/Workspace/android-toolchain/sysroot/lib/usr/lib
LOCAL_LDLIBS += -lz

LOCAL_MODULE := rtmp

include $(BUILD_SHARED_LIBRARY)


