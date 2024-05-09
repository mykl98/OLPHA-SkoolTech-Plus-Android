package com.skooltech.OLPHA_skooltechplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;

public class ProfileFragment extends Fragment {
    int option = 0;
    CardView serverSync;
    CardView clearHistory;
    CardView logout;

    String number;
    SharedPreferences sharedPreferences;

    String TAG = "Profile Fragment";

    AlertDialog confirmDialog;

    LinearLayout confirmAnimation;
    Button okButton;

    DbHandler db;
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = new DbHandler(getContext());

        sharedPreferences = getContext().getSharedPreferences("skooltech", Context.MODE_PRIVATE);
        number = sharedPreferences.getString("number", "");

        serverSync = view.findViewById(R.id.fragment_profile_resync);
        clearHistory = view.findViewById(R.id.fragment_profile_clearhistory);
        logout = view.findViewById(R.id.fragment_profile_logout);

        serverSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option = 0;
                showConfirmationDialog("You're about to re-sync your app with the SchoolTech server. Please enter the mobile number you've registered below to proceed.");
            }
        });

        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option = 1;
                showConfirmationDialog("You're about to clear the data received from the SkoolTech server. Please enter the mobile number you've registered below to proceed.");
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option = 2;
                showConfirmationDialog("As a confirmation, you're about to log out. Please enter the mobile number you've registered below.");
            }
        });

        return view;
    }

    private void showConfirmationDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View customLayout = getActivity().getLayoutInflater().inflate(R.layout.confirmation_dialog_layout, null);
        builder.setView(customLayout);

        TextView msgTextview = customLayout.findViewById(R.id.confirmation_dialog_layout_body);
        EditText numEditText = customLayout.findViewById(R.id.confirmation_dialog_layout_number);
        numEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        okButton = customLayout.findViewById(R.id.confirmation_dialog_layout_okbtn);
        confirmAnimation = customLayout.findViewById(R.id.confimation_dialog_progressbar);
        confirmDialog = builder.create();

        msgTextview.setText(message);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = numEditText.getText().toString();
                if(num.isEmpty()){
                    numEditText.setError("Phone number field should not be empty!");
                }else if(num.length() != 11){
                    numEditText.setError("Invalid phone number!");
                }else if(!num.equals(number)){
                    numEditText.setError("The phone number you provided does not match to the registered phone number!");
                }else{
                    if(option == 0){
                        serverSync();
                    }else if(option == 1){
                        clearHistory();
                    }else if(option == 2){
                        MainActivity.instance.signOut();
                    }
                }
            }
        });
        confirmDialog.show();
    }

    private void serverSync(){
        confirmAnimation.setVisibility(View.VISIBLE);
        okButton.setEnabled(false);
        String token = sharedPreferences.getString("token", "");
        HashMap<String, String> params = new HashMap<>();
        params.put("activity", "register");
        params.put("id", getResources().getString(R.string.school_code));
        params.put("number", number);
        params.put("token", token);

        ((MainActivity) getActivity()).requestAPI(params, new MainActivity.APICallback() {
            @Override
            public void onSuccess(String result) {
                confirmAnimation.setVisibility(View.INVISIBLE);
                confirmDialog.dismiss();
                okButton.setEnabled(true);
                if(result.equals("true")){
                    ((MainActivity) getActivity()).showOkDialog(true, "Successfully re-sync to server!");
                }else{
                    ((MainActivity) getActivity()).showOkDialog(false, "Server re-Sync failed!");
                }
            }

            @Override
            public void onError(String error) {
                confirmAnimation.setVisibility(View.INVISIBLE);
                confirmDialog.dismiss();
                okButton.setEnabled(true);
                ((MainActivity) getActivity()).showOkDialog(false, error);
            }
        });
    }

    private void clearHistory(){
        db.clearDatabase();
        confirmDialog.dismiss();
        ((MainActivity) getActivity()).showOkDialog(true, "Successfully cleared history!");
    }
}
