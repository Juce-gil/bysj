package com.kecheng.market.auth.vo;

import com.kecheng.market.user.vo.UserProfileVo;

public record LoginResponse(String token, UserProfileVo profile) {
}
