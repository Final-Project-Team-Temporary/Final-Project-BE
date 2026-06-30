package com.example.whiplash.kis.controller;

import com.example.whiplash.kis.service.KisMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
public class KisStockController {

    private final KisMarketService kisMarketService;

    // GET /api/v1/stocks/005930
    @GetMapping("/{code}")
    public ResponseEntity<?> getStockPrice(@PathVariable String code) {
        var result = kisMarketService.getStockInfo(code);
        return ResponseEntity.ok(result);
    }
}
