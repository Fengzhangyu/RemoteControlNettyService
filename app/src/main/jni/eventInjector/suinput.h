#ifndef SUINPUT_H
#define SUINPUT_H

#include <stdint.h>
#include <linux/input.h>
#include <eventInjector/linux/uinput.h>


int suinput_write(int uinput_fd, uint16_t type, uint16_t code, int32_t value);

int suinput_open(const char* device_name, const struct input_id* id);

int suinput_close(int uinput_fd);

int suinput_press(int uinput_fd, uint16_t code);

int suinput_release(int uinput_fd, uint16_t code);

#endif


