package ca.cmpt276.project.model.types;

/**
 * Represents the inspection type.
 */
public enum InspectionType {
    ROUTINE("routine"),
    FOLLOWUP("follow-up");

    public final String value;

    InspectionType(String value) {
        this.value = value;
    }
}
