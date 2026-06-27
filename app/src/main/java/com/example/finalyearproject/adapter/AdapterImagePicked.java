package com.example.finalyearproject.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.example.finalyearproject.activites.MyUtils;
import com.example.finalyearproject.databinding.RowImagesPickedBinding;
import com.example.finalyearproject.models.ModelImagePicked;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterImagePicked extends RecyclerView.Adapter<AdapterImagePicked.HolderImagePicked> {
    private RowImagesPickedBinding binding;
    private static final String TAG="IMAGE_TAG";
    private Context context;

    private ArrayList<ModelImagePicked> imagePickedArrayList;

    private String propertyId;

    public AdapterImagePicked(Context context, ArrayList<ModelImagePicked> imagePickedArrayList,String propertyId) {
        this.context = context;
        this.imagePickedArrayList = imagePickedArrayList;
        this.propertyId=propertyId;
    }

    @NonNull
    @Override
    public HolderImagePicked onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=RowImagesPickedBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderImagePicked(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderImagePicked holder, int position) {


        ModelImagePicked modelImagePicked=imagePickedArrayList.get(position);

        if(modelImagePicked.isFromInternet()){
            String imageUrl=""+modelImagePicked.getImageUrl();
            Log.d(TAG, "onBindViewHolder: imageUrl"+imageUrl);

            try {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_gray)
                        .into(holder.imageView);
            }catch (Exception e){
                Log.e(TAG, "onBindViewHolder: ", e);
            }

        }else {

            try {
                Uri imageUri=modelImagePicked.getImageUri();

                Glide.with(context)
                        .load(imageUri)
                        .placeholder(R.drawable.image_gray)
                        .into(holder.imageView);
            }catch (Exception e){
                Log.e(TAG, "onBindViewHolder: ",e);
            }

        }




        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(modelImagePicked.isFromInternet()){
                    deleteImageFirebase(modelImagePicked,position);

                }else {
                    imagePickedArrayList.remove(modelImagePicked);
                    notifyItemRemoved(position);
                }

            }
        });


    }
    // Is method mein error handling mazeed behtar ki hai
    private void deleteImageFirebase(ModelImagePicked modelImagePicked, int position) {
        if (propertyId == null || propertyId.isEmpty()) {
            imagePickedArrayList.remove(position);
            notifyItemRemoved(position);
            return;
        }

        Log.d(TAG, "deleteImageFirebase: Deleting from Database node");
        String imageId = modelImagePicked.getId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.child(propertyId).child("Images").child(imageId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    MyUtils.toast(context, "Image deleted");
                    imagePickedArrayList.remove(position);
                    notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    MyUtils.toast(context, "Delete failed: " + e.getMessage());
                });
    }

    @Override
    public int getItemCount() {
        return imagePickedArrayList.size();
    }

    class HolderImagePicked extends RecyclerView.ViewHolder{

        ImageView imageView;
        ImageButton closeBtn;
        public HolderImagePicked(@NonNull View itemView) {
            super(itemView);
            imageView=binding.imageIv;
            closeBtn=binding.closeBtn;

        }
    }
}
