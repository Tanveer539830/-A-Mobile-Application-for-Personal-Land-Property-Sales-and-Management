package com.example.finalyearproject.activites;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject.R;
import com.example.finalyearproject.databinding.ActivityLoginOptionsBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginOptionsActivity extends AppCompatActivity {
    private ActivityLoginOptionsBinding binding;
    private static final String TAG = "LOGIN_OPTIONS";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.skipBtn.setOnClickListener(v -> finish());
        binding.loginGoogleBtn.setOnClickListener(v -> beginGoogleLogin());
        binding.loginEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginOptionsActivity.this,MainLoginEmailActivity.class);
                startActivity(intent);
            }
        });
        binding.loginPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginOptionsActivity.this,LoginPhoneActivity.class));
            }
        });
    }

    private void beginGoogleLogin() {
        Log.d(TAG, "beginGoogleLogin");
        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInnARl.launch(googleSignInIntent);
    }

    private final ActivityResultLauncher<Intent> googleSignInnARl = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "onActivityResult: ");
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogleAccount(account.getIdToken());
                    } catch (Exception e) {
                        Log.e(TAG, "OnActivityResult: ", e);
                        MyUtils.toast(this, "Login Failed: " + e.getMessage());
                    }
                } else {
                    MyUtils.toast(this, "Cancelled...!");
                }
            }
    );

    private void firebaseAuthWithGoogleAccount(String idToken) {
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getAdditionalUserInfo().isNewUser()) {
                        updateUserInfoDb();
                    } else {
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginOptionsActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "onFailure", e);
                    MyUtils.toast(this, e.getMessage());
                });
    }

    private void updateUserInfoDb() {
        progressDialog.setMessage("Saving User Info....!");

        long timestamp = MyUtils.timestamp();
        String registerUserUid = firebaseAuth.getUid();
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String name = firebaseAuth.getCurrentUser().getDisplayName();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", registerUserUid);
        hashMap.put("email", registerUserEmail);
        hashMap.put("name", name);
        hashMap.put("timestamp", timestamp);
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", "");
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType", MyUtils.USER_TYPE_GOOGLE);
        hashMap.put("token", "");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(registerUserUid).setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    startActivity(new Intent(LoginOptionsActivity.this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                   MyUtils.toast(this, "Failed to save: " + e.getMessage());
                });
    }
}