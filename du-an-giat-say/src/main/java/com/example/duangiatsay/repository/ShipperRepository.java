package com.example.duangiatsay.repository;

import com.example.duangiatsay.model.Shipper;
import com.example.duangiatsay.model.ShipperStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, Long> {
    List<Shipper> findByStatus(ShipperStatus status);
}