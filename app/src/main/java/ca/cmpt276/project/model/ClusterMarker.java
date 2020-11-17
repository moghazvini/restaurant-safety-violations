package ca.cmpt276.project.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private int Icon;
    private Restaurant Rest;

    public ClusterMarker(LatLng position, String title, String snippet, int icon, Restaurant rest) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        Icon = icon;
        Rest = rest;
    }


    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public Restaurant getRest() {
        return Rest;
    }

    public void setRest(Restaurant rest) {
        Rest = rest;
    }

    public int getIcon() {
        return Icon;
    }

    public void setIcon(int icon) {
        this.Icon = icon;
    }
}
