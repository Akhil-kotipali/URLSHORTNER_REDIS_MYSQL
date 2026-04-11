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

    public UrlService(UrlRepository urlRepository, 
                      UserRepository userRepository, 
                      SystemSettingsRepository systemSettingsRepository) {
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
        this.systemSettingsRepository = systemSettingsRepository;
    }

    @Transactional
    public CreateUrlResponse createShortUrl(UrlRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        SystemSettings settings = systemSettingsRepository.findById(1L).orElse(new SystemSettings());

        // 1. Enforce Max Links per user
        if (urlRepository.findByUser(user).size() >= settings.getMaxLinksPerUser()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum allowed links reached for this account.");
        }

        Url url = new Url();
        url.setLongUrl(request.getLongUrl());
        url.setUser(user);
        
        // 2. Apply Custom Limits if provided, otherwise they remain null to favor Global Defaults during redirection
        if (request.getClickLimit() != null && request.getClickLimit() > 0) {
            url.setClickLimit(request.getClickLimit());
        }
        if (request.getExpirationHours() != null && request.getExpirationHours() > 0) {
            url.setExpiresAt(LocalDateTime.now().plusHours(request.getExpirationHours()));
        }
        
        // Initial save to generate ID
        Url savedUrl = urlRepository.save(url);
        String shortCode = UrlShortenerUtil.encode(savedUrl.getId());
        
        savedUrl.setShortCode(shortCode);
        urlRepository.save(savedUrl);
        
        return new CreateUrlResponse(baseUrl + "/" + shortCode, shortCode);
    }

    @Transactional
    @Cacheable(value = "urls", key = "#code")
    public String getLongUrl(String code) {
        Url url = urlRepository.findByShortCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found"));

        SystemSettings settings = systemSettingsRepository.findById(1L).orElse(new SystemSettings());

        // DYNAMIC EVALUATION: Use custom limit if set, otherwise fallback to system default
        LocalDateTime activeExpiry = (url.getExpiresAt() != null) ? url.getExpiresAt() : 
                                     url.getCreatedAt().plusHours(settings.getDefaultExpiryHours());
        
        int activeLimit = (url.getClickLimit() != null) ? url.getClickLimit() : settings.getDefaultMaxTaps();

        // 1. Check Expiry
        if (LocalDateTime.now().isAfter(activeExpiry)) {
            throw new ResponseStatusException(HttpStatus.GONE, "This link has expired.");
        }

        // 2. Check Click Limit (only if activeLimit > 0)
        if (activeLimit > 0 && url.getClicks() >= activeLimit) {
            throw new ResponseStatusException(HttpStatus.GONE, "This link has reached its maximum click limit.");
        }

        // 3. Increment analytics
        url.setClicks(url.getClicks() + 1);
        urlRepository.save(url);

        return url.getLongUrl();
    }

    @Transactional
    @CachePut(value = "urls", key = "#customCode")
    public String createAdminLink(String customCode, String longUrl) {
        if (urlRepository.findByShortCode(customCode).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom code already in use!");
        }

        Url url = new Url();
        url.setShortCode(customCode);
        url.setLongUrl(longUrl);
        
        urlRepository.save(url);
        return longUrl; 
    }

    public List<Url> getUserLinks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return urlRepository.findByUser(user);
    }
}
