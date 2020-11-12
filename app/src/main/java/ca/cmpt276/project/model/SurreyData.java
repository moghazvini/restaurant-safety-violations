package ca.cmpt276.project.model;

import java.time.LocalDateTime;

public class SurreyData {
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
}
