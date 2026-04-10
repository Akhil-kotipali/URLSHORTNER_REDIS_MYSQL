package com.akhil.urlShortner.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.akhil.urlShortner.dto.CreateUrlResponse;
import com.akhil.urlShortner.models.Url;
import com.akhil.urlShortner.repositories.UrlRepository;
import com.akhil.urlShortner.utils.UrlShortenerUtil;

@Service
public class UrlService {

    private final UrlRepository urlRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public CreateUrlResponse createShortUrl(Url url) {
        // Save initially to generate the ID
        Url savedUrl = urlRepository.save(url);
        
        // Generate the short code using the ID
        String shortCode = UrlShortenerUtil.encode(savedUrl.getId());
        String shortUrl = baseUrl + "/" + shortCode;
        
        // Update the entity with the short code and save again
        savedUrl.setShortCode(shortCode);
        urlRepository.save(savedUrl);
        
        return new CreateUrlResponse(shortUrl, shortCode);
    }

    @Cacheable(value = "urls", key = "#code")
    public String getLongUrl(String code) {
        Url url = urlRepository.findByShortCode(code)
                .orElseThrow(() -> new RuntimeException("URL not found"));
                
        return url.getLongUrl();
    }


    // Admin Custom Shortening
    @CachePut(value = "admin-urls", key = "#customCode")
public String createAdminLink(String customCode, String longUrl) {
    Url url = new Url();
    url.setShortCode(customCode);
    url.setLongUrl(longUrl);
    urlRepository.save(url);
    return longUrl; // This return value is what gets stored in the cache
}

    // This handles the redirect for BOTH types
    // We check the admin cache first, then the user cache
    @Cacheable(value = "admin-urls", key = "#code")
public String getUrl(String code) {
    long start = System.currentTimeMillis();
    // This only runs if NOT in cache
    Url url = urlRepository.findByShortCode(code)
            .orElseThrow(() -> new RuntimeException("URL not found"));
    long end = System.currentTimeMillis();
    System.out.println("Time taken for " + code + ": " + (end - start) + "ms");
    return url.getLongUrl();
}
}
