package com.polovyi.ivan.tutorials.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressClientResponse {

    private String id;

    private String street;

    private String streetNumber;

    private String city;

    private String state;

    private String zipCode;

    private String country;
}
