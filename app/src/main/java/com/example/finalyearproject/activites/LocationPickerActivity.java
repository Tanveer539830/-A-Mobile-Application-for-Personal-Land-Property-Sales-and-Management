package com.example.finalyearproject.activites;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject.R;
import com.example.finalyearproject.databinding.ActivityLocationPickerBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityLocationPickerBinding binding;
    private static final String TAG="LOCATION_PICKER_TAG";
    private static final int DEFAULT_ZOOM=15;

    private GoogleMap mMap=null;
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Double selectedLatitude=null;
    private Double selectedLongitude=null;
    private String selectedAddress="";
    private String selectedCity="";
    private String selectedState="";
    private String selectedCountry="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLocationPickerBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        binding.doneLl.setVisibility(View.GONE);
        SupportMapFragment mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        Places.initialize(this,getString(R.string.my_maps_api_key));
        mPlacesClient= Places.createClient(this);
        mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

        AutocompleteSupportFragment autocompleteSupportFragment=(AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        Place.Field[] placesList=new Place.Field[]{Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS,Place.Field.ADDRESS_COMPONENTS};
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(placesList));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Log.d(TAG, "onError:status: "+status);


            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.d(TAG, "onPlaceSelected:place: "+place);

                String id=place.getId();
                LatLng latLng=place.getLatLng();
                addressFromLating(latLng);


            }
        });
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.toolbarGpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGpsEnabled()){
                    requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                }else {
                    MyUtils.toast(LocationPickerActivity.this,"GPS is disabled");
                }
            }
        });
        binding.doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("latitude",selectedLatitude);
                intent.putExtra("longitude",selectedLongitude);
                intent.putExtra("address",selectedAddress);
                intent.putExtra("city",selectedCity);
              intent.putExtra("country",selectedCountry);
              intent.putExtra("state",selectedState);
              setResult(RESULT_OK,intent);
              finish();
            }
        });


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
mMap=googleMap;
requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        addressFromLating(latLng);
    }
});
    }
    @SuppressLint("MissingPermission")
    private ActivityResultLauncher<String> requestLocationPermission=registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult:isGranted: "+isGranted);
                    if(isGranted){
                        mMap.setMyLocationEnabled(true);
                        pickCurrentPlace();
                    }else{
                        MyUtils.toast(LocationPickerActivity.this,"Location permission is required");
                    }
                }
            }
    );
    private void pickCurrentPlace(){
        Log.d(TAG, "pickCurrentPlace: ");
        if(mMap==null){
            return;
        }
        detectAndShowDeviceLocationMap();
    }
    @SuppressLint("MissingPermission")
    private void detectAndShowDeviceLocationMap(){
        try {
            Task<Location> locationResult=mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                   if(location!=null){
                       LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                       addressFromLating(latLng);
                   }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: ",e);
                }
            });
        }catch (Exception e){
            Log.e(TAG, "detectAndShowDeviceLocationMap: ",e);
        }
    }
    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled=false;
        boolean networkEnabled=false;
        try {
            gpsEnabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception e){
            Log.e(TAG, "isGpsEnabled: ",e);
        }
        try {
            networkEnabled=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch (Exception e){
            Log.e(TAG, "isGpsEnabled: ",e);
        }
       return !(!gpsEnabled&&!networkEnabled);
    }

    private void addressFromLating(LatLng latLng){

        Geocoder geocoder=new Geocoder(this);
        try {
            List<Address> addresses=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            Address address=addresses.get(0);
            String addressLine=address.getAddressLine(0);
           String subLocality=address.getSubLocality();
           selectedLatitude=latLng.latitude;
           selectedLongitude=latLng.longitude;
           selectedCountry=address.getCountryName();
           selectedState=address.getAdminArea();
           selectedCity=address.getLocality();
           selectedAddress=addressLine;
           Log.d(TAG,"addressFromLating: selectedLatitude: "+selectedLatitude);
           Log.d(TAG,"addressFromLating: selectedLongitude: "+selectedLongitude);
           Log.d(TAG,"addressFromLating: selectedCountry: "+selectedCountry);
           Log.d(TAG,"addressFromLating: selectedState: "+selectedState);
           Log.d(TAG,"addressFromLating: selectedCity: "+selectedCity);
           Log.d(TAG,"addressFromLating: selectedAddress: "+selectedAddress);

           addMarker(latLng,subLocality,addressLine);


        }catch (Exception e){
            Log.e(TAG, "addressFromLating: ",e);
        }
    }
    private void addMarker(LatLng latLng, String title, String address){
     mMap.clear();
     try {
         MarkerOptions markerOptions=new MarkerOptions();
         markerOptions.position(latLng);
         markerOptions.title(title);
         markerOptions.snippet(address);
         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
         mMap.addMarker(markerOptions);
         binding.doneLl.setVisibility(View.VISIBLE);
         binding.selectedPlaceTv.setText(address);

     }catch (Exception e){
         Log.e(TAG, "addMarker: ",e);
     }
    }
}