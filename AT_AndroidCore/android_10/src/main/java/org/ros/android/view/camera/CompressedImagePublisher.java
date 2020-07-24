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


import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera.Size;

import com.google.common.base.Preconditions;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;

import org.ros.internal.message.MessageBuffers;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;
import org.ros.namespace.NodeNameResolver;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import sensor_msgs.CameraInfo;
import sensor_msgs.CompressedImage;
import std_msgs.Header;

/**
 * Publishes preview frames.
 *
 * @author damonkohler@google.com (Damon Kohler)
 */
class CompressedImagePublisher implements RawImageListener {


  private final Publisher<sensor_msgs.CameraInfo> cameraInfoPublisher;
  private final ConnectedNode connectedNode;
  private final Publisher<sensor_msgs.CompressedImage> imagePublisher;
  private byte[] rawImageBuffer;
  private Size rawImageSize;
  private Rect rect;
  private ChannelBufferOutputStream stream;
  private YuvImage yuvImage;



  //设置发布topic
  public CompressedImagePublisher(ConnectedNode connectedNode) {
    this.connectedNode = connectedNode;
    NameResolver resolver = connectedNode.getResolver().newChild("android");
    imagePublisher = connectedNode.newPublisher(resolver.resolve("image/compressed"), sensor_msgs.CompressedImage._TYPE);
    cameraInfoPublisher = connectedNode.newPublisher(resolver.resolve("camera_info"), sensor_msgs.CameraInfo._TYPE);
    stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());
  }

  //发布图片数据（CompressedImage类型）
  @Override
  public void onNewRawImage(byte[] paramArrayOfByte, Size paramSize) {
    Preconditions.checkNotNull(paramArrayOfByte);
    Preconditions.checkNotNull(paramSize);
    if ((paramArrayOfByte != this.rawImageBuffer) || (!paramSize.equals(this.rawImageSize)))
    {
      this.rawImageBuffer = paramArrayOfByte;
      this.rawImageSize = paramSize;
      this.yuvImage = new YuvImage(this.rawImageBuffer, 17, paramSize.width, paramSize.height, null);
      this.rect = new Rect(0, 0, paramSize.width, paramSize.height);
    }

    Time localTime = this.connectedNode.getCurrentTime();
    CompressedImage localCompressedImage = (CompressedImage)this.imagePublisher.newMessage();
    localCompressedImage.setFormat("jpeg");
    localCompressedImage.getHeader().setStamp(localTime);
    localCompressedImage.getHeader().setFrameId("camera");
    Preconditions.checkState(this.yuvImage.compressToJpeg(this.rect, 20, this.stream));
    localCompressedImage.setData(this.stream.buffer().copy());
    this.stream.buffer().clear();
    this.imagePublisher.publish(localCompressedImage);
    CameraInfo localCameraInfo = (CameraInfo)this.cameraInfoPublisher.newMessage();
    localCameraInfo.getHeader().setStamp(localTime);
    localCameraInfo.getHeader().setFrameId("camera");
    localCameraInfo.setWidth(paramSize.width);
    localCameraInfo.setHeight(paramSize.height);
    this.cameraInfoPublisher.publish(localCameraInfo);

  }
}