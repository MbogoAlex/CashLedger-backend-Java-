package com.app.CashLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEditDto {
    private Integer userId;
    private String categoryName;
    private List<String> keywords = new ArrayList<>();
}

