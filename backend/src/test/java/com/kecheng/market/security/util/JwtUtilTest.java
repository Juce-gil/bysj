package com.kecheng.market.security.util;

import com.kecheng.market.common.exception.UnauthorizedException;
import com.kecheng.market.security.model.LoginUser;
import com.kecheng.market.security.properties.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    @Test
    void generateAndParseTokenRoundTripsLoginUser() {
        JwtUtil jwtUtil = new JwtUtil(jwtProperties());
        LoginUser loginUser = new LoginUser(7L, "alice", "Alice", "admin");

        String token = jwtUtil.generateToken(loginUser);
        LoginUser parsedUser = jwtUtil.parseToken(token);

        assertThat(parsedUser).isEqualTo(loginUser);
    }

    @Test
    void parseTokenRejectsTamperedPayload() {
        JwtUtil jwtUtil = new JwtUtil(jwtProperties());
        String token = jwtUtil.generateToken(new LoginUser(8L, "bob", "Bob", "admin"));
        String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String tamperedDecoded = decoded.replace("|admin|", "|user|");
        String tamperedToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tamperedDecoded.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> jwtUtil.parseToken(tamperedToken))
                .isInstanceOf(UnauthorizedException.class);
    }

    private JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret");
        properties.setExpireHours(2L);
        return properties;
    }
}
