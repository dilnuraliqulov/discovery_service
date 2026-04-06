package com.example.demo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String TRANSACTION_ID = "transactionId";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Generate or extract transaction ID
        String transactionId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        // Put transaction ID in MDC for logging
        MDC.put(TRANSACTION_ID, transactionId);

        // Add transaction ID to response header for downstream services
        httpResponse.setHeader(CORRELATION_ID_HEADER, transactionId);

        long startTime = System.currentTimeMillis();
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();

        // Log incoming request
        logger.info("Transaction START | TransactionId: {} | Method: {} | URI: {} | Query: {}",
                transactionId, method, uri, queryString != null ? queryString : "");

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();

            // Log response
            if (status >= 200 && status < 300) {
                logger.info("Transaction END | TransactionId: {} | Status: {} | Duration: {}ms | SUCCESS",
                        transactionId, status, duration);
            } else if (status >= 400) {
                logger.warn("Transaction END | TransactionId: {} | Status: {} | Duration: {}ms | ERROR",
                        transactionId, status, duration);
            } else {
                logger.info("Transaction END | TransactionId: {} | Status: {} | Duration: {}ms",
                        transactionId, status, duration);
            }

            // Clear MDC
            MDC.remove(TRANSACTION_ID);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("LoggingFilter initialized");
    }

    @Override
    public void destroy() {
        logger.info("LoggingFilter destroyed");
    }
}

