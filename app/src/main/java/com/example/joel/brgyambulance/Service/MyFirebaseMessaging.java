package com.example.joel.brgyambulance.Service;


import android.content.Intent;

import com.example.joel.brgyambulance.VictimCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override

    public void onMessageReceived(RemoteMessage remoteMessage) {
        LatLng victim_location = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);

        Intent intent = new Intent(getBaseContext(),VictimCall.class);
        intent.putExtra("lat",victim_location.latitude);
        intent.putExtra("lng",victim_location.longitude);
        intent.putExtra("victimId",remoteMessage.getNotification().getTitle());
        startActivity(intent);
    }

}
