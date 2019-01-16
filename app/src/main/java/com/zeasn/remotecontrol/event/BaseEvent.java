package com.zeasn.remotecontrol.event;

public abstract class BaseEvent {

	/**
	 * 发送文本
	 * @param content
	 */
	public abstract void sendString(String content);

	/**
	 * 发送按键事件
	 * @param event
	 */
	public abstract void sendKeyEvent(int event);
	
}
