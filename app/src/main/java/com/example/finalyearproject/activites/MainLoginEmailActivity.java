package com.example.finalyearproject.activites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject.databinding.ActivityMainLoginEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainLoginEmailActivity extends AppCompatActivity {
    private ActivityMainLoginEmailBinding binding;
    private static final String TAG="LOGIN_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    @Override
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...!");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth=FirebaseAuth.getInstance();
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               validData();
            }
        });
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainLoginEmailActivity.this, RegisterEmailActivity.class));
            }
        });
        binding.forgetPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainLoginEmailActivity.this, ForgetPasswordActivity.class));
                finish();
            }
        });
    }
    private String email,password;
private void validData(){
        email=binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString();
    Log.d(TAG,"ValidateData: Email: "+email);
    Log.d(TAG,"ValidateData: Password: "+password);

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.setError("Invalid Email!");
            binding.emailEt.requestFocus();
        }else if(password.isEmpty()){
            binding.passwordEt.setError("Enter Password!");
            binding.passwordEt.requestFocus();
        }else {
            loginUser();
        }
}
private void loginUser(){
    progressDialog.setMessage("Logging In....");
    progressDialog.show();
    firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Log.d(TAG,"onSuccess: Logged In...!");
                    progressDialog.dismiss();
                    startActivity(new Intent(MainLoginEmailActivity.this, MainActivity.class));
                    finishAffinity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG,"onFailure",e);
                    progressDialog.dismiss();
                    MyUtils.toast(MainLoginEmailActivity.this,"Failed due to"+e.getMessage());
                }
            });
}
}
