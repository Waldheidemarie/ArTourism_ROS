//
// Created by iFinVer on 2016/12/13.
//

#include "UnityTransfer.h"
#include "log.h"
#include <cmath>
#include <string>

#define LOG_TAG "UnityTransfer"

UnityTransfer::UnityTransfer() {
    this->mUnityMsg = new UnityMsg();
    this->mFaceMsg = new FaceMsg();
}

void UnityTransfer::setTransferByUnity(UnityTransfer::Transfer transfer) {
    this->mTransfer = transfer;
}

void UnityTransfer::setFaceTransferByUnity(FaceTransfer transfer) {
    this->mFaceTransfer = transfer;
}

void UnityTransfer::setMonalisaTransferByUnity(MonalisaTransfer transfer) {
    this->mMonalisaTransfer = transfer;
}

#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
void UnityTransfer::setVideoRecordActionTransferByUnity(VideoRecordActionTransfer transfer) {
    this->mVideoRecordActionTransfer = transfer;
}

void UnityTransfer::setEnableBlurTransferByUnity(EnableBlurTransfer transfer) {
    this->mEnableBlurTransfer = transfer;
}

void UnityTransfer::setPauseAssetAudioTransferByUnity(PauseAssetAudioTransfer transfer) {
    this->mPauseAssetAudioTransfer = transfer;
}

void UnityTransfer::setInitAssetsLoaderTransferByUnity(InitAssetsLoaderTransfer transfer) {
    this->mInitAssetsLoaderTransfer = transfer;
}

void UnityTransfer::setSelectAssetTransferByUnity(SelectAssetTransfer transfer) {
    this->mSelectAssetTransfer = transfer;
}

void UnityTransfer::setCleanUpAssetsCacheTransferByUnity(CleanUpAssetsCacheTransfer transfer) {
    this->mCleanUpAssetsCacheTransfer = transfer;
}
#endif

std::string UnityTransfer::transformToUnity(jbyte *yuvData, int width, int height, int degree, jboolean mirror, jlong facePtr) {
    //宽高改变时重新初始化
    //640*360

    if (mUnityMsg->yPtr == nullptr || mUnityMsg->uvPtr == nullptr || mUnityMsg->width != width || mUnityMsg->height != height) {
        mUnityMsg->width = width;
        mUnityMsg->height = height;
        if (mUnityMsg->uvPtr != nullptr) {
            delete[] mUnityMsg->uvPtr;
        }
        if(mUnityMsg->yPtr != nullptr){
            delete[] mUnityMsg->yPtr;
        }
        mUnityMsg->uvPtr = new unsigned char[width * height * 3 / 4];
        mUnityMsg->yPtr = new unsigned char[width * height];
    }
    //处理uv通道
    int yLen = width * height;
    int yuvLen = yLen * 3 / 2;
    int dstPtr = 0;
  // for (int i = yLen; i < yuvLen; i += 2) {
  //     mUnityMsg->uvPtr[dstPtr++] = (unsigned char) (yuvData[i] & 0xFF);
  //     mUnityMsg->uvPtr[dstPtr++] = (unsigned char) (yuvData[i + 1] & 0xFF);
  //     mUnityMsg->uvPtr[dstPtr++] = 0;
  // }
//
  // for(int j = 0;j<yLen;j++){
  //     mUnityMsg->yPtr[j] = (unsigned char)(yuvData[j] & 0xFF);
  // }

    //更改数据流使得图像朝向为正
    jbyte tempData;
    for (int i = 0; i < height * 3 / 2; i++) {
        for (int j = 0; j < width / 2; j++) {
            tempData = yuvData[i * width + j];
            yuvData[i * width + j] = yuvData[(i + 1) * width - 1 - j];
            yuvData[(i + 1) * width - 1 - j] = tempData;
        }
    }


    int Ly = 0;
    for(int i = 0;i<480;i++) {
        if (i>59&&i<420) {
        for (int k = 0; k < 640; k++) {

                mUnityMsg->yPtr[Ly] = (unsigned char) (yuvData[Ly] & 0xFF)  ;
                Ly++;

        }
    }else {
            for (int k = 0; k < 640; k++) {

                mUnityMsg->yPtr[Ly] = 0 & 0xFF;
                Ly++;

            }
        }
    }
    int Luv = yLen;
    for (int j = 0; j < 480; j ++) {
        if(j>59&&j<420) {
            for (int z = 0; z < 160; z++) {
//                mUnityMsg->uvPtr[dstPtr++] = (unsigned char) (yuvData[Luv] & 0xFF);
//                mUnityMsg->uvPtr[dstPtr++] = (unsigned char) (yuvData[Luv+1] & 0xFF);
                mUnityMsg->uvPtr[dstPtr++] = (unsigned char) (yuvData[Luv+1] & 0xFF);
                mUnityMsg->uvPtr[dstPtr++] = (unsigned char) (yuvData[Luv] & 0xFF);
                mUnityMsg->uvPtr[dstPtr++] = 0;
                Luv += 2;

            }
        }else {
            for (int z = 0; z < 160; z++) {
                mUnityMsg->uvPtr[dstPtr++] = 0 & 0xFF;
                mUnityMsg->uvPtr[dstPtr++] = 0 & 0xFF;
                mUnityMsg->uvPtr[dstPtr++] = 0;
                Luv += 2;
            }

        }
    }

  //  mUnityMsg->width = 320;
  //  mUnityMsg->height = 640;
    //y通道赋值
    //mUnityMsg->yPtr = yuvData;
    //旋转角度
    mUnityMsg->degree = degree;
    //翻转控制
    mUnityMsg->mirror = mirror ? 1 : 0;
    //传送给Unity
    std::string transformResult = transform();

    transformResult = "yLen:";

    return transformResult;
}

