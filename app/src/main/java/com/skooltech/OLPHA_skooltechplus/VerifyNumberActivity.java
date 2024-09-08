package com.skooltech.OLPHA_skooltechplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerifyNumberActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    FirebaseAuth mAuth;

    String phoneNumber;

    PinView otpCode;
    Button registerButton;

    LinearLayout statusContainer;
    TextView statusText;

    String otp;

    String TAG = ">>>>>Verify Number Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_number);

        sharedPreferences = getSharedPreferences("skooltech", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mAuth = FirebaseAuth.getInstance();

        phoneNumber = sharedPreferences.getString("number", "");
        sendOtpCode();

        statusContainer = findViewById(R.id.verify_activity_status_container);
        statusText = findViewById(R.id.verify_activity_status_text);

        otpCode = findViewById(R.id.verify_activity_otp_edittext);
        otpCode.requestFocus();
        otpCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                otp = "";
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                otp = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        registerButton = findViewById(R.id.verify_activity_register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(otp.isEmpty()){
                    otpCode.setError("OTP Code field should not be empty!");
                }if(otp.length() != 6){
                    otpCode.setError("Invalid OTP Ccode");
                }else{
                    statusContainer.setVisibility(View.VISIBLE);
                    registerButton.setEnabled(false);

                    HashMap<String, String> params = new HashMap<>();
                    params.put("activity", "validateotp");
                    params.put("id", getResources().getString(R.string.school_code));
                    params.put("otp", otp);
                    params.put("number", phoneNumber);

                    MainActivity.instance.requestAPI(params, new MainActivity.APICallback() {
                        @Override
                        public void onSuccess(String result) {
                            if(result.equals("true")){
                                registerToServer();
                            }else{
                                otpCode.setError(result);
                                statusContainer.setVisibility(View.INVISIBLE);
                                registerButton.setEnabled(true);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            otpCode.setError(error);
                        }
                    });

                }
            }
        });
    }

    private void sendOtpCode(){
        HashMap<String, String> params = new HashMap<>();
        params.put("activity", "sendotp");
        params.put("id", getResources().getString(R.string.school_code));
        params.put("number", phoneNumber);

        MainActivity.instance.requestAPI(params, new MainActivity.APICallback() {
            @Override
            public void onSuccess(String result) {
                if(!result.equals("true")){
                    otpCode.setError(result);
                }
            }

            @Override
            public void onError(String error) {
                otpCode.setError(error);
            }
        });
    }

    private void registerToServer(){
        String token = sharedPreferences.getString("token", "");
        if(token.isEmpty()){
            statusContainer.setVisibility(View.GONE);
            MainActivity.instance.showOkDialog(false, "OTP Verification failed!\nTry Again");
            return;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("activity", "register");
        params.put("id", getResources().getString(R.string.school_code));
        params.put("number", phoneNumber);
        params.put("token", token);

        MainActivity.instance.requestAPI(params, new MainActivity.APICallback(){

            @Override
            public void onSuccess(String result) {
                if(result.equals("true")){
                    statusContainer.setVisibility(View.GONE);
                    editor.putString("isLoggedIn", "true");
                    editor.commit();
                    gotoMainActivity();
                }else{
                    MainActivity.instance.showOkDialog(false, "Registration failed!");
                }
            }

            @Override
            public void onError(String error) {
                statusContainer.setVisibility(View.GONE);
            }
        });

    }

    private void gotoMainActivity(){
        Intent intent = new Intent(VerifyNumberActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
