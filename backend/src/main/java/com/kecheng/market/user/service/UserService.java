package com.kecheng.market.user.service;

import com.kecheng.market.user.vo.UserProfileVo;

public interface UserService {
    UserProfileVo currentUser(Long userId);

    UserProfileVo updateProfile(Long userId, String name, String phone, String qq, String slogan);
}
