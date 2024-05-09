package com.skooltech.OLPHA_skooltechplus;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationFragment extends Fragment {
    public NotificationFragment() {
        // Required empty public constructor
    }

    Spinner spinner;
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
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        db = new DbHandler(getContext());

        spinner = view.findViewById(R.id.fragment_notification_spinner);
        lv = view.findViewById(R.id.fragment_notification_listview);
        noData = view.findViewById(R.id.fragment_notification_no_data);

        ArrayList<HashMap<String, String>> childList = db.getChildList();
        ArrayList<String> childIds = new ArrayList<>();
        ArrayList<String> childNames = new ArrayList<>();

        childIds.add("all");
        childNames.add("All");

        for(HashMap<String, String> map : childList){
            String id = map.get("id");
            String name = map.get("name");
            if(id != null && name != null){
                childIds.add(id);
                childNames.add(name);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                childNames
        );

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getNotificationList(childIds.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    private void getNotificationList(String id){
        ArrayList<HashMap<String, String>> lists = db.getNotificationByType(id, "notification", 100);

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


