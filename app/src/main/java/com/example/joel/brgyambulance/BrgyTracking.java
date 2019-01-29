package com.example.joel.brgyambulance;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.example.joel.brgyambulance.Helper.DirectionJSONParser;
import com.example.joel.brgyambulance.Interaction.Common;
import com.example.joel.brgyambulance.Model.FCMResponse;
import com.example.joel.brgyambulance.Model.Notification;
import com.example.joel.brgyambulance.Model.Sender;
import com.example.joel.brgyambulance.Model.Token;
import com.example.joel.brgyambulance.Remote.IFCMService;
import com.example.joel.brgyambulance.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrgyTracking extends FragmentActivity implements OnMapReadyCallback
    ,GoogleApiClient.OnConnectionFailedListener
    ,GoogleApiClient.ConnectionCallbacks
    , LocationListener {

    private GoogleMap mMap;

    private static final int PLAY_SERVICES_RES_REQUEST = 7001;

    private LocationRequest mlocationRequest;
    private GoogleApiClient mgoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    String victimId;
    double victimlat,victimlng;
    private Circle victimMarker;
    private Polyline direction;
    private Marker ambulanceMarker;
    IGoogleAPI mService;
    IFCMService mFCMService;

    GeoFire geoFire;
    Button btnback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brgy_tracking);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnback=findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(BrgyTracking.this,AmbulanceHome.class));
            }
        });

        if(getIntent()!=null)
        {
            victimlat = getIntent().getDoubleExtra("lat",-1.0);
            victimlng = getIntent().getDoubleExtra("lng",-1.0);
            victimId = getIntent().getStringExtra("victimId");
        }

        mService = Common.getIGoogleAPI();
        mFCMService = Common.getFCMService();
        setUpLocation();

    }

    private void setUpLocation() {

            if (checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                displaylocation();
            }
    }
    private void createLocationRequest() {
        mlocationRequest = LocationRequest.create();
        mlocationRequest.setInterval(UPDATE_INTERVAL);
        mlocationRequest.setFastestInterval(FATEST_INTERVAL);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }
    private void buildGoogleApiClient() {
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleApiClient.connect();
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RES_REQUEST).show();
            else{
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        victimMarker = mMap.addCircle(new CircleOptions()
        .center(new LatLng(victimlat,victimlng))
        .radius(50)
        .strokeColor(android.R.color.holo_red_light)
        .fillColor(0x220000FF)
        .strokeWidth(5.0f));

        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.available_Ambulance));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(victimlat,victimlng),0.05f);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                sendArrivedNotification(victimId);
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendArrivedNotification(String victimId) {
        Token token  = new Token(victimId);
        Notification notification = new Notification("Arrived",String.format("The Ambulance has arrived to rescue"));
        Sender sender = new Sender(token.getToken(),notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success!=1)
                {
                    Toast.makeText(BrgyTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });


      /*  showAccidentsInstances();*/
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displaylocation();
        startLocationUpdates();
    }
    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient,mlocationRequest,this);

    }

    private void displaylocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Common.mLastlocation = LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);
            if (Common.mLastlocation != null) {

                final double latitude = Common.mLastlocation.getLatitude();
                final double longitude = Common.mLastlocation.getLongitude();

                if (ambulanceMarker != null)
                    ambulanceMarker.remove();
                ambulanceMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title("Your Ambulance")
                        .icon(BitmapDescriptorFactory.defaultMarker()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 18.0f));

                if (direction != null)
                    direction.remove();
               /* getDirection();*/

            } else {
                Toast.makeText(this, "Cannot get Location!", Toast.LENGTH_SHORT).show();
                Log.d("ERROR", "Cannot get your Lcoation");
            }
        }catch (Exception e){
            Toast.makeText(this, "Exception:" +e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("LatlLng","Lat: " +victimlat );

        }
    }

    private void  getDirection() {
        LatLng currentPosition = new LatLng(Common.mLastlocation.getLatitude(),Common.mLastlocation.getLongitude());
       /* String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preferences=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+victimlat+","+victimlng+"&"+
                    "key="+getResources().getString(R.string.google_directions_api);
            Log.d("BRGYTRACKING",requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                new ParserTask().execute(response.body().toString());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(BrgyTracking.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                            t.printStackTrace();
                        }
                    });
        }
        catch (Exception e){
            e.printStackTrace();

        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        mgoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastlocation = location;
        displaylocation();
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
    {

        ProgressDialog mDialog = new ProgressDialog(BrgyTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please wait . . .");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jObject);
            }
            catch (JSONException e ){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for(int i=0;i<lists.size();i++)
            {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                List<HashMap<String,String>> path = lists.get(i);

                for(int j=0;j<path.size();j++)
                {
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);

            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }


}
