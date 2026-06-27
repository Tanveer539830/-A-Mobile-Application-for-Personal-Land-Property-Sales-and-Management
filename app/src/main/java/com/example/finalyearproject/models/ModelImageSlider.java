package com.example.finalyearproject.models;

public class ModelImageSlider {
    String id;
    String imageUrl;
    int imageResId; // Dummy data ke liye extra field

    public ModelImageSlider() {}

    // Updated Constructor
    public ModelImageSlider(String id, String imageUrl, int imageResId) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.imageResId = imageResId;
    }

    public int getImageResId() {
        return imageResId;
    }
    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
