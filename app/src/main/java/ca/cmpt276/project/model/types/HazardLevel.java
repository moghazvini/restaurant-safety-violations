package ca.cmpt276.project.model.types;

/**
 * Represents the Hazard level of an Inspection
 */
public enum HazardLevel {
    NONE("None"),
    LOW("Low"),
    MODERATE("Moderate"),
    HIGH("High");

    public final String level;

    HazardLevel(String level) {
        this.level = level;
    }
}
