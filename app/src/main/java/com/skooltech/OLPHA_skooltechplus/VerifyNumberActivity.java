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

    String number;
    String phoneNumber;

    PinView otpCode;
    Button registerButton;

    LinearLayout statusContainer;
    TextView statusText;

    String otp;

    String TAG = "Verify Number Activity";

    String mVerificationId;

    RequestQueue requestQueue;

    MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_number);

        sharedPreferences = getSharedPreferences("skooltech", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mAuth = FirebaseAuth.getInstance();

        phoneNumber = sharedPreferences.getString("number", "");
        number = phoneNumber.substring(1);
        number = "+63" + number;

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
                    if(mVerificationId != null){
                        statusContainer.setVisibility(View.VISIBLE);
                        registerButton.setEnabled(false);
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
            }
        });
        sendOtpCode();
    }

    private void sendOtpCode(){
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(number)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if(code != null){
                otpCode.setText(code);
                statusContainer.setVisibility(View.VISIBLE);
                registerButton.setEnabled(false);
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            otpCode.setError(e.getMessage());
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token){
            mVerificationId = verificationId;
        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyNumberActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            registerToServer();
                        }else{
                            statusContainer.setVisibility(View.GONE);
                            registerButton.setEnabled(true);
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(VerifyNumberActivity.this, "Invalid code entered", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void registerToServer(){
        String token = sharedPreferences.getString("token", "");
        editor.putString("token", token);
        editor.commit();
        HashMap<String, String> params = new HashMap<>();
        params.put("activity", "register");
        params.put("id", getResources().getString(R.string.school_code));
        params.put("number", phoneNumber);
        params.put("token", token);

        requestAPI(params, new VerifyNumberActivity.APICallback() {
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

    private void requestAPI(Map<String, String> parameters, final VerifyNumberActivity.APICallback callback){
        String url = getString(R.string.app_api);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] resp = response.split("\\*_\\*");
                        if(resp[0].equals("true")){
                            callback.onSuccess("true");
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
            requestQueue = Volley.newRequestQueue(this);
        }
        requestQueue.getCache().clear();
        requestQueue.add(postRequest);
    }

    public interface APICallback{
        void onSuccess(String result);

        void onError(String error);
    }
}
