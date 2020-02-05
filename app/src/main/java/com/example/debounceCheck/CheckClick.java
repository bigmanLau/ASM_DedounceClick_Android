package com.example.debounceCheck;


import android.util.Log;

public class CheckClick {
    public static int lastClickId=0;
    public static long lastClickTime=0;
    public static boolean checkIsClicked(int resourceId){
        if(lastClickId==resourceId){
            if(System.currentTimeMillis()-lastClickTime<300L){
               Log.d("CheckClick","刚才点击过哦");
                return true;
            }
        }
        lastClickId=resourceId;
        lastClickTime=System.currentTimeMillis();
        return false;
    }
}
