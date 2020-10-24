package ca.cmpt276.project.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the list of restaurants that had
 * inspections.
 */
public class RestaurantListManager {
    private final List<Restaurant> restaurants;
    private static RestaurantListManager instance;

    private RestaurantListManager() {
        restaurants = new ArrayList<>();
    }

    public static RestaurantListManager getInstance() {
        if (instance == null) {
            instance = new RestaurantListManager();
        }
        return instance;
    }

    public void add (Restaurant restaurant) {
        restaurants.add(restaurant);
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
}
