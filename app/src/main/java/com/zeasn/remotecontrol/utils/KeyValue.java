package com.zeasn.remotecontrol.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class KeyValue implements Parcelable {

	/**遥控器向左*/
	public static final int KEYCODE_LEFT = 0x1;
	/**遥控器向右*/
	public static final int KEYCODE_RIGHT = 0x2;
	/**遥控器向上*/
	public static final int KEYCODE_UP = 0x3;
	/**遥控器向下*/
	public static final int KEYCODE_DOWN = 0x4;
	/**遥控器确定*/
	public static final int KEYCODE_ENTER = 0x5;
	/**增大音量*/
	public static final int KEYCODE_VOLUME_UP = 0x6;
	/**减小音量*/
	public static final int KEYCODE_VOLUME_DOWN = 0x7;
	/**关机*/
	public static final int KEYCODE_POWER_OFF = 0x8;
	/**返回按键*/
	public static final int KEYCODE_BACK = 0x9;
	/**Menu菜单*/
	public static final int KEYCODE_MENU = 0xa;
	/**HOME按键*/
	public static final int KEYCODE_HOME = 0xb;
	/**键盘输入*/
	public static final int KEYCODE_KEYBOARD_INPUT = 0xc;

	public KeyValue(Parcel in) {
		
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
	}
	
	public static final Parcelable.Creator<KeyValue> CREATOR = new Parcelable.Creator<KeyValue>() {

		@Override
		public KeyValue createFromParcel(Parcel in) {
			return new KeyValue(in);
		}

		@Override
		public KeyValue[] newArray(int size) {
			return new KeyValue[size];
		}
	};
}
