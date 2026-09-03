package com.naymyolin.employee_management.service;

import java.time.Instant;

record PendingEmployeeDeletion(
        Long employeeId,
        String employeeName,
        Instant expiresAt
) {

    boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}