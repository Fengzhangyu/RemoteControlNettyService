// mInterface.aidl
package com.zeasn.remotecontrol;

// Declare any non-default types here with import statements

interface mInterface {
	void sendKeyEvent(int keyValue);
	void sendString(String content);
}
