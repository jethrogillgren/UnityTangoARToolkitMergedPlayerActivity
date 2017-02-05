package com.google.unity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.YuvImage;
import android.content.Context;

import android.graphics.ImageFormat;

import org.artoolkit.ar.base.NativeInterface;

//import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
//import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
//import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
//import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
//import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import org.artoolkit.ar.unity.CameraSurface;

import android.hardware.Camera;

import java.util.List;


/**
 * Created by jethro on 31/01/2017.
 */

public class UnityTangoARPlayer extends GoogleUnityActivity {

    protected final static String TAG = "J#  UnityTangoARPlayer";

    private static  int mWidth = 1920;
    private static  int mHeight = 1080;
    private static  boolean mCameraIsFrontFacing = true;
    private static  int mCameraIndex = 0;

    private static Context context;


    public static Context getAppContext() {
        return UnityTangoARPlayer.context;
    }

    // Setup activity layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "OnCreate() called");

        super.onCreate(savedInstanceState);

        UnityTangoARPlayer.context = getApplicationContext();


        /*This method copies the ffmpeg binary to device according to device's architecture. You just need to put this once in your code, whenever you are starting the application or using FFmpeg for the first time. This command does the following:

        Loads/Copies binary to device according to architecture
        Updates binary if it is using old FFmpeg version
        Provides callbacks through FFmpegLoadBinaryResponseHandler interface*/
//        FFmpeg ffmpeg = FFmpeg.getInstance(context);
//        try {
//            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
//
//                @Override
//                public void onStart() {Log.i(TAG, "FFMPEG LoadBinaryResponseHandler Start: ");}
//
//                @Override
//                public void onFailure() { Log.i(TAG, "FFMPEG LoadBinaryResponseHandler Failure: ");}
//
//                @Override
//                public void onSuccess() {Log.i(TAG, "FFMPEG LoadBinaryResponseHandler Success: ");}
//
//                @Override
//                public void onFinish() {Log.i(TAG, "FFMPEG LoadBinaryResponseHandler Finish: ");}
//            });
//        } catch (FFmpegNotSupportedException e) {
//            // Handle if FFmpeg is not supported by device
//            Log.i(TAG, "FFmpegNotSupportedException: " + e.getMessage() );
//        }


        // This needs to be done just only the very first time the application is run,
        // or whenever a new preference is added (e.g. after an application upgrade).
        int resID = getResources().getIdentifier("preferences", "xml", getPackageName());
        PreferenceManager.setDefaultValues(this, resID, false);

//        // Create the camera preview.
//        previewView = new CameraSurface(this);
//        ((ViewGroup)this.findViewById(android.R.id.content)).addView(previewView, -1, new ViewGroup.LayoutParams(128, 128)); //TODO wrong order?



        SurfaceView sv = findSurfaceView( this.mUnityPlayer.getView() );
        if (sv == null) {
            Log.w(TAG, "No SurfaceView found in Unity view hierarchy.");
        } else {
            Log.i(TAG, "Found SurfaceView " + sv.toString() + ".   I would set its media overlay if I wasn't a chicken");
//            sv.setZOrderMediaOverlay(true);
        }

        foo();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume() called");

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");

        super.onPause();

//        // Restore the original view hierarchy.
//        ((ViewGroup)this.findViewById(android.R.id.content)).removeAllViews();
//        previewView = null; // Make sure camera is released in onPause().
    }


    // call this method from unity
    public static void foo(){

        Log.i(TAG, "Foo FFMPEG");

//        FFmpeg ffmpeg = FFmpeg.getInstance(context);
//        try {
//            // to execute "ffmpeg -version" command you just need to pass "-version"
//            String[] cmd = new String[]{"-version"};
//            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
//
//                @Override
//                public void onStart() {}
//
//                @Override
//                public void onProgress(String message) {Log.i(TAG, "Foo - " + message);}
//
//                @Override
//                public void onFailure(String message) {Log.i(TAG, "Foo - " + message);}
//
//                @Override
//                public void onSuccess(String message) {Log.i(TAG, "Foo - " + message);}
//
//                @Override
//                public void onFinish() {}
//            });
//        } catch (FFmpegCommandAlreadyRunningException e) {
//            // Handle if FFmpeg is already running
//            Log.i(TAG, "FFmpegCommandAlreadyRunningException: " + e.getMessage() );
//        }

//        int[] width = new int[1];
//        int[] height = new int[1];
//        int[] pixelSize = new int[1];
//        String[] pixelFormatString = new String[1];
//        boolean ok = NativeInterface.arwGetVideoParams(width, height, pixelSize, pixelFormatString);
//
//        Log.i(TAG, "Foo method: GOt Video Params: " + ok + ".    " + width[0] + " " + height[0] + " " + pixelSize[0] + " " + pixelFormatString );
    }

    /**
     * Passes a video frame to the native library for processing.
     *
     * @param image               Buffer containing the video frame
     */
    public static void arwAcceptVideoImage(byte[] image ) {

        arwAcceptVideoImage_(image,  mWidth,  mHeight,  mCameraIndex, mCameraIsFrontFacing);
    }

    /**
     * Passes a video frame to the native library for processing.
     *
     * @param image               Buffer containing the video frame
     * @param width               Width of the video frame in pixels
     * @param height              Height of the video frame in pixels
     * @param cameraIndex         Zero-based index of the camera in use. If only one camera is present, will be 0.
     * @param cameraIsFrontFacing false if camera is rear-facing (the default) or true if camera is facing toward the user.
     */
    public static void arwAcceptVideoImage_(byte[] image, int width, int height, int cameraIndex, boolean cameraIsFrontFacing ) {

        YuvImage yuvImage = new YuvImage(image, android.graphics.ImageFormat.NV21, width, height, null);//TODO

        NativeInterface.arwAcceptVideoImage(image,  width,  height,  cameraIndex, cameraIsFrontFacing);
    }

    public View getAndroidViewLayer() {
        return findViewById(R.id.android_view_container);
    }


    /**
     * Walk a view hierarchy looking for the first SurfaceView.
     * Search is depth first.
     *
     * @param v View hierarchy root.
     * @return The first SurfaceView in the hierarchy, or null if none could be found.
     */
    private SurfaceView findSurfaceView(View v) {
        if (v == null) return null;
        else if (v instanceof SurfaceView) return (SurfaceView) v;
        else if (v instanceof ViewGroup) {
            int childCount = ((ViewGroup) v).getChildCount();
            for (int i = 0; i < childCount; i++) {
                SurfaceView ret = findSurfaceView(((ViewGroup) v).getChildAt(i));
                if (ret != null) return ret;
            }
        }
        return null;
    }

    void setStereo(boolean stereo) {
        //TODO enable stereo feeds see BT-200 ARToolkit implementation
    }
    void launchPreferencesActivity() {
        //TODO  This may not be needed - it might be called form the NDK code though...
    }

}
