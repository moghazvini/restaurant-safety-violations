package ca.cmpt276.project.model.types;

/**
 * Represents the type of Violation
 */
public enum ViolationType {
    OPERATOR("operator violation"),
    FOOD("food violation"),
    EQUIPMENT("equipment violation"),
    PESTS("pest violation"),
    EMPLOYEES("employee violation"),
    ESTABLISHMENT("establishment violation"),
    CHEMICAL("chemical violation");

    public final String violation;

    ViolationType(String violation) {
        this.violation = violation;
    }
}
