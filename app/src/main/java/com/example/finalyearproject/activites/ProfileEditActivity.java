package com.example.finalyearproject.activites;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.example.finalyearproject.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {
    private ActivityProfileEditBinding binding;
    private static final String TAG = "PROFILE_EDIT_TAG";

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String myUserType = "";
    private Uri imageUri = null;

    private String name = "";
    private String dob = "";
    private String email = "";
    private String phoneCode = "";
    private String phoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadUserInfo();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.profileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        binding.dobEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                binding.dobEt.setText(selectedDate);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        dob = binding.dobEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneCode = binding.countryCodePicker.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();

        if (name.isEmpty()) {
            binding.nameEt.setError("Enter Name");
            binding.nameEt.requestFocus();
        } else {
            if (imageUri == null) {
                updateProfileDb(null);
            } else {
                uploadProfileImageStorage();
            }
        }
    }

    private void uploadProfileImageStorage() {
        Log.d(TAG, "uploadProfileImageStorage: ImageUri: " + imageUri);
        progressDialog.setMessage("Uploading Profile Image");
        progressDialog.show();

        String filePathAndName = "UserImages/" + firebaseAuth.getUid();

        // بکٹ چیک کریں
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child(filePathAndName);

        storageReference.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading Profile Image " + (int) progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Profile Image Uploaded");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String uploadedImageUrl = uri.toString();
                                updateProfileDb(uploadedImageUrl);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: Storage Error: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditActivity.this, "Failed to upload Profile Image due to " + e.getMessage());
                    }
                });
    }

    private void updateProfileDb(String imageUrl) {
        Log.d(TAG, "updateProfileDb:");
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();

        HashMap<String, Object> hashMap = getStringObjectHashMap(imageUrl);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Profile Updated");
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditActivity.this, "Profile Updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure:", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditActivity.this, "Failed to update profile due to " + e.getMessage());
                    }
                });
    }

    @NonNull
    private HashMap<String, Object> getStringObjectHashMap(String imageUrl) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", name);
        hashMap.put("dob", dob);
        if (imageUrl != null) {
            hashMap.put("profileImageUrl", imageUrl);
        }
        if (myUserType.equals(MyUtils.USER_TYPE_EMAIL) || myUserType.equals(MyUtils.USER_TYPE_GOOGLE)) {
            hashMap.put("phoneCode", phoneCode);
            hashMap.put("phoneNumber", phoneNumber);
        } else if (myUserType.equals(MyUtils.USER_TYPE_PHONE)) {
            hashMap.put("email", email);
        }
        return hashMap;
    }

    private void imagePickDialog() {
        PopupMenu popupMenu = new PopupMenu(this, binding.profileImagePickFab);
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA});
                    } else {
                        requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId == 2) {
                    pickImageGallery();
                }
                return true;
            }
        });
    }

    private final ActivityResultLauncher<String[]> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {
                        if (!isGranted) {
                            areAllGranted = false;
                            break;
                        }
                    }
                    if (areAllGranted) {
                        pickImageCamera();
                    } else {
                        MyUtils.toast(ProfileEditActivity.this, "Camera or Storage permission denied");
                    }
                }
            }
    );

    private void pickImageCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "temp_image");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "temp_image_description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Glide.with(ProfileEditActivity.this)
                                .load(imageUri)
                                .placeholder(R.drawable.person_black)
                                .into(binding.profileImageView);
                    } else {
                        MyUtils.toast(ProfileEditActivity.this, "Canceled");
                    }
                }
            }
    );

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imageUri = data.getData();
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.person_black)
                                    .into(binding.profileImageView);
                        }
                    } else {
                        MyUtils.toast(ProfileEditActivity.this, "Canceled");
                    }
                }
            }
    );

    private void loadUserInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");        ref.child("" + firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // ڈیٹا بیس سے ویلیوز حاصل کریں
                        String name = "" + snapshot.child("name").getValue();
                        String dob = "" + snapshot.child("dob").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                        myUserType = "" + snapshot.child("userType").getValue();

                        // ویوز میں ڈیٹا سیٹ کریں
                        binding.nameEt.setText(name);
                        binding.dobEt.setText(dob);
                        binding.emailEt.setText(email);
                        binding.phoneNumberEt.setText(phoneNumber);

                        try {
                            int code = Integer.parseInt(phoneCode.replace("+", ""));
                            binding.countryCodePicker.setCountryForPhoneCode(code);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: PhoneCode Error", e);
                        }

                        // یوزر ٹائپ کے حساب سے فیلڈز چھپائیں یا دکھائیں
                        if (myUserType.equals(MyUtils.USER_TYPE_EMAIL) || myUserType.equals(MyUtils.USER_TYPE_GOOGLE)) {
                            binding.emailEt.setEnabled(false); // ای میل تبدیل نہیں ہو سکتی
                        } else if (myUserType.equals(MyUtils.USER_TYPE_PHONE)) {
                            binding.phoneNumberEt.setEnabled(false); // فون نمبر تبدیل نہیں ہو سکتا
                            binding.countryCodePicker.setEnabled(false);
                        }

                        // پروفائل امیج لوڈ کریں
                        Glide.with(ProfileEditActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.person_black)
                                .into(binding.profileImageView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: ", error.toException());
                    }
                });
    }
}