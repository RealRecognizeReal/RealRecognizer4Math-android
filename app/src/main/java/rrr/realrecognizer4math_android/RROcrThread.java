package rrr.realrecognizer4math_android;

import android.util.Pair;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * Created by waps12b on 16. 12. 13..
 */
public class RROcrThread extends Thread {

    private static Map<Long, CharacterData> MapCode2Character = null;
    private static Map<Long, ArrayList< OcrData > > MapCode2OcrData = null;

    public static synchronized void Init(Map<Long, CharacterData> mapCode2Character, Map<Long, ArrayList< OcrData > > mapCode2OcrData)
    {
        MapCode2Character = mapCode2Character;
        MapCode2OcrData = mapCode2OcrData;
    }

    public interface OnOcrListener {
        void OnResult(int id, boolean result, String name);
    }


    private Mat img;
    private final int id;
    private OnOcrListener onOcrListener = null;
    private PriorityQueue<Pair<Integer , String> > q = new PriorityQueue<>(10, new Comparator<Pair<Integer, String>>() {
        @Override
        public int compare(Pair<Integer, String> lhs, Pair<Integer, String> rhs) {
            return - (lhs.first - rhs.first);
        }
    });


    public RROcrThread(Mat img, int id, OnOcrListener onOcrListener)
    {
        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;
        if(img.rows() < img.cols())
        {
            int d = img.cols() - img.rows();
            top = d / 2;
            bottom = d - top;
        }else if(img.cols() < img.rows())
        {
            int d = img.rows() - img.cols();
            left = d / 2;
            right = d - left;
        }

        int size =Math.max(img.rows(), img.cols());
        Mat black = Mat.zeros(size ,size, CvType.CV_8UC1);
        img.copyTo(black.colRange(left+1, size-right).rowRange(top+1, size-bottom));
        img = black;
        Imgproc.resize(img, img, new Size(32, 32));
        this.img = img;
        this.id = id;
        this.onOcrListener = onOcrListener;
    }

    @Override
    public void run() {
        for(ArrayList<OcrData> datas : MapCode2OcrData.values())
        {
            for(OcrData d : datas)
            {
                int differ = 0;
                boolean insert = true;
                for(int r = 0 ; r < 32 && insert; r++)
                {
                    for(int c = 0; c < 32 && insert; c++)
                    {
                        long bit  = 31 - c;
                        boolean a = (d.blocks[r] >> (31-bit)) > 0 ;
                        boolean b = this.img.get(r,c)[0] > 0;
                        if(a != b)
                        {
                            differ++;
                            if(!q.isEmpty())
                            {
                                if(differ >= q.peek().first)
                                {
                                    insert = false;
                                    break;
                                }
                            }
                        }

                    }
                }
                if(!insert)
                    continue;

                q.add(new Pair<Integer, String>(differ, MapCode2Character.get(d.code).name ));
                if(q.size() > 2)
                    q.poll();

                if(!q.isEmpty() && q.peek().first < 32) break;
            }
            if(!q.isEmpty() && q.peek().first < 32) break;
        }

        Map<String, Integer> counter = new TreeMap<>();
        while (!q.isEmpty())
        {
            String name = q.peek().second;
            int dist = q.peek().first;
            q.poll();

            if(!counter.containsKey(name))
                counter.put(name, 0);

            counter.put(name, counter.get(name) + dist * dist);
        }

        String answer = null;
        int min = Integer.MAX_VALUE;
        for(String name : counter.keySet())
        {
            int distance = counter.get(name);
            if(distance < min)
            {
                min = distance;
                answer = name;
            }
        }

        if(onOcrListener!=null)
            onOcrListener.OnResult(this.id, (answer!=null), answer);
    }

    public static class OcrData
    {
        private long code;
        private long[] blocks;
        public OcrData(long code, long[] blocks)
        {
            this.code = code;
            this.blocks = blocks;
        }

    }

    public static class CharacterData
    {
        private long code;
        private String type;
        private String name;
        public CharacterData(long code, String type, String name)
        {
            this.code = code;
            this.type = type;
            this.name = name;
        }
    }
}
