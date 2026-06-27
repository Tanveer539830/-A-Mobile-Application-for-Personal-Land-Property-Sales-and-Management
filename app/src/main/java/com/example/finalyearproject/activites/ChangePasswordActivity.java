package com.example.finalyearproject.activites;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    // View Binding for accessing UI elements
    private ActivityChangePasswordBinding binding;

    // TAG for Logcat debugging
    private static final String TAG="CHANGE_PASSWORD_TAG";

    // Firebase Auth instances to handle password change
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    
    // ProgressDialog to show loading status
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initializing View Binding
        binding=ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initializing Firebase Auth and getting current user
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        
        // Configuring ProgressDialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Toolbar back button click listener
        binding.toolbarBackBtn.setOnClickListener(v->{
            finish();
        });
        
        // Submit button click listener
        binding.submitBtn.setOnClickListener(v->{
            validateData();
        });
    }

    // Variables to hold password strings
    private String currentPassword="";
    private String newPassword="";
    private String confirmNewPassword="";

    // Function to validate user input
    private void validateData() {
        Log.d(TAG,"validateData:");

        // Getting text from EditText fields
        currentPassword=binding.currentPasswordEt.getText().toString().trim();
        newPassword=binding.newPasswordEt.getText().toString().trim();
        confirmNewPassword=binding.confirmNewPasswordEt.getText().toString().trim();
        
        // Validation logic
        if(currentPassword.isEmpty()){
            binding.confirmNewPasswordEt.setError("Enter current password");
            binding.currentPasswordEt.requestFocus();
        }else if(newPassword.isEmpty()){
            binding.newPasswordEt.setError("Enter new password");
            binding.newPasswordEt.requestFocus();
        }else if(confirmNewPassword.isEmpty()){
            binding.confirmNewPasswordEt.setError("Enter confirm password");
            binding.confirmNewPasswordEt.requestFocus();
        }else if(!newPassword.equals(confirmNewPassword)){
            binding.confirmNewPasswordEt.setError("Password doesn't match");
            binding.confirmNewPasswordEt.requestFocus();
        }else{
            // If all validations pass, proceed to re-authentication
            authenticationUserForUpdatePassword();
        }
    }

    // Re-authenticating the user because changing password is a sensitive operation
    private void authenticationUserForUpdatePassword(){
      Log.d(TAG,"authenticationUserForUpdatePassword:");
      progressDialog.setMessage("Authenticating User");
      progressDialog.show();
      
      // Creating credentials with current email and password
      AuthCredential authCredential= EmailAuthProvider.getCredential(firebaseUser.getEmail(),currentPassword);
      
      // Re-authenticating with Firebase
      firebaseUser.reauthenticate(authCredential)
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void unused) {
                      Log.d("TAG","onSuccess: Authenticated");
                      // If authentication is successful, call updatePassword()
                      updatePassword();
                  }
              })
              .addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {
                      Log.e(TAG,"onFailure: ",e);
                      progressDialog.dismiss();
                      // Showing error toast if authentication fails
                      MyUtils.toast(ChangePasswordActivity.this,"Failed to authenticate due to "+e.getMessage());
                  }
              });
    }

    // Function to update the password in Firebase
    private void updatePassword() {
        Log.d(TAG,"updatePassword:");
        progressDialog.setMessage("Updating Password");
        progressDialog.show();
        
        // Updating password for the current Firebase user
        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                      Log.d(TAG,"onSuccess: Password Updated");
                      progressDialog.dismiss();
                      // Success message
                      MyUtils.toast(ChangePasswordActivity.this,"Password Updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ",e);
                        progressDialog.dismiss();
                        // Error message if update fails
                        MyUtils.toast(ChangePasswordActivity.this,"Failed to update due to "+e.getMessage());
                    }
                });
    }
}
