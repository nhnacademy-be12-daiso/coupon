package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.request.issue.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponIssueController.class)
class CouponIssueControllerTest {

    @MockitoBean(name = "jpaAuditingHandler")
    Object jpaAuditingHandler;

    @MockitoBean(name = "jpaMappingContext")
    Object jpaMappingContext;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponPolicyService couponIssueService;

    @Test
    @DisplayName("POST /api/coupons/{couponPolicyId}/download - 쿠폰 발급 성공: 201 반환 + 서비스 호출")
    void downloadCoupon_returns201() throws Exception {
        // given
        Long userId = 100L;
        Long couponPolicyId = 1L;

        Mockito.when(couponIssueService.issueCoupon(eq(userId), any(UserCouponIssueRequest.class)))
                .thenReturn(Mockito.mock(UserCouponResponse.class));

        // when & then
        mockMvc.perform(post("/api/coupons/{couponPolicyId}/download", couponPolicyId)
                        .header("X-User-Id", userId))
                .andExpect(status().isCreated());

        Mockito.verify(couponIssueService)
                .issueCoupon(eq(userId), any(UserCouponIssueRequest.class));
    }

    @Test
    @DisplayName("POST /api/coupons/{couponPolicyId}/download - X-User-Id 헤더 없으면 400")
    void downloadCoupon_withoutUserHeader_returns400() throws Exception {
        Long couponPolicyId = 1L;

        mockMvc.perform(post("/api/coupons/{couponPolicyId}/download", couponPolicyId))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(couponIssueService);
    }

    @Test
    @DisplayName("POST /api/coupons/{couponPolicyId}/download - couponPolicyId 없으면 404")
    void downloadCoupon_withoutPathVariable_returns404() throws Exception {
        Long userId = 100L;

        // pathVariable 빠진 URL은 매핑 자체가 안 됨
        mockMvc.perform(post("/api/coupons//download")
                        .header("X-User-Id", userId))
                .andExpect(status().is5xxServerError());

        Mockito.verifyNoInteractions(couponIssueService);
    }
}
