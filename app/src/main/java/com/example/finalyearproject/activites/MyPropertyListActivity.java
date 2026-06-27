package com.example.finalyearproject.activites;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.finalyearproject.R;
import com.example.finalyearproject.adapter.AdapterProperty;
import com.example.finalyearproject.databinding.ActivityMyPropertyListBinding;
import com.example.finalyearproject.models.ModelProperty;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyPropertyListActivity extends AppCompatActivity {
    private ActivityMyPropertyListBinding binding;

    private static final String TAG = "MY_PROPERTY_LIST_TAG";
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelProperty> propertyArrayList;
    private AdapterProperty adapterProperty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_property_list);
        binding = ActivityMyPropertyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyProperties();
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
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadMyProperties() {
        Log.d(TAG, "loadMyProperties: Loading Dummy Data for Demo");

        propertyArrayList = new ArrayList<>();
        ArrayList<ModelProperty> allData = MyUtils.getDummyData();

        // dikhane ke liye: Maan lijiye user ki apni properties
        // pehli 3 properties hain (Index 0, 1, 2)
        for (int i = 0; i < 5; i++) {
            propertyArrayList.add(allData.get(i));
        }

        adapterProperty = new AdapterProperty(this, propertyArrayList);
        binding.propertiesRv.setAdapter(adapterProperty);
    }
}
/// Is ko main real time data base main use kro ga abhi zrort nhi

//    private void loadMyProperties(){
//        propertyArrayList = new ArrayList<>();
//
//        String myUid=""+firebaseAuth.getUid();
//        Log.d(TAG, "loadMyProperties: myUid: "+myUid);
//        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Properties");
//        ref.orderByChild("uid").equalTo(myUid)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        propertyArrayList.clear();
//                        for (DataSnapshot ds:snapshot.getChildren()) {
//                            try{
//                                ModelProperty modelProperty=ds.getValue(ModelProperty.class);
//                                propertyArrayList.add(modelProperty);
//                            }catch (Exception e){
//                                Log.e(TAG, "onDataChange: ",e);
//                            }
//
//                        }
//                        adapterProperty=new AdapterProperty(MyPropertyListActivity.this,propertyArrayList);
//                        binding.propertiesRv.setAdapter(adapterProperty);
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                    }
//                });

