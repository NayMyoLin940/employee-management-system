package com.naymyolin.employee_management.service;

import com.naymyolin.employee_management.model.Employee;

import java.time.Instant;

public record PendingEmployeeCreation(

        Employee employee,
        Instant expiresAt

) {

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}