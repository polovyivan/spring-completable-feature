package com.polovyi.ivan.tutorials.service;

import com.polovyi.ivan.tutorials.client.AddressClient;
import com.polovyi.ivan.tutorials.client.FinancialClient;
import com.polovyi.ivan.tutorials.client.LoyaltyClient;
import com.polovyi.ivan.tutorials.client.PurchaseTransactionClient;
import com.polovyi.ivan.tutorials.dto.AddressResponse;
import com.polovyi.ivan.tutorials.dto.CustomerResponse;
import com.polovyi.ivan.tutorials.dto.FinancialResponse;
import com.polovyi.ivan.tutorials.dto.LoyaltyClientResponse;
import com.polovyi.ivan.tutorials.dto.LoyaltyResponse;
import com.polovyi.ivan.tutorials.dto.PurchaseTransactionResponse;
import com.polovyi.ivan.tutorials.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressClient addressClient;
    private final PurchaseTransactionClient purchaseTransactionClient;
    private final FinancialClient financialClient;
    private final LoyaltyClient loyaltyClient;

    public List<CustomerResponse> getAllCustomers() {
        log.info("Getting all customers");
        return customerRepository.findAll().stream()
                .map(CustomerResponse::valueOf)
                .collect(Collectors.toList());
    }

    public CustomerResponse getCustomerById(Integer customerId) {
        log.info("Getting customer by id {} ", customerId);

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        LocalDateTime startTime = LocalDateTime.now();
        log.info("====> {} available processors. <====", availableProcessors);

        log.info("Getting customer from database");
        CustomerResponse customerResponse = customerRepository.findAll().stream()
                .filter(c -> c.getId().equals(customerId))
                .findFirst()
                .map(CustomerResponse::valueOf)
                .orElseThrow();

        AddressResponse addressResponse = addressClient.getAddressByCustomerId(customerId)
                .map(AddressResponse::valueOf)
                .orElse(null);

        List<PurchaseTransactionResponse> purchaseTransactionResponses = purchaseTransactionClient.getPurchaseTransactionsByCustomerId(
                        customerId).stream().map(PurchaseTransactionResponse::valueOf)
                .collect(Collectors.toList());

        List<FinancialResponse> financialResponses = financialClient.getFinancialInfoByCustomerId(customerId).stream()
                .map(FinancialResponse::valueOf)
                .collect(Collectors.toList());

        LoyaltyResponse loyaltyResponse = loyaltyClient.getLoyaltyPointsByCustomerId(customerId)
                .map(LoyaltyClientResponse::getPoints)
                .map(LoyaltyResponse::new)
                .orElse(null);

        customerResponse.setAddressResponse(addressResponse);
        customerResponse.setPurchaseTransactions(purchaseTransactionResponses);
        customerResponse.setFinancialResponses(financialResponses);
        customerResponse.setLoyaltyResponse(loyaltyResponse);

        log.info("Operation duration {} sec", Duration.between(startTime, LocalDateTime.now()).toSeconds());

        return customerResponse;
    }

}
