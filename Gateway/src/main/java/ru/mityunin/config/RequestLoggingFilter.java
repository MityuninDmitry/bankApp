package ru.mityunin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Profile("production")
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Оборачиваем запрос для кэширования содержимого
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        logger.info("=== GATEWAY REQUEST ===");
        logger.info("Method: {}", wrappedRequest.getMethod());
        logger.info("URI: {}", wrappedRequest.getRequestURI());
        logger.info("Query: {}", wrappedRequest.getQueryString());
        logger.info("Remote Host: {}", wrappedRequest.getRemoteHost());
        logger.info("Headers:");
        wrappedRequest.getHeaderNames().asIterator()
                .forEachRemaining(headerName ->
                        logger.info("  {}: {}", headerName, wrappedRequest.getHeader(headerName)));

        // Пропускаем запрос через цепочку фильтров
        filterChain.doFilter(wrappedRequest, response);

        // После обработки логируем тело запроса
        logRequestBody(wrappedRequest);

        logger.info("=== GATEWAY REQUEST OUT ===");
        logger.info("Response status: {}", response.getStatus());
        logger.info("===========================");

        logger.info("=========================");
    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            logger.info("Request Body: {}", body);
        } else {
            logger.info("Request Body: [empty]");
        }
    }
}
