package com.app.CashLedger.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MultipleCategoriesReportDto {
    private Integer userId;
    private List<Integer> categoryIds;
    private String startDate;
    private String lastDate;
}
