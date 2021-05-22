package com.skooltech.skooltechsolutionsacademy;

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

public class AttendanceFragment extends Fragment {
    GlobalFunctions globalFunctions = new GlobalFunctions(getActivity());
    View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_attendance, null);

        DbHandler db0 = new DbHandler(getActivity());
        ArrayList<HashMap<String,String>> childList = db0.getChildList();

        final ArrayList<String> childId = new ArrayList<>();
        ArrayList<String> childName = new ArrayList<>();

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
                renderAttendanceList(childId.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    public void renderAttendanceList(String id){
        DbHandler db = new DbHandler(getActivity());
        ArrayList<HashMap<String,String>> attendanceList = db.getAttendanceById(id,50);

        //Log.d("test1234",attendanceList.toString());

        ListView lv = view.findViewById(R.id.attendance_list);
        ListAdapter adapter = new SimpleAdapter(getActivity(), attendanceList, R.layout.attendance_row,new String[]{"date","day","login","logout"},new int[]{R.id.textViewDate,R.id.textViewDay,R.id.textViewTimeIn,R.id.textViewTimeOut});
        lv.setAdapter(adapter);
    }
}
