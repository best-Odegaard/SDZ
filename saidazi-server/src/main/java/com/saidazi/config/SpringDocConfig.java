package com.saidazi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI / Swagger 配置
 *
 * 访问地址：http://localhost:8080/swagger-ui/index.html
 * JSON：    http://localhost:8080/v3/api-docs
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("赛搭子 API 接口文档")
                        .version("1.0.0")
                        .description("赛搭子 —— 高校赛事信息聚合与组队平台后端接口\n\n" +
                                "前端：微信小程序 ｜ 后端：Spring Boot 3.2\n\n" +
                                "**当前状态：所有接口暂不校验登录态，userId 直接传参**")
                        .contact(new Contact()
                                .name("赛搭子团队")
                                .email("support@saidazi.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
