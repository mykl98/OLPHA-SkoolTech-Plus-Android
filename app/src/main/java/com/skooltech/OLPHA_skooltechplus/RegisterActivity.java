package com.skooltech.OLPHA_skooltechplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity{
    RequestQueue requestQueue;

    EditText phoneNumber;
    Button continueButton;

    LinearLayout statusContainer;
    TextView statusText;

    String TAG = "MainActivity";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[] {android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        sharedPreferences = getSharedPreferences("skooltech", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        phoneNumber = findViewById(R.id.register_activity_phone_number);
        continueButton = findViewById(R.id.register_activity_continue_button);
        statusContainer = findViewById(R.id.register_activity_status_container);
        statusText = findViewById(R.id.register_activity_status_text);

        phoneNumber.requestFocus();

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = phoneNumber.getText().toString().trim();
                showStatus("Checking phone number... ");
                if(number.isEmpty()){
                    phoneNumber.setError("Phone number field should not be empty!");
                }else if(number.length() != 11){
                    phoneNumber.setError("Invalid phone number!");
                }else{
                    continueButton.setEnabled(false);
                    HashMap<String, String> params= new HashMap<>();
                    params.put("activity", "validate");
                    params.put("id", getResources().getString(R.string.school_code));
                    params.put("number", number);

                    requestAPI(params, new APICallback() {
                        @Override
                        public void onSuccess(String result) {
                            continueButton.setEnabled(true);
                            if(result.equals("true")){
                                editor.putString("number", number);
                                editor.commit();
                                gotoVerifyNumberActivity();
                            }else if(result.equals("false")){
                                phoneNumber.setError("Unauthorized phone number!");
                            }
                            hideStatus();
                        }

                        @Override
                        public void onError(String error) {
                            continueButton.setEnabled(true);
                            phoneNumber.setError(error);
                            hideStatus();
                        }
                    });
                }
            }
        });
    }

    private void gotoVerifyNumberActivity(){
        Intent intent = new Intent(RegisterActivity.this, VerifyNumberActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showStatus(String text){
        statusContainer.setVisibility(View.VISIBLE);
        statusText.setText(text);
    }

    private void hideStatus(){
        statusContainer.setVisibility(View.GONE);
    }

    private void requestAPI(Map<String, String> parameters, final RegisterActivity.APICallback callback){
        String url = getString(R.string.app_api);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] resp = response.split("\\*_\\*");
                        if(resp[0].equals("true")){
                            callback.onSuccess("true");
                        }else if(resp[0].equals("false")){
                            callback.onSuccess("false");
                        }else{
                            callback.onError(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.getLocalizedMessage());
                    }
                }
        ){
            @Override
            protected Map<String,String> getParams() {
                return parameters;
            }
        };

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(RegisterActivity.this);
        }
        requestQueue.getCache().clear();
        requestQueue.add(postRequest);
    }

    public interface APICallback{
        void onSuccess(String result);

        void onError(String error);
    }
}

