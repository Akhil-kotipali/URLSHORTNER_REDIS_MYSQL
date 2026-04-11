package com.akhil.urlShortner.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class UrlRequest {
    
    @NotBlank(message = "URL cannot be empty")
    @URL(message = "Please provide a valid URL (e.g., https://google.com)")
    private String longUrl;

    private Integer expirationHours;
    private Integer clickLimit;

    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }
    public Integer getExpirationHours() { return expirationHours; }
    public void setExpirationHours(Integer expirationHours) { this.expirationHours = expirationHours; }
    public Integer getClickLimit() { return clickLimit; }
    public void setClickLimit(Integer clickLimit) { this.clickLimit = clickLimit; }
}