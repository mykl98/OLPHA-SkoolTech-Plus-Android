package com.skooltech.OLPHA_skooltechplus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationFragment extends Fragment {
    GlobalFunctions globalFunctions = new GlobalFunctions(getActivity());
    View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notification, null);

        DbHandler db0 = new DbHandler(getActivity());
        ArrayList<HashMap<String,String>> childList = db0.getChildList();

        final ArrayList<String> childId = new ArrayList<>();
        ArrayList<String> childName = new ArrayList<>();

        childName.add("ALL");
        childId.add("all");

        for (HashMap<String,String> map : childList){
            childName.add(globalFunctions.decodeText(map.get("name")));
            childId.add(map.get("id"));
        }

        Spinner spinner = view.findViewById(R.id.spinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,childName);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                renderNotificationList(childId.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    public void renderNotificationList(String id){
        DbHandler db = new DbHandler(getActivity());
        ArrayList<HashMap<String,String>> notificationList = db.getNotificationByType(id,"notification",50);

        ArrayList<HashMap<String,String>> newNotificationList = new ArrayList<>();

        int[] listviewIcon = new int[]{R.drawable.ic_action_notification,R.drawable.ic_action_announcement};

        for(HashMap<String,String> map : notificationList){
            HashMap<String,String> notification = new HashMap<>();
            notification.put("title",map.get("title"));
            notification.put("body",globalFunctions.decodeText(map.get("body")));
            if (map.get("type").equals("notification")){
                notification.put("image", Integer.toString(listviewIcon[0]));
            }else{
                notification.put("image", Integer.toString(listviewIcon[1]));
            }
            newNotificationList.add(notification);
        }

        ListView lv = view.findViewById(R.id.notification_list);
        ListAdapter adapter = new SimpleAdapter(getActivity(), newNotificationList, R.layout.notification_row,new String[]{"title","body","image"},new int[]{R.id.title,R.id.body,R.id.icon});
        lv.setAdapter(adapter);
    }
}
