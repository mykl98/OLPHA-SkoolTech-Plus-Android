package com.skooltech.OLPHA_skooltechplus;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    public static  int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "SA_skooltech+plus";
    public static final String CHANNEL_NAME = "SkoolTech Pro Solutions Notification";
    public static final String CHANNEL_DESCRIPTION = "www.skooltech.com";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        if(remoteMessage.getData().size() > 0){
            Map<String,String> data = remoteMessage.getData();

            String title = data.get("title");
            String body = data.get("body");
            String type = data.get("type");

            //Save notification to DB
            DbHandler dbHandler = new DbHandler(MyFirebaseMessagingService.this);
            if(type.equals("notification")){
                String studentId = data.get("id");
                String studentName = data.get("name");
                String activity = data.get("activity");
                String date = data.get("date");
                String time = data.get("time");

                dbHandler.insertNotification(title,body,studentName,studentId,activity,time,date,type);
            }else{
                dbHandler.insertNotification(title,body,"","","","","",type);
            }

            body = MainActivity.instance.decodeText(body);
            generateNotification(title, body);
        }
    }

    @Override
    public void onNewToken(String token){
        super.onNewToken(token);
    }

    private void generateNotification(String title, String messageBody) {
        String CHANNEL_ID = "SkoolTech";
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.splash_logo))
                .setSmallIcon(R.drawable.splash_logo)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify(NOTIFICATION_ID++, builder.build());

        if (NOTIFICATION_ID > 1073741824) {
            NOTIFICATION_ID = 0;
        }
    }
}
