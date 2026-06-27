package com.example.finalyearproject.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.example.finalyearproject.databinding.RowImageSliderBinding;
import com.example.finalyearproject.databinding.RowImagesPickedBinding;
import com.example.finalyearproject.models.ModelImageSlider;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class AdapterImageSlider extends RecyclerView.Adapter<AdapterImageSlider.HolerImageSlider>{
    private RowImageSliderBinding binding;
    private static final String TAG="ADAPTER_IMAGE_SLIDER_TAG";
    private Context context;
    private ArrayList<ModelImageSlider> imageSliderArrayList;

    public AdapterImageSlider(Context context, ArrayList<ModelImageSlider> imageSliderArrayList) {
        this.context = context;
        this.imageSliderArrayList = imageSliderArrayList;
    }

    @NonNull
    @Override
    public HolerImageSlider onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Local binding banayein
        RowImageSliderBinding rowBinding = RowImageSliderBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolerImageSlider(rowBinding);
    }


    @Override
    public void onBindViewHolder(@NonNull HolerImageSlider holder, int position) {
        ModelImageSlider modelImageSlider = imageSliderArrayList.get(position);

        // Dummy Data ke liye drawable load karein
        try {
            Glide.with(context)
                    .load(modelImageSlider.getImageResId()) // Drawable resource ID use karein
                    .placeholder(R.drawable.image_gray)
                    .into(holder.imageIv);
        } catch (Exception e) {
            Log.e("TAG", "onBindViewHolder: ", e);
        }

        String imageCount = (position + 1) + "/" + imageSliderArrayList.size();
        holder.imageCountTv.setText(imageCount);
    }

    @Override
    public int getItemCount() {
        return imageSliderArrayList.size();
    }

    class HolerImageSlider extends RecyclerView.ViewHolder{
        ShapeableImageView imageIv;
        TextView imageCountTv;

        public HolerImageSlider(@NonNull RowImageSliderBinding binding) { // Parameter change karein
            super(binding.getRoot());
            imageIv = binding.imageIv;
            imageCountTv = binding.imageCountTv;
        }
    }
}
