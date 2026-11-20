package com.tidewater.freight.exception;

public class RateUnavailableException extends ShippingException {
    public RateUnavailableException(String message, Throwable cause) { super(message, cause); }
    public RateUnavailableException(String message) { super(message); }
}