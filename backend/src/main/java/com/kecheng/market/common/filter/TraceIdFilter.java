package com.kecheng.market.common.filter;

import com.kecheng.market.common.log.RequestLogFormatter;
import com.kecheng.market.common.log.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final Logger requestLog = LoggerFactory.getLogger("market.request");
    private static final Logger operationLog = LoggerFactory.getLogger("market.operation");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        request.setAttribute(TraceContext.TRACE_ID_ATTRIBUTE, traceId);
        request.setAttribute(TraceContext.START_TIME_ATTRIBUTE, System.nanoTime());
        response.setHeader(TraceContext.TRACE_ID_HEADER, traceId);
        TraceContext.bindTraceId(traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            logRequest(request, response, traceId);
            TraceContext.clearAll();
        }
    }

    private void logRequest(HttpServletRequest request, HttpServletResponse response, String traceId) {
        long startNanos = readStartTime(request);
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        Integer businessCode = readBusinessCode(request, response);
        String businessMessage = RequestLogFormatter.normalize((String) request.getAttribute(TraceContext.BUSINESS_MESSAGE_ATTRIBUTE));
        String accessPolicy = RequestLogFormatter.normalize((String) request.getAttribute(TraceContext.ACCESS_POLICY_ATTRIBUTE));
        String operation = RequestLogFormatter.normalize((String) request.getAttribute(TraceContext.OPERATION_ATTRIBUTE));
        String userId = RequestLogFormatter.normalize((String) request.getAttribute(TraceContext.USER_ID_ATTRIBUTE));
        String userRole = RequestLogFormatter.normalize((String) request.getAttribute(TraceContext.USER_ROLE_ATTRIBUTE));
        String query = RequestLogFormatter.formatQuery(request);
        String clientIp = RequestLogFormatter.clientIp(request);

        requestLog.info(
                "traceId={} method={} path={} query={} clientIp={} userId={} role={} access={} action={} httpStatus={} bizCode={} durationMs={} message={}",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                query,
                clientIp,
                userId,
                userRole,
                accessPolicy,
                operation,
                response.getStatus(),
                businessCode,
                durationMs,
                businessMessage
        );

        if (shouldWriteOperationLog(request)) {
            operationLog.info(
                    "traceId={} operator={} role={} method={} path={} action={} resultCode={} durationMs={}",
                    traceId,
                    userId,
                    userRole,
                    request.getMethod(),
                    request.getRequestURI(),
                    operation,
                    businessCode,
                    durationMs
            );
        }
    }

    private boolean shouldWriteOperationLog(HttpServletRequest request) {
        String method = request.getMethod();
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri != null && (uri.startsWith("/api/admin/") || uri.startsWith("/api/auth/"));
    }

    private long readStartTime(HttpServletRequest request) {
        Object value = request.getAttribute(TraceContext.START_TIME_ATTRIBUTE);
        if (value instanceof Long longValue) {
            return longValue;
        }
        return System.nanoTime();
    }

    private Integer readBusinessCode(HttpServletRequest request, HttpServletResponse response) {
        Object value = request.getAttribute(TraceContext.BUSINESS_CODE_ATTRIBUTE);
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        return response.getStatus() >= 400 ? response.getStatus() : 200;
    }

    private String resolveTraceId(HttpServletRequest request) {
        String incomingTraceId = request.getHeader(TraceContext.TRACE_ID_HEADER);
        if (incomingTraceId != null) {
            String normalized = incomingTraceId.trim();
            if (!normalized.isEmpty() && normalized.length() <= 64) {
                return normalized;
            }
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
