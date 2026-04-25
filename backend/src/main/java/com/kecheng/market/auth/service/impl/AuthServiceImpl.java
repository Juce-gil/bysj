package com.kecheng.market.auth.service.impl;

import com.kecheng.market.auth.dto.LoginRequest;
import com.kecheng.market.auth.dto.RegisterRequest;
import com.kecheng.market.auth.service.AuthService;
import com.kecheng.market.auth.vo.LoginResponse;
import com.kecheng.market.common.exception.BusinessException;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import com.kecheng.market.security.model.LoginUser;
import com.kecheng.market.security.util.JwtUtil;
import com.kecheng.market.user.entity.UserEntity;
import com.kecheng.market.user.vo.UserProfileVo;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final StorageAccessSupport storage;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider,
            JwtUtil jwtUtil) {
        this.storage = new StorageAccessSupport(
                storageMode,
                marketStoreProvider.getIfAvailable(),
                persistenceServiceProvider.getIfAvailable()
        );
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (storage.useMysql()) {
            UserEntity user = storage.mysqlStore().findUserByUsernameOrStudentNo(request.username());
            if ("disabled".equalsIgnoreCase(user.getStatus())) {
                throw new BusinessException(403, "\u8d26\u53f7\u5df2\u88ab\u7981\u7528");
            }
            if (!user.getPassword().equals(request.password())) {
                throw new BusinessException(400, "\u8d26\u53f7\u6216\u5bc6\u7801\u9519\u8bef");
            }
            UserProfileVo profile = storage.mysqlStore().getUserProfile(user.getId());
            String token = jwtUtil.generateToken(new LoginUser(user.getId(), user.getUsername(), user.getRealName(), user.getRole()));
            return new LoginResponse(token, profile);
        }

        MarketStore.UserData user = storage.memoryStore().findUserByUsernameOrStudentNo(request.username());
        if (user.disabled) {
            throw new BusinessException(403, "\u8d26\u53f7\u5df2\u88ab\u7981\u7528");
        }
        if (!user.password.equals(request.password())) {
            throw new BusinessException(400, "\u8d26\u53f7\u6216\u5bc6\u7801\u9519\u8bef");
        }
        UserProfileVo profile = storage.memoryStore().getUserProfile(user.id);
        String token = jwtUtil.generateToken(new LoginUser(user.id, user.username, user.name, user.role));
        return new LoginResponse(token, profile);
    }

    @Override
    public UserProfileVo register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(400, "\u4e24\u6b21\u8f93\u5165\u7684\u5bc6\u7801\u4e0d\u4e00\u81f4");
        }
        if (storage.useMysql()) {
            return storage.mysqlStore().registerUser(request.username(), request.password(), request.nickname());
        }
        long timestamp = System.currentTimeMillis();
        String studentNo = "KC" + timestamp;
        String phone = "13" + String.format("%09d", timestamp % 1_000_000_000L);
        return storage.memoryStore().registerUser(request.username(), request.password(), request.nickname(), studentNo, phone);
    }
}
