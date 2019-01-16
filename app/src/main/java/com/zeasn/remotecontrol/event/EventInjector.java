package com.zeasn.remotecontrol.event;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.zeasn.remotecontrol.utils.MLog;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;


/**
 * 事件驱动实现
 * 该实现必须设备是root过的，或者有系统的签名，只有这样才有权限调用底层的驱动事件。
 * */
public class EventInjector extends BaseEvent {
	
	public EventInjector() {
		init();
	}
	
	public native final int init();
	public native final int sendkeyEvent(int action, int key);
	public native final int close();

	@Override
	public void sendString(String content) {
		if (content == null) {
            return;
        }
        KeyCharacterMap keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);

        KeyEvent[] events = keyCharacterMap.getEvents(content.toCharArray());

        if (events != null) {
        	int keyCode = -1;
            for (int i = 0; i < events.length; i++) {
                // We have to change the time of an event before injecting it because
                // all KeyEvents returned by KeyCharacterMap.getEvents() have the same
                // time stamp and the system rejects too old events. Hence, it is
                // possible for an event to become stale before it is injected if it
                // takes too long to inject the preceding ones.
            	keyCode = events[i].getKeyCode();
            	sendKeyEvent(keyCode);
            }
        }
	}

	@Override
	public void sendKeyEvent(int event) {
		sendkeyEvent(0, event); //TODO 这里的Action暂时不填写
	}
	
	
    private boolean execRootCommand() {
    	Process process = null;
    	DataOutputStream os = null;
    	DataInputStream is = null;
    	
//		try {
//			process = Runtime.getRuntime().exec("");
//			DataOutputStream os = new DataOutputStream(process.getOutputStream());
////	    	os.writeBytes("mount -oremount,rw /dev/block/mtdblock3 /system\n");
////	    	os.writeBytes("busybox cp /data/data/com.koushikdutta.superuser/su /system/bin/su\n");
//	    	os.writeBytes("busybox chown 0:0 /system/bin/su\n");
//	    	os.writeBytes("chmod 4755 /system/bin/su\n");
//	    	os.writeBytes("exit\n");
//	    	os.flush();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		

//    	
//		try {
//            process = Runtime.getRuntime().exec("chmod 4755 /system/bin/su"); /*这里可能需要修改su
//
//   的源代码 （注掉  if (myuid != AID_ROOT && myuid != AID_SHELL) {*/
//
//            os = new DataOutputStream(process.getOutputStream());
//            is = new DataInputStream(process.getInputStream());
//           os.writeBytes("/system/bin/ls" + " \n");  //这里可以执行具有root 权限的程序了  
//            os.writeBytes(" exit \n");
//            os.flush();
//            process.waitFor();
//        } catch (Exception e) {            
//            Log.e("mylog", "Unexpected error - Here is what I know:" + e.getMessage());
//        } finally {
//            try {
//                if (os != null) {
//                    os.close();
//                }
//                if (is != null) {
//                    is.close();
//                }
//                process.destroy();
//            } catch (Exception e) {
//            }
//        }// get the root privileges
//    	return false;

		
//    	try {
//			Runtime.getRuntime().exec("chmod 777 /system/xbin/su");
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
    	
    	
    	String[] args = {"chmod", "777", "/dev/uinput" };
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		processBuilder.redirectErrorStream(true);
		BufferedReader bufReader = null;
		String installState = null;
		try {
			process = processBuilder.start();
			bufReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = bufReader.readLine()) != null) {
				installState = line;
			}
			int ret = process.waitFor();
		} catch (IOException e) {
			MLog.d("IO异常：" + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			MLog.d("EX异常：" + e.toString());
			e.printStackTrace();
		} finally {


			if (installState == null || !installState.trim().equals("Success")) {
				//TODO TO DO 
			} else {
			}
			
			try {
				if (bufReader != null) 
					bufReader.close();
				
				if(process != null) 
					process.destroy();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			init();
		}
    	
    	
    	
    	
    	
//    	
//    	try {
////    		Runtime.getRuntime().exec("/system/xsbin/su\n");
//			process = Runtime.getRuntime().exec("su");
//			
//			os = new DataOutputStream(process.getOutputStream());
////			os.writeBytes("busybox mount -o remount rw /dev\n");
//			os.writeBytes("chmod 666 /dev/uinput\n");
////			os.writeBytes("chmod 777 /dev/input/*\n");
////			os.writeBytes("busybox chmod 666 /dev/uinput\n");
//			os.writeBytes("exit\n");
//			os.flush();
//			
//			process.waitFor();
//			
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} finally {
//			if (os != null) {
//				try {
//					os.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			if (process != null)
//				process.destroy();
//			
//			init();
//		}
    	
    	
		

//    }
//        try {
//        	Runtime.getRuntime().exec("chmod 777 /dev/uinput\n");
//            process = Runtime.getRuntime().exec("chmod 777 /dev/input\n");
////            os = new DataOutputStream(process.getOutputStream());
////            os.writeBytes("chmod 777 /dev/input\n");
////            os.writeBytes("chmod 777 /dev/uinput\n");
////            os.writeBytes("exit\n");
////            os.flush();
//            MLog.d("--------------Root------------ ");
//            // 1 is success
//            process.waitFor();
///*         if (process.waitFor() == 1) {
//                Log.d(TAG, "Root SUCCESS ");
//                return true;
//            } else {
//            	Log.d(TAG, "Root error ");
//                return false;
//            }
//  */      } catch (Exception e) {
//            MLog.e( "ROOT ERR:" + e.getMessage());
//            return false;
//        } finally {
//            try {
//                if (os != null) {
//                    os.close();
//                }
//                process.destroy();
//                MLog.d("Root SUCCESS");
//            } catch (Exception e) {
//            }
//        }
        return true;
    }
    
	public static int chmod(String path, int mode) throws Exception {
		  Class fileUtils = Class.forName("android.os.FileUtils");
		  Method setPermissions =
		      fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
		  return (Integer) setPermissions.invoke(null, path, mode, -1, -1);
	}
    
}
