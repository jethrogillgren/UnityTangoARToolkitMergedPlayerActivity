package com.google.unity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by jethro on 31/01/2017.
 */

public class UnityTangoARPlayer extends GoogleUnityActivity {

    protected final static String TAG = "J#  UnityTangoARPlayer";

    // Setup activity layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "LIFECYCLE  onCreate called");
    }

    public View getAndroidViewLayer() {
        return findViewById(R.id.android_view_container);
    }

}

