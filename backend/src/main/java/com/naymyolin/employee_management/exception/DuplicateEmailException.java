package com.naymyolin.employee_management.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Email is already in use: " + email);
    }
}