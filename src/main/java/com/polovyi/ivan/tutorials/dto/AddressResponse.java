package com.polovyi.ivan.tutorials.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private String id;

    private String street;

    private String streetNumber;

    private String city;

    private String state;

    private String zipCode;

    private String country;

    public static AddressResponse valueOf(AddressClientResponse addressClientResponse) {
        return AddressResponse.builder()
                .id(addressClientResponse.getId())
                .street(addressClientResponse.getStreet())
                .streetNumber(addressClientResponse.getStreetNumber())
                .city(addressClientResponse.getCity())
                .state(addressClientResponse.getState())
                .zipCode(addressClientResponse.getZipCode())
                .country(addressClientResponse.getCountry())
                .build();

    }
}
