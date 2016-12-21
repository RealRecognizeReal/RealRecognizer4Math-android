package rrr.realrecognizer4math_android;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by waps12b on 16. 12. 13..
 */
public class RRApplication extends Application {
    private static final String TAG = "RRApplication";

    public static final AtomicBoolean DataLoaded = new AtomicBoolean(false);


    @Override
    public void onCreate() {
        Log.i(TAG, "RRApplication starts...");
        super.onCreate();

        loadOpenCV();
        loadRawFiles();
    }

    private void loadOpenCV()
    {
        Log.i(TAG, "OpenCV Loading...");
        boolean cvResult = RRManager.initOpenCV();
        if(!cvResult)
        {
            Log.e(TAG, "OpenCV Loading failed!!!!!");
        }
    }


    private static final Set<String> TARGET_CHARACTER_NAMES = new HashSet<String>( );
    static {
        String[] names = new String[]{ "plus", "minus", "LeftBracket", "RightBracket", "LeftBrace", "RightBrace", "LeftPar", "RightPar" };
        for(String name : names)
            TARGET_CHARACTER_NAMES.add(name);
    }


    private void loadRawFiles()
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Resources res = getResources();
                InputStream is = null;
                BufferedReader reader = null;
                String line = null;
                //

                try
                {
                    is = res.openRawResource(R.raw.code);
                    reader = new BufferedReader(new InputStreamReader(is));

                    Map<Long, RROcrThread.CharacterData> mapCode2CharacterData = new TreeMap<>();
                    while( (line=reader.readLine()) != null )
                    {
                        String[] splited = line.trim().split(" ");
                        long code = Long.parseLong(splited[0].replace("0x", "0"), 16);
                        String type = splited[1];
                        String name = splited[2];
                        if(name.matches("[a-zA-Z0-9]") || TARGET_CHARACTER_NAMES.contains(name))
                        {
                            RROcrThread.CharacterData c = new RROcrThread.CharacterData(code  , type, name);
                            mapCode2CharacterData.put(code,c);
                        }
                    }
                    reader.close();
                    is.close();


                    is = res.openRawResource(R.raw.pixel);
                    reader = new BufferedReader(new InputStreamReader(is));
                    Map<Long, ArrayList<RROcrThread.OcrData> > mapCode2OcrData = new TreeMap<>();
                    while( (line=reader.readLine()) != null )
                    {
                        String[] splited = line.trim().split(",");
                        long code =  Long.parseLong(splited[1], 16);
                        if(!mapCode2CharacterData.containsKey(code))
                            continue;

                        if(!mapCode2OcrData.containsKey(code))
                            mapCode2OcrData.put(code, new ArrayList<RROcrThread.OcrData>());

                        if(mapCode2OcrData.get(code).size() >= 30)
                            continue;

                        long[] blocks = new long[32];
                        for(int r = 0 ; r < 32; r++)
                        {
                            blocks[r] = Long.parseLong(splited[2+r]);
                        }

                        RROcrThread.OcrData d = new RROcrThread.OcrData(code, blocks);
                        mapCode2OcrData.get(code).add(d);
                    }
                    reader.close();
                    is.close();

                    RROcrThread.Init(mapCode2CharacterData, mapCode2OcrData);
                    Log.i(TAG, "[OCR DATA] Loaded OCR DATA Successfully ");
                    DataLoaded.set(true);
                }catch (Exception ex)
                {
                    Log.e(TAG, "[OCR DATA] Failed to read OCR resources...!!!! ");
                    ex.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void requestCameraPermission()
    {

    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
}
