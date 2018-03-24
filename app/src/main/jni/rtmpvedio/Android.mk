LOCAL_PATH := $(call my-dir)

subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, \
		rtmpdump \
	))

$(warning "the value of LOCAL_PATH is $(LOCAL_PATH)")
$(warning "the value of Sub dir is $(subdirs)")

#SSL := /home/apadmin/Workspace/android-toolchain/sysroot
SSL := $(LOCAL_PATH)/polarssl-1.2.14/sysroot
ifndef SSL
$(error "You must define SSL before starting")
endif

include $(subdirs)
