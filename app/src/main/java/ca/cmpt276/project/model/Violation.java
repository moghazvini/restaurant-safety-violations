package ca.cmpt276.project.model;

import ca.cmpt276.project.model.types.Severity;
import ca.cmpt276.project.model.types.ViolationType;

/**
 * Represents a violation.
 */
public class Violation {
    private int code;
    private ViolationType type;
    private Severity severity;
    private String longDis;
    private String repeat;

    public Violation(int code, Severity severity, String longDis, String repeat) {
        this.code = code;
        this.severity = severity;
        this.longDis = longDis;
        this.repeat = repeat;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getLongDis() {
        return longDis;
    }

    public void setLongDis(String longDis) {
        this.longDis = longDis;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }
}
