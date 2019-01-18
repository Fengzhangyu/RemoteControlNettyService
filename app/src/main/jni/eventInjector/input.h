#ifndef KEYMANIP_H
#define KEYMANIP_H

#include "suinput.h"

#define BUS_VIRTUAL 0x06

struct fbinfo {
	unsigned int version;
	unsigned int width;
	unsigned int height;
};

enum {
	ACTION_DOWN = 0, ACTION_UP, ACTION_MOVE
};

int initInput();
void keyEvent(int action, int key);
void cleanupInput();

#endif
