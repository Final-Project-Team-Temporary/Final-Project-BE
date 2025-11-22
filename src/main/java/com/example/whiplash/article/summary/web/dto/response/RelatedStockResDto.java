package com.example.whiplash.article.summary.web.dto.response;

import lombok.Builder;

@Builder
public record RelatedStockResDto(
   String stockName,
   String stockCode,
   String market,
   String sector
) {}
