#include <jni.h>
#include <stdlib.h>

/* java jstring turn to c/c++ string */

static char *jstringTostr(JNIEnv *env, jstring jstr) {
    char *pStr = NULL;
    jclass jstrObj = env->FindClass("java/lang/String");
    jstring encode = env->NewStringUTF("utf-8");
    jmethodID methodId = env->GetMethodID(jstrObj, "getBytes",
                                          "(Ljava/lang/String;)[B");
    jbyteArray byteArray = (jbyteArray) env->CallObjectMethod(jstr, methodId,
                                                              encode);
    jsize strLen = env->GetArrayLength(byteArray);
    jbyte *jBuf = env->GetByteArrayElements(byteArray, JNI_FALSE);
    if (jBuf != NULL) {
        pStr = (char *) malloc(strLen + 1);
        if (!pStr) {
            return NULL;
        }
        memcpy(pStr, jBuf, strLen);
        pStr[strLen] = 0;
    }
    env->ReleaseByteArrayElements(byteArray, jBuf, 0);

    return pStr;

}
