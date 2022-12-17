package com.polovyi.ivan.tutorials.client;

import com.polovyi.ivan.tutorials.configuration.DataLoader;
import com.polovyi.ivan.tutorials.dto.PurchaseTransaction;
import com.polovyi.ivan.tutorials.utils.SleepUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseTransactionClient {

   private final DataLoader dataLoader;

   public Set<PurchaseTransaction> getPurchaseTransactionsByCustomerId(Integer customerId) {
       log.info("Getting purchase transactions by customerId {}", customerId);
       SleepUtils.loadingSimulator(3);
       return dataLoader.getPurchaseTransactionResponses().get(customerId);

   }

}
