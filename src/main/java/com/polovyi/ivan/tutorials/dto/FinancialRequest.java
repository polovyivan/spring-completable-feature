package com.polovyi.ivan.tutorials.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRequest {

    private String creditCardNumber;

    private String iban;

}
