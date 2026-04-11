package com.akhil.urlShortner.repositories;

import com.akhil.urlShortner.models.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
}