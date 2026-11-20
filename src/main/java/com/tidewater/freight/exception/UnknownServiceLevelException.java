package com.tidewater.freight.exception;

public class UnknownServiceLevelException extends ShippingException {
    public UnknownServiceLevelException(String message) { super(message); }
    public UnknownServiceLevelException(String message, Throwable cause) { super(message, cause); }
}