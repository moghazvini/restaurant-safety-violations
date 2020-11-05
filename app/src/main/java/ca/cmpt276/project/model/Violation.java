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

    public Violation(String lump) {
        System.out.println("lump = " + lump);
        // parse the lump to extract the info
        String[] info = lump.split(",");
        code = Integer.parseInt(info[0].replace("\"",""));

        Severity severity;
        if (info[1].equals("Not Critical")) {
            severity = Severity.NOTCRITICAL;
        } else {
            severity = Severity.CRITICAL;
        }
        longDis = info[2];
        repeat = info[3];
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
                ", repeat='" + repeat + '\'' +
                '}';
    }
}
