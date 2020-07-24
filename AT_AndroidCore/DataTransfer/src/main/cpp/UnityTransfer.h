//
// Created by iFinVer on 2016/12/13.
//

#ifndef FINENGINE_UNITYCONNECTOR_H
#define FINENGINE_UNITYCONNECTOR_H

#include <jni.h>
#include <string>

//#define USE_UNITY_NATIVE_SEND_MESSAGE false

class UnityTransfer{
public:
    typedef struct UnityTransferMessage{
        int degree;
        int width;
        int height;
        int mirror;
        //void* yPtr;
        unsigned char* yPtr = nullptr;
        unsigned char* uvPtr = nullptr;
    } UnityMsg;
    typedef void (*Transfer)(void * intPtr);

    typedef struct UnityFaceMessage{
        int degree;
        int width;
        int height;
        int mirror;
        int faceCount;
        void* faceOutlinePoint;
        void* faceDetectRect;
        void* faceOrientation;
    }FaceMsg;
    typedef void (*FaceTransfer)(void * intPtr);

    typedef struct MonalisaMessage{
        int width;
        int height;
        int texSize;
        unsigned char* texData;
    }MonalisaMsg;
    typedef void (*MonalisaTransfer)(void * intPtr);

#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
    enum RecordingAction
    {
        None = -1,
        Start = 0,
        Stop = 1,
        Cancel = 2,
    };

    typedef void (*VideoRecordActionTransfer)(int action);

    typedef void (*EnableBlurTransfer)(bool enable);

    typedef void (*PauseAssetAudioTransfer)(bool pause);

    typedef void (*InitAssetsLoaderTransfer)(char *json);

    typedef void (*SelectAssetTransfer)(int assetID);

    typedef void (*CleanUpAssetsCacheTransfer)();

#endif

    UnityTransfer();
    void setTransferByUnity(Transfer);
    void setFaceTransferByUnity(FaceTransfer);
    void setMonalisaTransferByUnity(MonalisaTransfer);
#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
    void setVideoRecordActionTransferByUnity(VideoRecordActionTransfer);
    void setEnableBlurTransferByUnity(EnableBlurTransfer);
    void setPauseAssetAudioTransferByUnity(PauseAssetAudioTransfer);
    void setInitAssetsLoaderTransferByUnity(InitAssetsLoaderTransfer);
    void setSelectAssetTransferByUnity(SelectAssetTransfer);
    void setCleanUpAssetsCacheTransferByUnity(CleanUpAssetsCacheTransfer);
#endif
    std::string transformToUnity(jbyte*,int width,int height,int degree,jboolean mirror,jlong facePtr);
    //void transformMonalisa(jlong monalisaMsgPtr);

    //will never be destroyed after app starting.
//    ~UnityConnector();
#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
    inline void transferEnableBlur(bool enable){
        if(mEnableBlurTransfer != nullptr){
            mEnableBlurTransfer(enable);
        }
    }

    inline void transferVideoRecordingAction(RecordingAction action){
        if(mVideoRecordActionTransfer != nullptr){
            mVideoRecordActionTransfer(action);
        }
    }

    inline void transferPauseAssetAudio(bool enable){
        if(mPauseAssetAudioTransfer != nullptr){
            mPauseAssetAudioTransfer(enable);
        }
    }

    inline void transferInitAssetsLoader(char *json){
        if(mInitAssetsLoaderTransfer != nullptr){
            mInitAssetsLoaderTransfer(json);
        }
    }

    inline void transferSelectAsset(jint assetID){
        if(mSelectAssetTransfer != nullptr){
            mSelectAssetTransfer(assetID);
        }
    }

    inline void transferCleanUpAssetsCache(){
        if(mCleanUpAssetsCacheTransfer != nullptr){
            mCleanUpAssetsCacheTransfer();
        }
    }
#endif

private:
    UnityMsg *mUnityMsg = nullptr;
    Transfer mTransfer = nullptr;

    FaceMsg *mFaceMsg = nullptr;
    FaceTransfer mFaceTransfer = nullptr;

#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
    //调用Unity方法
    EnableBlurTransfer mEnableBlurTransfer = nullptr;

    VideoRecordActionTransfer mVideoRecordActionTransfer = nullptr;

    PauseAssetAudioTransfer mPauseAssetAudioTransfer = nullptr;

    InitAssetsLoaderTransfer mInitAssetsLoaderTransfer = nullptr;

    SelectAssetTransfer mSelectAssetTransfer = nullptr;

    CleanUpAssetsCacheTransfer mCleanUpAssetsCacheTransfer = nullptr;
#endif
//    MonalisaMsg *mMonalisaMsg = nullptr;
    MonalisaTransfer  mMonalisaTransfer = nullptr;

    inline std::string transform(){
        if(mTransfer != nullptr && mUnityMsg != nullptr){
            mTransfer(mUnityMsg);
            return "suc";
        }
        return "fail";
    }
};

#endif //FINENGINE_UNITYCONNECTOR_H
