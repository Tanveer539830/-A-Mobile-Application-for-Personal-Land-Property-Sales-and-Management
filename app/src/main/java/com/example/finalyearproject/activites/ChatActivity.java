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

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.example.finalyearproject.adapter.AdapterChat;
import com.example.finalyearproject.databinding.ActivityChatBinding;
import com.example.finalyearproject.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private static final String TAG = "CHAT_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String receiptUid = "";
    private String myUid = "";
    private String chatPath = "";
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait....");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        receiptUid = getIntent().getStringExtra("receiptUid");
        myUid = firebaseAuth.getUid();

        // MyUtils check karein ke path correct generate ho raha hai
        chatPath = MyUtils.chatPath(receiptUid, myUid);

        loadReceiptDetails();
        loadMessages();

        binding.toolbarBackBtn.setOnClickListener(v -> finish());
        binding.sendFab.setOnClickListener(v -> validateData());
        binding.attachFab.setOnClickListener(v -> imagePickOptions());
    }
    // ChatActivity.java ke loadReceiptDetails mein ye lines add kar dein agar real data nahi hai
    private void loadReceiptDetails() {
        // Dummy data for Demo
        binding.toolbarTitleTv.setText("Muhammad Ali");
        binding.toolbarProfileIv.setImageResource(R.drawable.tanveer_image);

        // Baqi Firebase wala code niche rehne dein...
    }
//    private void loadReceiptDetails() {
//        Log.d(TAG, "loadReceiptDetails: Loading seller details for UID: " + receiptUid);
//
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//        reference.child(receiptUid).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                try {
//                    // Database se name aur image uthayein
//                    String name = "" + snapshot.child("name").getValue();
//                    String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
//
//                    // UI update karein
//                    if (name.equals("null") || name.isEmpty()) {
//                        binding.toolbarTitleTv.setText("Seller"); // Default agar naam na ho
//                    } else {
//                        binding.toolbarTitleTv.setText(name);
//                    }
//
//                    // Image load karein Glide se
//                    Glide.with(ChatActivity.this)
//                            .load(profileImageUrl)
//                            .placeholder(R.drawable.person_black) // Default icon
//                            .error(R.drawable.person_black)       // Error icon
//                            .into(binding.toolbarProfileIv);
//
//                } catch (Exception e) {
//                    Log.e(TAG, "onDataChange: Error loading seller details", e);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "onCancelled: " + error.getMessage());
//            }
//        });
//    }

    private void loadMessages(){
        Log.d(TAG, "loadMessages: Loading messages");

        ArrayList<ModelChat> chatsArrayLists=new ArrayList<>();
        DatabaseReference refChat=FirebaseDatabase.getInstance().getReference("Chats");
        refChat.child(chatPath)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatsArrayLists.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                           try {
                               ModelChat modelChat=ds.getValue(ModelChat.class);
                               chatsArrayLists.add(modelChat);
                           }catch (Exception e){
                               Log.e(TAG, "onDataChange: ",e);
                           }
                        }
                        AdapterChat adapterChat=new AdapterChat(ChatActivity.this,chatsArrayLists);
                        binding.chatRv.setAdapter(adapterChat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void validateData() {
        String message = binding.messageEt.getText().toString().trim();
        long timestamp = MyUtils.timestamp();

        if (message.isEmpty()) {
            MyUtils.toast(this, "Enter Message to send....");
        } else {
            sendMessage(MyUtils.MESSAGE_TYPE_TEXT, message, timestamp);
        }
    }

    private void sendMessage(String messageType, String message, long timestamp) {
        progressDialog.setMessage("Sending...");
        progressDialog.show();

        DatabaseReference refChat = FirebaseDatabase.getInstance().getReference("Chats");
        String keyId = refChat.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("messageId", keyId);
        hashMap.put("message", message);
        hashMap.put("messageType", messageType);
        hashMap.put("toUid", receiptUid);
        hashMap.put("fromUid", myUid);
        hashMap.put("timestamp", timestamp);

        refChat.child(chatPath).child(keyId).setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    binding.messageEt.setText("");
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    MyUtils.toast(ChatActivity.this, "Failed: " + e.getMessage());
                });
    }

    private void imagePickOptions() {
        PopupMenu popupMenu = new PopupMenu(this, binding.attachFab);
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA});
                } else {
                    requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
            } else if (itemId == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImageGallery();
                } else {
                    requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
            return true;
        });
    }

    private final ActivityResultLauncher<String[]> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean areAllGranted = true;
                for (Boolean isGranted : result.values()) areAllGranted = areAllGranted && isGranted;
                if (areAllGranted) pickImageCamera();
                else MyUtils.toast(this, "Camera Permission Denied");
            }
    );

    private final ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) pickImageGallery();
                else MyUtils.toast(this, "Storage Permission Denied");
            }
    );

    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: Initializing Camera");
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Chat_Image_" + System.currentTimeMillis());
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Captured via App");

        // ImageUri ko initialize karna
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        try {
            cameraActivityResultLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "pickImageCamera: ", e);
            MyUtils.toast(this, "Camera failed to open");
        }
    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG,"onActivityResult: Image Uri: "+imageUri);
                    // Storage nahi hai, isliye seedha dummy function call karein
                    uploadImageDummy();
                } else {
                    MyUtils.toast(ChatActivity.this,"Cancelled");
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    // Storage nahi hai, isliye dummy function call karein
                    uploadImageDummy();
                } else {
                    MyUtils.toast(ChatActivity.this,"Cancelled");
                }
            }
    );

    private void uploadImageDummy() {
        progressDialog.setMessage("Sending image...");
        progressDialog.show();

        // STORAGE BYPASS: Dummy Image URL for Demo (Real image link from internet)
        String dummyImageUrl = "https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=500";

        // Chat node mein message save karein
        sendMessage(MyUtils.MESSAGE_TYPE_IMAGE, dummyImageUrl, MyUtils.timestamp());
    }
}