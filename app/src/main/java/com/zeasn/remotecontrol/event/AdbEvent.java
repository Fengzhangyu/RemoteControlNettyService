package com.zeasn.remotecontrol.event;

import android.util.Log;

import com.zeasn.remotecontrol.utils.KeyUtil;
import com.zeasn.remotecontrol.utils.KeyValue;

import java.io.IOException;
import java.util.concurrent.Executors;


/**adb事件命令*/
public class AdbEvent extends BaseEvent {

	/**
	 * 事件响应处理
	 * 
	 * @param event
	 *            按键事件
	 */
	private int keyCode;
	private String command;

	/**
	 * 发送按键
	 * @param event
	 */
	public void sendKeyEvent(int event) {
		keyCode = event;
		Log.d("mylog", "responseEvent============" + keyCode);

		command = null;
		switch (keyCode) {
		case KeyValue.KEYCODE_KEYBOARD_INPUT:
			// TODO 键盘输入
			break;
		default:
			/**
			 * 为解决操作按键时，Fatal spin-on-suspend, dumping threads的问题，可能是由于执行adb
			 * shell命令时产生的中断使守护线程的子线程死掉
			 */
			Executors.newCachedThreadPool().execute(new KeyThread());
			break;
		}
	}
	
	class KeyThread implements Runnable {
		
		@Override
		public void run() {
			keyCode = KeyUtil.keyValueConvert(keyCode);
			command = "adb shell input keyevent " + keyCode;
			execCommand(command);
		}
	}

	/** 执行命令 */
	private void execCommand(String command) {
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送文本
	 * @param content
	 */
	public void sendString(String content) {
		Executors.newCachedThreadPool().execute(new InputThread(content));
	}
	
	class InputThread implements Runnable {
		String text;
		public InputThread(String text) {
			this.text = text;
		}
		@Override
		public void run() {
			String command = "adb shell input text " + text;
			execCommand(command);
		}
	}
	
	
}
