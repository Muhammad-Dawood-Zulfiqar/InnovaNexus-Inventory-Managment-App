package com.example.pelagiahotelapp.ModelClasses;


public class Booking {
    private String bookingId;
    private String userId;
    private String hotelId;
    private String hotelName;
    private String hotelLocation;
    private String hotelImage;
    private String checkInDate;
    private String checkOutDate; // Add this field
    private String checkInTime;
    private String checkOutTime;
    private int guests;
    private int nights;
    private double totalPrice;
    private String status;
    private long bookingDate;

    // Default constructor required for Firestore
    public Booking() {}

    // Updated constructor with checkOutDate
    public Booking(String bookingId, String userId, String hotelId, String hotelName,
                   String hotelLocation, String hotelImage, String checkInDate,
                   String checkOutDate, String checkInTime, String checkOutTime,
                   int guests, int nights, double totalPrice, String status, long bookingDate) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.hotelLocation = hotelLocation;
        this.hotelImage = hotelImage;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate; // Initialize new field
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.guests = guests;
        this.nights = nights;
        this.totalPrice = totalPrice;
        this.status = status;
        this.bookingDate = bookingDate;
    }

    // Getters and setters for ALL fields
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getHotelLocation() { return hotelLocation; }
    public void setHotelLocation(String hotelLocation) { this.hotelLocation = hotelLocation; }

    public String getHotelImage() { return hotelImage; }
    public void setHotelImage(String hotelImage) { this.hotelImage = hotelImage; }

    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }

    // Add getter and setter for checkOutDate
    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }

    public int getNights() { return nights; }
    public void setNights(int nights) { this.nights = nights; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getBookingDate() { return bookingDate; }
    public void setBookingDate(long bookingDate) { this.bookingDate = bookingDate; }

    // For compatibility with existing code that uses timestamp
    public long getTimestamp() { return bookingDate; }
}
