package com.kecheng.market.auth.service;

import com.kecheng.market.auth.dto.LoginRequest;
import com.kecheng.market.auth.dto.RegisterRequest;
import com.kecheng.market.auth.vo.LoginResponse;
import com.kecheng.market.user.vo.UserProfileVo;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    UserProfileVo register(RegisterRequest request);
}
