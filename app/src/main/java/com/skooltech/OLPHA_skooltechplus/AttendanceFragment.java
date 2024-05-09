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
public class AttendanceFragment extends Fragment {
    public AttendanceFragment() {
        // Required empty public constructor
    }

    DbHandler db;
    ListView lv;
    TextView noData;
    Spinner spinner;

    String TAG = "Attendance Fragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);

        db = new DbHandler(getContext());
        lv = view.findViewById(R.id.fragment_attendance_listview);
        noData = view.findViewById(R.id.fragment_attendance_no_data);
        spinner = view.findViewById(R.id.fragment_attendance_spinner);

        ArrayList<HashMap<String, String>> childList = db.getChildList();
        ArrayList<String> childIds = new ArrayList<>();
        ArrayList<String> childNames = new ArrayList<>();

        if(childList.size() > 0){
            for(HashMap<String, String> map : childList){
                String id = map.get("id");
                String name = map.get("name");
                if(id != null && name != null){
                    childIds.add(id);
                    childNames.add(name);
                }
            }
        }else{
            childNames.add("- No Data -");
            childIds.add("0");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                childNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getAttendanceList(childIds.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        noData.setText("- No Available Data -");
        return view;
    }

    private void getAttendanceList(String id){
        ArrayList<HashMap<String, String>> lists = db.getAttendanceById(id, 100);
        if(lists.size() > 0){
            noData.setText("");
            ListAdapter adapter = new SimpleAdapter(getActivity(),
                    lists,
                    R.layout.attendance_row,
                    new String[]{"date", "day", "login", "logout"},
                    new int[]{R.id.date, R.id.day, R.id.timein, R.id.timeout});
            lv.setAdapter(adapter);
        }else{
            noData.setText("- No Available Data -");
        }
    }
}
