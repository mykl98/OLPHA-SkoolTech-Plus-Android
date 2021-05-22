package com.skooltech.skooltechsolutionsacademy;

import android.content.Context;

public class GlobalFunctions {
    Context mContext;

    public GlobalFunctions(Context context){
        this.mContext = context;
    }

    public String decodeText(String inText){
        String decodedText = inText.replace("%2520", " ");
        return decodedText;
    }
}
