package ca.cmpt276.project.model;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.cmpt276.project.model.types.HazardLevel;
import ca.cmpt276.project.model.types.InspectionType;

/**
 * Manages the list of restaurants that had
 * inspections.
 */
public class RestaurantListManager {
    private List<Restaurant> restaurants;
    private static RestaurantListManager instance;
    private final List<Restaurant> favourited;

    private RestaurantListManager() {
        restaurants = new ArrayList<>();
        favourited = new ArrayList<>();
    }

    public static RestaurantListManager getInstance() {
        if (instance == null) {
            instance = new RestaurantListManager();
        }
        return instance;
    }

    public List<Restaurant> getList() {
        return restaurants;
    }

    public Restaurant getRestaurant(int index) {
        if (index < 0 || index >= restaurants.size()) {
            return null;
        }
        return restaurants.get(index);
    }

    public Restaurant find(String tracking) {
        for (Restaurant restaurant : restaurants) {
            if (restaurant.getTracking().equals(tracking)) {
                return restaurant;
            }
        }
        return null;
    }

    public void fillRestaurantManager(BufferedReader reader) {
        String line = "";
        try {
            reader.readLine();
            List<Restaurant> updated = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                line = line.replace("\"", "");

                boolean favourite = false;
                String[] attributes = line.split(",");
                String tracking = attributes[0];
                tracking = tracking.replace(" ", "");

                String name = attributes[1];

                int addrIndex = attributes.length - 5;
                for (int i = 2; i < addrIndex; i++) {
                    name = name.concat(attributes[i]);
                }
                String addr = attributes[addrIndex];
                String city = attributes[addrIndex + 1];
                float gpsLat = Float.parseFloat(attributes[addrIndex + 3]);
                float gpsLong = Float.parseFloat(attributes[addrIndex + 4]);

                //read data
                Restaurant restaurant = new Restaurant(
                        tracking,
                        name,
                        addr,
                        city,
                        gpsLong, // Restaurant Longitude
                        gpsLat, // Restaurant Latitude
                        favourite
                );
                updated.add(restaurant);
            }
            restaurants = updated;
            Collections.sort(restaurants);
        } catch(IOException e){
            Log.wtf("RestaurantListActivity", "error reading data file on line " + line, e);
        }
    }

    public void fillInspectionManager(BufferedReader reader) {
        String line = "";
        try {
            Restaurant restaurant = instance.getRestaurant(0);
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                line = line.replace("\"", "");
                //split by ','
                String[] tokens = line.split(",");

                //read data
                if (tokens.length > 0) {
                    // Get the restaurant that matches the tracking number of the inspection
                    String inspectionTracking = tokens[0];
                    // find the restaurant that has the same tracking number
                    if (!restaurant.getTracking().equals(inspectionTracking)) {
                        restaurant = instance.find(inspectionTracking);
                    }

                    if (restaurant != null) {
                        InspectionListManager inspectionList = restaurant.getInspections();
                        // Format string date
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                        LocalDate date = LocalDate.parse(tokens[1], formatter);

                        InspectionType type = getInspectionType(tokens[2]);
                        int numCritical = Integer.parseInt(tokens[3]);
                        int numNonCritical = Integer.parseInt(tokens[4]);
                        HazardLevel hazardLevel = getHazardLevel(tokens[tokens.length-1]);

                        Inspection inspection;
                        if (tokens.length > 5) {
                            if (tokens[5].length() > 0) {
                                String violationLump = getVioLump(tokens);
                                inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel, violationLump);
                            } else {
                                inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel);
                            }
                        } else {
                            inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel);
                        }
                        inspectionList.add(inspection);

                    } else {
                        // reset restaurant if restaurant wasn't found
                        // otherwise restaurant would be null for next iteration
                        restaurant = instance.getRestaurant(0);
                    }
                }

            }

            checkUpdatedFavourites();
        } catch(IOException e){
            Log.wtf("RestaurantListActivity", "error reading data file on line " + line, e);
        }
    }

    private void checkUpdatedFavourites() {
        for (Restaurant favourite : favourited) {
            List<Inspection> previous = favourite.getInspections().getInspections();
            List<Inspection> updated = this.find(favourite.getTracking()).getInspections().getInspections();

            if (previous.size() == updated.size()) {
                favourited.remove(favourite);
            } else {
                favourite.getInspections().setInspectionsList(updated);
            }
        }
    }

    private InspectionType getInspectionType(String type) {
        if (type.equals("Routine")) {
            return InspectionType.ROUTINE;
        } else {
            return InspectionType.FOLLOWUP;
        }
    }

    private HazardLevel getHazardLevel(String hazard) {
        if (hazard.equals("High")) {
            return HazardLevel.HIGH;
        } else if (hazard.equals("Moderate")) {
            return HazardLevel.MODERATE;
        } else {
            return HazardLevel.LOW;
        }
    }

    private String getVioLump(String[] inspectionRow) {
        StringBuilder lump = new StringBuilder();
        for (int i = 5; i < inspectionRow.length - 1; i++) {
            lump.append(inspectionRow[i]).append(",");
        }
        return lump.toString();
    }

    public List<Restaurant> getFavourited() {
        return favourited;
    }
}
