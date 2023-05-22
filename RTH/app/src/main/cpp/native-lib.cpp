#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_espol_rth_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_espol_rth_CamBufferActivity_stringFromJNI(
        JNIEnv* env,
        jobject){
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}