//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.google.unity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.SurfaceHolder.Callback2;
import android.view.ViewGroup.LayoutParams;
import com.google.unity.R.id;
import com.google.unity.R.layout;
import com.unity3d.player.UnityPlayer;

public class GoogleUnityActivity extends Activity implements OnRequestPermissionsResultCallback {
    protected UnityPlayer mUnityPlayer;
    protected GoogleUnityActivity.AndroidLifecycleListener mAndroidLifecycleListener;
    protected boolean mIsUnityQuit = false;

    public GoogleUnityActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(1);
        super.onCreate(savedInstanceState);
        this.setContentView(layout.activity_main);
        this.getWindow().takeSurface((Callback2)null);
        this.setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        this.getWindow().setFormat(4);
        DisplayManager displayManager = (DisplayManager)this.getSystemService(Context.DISPLAY_SERVICE);
        if(displayManager != null) {
            displayManager.registerDisplayListener(new DisplayListener() {
                public void onDisplayAdded(int displayId) {
                }

                public void onDisplayChanged(int displayId) {
                    synchronized(this) {
                        if(GoogleUnityActivity.this.mAndroidLifecycleListener != null) {
                            GoogleUnityActivity.this.mAndroidLifecycleListener.onDisplayChanged();
                        }

                    }
                }

                public void onDisplayRemoved(int displayId) {
                }
            }, (Handler)null);
        }

        this.mUnityPlayer = new UnityPlayer(this);
        if(this.mUnityPlayer.getSettings().getBoolean("hide_status_bar", true)) {
            this.getWindow().setFlags(1024, 1024);
        }

        ((ViewGroup)this.findViewById(android.R.id.content)).addView(this.mUnityPlayer.getView(), 0);
        this.mUnityPlayer.requestFocus();
    }

    public void showAndroidViewLayer(final View view) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                ViewGroup androidViewContainer = (ViewGroup)GoogleUnityActivity.this.findViewById(id.android_view_container);
                androidViewContainer.removeAllViews();
                androidViewContainer.addView(view, new LayoutParams(-1, -1));
            }
        });
    }

    public void launchIntent(String packageName, String className, String[] args, int requestcode) {
        Intent intent = new Intent();
        intent.setClassName(packageName, className);
        if(args != null) {
            for(int i = 0; i < args.length; ++i) {
                String[] keyVal = args[i].split(":");
                if(keyVal.length == 2) {
                    intent.putExtra(keyVal[0], keyVal[1]);
                }
            }
        }

        this.startActivityForResult(intent, requestcode);
    }

    public boolean checkAndroidPermission(String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
        return permissionCheck == 0;
    }

    public void requestAndroidPermissions(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    public boolean shouldShowRequestAndroidPermissionRationale(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
    }

    public void launchApplicationDetailsSettings() {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        Uri uri = Uri.fromParts("package", this.getPackageName(), (String)null);
        intent.setData(uri);
        this.startActivity(intent);
    }

    public void LaunchIntent(String packageName, String className, String[] args, int requestcode) {
        this.launchIntent(packageName, className, args, requestcode);
    }

    public void attachLifecycleListener(GoogleUnityActivity.AndroidLifecycleListener listener) {
        this.mAndroidLifecycleListener = listener;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(this.mAndroidLifecycleListener != null) {
            this.mAndroidLifecycleListener.onActivityResult(requestCode, resultCode, data);
        }

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(this.mAndroidLifecycleListener != null) {
            this.mAndroidLifecycleListener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    protected void onDestroy() {
        this.mUnityPlayer.quit();
        this.mIsUnityQuit = true;
        super.onDestroy();
    }

    protected void onStart() {
        super.onStart();
        if(this.mAndroidLifecycleListener != null) {
            this.mAndroidLifecycleListener.onStart();
        }

    }

    protected void onStop() {
        super.onStop();
        if(this.mAndroidLifecycleListener != null) {
            this.mAndroidLifecycleListener.onStop();
        }

    }

    protected void onPause() {
        super.onPause();
        if(this.mAndroidLifecycleListener != null) {
            this.mAndroidLifecycleListener.onPause();
        }

        if(!this.mIsUnityQuit) {
            this.mUnityPlayer.pause();
        }

    }

    protected void onResume() {
        super.onResume();
        if(this.mAndroidLifecycleListener != null) {
            this.mAndroidLifecycleListener.onResume();
        }

        if(!this.mIsUnityQuit) {
            this.mUnityPlayer.resume();
        }

    }

    public void logAndroidErrorMessage(String message) {
        Log.e(this.getPackageName(), message);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(!this.mIsUnityQuit) {
            this.mUnityPlayer.configurationChanged(newConfig);
        }

    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!this.mIsUnityQuit) {
            this.mUnityPlayer.windowFocusChanged(hasFocus);
        }

    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return event.getAction() == 2?this.mUnityPlayer.injectEvent(event):super.dispatchKeyEvent(event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return this.mUnityPlayer.injectEvent(event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.mUnityPlayer.injectEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return this.mUnityPlayer.injectEvent(event);
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return this.mUnityPlayer.injectEvent(event);
    }

    public interface AndroidLifecycleListener {
        void onStart();

        void onStop();

        void onPause();

        void onResume();

        void onActivityResult(int var1, int var2, Intent var3);

        void onRequestPermissionsResult(int var1, String[] var2, int[] var3);

        void onDisplayChanged();
    }
}