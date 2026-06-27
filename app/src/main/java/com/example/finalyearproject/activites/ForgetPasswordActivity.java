package com.example.finalyearproject.activites;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject.databinding.ActivityForgetPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {
    private ActivityForgetPasswordBinding binding;
    public static final String TAG="FORGET_PASSWORD_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth=FirebaseAuth.getInstance();
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    private String email="";
    private void validateData(){
        email=binding.emailEt.getText().toString().trim();
        Log.d(TAG,"validateData: Email: "+email);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();

        }else {
            sendPasswordRecoveryInstructions();
        }
    }
    private void sendPasswordRecoveryInstructions(){
        Log.d(TAG,"sendPasswordRecoveryInstructions");
        progressDialog.setMessage("Sending password recovery Instruction to "+email);
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Instructions Sent ");
                        progressDialog.dismiss();
                        MyUtils.toast(ForgetPasswordActivity.this,"Instruction to reset password sent to"+email);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ",e);
                        progressDialog.dismiss();
                        MyUtils.toast(ForgetPasswordActivity.this,"Failure to send due to "+e.getMessage());
                    }
                });
    }
}