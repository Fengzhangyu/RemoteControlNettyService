#include <errno.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <eventInjector/linux/uinput.h>
#include <stdio.h>
#include <errno.h>
#include <android/log.h>
#include "suinput.h"

char* UINPUT_FILEPATHS[] = {
		"/android/dev/uinput",
		"/dev/uinput",
		"/dev/input/uinput",
		"/dev/misc/uinput",
};

#define LOGSI(...)  __android_log_print(ANDROID_LOG_INFO, "SUINPUT", __VA_ARGS__)
#define LOGSE(...)  __android_log_print(ANDROID_LOG_ERROR, "SUINPUT", __VA_ARGS__)

#define UINPUT_FILEPATHS_COUNT (sizeof(UINPUT_FILEPATHS) / sizeof(char*))

int suinput_write(int uinput_fd, uint16_t type, uint16_t code, int32_t value)
{
	struct input_event event;
	memset(&event, 0, sizeof(event));
    gettimeofday(&event.time, 0); /* This should not be able to fail ever.. */
    event.type = type;
    event.code = code;
    event.value = value;
    if (write(uinput_fd, &event, sizeof(event)) != sizeof(event))
    	return -1;
    return 0;
}

int suinput_write_syn(int uinput_fd,
                             uint16_t type, uint16_t code, int32_t value)
{
    if (suinput_write(uinput_fd, type, code, value))
        return -1;
    return suinput_write(uinput_fd, EV_SYN, SYN_REPORT, 0);
}

int suinput_open(const char* device_name, const struct input_id* id)
{
	int original_errno = 0;
	int uinput_fd = -1;
	struct uinput_user_dev user_dev;
	int i;
	LOGSI("suinput_open");
	for (i = 0; i < UINPUT_FILEPATHS_COUNT; ++i) {
		uinput_fd = open(UINPUT_FILEPATHS[i], O_WRONLY | O_NONBLOCK);
		LOGSI("suinput_open++ %s  %s", UINPUT_FILEPATHS[i],strerror(errno));
        if (uinput_fd != -1)
            break;
	}

    if (uinput_fd == -1)
        return -1;

    /* Set device to handle following types of events: */

    /* Key and button events */
     if (ioctl(uinput_fd, UI_SET_EVBIT, EV_KEY) == -1)
         goto err;

//
//     /* Key and button repetition events */
     if (ioctl(uinput_fd, UI_SET_EVBIT, EV_REP) == -1)
         goto err;

//
//     /* Relative pointer motions */
//     if (ioctl(uinput_fd, UI_SET_EVBIT, EV_REL) == -1)
//         goto err;

    /* Absolute pointer motions */
    if (ioctl(uinput_fd, UI_SET_EVBIT, EV_ABS) == -1)
        goto err;

    /* Synchronization events, this is probably set implicitely too. */
    if (ioctl(uinput_fd, UI_SET_EVBIT, EV_SYN) == -1)
        goto err;


    /* Configure device to handle relative x and y axis. */
//     if (ioctl(uinput_fd, UI_SET_RELBIT, REL_X) == -1)
//         goto err;
//     if (ioctl(uinput_fd, UI_SET_RELBIT, REL_Y) == -1)
//         goto err;

    /* Configure device to handle absolute x and y axis. */
    if (ioctl(uinput_fd, UI_SET_ABSBIT, ABS_X) == -1)
        goto err;
    if (ioctl(uinput_fd, UI_SET_ABSBIT, ABS_Y) == -1)
        goto err;

    /* Configure device to handle all keys, see linux/input.h. */
    for (i = 0; i < KEY_MAX; i++) {
        if (ioctl(uinput_fd, UI_SET_KEYBIT, i) == -1)
            goto err;
    }

    /* Set device-specific information. */
    memset(&user_dev, 0, sizeof(user_dev));
    strncpy(user_dev.name, device_name, UINPUT_MAX_NAME_SIZE);
    user_dev.id.bustype = id->bustype;
    user_dev.id.vendor = id->vendor;
    user_dev.id.product = id->product;
    user_dev.id.version = id->version;

    //minor tweak to support ABSolute events
    user_dev.absmin[ABS_X] = -2047;
    user_dev.absmax[ABS_X] = 2048;
    user_dev.absfuzz[ABS_X] = 0;
    user_dev.absflat[ABS_X] = 0;

    user_dev.absmin[ABS_Y] = -2047;
    user_dev.absmax[ABS_Y] = 2048;
    user_dev.absfuzz[ABS_Y] = 0;
    user_dev.absflat[ABS_Y] = 0;

    if (write(uinput_fd, &user_dev, sizeof(user_dev)) != sizeof(user_dev))
        goto err;

    if (ioctl(uinput_fd, UI_DEV_CREATE) == -1)
        goto err;

    /*
  The reason for generating a small delay is that creating succesfully
  an uinput device does not guarantee that the device is ready to process
  input events. It's probably due the asynchronous nature of the udev.
  However, my experiments show that the device is not ready to process input
  events even after a device creation event is received from udev.
  */

    //sleep(2);

    return uinput_fd;

    err:

    /*
    At this point, errno is set for some reason. However, cleanup-actions
    can also fail and reset errno, therefore we store the original one
    and reset it before returning.
  */
    LOGSI("suinput_open error");
    original_errno = errno;

    /* Cleanup. */
    close(uinput_fd); /* Might fail, but we don't care anymore at this point. */

    errno = original_errno;
    return -1;
}

int suinput_close(int uinput_fd)
{
    /*
    Sleep before destroying the device because there still can be some
    unprocessed events. This is not the right way, but I am still
    looking for better ways. The question is: how to know whether there
    are any unprocessed uinput events?
   */
    sleep(2);

    if (ioctl(uinput_fd, UI_DEV_DESTROY) == -1) {
    	close(uinput_fd);
    	return -1;
    }

    if (close(uinput_fd) == -1)
        return -1;

    return 0;
}

int suinput_release(int uinput_fd, uint16_t code)
{
    return suinput_write(uinput_fd, EV_KEY, code, 0);
}
