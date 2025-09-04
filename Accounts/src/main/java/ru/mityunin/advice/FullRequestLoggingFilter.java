package ru.mityunin.advice;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class FullRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FullRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Оборачиваем запрос и ответ для кэширования
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // Логируем входящий запрос
        logRequest(wrappedRequest);

        long startTime = System.currentTimeMillis();

        try {
            // Пропускаем запрос через цепочку фильтров
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // Логируем ответ и время выполнения
            long duration = System.currentTimeMillis() - startTime;
            logResponse(wrappedRequest, wrappedResponse, duration);

            // Копируем содержимое ответа обратно в оригинальный response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        logger.info("=== INCOMING REQUEST ===");
        logger.info("Method: {}", request.getMethod());
        logger.info("URI: {}", request.getRequestURI());
        logger.info("Query: {}", request.getQueryString());
        logger.info("Remote Addr: {}", request.getRemoteAddr());
        logger.info("Remote Host: {}", request.getRemoteHost());
        logger.info("Content-Type: {}", request.getContentType());
        logger.info("Content-Length: {}", request.getContentLength());

        // Логируем заголовки
        logger.info("Headers:");
        request.getHeaderNames().asIterator()
                .forEachRemaining(headerName ->
                        logger.info("  {}: {}", headerName, request.getHeader(headerName)));

        // Логируем тело запроса
        logRequestBody(request);
        logger.info("=========================");
    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);

            // Ограничиваем длину для больших тел
            int maxLength = 2000;
            String logBody = body.length() > maxLength ?
                    body.substring(0, maxLength) + "... [truncated]" : body;

            logger.info("Request Body ({} chars): {}", content.length, logBody);
        } else {
            logger.info("Request Body: [empty]");
        }
    }

    private void logResponse(ContentCachingRequestWrapper request,
                             ContentCachingResponseWrapper response,
                             long duration) {
        logger.info("=== RESPONSE ===");
        logger.info("Method: {}", request.getMethod());
        logger.info("URI: {}", request.getRequestURI());
        logger.info("Status: {}", response.getStatus());
        logger.info("Duration: {} ms", duration);

        // Логируем заголовки ответа
        logger.info("Response Headers:");
        response.getHeaderNames().forEach(headerName ->
                logger.info("  {}: {}", headerName, response.getHeader(headerName)));

        // Логируем тело ответа
        logResponseBody(response);
        logger.info("=================");
    }

    private void logResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);

            // Ограничиваем длину для больших тел
            int maxLength = 2000;
            String logBody = body.length() > maxLength ?
                    body.substring(0, maxLength) + "... [truncated]" : body;

            logger.info("Response Body ({} chars): {}", content.length, logBody);
        } else {
            logger.info("Response Body: [empty]");
        }
    }
}
