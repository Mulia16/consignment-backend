package com.consignment.service.filter;

import com.consignment.service.logging.ApiLoggingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private final ApiLoggingService apiLoggingService;

    public ApiLoggingFilter(ApiLoggingService apiLoggingService) {
        this.apiLoggingService = apiLoggingService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        Instant start = Instant.now();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            String correlationId = wrappedResponse.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
            apiLoggingService.logInbound(
                    request.getRequestURI(),
                    correlationId,
                    extractPayload(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding()),
                    extractPayload(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding()),
                    wrappedResponse.getStatus(),
                    Duration.between(start, Instant.now()).toMillis()
            );
            wrappedResponse.copyBodyToResponse();
        }
    }

    private String extractPayload(byte[] content, String encoding) {
        if (content == null || content.length == 0) {
            return null;
        }
        Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        return new String(content, charset);
    }
}
