package com.nhnacademy.coupon.global.notify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoorayNotifier {

    private final RestClient restClient = RestClient.create();

    @Value("${dooray.webhook-url:}")
    private String webhookUrl;

    @Value("${dooray.enabled:true}")
    private boolean enabled;

    public void send(String text) {
        if (!enabled) return;

        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("[Dooray] webhookUrl is empty. skip send. text={}", text);
            return;
        }

        try {
            // Dooray Incoming Webhook은 보통 {"text":"..."} 형태로 받음
            DoorayMessage payload = new DoorayMessage(text);

            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            // 알림 실패가 비즈니스 로직을 깨면 안됨
            log.error("[Dooray] send failed. error={}", e.getMessage(), e);
        }
    }

    // payload DTO
    private record DoorayMessage(String text) {}
}
