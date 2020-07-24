
#ifndef FINENGINE_MAIN_H
#define FINENGINE_MAIN_H

#include "UnityTransfer.h"

extern "C" {

JNIEXPORT jstring JNICALL Java_com_cetc15_datatransfer_DataTransfer_testOutput(JNIEnv* env, jobject);

JNIEXPORT jstring JNICALL Java_com_cetc15_datatransfer_DataTransfer_onVideoBuffer(JNIEnv *, jclass, jbyteArray, jint, jint, jint,jboolean,jlong);

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_onMonalisaData(JNIEnv *env, jclass type, jlong msgPtr);

#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_initAssetsLoader(JNIEnv *env, jclass type, jstring json_);

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_loadAsset(JNIEnv *env, jclass type, jint assetId);

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_setRecordAction(JNIEnv *env, jclass type, jint recordAction);

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_enableBlur(JNIEnv *env, jclass type, jboolean isEnable);

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_pauseAssetAudio(JNIEnv *env, jclass type, jboolean isPause);

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_cleanUpAssetsCache(JNIEnv *env, jclass type);
#endif
/**
 * will be invoked by unity's scripts
 */
void UnitySetCameraRenderFuc(UnityTransfer::Transfer);
void setTransferByUnity(UnityTransfer::Transfer);

//void setFaceTransferByUnity(UnityTransfer::FaceTransfer);
void UnitySetOnFaceDetectedFuc(UnityTransfer::FaceTransfer);
#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
void setMonalisaCallbackByUnity(UnityTransfer::MonalisaTransfer);

void UnitySetVideoRecordingFuc(UnityTransfer::VideoRecordActionTransfer);

void UnitySetEnableBlurFuc(UnityTransfer::EnableBlurTransfer);

void UnitySetPauseAssetAudioFuc(UnityTransfer::PauseAssetAudioTransfer);

void UnitySetSelectAssetObjectFuc(UnityTransfer::SelectAssetTransfer);

void UnitySetCleanUpAssetsCacheFuc(UnityTransfer::CleanUpAssetsCacheTransfer);

void UnitySetInitAssetsLoaderFuc(UnityTransfer::InitAssetsLoaderTransfer);
#endif
}

#endif //FINENGINE_MAIN_H
