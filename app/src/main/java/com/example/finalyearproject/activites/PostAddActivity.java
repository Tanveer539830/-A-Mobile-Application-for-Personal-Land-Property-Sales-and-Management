package com.example.finalyearproject.activites;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.finalyearproject.adapter.AdapterImagePicked;
import com.example.finalyearproject.models.ModelImagePicked;
import com.example.finalyearproject.databinding.ActivityPostAddBinding;
import com.example.finalyearproject.models.ModelImageSlider;
import com.example.finalyearproject.models.ModelProperty;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostAddActivity extends AppCompatActivity {
    private ActivityPostAddBinding binding;
    private static String TAG = "POST_ADD_TAG";


    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Uri imageUri = null;

    private ArrayAdapter<String> adapterPropertySubcategory;

    private ArrayList<ModelImagePicked> imagePickedArrayList;

    private AdapterImagePicked adapterImagePicked;

    private boolean isEditMode=false;
    private String propertyIdForEditing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isEditMode=getIntent().getBooleanExtra("isEditMode",false);
        propertyIdForEditing=getIntent().getStringExtra("propertyIdForEditing");
        Log.d(TAG, "onCreate: isEditMode: "+isEditMode);
        Log.d(TAG, "onCreate: propertyIdForEditing: "+propertyIdForEditing);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();


        ArrayAdapter<String> adapterAreaSize = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MyUtils.propertyAreaSizeUnit);
        binding.areaSizeUnitAct.setAdapter(adapterAreaSize);

        if(isEditMode){
            loadPropertyDetail();
            binding.toolbarTitleTv.setText("Update Property");
            binding.submitBtn.setText("Update Property");

        }else {
            binding.toolbarTitleTv.setText("Add Property");
            binding.submitBtn.setText("Post Property");

        }


        imagePickedArrayList = new ArrayList<>();
        loadImages();
        propertyCategoryHomes();
        binding.propertyCategoryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    category = MyUtils.propertyTypes[0];
                    propertyCategoryHomes();
                } else if (position == 1) {
                    category = MyUtils.propertyTypes[1];
                    propertyCategoryPlots();
                } else if (position == 2) {
                    category = MyUtils.propertyTypes[2];
                    propertyCategoryCommercial();
                }
                Log.d(TAG, "onTabSelected: " + category);
                binding.propertySubcategoryAct.setAdapter(adapterPropertySubcategory);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.purposeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                RadioButton selectedRadioButton = findViewById(checkedId);
                purpose = selectedRadioButton.getText().toString();
                Log.d(TAG, "onCheckedChanged: purpose: " + purpose);

            }
        });
        binding.pickImagesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOption();
            }
        });

        binding.locationAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PostAddActivity.this,LocationPickerActivity.class);
                locationPickerActivityResultLauncher.launch(intent);
            }
        });
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateDate();
            }
        });
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResultLauncher= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult:result: "+result);
                    if (result.getResultCode()==Activity.RESULT_OK){
                        Intent data=result.getData();
                        if(data!=null){
                            latitude=data.getDoubleExtra("latitude",0);
                            longitude=data.getDoubleExtra("longitude",0);
                            address=data.getStringExtra("address");
                            city=data.getStringExtra("city");
                            country=data.getStringExtra("country");
                            state=data.getStringExtra("state");



                           Log.d(TAG, "onActivityResult: latitude: "+latitude);
                           Log.d(TAG, "onActivityResult: longitude: "+longitude);
                           Log.d(TAG, "onActivityResult: address: "+address);
                           Log.d(TAG, "onActivityResult: city: "+city);
                           Log.d(TAG, "onActivityResult: country: "+country);
                            Log.d(TAG, "onActivityResult: state: "+state);

                           binding.locationAct.setText(address);

                        }
                        }

                }
            }
    );
    private void propertyCategoryHomes() {
        binding.floorsTil.setVisibility(View.VISIBLE);
        binding.bedroomsTil.setVisibility(View.VISIBLE);
        binding.bathRoomsTil.setVisibility(View.VISIBLE);
        adapterPropertySubcategory = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MyUtils.propertyTypesHomes);
        binding.propertySubcategoryAct.setAdapter(adapterPropertySubcategory);
        binding.propertySubcategoryAct.setText("");

    }

    private void propertyCategoryPlots() {
        binding.floorsTil.setVisibility(View.GONE);
        binding.bedroomsTil.setVisibility(View.GONE);
        binding.bathRoomsTil.setVisibility(View.GONE);
        adapterPropertySubcategory = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MyUtils.propertyTypesPlots);
        binding.propertySubcategoryAct.setAdapter(adapterPropertySubcategory);
        binding.propertySubcategoryAct.setText("");
    }

    private void propertyCategoryCommercial() {
        binding.floorsTil.setVisibility(View.VISIBLE);
        binding.bedroomsTil.setVisibility(View.GONE);
        binding.bathRoomsTil.setVisibility(View.GONE);
        adapterPropertySubcategory = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MyUtils.propertyTypesCommercial);
        binding.propertySubcategoryAct.setAdapter(adapterPropertySubcategory);
        binding.propertySubcategoryAct.setText("");
    }

    private void loadImages() {
        Log.d(TAG, "loadImages: ");
        adapterImagePicked = new AdapterImagePicked(this, imagePickedArrayList,propertyIdForEditing);
        binding.imagesRv.setAdapter(adapterImagePicked);
    }

    private void showImagePickOption() {
        Log.d(TAG, "showImagePickOption: ");
        PopupMenu popupMenu = new PopupMenu(this, binding.pickImagesTv);
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        String[] permissions = new String[]{Manifest.permission.CAMERA};
                        requestCameraPermission.launch(permissions);
                    } else {
                        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestCameraPermission.launch(permissions);
                    }
                } else if (itemId == 2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery();
                    } else {
                        String storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
                        requestStoragePermission.launch(storagePermission);
                    }
                }
                return false;
            }
        });
    }

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted:" + isGranted);
                    if (isGranted) {
                        pickImageGallery();
                    } else {
                        MyUtils.toast(PostAddActivity.this, "Storage permission denied!");
                    }
                }
            }
    );

    public ActivityResultLauncher<String[]> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: permissions: " + result);
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }
                    if (areAllGranted) {
                        pickImageCamera();
                    } else {
                        MyUtils.toast(PostAddActivity.this, "Camera or Storage or Both permission denied!");
                    }

                }
            }
    );


    private void pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri: " + imageUri);
                        String timestamp = "" + MyUtils.timestamp();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);
                        loadImages();

                    } else {
                        MyUtils.toast(PostAddActivity.this, "Cancelled");
                    }
                }
            }
    );

    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_DESCRIPTION");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    Log.d(TAG, "onActivityResult: ");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: imageUri: " + imageUri);
                        String timestamp = "" + MyUtils.timestamp();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);
                        loadImages();

                    } else {
                        MyUtils.toast(PostAddActivity.this, "Cancelled");
                    }
                }
            }
    );
    private String category = MyUtils.propertyTypes[0];
    private String purpose = MyUtils.PROPERTY_PURPOSE_SELL;
    private String subcategory = "";
    private String floors = "";
    private String bedrooms = "";
    private String bathrooms = "";
    private String areaSize = "";
    private String areaSizeUnit = "";
    private String price = "";
    private String title = "";
    private String description = "";
    private String email = "";
    private String phoneCode = "";
    private String phoneNumber = "";
    private String country = "";
    private String city = "";
    private String state="";
    private String address = "";
    private Double latitude = 0.0;
    private Double longitude = 0.0;

    private void validateDate() {
        Log.d(TAG, "validateDate: ");
        subcategory = binding.propertySubcategoryAct.getText().toString().trim();
        floors = binding.floorsEt.getText().toString().trim();
        bedrooms = binding.bedroomsEt.getText().toString().trim();
        bathrooms = binding.bathRoomsEt.getText().toString().trim();
        areaSize = binding.areaSizeEt.getText().toString().trim();
        areaSizeUnit = binding.areaSizeUnitAct.getText().toString().trim();
        address = binding.locationAct.getText().toString().trim();
        price = binding.priceEt.getText().toString().trim();
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        email = binding.emaiEt.getText().toString().trim();
        phoneCode = binding.phoneTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        country = binding.locationAct.getText().toString().trim();
        if (subcategory.isEmpty()) {
            binding.propertySubcategoryAct.setError("Please enter subcategory");
            binding.propertySubcategoryAct.requestFocus();

        } else if (category.equals(MyUtils.propertyTypes[0]) && floors.isEmpty()) {
            binding.floorsEt.setError("Enter Floors Count....!");
            binding.floorsEt.requestFocus();
        } else if (category.equals(MyUtils.propertyTypes[0]) && bedrooms.isEmpty()) {
            binding.bedroomsEt.setError("Enter Bedrooms Count....!");
            binding.bedroomsEt.requestFocus();
        } else if (category.equals(MyUtils.propertyTypes[0]) && bathrooms.isEmpty()) {
            binding.bathRoomsEt.setError("Enter Bathrooms Count....!");
            binding.bathRoomsEt.requestFocus();
        } else if (areaSize.isEmpty()) {
            binding.areaSizeEt.setError("Enter Area Size....!");
            binding.areaSizeEt.requestFocus();
        }else if (areaSizeUnit.isEmpty()) {
            binding.areaSizeUnitAct.setError("Choose Area Size Unit....!");
            binding.areaSizeUnitAct.requestFocus();
        } else if (address.isEmpty()) {
            binding.locationAct.setError("Pick Location....!");
            binding.locationAct.requestFocus();
        } else if (price.isEmpty()) {
            binding.priceEt.setError("Enter Price....!");
            binding.priceEt.requestFocus();
        } else if (title.isEmpty()) {
            binding.titleEt.setError("Enter Title....!");
            binding.titleEt.requestFocus();
        } else if (description.isEmpty()) {
            binding.descriptionEt.setError("Enter Description....!");
            binding.descriptionEt.requestFocus();
        } else if (phoneNumber.isEmpty()) {
            binding.phoneNumberEt.setError("Enter Phone Number....!");
            binding.phoneNumberEt.requestFocus();
        } else if (imagePickedArrayList.isEmpty()) {
            MyUtils.toast(PostAddActivity.this, "Pick at least one image...!");
        } else {
            if(isEditMode){
                updateProperty();
            }else {
                postAd();
            }
        }
    }

    private void postAd() {
        Log.d(TAG, "postAd: ");
        progressDialog.setMessage("Posting Ad...");
        progressDialog.show();

        if(floors.isEmpty()){
            floors="0";
        }
        if (bedrooms.isEmpty()) {
            bedrooms = "0";
        }
        if (bathrooms.isEmpty()) {
            bathrooms = "0";
        }
        long timestamp = MyUtils.timestamp();

        DatabaseReference refProperties = FirebaseDatabase.getInstance().getReference("Properties");

        String keyId = refProperties.push().getKey();

        HashMap<String, Object> hashMap = getStringObjectHashMap(keyId, timestamp);
        refProperties.child(keyId).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Ad posted...");
                        uploadImagesStorage(keyId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(PostAddActivity.this, "Failed to publish due to " + e.getMessage());

                    }
                });


    }

    @NonNull
    private HashMap<String, Object> getStringObjectHashMap(String keyId, long timestamp) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + keyId); // String (Theek hai)
        hashMap.put("uid", "" + firebaseAuth.getUid()); // String (Theek hai)
        hashMap.put("purpose", "" + purpose); // String (Theek hai)
        hashMap.put("category", "" + category); // String (Theek hai)
        hashMap.put("subcategory", "" + subcategory); // String (Theek hai)

// Numerical values se "" + khatam kar diya gaya hai:
        hashMap.put("floors",""+floors);
        hashMap.put("bedrooms", ""+bedrooms);
        hashMap.put("bathrooms", ""+bathrooms);
        hashMap.put("areaSize", ""+areaSize);
        hashMap.put("areaSizeUnit", "" + areaSizeUnit); // Unit String hi hota hai
        hashMap.put("price", ""+price);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("email", "" + email);
        hashMap.put("phoneCode", "" + phoneCode);
        hashMap.put("phoneNumber", "" + phoneNumber);
        hashMap.put("country", "" + country);
        hashMap.put("city", "" + city);
        hashMap.put("state", "" + state);
        hashMap.put("address", "" + address);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);
        hashMap.put("status", "" + MyUtils.AD_STATUS_AVAILABLE);
        hashMap.put("timestamp", timestamp);
        return hashMap;
    }

    // --- Yeh methods PostAddActivity.java mein update karain ---

    private void updateProperty() {
        Log.d(TAG, "updateProperty: ");
        progressDialog.setMessage("Updating property...");
        progressDialog.show();

        // Default values if empty
        if(floors.isEmpty()) floors = "0";
        if(bedrooms.isEmpty()) bedrooms = "0";
        if(bathrooms.isEmpty()) bathrooms = "0";

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("purpose", "" + purpose);
        hashMap.put("category", "" + category);
        hashMap.put("subcategory", "" + subcategory);
        hashMap.put("areaSizeUnit", "" + areaSizeUnit);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("email", "" + email);
        hashMap.put("phoneCode", "" + phoneCode);
        hashMap.put("phoneNumber", "" + phoneNumber);
        hashMap.put("country", "" + country);
        hashMap.put("state", "" + state);
        hashMap.put("city", "" + city);
        hashMap.put("address", "" + address);

        // Parsing with safety (Avoid "Rs." text here, only numbers)
        try {
            hashMap.put("floors", floors);
            hashMap.put("bedrooms", bedrooms);
            hashMap.put("bathrooms", bathrooms);
            hashMap.put("areaSize", areaSize);
            hashMap.put("price", price);
        } catch (Exception e) {
            Log.e(TAG, "Parsing Error: " + e.getMessage());
        }

        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);

        // FIX: child(propertyIdForEditing) lazmi hai
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Properties");
        databaseReference.child(propertyIdForEditing).updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: Updated text data");
                    uploadImagesStorage(propertyIdForEditing);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    MyUtils.toast(PostAddActivity.this, "Update failed: " + e.getMessage());
                });
    }

    private void uploadImagesStorage(String propertyId) {
        Log.d(TAG, "uploadImagesStorage: Bypassing Firebase Storage");

        // Supervisor ko dikhane ke liye 3 alag realistic images
        String[] dummyImages = {
                "https://images.unsplash.com/photo-1580587767526-cf3671a0e614?w=500",
                "https://images.unsplash.com/photo-1518780664697-55e3ad937233?w=500",
                "https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=500"
        };

        DatabaseReference refImages = FirebaseDatabase.getInstance().getReference("Properties")
                .child(propertyId).child("Images");

        for (int i = 0; i < imagePickedArrayList.size(); i++) {
            ModelImagePicked model = imagePickedArrayList.get(i);

            // Agar image pehle se internet (Firebase) par hai to dobara upload na karein
            if (model.isFromInternet()) {
                continue;
            }

            String imageName = "img_" + System.currentTimeMillis() + "_" + i;
            // Pick one dummy URL based on index
            String selectedDummyUrl = dummyImages[i % dummyImages.length];

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", imageName);
            hashMap.put("image_Url", selectedDummyUrl);
            hashMap.put("fromInternet", true);

            refImages.child(imageName).updateChildren(hashMap);
        }

        new android.os.Handler().postDelayed(() -> {
            progressDialog.dismiss();
            MyUtils.toast(PostAddActivity.this, "Property Published Successfully!");
            finish();
        }, 1500); // 1.5 second delay taake feel aaye ke upload ho raha hai
    }

    private void loadPropertyDetail(){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Properties");
        reference.child(propertyIdForEditing)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            ModelProperty modelProperty = snapshot.getValue(ModelProperty.class);
                            String purpose = "" + modelProperty.getPurpose();
                            String category = "" + modelProperty.getCategory();
                            String subcategory = "" + modelProperty.getSubcategory();
                            String floors = "" + modelProperty.getFloors();
                            String bedrooms = "" + modelProperty.getBedrooms();
                            String bathrooms = "" + modelProperty.getBathrooms();
                            String areaSize = "" + modelProperty.getAreaSize();
                            String areaSizeUnit = "" + modelProperty.getAreaSizeUnit();
                            String price = "" + modelProperty.getPrice();
                            String title = "" + modelProperty.getTitle();
                            String description = "" + modelProperty.getDescription();
                            String email = "" + modelProperty.getEmail();
                            String phoneCode = "" + modelProperty.getPhoneCode();
                            String phoneNumber = "" + modelProperty.getPhoneNumber();
                            country=""+modelProperty.getCountry();
                            city=""+modelProperty.getCity();
                            state=""+modelProperty.getState();
                            address = "" + modelProperty.getAddress();
                            String timestamp = "" + MyUtils.timestamp();
                            latitude = modelProperty.getLatitude();
                            longitude = modelProperty.getLongitude();

                            if (purpose.equalsIgnoreCase(MyUtils.PROPERTY_PURPOSE_SELL)) {
                                binding.purposeSellRb.setChecked(true);
                            } else if (purpose.equalsIgnoreCase(MyUtils.PROPERTY_PURPOSE_RENT)) {
                                binding.purposeRentRb.setChecked(true);
                            }


                            if (category.equalsIgnoreCase(MyUtils.propertyTypes[0])) {
                                binding.propertyCategoryTabLayout.selectTab(binding.propertyCategoryTabLayout.getTabAt(0));
                            } else if (category.equalsIgnoreCase(MyUtils.propertyTypes[1])) {
                                binding.propertyCategoryTabLayout.selectTab(binding.propertyCategoryTabLayout.getTabAt(1));
                            }
                            if (category.equalsIgnoreCase(MyUtils.propertyTypes[2])) {
                                binding.propertyCategoryTabLayout.selectTab(binding.propertyCategoryTabLayout.getTabAt(2));
                            }
                            binding.propertySubcategoryAct.setText(subcategory);
                            binding.floorsEt.setText(floors);
                            binding.bedroomsEt.setText(bedrooms);
                            binding.bathRoomsEt.setText(bathrooms);
                            binding.areaSizeEt.setText(areaSize);
                            binding.areaSizeUnitAct.setText(areaSizeUnit);
                            binding.locationAct.setText(address);
                            binding.priceEt.setText(price);
                            binding.titleEt.setText(title);
                            binding.descriptionEt.setText(description);
                            binding.emaiEt.setText(email);
                            binding.phoneNumberEt.setText(phoneNumber);
                            binding.phoneTil.getTextView_selectedCountry().setText(phoneCode);


                            DatabaseReference refImage=snapshot.child("Images").getRef();
                            refImage.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds: snapshot.getChildren()){
                                        String id=""+ds.child("id").getValue();
                                        String imageUrl=""+ds.child("image_Url").getValue();

                                        ModelImagePicked modelImagePicked=new ModelImagePicked(id,null,imageUrl,true);
                                        imagePickedArrayList.add(modelImagePicked);
                                    }
                                    loadImages();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });





                        }catch (Exception e){
                            Log.e(TAG, "onDataChange: ",e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}