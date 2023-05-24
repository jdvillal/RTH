#include <jni.h>
#include <string>
#include <string.h>
#include <android/log.h>
#include "LinkedList.h"

typedef struct frameNode{
        unsigned long timestamp;
        unsigned int width;
        unsigned int height;
        unsigned int arr_size;
        unsigned char* yuv_array;
        frameNode *next;
        //frameNode *prev;
} frameNode_t;

typedef struct frameLinkedList{
    frameNode_t *head;
    frameNode_t *tail;
    int size;
} frameLinkedList_t;

frameLinkedList_t* new_frameLinkedList(){
    frameLinkedList *t = (frameLinkedList*)malloc(sizeof (frameLinkedList));
    t->size = 0;
    t-> head = NULL;
    return t;
}
void frameLinkedList_push(frameLinkedList_t *list, frameNode_t *node){
    if(list->size == 0){
        list->head = node;
        list->tail = node;
        list->size = 1;
    }else{
        list->tail->next = node;
        list->tail = node;
        list->size = list->size + 1;
    }
}

int frameLinkedList_removeFirst(frameLinkedList_t *list){
    if(list->size == 0) return -1;

    frameNode_t *to_remove = list->head;
    if(list-> size == 1){
        list->head = NULL;
        list->tail = NULL;
        list->size = 0;
    }else{
        list->head = to_remove->next;
        list->size = list->size - 1;
    }
    free(to_remove->yuv_array);//free array
    free(to_remove);//free node
    return 0;
}


static frameLinkedList_t *frames_list = new_frameLinkedList();


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

extern "C" JNIEXPORT jstring JNICALL
Java_com_espol_rth_CamBufferActivity_printLog(){
    __android_log_write(ANDROID_LOG_ERROR, "Tag", "Error here");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_espol_rth_CamBufferActivity_addFrame(
        JNIEnv* env,
        jobject,
        jbyteArray array,
        jint array_size,
        jint width,
        jint height,
        jlong timestamp
        ){
    char str_size[10];
    sprintf(str_size, "%d", (int)array_size);
    __android_log_write(ANDROID_LOG_ERROR, "LOG FROM C++ ======> ", str_size);
    signed char *b_array = (signed char*)malloc((int)array_size);
    env->GetByteArrayRegion(array, 0, (int)array_size, b_array);
    unsigned char *rb_array = (unsigned char*)b_array;
    frameNode_t *frame = (frameNode_t *)(malloc(sizeof(frameNode_t)));
    frame->yuv_array = rb_array;
    frame->arr_size = (int)array_size;
    frame->width = (int)width;
    frame->height = (int)height;

    if(frames_list->size > 120){
        frameLinkedList_removeFirst(frames_list);
    }
    frameLinkedList_push(frames_list, frame);
    linkedlist_t *l = new_linkedlist();
    return 1;

}