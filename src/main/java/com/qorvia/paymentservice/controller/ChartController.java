package com.qorvia.paymentservice.controller;

import com.qorvia.paymentservice.security.RequireRole;
import com.qorvia.paymentservice.security.Roles;
import com.qorvia.paymentservice.service.ChartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment/dashboard")
@RequiredArgsConstructor
@Slf4j
public class ChartController {

    private final ChartService chartService;

//    @GetMapping("/admin/revenue")
//    @RequireRole(role = Roles.ADMIN)
//    public ResponseEntity<?> getAdminRevenueData(String type){
//        return chartService.getRevenue(type);
//    }

    @GetMapping("/admin/top")
    @RequireRole(role = Roles.ADMIN)
    public ResponseEntity<?> getTopRevenueEvents(){
       return chartService.getTopRevenueEvents();
    }

}
