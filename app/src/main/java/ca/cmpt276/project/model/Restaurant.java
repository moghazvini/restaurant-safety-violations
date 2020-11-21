package ca.cmpt276.project.model;

/**
 * Represents a Restaurant and contains
 * a list of inspections.
 */
public class Restaurant implements Comparable<Restaurant>{
    private String name;
    private String address;
    private String city;
    private float gpsLong;
    private float gpsLat;
    private String tracking;
    private boolean isFavourite;
    private final InspectionListManager inspections;

    public Restaurant(String tracking, String name, String address, String city, float gpsLong, float gpsLat) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.gpsLong = gpsLong;
        this.gpsLat = gpsLat;
        this.tracking = tracking;
        this.isFavourite = false;
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
}
