package com.example.finalyearproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.VideoView;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.example.finalyearproject.activites.MyUtils;
import com.example.finalyearproject.activites.PropertyDetailsActivity;
import com.example.finalyearproject.databinding.RowPropertyBinding;
import com.example.finalyearproject.models.ModelProperty;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

// RecyclerView.ViewHolder generic type use karenge taake multi-holders handle ho saken
public class AdapterProperty extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final String TAG = "PROPERTY_TAG";
    private Context context;
    private ArrayList<ModelProperty> propertyArrayList;
    private ArrayList<ModelProperty> filterList;
    private FirebaseAuth firebaseAuth;

    // Multi-View Types constants
    private static final int VIEW_TYPE_PROPERTY = 0;
    private static final int VIEW_TYPE_AD = 1;
    private static final int AD_INTERVAL = 5; // Har 5 items ke baad ek Ad

    public AdapterProperty(Context context, ArrayList<ModelProperty> propertyArrayList) {
        this.context = context;
        this.propertyArrayList = new ArrayList<>(propertyArrayList);
        this.filterList = new ArrayList<>(propertyArrayList);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public int getItemViewType(int position) {
        // Har 6th position (0,1,2,3,4 -> Property, 5 -> Ad) par Ad dikhao
        if ((position + 1) % (AD_INTERVAL + 1) == 0) {
            return VIEW_TYPE_AD;
        }
        return VIEW_TYPE_PROPERTY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_AD) {
            // Ad layout inflate karein
            View view = LayoutInflater.from(context).inflate(R.layout.property_ads_ui_design, parent, false);
            return new HolderAd(view);
        } else {
            // Property layout inflate karein
            RowPropertyBinding rowBinding = RowPropertyBinding.inflate(LayoutInflater.from(context), parent, false);
            return new HolderProperty(rowBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_PROPERTY) {

            // Property Index calculate karein (Kyunki list mein sirf properties hain ads nahi)
            int propertyIndex = position - (position / (AD_INTERVAL + 1));
            ModelProperty modelProperty = propertyArrayList.get(propertyIndex);

            HolderProperty propertyHolder = (HolderProperty) holder;

            // Data setting logic
            propertyHolder.titleTv.setText(modelProperty.getTitle());
            propertyHolder.descriptionTv.setText(modelProperty.getDescription());
            propertyHolder.purposeTv.setText(modelProperty.getPurpose());
            propertyHolder.categoryTv.setText(modelProperty.getCategory());
            propertyHolder.subcategoryTv.setText(modelProperty.getSubcategory());
            propertyHolder.addressTv.setText(modelProperty.getAddress());

            String formattedPrice = MyUtils.formateCurrency(Double.parseDouble(modelProperty.getPrice()));
            propertyHolder.priceTv.setText(formattedPrice);
            propertyHolder.dateTv.setText(MyUtils.formatTimestamp(modelProperty.getTimestamp()));

            loadPropertyFirstImage(modelProperty, propertyHolder);

            // Click Listeners
            propertyHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PropertyDetailsActivity.class);
                intent.putExtra("propertyId", modelProperty.getId());
                context.startActivity(intent);
            });

            propertyHolder.favouriteBtn.setOnClickListener(v -> {
                boolean isNowFavorite = !modelProperty.isFavorite();
                modelProperty.setFavorite(isNowFavorite);
                propertyHolder.favouriteBtn.setImageResource(isNowFavorite ? R.drawable.fav_yes_black : R.drawable.fav_no_black);
                MyUtils.toast(context, isNowFavorite ? "Added to Favorites" : "Removed from Favorites");
            });

        } else {
            // Ad logic: Video play karein
            HolderAd adHolder = (HolderAd) holder;
            adHolder.playAdVideo();
        }
    }

    @Override
    public int getItemCount() {
        if (propertyArrayList.isEmpty()) return 0;
        // Total items = Original List + Jitne Ads fit aate hain
        return propertyArrayList.size() + (propertyArrayList.size() / AD_INTERVAL);
    }
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof HolderAd) {
            HolderAd adHolder = (HolderAd) holder;
            // Scroll karte waqt memory bachane ke liye video stop karein
            adHolder.videoView.stopPlayback();
            Log.d(TAG, "onViewRecycled: Ad Video Stopped");
        }
    }
    private void loadPropertyFirstImage(ModelProperty modelProperty, HolderProperty holder) {
        try {
            // Dummy array ko khatam karein aur direct modelProperty se image lein
            // ModelProperty mein jo image pass ki gayi hai MyUtils se, wo use hogi
            int imageResId = modelProperty.getImageResId();

            Glide.with(context)
                    .load(imageResId) // MyUtils wala image yahan load hoga
                    .placeholder(R.drawable.building_asset01)
                    .error(R.drawable.building_asset01) // Agar image na mile toh default
                    .into(holder.propertyIv);

        } catch (Exception e) {
            Log.e(TAG, "Image Load Error: ", e);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    constraint = constraint.toString().toLowerCase();
                    ArrayList<ModelProperty> filteredModels = new ArrayList<>();
                    for (ModelProperty model : filterList) {
                        if (model.getTitle().toLowerCase().contains(constraint) ||
                                model.getAddress().toLowerCase().contains(constraint)) {
                            filteredModels.add(model);
                        }
                    }
                    results.count = filteredModels.size();
                    results.values = filteredModels;
                } else {
                    results.count = filterList.size();
                    results.values = filterList;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                propertyArrayList = (ArrayList<ModelProperty>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // --- HOLDERS ---

    class HolderProperty extends RecyclerView.ViewHolder {
        ShapeableImageView propertyIv;
        TextView titleTv, descriptionTv, purposeTv, categoryTv, subcategoryTv, addressTv, dateTv, priceTv;
        FloatingActionButton favouriteBtn;

        public HolderProperty(@NonNull RowPropertyBinding binding) {
            super(binding.getRoot());
            propertyIv = binding.propertyIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            purposeTv = binding.purposeTv;
            categoryTv = binding.categoryTv;
            subcategoryTv = binding.subcategoryTv;
            addressTv = binding.addressTv;
            dateTv = binding.dateTv;
            priceTv = binding.priceTv;
            favouriteBtn = binding.favouriteBtn;
        }
    }

    class HolderAd extends RecyclerView.ViewHolder {
        VideoView videoView;

        public HolderAd(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.adsvideoView);
        }

        void playAdVideo() {
            String videoPath = "android.resource://" + context.getPackageName() + "/" + R.raw.video1;
            Uri uri = Uri.parse(videoPath);
            videoView.setVideoURI(uri);

            videoView.setOnPreparedListener(mp -> {
                // --- Full View Logic START ---
                // Video ki asli dimensions lein
                float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                // VideoView (Container) ki dimensions lein
                float screenRatio = videoView.getWidth() / (float) videoView.getHeight();

                float scale = videoRatio / screenRatio;
                if (scale >= 1f) {
                    videoView.setScaleX(scale);
                } else {
                    videoView.setScaleY(1f / scale);
                }
                // --- Full View Logic END ---

                mp.setLooping(true);
                videoView.start();
            });

            videoView.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Ad Video Error: " + what);
                return true;
            });
        }
    }
}