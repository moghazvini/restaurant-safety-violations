package ca.cmpt276.project.model;

import java.util.ArrayList;
import java.util.List;

public class InspectionListManager {
    private final List<Inspection> inspections;
    private static InspectionListManager instance;

    private InspectionListManager() {
        inspections = new ArrayList<>();
    }

    public static InspectionListManager getInstance() {
        if (instance == null) {
            instance = new InspectionListManager();
        }
        return instance;
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
