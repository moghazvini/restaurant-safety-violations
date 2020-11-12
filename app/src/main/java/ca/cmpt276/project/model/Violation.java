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

    public Violation(int code, Severity severity, String longDis) {
        this.code = code;
        this.severity = severity;
        this.longDis = longDis;
        getViolationType(code);
    }

    private void getViolationType(int code) {
        if ((code >= 101 && code <= 104)
                || (code >=311 && code <=312)) {
            type = ViolationType.ESTABLISHMENT;
        }
        else if ((code >= 201 && code <= 212)
                    || code == 310) {
            type = ViolationType.FOOD;
        }
        else if ((code >= 301 && code <= 303)
                || (code >= 306 && code <= 308)
                || code == 315) {
            type = ViolationType.EQUIPMENT;
        }
        else if ((code >= 304 && code <= 305)
                || code == 313) {
            type = ViolationType.PESTS;
        }
        else if (code >= 401 && code <= 404) {
            type = ViolationType.EMPLOYEES;
        }
        else if ((code >= 501 && code <= 502)
                || code == 314) {
            type = ViolationType.OPERATOR;
        } else if (code == 309) {
            type = ViolationType.CHEMICAL;
        }
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

    public ViolationType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Violation{" +
                "code=" + code +
                ", type=" + type +
                ", severity=" + severity +
                ", longDis='" + longDis + '\'' +
                '}';
    }
}
