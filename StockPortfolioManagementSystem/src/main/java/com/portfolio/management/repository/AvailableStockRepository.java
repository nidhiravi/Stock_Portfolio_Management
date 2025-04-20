package com.portfolio.management.repository;

import com.portfolio.management.model.AvailableStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AvailableStockRepository extends JpaRepository<AvailableStock, Long> {
    Optional<AvailableStock> findBySymbol(String symbol);
}