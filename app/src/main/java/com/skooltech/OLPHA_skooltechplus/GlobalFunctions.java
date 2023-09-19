package com.skooltech.OLPHA_skooltechplus;

import android.content.Context;

public class GlobalFunctions {
    Context mContext;

    public GlobalFunctions(Context context){
        this.mContext = context;
    }

    public String decodeText(String inText){
        String decodedText = inText.replace("%2520", " ");
        decodedText = decodedText.replace("\\n", "\n");
        return decodedText;
    }
}
