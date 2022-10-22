package com.polovyi.ivan.tutorials.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialResponse {

    private String id;

    private String creditCardNumber;

    private String iban;

    public static FinancialResponse valueOf(FinancialClientResponse financialClientResponse) {
        return builder()
                .id(financialClientResponse.getId())
                .creditCardNumber(financialClientResponse.getCreditCardNumber())
                .iban(financialClientResponse.getIban())
                .build();
    }
}
