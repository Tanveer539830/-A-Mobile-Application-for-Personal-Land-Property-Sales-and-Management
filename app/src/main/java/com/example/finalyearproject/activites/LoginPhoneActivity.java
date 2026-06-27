package com.example.finalyearproject.activites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject.databinding.ActivityLoginPhoneBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {
private ActivityLoginPhoneBinding binding;
public static final String TAG="LOGIN_PHONE_TAG";
private FirebaseAuth firebaseAuth;
private ProgressDialog progressDialog;
private PhoneAuthProvider.ForceResendingToken forceResendingToken;
private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
private String mVerificationId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        binding=ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.phoneInputRl.setVisibility(View.VISIBLE);
       binding.resendOtpTv.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               resendVerificationCode(forceResendingToken);
           }
       });
       binding.verifyOtpBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String otp = binding.otpEt.getText().toString().trim();
               if(otp.isEmpty()){
                   binding.otpEt.setError("Enter OTP");
                   binding.otpEt.requestFocus();
               } else if (otp.length()<6) {
                   binding.otpEt.setError("OTP Length must be 6 character");
                   binding.otpEt.requestFocus();
               }else {
                   verifyPhoneNoWithCode(otp);
               }
           }
       });

       firebaseAuth=FirebaseAuth.getInstance();
      phoneLoginCallBack();
       binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               finish();
           }
       });
       binding.sendOtpBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
             validData();
           }
       });
    }
    private String phoneCode="",phoneNumber="",phoneNumberwithCode="";
    private void validData(){
        phoneCode=binding.phoneTil.getDefaultCountryCodeWithPlus();
        phoneNumber=binding.phoneNumberEt.getText().toString().trim();
        phoneNumberwithCode=phoneCode+phoneNumber;
        Log.d(TAG,"validData:Phone Code: "+phoneCode);
        Log.d(TAG,"validData:Phone Number: "+phoneNumber);
        Log.d(TAG,"validDate:Phone Number With Code: "+phoneNumberwithCode);
        if(phoneNumber.isEmpty()){
            binding.phoneNumberEt.setError("Enter Phone Number");
            binding.phoneNumberEt.requestFocus();
        }else {
            startPhoneNumberVerification();
        }
    }
    private void verifyPhoneNoWithCode(String otp){
        Log.d(TAG,"verifyPhoneNumberWithCode: OTP"+otp);
        progressDialog.setMessage("Verifying OTP....");
        progressDialog.show();
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVerificationId,otp);
        signInWithPhoneAuthCredential(credential);
    }
    private void startPhoneNumberVerification(){
        progressDialog.setMessage("Sending OTP to "+phoneNumberwithCode);
        progressDialog.show();
        PhoneAuthOptions phoneAuthOptions=PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumberwithCode)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallBacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }
    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token){
        progressDialog.setMessage("Resending OTP to "+phoneNumberwithCode);
        progressDialog.show();
        PhoneAuthOptions phoneAuthOptions=PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumberwithCode)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallBacks)
                .setForceResendingToken(token)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }
    private void phoneLoginCallBack(){
        Log.d("TAG","phoneLoginCallBack: ");
        mCallBacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                Log.d(TAG,"onCodeSent: ");
                mVerificationId=verificationId;
                forceResendingToken=token;

                progressDialog.dismiss();
                binding.phoneInputRl.setVisibility(View.GONE);
                binding.otpInputRl.setVisibility(View.VISIBLE);

                MyUtils.toast(LoginPhoneActivity.this,"OTP sent to "+phoneNumberwithCode);
                binding.loginPhoneLabel.setText("Please Type verification code sent to "+phoneNumberwithCode);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG,"onVerificationCompleted: ");
              signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG,"onVerificationFailed",e);
                progressDialog.dismiss();
                MyUtils.toast(LoginPhoneActivity.this,"Failed to verify due to "+e.getMessage());
            }
        };
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        Log.d(TAG,"SignInWithPhoneAuthCredential: ");
        progressDialog.setMessage("Logging In...");
        progressDialog.show();
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.d(TAG,"onSuccess");
                if(authResult.getAdditionalUserInfo().isNewUser()){
                    Log.d(TAG,"onSuccess:New User,Account created...");
                    updateUserInfo();
                }else {
                    Log.d(TAG,"onSuccess: Existing User ,Logged In...");
                    startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                    finishAffinity();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
           Log.e(TAG,"onFailure: ",e);
           progressDialog.dismiss();
           MyUtils.toast(LoginPhoneActivity.this,"Login Fail due to "+e.getMessage());
            }
        });
    }
    private void updateUserInfo(){
        Log.d(TAG,"updateUserInfo: ");
        progressDialog.setMessage("Saving User Info...");
        progressDialog.show();
        long timestamp=MyUtils.timestamp();
        String registeredUserUid=firebaseAuth.getUid();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid",registeredUserUid);
        hashMap.put("email","");
        hashMap.put("name","");
        hashMap.put("timestamp",timestamp);
        hashMap.put("phoneCode",phoneCode);
        hashMap.put("phoneNumber",phoneNumber);
        hashMap.put("profileImageUrl","");
        hashMap.put("dob","");
        hashMap.put("userType",""+MyUtils.USER_TYPE_PHONE);
        hashMap.put("token","");

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registeredUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess : User Info Saved");
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));                        finishAffinity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                   Log.e(TAG,"onFailure: ", e);
                   progressDialog.dismiss();
                   MyUtils.toast(LoginPhoneActivity.this,"Failed to save due to "+e.getMessage());
                    }
                });




    }
}