package com.clothingstore.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRevenueDTO {
    private String date; // dd/MM/yyyy hoặc dd/MM
    private BigDecimal totalRevenue; // Y Axis value
    private Long orderCount;
}
