package com.example.joel.brgyambulance.Interaction;


import android.location.Location;

import com.example.joel.brgyambulance.Model.Barangay;
import com.example.joel.brgyambulance.Remote.FCMClient;
import com.example.joel.brgyambulance.Remote.IFCMService;
import com.example.joel.brgyambulance.Remote.IGoogleAPI;
import com.example.joel.brgyambulance.Remote.RetrofitClient;

import retrofit2.Retrofit;

public class Common  {

    public static final String available_Ambulance = "AvailableAmbulance";
    public static final String barangay_ambulance = "BarangayAmbulance";
    public static final String victim_information = "Victim";
    public static final String pickup_request = "helprequest";
    public static final String request_hospital = "RequestHospital";
    public static final String available_Hospitals = "AvailableHospital";
    public static final String hospitals = "Hospitals";
    public static final String token = "Tokens";

    public static Barangay currentAmbulance;
    public static Location mLastlocation=null;
    public static final String message="";
    public static final String user_field = "email";
    public static final String pwd_field = "password";

    public static final String baseURL ="https://maps.googleapis.com";
    public static final String fcmURL ="https://fcm.googleapis.com";

    public static IGoogleAPI getIGoogleAPI()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
