package com.bigman.clicksupport;

import android.util.Log;
import android.view.View;

public class CheckClick {
    public static int lastClickId=0;
    public static long lastClickTime=0;
    public static boolean checkIsClicked(View view){
        if(lastClickId==view.getId()){
            if(System.currentTimeMillis()-lastClickTime<300L){
                Log.d("CheckClick","刚才点击过哦");
                return true;
            }
        }
        lastClickId=view.getId();
        lastClickTime=System.currentTimeMillis();
        return false;
    }
}
