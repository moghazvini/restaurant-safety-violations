package ca.cmpt276.project.model.types;

/**
 * Represents the severity of a violation
 */
public enum Severity {
    CRITICAL("Critical"),
    NOTCRITICAL("Non-Critical");

    public final String severity;

    Severity(String severity) {
        this.severity = severity;
    }
}
