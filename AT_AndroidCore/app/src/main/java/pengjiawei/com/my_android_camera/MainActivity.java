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

package pengjiawei.com.my_android_camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.FaceDetector;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.ros.android.RosActivity;
import org.ros.android.view.camera.RosCameraPreviewView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import org.ros.android.view.RosTextView;
import org.ros.android.MessageCallable;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cetc15.datatransfer.DataTransfer;
import com.unity3d.player.UnityPlayer;

import static android.content.ContentValues.TAG;


/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends RosActivity {

    public DataTransfer dt;

    //ROS相关定义
    public static long start_time;
    public static Handler handler;

    private int cameraId;
    private ImuPublisher imu_pub;
    private RosCameraPreviewView rosCameraPreviewView;
    private SensorManager mSensorManager;
    private RosTextView<std_msgs.String> rosTextView;

    //Unity相关定义
    protected MyUnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    private LinearLayout unityLayout;
    private Button secondButton;



    public MainActivity()
    {
        super("CameraTutorial", "CameraTutorial");
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        this.cameraId = 0;
        this.rosCameraPreviewView.setCamera(Camera.open(this.cameraId));
        try {
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();
            NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());
            nodeMainExecutor.execute(rosCameraPreviewView, nodeConfiguration);

            NodeConfiguration nodeConfiguration2 = NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());
            this.imu_pub = new ImuPublisher(this.mSensorManager);
            nodeMainExecutor.execute(this.imu_pub, nodeConfiguration2);

            //启动rosTexView节点，该节点的作用是接受string_test这个topic并将接受的字符串显示出来
            nodeMainExecutor.execute(rosTextView, nodeConfiguration);
        }
        catch (IOException e) {
            // Socket problem
            Log.e("Camera Tutorial", "socket error trying to get networking information from the master uri");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        unityLayout = findViewById(R.id.unityLayout);

        getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy
        // 创建Unity视图
        mUnityPlayer = new MyUnityPlayer(this);
        // 添加Unity视图
        unityLayout.addView(mUnityPlayer.getView());
        mUnityPlayer.requestFocus();

        rosCameraPreviewView = (RosCameraPreviewView) findViewById(R.id.ros_camera_preview_view);
        this.mSensorManager = ((SensorManager)getSystemService(Context.SENSOR_SERVICE));
        rosTextView = (RosTextView<std_msgs.String>) findViewById(R.id.text);
        rosTextView.setTopicName("string_test");
        rosTextView.setMessageType(std_msgs.String._TYPE);
        rosTextView.setMessageToStringCallable(new MessageCallable<String, std_msgs.String>() {
            @Override
            public String call(std_msgs.String message) {
                return message.getData();
            }
        });

    }

    //unity调用Android
    public void UnityCallAndroid () {
        Toast.makeText(this,"unity调用android成功", Toast.LENGTH_LONG).show();
        AndroidCallUnity();
    }

    //android调用unity
    public void AndroidCallUnity () {
        //第1个参数为Unity场景中用于接收android消息的对象名称
        //第2个参数为对象上的脚本的一个成员方法名称（脚本名称不限制）
        //第3个参数为unity方法的参数
        String val = DataTransfer.testOutput();
        UnityPlayer.UnitySendMessage("receiveObj", "UnityMethod", val);

    }

    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            ImageHandler(reader);
        }
    };

    private void ImageHandler(ImageReader reader){
        // 获取当前帧图片，并检测图片是否有效
        Image image = reader.acquireNextImage();
        float time = image.getTimestamp();
        if (image == null) {
            Log.e(TAG, "camera image is null");
            return;
        }
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            Log.e(TAG, "camera image is in wrong format");
            return;
        }

        // 获取图片的基本信息
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] data = ImageUtil.getBytesFromImageAsType(image,ImageUtil.NV21);

        byte[] imageResult;

    }


    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        mUnityPlayer.quit();
        super.onDestroy();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();
    }

    @Override protected void onStart()
    {
        super.onStart();
        mUnityPlayer.start();
    }

    @Override protected void onStop()
    {
        super.onStop();
        mUnityPlayer.stop();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }


}
