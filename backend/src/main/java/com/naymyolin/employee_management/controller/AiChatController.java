package com.naymyolin.employee_management.controller;

import com.naymyolin.employee_management.dto.ChatRequest;
import com.naymyolin.employee_management.dto.ChatResponse;
import com.naymyolin.employee_management.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request
    ) {
        return ResponseEntity.ok(
                aiChatService.chat(
                        request.message(),
                        request.confirmationToken()
                )
        );
    }
}