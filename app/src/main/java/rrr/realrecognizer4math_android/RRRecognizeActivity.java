package rrr.realrecognizer4math_android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created by waps12b on 16. 12. 13..
 */
public class RRRecognizeActivity extends Activity {

    private Mat img = new Mat();
    private ImageView imageView;
    private TextView textView;

    private static final String TAG = "RRRecognizeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);
        imageView = (ImageView) findViewById(R.id.image_view);
        textView = (TextView) findViewById(R.id.text_view);

        Intent intent = getIntent();
        Uri imageUri = intent.getData();

        if(imageUri == null)
        {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        loadImage(imageUri);
        showImage(img);
        runOCR();
    }


    private ArrayList<String> results = new ArrayList<>();
    private void runOCR() {
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hi = new Mat();
        Imgproc.findContours(img, contours, hi, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_KCOS, new Point(0, 0));

        ArrayList<MatOfPoint> poly = new ArrayList<>();

        MatOfPoint2f approxCurve = new MatOfPoint2f();
        int id = 0;
        for (int i = 0; i < contours.size(); i++)
        {
            MatOfPoint2f contours2f = new MatOfPoint2f( contours.get(i).toArray());
            Imgproc.approxPolyDP( contours2f, approxCurve, 3, true);

            MatOfPoint points = new MatOfPoint( approxCurve.toArray() );

            final Rect rect = Imgproc.boundingRect(points);
            if(rect.height <= 5 && rect.width <= 5 )
                continue;

            Mat cmat = img.submat( rect );
            results.add(null);

            RROcrThread thread = new RROcrThread(cmat, id++, new RROcrThread.OnOcrListener() {
                @Override
                public void OnResult(int id, boolean result, String name) {
                    results.set(id, name);
                    handler.sendEmptyMessage(0);
                }
            });
            thread.start();
        }
    }

    private Handler handler = new Handler()
    {
        @Override
        public synchronized void handleMessage(Message msg) {
            StringBuilder builder = new StringBuilder();
            for(int i = 0 ; i < results.size(); i++)
            {
                if(results.get(i) == null) continue;
                builder.append(results.get(i));
                builder.append(" ");
            }
            textView.setText( builder.toString() );
        }
    };

    private void loadImage(Uri uri)
    {
        Log.i(TAG, "loadImage() begins... ");
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bmp32, img);
            Core.flip(img.t(), img, 1);
//            Imgproc.resize(img, img, new Size(img.width() * 2, img.height() * 2));
            img = RRImgProc.Simplify(img);
        }catch (Exception ex)
        {
            Log.e(TAG, "failed to load image...");
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    private void showImage(Mat mat)
    {
        // convert to bitmap:
        Bitmap bm = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bm);

        imageView.setImageBitmap(bm);
    }


}
