package com.sunbird.serve.fulfill.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExternalServiceException extends ServiceException {
    private final HttpStatus httpStatus;
    private final String serviceUrl;

    public ExternalServiceException(String message, String errorCode, String serviceName, 
                                   HttpStatus httpStatus, String serviceUrl) {
        super(message, errorCode, serviceName);
        this.httpStatus = httpStatus;
        this.serviceUrl = serviceUrl;
    }

    public ExternalServiceException(String message, String errorCode, String serviceName, 
                                   HttpStatus httpStatus, String serviceUrl, Throwable cause) {
        super(message, errorCode, serviceName, cause);
        this.httpStatus = httpStatus;
        this.serviceUrl = serviceUrl;
    }
} 