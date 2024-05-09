package com.skooltech.OLPHA_skooltechplus;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class AnnouncementFragment extends Fragment {
    public AnnouncementFragment() {
        // Required empty public constructor
    }


    DbHandler db;
    ListView lv;
    TextView noData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcement, container, false);
        db = new DbHandler(getContext());
        lv = view.findViewById(R.id.fragment_announcement_listview);
        noData = view.findViewById(R.id.fragment_announcement_no_data);
        getAnnouncementList();
        return view;
    }

    private void getAnnouncementList(){
        ArrayList<HashMap<String, String>> lists = db.getNotificationByType("all", "announcement", 100);
        if(lists.size() > 0){
            noData.setText("");
            ListAdapter adapter = new SimpleAdapter(getActivity(),
                    lists,
                    R.layout.notification_announcement_row,
                    new String[]{"title", "body", "icon"},
                    new int[]{R.id.title, R.id.body, R.id.icon});

            lv.setAdapter(adapter);
        }else{
            noData.setText("- No Available Data -");
        }
    }
}
