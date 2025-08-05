package com.lorenz.esignagep32.repository;

import com.lorenz.esignagep32.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByOwnerUsername(String username);
}