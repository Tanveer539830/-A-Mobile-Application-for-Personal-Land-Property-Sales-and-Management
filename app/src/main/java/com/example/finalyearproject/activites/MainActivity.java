package com.example.finalyearproject.activites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.example.finalyearproject.databinding.ActivityMainBinding;
import com.example.finalyearproject.fragment.ChatListFragment;
import com.example.finalyearproject.fragment.FavoritListFragment;
import com.example.finalyearproject.fragment.HomeFragment;
import com.example.finalyearproject.fragment.ProfileFragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "MAIN_ACTIVITY_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            startLoginOptionActivity();
            return;
        }

        // --- DRAWER HEADER DATA LOADING START ---

        // 1. NavigationView se Header View ko access karein
        View headerView = binding.navigationView.getHeaderView(0);
        TextView headerNameTv = headerView.findViewById(R.id.headerNameTv);
        TextView headerEmailTv = headerView.findViewById(R.id.headerEmailTv);
        ShapeableImageView headerProfileIv = headerView.findViewById(R.id.headerProfileIv);

        // 2. PEHLE DUMMY DATA SET KAREIN (Taka UI khali na lage)
        headerNameTv.setText("Tanveer Hussain"); // Default Name
        if (firebaseAuth.getCurrentUser() != null) {
            headerEmailTv.setText(firebaseAuth.getCurrentUser().getEmail()); // Auth se direct email
        }

        // 3. PHIR FIREBASE SE REAL DATA LOAD KAREIN (Background mein update ho jayega)
        String uid = firebaseAuth.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).addValueEventListener(new ValueEventListener() {
            // Firebase se data load karne wala part (Step 3 ka onDataChange)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = "" + snapshot.child("name").getValue();
                String email = "" + snapshot.child("email").getValue();
                String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();

                // Text update karein
                if (!name.equals("null") && !name.isEmpty()) headerNameTv.setText(name);
                if (!email.equals("null") && !email.isEmpty()) headerEmailTv.setText(email);

                // IMAGE FIX: Supervisor ko dikhane ke liye permanent image
                // Agar internet se image nahi milti to tanveer_image hi rahegi
                Glide.with(MainActivity.this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.tanveer_image) // Load hote waqt
                        .error(R.drawable.tanveer_image)       // Error aaye to bhi aapki pic
                        .fallback(R.drawable.tanveer_image)    // Kuch na mile to bhi aapki pic
                        .circleCrop()                          // Gol image (Professional look)
                        .into(headerProfileIv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
        // --- DRAWER HEADER DATA LOADING END ---


        // --- DRAWER LOGIC START ---

        // Hamburger Menu Button Click (Toolbar wala button)
        binding.menuBtn.setOnClickListener(v -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });

        // Navigation View (Drawer) Items Click Listener
        // MainActivity.java ke andar NavigationItemSelectedListener mein ye tabdeeli karein:

        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showHomeFragment();
                binding.bottomNavigationView.setSelectedItemId(R.id.itemHome);
            }
            else if (id == R.id.nav_profile) {
                showProfileFragment();
                binding.bottomNavigationView.setSelectedItemId(R.id.item_profile);
            }
            else if (id == R.id.nav_my_ads) {
                // --- CHANGE HERE: Fragment ke bajaye Activity open karein ---
                Intent intent = new Intent(MainActivity.this, MyPropertyListActivity.class);
                startActivity(intent);
            }
            else if (id == R.id.nav_chat) {
                showChatListFragment();
                binding.bottomNavigationView.setSelectedItemId(R.id.item_chats);
            }
            else if (id == R.id.nav_fav) {
                showFavoriteFragment();
                binding.bottomNavigationView.setSelectedItemId(R.id.item_favorite);
            }
            else if (id == R.id.nav_logout) {
                firebaseAuth.signOut();
                startLoginOptionActivity();
            }

            // Drawer ko hamesha band karein
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // --- DRAWER LOGIC END ---

        showHomeFragment();

        // Bottom Navigation Setup
        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.itemHome) {
                    showHomeFragment();
                    return true;
                } else if (itemId == R.id.item_chats) {
                    showChatListFragment();
                    return true;
                } else if (itemId == R.id.item_favorite) {
                    showFavoriteFragment();
                    return true;
                } else if (itemId == R.id.item_profile) {
                    showProfileFragment();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void showHomeFragment() {
        binding.toolbarTitleTv.setText("Home");
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), homeFragment, "HomeFragment");
        fragmentTransaction.commit();
    }

    private void showChatListFragment() {
        binding.toolbarTitleTv.setText("Chats");
        ChatListFragment chatListFragment = new ChatListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), chatListFragment, "ChatFragment");
        fragmentTransaction.commit();
    }

    private void showFavoriteFragment() {
        binding.toolbarTitleTv.setText("Favorite");
        FavoritListFragment favoritListFragment = new FavoritListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), favoritListFragment, "Favorite");
        fragmentTransaction.commit();
    }

    private void showProfileFragment() {
        binding.toolbarTitleTv.setText("Profile");
        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), profileFragment, "Profile");
        fragmentTransaction.commit();
    }

    private void startLoginOptionActivity() {
        Intent intent = new Intent(this, LoginOptionsActivity.class);
        startActivity(intent);
        // Animation add karein professional feel ke liye
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}