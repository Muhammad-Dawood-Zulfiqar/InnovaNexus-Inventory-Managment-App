package com.example.pelagiahotelapp.ModelClasses;

import java.io.Serializable;
import java.util.List;

public class Hotel implements Serializable {
    private String id;

    private String ownerId;
    private String name;
    private String city;
    private String country;
    private String location;
    private double price;
    private double rating;
    private int beds;
    private int washrooms;
    private boolean wifi;
    private boolean gaming;
    private int totalRooms;
    private List<String> images;
    private boolean popular;
    private String category;
    private String description; // Added description field

    // Default constructor required for Firestore
    public Hotel() {}

    // Updated constructor with description

    public Hotel(String id,String ownerId, String name, String city, String country, String location,
                 double price, double rating, int beds, int washrooms, boolean wifi,
                 boolean gaming, int totalRooms, List<String> images,
                 boolean popular, String category, String description) {
        this.id = id;
        this.ownerId=ownerId;
        this.name = name;
        this.city = city;
        this.country = country;
        this.location = location;
        this.price = price;
        this.rating = rating;
        this.beds = beds;
        this.washrooms = washrooms;
        this.wifi = wifi;
        this.gaming = gaming;
        this.totalRooms = totalRooms;
        this.images = images;
        this.popular = popular;
        this.category = category;
        this.description = description;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getBeds() { return beds; }
    public void setBeds(int beds) { this.beds = beds; }

    public int getWashrooms() { return washrooms; }
    public void setWashrooms(int washrooms) { this.washrooms = washrooms; }

    public boolean isWifi() { return wifi; }
    public void setWifi(boolean wifi) { this.wifi = wifi; }

    public boolean isGaming() { return gaming; }
    public void setGaming(boolean gaming) { this.gaming = gaming; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public boolean isPopular() { return popular; }
    public void setPopular(boolean popular) { this.popular = popular; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}