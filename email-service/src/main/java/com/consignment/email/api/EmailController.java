package com.consignment.email.api;

import com.consignment.email.model.EmailRequest;
import com.consignment.email.service.EmailSenderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailSenderService emailSenderService;

    public EmailController(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@Valid @RequestBody EmailRequest request) {
        emailSenderService.send(request);
        return ResponseEntity.accepted().body(Map.of("message", "Email queued for delivery"));
    }
}
