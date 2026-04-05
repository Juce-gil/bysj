package com.kecheng.market.security.interceptor;

import com.kecheng.market.common.exception.BusinessException;
import com.kecheng.market.common.exception.UnauthorizedException;
import com.kecheng.market.security.annotation.AnonymousAccess;
import com.kecheng.market.security.annotation.RequireAdmin;
import com.kecheng.market.security.annotation.RequireLogin;
import com.kecheng.market.security.context.UserContext;
import com.kecheng.market.security.properties.JwtProperties;
import com.kecheng.market.security.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public JwtAuthInterceptor(JwtUtil jwtUtil, JwtProperties jwtProperties) {
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            UserContext.set(jwtUtil.parseToken(token));
        }
        boolean anonymous = handlerMethod.hasMethodAnnotation(AnonymousAccess.class)
                || handlerMethod.getBeanType().isAnnotationPresent(AnonymousAccess.class);
        if (anonymous) {
            return true;
        }
        boolean requireLogin = handlerMethod.hasMethodAnnotation(RequireLogin.class)
                || handlerMethod.getBeanType().isAnnotationPresent(RequireLogin.class);
        boolean requireAdmin = handlerMethod.hasMethodAnnotation(RequireAdmin.class)
                || handlerMethod.getBeanType().isAnnotationPresent(RequireAdmin.class);
        if ((requireLogin || requireAdmin) && UserContext.get() == null) {
            throw new UnauthorizedException("请先登录后再访问");
        }
        if (requireAdmin && !"admin".equalsIgnoreCase(UserContext.getRole())) {
            throw new BusinessException(403, "当前接口仅允许管理员访问");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String rawToken = request.getHeader(jwtProperties.getTokenHeader());
        if (!StringUtils.hasText(rawToken)) {
            return null;
        }
        String prefix = jwtProperties.getTokenPrefix();
        if (StringUtils.hasText(prefix) && rawToken.startsWith(prefix)) {
            return rawToken.substring(prefix.length()).trim();
        }
        return rawToken.trim();
    }
}
