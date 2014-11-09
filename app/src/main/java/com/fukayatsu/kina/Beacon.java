package com.fukayatsu.kina;

/**
 * Created by fukayatsu on 14/11/09.
 */
public class Beacon {
    private String uuid;
    private String major;
    private String minor;

    public Beacon(String uuid, String major, String minor) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMajor() {
        return major;
    }

    public String getMinor() {
        return minor;
    }
}
