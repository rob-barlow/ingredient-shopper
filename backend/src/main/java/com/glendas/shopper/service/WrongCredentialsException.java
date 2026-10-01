package com.glendas.shopper.service;

/**
 * The username or password was wrong. Deliberately doesn't say which (004 AC-3).
 * {@link com.glendas.shopper.controller.GlobalExceptionHandler} turns it into a 401 Problem.
 */
public class WrongCredentialsException extends RuntimeException {

    public static final String MESSAGE = "Wrong username or password";

    public WrongCredentialsException() {
        super(MESSAGE);
    }
}
