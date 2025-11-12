package com.wallet.flowpay.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.flowpay.utility.log.BufferedRequestWrapper;
import com.wallet.flowpay.utility.log.BufferedResponseWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

@Component
@Order(1)
@Slf4j
public class RequestResponseLogFilter extends OncePerRequestFilter {

    private static final Set<String> SKIPPED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/octet-stream",
            "image/png",
            "image/jpeg",
            "image/jpg",
            "multipart/form-data"
    );
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${app.logging.request-response.enabled:true}")
    private boolean isLoggingEnabled;
    @Value("${app.logging.http.max-body-length:2000}")
    private int maxBodyLength;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!isLoggingEnabled) {
            log.info("Skipping logging as it is disabled or request/response is not HTTP.");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Content type: {}", request.getContentType());

        if (request.getContentType() != null && SKIPPED_CONTENT_TYPES.stream().anyMatch(request.getContentType()::startsWith)) {
            log.warn("Skipping logging for content type: {}", request.getContentType());
            filterChain.doFilter(request, response);
            return;
        }

        BufferedRequestWrapper requestWrapper = new BufferedRequestWrapper(request);
        BufferedResponseWrapper responseWrapper = new BufferedResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        try {
            logRequest(requestWrapper);
            filterChain.doFilter(requestWrapper, responseWrapper);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logResponse(responseWrapper, duration);
        }

    }

    private void logResponse(BufferedResponseWrapper responseWrapper, long duration) {
        try {
            String responseBody = truncate(maskSensitiveData(new String(responseWrapper.getCopy())));

            HashMap<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("STATUS", responseWrapper.getStatus());
            if (responseBody == null || responseBody.isBlank()) {
                responseMap.put("RESPONSE BODY", null);
            } else {
                responseMap.put("RESPONSE BODY", new JSONObject(responseBody).toMap());
            }
            responseMap.put("RESPONSE TIME", duration);

            log.info(
                    "[Response]: {}",
                    objectMapper.writeValueAsString(responseMap)
            );
        } catch (Exception e) {
            log.error("Failed to log response", e);
        }
    }

    private void logRequest(BufferedRequestWrapper requestWrapper) {
        try {
            String requestBody = truncate(maskSensitiveData(requestWrapper.getRequestBody()));

            HashMap<String, Object> requestMap = new LinkedHashMap<>();
            requestMap.put("METHOD", requestWrapper.getMethod());
            requestMap.put("URI", requestWrapper.getRequestURI());
            requestMap.put("HEADERS", requestWrapper.getHeaderMap().toString());
            if (requestBody == null || requestBody.isBlank()) {
                requestMap.put("REQUEST BODY", null);
            } else {
                requestMap.put("REQUEST BODY", new JSONObject(requestBody).toMap());
            }

            log.info(
                    "[Request]: {}",
                    objectMapper.writeValueAsString(requestMap)
            );
        } catch (Exception e) {
            log.error("Failed to log request", e);
        }
    }

    private String truncate(String value) {
        if (value == null) return null;
        return value.length() > maxBodyLength
                ? value.substring(0, maxBodyLength) + "...[TRUNCATED]"
                : value;
    }

    private String maskSensitiveData(String content) {
        if (content == null) return null;

        // Mask sensitive JSON fields like password, token, otp, etc.
        return content
                .replaceAll("(?i)(\"password\"\\s*:\\s*\").*?(\")", "$1***$2")
                .replaceAll("(?i)(\"token\"\\s*:\\s*\").*?(\")", "$1***$2")
                .replaceAll("(?i)(\"otp\"\\s*:\\s*\").*?(\")", "$1***$2")
                .replaceAll("(?i)(\"pin\"\\s*:\\s*\").*?(\")", "$1***$2");
    }
}
