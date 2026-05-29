package com.example.vuecourseproject.admin.other.view;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesTrendVO {
    private List<String> labels;
    private List<BigDecimal> amounts;
    private List<Integer> counts;
    private String granularity;
}
