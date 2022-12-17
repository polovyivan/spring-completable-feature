package com.polovyi.ivan.tutorials.controller;

import com.polovyi.ivan.tutorials.dto.CustomerResponse;
import com.polovyi.ivan.tutorials.dto.UpdateCustomerRequest;
import com.polovyi.ivan.tutorials.service.CustomerCompletableFeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/customers/completable-feature")
public class CustomerCFController {

    private final CustomerCompletableFeatureService customerCompletableFeatureService;

    @PutMapping("/{customerId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void replaceCustomerUsingCompletableFeature(@PathVariable Integer customerId,
            @RequestBody UpdateCustomerRequest request) {
        customerCompletableFeatureService.replaceCustomer(customerId, request);
    }

    @PatchMapping("/{customerId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateCustomerUsingCompletableFeature(@PathVariable Integer customerId,
            @RequestBody UpdateCustomerRequest request) {
        customerCompletableFeatureService.updateCustomer(customerId, request);
    }

    @GetMapping("{customerId}")
    public CustomerResponse getCustomerByIdUsingCompletableFeature(@PathVariable Integer customerId) {
        return customerCompletableFeatureService.getCustomerById(customerId);
    }

    @GetMapping("/all-of/{customerId}")
    public CustomerResponse getCustomerByIdUsingCompletableFeatureUsingAllOf(@PathVariable Integer customerId) {
        return customerCompletableFeatureService.getCustomerByIdUsingAllOf(customerId);
    }
}
