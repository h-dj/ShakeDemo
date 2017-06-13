package com.example.h_dj.shakedemo;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int START_SHAKE = 1;
    private static final int AGAIN_SHAKE = 2;
    private static final int END_SHAKE = 3;
    private SensorManager manager;
    private Sensor mAccelerometerSensor;
    private boolean isShake;
    private LinearLayout top;
    private LinearLayout bottom;
    private ImageView top_line;
    private ImageView bottom_line;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_SHAKE:
                    Log.e("handlerMsg", "START_SHAKE");
                    mVibrator.vibrate(500);
                    mSoundPool.play(mWeiChatAudio, 1, 1, 0, 0, 1);
                    top_line.setVisibility(View.VISIBLE);
                    bottom_line.setVisibility(View.VISIBLE);
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

    private SoundPool mSoundPool;
    private int mWeiChatAudio;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
    }

    private void initView() {
        top = (LinearLayout) findViewById(R.id.main_linear_top);
        bottom = (LinearLayout) findViewById(R.id.main_linear_bottom);
        top_line = (ImageView) findViewById(R.id.main_shake_top_line);
        bottom_line = (ImageView) findViewById(R.id.main_shake_bottom_line);

    }

    private void init() {
        //初始化SoundPool
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        //加载音频文件
        mWeiChatAudio = mSoundPool.load(this, R.raw.weichat_audio, 1);
        //获取Vibrator震动服务
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }


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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.e("onAccuracyChanged", ":" + sensor.getName());
    }
}
