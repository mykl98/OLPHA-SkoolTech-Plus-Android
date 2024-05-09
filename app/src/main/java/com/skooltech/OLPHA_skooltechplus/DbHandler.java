package com.skooltech.OLPHA_skooltechplus;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class DbHandler extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "skooltech";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_BODY = "body";
    private static final String KEY_NAME = "name";
    private static final String KEY_STUDENT_ID = "studentid";
    private static final String KEY_ACTIVITY = "activity";
    private static final String KEY_TIME = "time";
    private static final String KEY_DATE = "date";
    private static final String KEY_TYPE = "type";

    int[] listviewIcon = new int[]{R.drawable.ic_action_notification, R.drawable.ic_action_announcement};

    public DbHandler(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + DB_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT,"
                + KEY_BODY + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_STUDENT_ID + " TEXT,"
                + KEY_ACTIVITY + " TEXT,"
                + KEY_TIME + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_TYPE + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DB_NAME);
        onCreate(db);
    }

    void clearDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DB_NAME);
        onCreate(db);
    }

    void insertNotification(String title, String body, String name, String studentid, String activity, String time, String date, String type){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put(KEY_TITLE, title);
        val.put(KEY_BODY, body);
        val.put(KEY_NAME, name);
        val.put(KEY_STUDENT_ID, studentid);
        val.put(KEY_ACTIVITY, activity);
        val.put(KEY_TIME, time);
        val.put(KEY_DATE, date);
        val.put(KEY_TYPE, type);

        long newRowId = db.insert(DB_NAME,null,val);
        db.close();
    }

    @SuppressLint("Range")
    public ArrayList<HashMap<String,String>> getDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String,String>> dataList = new ArrayList<>();
        String query = "SELECT title,body,name,studentid,activity,time,date,type FROM " + DB_NAME;
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            HashMap<String,String> data = new HashMap<>();
            data.put("title",cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
            data.put("body",cursor.getString(cursor.getColumnIndex(KEY_BODY)));
            data.put("name",cursor.getString(cursor.getColumnIndex(KEY_NAME)));
            data.put("id",cursor.getString((cursor.getColumnIndex(KEY_STUDENT_ID))));
            data.put("activity",cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY)));
            data.put("time",cursor.getString(cursor.getColumnIndex(KEY_TIME)));
            data.put("date",cursor.getString(cursor.getColumnIndex(KEY_DATE)));
            data.put("type",cursor.getString(cursor.getColumnIndex(KEY_TYPE)));
            dataList.add(data);
        }
        return dataList;
    }

    @SuppressLint("Range")
    public ArrayList<HashMap<String,String>> getAllNotification(String student, int limit){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String,String>> notificationList = new ArrayList<>();
        String query;
        Cursor cursor;
        if (student == "all") {
            query = "SELECT title,body,type FROM " + DB_NAME + " ORDER BY id DESC LIMIT " + limit;
            cursor = db.rawQuery(query, null);
        }else {
            query = "SELECT title,body,type FROM " + DB_NAME + " WHERE student = ? ORDER BY id DESC LIMIT " + limit;
            cursor = db.rawQuery(query, new String[]{student});
        }
        while (cursor.moveToNext()){
            HashMap<String,String> notification = new HashMap<>();
            String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
            String body = cursor.getString(cursor.getColumnIndex(KEY_BODY));
            String type = cursor.getString(cursor.getColumnIndex(KEY_TYPE));

            if(title != null){
                title = MainActivity.instance.decodeText(title);
            }

            if(body != null){
                body = MainActivity.instance.decodeText(body);
            }

            notification.put("title", title);
            notification.put("body", body);

            if(type.equals("notification")){
                notification.put("icon", Integer.toString(listviewIcon[0]));
            }else{
                notification.put("icon", Integer.toString(listviewIcon[1]));
            }

            notificationList.add(notification);
        }
        return notificationList;
    }

    @SuppressLint("Range")
    public ArrayList<HashMap<String,String>> getNotificationByType(String student, String type, int limit){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String,String>> notificationList = new ArrayList<>();
        String query;
        Cursor cursor;
        if (student.equals("all")) {
            query = "SELECT title, body, type FROM " + DB_NAME + " WHERE type = ? ORDER BY id DESC LIMIT " + limit;
            cursor = db.rawQuery(query, new String[]{type});
        }else{
            query = "SELECT title, body, type FROM " + DB_NAME + " WHERE type = ? AND studentid = ? ORDER BY id DESC LIMIT " + limit;
            cursor = db.rawQuery(query, new String[]{type, student});
        }
        while (cursor.moveToNext()){
            HashMap<String,String> notification = new HashMap<>();
            String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
            String body = cursor.getString(cursor.getColumnIndex(KEY_BODY));

            if(title != null){
                title = MainActivity.instance.decodeText(title);
            }

            if(body != null){
                body = MainActivity.instance.decodeText(body);
            }

            notification.put("title", title);
            notification.put("body", body);

            if(type.equals("notification")){
                notification.put("icon", Integer.toString(listviewIcon[0]));
            }else{
                notification.put("icon", Integer.toString(listviewIcon[1]));
            }

            notificationList.add(notification);
        }
        cursor.close();
        return notificationList;
    }

    @SuppressLint("Range")
    public ArrayList<HashMap<String,String>> getChildList(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String,String>> childList = new ArrayList<>();
        String query = "SELECT DISTINCT " + KEY_STUDENT_ID + "," + KEY_NAME + " FROM " + DB_NAME + " WHERE " + KEY_TYPE + " = ? GROUP BY " + KEY_STUDENT_ID;
        Cursor cursor = db.rawQuery(query,new String[]{"notification"});

        while (cursor.moveToNext()){
            HashMap<String,String> child = new HashMap<>();
            String id = cursor.getString(cursor.getColumnIndex(KEY_STUDENT_ID));
            child.put("id", cursor.getString(cursor.getColumnIndex(KEY_STUDENT_ID)));
            child.put("name", cursor.getString(cursor.getColumnIndex(KEY_NAME)));
            childList.add(child);
        }
        cursor.close();
        return childList;
    }

    @SuppressLint("Range")
    public ArrayList<HashMap<String,String>> getAttendanceById(String id, int limit){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String,String>> attendanceList = new ArrayList<>();
        String query;
        Cursor cursor;
        if (id.equals("all")){
            query = "SELECT DISTINCT " + KEY_TIME + "," + KEY_DATE + "," + KEY_ACTIVITY + " FROM " + DB_NAME + " WHERE " + KEY_TYPE + " = ? GROUP BY " + KEY_DATE + "," + KEY_ACTIVITY + " ORDER BY " + KEY_ID + " DESC";
            cursor = db.rawQuery(query,new String[]{"notification"});
        }else {
            query = "SELECT DISTINCT " + KEY_TIME + "," + KEY_DATE + "," + KEY_ACTIVITY + " FROM " + DB_NAME + " WHERE " + KEY_TYPE + " = ? AND " + KEY_STUDENT_ID + " = ? GROUP BY " + KEY_DATE + "," + KEY_ACTIVITY + " ORDER BY " + KEY_ID + " DESC";
            cursor = db.rawQuery(query,new String[]{"notification",id});
        }
        String prevDate = "";
        String prevDay = "";
        String prevTime = "";
        String prevActivity = "";
        String date = "1986-02-09";
        String day = "";
        String time = "";
        String activity = "";
        HashMap<String,String> attendance;
        while (cursor.moveToNext()){
            attendance = new HashMap<>();
            date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
            time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
            activity = cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY));

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date d = new Date();
            try {
                d = dateFormat.parse(date);
            }catch (ParseException ex){
                Log.d("test1234",ex.getLocalizedMessage());
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(d);

            int _day = cal.get(Calendar.DAY_OF_WEEK);

            switch (_day){
                case 1: day = "Sunday";
                    break;
                case 2: day = "Monday";
                    break;
                case 3: day = "Tuesday";
                    break;
                case 4: day = "Wednesday";
                    break;
                case 5: day = "Thursday";
                    break;
                case 6: day = "Friday";
                    break;
                case 7: day = "Saturday";
                    break;
            }

            if(!prevDate.equals("") && prevDate.equals(date)){
                attendance.put("date",prevDate);
                attendance.put("day",prevDay);
                if(prevActivity.equals("login")){
                    attendance.put("login",prevTime);
                }
                if(prevActivity.equals("logout")){
                    attendance.put("logout",prevTime);
                }
                if(activity.equals("login")){
                    attendance.put("login",time);
                }
                if(activity.equals("logout")){
                    attendance.put("logout",time);
                }
                prevDate = "";
                attendanceList.add(attendance);
            }else if (!prevDate.equals("") && !prevDate.equals(date)){
                attendance.put("date",prevDate);
                attendance.put("day",prevDay);
                if(prevActivity.equals("login")){
                    attendance.put("login",prevTime);
                    attendance.put("logout","");
                }
                if(prevActivity.equals("logout")){
                    attendance.put("login","");
                    attendance.put("logout",prevTime);
                }
                attendanceList.add(attendance);
                prevDate = date;
                prevDay = day;
                prevTime = time;
                prevActivity = activity;
            }else if(prevDate.equals("")){
                prevDate = date;
                prevDay = day;
                prevTime = time;
                prevActivity = activity;
            }
        }
        if(!prevDate.equals("")){
            attendance = new HashMap<>();
            attendance.put("date",prevDate);
            attendance.put("time",prevTime);
            if(prevActivity.equals("login")){
                attendance.put("login",prevTime);
                attendance.put("logout","");
            }else if(prevActivity.equals("logout")){
                attendance.put("login","");
                attendance.put("logout",prevTime);
            }
            attendanceList.add(attendance);
        }
        return attendanceList;
    }
}
