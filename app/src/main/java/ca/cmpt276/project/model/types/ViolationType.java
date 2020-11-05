package ca.cmpt276.project.model.types;

/**
 * Represents the type of Violation
 */
public enum ViolationType {
    OPERATOR("operator"),
    FOOD("food"),
    EQUIPMENT("equipment"),
    PESTS("pests"),
    EMPLOYEES("employees"),
    ESTABLISHMENT("establishment violation"),
    CHEMICAL("chemical");

    public final String violation;

    ViolationType(String violation) {
        this.violation = violation;
    }
}
