# 
# Copyright 2006 The Android Open Source Project
#
# Android Asset Packaging Tool
#

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	StringPool.cpp \
  ZipFile.cpp \
  ZipEntry.cpp \
	Images.cpp \
  SourcePos.cpp \
	ResourceTable.cpp \
	Resource.cpp \
	AaptAssets.cpp \
	Main.cpp \
	Package.cpp \
	Command.cpp \
	XMLNode.cpp \

LOCAL_CFLAGS += -Wno-format-y2k
LOCAL_CFLAGS += -DHAVE_ENDIAN_H -DHAVE_ANDROID_OS -DHAVE_PTHREADS -DHAVE_SYS_UIO_H -DHAVE_POSIX_FILEMAP
LOCAL_CFLAGS += -DHAVE_SCHED_H -DHAVE_SYS_UIO_H -DHAVE_IOCTL -DHAVE_TM_GMTOFF
LOCAL_CFLAGS += -DHAVE_EXPAT_CONFIG_H

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../libpng/jni
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../expat/jni/lib
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../libhost/jni/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../libcutils/jni/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../libutils/jni/include

ifeq ($(HOST_OS),linux)
LOCAL_LDLIBS += -lrt -lpthread
endif

# Statically link libz for MinGW (Win SDK under Linux),
# and dynamically link for all others.
ifneq ($(strip $(USE_MINGW)),)
  LOCAL_STATIC_LIBRARIES += libz
else
  LOCAL_LDLIBS += -lz -llog
endif

LOCAL_MODULE := aapt

include $(BUILD_SHARED_LIBRARY)
