package com.skooltech.OLPHA_skooltechplus;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    View view;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    private Dialog dialog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, null);

        settings  = getActivity().getSharedPreferences("app",Context.MODE_PRIVATE);
        editor = settings.edit();

        ArrayList<HashMap<String,String>> configList = new ArrayList<>();

        HashMap<String,String> config = new HashMap<>();
        config.put("title","SERVER SYNC");
        config.put("body","Use this to re-sync your phone to SkoolTech Solutions Server. Re-sync only when you enroll an additional child to school or if you are not receiving notification from SkoolTech Solution Server.");
        configList.add(config);

        config = new HashMap<>();
        config.put("title","CLEAR HISTORY");
        config.put("body","Use this to clear all the data you have received from SkoolTech Solution Server. Clear your data only if necessary.");
        configList.add(config);

        ListView lv = view.findViewById(R.id.config_list);
        ListAdapter adapter = new SimpleAdapter(getActivity(), configList, R.layout.me_row,new String[]{"title","body"},new int[]{R.id.title,R.id.body});
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String card = String.valueOf(position);
                if(card.equals("0")){
                    showInputDialog("Confirmation","You are about to re-sync your app to SkoolTech server. Input your mobile number you have registered below to proceed.", "resync");
                }else if(card.equals("1")){
                    showInputDialog("Confirmation","You are about to clear the data you have receive from SkoolTech server. Input your mobile number you have registered below to proceed.", "clear");
                }

            }
        });
        return view;
    }

    private void showSuccessDialog(String title, String body) {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.success_dialog);
        dialog.show();

        final TextView dialogTitle = dialog.findViewById(R.id.textviewSuccessDialogtitle);
        final TextView dialogBody = dialog.findViewById(R.id.textviewSuccessDialogbody);
        final Button dialogBtn = dialog.findViewById(R.id.buttonSuccessDialogOk);

        dialogTitle.setText(title);
        dialogBody.setText(body);

        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void showInputDialog(String title, String body, final String action) {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.input_dialog);
        dialog.show();

        final TextView dialogTitle = dialog.findViewById(R.id.textviewInputDialogtitle);
        final TextView dialogBody = dialog.findViewById(R.id.textviewInputDialogbody);
        final Button dialogBtn = dialog.findViewById(R.id.buttonInputDialogOk);

        dialogTitle.setText(title);
        dialogBody.setText(body);

        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText dialogNumber = dialog.findViewById(R.id.editTextInputNumber);
                final String number = dialogNumber.getText().toString();

                if(number.isEmpty() || number.length() < 11){
                    dialogNumber.setError("Enter a valid mobile number");
                    dialogNumber.requestFocus();
                    return;
                }
                String savedNumber = settings.getString("number","");
                if (!number.equals(savedNumber)){
                    dialogNumber.setError("This is not the number you use to register");
                    dialogNumber.requestFocus();
                    return;
                }

                if(action.equals("resync")){
                    registerToServer(number);
                    dialog.dismiss();
                }else if(action.equals("clear")){
                    DbHandler dbHandler = new DbHandler(getActivity());
                    dbHandler.clearDatabase();
                    dialog.dismiss();
                    showSuccessDialog("Success","Fresh as new! Data are successfully cleared.");
                }
            }
        });
    }

    private void registerToServer(final String number){
        String url = getString(R.string.app_api);
        final String schoolCode = getString(R.string.school_code);

        //get token
        MyFirebaseMessagingService mFMS = new MyFirebaseMessagingService();
        final String token = MyFirebaseMessagingService.getToken(getActivity());

        setSetting("syncDate", getDate());

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("true")){
                            showSuccessDialog("Success","Hooray! Your phone is successfully re-synced to SkoolTech Server!");
                        }else{
                            showSuccessDialog("Error","Oh no! Your phone record is not found in the SkoolTech server. Please contact admin.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showSuccessDialog("Error", "We have encountered an error while we re-sync your phone to SkoolTech Server. Please check your internet connection.");
                        //Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();/
                    }
                }
        ){
            @Override
            protected Map<String,String> getParams()
            {
                Map<String,String> params = new HashMap<String, String>();
                params.put("activity","register");
                params.put("id",schoolCode);
                params.put("number",number);
                params.put("token", token);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.getCache().clear();
        requestQueue.add(postRequest);
    }

    private void setSetting(String tag, String value){
        editor.putString(tag,value);
        editor.commit();
    }

    private String getSetting(String tag){
        String value = settings.getString(tag,"");
        return value;
    }

    private String getDate(){
        Calendar cal = Calendar.getInstance();
        String date = String.valueOf(cal.get(Calendar.DATE));
        return date;
    }
}
