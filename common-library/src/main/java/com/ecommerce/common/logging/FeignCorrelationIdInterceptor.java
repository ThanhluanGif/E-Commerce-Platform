package com.ecommerce.common.logging;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;


public class FeignCorrelationIdInterceptor implements RequestInterceptor {

    private static final String CORRELATION_ID_HEADER = "Correlation-ID";
    private static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    public void apply(RequestTemplate template) {
        String traceId = MDC.get(MDC_TRACE_ID_KEY);
        if (traceId != null && !traceId.isEmpty()) {
            template.header(CORRELATION_ID_HEADER, traceId);
        }
    }
}
