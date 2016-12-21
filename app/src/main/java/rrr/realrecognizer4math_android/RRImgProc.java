package rrr.realrecognizer4math_android;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created by waps12b on 16. 11. 26..
 */
public abstract class RRImgProc {


    public static final Size SIMPLIFY_BLUR_SIZE = new Size(3,3);

    public static Mat Simplify(Mat mat)
    {
        Mat img = new Mat();

        mat.copyTo(img);
        if(img.type() != CvType.CV_8UC1)
        {
            Imgproc.cvtColor(img, img , Imgproc.COLOR_RGBA2GRAY);
        }
        Imgproc.GaussianBlur(img, img, SIMPLIFY_BLUR_SIZE, 0);
        Imgproc.adaptiveThreshold(img, img, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 75, 10);
        Core.bitwise_not(img, img);
        return img;
    }

    public static final int SPLIT_MINIMUM_WIDTH = 5;

    public static ArrayList<Mat> SplitsElemntsHH(Mat img)
    {
        ArrayList<Mat> array = new ArrayList<>();

        boolean isOn = false;
        int left = 0;
        int right = 0 ;
        for(int x = 0;  x < img.cols(); x++)
        {
            boolean isWhite = false;
            for(int y = 0 ; y < img.rows(); y++)
            {
                if( img.get(y,x)[0] != 0 )
                {
                    isWhite = true;
                    break;
                }
                if(isOn == false && isWhite == true)
                {
                    left = x;
                    right = x;
                    isOn = true;
                }
                if(isOn == true && isWhite == true)
                {
                    right = x;
                }
                if(isOn == true && isWhite == false)
                {
                    int width = right - left + 1;
                    if(width >= SPLIT_MINIMUM_WIDTH)
                        array.add(img.submat(left, 0, right - left + 1, img.rows()));
                    isOn = false;
                }
            }
        }
        return array;
    }
}
