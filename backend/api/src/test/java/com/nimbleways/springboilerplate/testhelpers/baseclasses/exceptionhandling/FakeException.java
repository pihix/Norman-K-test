package com.nimbleways.springboilerplate.testhelpers.baseclasses.exceptionhandling;

class FakeException extends RuntimeException {

    public FakeException() {
        super();
    }

    public FakeException(String message) {
        super(message);
    }
}
