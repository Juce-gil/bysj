package com.kecheng.market.security.context;

import com.kecheng.market.security.model.LoginUser;

public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        return HOLDER.get() == null ? null : HOLDER.get().userId();
    }

    public static String getRole() {
        return HOLDER.get() == null ? null : HOLDER.get().role();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
