package com.akhil.urlShortner.controllers;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.akhil.urlShortner.dto.CreateUrlResponse;
import com.akhil.urlShortner.dto.UrlRequest;
import com.akhil.urlShortner.models.Url;
import com.akhil.urlShortner.services.UrlService;

import jakarta.validation.Valid;

@RestController
public class UrlController {

    private final UrlService urlService;

    
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

@PostMapping("/api/urls")
public ResponseEntity<CreateUrlResponse> createShortUrl(@RequestBody @Valid UrlRequest request, Principal principal) {
    // principal.getName() extracts the username from the validated JWT
    CreateUrlResponse response = urlService.createShortUrl(request.getLongUrl(), principal.getName());
    return ResponseEntity.status(201).body(response);
}

@GetMapping("/{code:[a-zA-Z0-9]+}")
public ResponseEntity<Void> redirectToLongUrl(@PathVariable String code) {
    String longUrl = urlService.getLongUrl(code);
    return ResponseEntity.status(302).location(URI.create(longUrl)).build();
}

    @PostMapping("/create-custom")
    public ResponseEntity<String> createCustomUrl(
            @RequestParam String customCode, 
            @RequestParam String longUrl) {
        
        String customUrl = urlService.createAdminLink(customCode, longUrl);
        return ResponseEntity.status(201).header("Link", "<" + longUrl + ">; rel=preconnect").body(customUrl);
    }
    
    @GetMapping("/api/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping("/api/urls/my-links")
public ResponseEntity<List<Url>> getMyLinks(Principal principal) {
    return ResponseEntity.ok(urlService.getUserLinks(principal.getName()));
}

}