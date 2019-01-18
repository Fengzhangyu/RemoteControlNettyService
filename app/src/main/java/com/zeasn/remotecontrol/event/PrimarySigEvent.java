package com.zeasn.remotecontrol.event;

import android.app.Instrumentation;

import com.zeasn.remotecontrol.utils.KeyUtil;


/**原生签名命令事件*/
public class PrimarySigEvent extends BaseEvent {
	
	Instrumentation instrumentation;
	
	
	public PrimarySigEvent() {
		instrumentation = new Instrumentation();
	}

	@Override
	public void sendString(String content) {
		instrumentation.sendStringSync(content);
		
	}

	@Override
	public void sendKeyEvent(int event) {
		int keyCode = KeyUtil.keyValueConvert(event);
		instrumentation.sendKeyDownUpSync(keyCode);
		
	}

}
