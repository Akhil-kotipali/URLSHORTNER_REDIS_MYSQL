package com.akhil.urlShortner.services;

import com.akhil.urlShortner.dto.CreateUrlResponse;
import com.akhil.urlShortner.dto.UrlRequest;
import com.akhil.urlShortner.models.SystemSettings;
import com.akhil.urlShortner.models.Url;
import com.akhil.urlShortner.models.User;
import com.akhil.urlShortner.repositories.SystemSettingsRepository;
import com.akhil.urlShortner.repositories.UrlRepository;
import com.akhil.urlShortner.repositories.UserRepository;
import com.akhil.urlShortner.utils.UrlShortenerUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final SystemSettingsRepository systemSettingsRepository;
    @Value("${app.base-url}")
    private String baseUrl;

    public UrlService(UrlRepository urlRepository, UserRepository userRepository, SystemSettingsRepository systemSettingsRepository) {
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
        this.systemSettingsRepository=systemSettingsRepository;
    }

    /**
     * Creates a standard shortened URL for a user.
     */
  // Inject SystemSettingsRepository into UrlService constructor first!

@Transactional
public CreateUrlResponse createShortUrl(UrlRequest request, String username) {
    User user = userRepository.findByUsername(username).orElseThrow();
    SystemSettings settings = systemSettingsRepository.findById(1L).orElseThrow();

    // 1. Enforce Max Links per user
    if (urlRepository.findByUser(user).size() >= settings.getMaxLinksPerUser()) {
        throw new RuntimeException("Maximum allowed links reached for this account.");
    }

    Url url = new Url();
    url.setLongUrl(request.getLongUrl());
    url.setUser(user);
    
    // 2. Apply Global Defaults automatically
    url.setExpiresAt(LocalDateTime.now().plusHours(settings.getDefaultExpiryHours()));
    
    if (settings.getDefaultMaxTaps() > 0) {
        url.setClickLimit(settings.getDefaultMaxTaps());
    }
    
    Url savedUrl = urlRepository.save(url);
    String shortCode = UrlShortenerUtil.encode(savedUrl.getId());
    String shortUrl = baseUrl + "/" + shortCode;
    
    savedUrl.setShortCode(shortCode);
    urlRepository.save(savedUrl);
    
    return new CreateUrlResponse(shortUrl, shortCode);
}

    /**
     * Admin method for custom short codes. 
     * Uses CachePut to ensure the new custom link is immediately available in cache.
     */
    @Transactional
    @CachePut(value = "urls", key = "#customCode")
    public String createAdminLink(String customCode, String longUrl) {
        if (urlRepository.findByShortCode(customCode).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom code already in use!");
        }

        Url url = new Url();
        url.setShortCode(customCode);
        url.setLongUrl(longUrl);
        // Note: Admin links usually don't have a user or limits unless specified
        
        urlRepository.save(url);
        return longUrl;
    }

    
    @Transactional
    @Cacheable(value = "urls", key = "#code")
    public String getLongUrl(String code) {
        Url url = urlRepository.findByShortCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found"));

        // 1. Check if expired
        if (url.getExpiresAt() != null && LocalDateTime.now().isAfter(url.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "This link has expired.");
        }

        // 2. Check click limit
        if (url.getClickLimit() != null && url.getClicks() >= url.getClickLimit()) {
            throw new ResponseStatusException(HttpStatus.GONE, "This link has reached its maximum click limit.");
        }

        // 3. Increment analytics tap counter
        url.setClicks(url.getClicks() + 1);
        urlRepository.save(url);

        return url.getLongUrl();
    }

    public List<Url> getUserLinks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return urlRepository.findByUser(user);
    }
}
