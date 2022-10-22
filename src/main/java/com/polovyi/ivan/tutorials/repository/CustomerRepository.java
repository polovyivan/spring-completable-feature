package com.polovyi.ivan.tutorials.repository;

import com.polovyi.ivan.tutorials.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {

}
