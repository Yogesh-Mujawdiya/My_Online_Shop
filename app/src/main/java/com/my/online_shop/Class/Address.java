package com.my.online_shop.Class;

public class Address {
    public String Longitude, Latitude,LongLabel, City, SubRegion, Region, CountryCode;

    public Address(){

    }
    public Address(String longitude, String latitude, String longLabel, String city, String subRegion, String region, String countryCode) {
        Longitude = longitude;
        Latitude = latitude;
        LongLabel = longLabel;
        City = city;
        SubRegion = subRegion;
        Region = region;
        CountryCode = countryCode;
    }
}
