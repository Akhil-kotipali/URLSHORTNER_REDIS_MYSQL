package com.akhil.urlShortner.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akhil.urlShortner.models.Url;
import com.akhil.urlShortner.models.User;

@Repository
public interface UrlRepository extends JpaRepository<Url,Long>{
    Optional<Url> findByShortCode(String shortCode);
    List<Url> findByUser(User user);
}
