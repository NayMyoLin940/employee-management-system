package com.naymyolin.employee_management.dto;

public record AiActionDecision(
        String action,
        String reply,
        Long employeeId,
        String targetEmail,
        String targetName,
        String name,
        String email,
        String phone,
        String department,
        String position,
        String salary,
        String hireDate
) {
}