package com.kecheng.market.common.handler;

import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.common.exception.BusinessException;
import com.kecheng.market.common.log.TraceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        recordBusinessFailure(request, exception.getCode(), exception.getMessage());
        if (exception.getCode() >= 500) {
            log.error("Business exception traceId={} method={} path={} code={} message={}",
                    TraceContext.getTraceId(), request.getMethod(), request.getRequestURI(), exception.getCode(), exception.getMessage(), exception);
        } else {
            log.warn("Business exception traceId={} method={} path={} code={} message={}",
                    TraceContext.getTraceId(), request.getMethod(), request.getRequestURI(), exception.getCode(), exception.getMessage());
        }
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(item -> item.getField() + ": " + item.getDefaultMessage())
                .collect(Collectors.joining("; "));
        recordBusinessFailure(request, 400, message);
        log.warn("Validation exception traceId={} method={} path={} message={}",
                TraceContext.getTraceId(), request.getMethod(), request.getRequestURI(), message);
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBind(BindException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(item -> item.getField() + ": " + item.getDefaultMessage())
                .collect(Collectors.joining("; "));
        recordBusinessFailure(request, 400, message);
        log.warn("Bind exception traceId={} method={} path={} message={}",
                TraceContext.getTraceId(), request.getMethod(), request.getRequestURI(), message);
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraint(ConstraintViolationException exception, HttpServletRequest request) {
        recordBusinessFailure(request, 400, exception.getMessage());
        log.warn("Constraint violation traceId={} method={} path={} message={}",
                TraceContext.getTraceId(), request.getMethod(), request.getRequestURI(), exception.getMessage());
        return ApiResponse.fail(400, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        String message = "Server error, please contact the administrator and provide traceId=" + (traceId == null ? "-" : traceId);
        recordBusinessFailure(request, 500, "internal_error");
        log.error("Unhandled exception traceId={} method={} path={} message={}",
                traceId, request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return ApiResponse.fail(500, message);
    }

    private void recordBusinessFailure(HttpServletRequest request, Integer code, String message) {
        request.setAttribute(TraceContext.BUSINESS_CODE_ATTRIBUTE, code);
        request.setAttribute(TraceContext.BUSINESS_MESSAGE_ATTRIBUTE, message);
    }
}
