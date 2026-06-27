package com.example.finalyearproject.activites;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.finalyearproject.R;
import com.example.finalyearproject.models.ModelProperty;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyUtils {
    public static final String AD_STATUS_AVAILABLE = "AVAILABLE";
    public static final String AD_STATUS_SOLD = "SOLD";
    public static final String AD_STATUS_RENTED = "RENTED";

    public static final String USER_TYPE_GOOGLE = "Google";
    public static final String USER_TYPE_EMAIL = "Email";
    public static final String USER_TYPE_PHONE = "Phone";
    public static final String[] propertyTypes = {"Homes", "Plots", "Commercial"};
    public static final String[] propertyTypesHomes = {"House", "Flat", "Upper Portion", "lower Portion", "Farm House", "Room", "Penthouse"};
    public static final String[] propertyTypesPlots = {"Residential Plot", "Commercial Plot", "Agricultural plot", "Industrial plot", "Plot File", "PlotForm"};
    public static final String[] propertyTypesCommercial = {"Office", "Shop", "Warehouse", "Factory", "Building", "Others"};
    public static final String[] propertyAreaSizeUnit = {"Square Feet", "Square Yards", "Square Meters", "Marla", "Kanal"};

    public static final String PROPERTY_PURPOSE_ANY = "Any";
    public static final String PROPERTY_PURPOSE_SELL = "Sell";
    public static final String PROPERTY_PURPOSE_RENT = "Rent";

    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_IMAGE = "image";

    public static final int MAX_DISTANCE_TO_LOAD_PROPERTIES = 10;


    //A function to show toast
    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static long timestamp() {
        return System.currentTimeMillis();
    }

    public static String formatTimestamp(long timestamp) {
        Calendar calender = Calendar.getInstance(Locale.ENGLISH);
        calender.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy", calender).toString();
        return date;

    }

    public static String formatTimestampDateTime(long timestamp) {
        Calendar calender = Calendar.getInstance(Locale.ENGLISH);
        calender.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy hh:mm aa", calender).toString();
        return date;

    }

    public static String formateCurrency(Double price) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(price);
    }
    public static String chatPath(String receiptUid,String yourUid){
        String [] arrayUids=new String[]{receiptUid,yourUid};

        Arrays.sort(arrayUids);
        String chatPath=arrayUids[0]+"_"+arrayUids[1];
        return chatPath;
    }

    public static double calculateDistanceKm(double currentLatitude, double currentLongitude, double propertyLatitude, double propertyLongitude) {
        Location startPoint = new Location(LocationManager.NETWORK_PROVIDER);
        startPoint.setLatitude(currentLatitude);
        startPoint.setLongitude(currentLongitude);
        Location endPoint = new Location(LocationManager.NETWORK_PROVIDER);
        endPoint.setLatitude(propertyLatitude);
        endPoint.setLongitude(propertyLongitude);
       double distanceInMeters= startPoint.distanceTo(endPoint);
       double distanceInKm=distanceInMeters/1000;

       return distanceInKm;
    }
    public static void addToFavorites(Context context,String propertyId){
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            MyUtils.toast(context,"You are not logged in");
        }
        else{
            long timestamp=MyUtils.timestamp();
            HashMap<String ,Object> hashMap=new HashMap<>();
            hashMap.put("propertyId",propertyId);
            hashMap.put("timestamp",timestamp);
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(propertyId).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            MyUtils.toast(context,"Added to favorites");

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MyUtils.toast(context,"Failed to add to favorites"+e.getMessage());
                        }
                    });


        }
    }
    /// Is ko main real time data base main use kro ga abhi zrort nhi
    public static void removeFromFavorites(Context context,String propertyId){
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            MyUtils.toast(context,"You are not logged in");
        }else {
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(propertyId).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            MyUtils.toast(context,"Removed from favorites");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MyUtils.toast(context,"Fail to Removed from favorites"+e.getMessage());
                        }
                    });

        }
    }
    public static ArrayList<ModelProperty> dummyList = new ArrayList<>();

    public static ArrayList<ModelProperty> getDummyData() {
        if (dummyList.isEmpty()) {
                    // 1. Modern Apartment
                    dummyList.add(new ModelProperty("1", "uid1", "Sell", "Homes", "Flat", "5", "3", "2", "1200", "Square Feet", "8500000", "Modern Luxury Apartment", "Beautiful view with modern facilities.", "admin@test.com", "+92", "3001112223", "Pakistan", "Bahawalpur", "Punjab", "Model Town", 29.3, 71.6, "AVAILABLE", 1714123200000L, R.drawable.modrenapartment, false));

                    // 2. Luxury Villa
                    dummyList.add(new ModelProperty("2", "uid2", "Sell", "Homes", "House", "2", "5", "4", "1", "Kanal", "45000000", "Executive Luxury Villa", "State of the art architecture with garden.", "owner@test.com", "+92", "3014445556", "Pakistan", "Lahore", "Punjab", "DHA Phase 6", 31.5, 74.3, "AVAILABLE", 1714123200000L, R.drawable.luxury_vila, false));

                    // 3. Residential House
                    dummyList.add(new ModelProperty("3", "uid3", "Rent", "Homes", "House", "1", "2", "2", "5", "Marla", "35000", "Cozy Family Home", "Near market and main road.", "agent@test.com", "+92", "3027778889", "Pakistan", "Karachi", "Sindh", "Gulshan-e-Iqbal", 24.8, 67.0, "AVAILABLE", 1714123200000L, R.drawable.residential_house, false));

                    // 4. Commercial Plaza
                    dummyList.add(new ModelProperty("4", "uid4", "Rent", "Commercial", "Building", "4", "0", "10", "2", "Kanal", "500000", "Prime Location Plaza", "Ideal for banks and multinational offices.", "info@plaza.com", "+92", "3031234567", "Pakistan", "Islamabad", "ICT", "Blue Area", 33.6, 73.0, "AVAILABLE", 1714123200000L, R.drawable.commercialplaza, false));

                    // 5. Farm House
                    dummyList.add(new ModelProperty("5", "uid5", "Sell", "Homes", "Farm House", "1", "4", "4", "4", "Kanal", "65000000", "Peaceful Farm House", "Lush green lawns and swimming pool.", "farm@test.com", "+92", "3049876543", "Pakistan", "Multan", "Punjab", "Boson Road", 30.1, 71.4, "AVAILABLE", 1714123200000L, R.drawable.farm_house, false));

                    // 6. Shop
                    dummyList.add(new ModelProperty("6", "uid6", "Sell", "Commercial", "Shop", "0", "0", "0", "250", "Square Feet", "2500000", "Main Bazaar Shop", "Busy area with high footfall.", "shop@test.com", "+92", "3051113335", "Pakistan", "Faisalabad", "Punjab", "Clock Tower", 31.4, 73.0, "AVAILABLE", 1714123200000L, R.drawable.commercialplaza, false));

                    // 7. Penthouse
                    dummyList.add(new ModelProperty("7", "uid7", "Rent", "Homes", "Penthouse", "1", "3", "3", "2500", "Square Feet", "150000", "Skyline Penthouse", "Full city view with private elevator.", "sky@test.com", "+92", "3062224446", "Pakistan", "Rawalpindi", "Punjab", "Bahria Town", 33.5, 73.0, "AVAILABLE", 1714123200000L, R.drawable.modrenapartment, false));

                    // 8. Agricultural Plot
                    dummyList.add(new ModelProperty("8", "uid8", "Sell", "Plots", "Agricultural plot", "0", "0", "0", "12", "Kanal", "12000000", "Fertile Agri Land", "Perfect for farming or investment.", "land@test.com", "+92", "3073335557", "Pakistan", "Sialkot", "Punjab", "Daska Road", 32.5, 74.3, "AVAILABLE", 1714123200000L, R.drawable.residential_house, false));

                    // 9. Office Space
                    dummyList.add(new ModelProperty("9", "uid9", "Rent", "Commercial", "Office", "1", "1", "1", "500", "Square Feet", "45000", "Corporate Office", "Fully furnished with high speed internet.", "corp@test.com", "+92", "3084446668", "Pakistan", "Peshawar", "KPK", "Hayatabad", 33.9, 71.4, "AVAILABLE", 1714123200000L, R.drawable.luxury_vila, false));

                    // 10. Small Cottage
                    dummyList.add(new ModelProperty("10", "uid10", "Sell", "Homes", "House", "1", "2", "1", "3", "Marla", "5500000", "Small Budget House", "Small family home in gated community.", "home@test.com", "+92", "3095557779", "Pakistan", "Quetta", "Balochistan", "Samungli Road", 30.1, 66.9, "AVAILABLE", 1714123200000L, R.drawable.residential_house, false));
        }
        return dummyList;
    }
    public static void callIntent(Context context,String phoneNumber){
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+Uri.encode(phoneNumber)));
        context.startActivity(intent);
    }
    public static void smsIntent(Context context,String phoneNumber){
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+Uri.encode(phoneNumber)));
        context.startActivity(intent);
    }

    public static void mapIntent(Context context,double latitude,double longitude){
        Uri mapIntentUri=Uri.parse("geo:"+latitude+","+longitude);
        Intent mapIntent=new Intent(Intent.ACTION_VIEW,mapIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if(mapIntent.resolveActivity(context.getPackageManager())!=null){
            context.startActivity(mapIntent);
        }else {
            MyUtils.toast(context,"Google map not installed!");

        }
    }
}
