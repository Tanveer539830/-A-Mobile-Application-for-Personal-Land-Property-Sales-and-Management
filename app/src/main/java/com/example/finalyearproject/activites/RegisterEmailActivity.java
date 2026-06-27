package com.example.finalyearproject.activites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.finalyearproject.R;
import com.example.finalyearproject.databinding.ActivityRegisterEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterEmailActivity extends AppCompatActivity {
private ActivityRegisterEmailBinding binding;
private FirebaseAuth firebaseAuth;
private ProgressDialog progressDialog;

private static final String TAG="REGISTER_EMAIL_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_email);
        binding=ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.haveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            validData();
            }
        });
    }
    private String email,password,cPassword;
    private void validData(){

        email=binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString().trim();
        cPassword=binding.cPasswordEt.getText().toString().trim();
        Log.d(TAG,"validData:Email:"+email);
        Log.d(TAG,"validData:password:"+password);
        Log.d(TAG,"validData:Confirm Password:"+cPassword);
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {
            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        } else if (!password.equals(cPassword)) {
            binding.cPasswordEt.setError("Password doesn't match");
            binding.cPasswordEt.requestFocus();
        }else {
            registerUser();
        }
    }
    private void registerUser(){
        progressDialog.setMessage("Creating Account");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG,"onSuccess Register Success");
                       updateUserInfo();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure",e);
                        MyUtils.toast(RegisterEmailActivity.this,"Failed due to"+e.getMessage());
                        progressDialog.dismiss();
                    }
                });
    }
    private void updateUserInfo(){
        progressDialog.setMessage("Saving User Info...");
        long timestamp=MyUtils.timestamp();
        String registerUserEmail=firebaseAuth.getCurrentUser().getEmail();
        String registerUserUid=firebaseAuth.getUid();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid",registerUserUid);
        hashMap.put("email",registerUserEmail);
        hashMap.put("name","");
        hashMap.put("timestamp",timestamp);
        hashMap.put("phoneCode","");
        hashMap.put("phoneNumber","");
        hashMap.put("profileImageUrl","");
        hashMap.put("dob","");
        hashMap.put("userType",MyUtils.USER_TYPE_EMAIL);
        hashMap.put("token","");
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(registerUserUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                     Log.d(TAG,"on Success :Info Saved...");
                     progressDialog.dismiss();
                     startActivity(new Intent(RegisterEmailActivity.this,MainActivity.class));
                     finishAffinity();
                }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                    Log.e(TAG,"onFailure: ",e);
                    MyUtils.toast(RegisterEmailActivity.this,"Failed to Save due to "+e.getMessage());
                    progressDialog.dismiss();
            }
        });
    }
}