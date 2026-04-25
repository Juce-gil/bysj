package com.kecheng.market.auth.service.impl;

import com.kecheng.market.auth.dto.LoginRequest;
import com.kecheng.market.auth.dto.RegisterRequest;
import com.kecheng.market.auth.service.AuthService;
import com.kecheng.market.auth.vo.LoginResponse;
import com.kecheng.market.common.exception.BusinessException;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.security.model.LoginUser;
import com.kecheng.market.security.util.JwtUtil;
import com.kecheng.market.user.entity.UserEntity;
import com.kecheng.market.user.vo.UserProfileVo;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider,
            JwtUtil jwtUtil) {
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (useMysql()) {
            UserEntity user = requirePersistence().findUserByUsernameOrStudentNo(request.username());
            if ("disabled".equalsIgnoreCase(user.getStatus())) {
                throw new BusinessException(403, "Account is disabled");
            }
            if (!user.getPassword().equals(request.password())) {
                throw new BusinessException(400, "Account or password is incorrect");
            }
            UserProfileVo profile = requirePersistence().getUserProfile(user.getId());
            String token = jwtUtil.generateToken(new LoginUser(user.getId(), user.getUsername(), user.getRealName(), user.getRole()));
            return new LoginResponse(token, profile);
        }

        MarketStore.UserData user = requireStore().findUserByUsernameOrStudentNo(request.username());
        if (user.disabled) {
            throw new BusinessException(403, "Account is disabled");
        }
        if (!user.password.equals(request.password())) {
            throw new BusinessException(400, "Account or password is incorrect");
        }
        UserProfileVo profile = requireStore().getUserProfile(user.id);
        String token = jwtUtil.generateToken(new LoginUser(user.id, user.username, user.name, user.role));
        return new LoginResponse(token, profile);
    }

    @Override
    public UserProfileVo register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(400, "Passwords do not match");
        }
        if (useMysql()) {
            return requirePersistence().registerUser(request.username(), request.password(), request.nickname());
        }
        long timestamp = System.currentTimeMillis();
        String studentNo = "KC" + timestamp;
        String phone = "13" + String.format("%09d", timestamp % 1_000_000_000L);
        return requireStore().registerUser(request.username(), request.password(), request.nickname(), studentNo, phone);
    }

    private boolean useMysql() {
        return "mysql".equalsIgnoreCase(storageMode);
    }

    private MarketPersistenceService requirePersistence() {
        if (persistenceService == null) {
            throw new IllegalStateException("MySQL persistence service is not available");
        }
        return persistenceService;
    }

    private MarketStore requireStore() {
        if (marketStore == null) {
            throw new IllegalStateException("Memory store is not available");
        }
        return marketStore;
    }
}
