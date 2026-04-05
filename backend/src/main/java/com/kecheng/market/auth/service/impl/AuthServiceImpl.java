package com.kecheng.market.auth.service.impl;

import com.kecheng.market.auth.dto.LoginRequest;
import com.kecheng.market.auth.dto.RegisterRequest;
import com.kecheng.market.auth.service.AuthService;
import com.kecheng.market.auth.vo.LoginResponse;
import com.kecheng.market.common.exception.BusinessException;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.security.model.LoginUser;
import com.kecheng.market.security.util.JwtUtil;
import com.kecheng.market.user.vo.UserProfileVo;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final MarketStore marketStore;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(MarketStore marketStore, JwtUtil jwtUtil) {
        this.marketStore = marketStore;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        MarketStore.UserData userData = marketStore.findUserByUsernameOrStudentNo(request.username());
        if (userData.disabled) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!userData.password.equals(request.password())) {
            throw new BusinessException(400, "账号或密码错误");
        }
        UserProfileVo profile = marketStore.getUserProfile(userData.id);
        String token = jwtUtil.generateToken(new LoginUser(userData.id, userData.username, userData.name, userData.role));
        return new LoginResponse(token, profile);
    }

    @Override
    public UserProfileVo register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
        String studentNo = "KC" + System.currentTimeMillis();
        String phone = "13" + String.valueOf(System.currentTimeMillis()).substring(5, 14);
        return marketStore.registerUser(request.username(), request.password(), request.nickname(), studentNo, phone);
    }
}
