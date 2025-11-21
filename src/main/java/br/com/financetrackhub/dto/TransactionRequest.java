package br.com.financetrackhub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    
    @NotBlank(message = "O tipo da transação é obrigatório")
    private String type;
    
    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    private BigDecimal value;
    
    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    private String description;
    
    @NotNull(message = "A data é obrigatória")
    private LocalDate date;
    
    @NotNull(message = "A categoria é obrigatória")
    private Long categoryId;
}

