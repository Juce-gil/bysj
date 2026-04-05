package com.kecheng.market.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "market.jwt")
public class JwtProperties {

    private String secret;
    private Long expireHours = 24L;
    private String tokenHeader = "Authorization";
    private String tokenPrefix = "Bearer ";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpireHours() {
        return expireHours;
    }

    public void setExpireHours(Long expireHours) {
        this.expireHours = expireHours;
    }

    public String getTokenHeader() {
        return tokenHeader;
    }

    public void setTokenHeader(String tokenHeader) {
        this.tokenHeader = tokenHeader;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
}
