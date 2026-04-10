package com.akhil.urlShortner.dto;

public class CreateUrlResponse {
    private String shortUrl;
    private String code;

    public CreateUrlResponse(String shortUrl, String code) {
        this.shortUrl = shortUrl;
        this.code = code;
    }

    public String getShortUrl() { return shortUrl; }
    public String getCode() { return code; }
}
