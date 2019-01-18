LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := RemoteControl
LOCAL_SRC_FILES := guard.cpp \
				   eventInjector/EventInjector.c\
				   eventInjector/suinput.c \
				   eventInjector/input.c
LOCAL_LDLIBS    := -lm -llog

include $(BUILD_SHARED_LIBRARY)
