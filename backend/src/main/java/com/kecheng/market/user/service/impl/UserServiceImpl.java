package com.kecheng.market.user.service.impl;

import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.user.service.UserService;
import com.kecheng.market.user.vo.UserProfileVo;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final MarketStore marketStore;

    public UserServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public UserProfileVo currentUser(Long userId) {
        return marketStore.getUserProfile(userId);
    }

    @Override
    public UserProfileVo updateProfile(Long userId, String name, String phone, String qq, String slogan) {
        return marketStore.updateUserProfile(userId, name, phone, qq, slogan);
    }
}
