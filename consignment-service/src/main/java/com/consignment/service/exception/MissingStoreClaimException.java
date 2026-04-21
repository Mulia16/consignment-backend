package com.consignment.service.exception;

public class MissingStoreClaimException extends RuntimeException {

    public MissingStoreClaimException(String message) {
        super(message);
    }
}
