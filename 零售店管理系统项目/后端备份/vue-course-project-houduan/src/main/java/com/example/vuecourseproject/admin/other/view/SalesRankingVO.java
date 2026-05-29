package com.example.vuecourseproject.admin.other.view;

import lombok.Data;

import java.util.List;

@Data
public class SalesRankingVO {
    private List<SalesRankingDTO> cashierRanking;
    private List<SalesRankingDTO> userRanking;
}