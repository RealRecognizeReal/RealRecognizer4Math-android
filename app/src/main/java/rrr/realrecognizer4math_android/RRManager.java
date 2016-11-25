package rrr.realrecognizer4math_android;

import android.util.Log;

import org.opencv.android.OpenCVLoader;

/**
 * Created by waps12b on 16. 11. 26..
 */
public abstract class RRManager {
    private static final String TAG = "RRManager";


    public static boolean initOpenCV()
    {
        boolean success = OpenCVLoader.initDebug();
        if(success)
            Log.i(TAG, "OpenCV Loaded Succesfully");
        else
            Log.e(TAG, "OpenCV Loading Failed!!!!!!");
        return success;
    }

}
