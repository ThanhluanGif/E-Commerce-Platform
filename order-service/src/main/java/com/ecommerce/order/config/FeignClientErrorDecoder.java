package com.ecommerce.order.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
public class FeignClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 409) {
            return new ObjectOptimisticLockingFailureException(
                    "Optimistic locking failure from downstream service. Method: " + methodKey,
                    new RuntimeException("Conflict on variant version")
            );
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
