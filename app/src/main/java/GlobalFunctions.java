import android.content.Context;

public class GlobalFunctions {
    Context mContext;

    // constructor
    public GlobalFunctions(Context context){
        this.mContext = context;
    }

    public String URLDecodeText(String text){
        String decodedText = text.replace("%20", " ");
        return decodedText;
    }

}
