package ca.cmpt276.project.model;

/**
 * Represents the severity of a violation
 */
public enum Severity {
    CRITICAL("Critical"),
    NOTCRITICAL("Not Critical");

    public final String severity;

    Severity(String severity) {
        this.severity = severity;
    }
}
