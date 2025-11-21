package br.com.financetrackhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    
    private Summary summary;
    private List<CategoryData> categoryData;
    private List<MonthlyData> monthlyData;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal balance;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryData {
        private String name;
        private BigDecimal value;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyData {
        private String date;
        private BigDecimal value;
    }
}

