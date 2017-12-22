package video.com.relavideolibrary.camera.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.io.InputStream;

/**
 * Created by hirochin on 20/12/2017.
 */

public class Constants {

    private static Constants instance;
    private Context mContext;
    private int screenWidth;
    private int screenHeight;

    private Constants(){}

    public static synchronized Constants getInstance(){
        if(instance == null){
            instance = new Constants();
        }
        return instance;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context c) {
        mContext = c;
        DisplayMetrics mDisplayMetrics = c.getResources()
                .getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        screenHeight = mDisplayMetrics.heightPixels;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public String getShaderString(String path){
        Resources mRes = mContext.getResources();
        StringBuilder result=new StringBuilder();
        try{
            InputStream is=mRes.getAssets().open(path);
            int ch;
            byte[] buffer=new byte[1024];
            while (-1!=(ch=is.read(buffer))){
                result.append(new String(buffer,0,ch));
            }
        }catch (Exception e){
            return null;
        }
        return result.toString().replaceAll("\\r\\n","\n");
    }
}
