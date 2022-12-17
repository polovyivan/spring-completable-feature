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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public record CustomerService(CustomerRepository customerRepository,
                              AddressClient addressClient,
                              PurchaseTransactionClient purchaseTransactionClient,
                              FinancialClient financialClient,
                              LoyaltyClient loyaltyClient) {

    public List<CustomerResponse> getAllCustomers() {
        log.info("Getting all customers");
        return customerRepository.findAll().stream()
                .map(CustomerResponse::valueOf)
                .collect(Collectors.toList());
    }

    public CustomerResponse getCustomerById(Integer customerId) {
        log.info("Getting customer by id {} ", customerId);
        LocalDateTime startTime = LocalDateTime.now();
        CustomerResponse customerResponse = customerRepository.findAll().stream()
                .filter(c -> c.getId().equals(customerId))
                .findFirst()
                .map(CustomerResponse::valueOf)
                .map(this::fetchCustomerInfo)
                .orElse(null);
        log.info("Operation duration {} sec", Duration.between(startTime, LocalDateTime.now()).toSeconds());
        return customerResponse;
    }

    public void replaceCustomer(Integer customerId, UpdateCustomerRequest request) {
        log.info("Replacing customer", customerId);
        customerRepository.findById(customerId)
                .ifPresent(customerEntity -> {
                    customerEntity.setPhoneNumber(request.getPhoneNumber());
                    customerRepository.save(customerEntity);
                });
        Set<FinancialInfo> financialInfo = request.getFinancialInfo().stream()
                .map(FinancialInfo::valueOf)
                .collect(Collectors.toSet());
        financialClient.updateFinancialInfo(customerId, financialInfo);

        Address address = Address.valueOf(request.getAddress());
        addressClient.updateAddressByCustomerId(customerId, address);
        log.info("Customer updated successfully!");
    }

    public void updateCustomer(Integer customerId, UpdateCustomerRequest request) {
        log.info("Updating customer", customerId);
        if (request.getPhoneNumber() != null) {
            log.info("Received a phone number, updating customer");
            customerRepository.findById(customerId)
                    .ifPresent(customerEntity -> {
                        customerEntity.setPhoneNumber(request.getPhoneNumber());
                        customerRepository.save(customerEntity);
                    });
        }
        if (!CollectionUtils.isEmpty(request.getFinancialInfo())) {
            log.info("Received a financial info, updating it");
            Set<FinancialInfo> financialInfo = request.getFinancialInfo().stream()
                    .map(FinancialInfo::valueOf)
                    .collect(Collectors.toSet());
            financialClient.updateFinancialInfo(customerId, financialInfo);
        }
        if (request.getAddress() != null) {
            log.info("Received a address, updating it");
            Address address = Address.valueOf(request.getAddress());
            addressClient.updateAddressByCustomerId(customerId, address);
        }
        log.info("Customer updated successfully!");
    }

    private CustomerResponse fetchCustomerInfo(CustomerResponse customerResponse) {
        Integer customerId = customerResponse.getId();
        AddressResponse addressResponse = addressClient.getAddressByCustomerId(customerId)
                .map(AddressResponse::valueOf)
                .orElse(null);

        List<PurchaseTransactionResponse> purchaseTransactionResponses = Stream.ofNullable(
                        purchaseTransactionClient.getPurchaseTransactionsByCustomerId(customerId))
                .flatMap(Collection::stream)
                .map(PurchaseTransactionResponse::valueOf)
                .collect(Collectors.toList());

        List<FinancialResponse> financialResponses = Stream.ofNullable(
                        financialClient.getFinancialInfoByCustomerId(customerId))
                .flatMap(Collection::stream)
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
        return customerResponse;
    }
}
