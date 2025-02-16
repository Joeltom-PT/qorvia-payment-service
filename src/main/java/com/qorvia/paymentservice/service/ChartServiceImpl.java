package com.qorvia.paymentservice.service;

import com.qorvia.paymentservice.repository.AdminRevenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartServiceImpl implements ChartService{

    private final AdminRevenueRepository adminRevenueRepository;

//    @Override
//    public ResponseEntity<?> getRevenue(String type) {
//        return null;
//    }

    @Override
    public ResponseEntity<?> getTopRevenueEvents() {
        Pageable pageable = PageRequest.of(0, 4);
        List<String> top5EventIds = adminRevenueRepository.findTop4EventsByRevenue(pageable);

        return ResponseEntity.ok(top5EventIds);
    }
}
