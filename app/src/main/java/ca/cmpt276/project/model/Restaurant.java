package ca.cmpt276.project.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a Restaurant and contains
 * a list of inspections.
 */
public class Restaurant implements Comparable<Restaurant>, Parcelable {
    private String name;
    private String address;
    private String city;
    private float gpsLong;
    private float gpsLat;
    private String tracking;
    private boolean isFavourite;
    private InspectionListManager inspections;

    public Restaurant(String tracking, String name, String address, String city, float gpsLong, float gpsLat, boolean isFavourite) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.gpsLong = gpsLong;
        this.gpsLat = gpsLat;
        this.tracking = tracking;
        this.isFavourite = isFavourite;
        this.inspections = new InspectionListManager();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public float getGpsLong() {
        return gpsLong;
    }

    public void setGpsLong(float gpsLong) {
        this.gpsLong = gpsLong;
    }

    public float getGpsLat() {
        return gpsLat;
    }

    public void setGpsLat(float gpsLat) {
        this.gpsLat = gpsLat;
    }

    public String getTracking() {
        return tracking;
    }

    public void setTracking(String tracking) {
        this.tracking = tracking;
    }

    public InspectionListManager getInspections() {
        return inspections;
    }

    public void setInspections(InspectionListManager inspectionListManager){
        inspections = inspectionListManager;
    }

    public String getGPS(){
        return "Longitude: " + this.getGpsLong() + ", Latitude: " + this.getGpsLat();
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    @Override
    public int compareTo(Restaurant other) {
        return this.name.compareTo(other.name);
    }

    protected Restaurant(Parcel in) {

        name = in.readString();
        address = in.readString();
        city = in.readString();
        gpsLong = in.readFloat();
        gpsLat = in.readFloat();
        tracking = in.readString();
        isFavourite = in.readByte() != 0;
    }

    public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeFloat(gpsLong);
        dest.writeFloat(gpsLat);
        dest.writeString(tracking);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
