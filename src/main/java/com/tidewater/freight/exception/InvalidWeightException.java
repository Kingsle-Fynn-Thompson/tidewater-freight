package com.tidewater.freight.exception;

public class InvalidWeightException extends ShippingException {
    public InvalidWeightException(String message) { super(message); }
}