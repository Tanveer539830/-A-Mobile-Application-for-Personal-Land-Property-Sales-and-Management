package com.example.finalyearproject.activites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalyearproject.R;
import com.example.finalyearproject.databinding.ActivityDeleteAccountBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeleteAccountActivity extends AppCompatActivity {

    private ActivityDeleteAccountBinding binding;
    private static final String TAG = "DELETE_ACCOUNT_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        binding.toolbarBackBtn.setOnClickListener(v -> finish());

        // Submit Button Click
        binding.submitBtn.setOnClickListener(v -> {
            // Check if user is logged in
            if (firebaseUser != null) {
                deleteUserData();
            } else {
                MyUtils.toast(this, "No user logged in");
                startMainActivity();
            }
        });
    }

    private void deleteUserData() {
        Log.d(TAG, "deleteUserData: Deleting user profile from database...");
        progressDialog.setMessage("Deleting user profile...");
        progressDialog.show();

        // Realtime Database se user ka profile delete karna (Free feature)
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: User profile deleted");
                    deleteUserProperties(); // Agla step: Ads delete karna
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "onFailure: ", e);
                    MyUtils.toast(this, "Failed to delete user data: " + e.getMessage());
                });
    }

    private void deleteUserProperties() {
        Log.d(TAG, "deleteUserProperties: Deleting user ads...");
        progressDialog.setMessage("Deleting your published ads...");

        // FIX: getSourceDatabase() ko hata kar seedha getReference() use kiya
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Properties");

        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.d(TAG, "onDataChange: No ads found for this user");
                            deleteAccount();
                            return;
                        }

                        // Loop chala kar sirf Database se data delete karein (Storage ko hath nahi lagana)
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }

                        Log.d(TAG, "onDataChange: Ads deleted from database");
                        deleteAccount();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (progressDialog != null) progressDialog.dismiss();
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
    }

    private void deleteAccount() {
        Log.d(TAG, "deleteAccount: Deleting Firebase Auth account");
        progressDialog.setMessage("Deleting your account credentials...");

        // Firebase Auth se user delete karna (Free feature)
        firebaseUser.delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: Auth account deleted");
                    progressDialog.dismiss();
                    MyUtils.toast(this, "Account Deleted Successfully");
                    startMainActivity();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "onFailure: ", e);
                    // Agar re-authentication ka error aaye (security reason) to sign out karwa dein
                    MyUtils.toast(this, "Authentication error. Please login again to delete account.");
                    startMainActivity();
                });
    }

    private void startMainActivity() {
        firebaseAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}