package com.example.finalyearproject.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.finalyearproject.R;
import com.example.finalyearproject.activites.LocationPickerActivity;
import com.example.finalyearproject.activites.MyUtils;
import com.example.finalyearproject.adapter.AdapterProperty;
import com.example.finalyearproject.databinding.BsFilterCategoryBinding;
import com.example.finalyearproject.databinding.FragmentHomeBinding;
import com.example.finalyearproject.models.ModelProperty;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final String TAG = "Home_TAG";

    private Context mContext;
    private ArrayList<ModelProperty> propertyArrayList;
    private AdapterProperty adapterProperty;

    // Location & SharedPrefs Variables
    private SharedPreferences locationSharedPreferences;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "";
    private String currentCity = "";

    private String filterPurpose = MyUtils.PROPERTY_PURPOSE_ANY;
    private String filterCateogry = "";
    private String filterSubcategory = "";

    private Double filterMinPrice = 0.0;
    private Double filterMaxPrice = 0.0;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI Setup
        binding.toolbarRl.setVisibility(View.VISIBLE); // Dummy mode mein location bar hide hai

        // Initial Data Load
        loadProperties();

        // Search Functionality
        setupSearch();

        // Location UI Click (Future Use)
        binding.cityTv.setOnClickListener(v -> {
            // Intent intent = new Intent(mContext, LocationPickerActivity.class);
            // locationActivityResultLauncher.launch(intent);
        });
        binding.filterTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }

    private void setupSearch() {
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (adapterProperty != null) {
                        adapterProperty.getFilter().filter(s.toString());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Search Error: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private boolean isPropertyMatchingFilter(ModelProperty modelProperty) {
        //1. Purpose Check
        boolean matchesPurpose = true;
        if (!filterPurpose.equals(MyUtils.PROPERTY_PURPOSE_ANY)) {
            matchesPurpose = modelProperty.getPurpose().equalsIgnoreCase(filterPurpose);
        }

        // 2. Category Check
        boolean matchesCategory = true;
        if (!filterCateogry.isEmpty()) {
            matchesCategory = modelProperty.getCategory().equalsIgnoreCase(filterCateogry);
        }

        // 3. Subcategory Check
        boolean matchesSubCategory = true;
        if (!filterSubcategory.isEmpty()) {
            matchesSubCategory = modelProperty.getSubcategory().equalsIgnoreCase(filterSubcategory);
        }

        // 4. Price Check (String ko Double mein convert karke)
        boolean matchesPrice = true;
        try {
            // String price ko double mein convert kar rahe hain comparison ke liye
            double propertyPrice = Double.parseDouble(modelProperty.getPrice());

            boolean matchesMin = propertyPrice >= filterMinPrice;
            boolean matchesMax = (filterMaxPrice == null || filterMaxPrice == 0 || propertyPrice <= filterMaxPrice);

            matchesPrice = matchesMin && matchesMax;
        } catch (Exception e) {
            // Agar price numeric nahi hai to filtering fail ho sakti hai
            Log.e(TAG, "isPropertyMatchingFilter: Price parsing error", e);
        }

        // Final Result: Tamam conditions ka 'True' hona zaroori hai
        return matchesPurpose && matchesCategory && matchesSubCategory && matchesPrice;
    }
private ArrayAdapter<String> stringArrayPropertyCategory;
private ArrayAdapter<String> stringArrayPropertySubCategory;


    private void loadProperties() {
        // --- OPTION 1: DUMMY DATA MODE (Current Working) ---
        loadDummyProperties();

        // --- OPTION 2: LOCATION BASED FILTERING (Commented) ---
        // loadPropertiesByDistance();

        // --- OPTION 3: REALTIME DATABASE MODE (Commented) ---
        // loadFirebaseProperties();
    }

    // ... (Upar ka code same rahega)

    private void loadDummyProperties() {
        Log.d(TAG, "loadDummyProperties: Loading Filtered Dummy Data");

        // Pehle saara data lein
        ArrayList<ModelProperty> allData = MyUtils.getDummyData();
        // Filtered items ke liye nayi list
        propertyArrayList = new ArrayList<>();

        // Har item ko check karein ke wo filter criteria par poora utarta hai?
        for (ModelProperty model : allData) {
            if (isPropertyMatchingFilter(model)) {
                propertyArrayList.add(model);
            }
        }

        binding.propertiesRv.setLayoutManager(new LinearLayoutManager(mContext));
        adapterProperty = new AdapterProperty(mContext, propertyArrayList);
        binding.propertiesRv.setAdapter(adapterProperty);

        // UI par update dikhayen ke kitni items mili hain
        if (filterCateogry.isEmpty()) {
            binding.filterSelectedTv.setText("Showing All");
        } else {
            binding.filterSelectedTv.setText("Filtered: " + filterCateogry + " (" + propertyArrayList.size() + ")");
        }
    }

    private void showFilterDialog() {
        BsFilterCategoryBinding bindingBs = BsFilterCategoryBinding.inflate(getLayoutInflater());
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
        bottomSheetDialog.setContentView(bindingBs.getRoot());

        // Existing values set karein
        if (!filterCateogry.isEmpty()) {
            bindingBs.propertyCategoryAct.setText(filterCateogry);
        }
        if (!filterSubcategory.isEmpty()) {
            bindingBs.propertySubCategoryAct.setText(filterSubcategory);
        }
        if (filterMinPrice != 0) {
            bindingBs.priceMinEt.setText("" + filterMinPrice);
        }
        if (filterMaxPrice != null && filterMaxPrice != 0) {
            bindingBs.priceMaxEt.setText("" + filterMaxPrice);
        }

        bindingBs.tabBuyTv.setOnClickListener(v -> {
            filterPurpose = MyUtils.PROPERTY_PURPOSE_SELL;
            bindingBs.tabBuyTv.setBackgroundResource(R.drawable.shape_rounded_white);
            bindingBs.tabBuyTv.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            bindingBs.tabBuyTv.setTypeface(null, Typeface.BOLD);
            bindingBs.tabRentTv.setBackground(null);
            bindingBs.tabRentTv.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            bindingBs.tabRentTv.setTypeface(null, Typeface.NORMAL);
        });

        bindingBs.tabRentTv.setOnClickListener(v -> {
            filterPurpose = MyUtils.PROPERTY_PURPOSE_RENT;
            bindingBs.tabRentTv.setBackgroundResource(R.drawable.shape_rounded_white);
            bindingBs.tabRentTv.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            bindingBs.tabRentTv.setTypeface(null, Typeface.BOLD);
            bindingBs.tabBuyTv.setBackground(null);
            bindingBs.tabBuyTv.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            bindingBs.tabBuyTv.setTypeface(null, Typeface.NORMAL);
        });

        stringArrayPropertyCategory = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, MyUtils.propertyTypes);
        bindingBs.propertyCategoryAct.setAdapter(stringArrayPropertyCategory);

        bindingBs.propertyCategoryAct.setOnItemClickListener((parent, view, position, id) -> {
            filterCateogry = (String) parent.getItemAtPosition(position);
            filterSubcategory = "";
            bindingBs.propertySubCategoryAct.setText("");
            String[] subCats;
            if (filterCateogry.equals(MyUtils.propertyTypes[0])) subCats = MyUtils.propertyTypesHomes;
            else if (filterCateogry.equals(MyUtils.propertyTypes[1])) subCats = MyUtils.propertyTypesPlots;
            else subCats = MyUtils.propertyTypesCommercial;

            stringArrayPropertySubCategory = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, subCats);
            bindingBs.propertySubCategoryAct.setAdapter(stringArrayPropertySubCategory);
        });

        bindingBs.propertySubCategoryAct.setOnItemClickListener((parent, view, position, id) -> {
            filterSubcategory = (String) parent.getItemAtPosition(position);
        });

        bindingBs.resetBtn.setOnClickListener(v -> {
            filterPurpose = MyUtils.PROPERTY_PURPOSE_ANY;
            filterCateogry = "";
            filterSubcategory = "";
            filterMinPrice = 0.0;
            filterMaxPrice = 0.0;
            bottomSheetDialog.dismiss();
            loadProperties();
        });

        bindingBs.applyBtn.setOnClickListener(v -> {
            String pMin = bindingBs.priceMinEt.getText().toString().trim();
            String pMax = bindingBs.priceMaxEt.getText().toString().trim();
            filterMinPrice = pMin.isEmpty() ? 0.0 : Double.parseDouble(pMin);
            filterMaxPrice = pMax.isEmpty() ? 0.0 : Double.parseDouble(pMax);
            bottomSheetDialog.dismiss();
            loadProperties();
        });

        bottomSheetDialog.show();
    }

    // ... (Baaki code same rahega)
    /*
    // FUTURE USE: Location based filtering logic
    private void loadPropertiesByDistance() {
        locationSharedPreferences = mContext.getSharedPreferences("Location", Context.MODE_PRIVATE);
        currentLatitude = locationSharedPreferences.getFloat("CURRENT_LATITUDE", 0.0f);
        currentLongitude = locationSharedPreferences.getFloat("CURRENT_LONGITUDE", 0.0f);

        ArrayList<ModelProperty> allData = MyUtils.getDummyData();
        propertyArrayList = new ArrayList<>();

        for (ModelProperty model : allData) {
            double distance = MyUtils.calculateDistanceKm(currentLatitude, currentLongitude, model.getLatitude(), model.getLongitude());
            if (distance <= MyUtils.MAX_DISTANCE_TO_LOAD_PROPERTIES&&isPropertyMatchingFilter(modelProperty) {
                propertyArrayList.add(modelProperty);
            }
        }
        adapterProperty = new AdapterProperty(mContext, propertyArrayList);
        binding.propertiesRv.setAdapter(adapterProperty);
    }
    */

    /*
    // FUTURE USE: Firebase Realtime Database logic
    private void loadFirebaseProperties() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                propertyArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelProperty model = ds.getValue(ModelProperty.class);
                    propertyArrayList.add(model);
                }
                adapterProperty.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase Error: ", error.toException());
            }
        });
    }
    */

    // Location Result Launcher (Commented for future use)
    /*
    private final ActivityResultLauncher<Intent> locationActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Update SharedPreferences and reload
                    loadProperties();
                }
            }
    );
    */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}