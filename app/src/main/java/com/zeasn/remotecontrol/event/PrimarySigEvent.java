package com.zeasn.remotecontrol.event;

import android.app.Instrumentation;
import android.util.Log;

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
		Log.d("PrimarySigEvent==star", event + "");
		int keyCode = KeyUtil.keyValueConvert(event);
		Log.d("PrimarySigEvent==end", event + "");
		instrumentation.sendKeyDownUpSync(keyCode);
		
	}

}
