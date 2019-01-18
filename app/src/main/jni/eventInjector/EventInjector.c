#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/input.h>
#include <linux/limits.h>
#include <dirent.h>
#include <errno.h>
#include "input.h"

#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, "EventInjector", __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, "EventInjector", __VA_ARGS__)

#define ABS_MT_POSITION_X 0x35
#define ABS_MT_POSITION_Y 0x36

#define DEV_DIR "/dev/input"
#define sizeof_bit_array(bits)  ((bits + 7) / 8)
#define test_bit(bit, array)    (array[bit/8] & (1<<(bit%8)))

struct EVENT_INFO ei;
int initEVT = 0;

const int KEYCODE_LEFT 			= 0x1;
const int KEYCODE_RIGHT 		=0x2;
const int KEYCODE_UP 			=0x3;
const int KEYCODE_DOWN 			=0x4;
const int KEYCODE_ENTER 		=0x5;
const int KEYCODE_VOLUME_UP 	=0x6;
const int KEYCODE_VOLUME_DOWN 	=0x7;
const int KEYCODE_POWER_OFF 	=0x8;
const int KEYCODE_BACK 			=0x9;
const int KEYCODE_MENU			=0xa;
const int KEYCODE_HOME			=0xb;

struct EVENT_INFO
{
	int fd_touch;
	int fd_key;
	int screen_width;
	int screen_height;
	int abs_x_min;
	int abs_x_max;
	int abs_y_min;
	int abs_y_max;
};

int scan_dir(const char *dirname);
void open_dev(const char *deviceName);
int containsNonZeroByte(const uint8_t* array, uint32_t startIndex, uint32_t endIndex);
int convertKey(int key);


int scan_dir(const char *dirname)
{
	char devname[PATH_MAX];
	char *filename;
	DIR *dir;
	struct dirent *de;
	dir = opendir(dirname);
	if(dir == NULL)
		return -1;
	strcpy(devname, dirname);
	filename = devname + strlen(devname);
	*filename++ = '/';
	while((de = readdir(dir))) {
		if(de->d_name[0] == '.' &&
		   (de->d_name[1] == '\0' ||
			(de->d_name[1] == '.' && de->d_name[2] == '\0')))
			continue;
		strcpy(filename, de->d_name);
		open_dev(devname);
	}
	closedir(dir);
	return 0;
}

void open_dev(const char *deviceName)
{
	int fd;
	int version;
	uint8_t key_bitmask[sizeof_bit_array(KEY_MAX + 1)];
	uint8_t abs_bitmask[sizeof_bit_array(ABS_MAX + 1)];
	LOGI("open_dev: %s", deviceName);
	fd = open(deviceName, O_RDWR);
	if(fd < 0) {
		LOGI("could not open device[%d]: %s", errno, strerror(errno));
		return;
	}

	if(ioctl(fd, EVIOCGVERSION, &version)) {
		return;
	}

	memset(key_bitmask, 0, sizeof(key_bitmask));
	if (ioctl(fd, EVIOCGBIT(EV_KEY, sizeof(key_bitmask)), key_bitmask) >= 0) {
		if (containsNonZeroByte(key_bitmask, 0, sizeof_bit_array(BTN_MISC))
				|| containsNonZeroByte(key_bitmask, sizeof_bit_array(BTN_GAMEPAD),
						sizeof_bit_array(BTN_DIGI))
				|| containsNonZeroByte(key_bitmask, sizeof_bit_array(KEY_OK),
						sizeof_bit_array(KEY_MAX + 1))) {
			ei.fd_key = fd;
			LOGI("get key input device: %s", deviceName);
		}
	}

	memset(abs_bitmask, 0, sizeof(abs_bitmask));
	if (ioctl(fd, EVIOCGBIT(EV_ABS, sizeof(abs_bitmask)), abs_bitmask) >= 0) {
		// Is this a new modern multi-touch driver?
		if (test_bit(ABS_MT_POSITION_X, abs_bitmask)
				&& test_bit(ABS_MT_POSITION_Y, abs_bitmask)) {
			ei.fd_touch = fd;
			LOGI("get multi-touch input device: %s", deviceName);

		// Is this an old style single-touch driver?
		} else if (test_bit(BTN_TOUCH, key_bitmask)
				&& test_bit(ABS_X, abs_bitmask) && test_bit(ABS_Y, abs_bitmask)) {
			ei.fd_touch = fd;
			LOGI("get single-touch input device: %s", deviceName);
		}
	}
}

int containsNonZeroByte(const uint8_t* array, uint32_t startIndex, uint32_t endIndex)
{
    const uint8_t* end = array + endIndex;
    array += startIndex;
    while (array != end) {
        if (*(array++) != 0) {
            return 1;
        }
    }
    return 0;
}

int convertKey(int key)
{
	if (key == KEYCODE_LEFT) {
		return KEY_LEFT;
	} else if (key == KEYCODE_RIGHT) {
		return KEY_RIGHT;
	} else if (key == KEYCODE_UP) {
		return KEY_UP;
	} else if (key == KEYCODE_DOWN) {
		return KEY_DOWN;
	} else if (key == KEYCODE_ENTER) {
		return KEY_ENTER;
	} else if (key == KEYCODE_VOLUME_UP) {
		return KEY_VOLUMEUP;
	} else if (key == KEYCODE_VOLUME_DOWN) {
		return KEY_VOLUMEDOWN;
	} else if (key == KEYCODE_POWER_OFF) {
		return KEY_POWER;
	} else if (key == KEYCODE_BACK) {
		return KEY_BACK;
	} else if (key == KEYCODE_MENU) {
		return KEY_MENU;
	} else if (key == KEYCODE_HOME) {
		return KEY_HOME;
	}

	return key; // 表示其他字母键值（a-z,其他符号）
}

int Java_com_zeasn_remotecontrol_event_EventInjector_init(JNIEnv* env, jobject thiz)
{
	if(initEVT)
		return 0;
	initInput();
	initEVT = 1;
	return 0;
}

int Java_com_zeasn_remotecontrol_event_EventInjector_sendkeyEvent(JNIEnv* env, jobject thiz, jint action, jint key)
{
	int ck = convertKey(key);
	LOGI("ck==========%d", ck);
	keyEvent(action, ck);
	return 0;
}

int Java_com_zeasn_remotecontrol_event_EventInjector_close(JNIEnv* env, jobject thiz)
{
	cleanupInput();
	return 0;
}

