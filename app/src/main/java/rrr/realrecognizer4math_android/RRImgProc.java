package rrr.realrecognizer4math_android;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

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
}
