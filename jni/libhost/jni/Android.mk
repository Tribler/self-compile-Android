LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	CopyFile.c \
	pseudolocalize.cpp

ifeq ($(HOST_OS),cygwin)
LOCAL_CFLAGS += -DWIN32_EXE
endif
ifeq ($(HOST_OS),windows)
  ifeq ($(USE_MINGW),)
    # Case where we're building windows but not under linux (so it must be cygwin)
    LOCAL_CFLAGS += -DUSE_MINGW
  endif
endif
ifeq ($(HOST_OS),darwin)
LOCAL_CFLAGS += -DMACOSX_RSRC
endif
ifeq ($(HOST_OS),linux)
endif

LOCAL_MODULE:= libhost
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

include $(BUILD_SHARED_LIBRARY)

