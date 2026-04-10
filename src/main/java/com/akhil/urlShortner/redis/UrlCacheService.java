package com.akhil.urlShortner.redis;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.akhil.urlShortner.models.Url;
import com.akhil.urlShortner.repositories.UrlRepository;

@Service
public class UrlCacheService {

 private final RedisTemplate<String, Object> redisTemplate;
 private final UrlRepository repository;
 @Value("${redis-ttl}")
 Long ttl;
 public UrlCacheService(RedisTemplate<String, Object> redisTemplate, UrlRepository
repository) {
 this.redisTemplate = redisTemplate;
 this.repository = repository;
 }
 public String getLongUrl(String shortCode) {
 String cacheKey = "url:" + shortCode; 
 // 1. CACHE HIT
 String cachedUrl = (String) redisTemplate.opsForValue().get(cacheKey);
 if (cachedUrl != null) return cachedUrl;
 // 2. CACHE MISS (Fetch from DB)
 Optional<Url> urlOptional = repository.findByShortCode(shortCode);
 if (urlOptional.isPresent()) {
	 Url url = urlOptional.get();
	 String dbUrl = url.getLongUrl();
	 
	 redisTemplate.opsForValue().set(cacheKey, dbUrl, ttl, TimeUnit.HOURS);
	 return dbUrl;
 }
 return null;
 }

//  // Manual Eviction (Deletion)
//  public void deleteUrl(String shortCode) {
//  // Delete from DB first, then Cache
//  repository.deleteByShortCode(shortCode);
//  redisTemplate.delete("url:" + shortCode);
//  }
}
