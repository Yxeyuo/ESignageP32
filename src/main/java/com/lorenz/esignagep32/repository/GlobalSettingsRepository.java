package com.lorenz.esignagep32.repository;

import com.lorenz.esignagep32.model.GlobalSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlobalSettingsRepository extends JpaRepository<GlobalSettings, Long> {
}