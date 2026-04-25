package com.kecheng.market.common.log;

import com.kecheng.market.security.model.LoginUser;
import org.slf4j.MDC;

public final class TraceContext {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_ATTRIBUTE = TraceContext.class.getName() + ".traceId";
    public static final String START_TIME_ATTRIBUTE = TraceContext.class.getName() + ".startTime";
    public static final String ACCESS_POLICY_ATTRIBUTE = TraceContext.class.getName() + ".accessPolicy";
    public static final String OPERATION_ATTRIBUTE = TraceContext.class.getName() + ".operation";
    public static final String BUSINESS_CODE_ATTRIBUTE = TraceContext.class.getName() + ".businessCode";
    public static final String BUSINESS_MESSAGE_ATTRIBUTE = TraceContext.class.getName() + ".businessMessage";
    public static final String USER_ID_ATTRIBUTE = TraceContext.class.getName() + ".userId";
    public static final String USER_ROLE_ATTRIBUTE = TraceContext.class.getName() + ".userRole";

    private static final String MDC_TRACE_ID_KEY = "traceId";
    private static final String MDC_USER_ID_KEY = "userId";
    private static final String MDC_USER_ROLE_KEY = "userRole";

    private TraceContext() {
    }

    public static void bindTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            MDC.remove(MDC_TRACE_ID_KEY);
            return;
        }
        MDC.put(MDC_TRACE_ID_KEY, traceId);
    }

    public static String getTraceId() {
        return MDC.get(MDC_TRACE_ID_KEY);
    }

    public static void bindUser(LoginUser loginUser) {
        if (loginUser == null || loginUser.userId() == null) {
            clearUser();
            return;
        }
        MDC.put(MDC_USER_ID_KEY, String.valueOf(loginUser.userId()));
        if (loginUser.role() == null || loginUser.role().isBlank()) {
            MDC.remove(MDC_USER_ROLE_KEY);
        } else {
            MDC.put(MDC_USER_ROLE_KEY, loginUser.role());
        }
    }

    public static void clearUser() {
        MDC.remove(MDC_USER_ID_KEY);
        MDC.remove(MDC_USER_ROLE_KEY);
    }

    public static void clearAll() {
        clearUser();
        MDC.remove(MDC_TRACE_ID_KEY);
    }
}
