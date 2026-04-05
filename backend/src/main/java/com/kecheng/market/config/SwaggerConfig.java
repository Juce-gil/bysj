package com.kecheng.market.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI marketOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("科成校园跳蚤市场后端接口")
                        .version("v1.1.0")
                        .description("当前默认使用内存数据驱动接口骨架，已预留 MyBatis-Plus / MySQL 接入结构。")
                        .contact(new Contact().name("backend-agent").email("backend@example.com")));
    }
}
