package com.akhil.urlShortner.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SystemSettings {
    @Id
    private Long id = 1L; // Always 1, as there is only one global settings row
    
    private int defaultExpiryHours = 1;
    private int defaultMaxTaps = 0; // 0 means unlimited
    private int maxLinksPerUser = 20;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getDefaultExpiryHours() { return defaultExpiryHours; }
    public void setDefaultExpiryHours(int defaultExpiryHours) { this.defaultExpiryHours = defaultExpiryHours; }
    public int getDefaultMaxTaps() { return defaultMaxTaps; }
    public void setDefaultMaxTaps(int defaultMaxTaps) { this.defaultMaxTaps = defaultMaxTaps; }
    public int getMaxLinksPerUser() { return maxLinksPerUser; }
    public void setMaxLinksPerUser(int maxLinksPerUser) { this.maxLinksPerUser = maxLinksPerUser; }
}