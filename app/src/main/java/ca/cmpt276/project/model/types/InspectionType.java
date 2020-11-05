package ca.cmpt276.project.model.types;

/**
 * Represents the inspection type.
 */
public enum InspectionType {
    ROUTINE("Routine Check"),
    FOLLOWUP("Follow-up Check");

    public final String value;

    InspectionType(String value) {
        this.value = value;
    }
}
