package com.kecheng.market.security.interceptor;

import com.kecheng.market.common.exception.ForbiddenException;
import com.kecheng.market.common.exception.UnauthorizedException;
import com.kecheng.market.common.log.TraceContext;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.security.annotation.AnonymousAccess;
import com.kecheng.market.security.annotation.RequireAdmin;
import com.kecheng.market.security.annotation.RequireLogin;
import com.kecheng.market.security.context.UserContext;
import com.kecheng.market.security.model.LoginUser;
import com.kecheng.market.security.properties.JwtProperties;
import com.kecheng.market.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public JwtAuthInterceptor(
            JwtUtil jwtUtil,
            JwtProperties jwtProperties,
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        AccessPolicy accessPolicy = resolveAccessPolicy(handlerMethod, request);
        request.setAttribute(TraceContext.ACCESS_POLICY_ATTRIBUTE, accessPolicy.name());
        request.setAttribute(TraceContext.OPERATION_ATTRIBUTE, resolveOperationName(handlerMethod));

        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            try {
                LoginUser loginUser = refreshLoginUser(jwtUtil.parseToken(token));
                UserContext.set(loginUser);
                TraceContext.bindUser(loginUser);
                request.setAttribute(TraceContext.USER_ID_ATTRIBUTE, String.valueOf(loginUser.userId()));
                request.setAttribute(TraceContext.USER_ROLE_ATTRIBUTE, loginUser.role());
            } catch (UnauthorizedException | ForbiddenException exception) {
                if (accessPolicy == AccessPolicy.ANONYMOUS) {
                    log.info("Ignore invalid token on anonymous endpoint path={} message={}", request.getRequestURI(), exception.getMessage());
                } else {
                    throw exception;
                }
            }
        }

        if (accessPolicy == AccessPolicy.ANONYMOUS) {
            return true;
        }
        if (UserContext.get() == null) {
            throw new UnauthorizedException("Please login before accessing this endpoint");
        }
        if (accessPolicy == AccessPolicy.ADMIN && !"admin".equalsIgnoreCase(UserContext.getRole())) {
            throw new ForbiddenException("This endpoint is only available to administrators");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
        TraceContext.clearUser();
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

    private LoginUser refreshLoginUser(LoginUser tokenUser) {
        if (useMysql()) {
            try {
                var currentUser = requirePersistence().findUserByUsernameOrStudentNo(tokenUser.username());
                if (!currentUser.getId().equals(tokenUser.userId())) {
                    throw new UnauthorizedException("Login session is no longer valid");
                }
                if ("disabled".equalsIgnoreCase(currentUser.getStatus())) {
                    throw new ForbiddenException("Account has been disabled");
                }
                return new LoginUser(currentUser.getId(), currentUser.getUsername(), currentUser.getRealName(), currentUser.getRole());
            } catch (UnauthorizedException | ForbiddenException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new UnauthorizedException("Login session is no longer valid");
            }
        }

        MarketStore.UserData currentUser;
        try {
            currentUser = requireStore().findUserByUsernameOrStudentNo(tokenUser.username());
        } catch (RuntimeException exception) {
            throw new UnauthorizedException("Login session is no longer valid");
        }
        if (!currentUser.id.equals(tokenUser.userId())) {
            throw new UnauthorizedException("Login session is no longer valid");
        }
        if (currentUser.disabled) {
            throw new ForbiddenException("Account has been disabled");
        }
        return new LoginUser(currentUser.id, currentUser.username, currentUser.name, currentUser.role);
    }

    private AccessPolicy resolveAccessPolicy(HandlerMethod handlerMethod, HttpServletRequest request) {
        boolean methodAnonymous = handlerMethod.hasMethodAnnotation(AnonymousAccess.class);
        boolean classAnonymous = handlerMethod.getBeanType().isAnnotationPresent(AnonymousAccess.class);
        boolean methodRequireLogin = handlerMethod.hasMethodAnnotation(RequireLogin.class);
        boolean classRequireLogin = handlerMethod.getBeanType().isAnnotationPresent(RequireLogin.class);
        boolean methodRequireAdmin = handlerMethod.hasMethodAnnotation(RequireAdmin.class);
        boolean classRequireAdmin = handlerMethod.getBeanType().isAnnotationPresent(RequireAdmin.class);

        boolean anonymous = methodAnonymous || classAnonymous;
        boolean requireLogin = methodRequireLogin || classRequireLogin;
        boolean requireAdmin = methodRequireAdmin || classRequireAdmin || isAdminPath(request);

        if (anonymous && (requireLogin || requireAdmin)) {
            log.error("Conflicting access policy on {}#{}", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName());
            throw new ForbiddenException("Conflicting access policy configuration");
        }
        if (anonymous) {
            return AccessPolicy.ANONYMOUS;
        }
        if (requireAdmin) {
            return AccessPolicy.ADMIN;
        }
        if (requireLogin) {
            return AccessPolicy.LOGIN;
        }

        log.warn("Missing access policy on {}#{} path={}",
                handlerMethod.getBeanType().getSimpleName(),
                handlerMethod.getMethod().getName(),
                request.getRequestURI());
        throw new ForbiddenException("This endpoint is not accessible");
    }

    private boolean isAdminPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/admin/");
    }

    private String resolveOperationName(HandlerMethod handlerMethod) {
        Operation operation = handlerMethod.getMethodAnnotation(Operation.class);
        if (operation != null && StringUtils.hasText(operation.summary())) {
            return operation.summary();
        }
        return handlerMethod.getBeanType().getSimpleName() + "#" + handlerMethod.getMethod().getName();
    }

    private boolean useMysql() {
        return "mysql".equalsIgnoreCase(storageMode);
    }

    private MarketStore requireStore() {
        if (marketStore == null) {
            throw new IllegalStateException("Memory store is not available");
        }
        return marketStore;
    }

    private MarketPersistenceService requirePersistence() {
        if (persistenceService == null) {
            throw new IllegalStateException("MySQL persistence service is not available");
        }
        return persistenceService;
    }

    private enum AccessPolicy {
        ANONYMOUS,
        LOGIN,
        ADMIN
    }
}
