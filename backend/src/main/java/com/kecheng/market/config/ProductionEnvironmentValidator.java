package com.kecheng.market.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("prod")
public class ProductionEnvironmentValidator implements ApplicationRunner {

    private final String storageMode;
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final String jwtSecret;

    public ProductionEnvironmentValidator(
            @Value("${market.storage-mode:mysql}") String storageMode,
            @Value("${DB_URL:${spring.datasource.url:}}") String dbUrl,
            @Value("${DB_USERNAME:${spring.datasource.username:}}") String dbUsername,
            @Value("${DB_PASSWORD:${spring.datasource.password:}}") String dbPassword,
            @Value("${JWT_SECRET:${market.jwt.secret:}}") String jwtSecret) {
        this.storageMode = storageMode;
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.jwtSecret = jwtSecret;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> errors = new ArrayList<>();
        if (!"mysql".equalsIgnoreCase(storageMode)) {
            errors.add("生产环境必须使用 mysql 持久化模式");
        }
        validateRequired("DB_URL", dbUrl, errors);
        validateRequired("DB_USERNAME", dbUsername, errors);
        validateRequired("DB_PASSWORD", dbPassword, errors);
        validateRequired("JWT_SECRET", jwtSecret, errors);
        validatePlaceholder("JWT_SECRET", jwtSecret, List.of("your-production-jwt-secret-key-2025", "change-this", "your-secure"), errors);
        validatePlaceholder("DB_PASSWORD", dbPassword, List.of("market_password", "change_this_password", "your_secure_password"), errors);

        if (!errors.isEmpty()) {
            throw new IllegalStateException("生产环境配置校验失败: " + String.join("；", errors));
        }
    }

    private void validateRequired(String name, String value, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            errors.add(name + " 未配置");
        }
    }

    private void validatePlaceholder(String name, String value, List<String> placeholders, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        String normalized = value.trim().toLowerCase();
        for (String placeholder : placeholders) {
            if (normalized.contains(placeholder.toLowerCase())) {
                errors.add(name + " 仍在使用示例占位值");
                return;
            }
        }
    }
}
