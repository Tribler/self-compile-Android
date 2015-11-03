#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(my-dir)
include $(CLEAR_VARS)

liblog_sources := logd_write.c

# some files must not be compiled when building against Mingw
# they correspond to features not used by our host development tools
# which are also hard or even impossible to port to native Win32
WITH_MINGW :=
ifeq ($(HOST_OS),windows)
    ifeq ($(strip $(USE_CYGWIN)),)
        WITH_MINGW := true
    endif
endif
# USE_MINGW is defined when we build against Mingw on Linux
ifneq ($(strip $(USE_MINGW)),)
    WITH_MINGW := true
endif

ifndef WITH_MINGW
    liblog_sources += \
        logprint.c \
        event_tag_map.c
endif

# Shared library for target
# =========================

LOCAL_MODULE := liblog
LOCAL_SRC_FILES := $(liblog_sources)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../libcutils/jni/include
LOCAL_CFLAGS += -DHAVE_PTHREADS
include $(BUILD_SHARED_LIBRARY)
