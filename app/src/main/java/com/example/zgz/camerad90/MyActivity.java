package com.example.zgz.camerad90;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.Image;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GestureDetectorCompat;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MyActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, SensorEventListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private DrawerLayout mDrawerLayout;

    private static final int REQ_SYSTEM_SETTINGS = 0;
    public static final int IN_SAMPLE_SIZE = 5;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private float focusSize = 300;
    private float meteringSize = 500;
    private float previewMaxX = 1080;
    private float previewMaxY = 1440;
    private int previewSizeX = 640;
    private int previewSizeY = 480;
    private int VideoBitRate = 8000000;
    private final String TAG = MyActivity.class.getSimpleName();
    private Camera mCamera;
    private Camera.AutoFocusCallback myAutoFocusCallback = null;
    private CameraPreview mPreview;
    private FrameLayout camera_preview;
    private MediaRecorder mMediaRecorder;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int photoDegree = 0;
    private int oldPhotoDegree = 90;//表示竖屏
    private GestureDetectorCompat mDetector;
    private ImageView ivFocus;
    private ImageView ivMetering;
    private Button button_capture;
    private Button button_settings;
    private Button button_ec;
    private Button button_ecAdd;
    private Button button_ecReduce;
    private Button button_zoom;
    private Button button_shutter;
    private Button button_record;
    private Button button_lock;
    private Button button_flash;
    private Button button_focusMode;
    private Button button_close;

    private RelativeLayout layout_review;
    private ImageView imageView_review;
    private ImageView imageView_review_zoom;
    private ImageView imageView_review_fake;
    private Button button_previous;
    private Button button_delete;
    private Button button_next;
    private Button button_back;
    private int photo_angle;//标识当前显示的图片的方向信息

    private RelativeLayout layout_buttons;
    private RelativeLayout layout_draw;

    private PointF tapPoint = new PointF();//用于ivFocus的onSingleTapUp

    //以下变量用于ivFocus.setOnTouchListener
    private static final int NONE = 0;
    private static final int MOVE = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDistance;
    private int startZoom;

    //以下变量用于imageView_review.setOnTouchListener
    private PointF start1 = new PointF();

    private int captureMode = 0;
    private boolean focusing = false;
    private boolean saving = false;
    private boolean isRecording = false;

    private SharedPreferences mSharedPreferences;

    private View mDecorView;

    private int cursorPosition;

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.

        //隐藏标题栏，不隐藏导航栏，导航栏图标为小圆点
//        mDecorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        //隐藏标题栏，隐藏导航栏，
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDecorView = getWindow().getDecorView();
        hideSystemUI();
        setContentView(R.layout.activity_my);

        mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(this) ;

        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        camera_preview = (FrameLayout) findViewById(R.id.camera_preview);
        camera_preview.addView(mPreview);

        ivFocus = (ImageView) findViewById(R.id.ivFocus);
        ivMetering = (ImageView) findViewById(R.id.ivMetering);
        button_capture = (Button) findViewById(R.id.button_capture);
        button_settings = (Button) findViewById(R.id.button_settings);
        button_record = (Button) findViewById(R.id.button_record);

        button_ec = (Button) findViewById(R.id.button_ec);
        button_ecAdd = (Button) findViewById(R.id.button_ecAdd);
        button_ecReduce = (Button) findViewById(R.id.button_ecReduce);

        button_shutter = (Button) findViewById(R.id.button_shutter);
        button_lock = (Button) findViewById(R.id.button_lock);

        layout_review=(RelativeLayout)findViewById(R.id.layout_review);
        imageView_review = (ImageView) findViewById(R.id.imageView_review);
        imageView_review_zoom = (ImageView) findViewById(R.id.imageView_review_zoom);
        imageView_review_fake = (ImageView) findViewById(R.id.imageView_review_fake);
        button_previous = (Button) findViewById(R.id.button_previous);
        button_delete = (Button) findViewById(R.id.button_delete);
        button_next = (Button) findViewById(R.id.button_next);
        button_back = (Button) findViewById(R.id.button_back);
        photo_angle = 0;

        button_flash = (Button) findViewById(R.id.button_flash);
        layout_buttons = (RelativeLayout) findViewById(R.id.layout_buttons);
        layout_draw = (RelativeLayout) findViewById(R.id.layout_draw);
        button_zoom = (Button) findViewById(R.id.button_zoom);
        button_focusMode = (Button) findViewById(R.id.button_focusMode);
        button_close = (Button) findViewById(R.id.button_close);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_draw.getLayoutParams();
        layoutParams.width = (int) previewMaxX;
        layoutParams.height = (int) previewMaxY;
        layout_draw.setLayoutParams(layoutParams);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //如果这里设置DrawerListener，则NavigationDrawerFragment里面的ActionBarDrawerToggle会失效
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {

            }

            @Override
            public void onDrawerOpened(View view) {
                mCamera.stopPreview();
                layout_review.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDrawerClosed(View view) {
                mCamera.startPreview();
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        //自动聚焦变量回调
        myAutoFocusCallback = new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                // TODO Auto-generated method stub
                focusing = false;

                ivFocus.clearAnimation();
                Camera.Parameters parameters = mCamera.getParameters();

                if (success)//success表示对焦成功
                {
                    //myCamera.setOneShotPreviewCallback(null);
                    drawArea(ivFocus, parameters.getFocusAreas().get(0).rect, Color.GREEN);

                    switch (captureMode) {
                        case 0:
                            break;

                        case 1:
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    //execute the task
                                    //对焦成功后,曝光设置会稍微偏高，若立即锁定曝光会导致轻微过曝，因此延迟200ms，等曝光设置正常后再锁定曝光
                                    //猜测，环境较暗时对焦，相机会自动把曝光值调高方便对焦，对焦完成后再调回正常值。
                                    //所以暗环境下如果对焦成功后立刻锁定曝光，会导致轻微过曝
                                    //可以在对焦完成后等待200ms（实际测试得出，150ms仍会导致过曝，200ms正常），等曝光设置正常后再锁定对焦，即可得到正常曝光
                                    Camera.Parameters parameters1 = mCamera.getParameters();
                                    parameters1.setAutoExposureLock(true);
                                    mCamera.setParameters(parameters1);
                                    button_lock.setVisibility(View.VISIBLE);

                                }
                            }, 200);

                            break;
                    }
                } else//未对焦成功
                {
                    drawArea(ivFocus, parameters.getFocusAreas().get(0).rect, Color.RED);
                }
            }
        };

        setUI();

    }

    private void setUI(){
        ivFocus.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mDetector.onTouchEvent(event);

                        Camera.Parameters parameters = mCamera.getParameters();

                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                start.set(event.getX(), event.getY());
                                startZoom = parameters.getZoom();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                float deltaX = event.getX() - start.x;
                                float deltaY = event.getY() - start.y;
                                float deltaZoom = (deltaX - deltaY) / 30f;
                                int zoom = startZoom + (int) deltaZoom;
                                //当zoom与当前值不同时才会设定
                                if (zoom != parameters.getZoom()) {
                                    //在zoom=16这个位置卡一下
                                    if (startZoom < 16 && zoom > 16) zoom = 16;

                                    if (zoom >= parameters.getMaxZoom())
                                        zoom = parameters.getMaxZoom();
                                    else if (zoom <= 0)
                                        zoom = 0;

                                    parameters.setZoom(zoom);
                                    mCamera.setParameters(parameters);
                                    button_zoom.setText(zoom + "");
                                    button_zoom.setVisibility(View.VISIBLE);
                                    mode = MOVE;
                                }
                            case MotionEvent.ACTION_UP:
                                if (mode == MOVE) {
                                    AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);//创建一个AlphaAnimation 对象，渐变从1->0
                                    aa.setDuration(1500);//设置持续时间
                                    aa.setFillAfter(true);//设置这个View最后的状态，由于是从1->0,所以最后的是消失状态（最后是看不到见这个View的）
                                    button_zoom.startAnimation(aa);

                                    mode = NONE;
                                }
                                break;
                        }

//                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                            case MotionEvent.ACTION_DOWN:
//                                start.set(event.getX(), event.getY());
//                                mode = MOVE;
//                                break;
//                            case MotionEvent.ACTION_UP:
//                            case MotionEvent.ACTION_POINTER_UP:
//                                mode = NONE;
//                                break;
//                            case MotionEvent.ACTION_POINTER_DOWN:
//                                oldDistance = (float) Math.sqrt((event.getX(0) - event.getX(1)) * (event.getX(0) - event.getX(1)) + (event.getY(0) - event.getY(1)) * (event.getY(0) - event.getY(1)));
//                                if (oldDistance > 10f) {
//                                    mid.set((event.getX(0)+event.getX(1))/2, (event.getY(0)+event.getY(1))/2);
//                                    mode = ZOOM;
//                                }
//                            case MotionEvent.ACTION_MOVE:
//                                if (mode == MOVE) {
//                                    float deltaX=0;
//                                    float deltaY=0;
//                                    if(event.getHistorySize()>0){
//                                        deltaX=event.getX()-event.getHistoricalX(0);
//                                        deltaY=event.getY()-event.getHistoricalY(0);
//                                    }
//                                    Camera.Parameters parameters = mCamera.getParameters();
//                                    float mx=parameters.getMeteringAreas().get(0).rect.centerX();
//                                    float my=parameters.getMeteringAreas().get(0).rect.centerY();
//                                    float[] ff = camera2touch(mx, my, previewMaxX, previewMaxY);
//                                    setMeteringArea(clamp(ff[0]+deltaX,0,previewMaxX),clamp(ff[1]+deltaY,0,previewMaxY), meteringSize);
//                                    drawArea(ivMetering, parameters.getMeteringAreas().get(0).rect, Color.BLUE);
//                                } else if (mode == ZOOM) {
//                                    float newDistance;
//                                    newDistance = (float) Math.sqrt((event.getX(0) - event.getX(1)) * (event.getX(0) - event.getX(1)) + (event.getY(0) - event.getY(1)) * (event.getY(0) - event.getY(1)));
//                                    if (newDistance > 10f) {
//                                        meteringSize+=2*(newDistance-oldDistance);
//                                        if(meteringSize<=300){ meteringSize=300; }
//                                        Camera.Parameters parameters = mCamera.getParameters();
//                                        float mx=parameters.getMeteringAreas().get(0).rect.centerX();
//                                        float my=parameters.getMeteringAreas().get(0).rect.centerY();
//                                        float[] ff = camera2touch(mx, my, previewMaxX, previewMaxY);
//                                        setMeteringArea(ff[0],ff[1], meteringSize);
//                                        drawArea(ivMetering, parameters.getMeteringAreas().get(0).rect, Color.BLUE);
//                                        oldDistance = newDistance;
//                                    }
//                                }
//                                break;
//                        }
                        return true;//这里一定要return true，否则mDetector.onTouchEvent(event)不会执行
                    }
                }
        );

        button_record.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isRecording) {
                            // stop recording and release camera
                            mMediaRecorder.stop();  // stop the recording
                            releaseMediaRecorder(); // release the MediaRecorder object
                            mCamera.lock();         // take camera access back from MediaRecorder

                            // inform the user that recording has stopped
                            button_record.setText("Rec");
                            isRecording = false;

                            setVisible(View.VISIBLE);
                            Camera.Parameters params = mCamera.getParameters();
                            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                            mCamera.setParameters(params);

                            focusing = false;
                            saving = false;


                            previewMaxX = 1080;
                            previewMaxY = 1440;
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_draw.getLayoutParams();
                            layoutParams.width = (int) previewMaxX;
                            layoutParams.height = (int) previewMaxY;
                            layout_draw.setLayoutParams(layoutParams);

                        } else {
                            drawArea2(ivFocus);
                            setVisible(View.INVISIBLE);

                            previewMaxX = 1080;
                            previewMaxY = 1920;
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_draw.getLayoutParams();
                            layoutParams.width = (int) previewMaxX;
                            layoutParams.height = (int) previewMaxY;
                            layout_draw.setLayoutParams(layoutParams);

                            focusing = true;
                            saving = true;


                            //prepareVideoRecorder之前必须releaseCamera
                            releaseCamera();

                            // initialize video camera
                            if (prepareVideoRecorder()) {
                                // Camera is available and unlocked, MediaRecorder is prepared,
                                // now you can start recording
                                mMediaRecorder.start();

                                // inform the user that recording has started
                                button_record.setText("Stop");
                                isRecording = true;
                            } else {
                                // prepare didn't work, release the camera
                                releaseMediaRecorder();
                                // inform user
                            }
                        }
                    }
                }
        );

        button_capture.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                captureMode=0;

                                break;

                            case MotionEvent.ACTION_UP:
                                float x = event.getX();
                                float y = event.getY();
                                float xx = button_capture.getWidth();
                                float yy = button_capture.getHeight();
                                if (x > 0 && x < xx && y > 0 && y < yy) {
                                    takePhoto();
                                }

                                break;
                        }
                        return true;
                    }
                }
        );

        button_shutter.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                if (focusing) {
                                    Toast.makeText(getApplicationContext(), "Focusing",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    focusing = true;

                                    float x1 = previewMaxX / 2;
                                    float y1 = previewMaxY / 2;
                                    setFocusArea(x1, y1, focusSize);
                                    //setMeteringArea(x, y, meteringSize);
                                    Camera.Parameters parameters1 = mCamera.getParameters();
                                    drawArea(ivFocus, parameters1.getFocusAreas().get(0).rect, Color.CYAN);
                                    //drawArea(ivMetering, parameters.getMeteringAreas().get(0).rect, Color.BLUE);

                                    captureMode = 1;
                                    mCamera.autoFocus(myAutoFocusCallback);
                                    layout_buttons.setVisibility(View.INVISIBLE);//开始对焦时隐藏其他按钮
                                }
                                break;

                            case MotionEvent.ACTION_UP:
                                float x = event.getX();
                                float y = event.getY();
                                float xx = button_shutter.getWidth();
                                float yy = button_shutter.getHeight();
                                //手指松开拍照，手指滑出按钮后松开则取消拍照
                                if (x > 0 && x < xx && y > 0 && y < yy) {
                                    takePhoto();
                                } else {
                                    mCamera.cancelAutoFocus();
                                    focusing = false;

                                    Camera.Parameters parameters = mCamera.getParameters();
                                    parameters.setAutoExposureLock(false);
                                    mCamera.setParameters(parameters);
                                    button_lock.setVisibility(View.INVISIBLE);

                                    layout_buttons.setVisibility(View.VISIBLE);//取消拍照后显示其他按钮
                                }


                                break;
                        }

                        return true;
                    }
                }
        );

        button_settings.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //转到Settings设置界面
                        startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), REQ_SYSTEM_SETTINGS);

//                        Camera.Parameters params = mCamera.getParameters();
//                        if (params.getPictureSize().width == 3264) {
//                            params.setPictureSize(5248, 3936);
//                            button_settings.setText("20M");
//                        } else {
//                            params.setPictureSize(3264, 2448);
//                            button_settings.setText("8M");
//                        }
//                        mCamera.setParameters(params);

                    }
                }
        );

        button_ec.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Camera.Parameters params = mCamera.getParameters();
                        params.setExposureCompensation(0);
                        mCamera.setParameters(params);
                        button_ec.setText("0");
                    }
                }
        );

        button_ecAdd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Camera.Parameters params = mCamera.getParameters();
                        int index = params.getExposureCompensation();
                        if (index < params.getMaxExposureCompensation()) index++;
                        params.setExposureCompensation(index);
                        mCamera.setParameters(params);
                        button_ec.setText(index + "");
                    }
                }
        );

        button_ecReduce.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Camera.Parameters params = mCamera.getParameters();
                        int index = params.getExposureCompensation();
                        if (index > params.getMinExposureCompensation()) index--;
                        params.setExposureCompensation(index);
                        mCamera.setParameters(params);
                        button_ec.setText(index + "");
                    }
                }
        );

        button_zoom.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Camera.Parameters params = mCamera.getParameters();
                        params.setZoom(0);
                        mCamera.setParameters(params);
                        button_zoom.setText(params.getZoom() + "");
                    }
                }
        );

        button_flash.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Camera.Parameters params = mCamera.getParameters();
                        //判断字符串是否相等使用equals方法!!!
                        if (params.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        } else {
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        }
                        mCamera.setParameters(params);
                        button_flash.setText(params.getFlashMode());
                    }
                }
        );

        button_focusMode.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Camera.Parameters params = mCamera.getParameters();
                        //判断字符串是否相等使用equals方法!!!
                        if (params.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                            params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                        }else if (params.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_MACRO)){
                            params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                        }else if (params.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_INFINITY)){
                            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        }else if (params.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        }
                        mCamera.setParameters(params);
                        button_focusMode.setText(params.getFocusMode());
                    }
                }
        );

//        imageView_review_zoom.setOnTouchListener(
//                new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//
//                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                            case MotionEvent.ACTION_DOWN:
//                                start1.set(event.getX(),event.getY());
//                                break;
//                            case MotionEvent.ACTION_UP:
//
//                                break;
//
//                            case MotionEvent.ACTION_MOVE:
//                                RectF zoomArea=new RectF();
//                                zoomArea.left=start1.x;
//                                zoomArea.top=start1.y;
//                                zoomArea.right=event.getX();
//                                zoomArea.bottom=event.getY();
//
//                                drawArea1((ImageView)findViewById(R.id.imageView_review_zoom),zoomArea,Color.GREEN);
//                                break;
//                        }
//
//                        return true;
//                    }
//                }
//        );

        imageView_review.setOnTouchListener(new mTouchListener());

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_review.setVisibility(View.INVISIBLE);
                mDrawerLayout.closeDrawers();
            }
        });

        button_previous.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setImage(-1);
                    }
                }
        );

        button_next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setImage(1);
                    }
                }
        );

        button_delete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeletePhoto();
                    }
                }
        );

        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSensorManager.unregisterListener(MyActivity.this);
                releaseMediaRecorder();
                releaseCamera();
                finish();
            }
        });

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        resumeCamera();
        setDefaultCamera();

        //resume后相机初始化了，ui没有，需要想ui恢复到与camera对应值
        Camera.Parameters params = mCamera.getParameters();
        button_ec.setText(params.getExposureCompensation()+"");
        button_flash.setText(params.getFlashMode());
        button_zoom.setText(params.getZoom()+"");
        button_focusMode.setText(params.getFocusMode());
        setImage(0);
    }

    private void resumeCamera(){
        //关键代码，实现resume后相机不会挂掉
        if (mCamera == null && camera_preview != null) {
            camera_preview.removeAllViews();
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(this, mCamera);
            camera_preview.addView(mPreview);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseMediaRecorder();
        releaseCamera();

        mSensorManager.unregisterListener(this);
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null) {
            mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void setDefaultCamera(){
        Camera.Parameters params = mCamera.getParameters();
        //默认previewSize为640*480
        params.setPreviewSize(previewSizeX, previewSizeY);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        String photo_size=mSharedPreferences.getString("photo_size","800");
        if(photo_size.equals("2070")){
            params.setPictureSize(5248, 3936);
        }else if(photo_size.equals("800")){
            params.setPictureSize(3264, 2448);
        }else if(photo_size.equals("300")){
            params.setPictureSize(2048, 1536);
        }

        String jpeg_quality=mSharedPreferences.getString("jpeg_quality","85");
        params.setJpegQuality(Integer.parseInt(jpeg_quality));

        String video_bps=mSharedPreferences.getString("video_bps","8000000");
        VideoBitRate=Integer.parseInt(video_bps);

        mCamera.setParameters(params);
        setFocusArea(previewMaxX / 2, previewMaxY / 2, focusSize);

    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * A basic Camera preview class
     */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//            // If your preview can change or rotate, take care of those events here.
//            // Make sure to stop the preview before resizing or reformatting it.
//
//            if (mHolder.getSurface() == null){
//                // preview surface does not exist
//                return;
//            }
//
//            // stop preview before making changes
//            try {
//                mCamera.stopPreview();
//            } catch (Exception e){
//                // ignore: tried to stop a non-existent preview
//            }
//
//            // set preview size and make any resize, rotate or
//            // reformatting changes here
//
//            // start preview with new settings
//            try {
//                mCamera.setPreviewDisplay(mHolder);
//                mCamera.startPreview();
//
//            } catch (Exception e){
//                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
//            }
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            //拍摄完成之后，才执行下面配置，避免曝光过程中取消对焦和曝光锁定！
            if(captureMode==1){
//                mCamera.cancelAutoFocus();
//                focusing = false;

                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setAutoExposureLock(false);
                mCamera.setParameters(parameters);
                button_lock.setVisibility(View.INVISIBLE);

                layout_buttons.setVisibility(View.VISIBLE);//取消拍照后显示其他按钮
            }
            if(!justFocus){
                justFocus=true;
            }


            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                //让保存的图片可以在媒体库中显示！！！
                Intent intent = new Intent(); //新建一个Intent，用来发广播
                Uri uri = Uri.fromFile(pictureFile); //新建一个Uri，使用file的地址
                intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); //设置intent的action，这条action表示要将数据添加进系统的媒体资源库
                /*
                gallery中显示照片全是在这个资源库中找到的，
                换句话说，我们需要存进去正确的文件地址才可以在gallery中显示出来，
                否则即使文件存在在SDcard上gallery也不知道它。
                */
                intent.setData(uri); //指定存入资源库的数据，就是我们照片文件的地址
                sendBroadcast(intent); //发布广播。使用MainActivity.this.sendBroadcast(intent);也可以

                mCamera.startPreview();

                button_capture.setEnabled(true);
                button_shutter.setEnabled(true);

                //延迟300ms，等待galley更新后再更新button_review背景
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //execute the task
                        setImage(0);
                    }
                }, 500);

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            saving = false;
        }
    };

    private void DeletePhoto() {
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(MyActivity.this);
        //    设置Title的图标
        builder.setIcon(R.drawable.ic_launcher);
        //    设置Title的内容
        builder.setTitle("弹出警告框");
        //    设置Content来显示一个信息
        builder.setMessage("确定删除吗？");
        //    设置一个PositiveButton
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                ContentResolver mContentResolver = getApplicationContext().getContentResolver();
                Cursor mCursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");// 根据Uri从数据库中找
                mCursor.moveToPosition(cursorPosition);

                //delete photo
                mContentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=" + mCursor.getString(0), null);
                mCursor.moveToNext();
                //cursorPosition = mCursor.getPosition();


                //cursor.moveToFirst();// 把游标移动到首位，因为这里的Uri是包含ID的所以是唯一的不需要循环找指向第一个就是了
                String filePath = mCursor.getString(mCursor.getColumnIndex("_data"));// 获取图片路
                String orientation = mCursor.getString(mCursor
                        .getColumnIndex("orientation"));// 获取旋转的角度
                //mCursor.close();
                if (filePath != null) {
                    //只解析为原图的inSampleSize分之一
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = false;
                    opts.inSampleSize = IN_SAMPLE_SIZE;
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts);//根据Path读取资源图片

                    int angle = 0;
                    if (orientation != null && !"".equals(orientation)) {
                        angle = Integer.parseInt(orientation);
                    }
                    if (angle != 0) {
                        // 下面的方法主要作用是把图片转一个角度，也可以放大缩小等
                        Matrix m = new Matrix();
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        m.setRotate(angle); // 旋转angle度
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                                m, true);// 从新生成图片

                    }

                    imageView_review.setImageBitmap(bitmap);
                    imageView_review_fake.setImageBitmap(bitmap);

                }
            }
        });
        //    设置一个NegativeButton
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        //    显示出该对话框
        builder.show();

    }

    private void setImage(int i) {

        // 不管是拍照还是选择图片每张图片都有在数据中存储也存储有对应旋转角度orientation值
        // 所以我们在取出图片是把角度值取出以便能正确的显示图片,没有旋转时的效果观看

        //ContentResolver cr = this.getContentResolver();
        ContentResolver mContentResolver = this.getContentResolver();
        Cursor mCursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");// 根据Uri从数据库中找
        mCursor.moveToPosition(cursorPosition);

        if (mCursor != null) {

            switch (i) {
                case 0:
                    mCursor.moveToFirst();
                    break;
                case -1:
                    if (!mCursor.isFirst())
                        mCursor.moveToPrevious();
                    break;
                case 1:
                    mCursor.moveToNext();
                    break;
            }
            cursorPosition = mCursor.getPosition();

            //cursor.moveToFirst();// 把游标移动到首位，因为这里的Uri是包含ID的所以是唯一的不需要循环找指向第一个就是了
            String filePath = mCursor.getString(mCursor.getColumnIndex("_data"));// 获取图片路
            String orientation = mCursor.getString(mCursor.getColumnIndex("orientation"));// 获取旋转的角度
            //mCursor.close();
            photo_angle = 0;
            if (orientation != null && !"".equals(orientation)) {
                photo_angle = Integer.parseInt(orientation);
            }

            if (filePath != null) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                //设置解析器不去真正的解析这个位图，而是解析这个图片的out输出信息(宽度，高度)，不会为图片的每个点申请内在空间
                opts.inJustDecodeBounds = true;
                //得到图片的宽高信息
                BitmapFactory.decodeFile(filePath,opts);
                //真正的解析这个图片，只解析为原图的inSampleSize分之一
                opts.inSampleSize = computeSampleSize(opts, -1, 1080*1440);
                opts.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts);//根据Path读取资源图片

                imageView_review.setImageBitmap(bitmap);
                imageView_review_fake.setImageBitmap(bitmap);



                Matrix matrix = new Matrix();

                //获取imageView的大小
                float windowWidth = previewMaxX;
                float windowHeight = previewMaxY;

                int picWidth = bitmap.getWidth();
                int picHeight = bitmap.getHeight();
                //解析的图片是没有旋转的，竖拍的照片须要将宽高反转
                if (orientation != null && !"".equals(orientation)) {
                   if(Integer.parseInt(orientation) != 0){
                       picWidth = bitmap.getHeight();
                       picHeight = bitmap.getWidth();
                   }
                }

                //得到缩放比例
                float scaleX = windowWidth/picWidth;
                float scaleY = windowHeight/picHeight;
                //确定缩放比例
                float scale = 1;
                if(scaleX < scaleY){
                    scale = scaleX;
                }else{
                    scale = scaleY;
                }
                matrix.postScale(scale, scale,0,0);

                // 把图片转一个角度,解决竖拍照片方向问题,同时使图片居中
                float dx = 0;
                float dy = 0;

                if (photo_angle != 0) {
                    matrix.postRotate(photo_angle); // 旋转angle度
                    dx = windowWidth/2+picWidth*scale/2;
                    dy = windowHeight/2-picHeight*scale/2;
//                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);// 从新生成图片
                }else{
                    dx = windowWidth/2-picWidth*scale/2;
                    dy = windowHeight/2-picHeight*scale/2;
                }
                matrix.postTranslate(dx, dy);

                imageView_review.setImageMatrix(matrix);
                imageView_review_fake.setImageMatrix(matrix);


            }
        }
    }

    //获取opts.inSampleSize
    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {

        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;

        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {

        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        // Do something with this sensor value.
        if (y >= x) {
            photoDegree = 90;//竖屏
        } else {
            photoDegree = 0;//横屏
        }

        //横竖屏发生变化时才改变控件方向
        if (photoDegree != oldPhotoDegree) {
            if (photoDegree == 90) {
                setRotation(0);
            } else if (photoDegree == 0) {
                setRotation(90);
            }
        }

        oldPhotoDegree = photoDegree;

    }

    //控件旋转函数
    private void setRotation(float r) {
        button_ecAdd.setRotation(r);
        button_ec.setRotation(r);
        button_ecReduce.setRotation(r);

        button_capture.setRotation(r);
        button_lock.setRotation(r);
        button_flash.setRotation(r);
        button_shutter.setRotation(r);
        button_zoom.setRotation(r);
        button_settings.setRotation(r);
        button_focusMode.setRotation(r);
        button_record.setRotation(r);
        button_close.setRotation(r);

        button_previous.setRotation(r);
        button_delete.setRotation(r);
        button_next.setRotation(r);
        button_back.setRotation(r);

        //这两个控件不旋转，里面的图片旋转
        Matrix matrix = new Matrix();
        matrix.postRotate(r);
        imageView_review.setImageMatrix(matrix);
        imageView_review_fake.setImageMatrix(matrix);

    }

    private void setVisible(int r) {
        button_ecAdd.setVisibility(r);
        button_ec.setVisibility(r);
        button_ecReduce.setVisibility(r);

        button_capture.setVisibility(r);
//        button_lock.setVisibility(r);
        button_flash.setVisibility(r);
        button_shutter.setVisibility(r);
//        button_zoom.setVisibility(r);
        button_settings.setVisibility(r);
        button_focusMode.setVisibility(r);

    }


    //触摸对焦相关函数
    public void setFocusArea(float x, float y, float areaSize) {
        Rect rect = calculateTapArea(x, y, areaSize);
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(new Camera.Area(rect, 1000));
            parameters.setFocusAreas(focusAreas);
        }
        mCamera.setParameters(parameters);
    }

    public void setMeteringArea(float x, float y, float areaSize) {
        Rect rect = calculateTapArea(x, y, areaSize);
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(rect, 1000));
            parameters.setMeteringAreas(meteringAreas);
        }
        mCamera.setParameters(parameters);
    }

    /**
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to 1000:1000.
     */
    private Rect calculateTapArea(float x, float y, float areaSize) {
        float[] touchPoint = touch2camera(x, y, previewMaxX, previewMaxY);
        float xx = touchPoint[0];
        float yy = touchPoint[1];

        float left = clamp(xx - areaSize / 2, -1000, 1000);
        float top = clamp(yy - areaSize / 2, -1000, 1000);
        float right = clamp(xx + areaSize / 2, -1000, 1000);
        float bottom = clamp(yy + areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, right, bottom);
        Rect rect = new Rect();
        rectF.round(rect);
        return rect;
    }

    private float[] touch2camera(float x, float y, float maxX, float maxY) {
        float xx = (y * 2000 / maxY - 1000);
        float yy = (-x * 2000 / maxX + 1000);
        float[] result = new float[2];
        result[0] = xx;
        result[1] = yy;
        return result;
    }

    private float[] camera2touch(float x, float y, float maxX, float maxY) {
        float xx = -(y - 1000) * maxX / 2000;
        float yy = (x + 1000) * maxY / 2000;
        float[] result = new float[2];
        result[0] = xx;
        result[1] = yy;
        return result;
    }

    private float clamp(float x, float min, float max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    //输入camera坐标下的矩形，绘图
    private void drawArea(ImageView imageView, Rect rect, int color) {
        float[] aa = camera2touch(rect.left, rect.top, previewMaxX, previewMaxY);
        float[] bb = camera2touch(rect.right, rect.bottom, previewMaxX, previewMaxY);
        float left = aa[0];
        float top = aa[1];
        float right = bb[0];
        float bottom = bb[1];
        RectF rectTouch = new RectF(left, top, right, bottom);

        Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(6);
        canvas.drawRect(rectTouch, p);

        imageView.setImageBitmap(bitmap);
    }

    //输入touch坐标下的矩形，绘图
    private void drawArea1(ImageView imageView, RectF rectF, int color) {
        Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(6);
        canvas.drawRect(rectF, p);
        imageView.setImageBitmap(bitmap);
    }

    //清空绘图
    private void drawArea2(ImageView imageView) {
        Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        imageView.setImageBitmap(bitmap);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (focusing) {
                Toast.makeText(getApplicationContext(), "Focusing",
                        Toast.LENGTH_SHORT).show();
            } else {
                focusing = true;

                float x = event.getX();
                float y = event.getY();
                //Toast.makeText(getApplicationContext(), "tap", Toast.LENGTH_SHORT).show();
                tapPoint.x = x;
                tapPoint.y = y;

                setFocusArea(x, y, focusSize);
                //setMeteringArea(x, y, meteringSize);
                Camera.Parameters parameters = mCamera.getParameters();
                drawArea(ivFocus, parameters.getFocusAreas().get(0).rect, Color.CYAN);
                //drawArea(ivMetering, parameters.getMeteringAreas().get(0).rect, Color.BLUE);

                captureMode = 0;
                mCamera.autoFocus(myAutoFocusCallback);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            return true;
        }
    }

    private boolean focusKey = false;//每个按键按下后只检测一次
    private boolean cameraKey = false;
    private boolean justFocus = true;//标识只按了对焦键，没有按拍照键的情况

    //当一个按键按下保持不放时，会不断触发onKeyDown时间,注意处理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Handle zoom in/out here and consume the key event
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            // Handle camera focus and consume the key event
            if (!focusKey) {
                focusKey = true;

                if (focusing) {
                    Toast.makeText(getApplicationContext(), "Focusing",
                            Toast.LENGTH_SHORT).show();
                } else {
                    focusing = true;

                    float x1 = previewMaxX / 2;
                    float y1 = previewMaxY / 2;
                    setFocusArea(x1, y1, focusSize);
                    //setMeteringArea(x, y, meteringSize);
                    Camera.Parameters parameters1 = mCamera.getParameters();
                    drawArea(ivFocus, parameters1.getFocusAreas().get(0).rect, Color.CYAN);
                    //drawArea(ivMetering, parameters.getMeteringAreas().get(0).rect, Color.BLUE);

                    captureMode = 1;
                    mCamera.autoFocus(myAutoFocusCallback);
                    layout_buttons.setVisibility(View.INVISIBLE);//开始对焦时隐藏其他按钮
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            if (!cameraKey) {
                cameraKey = true;
                justFocus=false;

                //按下按键后延迟拍照，避免抖动
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //execute the task
                        takePhoto();
                    }
                }, 1000);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Handle zoom in/out here and consume the key event
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            // Handle camera focus and consume the key event
            focusKey = false;

            if(justFocus) {
                mCamera.cancelAutoFocus();
                focusing = false;

                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setAutoExposureLock(false);
                mCamera.setParameters(parameters);
                button_lock.setVisibility(View.INVISIBLE);

                layout_buttons.setVisibility(View.VISIBLE);
            }

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            cameraKey = false;

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private void takePhoto() {
        if(focusing){
            mCamera.cancelAutoFocus();
            focusing = false;
        }
        if (saving) {
            Toast.makeText(getApplicationContext(), "Saving",
                    Toast.LENGTH_SHORT).show();
        } else {
            saving = true;

            Camera.Parameters parameters = mCamera.getParameters();
            //设置保存下来的图片旋转，不是预览的旋转
            parameters.setRotation(photoDegree);
            mCamera.setParameters(parameters);
            // get an image from the camera
            mCamera.takePicture(null, null, mPicture);
            button_capture.setEnabled(false);
            button_shutter.setEnabled(false);

            //动画
            drawArea2(ivFocus);
            //drawArea2(ivMetering);
            Animation scale = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(350);
            scale.setInterpolator(new DecelerateInterpolator());
            camera_preview.startAnimation(scale);
        }
    }

    private boolean prepareVideoRecorder(){

        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();


        //设置参数
        //设置显示的旋转
        mCamera.setDisplayOrientation(90);
        //设置保存的旋转信息
        mMediaRecorder.setOrientationHint(photoDegree);

        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mCamera.setParameters(params);



        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
//        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoSize(1920,1080);
        mMediaRecorder.setVideoEncodingBitRate(VideoBitRate);
        //设定帧率无效
//        mMediaRecorder.setVideoFrameRate(24);



        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private final class mTouchListener implements View.OnTouchListener {

        /** 记录是拖拉照片模式还是放大缩小照片模式 */
        private int mode = 0;// 初始状态
        /** 拖拉照片模式 */
        private static final int MODE_DRAG = 1;
        /** 放大缩小照片模式 */
        private static final int MODE_ZOOM = 2;

        /** 用于记录开始时候的坐标位置 */
        private PointF startPoint = new PointF();
        /** 用于记录拖拉图片移动的坐标位置 */
        private Matrix matrix = new Matrix();
        /** 用于记录图片要进行拖拉时候的坐标位置 */
        private Matrix currentMatrix = new Matrix();

        /** 两个手指的开始距离 */
        private float startDis;
        /** 两个手指的中间点 */
        private PointF midPoint;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 手指压下屏幕
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_DRAG;
                    // 记录ImageView当前的移动位置
                    currentMatrix.set(imageView_review.getImageMatrix());
                    matrix.set(currentMatrix);
                    startPoint.set(event.getX(), event.getY());
                    break;
                // 手指在屏幕上移动，改事件会被不断触发
                case MotionEvent.ACTION_MOVE:
                    // 拖拉图片
                    if (mode == MODE_DRAG) {
                        float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                        float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
                        // 在没有移动之前的位置上进行移动
                        matrix.set(currentMatrix);
                        matrix.postTranslate(dx, dy);
                    }
                    // 放大缩小图片
                    else if (mode == MODE_ZOOM) {
                        float endDis = distance(event);// 结束距离
                        if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                            float scale = endDis / startDis;// 得到缩放倍数
                            matrix.set(currentMatrix);
                            matrix.postScale(scale, scale,midPoint.x,midPoint.y);
                        }
                    }
                    break;
                // 手指离开屏幕
                case MotionEvent.ACTION_UP:
                    // 当触点离开屏幕，但是屏幕上还有触点(手指)
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    break;
                // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = MODE_ZOOM;
                    /** 计算两个手指间的距离 */
                    startDis = distance(event);
                    /** 计算两个手指间的中间点 */
                    if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                        midPoint = mid(event);
                        //记录当前ImageView的缩放倍数
                        currentMatrix.set(imageView_review.getImageMatrix());
                    }
                    break;
            }
            imageView_review.setImageMatrix(matrix);
            return true;
        }

        /** 计算两个手指间的距离 */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            return FloatMath.sqrt(dx * dx + dy * dy);
        }

        /** 计算两个手指间的中间点 */
        private PointF mid(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX, midY);
        }

    }


}
