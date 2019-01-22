package com.example.joel.brgyambulance;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joel.brgyambulance.Interaction.Common;
import com.example.joel.brgyambulance.Remote.IFCMService;
import com.example.joel.brgyambulance.Remote.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VictimCall extends AppCompatActivity {

    TextView textTime,textDistance,textAddress;

    MediaPlayer mediaPlayer;
    IGoogleAPI mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_victim_call);

        mService = Common.getIGoogleAPI();
        textTime = findViewById(R.id.textTime);
        textAddress = findViewById(R.id.textAddress);
        textDistance = findViewById(R.id.textDistance);
        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if(getIntent() !=null)
        {
            double lat = getIntent().getDoubleExtra("lat",-1.0);
            double lng = getIntent().getDoubleExtra("lng",-1.0);
            getDirection(lat,lng);

        }
    }


    private void getDirection(double lat,double lng) {
        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preferences=less_driving&"+
                    "origin="+ Common.mLastlocation.getLatitude()+","+Common.mLastlocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_directions_api);
            Log.d("POWER",requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                JSONObject object = routes.getJSONObject(0);
                                JSONArray legs = object.getJSONArray("legs");
                                JSONObject legsobject = legs.getJSONObject(0);

                                JSONObject distance = legsobject.getJSONObject("distance");
                                textDistance.setText(distance.getString("text"));

                                JSONObject time = legsobject.getJSONObject("duration");
                                textTime.setText(time.getString("text"));

                                String address = legsobject.getString("end_address");
                                textAddress.setText(address);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(VictimCall.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        catch (Exception e){

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }
}
