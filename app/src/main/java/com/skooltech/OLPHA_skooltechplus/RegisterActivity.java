package com.skooltech.OLPHA_skooltechplus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity{

    private EditText editTextNumber;
    private Button buttonContinue;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextNumber = findViewById(R.id.editTextNumber);
        buttonContinue = findViewById(R.id.buttonContinue);

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNumber();
            }
        });

        progressBar = findViewById(R.id.progressBarValidating);
        progressBar.setVisibility(View.GONE);
    }

    public void checkNumber(){
        progressBar.setVisibility(View.VISIBLE);
        final String number = editTextNumber.getText().toString().trim();

        if(number.isEmpty() || number.length() < 11){
            editTextNumber.setError("Enter a valid mobile number");
            editTextNumber.requestFocus();
            return;
        }

        String url = getString(R.string.app_api);
        final String schoolCode = getString(R.string.school_code);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        if(response.equals("false")){
                            editTextNumber.setError("Unauthorized number. Use the number you provide during enrollment.");
                            editTextNumber.requestFocus();
                        }else if(response.equals("true")){
                            Intent intent = new Intent(RegisterActivity.this,VerifyPhoneActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("number",number);
                            startActivity(intent);
                        }else{
                            Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Override
            protected Map<String,String> getParams()
            {
                Map<String,String> params = new HashMap<String, String>();
                params.put("activity","validate");
                params.put("id",schoolCode);
                params.put("number",number);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
        requestQueue.getCache().clear();
        requestQueue.add(postRequest);
    }
}
