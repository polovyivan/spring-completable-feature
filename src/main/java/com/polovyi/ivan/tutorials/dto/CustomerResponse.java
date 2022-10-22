package com.polovyi.ivan.tutorials.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.polovyi.ivan.tutorials.entity.CustomerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerResponse {

    private Integer id;

    private String fullName;

    private String phoneNumber;

    private String address;

    private LocalDate createdAt;

    private AddressResponse addressResponse;

    private LoyaltyResponse loyaltyResponse;

    private List<FinancialResponse> financialResponses;

    private List<PurchaseTransactionResponse> purchaseTransactions;

    public static CustomerResponse valueOf(CustomerEntity customer) {
        return builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
