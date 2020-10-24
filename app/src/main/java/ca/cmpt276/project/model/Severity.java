package ca.cmpt276.project.model;

public enum Severity {
    CRITICAL("Critical"),
    NOTCRITICAL("Not Critical");

    public final String severity;

    Severity(String severity) {
        this.severity = severity;
    }
}
