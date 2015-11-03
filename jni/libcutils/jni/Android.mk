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

ifeq ($(TARGET_CPU_SMP),true)
    targetSmpFlag := -DANDROID_SMP=1
else
    targetSmpFlag := -DANDROID_SMP=0
endif
hostSmpFlag := -DANDROID_SMP=0

commonSources := \
	array.c \
	hashmap.c \
	atomic.c.arm \
	native_handle.c \
	buffer.c \
	socket_inaddr_any_server.c \
	socket_local_client.c \
	socket_local_server.c \
	socket_loopback_client.c \
	socket_loopback_server.c \
	socket_network_client.c \
	config_utils.c \
	cpu_info.c \
	load_file.c \
	open_memstream.c \
	strdup16to8.c \
	strdup8to16.c \
	record_stream.c \
	process_name.c \
	properties.c \
	threads.c \
	sched_policy.c \
	iosched_policy.c \
	str_parms.c \
  abort_socket.c \
  selector.c \
  tztime.c \
  zygote.c
  
commonHostSources := \
        ashmem-host.c \
        tzstrftime.c


# Shared library for target
# ========================================================
include $(CLEAR_VARS)
LOCAL_MODULE := libcutils

LOCAL_SRC_FILES := $(commonSources) ashmem-dev.c mq.c uevent.c

ifeq ($(TARGET_ARCH),arm)
LOCAL_SRC_FILES += arch-arm/memset32.S
else  # !arm
ifeq ($(TARGET_ARCH),sh)
LOCAL_SRC_FILES += memory.c atomic-android-sh.c
else  # !sh
ifeq ($(TARGET_ARCH_VARIANT),x86-atom)
LOCAL_CFLAGS += -DHAVE_MEMSET16 -DHAVE_MEMSET32
LOCAL_SRC_FILES += arch-x86/android_memset16.S arch-x86/android_memset32.S memory.c
else # !x86-atom
LOCAL_SRC_FILES += memory.c
endif # !x86-atom
endif # !sh
endif # !arm

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_CFLAGS += $(targetSmpFlag)
LOCAL_CFLAGS += -DHAVE_PTHREADS -DHAVE_SCHED_H -DHAVE_SYS_UIO_H -DHAVE_ANDROID_OS -DHAVE_IOCTL -DHAVE_TM_GMTOFF

LOCAL_LDLIBS += -llog
LOCAL_LDLIBS += $(LOCAL_PATH)/../../liblog/libs/armeabi/liblog.so

$(info Building module $(LOCAL_MODULE) ...)
$(info LOCAL_PATH=$(LOCAL_PATH))
$(info LOCAL_C_INCLUDES=$(LOCAL_C_INCLUDES))

include $(BUILD_SHARED_LIBRARY)

