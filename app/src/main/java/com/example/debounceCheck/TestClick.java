package com.example.debounceCheck;

import android.view.View;
import com.example.debounceCheck.CheckClick;

public class TestClick implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        if( CheckClick.checkIsClicked(v.getId())){
            return;
        }
    }
}
