package com.polovyi.ivan.tutorials.service;

import com.polovyi.ivan.tutorials.client.AddressClient;
import com.polovyi.ivan.tutorials.client.FinancialClient;
import com.polovyi.ivan.tutorials.client.LoyaltyClient;
import com.polovyi.ivan.tutorials.client.PurchaseTransactionClient;
import com.polovyi.ivan.tutorials.dto.Address;
import com.polovyi.ivan.tutorials.dto.AddressResponse;
import com.polovyi.ivan.tutorials.dto.CustomerResponse;
import com.polovyi.ivan.tutorials.dto.FinancialInfo;
import com.polovyi.ivan.tutorials.dto.FinancialResponse;
import com.polovyi.ivan.tutorials.dto.LoyaltyClientResponse;
import com.polovyi.ivan.tutorials.dto.LoyaltyResponse;
import com.polovyi.ivan.tutorials.dto.PurchaseTransactionResponse;
import com.polovyi.ivan.tutorials.dto.UpdateCustomerRequest;
import com.polovyi.ivan.tutorials.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerCompletableFeatureService {

    private final CustomerRepository customerRepository;
    private final AddressClient addressClient;
    private final PurchaseTransactionClient purchaseTransactionClient;
    private final FinancialClient financialClient;
    private final LoyaltyClient loyaltyClient;

    public CompletableFuture<Void> replaceCustomer(Integer customerId, UpdateCustomerRequest request) {
        log.info("Replacing customer", customerId);
        CompletableFuture<Void> updateCustomerCF = CompletableFuture.runAsync(() -> {
            customerRepository.findById(customerId)
                    .ifPresent(customerEntity -> {
                        customerEntity.setPhoneNumber(request.getPhoneNumber());
                        customerRepository.save(customerEntity);
                    });
        });
        CompletableFuture<Void> updateFinancialInfoCF = CompletableFuture.runAsync(() -> {
            Set<FinancialInfo> financialInfo = request.getFinancialInfo().stream()
                    .map(FinancialInfo::valueOf)
                    .collect(Collectors.toSet());
            financialClient.updateFinancialInfo(customerId, financialInfo);
        });
        CompletableFuture<Void> updateAddressCF = CompletableFuture.runAsync(() -> {
            Address address = Address.valueOf(request.getAddress());
            addressClient.updateAddressByCustomerId(customerId, address);
        });
        return CompletableFuture.allOf(updateCustomerCF, updateAddressCF,
                updateFinancialInfoCF);
    }

    public CompletableFuture<Void> updateCustomer(Integer customerId, UpdateCustomerRequest request) {
        log.info("Updating customer", customerId);
        CompletableFuture<Void> updateCustomerCF = null;
        if (request.getPhoneNumber() != null) {
            log.info("Received a phone number, updating customer");
            updateCustomerCF = CompletableFuture.runAsync(() -> {
                customerRepository.findById(customerId)
                        .ifPresent(customerEntity -> {
                            customerEntity.setPhoneNumber(request.getPhoneNumber());
                            customerRepository.save(customerEntity);
                        });
            });
        }
        CompletableFuture<Void> updateFinancialInfoCF = null;
        if (!CollectionUtils.isEmpty(request.getFinancialInfo())) {
            log.info("Received a financial info, updating it");
            updateFinancialInfoCF = CompletableFuture.runAsync(() -> {
                Set<FinancialInfo> financialInfo = request.getFinancialInfo().stream()
                        .map(FinancialInfo::valueOf)
                        .collect(Collectors.toSet());
                financialClient.updateFinancialInfo(customerId, financialInfo);
            });
        }
        CompletableFuture<Void> updateAddressCF = null;
        if (request.getAddress() != null) {
            log.info("Received a address, updating it");
            updateAddressCF = CompletableFuture.runAsync(() -> {
                Address address = Address.valueOf(request.getAddress());
                addressClient.updateAddressByCustomerId(customerId, address);
            });
        }
        Stream.of(updateCustomerCF, updateFinancialInfoCF, updateAddressCF)
                .filter(Objects::nonNull)
                .forEach(CompletableFuture::join);
        log.info("Customer updated successfully!");
        return updateCustomerCF;
    }

    public CompletableFuture<CustomerResponse> getCustomerById(Integer customerId) {
        log.info("Getting customer by id {} ", customerId);
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

        CompletableFuture<CustomerResponse> response = customerResponseCF
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
                .thenApply(customerResponse -> customerResponse.orElse(null));
        return response;
    }

    public CompletableFuture<CustomerResponse> getCustomerByIdUsingAllOf(Integer customerId) {
        log.info("Getting customer by id {} using allOf(...)", customerId);
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
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(
                customerResponseCF, addressResponseCF,
                purchaseTransactionResponsesCF, financialResponsesCF, loyaltyResponseCF);
        Optional<CustomerResponse> customerResponseOptional = customerResponseCF.join();
        CompletableFuture<CustomerResponse> responseCF = voidCompletableFuture
                .thenApply(unusedVariable -> {
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
                );
        return responseCF;
    }
}
