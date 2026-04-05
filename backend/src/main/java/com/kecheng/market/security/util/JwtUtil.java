package com.kecheng.market.security.util;

import com.kecheng.market.common.exception.UnauthorizedException;
import com.kecheng.market.security.model.LoginUser;
import com.kecheng.market.security.properties.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(LoginUser loginUser) {
        long expiresAt = System.currentTimeMillis() + jwtProperties.getExpireHours() * 3600_000;
        String payload = String.join("|", String.valueOf(loginUser.userId()), safe(loginUser.username()), safe(loginUser.displayName()), safe(loginUser.role()), String.valueOf(expiresAt));
        String signature = sign(payload);
        return Base64.getUrlEncoder().withoutPadding().encodeToString((payload + "|" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public LoginUser parseToken(String token) {
        try {
            String plain = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = plain.split("\\|", 6);
            if (parts.length != 6) {
                throw new UnauthorizedException("登录凭证格式不正确");
            }
            String payload = String.join("|", parts[0], parts[1], parts[2], parts[3], parts[4]);
            if (!sign(payload).equals(parts[5])) {
                throw new UnauthorizedException("登录凭证签名校验失败");
            }
            if (System.currentTimeMillis() > Long.parseLong(parts[4])) {
                throw new UnauthorizedException("登录凭证已过期，请重新登录");
            }
            return new LoginUser(Long.parseLong(parts[0]), restore(parts[1]), restore(parts[2]), restore(parts[3]));
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("登录凭证无法解析");
        }
    }

    private String sign(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((payload + "|" + jwtProperties.getSecret()).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 算法不可用", exception);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("|", "");
    }

    private String restore(String value) {
        return value == null ? "" : value;
    }
}
