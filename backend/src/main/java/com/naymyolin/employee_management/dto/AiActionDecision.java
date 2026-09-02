package com.naymyolin.employee_management.dto;

public record AiActionDecision(

        String action,
        String reply,
        String name,
        String email,
        String phone,
        String department,
        String position,
        String salary,
        String hireDate

) {
}