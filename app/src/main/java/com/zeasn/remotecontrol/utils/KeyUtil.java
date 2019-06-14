package com.zeasn.remotecontrol.utils;

import android.view.KeyEvent;

public class KeyUtil {

	/**
	 * 为避免不同设备开发厂商修改键值，根据自定义键值转化为当前设备对应键值
	 * 
	 * @param autoKey
	 *            自定义键值
	 * @return 设备厂商键值
	 */
	public static final int keyValueConvert(int autoKey) {
		switch (autoKey) {
		case KeyValue.KEYCODE_LEFT:
			return KeyEvent.KEYCODE_DPAD_LEFT;
		case KeyValue.KEYCODE_RIGHT:
			return KeyEvent.KEYCODE_DPAD_RIGHT;
		case KeyValue.KEYCODE_UP:
			return KeyEvent.KEYCODE_DPAD_UP;
		case KeyValue.KEYCODE_DOWN:
			return KeyEvent.KEYCODE_DPAD_DOWN;
		case KeyValue.KEYCODE_ENTER:
			return KeyEvent.KEYCODE_ENTER;
		case KeyValue.KEYCODE_POWER_OFF:
			return KeyEvent.KEYCODE_POWER;
		case KeyValue.KEYCODE_MENU:
			return KeyEvent.KEYCODE_MENU;
		case KeyValue.KEYCODE_HOME:
			return KeyEvent.KEYCODE_HOME;
		case KeyValue.KEYCODE_BACK:
			return KeyEvent.KEYCODE_BACK;
		case KeyValue.KEYCODE_MUTE:
			return KeyEvent.KEYCODE_VOLUME_MUTE;
		default:
			if (MLog.DEBUG) {
				MLog.e("wrongKey", "keyValue>>>>>  " + autoKey);
//				throw new RuntimeException("keyValueConvert转化时，没有找到对应键值");
			}
			return -1;
		}
	}

}
