package ca.cmpt276.project.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a restaurant's list of inspections.
 */
public class InspectionListManager{
    private List<Inspection> inspections;

    public InspectionListManager() {
        inspections = new ArrayList<>();
    }


    public void add(Inspection inspection) {
        inspections.add(inspection);
    }

    public List<Inspection> getInspections() {
        return inspections;
    }

    public void setInspectionsList(List<Inspection> inspectionArrayList){
        inspections = inspectionArrayList;
    }

    public Inspection getInspection(int index) {
        if (index < 0 || index >= inspections.size()) {
            return null;
        }
        return inspections.get(index);
    }


}
