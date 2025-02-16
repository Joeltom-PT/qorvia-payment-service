package com.qorvia.paymentservice.service;

import org.springframework.http.ResponseEntity;

public interface ChartService {
//    ResponseEntity<?> getRevenue(String type);

    ResponseEntity<?> getTopRevenueEvents();
}
