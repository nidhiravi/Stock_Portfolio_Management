package com.portfolio.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class AvailableStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String name;
    private double totalShares;
    private double currentPrice;
}