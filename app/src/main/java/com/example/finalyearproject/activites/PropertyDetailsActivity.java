package com.example.finalyearproject.activites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.example.finalyearproject.adapter.AdapterImageSlider;
import com.example.finalyearproject.databinding.ActivityPropertyDetailsBinding;
import com.example.finalyearproject.models.ModelImageSlider;
import com.example.finalyearproject.models.ModelProperty;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class PropertyDetailsActivity extends AppCompatActivity {

    private ActivityPropertyDetailsBinding binding;
    private static final String TAG = "PROPERTY_DETAILS_TAG";
    private FirebaseAuth firebaseAuth;
    private String propertyId = "";
    private double propertyLatitude = 0.0;
    private double propertyLongitude = 0.0;

    private String sellerUid = null;
    private String sellerPhone = ""; // Seller ka phone number
    private String propertyStatus = "";
    private boolean favorite = false;
    private ArrayList<ModelImageSlider> imageSliderArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPropertyDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Buttons visible kar rahe hain
        binding.toolBarEditBtn.setVisibility(View.VISIBLE);
        binding.toolBarDeleteBtn.setVisibility(View.VISIBLE);
        binding.chatBtn.setVisibility(View.VISIBLE);
        binding.callBtn.setVisibility(View.VISIBLE);
        binding.smsBtn.setVisibility(View.VISIBLE);

        propertyId = getIntent().getStringExtra("propertyId");
        firebaseAuth = FirebaseAuth.getInstance();

        loadPropertyDetails();
        loadPropertyImages();

        binding.toolbarBackBtn.setOnClickListener(v -> finish());

        // --- CALL BUTTON LOGIC ---
        binding.callBtn.setOnClickListener(v -> {
            if (sellerPhone != null && !sellerPhone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + sellerPhone));
                startActivity(intent);
            } else {
                MyUtils.toast(this, "Phone number not available");
            }
        });

        // --- WHATSAPP / SMS BUTTON LOGIC ---
        binding.smsBtn.setOnClickListener(v -> {
            if (sellerPhone != null && !sellerPhone.isEmpty()) {
                try {
                    // WhatsApp Link Logic
                    String whatsappUrl = "https://api.whatsapp.com/send?phone=" + sellerPhone;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(whatsappUrl));
                    startActivity(intent);
                } catch (Exception e) {
                    // Agar WhatsApp nahi hai to normal SMS khol dein
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + sellerPhone));
                    startActivity(smsIntent);
                }
            } else {
                MyUtils.toast(this, "Phone number not available");
            }
        });

        binding.toolBarDeleteBtn.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Property")
                    .setMessage("Are you sure you want to delete this property? (Demo Mode)")
                    .setPositiveButton("Yes", (dialog, which) -> deleteProperty())
                    .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        binding.toolBarFavBtn.setOnClickListener(v -> {
            favorite = !favorite;
            binding.toolBarFavBtn.setImageResource(favorite ? R.drawable.fav_yes_black : R.drawable.fav_no_black);
            MyUtils.toast(this, favorite ? "Added to Favorites (Demo)" : "Removed from Favorites (Demo)");
        });

        binding.mapBtn.setOnClickListener(v -> MyUtils.mapIntent(this, propertyLatitude, propertyLongitude));
        binding.toolBarEditBtn.setOnClickListener(v -> editOptions());

        binding.sellerProfileCv.setOnClickListener(v -> {
            Intent intent = new Intent(PropertyDetailsActivity.this, SellerProfileActivity.class);
            intent.putExtra("sellerUid", sellerUid);
            startActivity(intent);
        });

        binding.chatBtn.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                MyUtils.toast(this, "Please Login First");
                startActivity(new Intent(this, LoginOptionsActivity.class));
            } else {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("receiptUid", sellerUid);
                startActivity(intent);
            }
        });
    }

    private void loadPropertyDetails() {
        ArrayList<ModelProperty> allData = MyUtils.getDummyData();
        ModelProperty selectedProperty = null;

        for (ModelProperty model : allData) {
            if (model.getId().equals(propertyId)) {
                selectedProperty = model;
                break;
            }
        }

        if (selectedProperty != null) {
            binding.toolbarTitleTv.setText(selectedProperty.getTitle());
            binding.descriptionTv.setText(selectedProperty.getDescription());
            binding.addressTv.setText(selectedProperty.getAddress());

            try {
                double p = Double.parseDouble(selectedProperty.getPrice());
                binding.priceTv.setText(MyUtils.formateCurrency(p));
            } catch (Exception e) {
                binding.priceTv.setText(selectedProperty.getPrice());
            }

            binding.categoryTv.setText(selectedProperty.getCategory());
            binding.purposeTv.setText(selectedProperty.getPurpose());
            binding.subcategoryTv.setText(selectedProperty.getSubcategory());

            binding.floorsTv.setText("Floors: " + selectedProperty.getFloors());
            binding.bedsTv.setText("Bed Rooms: " + selectedProperty.getBedrooms());
            binding.bathroomsTv.setText("Bath Rooms: " + selectedProperty.getBathrooms());
            binding.areaSizeTv.setText("Area: " + selectedProperty.getAreaSize() + " " + selectedProperty.getAreaSizeUnit());

            sellerUid = selectedProperty.getUid();
            propertyStatus = selectedProperty.getStatus();
            propertyLatitude = selectedProperty.getLatitude();
            propertyLongitude = selectedProperty.getLongitude();

            loadSellerDetails();
        }
    }

    private void loadSellerDetails() {
        binding.sellerNameTv.setText("Tanveer Hussain");
        binding.memberSinceTv.setText("Member since: 01/05/2026");

        // Dummy phone number (Viva ke liye isay use karein)
        sellerPhone = "+923487388790";

        Glide.with(this)
                .load(R.drawable.tanveer_image)
                .placeholder(R.drawable.person_gray)
                .into(binding.sellerProfileIv);
    }

    private void editOptions() {
        PopupMenu popupMen = new PopupMenu(this, binding.toolBarEditBtn);
        popupMen.getMenu().add(Menu.NONE, 0, 0, "Edit");
        popupMen.getMenu().add(Menu.NONE, 1, 1, "Mark as Sold");
        popupMen.show();

        popupMen.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == 0) {
                Intent intent = new Intent(this, PostAddActivity.class);
                intent.putExtra("isEditMode", true);
                intent.putExtra("propertyIdForEditing", propertyId);
                startActivity(intent);
            } else if (itemId == 1) {
                showMarksAsSoldDialog();
            }
            return true;
        });
    }

    private void showMarksAsSoldDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Mark as sold")
                .setMessage("Are you sure you want to mark this property as sold?")
                .setPositiveButton("YES", (dialog, which) -> {
                    binding.soldCv.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadPropertyImages() {
        imageSliderArrayList = new ArrayList<>();
        imageSliderArrayList.add(new ModelImageSlider("1", "", R.drawable.modrenapartment));
        imageSliderArrayList.add(new ModelImageSlider("2", "", R.drawable.luxury_vila));
        imageSliderArrayList.add(new ModelImageSlider("3", "", R.drawable.residential_house));

        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(this, imageSliderArrayList);
        binding.imageSliderVp.setAdapter(adapterImageSlider);
    }

    private void deleteProperty() {
        MyUtils.toast(this, "Property Deleted Successfully (Demo Mode)");
        finish();
    }
}