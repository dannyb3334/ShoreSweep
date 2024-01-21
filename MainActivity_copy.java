package com.example.opencv_app;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity_copy extends CameraActivity  implements CvCameraViewListener2, SensorEventListener {

    // Variables
    private static final String TAG = "CheckPoint";
    private int FRAME_HEIGHT_HALF = 724 /2;
    private int FRAME_WIDTH = 1024;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mOutFrameMat;
    private Mat mIntermediateMat;
    private double midpoint_x;
    private double midpoint_y;
    private List<MatOfPoint> contours;
    private Scalar boxColor = new Scalar(255.0, 255.0, 0.0);
    private int frameProcessingOption = 0;

    private final int delay_master = 10;
    private int delay = delay_master;

    Physicaloid mPhysicaloid;
    private final int Baudrate = 9600;

    private SensorManager sensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    private int heading = 0;

    private double hMinVar = 80.0;//  BLUE  //80
    private double sMinVar = 160.0;         //160
    private double vMinVar = 100.0;         //100
    private double hMaxVar = 141.0;         //141
    private double sMaxVar = 255.0;         //255
    private double vMaxVar = 255.0;         //255


    private int y = 0; // forward/backward
    private int x = 0; // left/right
    private int z = 0; // action
    private String actionOutput = "1";


    // Create and start MainACtivity
    public MainActivity_copy() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    // Confirm OpenCV Working
    // OpenCV Setup

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        // OpenCV initialization
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV started");
        } else {
            Log.e(TAG, "OpenCV initialization failed");
            return;
        }

        // Arduino Physicaloid initialization
        mPhysicaloid = new Physicaloid(this);
        mPhysicaloid.setBaudrate(Baudrate);
        openArduinoConnection();

        // Compass initialization
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Prevent screen dim and lock horizontal screen orientation
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // View listeners
        ToggleButton optionsButton = (ToggleButton)  findViewById(R.id.toggleButton);
        LinearLayout optionsView = (LinearLayout)  findViewById(R.id.optionsView);
        optionsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean buttonToggled) {
                if (buttonToggled) {
                    optionsView.setVisibility(optionsView.VISIBLE);
                } else {
                    optionsView.setVisibility(optionsView.GONE);
                }
            }
        });

        Switch mask = (Switch)  findViewById(R.id.switch1);
        mask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean switchActivated) {
                if (switchActivated) {
                    frameProcessingOption = 2;
                } else {
                    frameProcessingOption = 0;
                }
            }
        });

        SeekBar hMin = (SeekBar)  findViewById(R.id.hMin);
        TextView hMinValue = (TextView)  findViewById(R.id.hMinValue);
        hMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                hMinVar = (double) progress;
                hMinValue.setText(Integer.toString(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        SeekBar sMin = (SeekBar)  findViewById(R.id.sMin);
        TextView sMinValue = (TextView)  findViewById(R.id.sMinValue);
        sMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sMinVar = (double) progress;
                sMinValue.setText(Integer.toString(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        SeekBar vMin = (SeekBar)  findViewById(R.id.vMin);
        TextView vMinValue = (TextView)  findViewById(R.id.vMinValue);
        vMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vMinVar = (double) progress;
                vMinValue.setText(Integer.toString(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SeekBar hMax = (SeekBar)  findViewById(R.id.hMax);
        TextView hMaxValue = (TextView)  findViewById(R.id.hMaxValue);
        hMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                hMaxVar = (double) progress;
                hMaxValue.setText(Integer.toString(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        SeekBar sMax = (SeekBar)  findViewById(R.id.sMax);
        TextView sMaxValue = (TextView)  findViewById(R.id.sMaxValue);
        sMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sMaxVar = (double) progress;
                sMaxValue.setText(Integer.toString(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        SeekBar vMax = (SeekBar)  findViewById(R.id.vMax);
        TextView vMaxValue = (TextView)  findViewById(R.id.vMaxValue);
        vMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vMaxVar = (double) progress;
                vMaxValue.setText(Integer.toString(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    public void openArduinoConnection() {
        if (mPhysicaloid.open()) {
            Log.i(TAG, "Physicaloid loaded successfully");
            mPhysicaloid.addReadListener(new ReadLisener() {
                @Override
                public void onRead(int size) {
                    byte[] buf = new byte[size];
                    mPhysicaloid.read(buf, size);
                    String s = new String(buf);
                    updateTextView(s, "readData");
                }
            });
        } else {
            Log.i(TAG, "Physicaloid loaded unsuccessfully");
        }
    }

    // App not in focus
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if(mPhysicaloid.close())
            mPhysicaloid.clearReadListener();
        sensorManager.unregisterListener(this, magnetometer);
        sensorManager.unregisterListener(this, accelerometer);
    }

    // App resume focus
    @Override
    public void onResume() {
        super.onResume();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.enableView();
        openArduinoConnection();
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    // Camera
    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    // App user destroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if(mPhysicaloid.close())
            mPhysicaloid.clearReadListener();
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mOutFrameMat = new Mat(height, width, CvType.CV_8UC3);
        mIntermediateMat = new Mat();
    }

    // Release hardware once stopped
    @Override
    public void onCameraViewStopped() {
        mOutFrameMat.release();
    }

    // USB Detection
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            openArduinoConnection();
        }
    };


    // MAIN FUNCTIONS ------------------------------------------------------------------------------

    // Update text fields
    public void updateTextView(final String newText, String viewID) {
        int resID = getResources().getIdentifier(viewID,
                "id", getPackageName());
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView view= (TextView) findViewById(resID);
                view.setText(viewID + ": \n" + newText);
            }
        });
    }

    // Compass heading listener
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (delay == 0) {
            if (event.sensor == magnetometer) {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            } else if (event.sensor == accelerometer) {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            }
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuthInRadians = orientation[0];
            int azimuthInDegress = (int) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            heading = azimuthInDegress;
            updateTextView(Integer.toString(azimuthInDegress) + "°", "sensorData");
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // write data to arduino via Physicaloid serial comm library
    public void writeToArduino (String data) {
        byte[] buf = data.getBytes();
        mPhysicaloid.write(buf, buf.length);	//write data to arduino
        updateTextView(String.valueOf(data), "writtenData");
    }

    // helper for writeToArduino
    private static byte[] intArrayToByteArray(int[] intArray) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(intArray.length * Integer.BYTES);
        for (int value : intArray) {
            byteBuffer.putInt(value);
        }
        return byteBuffer.array();
    }

    // Image processing type fucntion

    private void proccessFrame(CvCameraViewFrame inputFrame) {

        switch (frameProcessingOption) {

            case 0:
                mOutFrameMat = inputFrame.rgba();
                break;

            case 1:
                Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 100, 200, 5, true);
                Imgproc.cvtColor( mIntermediateMat, mOutFrameMat, Imgproc.COLOR_GRAY2RGBA);
                break;

            case 2:
                Scalar lowerColorValue = new Scalar(hMinVar, sMinVar, vMinVar);
                Scalar upperColorValue = new Scalar(hMaxVar, sMaxVar, vMaxVar);
                contours = new ArrayList<>();
                mOutFrameMat = inputFrame.rgba();
                // rgba is accepted as rgb with no conversion
                // blur for reduced detail, only color detection
                //Imgproc.GaussianBlur(inputFrame.rgba(), mIntermediateMat, new Size(25, 25), 0, 0); // 29 max for kernel size 31
                Imgproc.cvtColor(mOutFrameMat, mIntermediateMat, Imgproc.COLOR_RGB2HSV);
                Core.inRange(mIntermediateMat, lowerColorValue, upperColorValue, mOutFrameMat);
                Imgproc.findContours(mOutFrameMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.cvtColor(mOutFrameMat, mOutFrameMat, Imgproc.COLOR_GRAY2RGBA);
                if (contours.size() > 0) {
                    Log.e(TAG, String.valueOf(contours.size()));
                    for ( int i=0; i<contours.size(); i++ ) {
                        RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
                        Point[] vertices = new Point[4];
                        rotatedRect.points(vertices);
                        //for (int j = 0; j < 4; j++)
                        if (vertices[2].x - vertices[3].x > 200) {
                            midpoint_x = (vertices[2].x + vertices[3].x) / 2;
                            midpoint_y = (vertices[2].y + vertices[3].y) / 2;
                            // 0    1
                            // 3    2
                            //
                            // 0  <-  1024
                            // ^
                            // |
                            // 724
                            Imgproc.circle(mOutFrameMat, new Point(midpoint_x,midpoint_y), 40, new Scalar(255.0,0.0,0.0), 2);
                            Imgproc.line(mOutFrameMat, vertices[3], vertices[2], boxColor, 2);
                        }

                        if (midpoint_y < FRAME_HEIGHT_HALF) {
                            actionOutput = "3";
                            writeToArduino(actionOutput);
                        }

                        actionOutput = "4";
                    }
                }
                break;
        }
    }

    @Override
    // MAIN LOOP (Frame) ---------------------------------------------------------------------------
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        if (delay == 0) {
            writeToArduino(actionOutput);
            delay = delay_master;
        } else {
            delay-=1;
        }
        proccessFrame(inputFrame);
        return mOutFrameMat;
    }

    //DPAD demo control
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                actionOutput = "1";     // both off    (1)
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                actionOutput = "2";     // both on     (2)
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                actionOutput = "3";     // turn right  (3)
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                actionOutput = "4";     // turn left   (4)
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
}