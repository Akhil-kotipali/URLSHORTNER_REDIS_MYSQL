package com.akhil.urlShortner.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.akhil.urlShortner.dto.CreateUrlResponse;
import com.akhil.urlShortner.models.Url;
import com.akhil.urlShortner.models.User;
import com.akhil.urlShortner.repositories.UrlRepository;
import com.akhil.urlShortner.repositories.UserRepository;
import com.akhil.urlShortner.utils.UrlShortenerUtil;

@Service
public class UrlService {

// Add UserRepository to the constructor in UrlService
private final UrlRepository urlRepository;
private final UserRepository userRepository; // ADD THIS

@Value("${app.base-url}")
private String baseUrl;

public UrlService(UrlRepository urlRepository, UserRepository userRepository) {
    this.urlRepository = urlRepository;
    this.userRepository = userRepository;
}

// Update the create method to accept the username
public CreateUrlResponse createShortUrl(String longUrl, String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Url url = new Url();
    url.setLongUrl(longUrl);
    url.setUser(user); // Attach the user!
    
    Url savedUrl = urlRepository.save(url);
    
    String shortCode = UrlShortenerUtil.encode(savedUrl.getId());
    String shortUrl = baseUrl + "/" + shortCode;
    
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

        if (urlRepository.findByShortCode(customCode).isPresent()) {
            throw new IllegalArgumentException("Custom code already in use!");
        }

        Url url = new Url();

        url.setShortCode(customCode);
        url.setLongUrl(longUrl);

        urlRepository.save(url);

        return longUrl; 
    }

    @Cacheable(value = "admin-urls", key = "#code")
    public String getUrl(String code) {

        Url url = urlRepository.findByShortCode(code)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found"));    

        return url.getLongUrl();
    }

    public List<Url> getUserLinks(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    return urlRepository.findByUser(user);
}
}
