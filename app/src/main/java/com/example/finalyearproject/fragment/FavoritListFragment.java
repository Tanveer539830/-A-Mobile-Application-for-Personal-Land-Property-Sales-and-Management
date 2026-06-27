package com.example.finalyearproject.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.finalyearproject.R;
import com.example.finalyearproject.activites.MyUtils;
import com.example.finalyearproject.adapter.AdapterProperty;
import com.example.finalyearproject.databinding.FragmentFavoritListBinding;
import com.example.finalyearproject.models.ModelProperty;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FavoritListFragment extends Fragment {

    private FragmentFavoritListBinding binding;
    private static final String TAG = "FAVORITE_LIST_TAG";

    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelProperty> propertyArrayList;
    private AdapterProperty adapterProperty;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public FavoritListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFavoritListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        loadFavoriteProperties();
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Type karte hi filter call hoga
                Log.d(TAG, "onTextChanged: " + s.toString());
                try {
                    adapterProperty.getFilter().filter(s);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    private void loadFavoriteProperties() {
        // Utils se static list lein
        ArrayList<ModelProperty> allProperties = MyUtils.getDummyData();
        ArrayList<ModelProperty> favoriteList = new ArrayList<>();

        // Filter karein sirf favorite items ko
        for (ModelProperty item : allProperties) {
            if (item.isFavorite()) {
                favoriteList.add(item);
            }
        }

        adapterProperty = new AdapterProperty(requireContext(), favoriteList);
        binding.favoriteRv.setAdapter(adapterProperty);
    }
    @Override
    public void onResume() {
        super.onResume();
        // Jab bhi user is fragment par wapas aayega, list dobara refresh hogi
        loadFavoriteProperties();
    }
}


/// Is ko main real time data base main use kro ga abhi zrort nhi
//    private void loadFavortieProperties(){
//        Log.d(TAG, "loadFavortieProperties: ");
//        propertyArrayList=new ArrayList<>();
//        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
//        reference.child(firebaseAuth.getUid()).child("Favorites")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        propertyArrayList.clear();
//                        for (DataSnapshot ds : snapshot.getChildren()) {
//                            String propertyId = "" + ds.child("propertyId").getValue();
//                            DatabaseReference propertyRef = FirebaseDatabase.getInstance().getReference("Properties");
//                            propertyRef.child(propertyId)
//                                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                            try {
//                                                ModelProperty modelProperty = snapshot.getValue(ModelProperty.class);
//                                                propertyArrayList.add(modelProperty);
//                                            } catch (Exception e) {
//                                                Log.e(TAG, "onDataChange: ", e);
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError error) {
//
//                                        }
//                                    });
//                        }
//                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    adapterProperty = new AdapterProperty(mContext, propertyArrayList);
//                                    binding.favoriteRv.setAdapter(adapterProperty);
//
//                                }
//                            }, 1000);
//                        }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }
