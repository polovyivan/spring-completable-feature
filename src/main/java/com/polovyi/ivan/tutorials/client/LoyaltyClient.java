package com.polovyi.ivan.tutorials.client;

import com.polovyi.ivan.tutorials.configuration.DataLoader;
import com.polovyi.ivan.tutorials.dto.LoyaltyClientResponse;
import com.polovyi.ivan.tutorials.utils.SleepUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyClient {

   private final DataLoader dataLoader;

   public Optional<LoyaltyClientResponse> getLoyaltyPointsByCustomerId(Integer customerId) {
       log.info("Getting loyalty points by customerId {}", customerId);
       SleepUtils.loadingSimulator(1);
       return Optional.ofNullable(new LoyaltyClientResponse(dataLoader.getPoints().get(customerId)));

   }

}
