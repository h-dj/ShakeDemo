### 一、参考资料
- http://www.jianshu.com/p/bc5298651b30#
- https://developer.android.google.cn/reference/android/hardware/SensorEvent.html

### 效果图：
- ![image](https://github.com/naughty-pig/ShakeDemo/blob/master/gif/shake.gif)

### 二、加速度传感器的基本使用
1. 获取传感器的管理者
```
 //获取传感器管理者服务
 SensorManager  manager = (SensorManager) getSystemService(SENSOR_SERVICE);
```
2. 获取加速度传感器
```
 //获取加速度传感器
Sensor  mAccelerometerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
```
3. 注册监听
```
//注册加速度传感器;
第一个参数：SensorEventListener listener--传感器事件监听
第二个参数：Sensor sensor--要注册的传感器
第三个参数：int samplingPeriodUs--传感器的灵敏度
manager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
```

4. 反注册
```
 manager.unregisterListener(this);
 //@see #unregisterListener(SensorEventListener)
// @see #unregisterListener(SensorEventListener, Sensor)

```

#### 三、传感器监听接口SensorEventListener
1.
```
//当Sensor值发生变化时，调用
 public void onSensorChanged(SensorEvent event);

 //被注册传感器的精进度发生改变时被调用
 public void onAccuracyChanged(Sensor sensor, int accuracy);
```

#### 四、例子（微信摇一摇）
1. 布局
````
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ff222222"
    android:orientation="vertical"
    tools:context=".MainActivity">
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <!--摇一摇中心图片-->
        <ImageView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:src="@mipmap/weichat_icon"/>
        <LinearLayout
            android:gravity="center"
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_alignParentTop="true"
            android:layout_alignParentLeft="true"
            android:layout_alignParentStart="true">
            <!--顶部的横线和图片-->
            <LinearLayout
                android:gravity="center_horizontal|bottom"
                android:id="@+id/main_linear_top"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">
                <ImageView
                    android:src="@mipmap/shake_top"
                    android:id="@+id/main_shake_top"
                    android:layout_width="wrap_content"
                    android:layout_height="100dp"/>
                <ImageView
                    android:background="@mipmap/shake_top_line"
                    android:id="@+id/main_shake_top_line"
                    android:visibility="gone"
                    android:layout_width="match_parent"
                    android:layout_height="5dp"/>
            </LinearLayout>
            <!--底部的横线和图片-->
            <LinearLayout
                android:gravity="center_horizontal|bottom"
                android:id="@+id/main_linear_bottom"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">
                <ImageView
                    android:visibility="gone"
                    android:background="@mipmap/shake_bottom_line"
                    android:id="@+id/main_shake_bottom_line"
                    android:layout_width="match_parent"
                    android:layout_height="5dp"/>
                <ImageView
                    android:src="@mipmap/shake_bottom"
                    android:id="@+id/main_shake_bottom"
                    android:layout_width="wrap_content"
                    android:layout_height="100dp"/>
            </LinearLayout>
        </LinearLayout>
    </RelativeLayout>
</LinearLayout>
````
2. 注册和反注册
```
 @Override
    protected void onStart() {
        super.onStart();
        //获取传感器管理者服务
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (manager != null) {
            //获取加速度传感器
            mAccelerometerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mAccelerometerSensor != null) {
                //注册加速度传感器
                manager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }
    @Override
    protected void onPause() {
        // 务必要在pause中注销 mSensorManager
        // 否则会造成界面退出后摇一摇依旧生效的bug
        if (manager != null) {
            manager.unregisterListener(this);
        }
        super.onPause();
    }

```

3. 摇一摇动画
```
 /**
     * 开启 摇一摇动画
     *
     * @param isBack 是否是返回初识状态
     */
    private void startAnimation(boolean isBack) {
        //动画坐标移动的位置的类型是相对自己的
        int type = Animation.RELATIVE_TO_SELF;

        float topFromY;
        float topToY;
        float bottomFromY;
        float bottomToY;
        if (isBack) {
            topFromY = -0.5f;
            topToY = 0;
            bottomFromY = 0.5f;
            bottomToY = 0;
        } else {
            topFromY = 0;
            topToY = -0.5f;
            bottomFromY = 0;
            bottomToY = 0.5f;
        }

        //上面图片的动画效果
        TranslateAnimation topAnim = new TranslateAnimation(
                type, 0, type, 0, type, topFromY, type, topToY
        );
        topAnim.setDuration(200);
        //动画终止时停留在最后一帧~不然会回到没有执行之前的状态
        topAnim.setFillAfter(true);

        //底部的动画效果
        TranslateAnimation bottomAnim = new TranslateAnimation(
                type, 0, type, 0, type, bottomFromY, type, bottomToY
        );
        bottomAnim.setDuration(200);
        bottomAnim.setFillAfter(true);

        //大家一定不要忘记, 当要回来时, 我们中间的两根线需要GONE掉
        if (isBack) {
            bottomAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //当动画结束后 , 将中间两条线GONE掉, 不让其占位
                    top_line.setVisibility(View.GONE);
                    bottom_line.setVisibility(View.GONE);
                }
            });
        }
        //设置动画
        top.startAnimation(topAnim);
        bottom.startAnimation(bottomAnim);
    }
```

4. 接口回调
```
 @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                //获取加速度传感器事件的定义的，x,y,z坐标
                //不同的传感器values的值和长度不同
                float[] values = event.values;
                float x = values[0];
                float y = values[0];
                float z = values[0];
                if (Math.abs(x) > 17 && Math.abs(y) > 17 && Math.abs(z) > 17 && !isShake) {
                    Log.e("SensorEvent", ":" + type + ":" + x + ":" + y + ":" + z + ":");
                    isShake = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //开始摇一摇;//开始震动 发出提示音 展示动画效果
                            mHandler.obtainMessage(START_SHAKE).sendToTarget();
                            //再来一次震动提示
                            mHandler.obtainMessage(AGAIN_SHAKE).sendToTarget();
                            mHandler.obtainMessage(END_SHAKE).sendToTarget();
                        }
                    }).start();
                }
                break;
        }
    }
```
5. 声音和震动
```
//初始化SoundPool
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        //加载音频文件
        mWeiChatAudio = mSoundPool.load(this, R.raw.weichat_audio, 1);
        //获取Vibrator震动服务
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
```
6. Handler更新UI
```

 private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_SHAKE:
                    Log.e("handlerMsg", "START_SHAKE");
                    //震动0.5秒
                    mVibrator.vibrate(500);
                    //播放声音
                    mSoundPool.play(mWeiChatAudio, 1, 1, 0, 0, 1);
                    //显示布局中的分割线
                    top_line.setVisibility(View.VISIBLE);
                    bottom_line.setVisibility(View.VISIBLE);
                    //开始布局往上下的动画
                    startAnimation(false);
                    break;
                case AGAIN_SHAKE:
                    Log.e("handlerMsg", "AGAIN_SHAKE");
                    mVibrator.vibrate(500);
                    break;
                case END_SHAKE:
                    Log.e("handlerMsg", "END_SHAKE");
                    isShake=false;
                    startAnimation(true);
                    break;
            }
            super.handleMessage(msg);
        }
    };

```