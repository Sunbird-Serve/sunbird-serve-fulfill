package com.sunbird.serve.fulfill.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
    private final String errorCode;
    private final String serviceName;

    public ServiceException(String message, String errorCode, String serviceName) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
    }

    public ServiceException(String message, String errorCode, String serviceName, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
    }

    public ServiceException(String message) {
        super(message);
        this.errorCode = "SERVICE_ERROR";
        this.serviceName = "UNKNOWN";
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SERVICE_ERROR";
        this.serviceName = "UNKNOWN";
    }
} 