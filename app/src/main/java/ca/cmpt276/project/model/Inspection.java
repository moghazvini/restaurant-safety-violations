package ca.cmpt276.project.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.cmpt276.project.model.types.HazardLevel;
import ca.cmpt276.project.model.types.InspectionType;
import ca.cmpt276.project.model.types.Severity;

/**
 * Represents an inpsection report and
 * contains a list of violations.
 */
public class Inspection implements Comparable<Inspection>{
    private Date date;
    private InspectionType type;
    private int critical;
    private int nonCritical;
    private HazardLevel level;
    private List<Violation> violations;

    public Inspection(Date date, InspectionType type, int critical, int nonCritical, HazardLevel level, String vioLump) {
        this.date = date;
        this.type = type;
        this.critical = critical;
        this.nonCritical = nonCritical;
        this.level = level;
        violations = new ArrayList<>();
        fillViolation(vioLump);
    }

    private void fillViolation(String lump) {
        // parse the lump to extract the info
        String[] info = lump.split(",");
        int code = Integer.parseInt(info[0]);

        Severity severity;
        if (info[1].equals("Not Critical")) {
            severity = Severity.NOTCRITICAL;
        } else {
            severity = Severity.CRITICAL;
        }

        violations.add(new Violation(code,severity,info[2],info[3]));
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public InspectionType getType() {
        return type;
    }

    public void setType(InspectionType type) {
        this.type = type;
    }

    public int getCritical() {
        return critical;
    }

    public void setCritical(int critical) {
        this.critical = critical;
    }

    public int getNonCritical() {
        return nonCritical;
    }

    public void setNonCritical(int nonCritical) {
        this.nonCritical = nonCritical;
    }

    public HazardLevel getLevel() {
        return level;
    }

    public void setLevel(HazardLevel level) {
        this.level = level;
    }

    @Override
    public int compareTo(Inspection other) {
        return this.date.compareTo(other.date);
    }
}
