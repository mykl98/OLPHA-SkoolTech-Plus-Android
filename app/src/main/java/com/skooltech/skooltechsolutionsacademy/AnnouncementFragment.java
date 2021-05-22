package com.skooltech.skooltechsolutionsacademy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;

public class AnnouncementFragment extends Fragment {
    GlobalFunctions globalFunctions = new GlobalFunctions(getActivity());
    View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_announcement, null);

        renderNotificationList("all");

        return view;
    }

    public void renderNotificationList(String id){
        DbHandler db = new DbHandler(getActivity());
        ArrayList<HashMap<String,String>> notificationList = db.getNotificationByType(id,"announcement",50);

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
