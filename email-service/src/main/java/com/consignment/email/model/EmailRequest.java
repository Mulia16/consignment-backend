package com.consignment.email.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record EmailRequest(
        @NotBlank @Email String to,
        @NotBlank String subject,
        @NotBlank String template,
        Map<String, Object> variables
) {}
