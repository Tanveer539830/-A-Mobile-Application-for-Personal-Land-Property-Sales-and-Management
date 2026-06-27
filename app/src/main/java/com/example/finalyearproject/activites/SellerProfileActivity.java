package com.example.finalyearproject.activites;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.example.finalyearproject.adapter.AdapterProperty;
import com.example.finalyearproject.databinding.ActivitySellerProfileBinding;
import com.example.finalyearproject.models.ModelProperty;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SellerProfileActivity extends AppCompatActivity {
    private ActivitySellerProfileBinding binding;
    private static final String TAG = "SELLER_INFO_TAG";

    private String sellerUid = "";
    private String sellerPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySellerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Intent se data lena
        sellerUid = getIntent().getStringExtra("sellerUid");

        // Data Load functions
        loadSellerDetail();
        loadSellerProperties();

        binding.toolbarBackBtn.setOnClickListener(v -> finish());
    }

    private void loadSellerDetail() {
        // Dummy Data for Profile
        binding.sellerNameTv.setText("Tanveer Hussain");
        binding.memberSinceTv.setText("Member since: 01/05/2024");
        sellerPhone = "+923487388790";

        // Profile Image Load (Error handling ke sath)
        Glide.with(this)
                .load(R.drawable.tanveer_image)
                .placeholder(R.drawable.person_gray) // Agar image na milay to ye dikhaye
                .error(R.drawable.person_gray)       // Agar crash ka khatra ho to ye dikhaye
                .into(binding.sellerProfileIv);
    }

    private void loadSellerProperties() {
        ArrayList<ModelProperty> propertyArrayList = new ArrayList<>();

        // --- DUMMY DATA START ---

        // Property 1: Apartment
        ModelProperty p1 = new ModelProperty();
        p1.setId("101");
        p1.setUid(sellerUid);
        p1.setTitle("Modern 3BHK Luxury Apartment");
        p1.setDescription("Fully furnished apartment with 24/7 security and gym.");
        p1.setCategory("Residential");
        p1.setPrice("Rs. 85,000");
        p1.setAddress("DHA Phase 6, Lahore");
        p1.setTimestamp(System.currentTimeMillis());
        // Seedhi tarah Resource ID pass karein
        p1.setImageResId(R.drawable.residential_house);
        propertyArrayList.add(p1);

        // Property 2: Villa
        ModelProperty p2 = new ModelProperty();
        p2.setId("102");
        p2.setUid(sellerUid);
        p2.setTitle("1 Kanal Designer Villa");
        p2.setDescription("Brand new villa with 5 master bedrooms and Italian kitchen.");
        p2.setCategory("House");
        p2.setPrice("Rs. 45,000,000");
        p2.setAddress("Bahria Town, Islamabad");
        p2.setTimestamp(System.currentTimeMillis() - 86400000);
        p2.setImageResId(R.drawable.residential_house);
        propertyArrayList.add(p2);

        // Property 3: Shop
        ModelProperty p3 = new ModelProperty();
        p3.setId("103");
        p3.setUid(sellerUid);
        p3.setTitle("Commercial Shop - Corner");
        p3.setDescription("Prime location shop, perfect for investment.");
        p3.setCategory("Commercial");
        p3.setPrice("Rs. 12,500,000");
        p3.setAddress("Gulberg III, Karachi");
        p3.setTimestamp(System.currentTimeMillis() - 172800000);
        p3.setImageResId(R.drawable.residential_house);
        propertyArrayList.add(p3);

        // Adapter Setup
        AdapterProperty adapterProperty = new AdapterProperty(this, propertyArrayList);
        binding.propertiesRv.setAdapter(adapterProperty);

        // Count update
        binding.publishAdsCountTv.setText("Published Ads: " + propertyArrayList.size());

        // --- DUMMY DATA END ---

        /*
        // FUTURE FIREBASE INTEGRATION:
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.orderByChild("uid").equalTo(sellerUid).addValueEventListener(...);
        */
    }
}