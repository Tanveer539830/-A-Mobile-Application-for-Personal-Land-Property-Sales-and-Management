package com.example.finalyearproject.models;

public class ModelProperty {
    String id;
    String uid;
    String purpose;
    String category;
    String subcategory;
    String floors;
    String bedrooms;
    String bathrooms;
    String areaSize;
    String areaSizeUnit;
    String price;
    String title;
    String description;
    String email;
    String phoneCode;
    String phoneNumber;
    String country;
    String city;
    String state;
    String address;
    Double latitude;
    Double longitude;
    String status;
    long timestamp;
    // Dummy images ke liye ye field zaroori hai
    int imageResId;
    boolean favorite;


    public ModelProperty() {
        // Required empty public constructor
    }

    // Updated Constructor including imageResId
    public ModelProperty(String id, String uid, String purpose, String category, String subcategory, String floors, String bedrooms, String bathrooms, String areaSize, String areaSizeUnit, String price, String title, String description, String email, String phoneCode, String phoneNumber, String country, String city, String state, String address, Double latitude, Double longitude, String status, long timestamp, int imageResId,boolean favorite) {
        this.id = id;
        this.uid = uid;
        this.purpose = purpose;
        this.category = category;
        this.subcategory = subcategory;
        this.floors = floors;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.areaSize = areaSize;
        this.areaSizeUnit = areaSizeUnit;
        this.price = price;
        this.title = title;
        this.description = description;
        this.email = email;
        this.phoneCode = phoneCode;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.city = city;
        this.state = state;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.timestamp = timestamp;
        this.imageResId = imageResId;
        this.favorite=favorite;
    }

    // Getter and Setter for imageResId
    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    // ... Baaki getters aur setters pehle jaise hi rahen ge ...
    // (Inhein delete mat kariyega)

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFloors() {
        return floors;
    }

    public void setFloors(String floors) {
        this.floors = floors;
    }

    public String getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(String bedrooms) {
        this.bedrooms = bedrooms;
    }

    public String getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(String bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(String areaSize) {
        this.areaSize = areaSize;
    }

    public String getAreaSizeUnit() {
        return areaSizeUnit;
    }

    public void setAreaSizeUnit(String areaSizeUnit) {
        this.areaSizeUnit = areaSizeUnit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}