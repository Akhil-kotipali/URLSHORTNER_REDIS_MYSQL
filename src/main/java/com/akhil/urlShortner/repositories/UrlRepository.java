package com.akhil.urlShortner.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akhil.urlShortner.models.Url;

@Repository
public interface UrlRepository extends JpaRepository<Url,Long>{
    Optional<Url> findByShortCode(String shortCode);
}
