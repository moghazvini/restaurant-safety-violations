package ca.cmpt276.project.model;

import java.time.LocalDateTime;

/**
 * Contains the information needed to download the CSV file and
 * its modification date.
 */
public class CsvInfo {
    private LocalDateTime last_modified;
    private String url;
    private Boolean changed;

    public LocalDateTime getLast_modified() {
        return last_modified;
    }

    public void setLast_modified(LocalDateTime last_modified) {
        this.last_modified = last_modified;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getChanged() {
        return changed;
    }

    public void setChanged(Boolean changed) {
        this.changed = changed;
    }

    @Override
    public String toString() {
        return "CsvInfo{" +
                "last_modified=" + last_modified +
                ", url='" + url + '\'' +
                ", changed=" + changed +
                '}';
    }
}
