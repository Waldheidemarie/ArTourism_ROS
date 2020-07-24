//
// Created by iFinVer on 2016/12/13.
//

#include "main.h"
#include "UnityTransfer.h"
#include <string>


UnityTransfer *mConnector = nullptr;

extern "C" JNIEXPORT jstring JNICALL
Java_com_cetc15_datatransfer_DataTransfer_testOutput(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from nativeLib-main.cpp";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_cetc15_datatransfer_DataTransfer_onVideoBuffer(
        JNIEnv *env, jclass , jbyteArray data_, jint frameWidth, jint frameHeight, jint degree,jboolean mirror,jlong facePtr) {
    std::string result = "notSend";

    jbyte *data = env->GetByteArrayElements(data_, 0);
    if(mConnector != nullptr){
        result = mConnector->transformToUnity(data,frameWidth,frameHeight,degree,mirror,facePtr);
    }
    env->ReleaseByteArrayElements(data_, data, JNI_ABORT);
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_initAssetsLoader(JNIEnv *env, jclass type, jstring json_) {
#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
    char *json = (char *) env->GetStringUTFChars(json_, 0);
    if(mConnector != nullptr){
        mConnector->transferInitAssetsLoader(json);
    }
    env->ReleaseStringUTFChars(json_, json);
#endif
}

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_loadAsset(JNIEnv *env, jclass type, jint assetId) {
#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
    if(mConnector != nullptr){
        mConnector->transferSelectAsset(assetId);
    }
#endif
}

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_setRecordAction(JNIEnv *env, jclass type, jint recordAction) {
#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
    if(mConnector != nullptr){
        mConnector->transferVideoRecordingAction((UnityTransfer::RecordingAction) recordAction);
    }
#endif
}

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_enableBlur(JNIEnv *env, jclass type,
                                                        jboolean isEnable) {
#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
    if(mConnector != nullptr){
        mConnector->transferEnableBlur(isEnable);
    }
#endif
}

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_pauseAssetAudio(JNIEnv *env, jclass type,
                                                             jboolean isPause) {
#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
    if(mConnector != nullptr){
        mConnector->transferPauseAssetAudio(isPause);
    }
#endif
}

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_cleanUpAssetsCache(JNIEnv *env, jclass type) {
#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
    if(mConnector != nullptr){
        mConnector->transferCleanUpAssetsCache();
    }
#endif
}

JNIEXPORT void JNICALL
Java_com_cetc15_datatransfer_DataTransfer_onMonalisaData(JNIEnv *env, jclass type, jlong msgPtr) {}

void setTransferByUnity(UnityTransfer::Transfer transfer) {
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setTransferByUnity(transfer);
}

void UnitySetCameraRenderFuc(UnityTransfer::Transfer transfer) {
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setTransferByUnity(transfer);
}

void UnitySetOnFaceDetectedFuc(UnityTransfer::FaceTransfer transfer){
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setFaceTransferByUnity(transfer);
}

void setMonalisaCallbackByUnity(UnityTransfer::MonalisaTransfer transfer){
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setMonalisaTransferByUnity(transfer);
}
#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
void UnitySetVideoRecordingFuc(UnityTransfer::VideoRecordActionTransfer transfer) {
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setVideoRecordActionTransferByUnity(transfer);
};

void UnitySetEnableBlurFuc(UnityTransfer::EnableBlurTransfer transfer) {
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setEnableBlurTransferByUnity(transfer);
};

void UnitySetPauseAssetAudioFuc(UnityTransfer::PauseAssetAudioTransfer transfer) {
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setPauseAssetAudioTransferByUnity(transfer);
};

void UnitySetSelectAssetObjectFuc(UnityTransfer::SelectAssetTransfer transfer) {
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setSelectAssetTransferByUnity(transfer);
};

void UnitySetCleanUpAssetsCacheFuc(UnityTransfer::CleanUpAssetsCacheTransfer transfer) {
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setCleanUpAssetsCacheTransferByUnity(transfer);
};

void UnitySetInitAssetsLoaderFuc(UnityTransfer::InitAssetsLoaderTransfer transfer) {
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setInitAssetsLoaderTransferByUnity(transfer);
};
#endif
