package com.app.CashLedger.exceptions;

public class DataExistsException extends RuntimeException {
    public DataExistsException(String message) {
        super(message);
    }

    public DataExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataExistsException(Throwable cause) {
        super(cause);
    }
}
