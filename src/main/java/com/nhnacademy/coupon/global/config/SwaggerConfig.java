package com.nhnacademy.coupon.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme userIdHeaderScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(USER_ID_HEADER)
                .description("요청 사용자 식별 헤더");

        return new OpenAPI()
                .info(new Info()
                        .title("Coupon API")
                        .description("쿠폰 관리 시스템 API 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("daiso-book 3조")
                                .email("team@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 서버"),
                        new Server().url("https://daiso-book.shop").description("운영 서버")
                ))
                .components(new Components().addSecuritySchemes(USER_ID_HEADER, userIdHeaderScheme))
                .addSecurityItem(new SecurityRequirement().addList(USER_ID_HEADER));
    }
}
