package rrr.realrecognizer4math_android;

import android.hardware.Camera;
import android.util.Log;
import android.util.Pair;

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

    public static Pair<Integer, Integer> getResolution()
    {

            int noOfCameras = Camera.getNumberOfCameras();
            float maxResolution = -1;
            long pixelCount = -1;

        int width = 640;
        int height = 480;

            for (int i = 0;i < noOfCameras;i++)
            {

                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(i, cameraInfo);

                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    Camera camera = Camera.open(i);;
                    Camera.Parameters cameraParams = camera.getParameters();
                    for (int j = 0;j < cameraParams.getSupportedPictureSizes().size();j++)
                    {
                        width = Math.max(width, cameraParams.getSupportedPictureSizes().get(j).width);
                        height = Math.max(height,  cameraParams.getSupportedPictureSizes().get(j).height);
                    }


                    camera.release();
                }
            }

            return new Pair<Integer, Integer>(width, height);

    }

}
