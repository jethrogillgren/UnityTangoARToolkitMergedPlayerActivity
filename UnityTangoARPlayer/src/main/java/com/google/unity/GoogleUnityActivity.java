/*
 *  UnityTangoARPlayerActivity.java
 *  ARToolKit5
 *
 *  Disclaimer: IMPORTANT:  This Daqri software is supplied to you by Daqri
 *  LLC ("Daqri") in consideration of your agreement to the following
 *  terms, and your use, installation, modification or redistribution of
 *  this Daqri software constitutes acceptance of these terms.  If you do
 *  not agree with these terms, please do not use, install, modify or
 *  redistribute this Daqri software.
 *
 *  In consideration of your agreement to abide by the following terms, and
 *  subject to these terms, Daqri grants you a personal, non-exclusive
 *  license, under Daqri's copyrights in this original Daqri software (the
 *  "Daqri Software"), to use, reproduce, modify and redistribute the Daqri
 *  Software, with or without modifications, in source and/or binary forms;
 *  provided that if you redistribute the Daqri Software in its entirety and
 *  without modifications, you must retain this notice and the following
 *  text and disclaimers in all such redistributions of the Daqri Software.
 *  Neither the name, trademarks, service marks or logos of Daqri LLC may
 *  be used to endorse or promote products derived from the Daqri Software
 *  without specific prior written permission from Daqri.  Except as
 *  expressly stated in this notice, no other rights or licenses, express or
 *  implied, are granted by Daqri herein, including but not limited to any
 *  patent rights that may be infringed by your derivative works or by other
 *  works in which the Daqri Software may be incorporated.
 *
 *  The Daqri Software is provided by Daqri on an "AS IS" basis.  DAQRI
 *  MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 *  THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE, REGARDING THE DAQRI SOFTWARE OR ITS USE AND
 *  OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 *
 *  IN NO EVENT SHALL DAQRI BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 *  MODIFICATION AND/OR DISTRIBUTION OF THE DAQRI SOFTWARE, HOWEVER CAUSED
 *  AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 *  STRICT LIABILITY OR OTHERWISE, EVEN IF DAQRI HAS BEEN ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Julian Looser, Philip Lamb
 *
 */

package com.google.unity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NativeActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.unity3d.player.UnityPlayer;

/**
 * Custom Unity Activity that passes through Android lifecycle events from Unity appropriately.
 */
public class GoogleUnityActivity
        extends Activity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    /**
     * Callbacks for common Android lifecycle events.
     */
    public interface AndroidLifecycleListener {
        public void onPause();

        public void onResume();

        public void onActivityResult(int requestCode, int resultCode, Intent data);

        public void onRequestPermissionsResult(
                int requestCode, String[] permissions, int[] grantResults);

        public void onDisplayChanged();
    }

    // don't change the name of this variable; referenced from native code
    protected UnityPlayer mUnityPlayer;

    protected AndroidLifecycleListener mAndroidLifecycleListener;

    protected boolean mIsUnityQuit = false;

    // Setup activity layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().takeSurface(null);
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        getWindow().setFormat(PixelFormat.RGB_565);

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {}

                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized (this) {
                        if (mAndroidLifecycleListener != null) {
                            mAndroidLifecycleListener.onDisplayChanged();
                        }
                    }
                }

                @Override
                public void onDisplayRemoved(int displayId) {}
            }, null);
        }

        mUnityPlayer = new UnityPlayer(this);
        if (mUnityPlayer.getSettings().getBoolean("hide_status_bar", true)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        ((ViewGroup) findViewById(android.R.id.content)).addView(mUnityPlayer.getView(), 0);
        mUnityPlayer.requestFocus();
    }



    public void showAndroidViewLayer(final int layoutResId) {
        final Activity self = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup androidViewContainer =
                        (ViewGroup) findViewById(R.id.android_view_container);
                androidViewContainer.removeAllViews();

                // Make it possible for the developer to specify their own layout.
                LayoutInflater.from(self).inflate(layoutResId, androidViewContainer);
            }
        });
    }
    public void showAndroidViewLayer(android.view.View view) {
        Log.i("J#", "HEYHO LIFECYCLE  showAndroidViewLayer: " + view.getTransitionName() );
    }

    public View getAndroidViewLayer() {
        return findViewById(R.id.android_view_container);
    }

    public void launchIntent(String packageName, String className, String[] args, int requestcode) {
        Intent intent = new Intent();
        intent.setClassName(packageName, className);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String[] keyVal = args[i].split(":");
                if (keyVal.length == 2) {
                    intent.putExtra(keyVal[0], keyVal[1]);
                }
            }
        }
        startActivityForResult(intent, requestcode);
    }

    public boolean checkAndroidPermission(String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public void requestAndroidPermissions(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    public boolean shouldShowRequestAndroidPermissionRationale(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
    }

    public void launchApplicationDetailsSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    // CHECKSTYLE:OFF
    public void LaunchIntent(String packageName, String className, String[] args, int requestcode) {
        // CHECKSTYLE:ON
        launchIntent(packageName, className, args, requestcode);
    }

    public void attachLifecycleListener(AndroidLifecycleListener listener) {
        mAndroidLifecycleListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAndroidLifecycleListener != null) {
            mAndroidLifecycleListener.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if (mAndroidLifecycleListener != null) {
            mAndroidLifecycleListener.onRequestPermissionsResult(
                    requestCode, permissions, grantResults);
        }
    }


    // Quit Unity
    @Override
    protected void onDestroy() {
        mUnityPlayer.quit();
        mIsUnityQuit = true;
        super.onDestroy();
    }

    // Pause Unity
    @Override
    protected void onPause() {
        super.onPause();
        if (mAndroidLifecycleListener != null) {
            mAndroidLifecycleListener.onPause();
        }

        if (!mIsUnityQuit) {
            mUnityPlayer.pause();
        }
    }

    // Resume Unity
    @Override
    protected void onResume() {
        super.onResume();
        if (mAndroidLifecycleListener != null) {
            mAndroidLifecycleListener.onResume();
        }

        if (!mIsUnityQuit) {
            mUnityPlayer.resume();
        }
    }

    public void logAndroidErrorMessage(String message) {
        Log.e(this.getPackageName(), message);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!mIsUnityQuit) {
            mUnityPlayer.configurationChanged(newConfig);
        }
    }

    // Notify Unity of the focus change.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!mIsUnityQuit) {
            mUnityPlayer.windowFocusChanged(hasFocus);
        }
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE) {
            return mUnityPlayer.injectEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    /* API12 */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }
}


//        implements ActivityCompat.OnRequestPermissionsResultCallback {
////    /**
////     * Callbacks for common Android lifecycle events.
////     */
////    public interface AndroidLifecycleListener {
////        public void onPause();
////
////        public void onResume();
////
////        public void onActivityResult(int requestCode, int resultCode, Intent data);
////
////        public void onRequestPermissionsResult(
////                int requestCode, String[] permissions, int[] grantResults);
////
////        public void onDisplayChanged();
////    }
//
//
//    // don't change the name of this variable; referenced from native code
////    protected UnityPlayer mUnityPlayer;
//    protected boolean mIsUnityQuit = false;
//
////    protected AndroidLifecycleListener mAndroidLifecycleListener;
//
//
//
//    protected final static String TAG = "J#  UnityTangoARPlayer";
//
////    private FrameLayout previewInserter = null;
////    private ViewGroup unityView = null;
////    private CameraSurface previewView = null;
//
//    protected final static int PERMISSION_REQUEST_CAMERA = 77;
//
//
//    /**
//     * Walk a view hierarchy looking for the first SurfaceView.
//     * Search is depth first.
//     *
//     * @param v View hierarchy root.
//     * @return The first SurfaceView in the hierarchy, or null if none could be found.
//     */
//    private SurfaceView findSurfaceView(View v) {
//        if (v == null) return null;
//        else if (v instanceof SurfaceView) return (SurfaceView) v;
//        else if (v instanceof ViewGroup) {
//            int childCount = ((ViewGroup) v).getChildCount();
//            for (int i = 0; i < childCount; i++) {
//                SurfaceView ret = findSurfaceView(((ViewGroup) v).getChildAt(i));
//                if (ret != null) return ret;
//            }
//        }
//        return null;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_main);
//
//        Log.i(TAG, "LIFECYCLE  onCreate");
//
//        getWindow().takeSurface(null);
//        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
//        getWindow().setFormat(PixelFormat.RGB_565);
//
//
//        // This needs to be done just only the very first time the application is run,
//        // or whenever a new preference is added (e.g. after an application upgrade).
//        int resID = getResources().getIdentifier("preferences", "xml", getPackageName());
//        PreferenceManager.setDefaultValues(this, resID, false);
//
//
////        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
////        if (displayManager != null) {
////            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
////                @Override
////                public void onDisplayAdded(int displayId) {}
////
////                @Override
////                public void onDisplayChanged(int displayId) {
////                    synchronized (this) {
////                        if (mAndroidLifecycleListener != null) {
////                            mAndroidLifecycleListener.onDisplayChanged();
////                        }
////                    }
////                }
////
////                @Override
////                public void onDisplayRemoved(int displayId) {}
////            }, null);
////        }
//
////        mUnityPlayer = new UnityPlayer(this);
////        if (mUnityPlayer.getSettings().getBoolean("hide_status_bar", true)) {
////            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
////                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
////        }
////
////        ((ViewGroup) findViewById(android.R.id.content)).addView(mUnityPlayer.getView(), 0);
////        mUnityPlayer.requestFocus();
//        mIsUnityQuit = true;
//
//        Log.i(TAG, "LIFECYCLE  End of onCreate()");
//    }
//
//    public boolean checkAndroidPermission(String permission) {
//        Log.i(TAG, "LIFECYCLE  checkAndroidPermissions");
//        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
//        return permissionCheck == PackageManager.PERMISSION_GRANTED;
//    }
//
//    public void requestAndroidPermissions(String[] permissions, int requestCode) {
//        ActivityCompat.requestPermissions(this, permissions, requestCode);
//    }
//
//    public boolean shouldShowRequestAndroidPermissionRationale(String permission) {
//        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
//    }
//
////    public void attachLifecycleListener(AndroidLifecycleListener listener) {
////        Log.i(TAG, "LIFECYCLE  Listener Attached");
////        mAndroidLifecycleListener = listener;
////    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.i(TAG, "LIFECYCLE  onActivityResult");
////        if (mAndroidLifecycleListener != null) {
////            mAndroidLifecycleListener.onActivityResult(requestCode, resultCode, data);
////        }
//    }
//
////    @Override
////    public void onRequestPermissionsResult(
////            int requestCode, String[] permissions, int[] grantResults) {
////        Log.i(TAG, "LIFECYCLE  onRequestPermissionsResult");
//
////        if (mAndroidLifecycleListener != null) {
////            mAndroidLifecycleListener.onRequestPermissionsResult(
////                    requestCode, permissions, grantResults);
////        }
//    }
//
//
////    public void showAndroidViewLayer(final int layoutResId) {
////        final Activity self = this;
////        runOnUiThread(new Runnable() {
////            @Override
////            public void run() {
////                ViewGroup androidViewContainer =
////                        (ViewGroup) findViewById(R.id.android_view_container);
////                androidViewContainer.removeAllViews();
////
////                // Make it possible for the developer to specify their own layout.
////                LayoutInflater.from(self).inflate(layoutResId, androidViewContainer);
////            }
////        });
////    }
////
////    public View getAndroidViewLayer() {
////        return findViewById(R.id.android_view_container);
////    }
//
//    // CHECKSTYLE:OFF
//    public void LaunchIntent(String packageName, String className, String[] args, int requestcode) {
//        // CHECKSTYLE:ON
//        launchIntent(packageName, className, args, requestcode);
//    }
//    public void launchIntent(String packageName, String className, String[] args, int requestcode) {
//        Log.i(TAG, "LIFECYCLE  launchIntent");
//        Intent intent = new Intent();
//        intent.setClassName(packageName, className);
//        if (args != null) {
//            for (int i = 0; i < args.length; i++) {
//                String[] keyVal = args[i].split(":");
//                if (keyVal.length == 2) {
//                    intent.putExtra(keyVal[0], keyVal[1]);
//                }
//            }
//        }
//        startActivityForResult(intent, requestcode);
//    }
//
//    // Quit Unity
//    @Override
//    protected void onDestroy() {
//        Log.i(TAG, "LIFECYCLE  onDestroy");
////        mUnityPlayer.quit();
//        mIsUnityQuit = true;
//        super.onDestroy();
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        Log.i(TAG, "LIFECYCLE onResume()");
//
//
////        if (mAndroidLifecycleListener != null) {
////            Log.i(TAG, "LIFECYCLE calling mAndroidLifecycleListener.onResume()");
////            mAndroidLifecycleListener.onResume();
////        }
//
//        if (!mIsUnityQuit) {
//            Log.i(TAG, "LIFECYCLE calling mUnityPlayer.resume()");
////            mUnityPlayer.resume();
//        }
//
//        Log.i(TAG, "LIFECYCLE End of onResume()" );
//
//        //ARToolkit Code:
//
////        //
////        // Wrap the Unity application's view and the camera preview in a FrameLayout;
////        //
////
////        //View focusView = getCurrentFocus(); // Save the focus, in case we inadvertently change it.
////        //Log.i(TAG, "Focus view is " + focusView.toString() + ".");
////
////        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
////        unityView = (ViewGroup) decorView.getChildAt(0);
////        if (unityView == null) {
////            Log.e(TAG, "Error: Could not find top view.");
////            return;
////        }
////        Log.i(TAG, "Top view is " + unityView.toString() + ".");
////
////        // Create a placeholder for us to insert the camera preview capture object to the
////        // view hierarchy.
////        previewInserter = new FrameLayout(this);
////        decorView.removeView(unityView); // We must remove the root view from its parent before we can add it somewhere else.
////        decorView.addView(previewInserter);
////
////        //focusView.requestFocus(); // Restore focus.
////
////        // Create the camera preview.
////        previewView = new CameraSurface(this);
////        previewInserter.addView(previewView, new LayoutParams(128, 128));
////
////        // Now add Unity view back in.
////        // In order to ensure that Unity's view covers the camera preview each time onResume
////        // is called, find the SurfaceView inside the Unity view hierachy, and
////        // set the media overlay mode on it. Add the Unity view AFTER adding the previewView.
////        SurfaceView sv = findSurfaceView(unityView);
////        if (sv == null) {
////            Log.w(TAG, "No SurfaceView found in Unity view hierarchy.");
////        } else {
////            Log.i(TAG, "Found SurfaceView " + sv.toString() + ".");
////            sv.setZOrderMediaOverlay(true);
////        }
////        previewInserter.addView(unityView);
//    }
//
//    @Override
//    protected void onPause() {
//        Log.i(TAG, "LIFECYCLE onPause()");
//
//        super.onPause();
//
////        if (mAndroidLifecycleListener != null) {
////            mAndroidLifecycleListener.onPause();
////        }
//
//        if (!mIsUnityQuit) {
//            Log.i(TAG, "LIFECYCLE Pauding mUnityPlayer");
////            mUnityPlayer.pause();
//        }
//
//        ////ARToolkit COde:
////
////        // Restore the original view hierarchy.
////        previewInserter.removeAllViews();
////        previewView = null; // Make sure camera is released in onPause().
////
////        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
////        decorView.removeView(previewInserter);
////        decorView.addView(unityView);
////
////        previewInserter = null;
////        unityView = null;
//    }
//
//    void launchPreferencesActivity() {
//        startActivity(new Intent(this, CameraPreferencesActivity.class));
//    }
//
//
//    public void logAndroidErrorMessage(String message) {
//        Log.e(this.getPackageName(), message);
//    }
//
////    @Override
////    public void onConfigurationChanged(Configuration newConfig) {
////        super.onConfigurationChanged(newConfig);
////        Log.i(TAG, "LIFECYCLE Passthrough COnfigurationChanged");
////        if (!mIsUnityQuit) {
////            mUnityPlayer.configurationChanged(newConfig);
////        }
////    }
////
////    // Notify Unity of the focus change.
////    @Override
////    public void onWindowFocusChanged(boolean hasFocus) {
////        super.onWindowFocusChanged(hasFocus);
////        Log.i(TAG, "LIFECYCLE Passthrough WindowFocusChanged");
////        if (!mIsUnityQuit) {
////            mUnityPlayer.windowFocusChanged(hasFocus);
////        }
////    }
////
////    // For some reason the multiple keyevent type is not supported by the ndk.
////    // Force event injection by overriding dispatchKeyEvent().
////    @Override
////    public boolean dispatchKeyEvent(KeyEvent event) {
////        Log.i(TAG, "LIFECYCLE Passthrough DispatchKeyEvent");
////        if (event.getAction() == KeyEvent.ACTION_MULTIPLE) {
////            return mUnityPlayer.injectEvent(event);
////        }
////        return super.dispatchKeyEvent(event);
////    }
////
////    // Pass any events not handled by (unfocused) views straight to UnityPlayer
////    @Override
////    public boolean onKeyUp(int keyCode, KeyEvent event) {
////
////        Log.i(TAG, "LIFECYCLE Passthrough KeyUp");
////        return mUnityPlayer.injectEvent(event);
////    }
////
////    @Override
////    public boolean onKeyDown(int keyCode, KeyEvent event) {
////        Log.i(TAG, "LIFECYCLE Passthrough KeyDown");
////        return mUnityPlayer.injectEvent(event);
////    }
////
////    @Override
////    public boolean onTouchEvent(MotionEvent event) {
////
////        Log.i(TAG, "LIFECYCLE Passthrough TouchEvent");
////        return mUnityPlayer.injectEvent(event);
////    }
////
////    /* API12 */
////    @Override
////    public boolean onGenericMotionEvent(MotionEvent event) {
////        Log.i(TAG, "LIFECYCLE Passthrough GenericMotionEvent");
////        return mUnityPlayer.injectEvent(event);
////    }
//
//}
//
///*
//// FOR REFERENCE, PARENT UNITY3DPLAYER CLASS FOLLOWS.
//
//package com.unity3d.player;
//
//import android.app.NativeActivity;
//import android.content.res.Configuration;
//import android.graphics.PixelFormat;
//import android.os.Bundle;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//
//public class UnityPlayerNativeActivity extends NativeActivity
//{
//	protected UnityPlayer mUnityPlayer;		// don't change the name of this variable; referenced from native code
//
//	// UnityPlayer.init() should be called before attaching the view to a layout - it will load the native code.
//	// UnityPlayer.quit() should be the last thing called - it will unload the native code.
//	protected void onCreate (Bundle savedInstanceState)
//	{
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		super.onCreate(savedInstanceState);
//
//		getWindow().takeSurface(null);
//		setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
//		getWindow().setFormat(PixelFormat.RGB_565);
//
//		mUnityPlayer = new UnityPlayer(this);
//		if (mUnityPlayer.getSettings ().getBoolean ("hide_status_bar", true))
//			getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN,
//			                       WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//		int glesMode = mUnityPlayer.getSettings().getInt("gles_mode", 1);
//		boolean trueColor8888 = false;
//		mUnityPlayer.init(glesMode, trueColor8888);
//
//		View playerView = mUnityPlayer.getView();
//		setContentView(playerView);
//		playerView.requestFocus();
//	}
//	protected void onDestroy ()
//	{
//		mUnityPlayer.quit();
//		super.onDestroy();
//	}
//
//	// onPause()/onResume() must be sent to UnityPlayer to enable pause and resource recreation on resume.
//	protected void onPause()
//	{
//		super.onPause();
//		mUnityPlayer.pause();
//	}
//	protected void onResume()
//	{
//		super.onResume();
//		mUnityPlayer.resume();
//	}
//	public void onConfigurationChanged(Configuration newConfig)
//	{
//		super.onConfigurationChanged(newConfig);
//		mUnityPlayer.configurationChanged(newConfig);
//	}
//	public void onWindowFocusChanged(boolean hasFocus)
//	{
//		super.onWindowFocusChanged(hasFocus);
//		mUnityPlayer.windowFocusChanged(hasFocus);
//	}
//	public boolean dispatchKeyEvent(KeyEvent event)
//	{
//		if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
//			return mUnityPlayer.onKeyMultiple(event.getKeyCode(), event.getRepeatCount(), event);
//		return super.dispatchKeyEvent(event);
//	}
//}
//*/
