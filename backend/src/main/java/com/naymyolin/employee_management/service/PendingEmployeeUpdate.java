package com.naymyolin.employee_management.service;

import com.naymyolin.employee_management.model.Employee;

import java.time.Instant;

record PendingEmployeeUpdate(
        Long employeeId,
        Employee updatedEmployee,
        Instant expiresAt
) {

    boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}