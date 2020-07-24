/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.view.camera;

import com.google.common.base.Preconditions;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import org.ros.exception.RosRuntimeException;

import java.util.Iterator;
import java.io.IOException;
import java.util.List;

import com.cetc15.datatransfer.DataTransfer;
import com.unity3d.player.UnityPlayer;

/**
 * Displays preview frames from the camera.
 *
 * @author damonkohler@google.com (Damon Kohler)
 */
public class CameraPreviewView extends ViewGroup {

  private final static double ASPECT_TOLERANCE = 0.1;

  private SurfaceHolder surfaceHolder;
  private Camera camera;
  private Size previewSize;
  private byte[] previewBuffer;
  private RawImageListener rawImageListener;
  private BufferingPreviewCallback bufferingPreviewCallback;

  //Unity相关内容
  public DataTransfer dt;

  //!!!byte[] data数据
  private final class BufferingPreviewCallback implements PreviewCallback {
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
      Preconditions.checkArgument(camera == CameraPreviewView.this.camera);
      Preconditions.checkArgument(data == previewBuffer);
      if (rawImageListener != null) {
        rawImageListener.onNewRawImage(data, previewSize);
      }

      //Log.i("dataTransfer",data[0] + "~~~~~");

      //Unity传输图像数据
      //DataTransfer.onVideoBuffer(data,640,480,0,false,0);
      DataTransfer.onVideoBuffer(previewBuffer,previewSize.width,previewSize.height,0,false,0);

      camera.addCallbackBuffer(previewBuffer);
    }
  }

  private final class SurfaceHolderCallback implements SurfaceHolder.Callback {
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      try {
        if (camera != null) {
          camera.setPreviewDisplay(holder);
        }
      } catch (IOException e) {
        throw new RosRuntimeException(e);
      }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
      releaseCamera();
    }
  }

  private void init(Context context) {
    SurfaceView surfaceView = new SurfaceView(context);
    addView(surfaceView);
    surfaceHolder = surfaceView.getHolder();
    surfaceHolder.addCallback(new SurfaceHolderCallback());
    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    bufferingPreviewCallback = new BufferingPreviewCallback();
  }

  public CameraPreviewView(Context context) {
    super(context);
    init(context);
  }

  public CameraPreviewView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public CameraPreviewView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  public void releaseCamera() {
    if (camera == null) {
      return;
    }
    camera.setPreviewCallbackWithBuffer(null);
    camera.stopPreview();
    camera.release();
    camera = null;
  }

  public void setRawImageListener(RawImageListener rawImageListener) {
    this.rawImageListener = rawImageListener;
  }

  public Size getPreviewSize() {
    return previewSize;
  }

  public void setCamera(Camera camera) {
    Preconditions.checkNotNull(camera);
    this.camera = camera;
    setupCameraParameters();
    setupBufferingPreviewCallback();

    camera.startPreview();
    camera.setDisplayOrientation(90);

    try {
      // This may have no effect if the SurfaceHolder is not yet created.
      camera.setPreviewDisplay(surfaceHolder);
    } catch (IOException e) {
      throw new RosRuntimeException(e);
    }
  }

  private void setupCameraParameters() {
    Camera.Parameters parameters = camera.getParameters();
    //List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
    this.previewSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), getWidth(), getHeight());
    //previewSize = getOptimalPreviewSize(supportedPreviewSizes, getWidth(), getHeight());
    parameters.setPreviewSize(previewSize.width, previewSize.height);
    parameters.setPreviewFormat(ImageFormat.NV21);
    camera.setParameters(parameters);
  }

  private Size getOptimalPreviewSize(List<Size> paramList, int paramInt1, int paramInt2) {
    Preconditions.checkNotNull(paramList);
    double targetRatio = (double) paramInt1 / paramInt2;
    double minimumDifference = Double.MAX_VALUE;
    Size optimalSize = null;

    // Try to find a size that matches the aspect ratio and size.
    Size localObject = null;
    Iterator localIterator1 = paramList.iterator();
    while (localIterator1.hasNext())
    {
      Camera.Size localSize2 = (Camera.Size)localIterator1.next();
      if ((localSize2.height == 480) && (localSize2.width == 640))
        localObject = localSize2;
    }
    if (localObject == null)
    {
      double d = Double.MAX_VALUE;
      Iterator localIterator2 = paramList.iterator();
      while (localIterator2.hasNext())
      {
        Camera.Size localSize1 = (Camera.Size)localIterator2.next();
        if (Math.abs(localSize1.height - paramInt2) < d)
        {
          localObject = localSize1;
          d = Math.abs(localSize1.height - paramInt2);
        }
      }
    }
    Preconditions.checkNotNull(localObject);
    return localObject;
  }

  private void setupBufferingPreviewCallback() {
    int format = camera.getParameters().getPreviewFormat();
    int bits_per_pixel = ImageFormat.getBitsPerPixel(format);
    previewBuffer = new byte[previewSize.height * previewSize.width * bits_per_pixel / 8];
    camera.addCallbackBuffer(previewBuffer);
    camera.setPreviewCallbackWithBuffer(bufferingPreviewCallback);
  }

  @Override
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    View localView;
    int i;
    int j;
    int k;
    int m;
    if ((paramBoolean) && (getChildCount() > 0))
    {
      localView = getChildAt(0);
      i = paramInt3 - paramInt1;
      j = paramInt4 - paramInt2;
      k = i;
      m = j;
      if (this.previewSize != null)
      {
        k = this.previewSize.width;
        m = this.previewSize.height;
      }
      if (i * m > j * k)
      {
        int i1 = k * j / m;
        localView.layout((i - i1) / 2, 0, (i + i1) / 2, j);
      }
    }
    else
    {
      return;
    }
    int n = m * i / k;
    localView.layout(0, (j - n) / 2, i, (j + n) / 2);
  }

}
