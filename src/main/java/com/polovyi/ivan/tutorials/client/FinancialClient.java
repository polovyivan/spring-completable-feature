package com.polovyi.ivan.tutorials.client;

import com.polovyi.ivan.tutorials.configuration.DataLoader;
import com.polovyi.ivan.tutorials.dto.FinancialClientResponse;
import com.polovyi.ivan.tutorials.utils.SleepUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialClient {

    private final DataLoader dataLoader;

    public List<FinancialClientResponse> getFinancialInfoByCustomerId(Integer customerId) {
        log.info("Getting financial info by customerId {}", customerId);
        SleepUtils.loadingSimulator(2);
        return dataLoader.getFinancialResponses().get(customerId);

    }

}
