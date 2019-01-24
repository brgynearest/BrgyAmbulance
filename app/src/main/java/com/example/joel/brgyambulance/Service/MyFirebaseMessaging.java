package com.example.joel.brgyambulance.Service;


import android.content.Intent;

import com.example.joel.brgyambulance.VictimCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override

    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage.getData()!=null) {
           /* LatLng victim_location = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);*/
            Map<String,String> data = remoteMessage.getData();
            String victim  = data.get("victim");
            String lat = data.get("lat");
            String lng = data.get("lng");

            Intent intent = new Intent(getBaseContext(), VictimCall.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            intent.putExtra("victim",victim);
            startActivity(intent);
       }
    }

}
