package ca.cmpt276.project.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a restaurant's list of inspections.
 */
public class InspectionListManager {
    private final List<Inspection> inspections;

    public InspectionListManager() {
        inspections = new ArrayList<>();
    }


    public void add(Inspection inspection) {
        inspections.add(inspection);
    }

    public List<Inspection> getInspections() {
        return inspections;
    }

    public Inspection getInspection(int index) {
        if (index < 0 || index >= inspections.size()) {
            return null;
        }
        return inspections.get(index);
    }
}
