package ca.cmpt276.project.model;

/**
 * Represents a Restaurant and contains
 * a list of inspections.
 */
public class Restaurant {
    private String name;
    private String address;
    private float gpsLong;
    private float gpsLat;
    private String tracking;
    private InspectionListManager inspections;

    public Restaurant(String name, String address, float gpsLong, float gpsLat, String tracking) {
        this.name = name;
        this.address = address;
        this.gpsLong = gpsLong;
        this.gpsLat = gpsLat;
        this.tracking = tracking;
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

    public void setInspections(InspectionListManager inspections) {
        this.inspections = inspections;
    }
}
