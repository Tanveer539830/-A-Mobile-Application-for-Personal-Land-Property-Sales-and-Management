package com.example.finalyearproject.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.example.finalyearproject.activites.ChangePasswordActivity;
import com.example.finalyearproject.activites.DeleteAccountActivity;
import com.example.finalyearproject.activites.MainActivity;
import com.example.finalyearproject.activites.MyPropertyListActivity;
import com.example.finalyearproject.activites.MyUtils;
import com.example.finalyearproject.activites.PostAddActivity;
import com.example.finalyearproject.activites.ProfileEditActivity;
import com.example.finalyearproject.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;

    private static final String TAG = "PROFILE_TAG";

    private Context mcontext;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;


    @Override
    public void onAttach(@NonNull Context context) {
        mcontext = context;
        super.onAttach(context);
    }

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressDialog = new ProgressDialog(mcontext);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();
        binding.postAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mcontext, PostAddActivity.class);
                startActivity(intent);
            }
        });
        binding.logoutCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(mcontext, MainActivity.class));
                getActivity().finishAffinity();
            }
        });
        binding.myPropertiesCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mcontext, MyPropertyListActivity.class));
            }
        });
        binding.editProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mcontext, ProfileEditActivity.class));
            }
        });
        binding.changePasswordCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mcontext, ChangePasswordActivity.class));
            }
        });
        binding.deleteAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(mcontext,DeleteAccountActivity.class));
            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child("" + firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dob = "" + snapshot.child("dob").getValue();
                String email = "" + snapshot.child("email").getValue();
                String name = "" + snapshot.child("name").getValue();
                String phoneCode = "" + snapshot.child("phoneCode").getValue();
                String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                String timestamp = "" + snapshot.child("timestamp").getValue();
                String userType = "" + snapshot.child("userType").getValue();
                String phone = phoneCode + phoneNumber;

                if (timestamp.equals("null")) {
                    timestamp = "0";
                }
                String formattedDate = MyUtils.formatTimestamp(Long.parseLong(timestamp));
                binding.emailTv.setText(email);
                binding.fullNameTv.setText(name);
                binding.phoneTv.setText(phone);
                binding.dobTv.setText(dob);
                binding.memberSinceTv.setText(formattedDate);

                try {
                    Glide.with(mcontext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.tanveer_image)
                            .into(binding.profileImageView);
                } catch (Exception e) {
                    // Handle Glide exception
                }

                if (userType.equals(MyUtils.USER_TYPE_EMAIL)) {
                    if (firebaseAuth.getCurrentUser() != null) {
                        boolean isVerified = firebaseAuth.getCurrentUser().isEmailVerified();
                        if (isVerified) {
                            binding.verifyAccountCv.setVisibility(View.GONE);
                            binding.verificationTv.setText("Verified");
                        } else {
                            binding.verifyAccountCv.setVisibility(View.VISIBLE);
                            binding.verificationTv.setText("Not Verified");
                        }
                    }
                } else {
                    binding.verifyAccountCv.setVisibility(View.GONE);
                    binding.verificationTv.setText("Verified");
                }
                try {
                  Glide.with(mcontext)
                          .load(profileImageUrl)
                          .placeholder(R.drawable.tanveer_image)
                          .into(binding.profileImageView);
                } catch (Exception e) {
                    Log.e(TAG,"onDateChange: ",e);
                    MyUtils.toast(mcontext,"Failed to load due to");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}