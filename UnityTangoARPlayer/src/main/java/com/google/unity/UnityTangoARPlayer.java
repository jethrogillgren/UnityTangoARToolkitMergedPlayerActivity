package com.google.unity;

import android.os.Bundle;
import android.util.Log;

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


}

