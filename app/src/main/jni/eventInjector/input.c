#include "input.h"
#include <android/log.h>

#define LOGII(...)  __android_log_print(ANDROID_LOG_INFO, "INPUT", __VA_ARGS__)
#define LOGIE(...)  __android_log_print(ANDROID_LOG_ERROR, "INPUT", __VA_ARGS__)

int inputfd = -1;

struct fbinfo rfbClientPtr;

int initInput()
{
	//  L("---Initializing uinput...---\n");
	struct input_id id = {
	BUS_VIRTUAL, /* Bus type. */
	1, /* Vendor id. */
	1, /* Product id. */
	4 /* Version id. */
	};

	if ((inputfd = suinput_open("qwerty", &id)) == -1)
	{
		 return -1;
	}

	return 0;
}

void keyEvent(int action, int key)
{
	suinput_write(inputfd, EV_KEY, key, 1);
	suinput_write(inputfd, EV_SYN, SYN_REPORT, 0);
	suinput_write(inputfd, EV_KEY, key, 0);
	suinput_write(inputfd, EV_SYN, SYN_REPORT, 0);
}

void cleanupInput()
{
	if(inputfd != -1)
	{
		suinput_close(inputfd);
	}
}

