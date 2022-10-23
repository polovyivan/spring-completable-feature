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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public record CustomerCompletableFeatureService(
        CustomerRepository customerRepository,
        AddressClient addressClient,
        PurchaseTransactionClient purchaseTransactionClient,
        FinancialClient financialClient,
        LoyaltyClient loyaltyClient) {

    public CustomerResponse getCustomerById(Integer customerId) {
        log.info("Getting customer by id {} ", customerId);

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        LocalDateTime startTime = LocalDateTime.now();
        log.info("====> {} available processors. <====", availableProcessors);

        log.info("Getting customer from database");

        CompletableFuture<Optional<CustomerResponse>> customerResponseCF = CompletableFuture.supplyAsync(
                () -> customerRepository.findById(customerId)
                        .map(CustomerResponse::valueOf));

        CompletableFuture<AddressResponse> addressResponseCF = CompletableFuture.supplyAsync(
                () -> addressClient.getAddressByCustomerId(customerId)
                        .map(AddressResponse::valueOf)
                        .orElse(null));

        CompletableFuture<List<PurchaseTransactionResponse>> purchaseTransactionResponsesCF = CompletableFuture.supplyAsync(
                () -> Stream.ofNullable(purchaseTransactionClient.getPurchaseTransactionsByCustomerId(customerId))
                        .flatMap(Collection::stream)
                        .map(PurchaseTransactionResponse::valueOf)
                        .collect(Collectors.toList()));

        CompletableFuture<List<FinancialResponse>> financialResponsesCF = CompletableFuture.supplyAsync(
                () -> Stream.ofNullable(financialClient.getFinancialInfoByCustomerId(customerId))
                        .flatMap(Collection::stream)
                        .map(FinancialResponse::valueOf)
                        .collect(Collectors.toList()));

        CompletableFuture<LoyaltyResponse> loyaltyResponseCF = CompletableFuture.supplyAsync(
                () -> loyaltyClient.getLoyaltyPointsByCustomerId(customerId)
                        .map(LoyaltyClientResponse::getPoints)
                        .map(LoyaltyResponse::new)
                        .orElse(null));

        Optional<CustomerResponse> response = customerResponseCF
                .thenCombine(addressResponseCF, (customerResponse, addressResponse) -> {
                    customerResponse.ifPresent(cr -> cr.setAddressResponse(addressResponse));
                    return customerResponse;
                })
                .thenCombine(purchaseTransactionResponsesCF, (customerResponse, purchaseTransactionResponses) -> {
                    customerResponse.ifPresent(cr -> cr.setPurchaseTransactions(purchaseTransactionResponses));
                    return customerResponse;
                })
                .thenCombine(financialResponsesCF, (customerResponse, financialResponses) -> {
                    customerResponse.ifPresent(cr -> cr.setFinancialResponses(financialResponses));
                    return customerResponse;
                })
                .thenCombine(loyaltyResponseCF, (customerResponse, loyaltyResponse) -> {
                    customerResponse.ifPresent(cr -> cr.setLoyaltyResponse(loyaltyResponse));
                    return customerResponse;
                })
                .join();

        log.info("Operation duration {} sec", Duration.between(startTime, LocalDateTime.now()).toSeconds());

        return response
                .orElse(null);
    }

    public CustomerResponse getCustomerByIdUsingAllOf(Integer customerId) {
        log.info("Getting customer by id {} using allOf(...)", customerId);

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        LocalDateTime startTime = LocalDateTime.now();
        log.info("====> {} available processors. <====", availableProcessors);

        log.info("Getting customer from database");

        CompletableFuture<Optional<CustomerResponse>> customerResponseCF = CompletableFuture.supplyAsync(
                () -> customerRepository.findById(customerId)
                        .map(CustomerResponse::valueOf));

        CompletableFuture<AddressResponse> addressResponseCF = CompletableFuture.supplyAsync(
                () -> addressClient.getAddressByCustomerId(customerId)
                        .map(AddressResponse::valueOf)
                        .orElse(null));

        CompletableFuture<List<PurchaseTransactionResponse>> purchaseTransactionResponsesCF = CompletableFuture.supplyAsync(
                () -> Stream.ofNullable(purchaseTransactionClient.getPurchaseTransactionsByCustomerId(customerId))
                        .flatMap(Collection::stream)
                        .map(PurchaseTransactionResponse::valueOf)
                        .collect(Collectors.toList()));

        CompletableFuture<List<FinancialResponse>> financialResponsesCF = CompletableFuture.supplyAsync(
                () -> Stream.ofNullable(financialClient.getFinancialInfoByCustomerId(customerId))
                        .flatMap(Collection::stream)
                        .map(FinancialResponse::valueOf)
                        .collect(Collectors.toList()));

        CompletableFuture<LoyaltyResponse> loyaltyResponseCF = CompletableFuture.supplyAsync(
                () -> loyaltyClient.getLoyaltyPointsByCustomerId(customerId)
                        .map(LoyaltyClientResponse::getPoints)
                        .map(LoyaltyResponse::new)
                        .orElse(null));


        CustomerResponse customerResponse = CompletableFuture.allOf(
                        customerResponseCF, addressResponseCF,
                        purchaseTransactionResponsesCF, financialResponsesCF, loyaltyResponseCF)
                .thenApply(unusedVariable -> {

                            Optional<CustomerResponse> customerResponseOptional = customerResponseCF.join();

                            customerResponseOptional.ifPresent(cr -> {
                                AddressResponse addressResponse = addressResponseCF.join();
                                cr.setAddressResponse(addressResponse);

                                List<PurchaseTransactionResponse> purchaseTransactionResponses = purchaseTransactionResponsesCF.join();
                                cr.setPurchaseTransactions(purchaseTransactionResponses);

                                List<FinancialResponse> financialResponses = financialResponsesCF.join();
                                cr.setFinancialResponses(financialResponses);

                                LoyaltyResponse loyaltyResponse = loyaltyResponseCF.join();
                                cr.setLoyaltyResponse(loyaltyResponse);
                            });
                            return customerResponseOptional
                                    .orElse(null);
                        }
                ).join();
        log.info("Operation duration {} sec", Duration.between(startTime, LocalDateTime.now()).toSeconds());

        return customerResponse;
    }
}
