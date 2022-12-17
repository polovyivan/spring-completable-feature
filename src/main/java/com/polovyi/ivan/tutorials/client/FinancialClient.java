package com.polovyi.ivan.tutorials.client;

import com.polovyi.ivan.tutorials.configuration.DataLoader;
import com.polovyi.ivan.tutorials.dto.FinancialInfo;
import com.polovyi.ivan.tutorials.utils.SleepUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialClient {

    private final DataLoader dataLoader;

    public Set<FinancialInfo> getFinancialInfoByCustomerId(Integer customerId) {
        log.info("Getting financial info by customerId {}", customerId);
        SleepUtils.loadingSimulator(2);
        return dataLoader.getFinancialResponses().get(customerId);

    }

    public void updateFinancialInfo(Integer customerId, Set<FinancialInfo> response) {
        log.info("Updating financial info by customerId {}", customerId);
        SleepUtils.loadingSimulator(2);
        dataLoader.getFinancialResponses().put(customerId, response);
    }
}
