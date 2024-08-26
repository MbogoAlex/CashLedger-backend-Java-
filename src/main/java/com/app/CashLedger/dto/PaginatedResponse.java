package com.app.CashLedger.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private List<T> data;
    private long totalUsers;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public PaginatedResponse(List<T> data, long totalUsers, int totalPages, int currentPage, int pageSize) {
        this.data = data;
        this.totalUsers = totalUsers;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    // Getters and setters
}
