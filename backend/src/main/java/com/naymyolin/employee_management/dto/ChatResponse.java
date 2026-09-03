package com.naymyolin.employee_management.dto;

public record ChatResponse(

        String reply,
        boolean confirmationRequired,
        String confirmationToken,
        boolean dataChanged

) {

    public ChatResponse(String reply) {
        this(reply, false, null, false);
    }
}