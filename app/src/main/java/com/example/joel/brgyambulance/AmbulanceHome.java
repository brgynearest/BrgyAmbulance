package com.example.joel.brgyambulance;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.joel.brgyambulance.Hospital.GetNearbyPlacesData;
import com.example.joel.brgyambulance.Interaction.Common;
import com.example.joel.brgyambulance.Model.Barangay;
import com.example.joel.brgyambulance.Model.FCMResponse;
import com.example.joel.brgyambulance.Model.Notification;
import com.example.joel.brgyambulance.Model.Sender;
import com.example.joel.brgyambulance.Model.Token;
import com.example.joel.brgyambulance.Remote.IFCMService;
import com.example.joel.brgyambulance.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.joel.brgyambulance.Interaction.Common.mLastlocation;



public class AmbulanceHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback
        ,GoogleApiClient.OnConnectionFailedListener
        ,GoogleApiClient.ConnectionCallbacks
        ,LocationListener {
    private BroadcastReceiver mCancelReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            btnFindHospitals.setText("FIND HOSPITAL");
            hospitalId="";
            isHospitalFound= false;

        }
    };

    private static final String TAG = "AmbulanceHome";

    Snackbar snackbar;
    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICES_RES_REQUEST = 7001;
    private LocationRequest mlocationRequest;
    private GoogleApiClient mgoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ambulanceavalable;
    GeoFire geoFire;

    Marker mCurrent;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;

    private List<LatLng> polyLineList;
    private Marker ambulanceMarker;
    private float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPosition,endPosition,currentPosition;
    private int index,next;
    private PlaceAutocompleteFragment places;
    AutocompleteFilter typeFilter;
    private String destination;
    private PolylineOptions polylineOptions,blackpolylineOptions;
    private Polyline blackPolyline,greyPolyline;
    private IGoogleAPI mService;
    private IFCMService mfcmService;

    boolean isHospitalFound=false;
    String hospitalId="";
    int radius=1;
    int distance=1;
    private static final int LIMIT =3;
    DatabaseReference hospitalAvailable;
    //for showing Hospitals
    double latitude,longitude;
    int PROXIMITY_RADIUS = 800;
    Button btnFindHospitals;
    DatabaseReference availableRef,currentAmbulanceRef;
    ImageView imgExpandable;
    BottomSheet mBottomSheet;
    ListView listView;
    ImageView imageView;
    CardView cardView;
    private static String text_viewString;


    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index < polyLineList.size() - 1) {
                index++;
                next = index + 1;
            }
            if (index < polyLineList.size() - 1) {
                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);
            }

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v * endPosition.longitude + (1-v) * startPosition.longitude;
                    lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                    LatLng newPos = new LatLng(lat, lng);
                    ambulanceMarker.setPosition(newPos);
                    ambulanceMarker.setAnchor(0.5f, 0.5f);
                    ambulanceMarker.setRotation(getBearing(startPosition, newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this, 3000);
        }
    };
    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        } else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        } else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        } else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        }
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        imageView = findViewById(R.id.imageview_service);
        cardView = findViewById(R.id.rrrlayout);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardView.setVisibility(View.VISIBLE);

            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mBottomSheet= BottomSheet.newInstance("Victim Case");
        btnFindHospitals = findViewById(R.id.btnfindhospitals);
        btnFindHospitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isHospitalFound)
                    requestHospital(FirebaseAuth.getInstance().getCurrentUser().getUid());
                else
                    sendRequestToHospital(hospitalId);

            }
        });


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        availableRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentAmbulanceRef = FirebaseDatabase.getInstance().getReference(Common.available_Ambulance)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        availableRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentAmbulanceRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        location_switch = findViewById(R.id.location_on_off_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean sAvailable) {
                try {
                    if (sAvailable) {
                        btnFindHospitals.setBackgroundResource(R.drawable.button_sign);
                        btnFindHospitals.setEnabled(true);
                        FirebaseDatabase.getInstance().goOnline();
                        startLocationUpdates();
                        displayLocation();
                        hospitalAvailable = FirebaseDatabase.getInstance().getReference(Common.available_Hospitals);
                        hospitalAvailable.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                loadAllAvailableHospitals();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        Snackbar.make(mapFragment.getView(), "You are available", Snackbar.LENGTH_LONG).show();

                    } else {
                        mMap.clear();
                        handler = new Handler();
                        /*btnFindHospitals.setBackgroundResource(R.color.grey);
                        btnFindHospitals.setEnabled(false);*/
                        FirebaseDatabase.getInstance().goOffline();
                        stopLocationUpdates();
                        mCurrent.remove();
                        handler.removeCallbacks(drawPathRunnable);
                        snackbar.make(mapFragment.getView(), "You are not available, Enable to Rescue and Find Hospital", 5000).show();
                    }
                }catch (Exception ex){
                    Log.d(TAG,"ToggleSwitch Error:" + ex.getMessage());

                }

            }
        });

        polyLineList = new ArrayList<>();

        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();
        places = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.places_autocomplete);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(location_switch.isChecked())
                {
                    destination = place.getAddress().toString();
                    destination = destination.replace("","+");
                    getDirection();
                }
                else
                {
                    Toast.makeText(AmbulanceHome.this, "Availability needed to turn on", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(AmbulanceHome.this, ""+status.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        ambulanceavalable = FirebaseDatabase.getInstance().getReference(Common.available_Ambulance);
        geoFire = new GeoFire(ambulanceavalable);
        setUpLocation();

        mfcmService = Common.getFCMService();

        mService = Common.getIGoogleAPI();
        updateFirebaseToken();

        listView = findViewById(R.id.samplelist);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    text_viewString = ((TextView)view).getText().toString();
                    cardView.setVisibility(View.GONE);

            }
        });
    }
    public static String getServices(){
        return text_viewString;

    }

    private void sendRequestToHospital(String hospitalId) {
        if (text_viewString != null){
            DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token);
            tokens.orderByKey().equalTo(hospitalId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot postsnapshot:dataSnapshot.getChildren())
                            {
                                Token token = postsnapshot.getValue(Token.class);
                                String jsonlatlang = new Gson().toJson(new LatLng(Common.mLastlocation.getLatitude(),Common.mLastlocation.getLongitude()));
                                String ambulanceToken = FirebaseInstanceId.getInstance().getToken();
                                Notification notify = new Notification(ambulanceToken,text_viewString);
                                Sender sender = new Sender(token.getToken(),notify);
                                mfcmService.sendMessage(sender)
                                        .enqueue(new Callback<FCMResponse>() {
                                            @Override
                                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                if(response.body().success ==1)
                                                    Toast.makeText(AmbulanceHome.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(AmbulanceHome.this, "Request not Sent!", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(Call<FCMResponse> call, Throwable t) {
                                                Log.e("POWER ERROR",t.getMessage());
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

        }
        else{
            Toast.makeText(this, "Victim's condition not selected", Toast.LENGTH_SHORT).show();
            cardView.setVisibility(View.GONE);
        }


    }

    private void requestHospital(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.request_hospital);
        GeoFire geoFire = new GeoFire(dbRequest);
        geoFire.setLocation(uid, new GeoLocation(Common.mLastlocation.getLatitude(), Common.mLastlocation.getLongitude())
                , new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

        if(mCurrent.isVisible())
            mCurrent.remove();

        mCurrent = mMap.addMarker(new MarkerOptions()
                .title("Requesting Hospital")
                .position(new LatLng(Common.mLastlocation.getLatitude(),Common.mLastlocation.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_me)));

        btnFindHospitals.setText("Finding Nearest Hospitals . . . ");

        findHospital();

    }

    private void findHospital() {
        loadAllAvailableHospitals();
        DatabaseReference hospitals = FirebaseDatabase.getInstance().getReference(Common.available_Hospitals);
        GeoFire gfHospitals = new GeoFire(hospitals);
        final GeoQuery geoQuery = gfHospitals.queryAtLocation(new GeoLocation(Common.mLastlocation.getLatitude(),Common.mLastlocation.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!isHospitalFound)
                {
                    isHospitalFound = true;
                    hospitalId=key;
                    btnFindHospitals.setText("Call Hospital");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!isHospitalFound && radius <LIMIT)
                {
                    radius++;
                    findHospital();
                }
                else {
                    if(!isHospitalFound){
                        Toast.makeText(AmbulanceHome.this, "No available ambulance near you", Toast.LENGTH_SHORT).show();
                        btnFindHospitals.setText("FIND HOSPITAL");
                        geoQuery.removeAllListeners();

                    }

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void showHospitals() {
        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

        mMap.clear();
        String hospital = "hospital";
        String url = getUrl(latitude, longitude, hospital);
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;

        getNearbyPlacesData.execute(dataTransfer);
        Toast.makeText(AmbulanceHome.this, "Showing Nearby Hospitals", Toast.LENGTH_SHORT).show();
        onLocationChanged(mLastlocation);


    }

    private String getUrl(double latitude , double longitude , String nearbyPlace)
    {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        /*googlePlaceUrl.append("&key="+"AIzaSyBGAFPZoNG_tY6ULWF6c-8wHFEpeTQ3EbE");*/
        /*googlePlaceUrl.append("&key="+"AIzaSyBAz36yCPfVJ6N8RCm8a5Ht5NqybPlC5e0");*/
        /*googlePlaceUrl.append("&key="+"AIzaSyC7bEU4_EK-tfsg3LZHRff6SZ55OPQIBCw");*/
        /*googlePlaceUrl.append("&key="+"AIzaSyBLEPBRfw7sMb73Mr88L91Jqh3tuE4mKsE");*/
        googlePlaceUrl.append("&key="+"AIzaSyBZwMHN95o72R5355U-vL7WyhbjlRT-NJ8");
        Log.d("NearbyHospitals", "url = "+googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token);
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void getDirection() {
        currentPosition = new LatLng(mLastlocation.getLatitude(), mLastlocation.getLongitude());
        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preferences=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+getResources().getString(R.string.google_directions_api);
            Log.d("POWER",requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for(int i=0;i<jsonArray.length();i++) {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyLine = poly.getString("points");
                                    polyLineList = decodePoly(polyLine);
                                }
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for(LatLng latLang:polyLineList)
                                    builder.include(latLang);
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GREEN);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                greyPolyline = mMap.addPolyline(polylineOptions);

                                blackpolylineOptions = new PolylineOptions();
                                blackpolylineOptions.color(Color.RED);
                                blackpolylineOptions.width(5);
                                blackpolylineOptions.startCap(new SquareCap());
                                blackpolylineOptions.endCap(new SquareCap());
                                blackpolylineOptions.jointType(JointType.ROUND);
                                blackPolyline = mMap.addPolyline(blackpolylineOptions);

                                mMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size()-1))
                                        .title("Example Victim"));

                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        List<LatLng> points = greyPolyline.getPoints();
                                        int percentValue = (int)valueAnimator.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue/100.0f));
                                        List<LatLng> p = points.subList(0,newPoints);
                                        blackPolyline.setPoints(p);
                                    }
                                });

                                polyLineAnimator.start();
                                ambulanceMarker = mMap.addMarker(new MarkerOptions()
                                        .position(currentPosition)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
                                handler = new Handler();
                                index=-1;
                                next=1;
                                handler.postDelayed(drawPathRunnable,3000);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(AmbulanceHome.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        catch (Exception e){

        }

    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                if (location_switch.isChecked())
                    displayLocation();
            }
        }
    }

    private void createLocationRequest() {
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL);
        mlocationRequest.setFastestInterval(FATEST_INTERVAL);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {
        mgoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addConnectionCallbacks(this)
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case  MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        if (location_switch.isChecked())
                            displayLocation();
                    }
                }
        }
    }

    private void stopLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleApiClient,this);

    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastlocation = LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);
        if (mLastlocation!=null)
        {


            if(location_switch.isChecked()){
                final double latitude = mLastlocation.getLatitude();
                final double longitude = mLastlocation.getLongitude();

                LatLng center  = new LatLng(latitude,longitude);
                LatLng northside = SphericalUtil.computeOffset(center,100000,0);
                LatLng southside = SphericalUtil.computeOffset(center,100000,180);

                LatLngBounds bounds = LatLngBounds.builder()
                        .include(northside)
                        .include(southside)
                        .build();

                places.setBoundsBias(bounds);
                places.setFilter(typeFilter);
                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude)
                        , new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if(mCurrent!=null)
                                    mCurrent.remove();

                                mCurrent = mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_me))
                                        .position(new LatLng(latitude,longitude))
                                        .title("Your Ambulance"));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),18.0f));

                            }
                        });

            }
        }
        else
        {
            Log.d("ERROR","Cannot get your Lcoation");
        }
    }

    private void loadAllAvailableHospitals() {
        mMap.clear();
        mCurrent.remove();
        mMap.addMarker(new MarkerOptions().position(new LatLng(Common.mLastlocation.getLatitude(),Common.mLastlocation.getLongitude()))
                .title("Your Ambulance")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_me)));

        DatabaseReference hospitalLocation = FirebaseDatabase.getInstance().getReference(Common.available_Hospitals);
        GeoFire gf = new GeoFire(hospitalLocation);
        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(Common.mLastlocation.getLatitude(),Common.mLastlocation.getLongitude()),distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference(Common.hospitals)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude,location.longitude))
                                        .flat(true)
                                        .title("")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital)));
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

            }
            @Override
            public void onKeyExited(String key) {
            }
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                if(distance<=LIMIT)
                {
                    distance++;
                    loadAllAvailableHospitals();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient,mlocationRequest,this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder alerdialog = new AlertDialog.Builder(this);
            alerdialog
                    .setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alerdialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ambulance_home, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_signout) {

            signOutAccount();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOutAccount() {
        backhome();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdates();

    }
    private void backhome() {
        Paper.init(this);
        Paper.book().destroy();

        FirebaseAuth.getInstance().signOut();

        startActivity(new Intent(AmbulanceHome.this,MainActivity.class));
        finish();
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



        mLastlocation = location;
        displayLocation();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        /*mMap.getUiSettings().setZoomControlsEnabled(false);*/
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
    }


}
