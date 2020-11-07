package ca.cmpt276.project.model;

import java.time.LocalDate;

public class SurreyData {
    private LocalDate last_modified;
    private String url;

    public LocalDate getLast_modified() {
        return last_modified;
    }

    public void setLast_modified(LocalDate last_modified) {
        this.last_modified = last_modified;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
