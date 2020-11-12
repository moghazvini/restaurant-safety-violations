package ca.cmpt276.project.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LastModified {
    private static final String LAST_CHECKED = "last checked";
    private static final String LAST_MODIFIED_REST = "last modified restaurant list";
    private static final String LAST_MODIFIED_INSP = "last modified inspection list";
    private final LocalDateTime lastCheck;
    private final LocalDateTime last_mod_restaurants;
    private final LocalDateTime last_mod_inspections;

    // Singleton Support
    private static LastModified instance;

    private LastModified(Context context) {
        lastCheck = getLastUpdate(context, getDefaultTime(LocalDateTime.now().minusDays(1)), LAST_CHECKED);
        last_mod_restaurants = getLastUpdate(context, 0, LAST_MODIFIED_REST);
        last_mod_inspections = getLastUpdate(context, 0, LAST_MODIFIED_INSP);
    }

    public static LastModified getInstance(Context context) {
        if (instance == null) {
            instance = new LastModified(context);
        }
        return instance;
    }

    // Shared Preferences to store last time checked
    private LocalDateTime getLastUpdate(Context context, long defaultTime, String modified) {
        long lastUpdated = readLastUpdated(context, defaultTime, modified);

        // Update
        writeLastUpdated(context, modified);

        // convert long to LocalTimeDate
        if (lastUpdated == 0) {
            return LocalDateTime.MIN;
        }
        return Instant.ofEpochMilli(lastUpdated).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static Long readLastUpdated(Context context, long defaultTime, String modified) {
        SharedPreferences stored = context.getSharedPreferences("AppPrefs",Context.MODE_PRIVATE);
        return stored.getLong(modified, defaultTime);
    }

    private void writeLastUpdated(Context context, String modified) {
        SharedPreferences stored = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = stored.edit();

        long save = convertToLong(LocalDateTime.now());

        editor.putLong(modified, save);
        editor.apply();
    }

    private long convertToLong(LocalDateTime date) {
        ZonedDateTime zdt = ZonedDateTime.of(date, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    // https://www.javaguides.net/2020/03/convert-localdatetime-to-long-in-java.html
    // Convert LocalDateTime to a Long
    private long getDefaultTime(LocalDateTime time) {
        ZonedDateTime zdt = ZonedDateTime.of(time, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    public LocalDateTime getLastCheck() {
        return lastCheck;
    }

    public LocalDateTime getLast_mod_restaurants() {
        return last_mod_restaurants;
    }

    public LocalDateTime getLast_mod_inspections() {
        return last_mod_inspections;
    }
}
