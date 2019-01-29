package com.example.joel.brgyambulance.Service;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.joel.brgyambulance.VictimCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override

    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if(remoteMessage.getData()!=null) {
            LatLng victim_location = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);

            Intent intent = new Intent(getBaseContext(), VictimCall.class);
            intent.putExtra("lat", victim_location.latitude);
            intent.putExtra("lng", victim_location.longitude);
            intent.putExtra("victim",remoteMessage.getNotification().getTitle());
            startActivity(intent);
       }
       else if (remoteMessage.getNotification().getTitle().equals("Declined")){
            Handler handler = new Handler(Looper.myLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessaging.this, ""+remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}
