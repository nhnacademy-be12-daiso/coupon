package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyUpdateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.policy.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponPolicyController.class)
class CouponPolicyControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CouponPolicyService couponPolicyService;

    @Test
    @DisplayName("POST /api/coupons/policies - 쿠폰 정책 생성: 201 반환 + 서비스 호출")
    void createCoupon_returns201() throws Exception{
        // given
        Mockito.when(couponPolicyService.createCouponPolicy(any(CouponPolicyCreateRequest.class)))
                .thenReturn(Mockito.mock(CouponPolicyResponse.class));
        String body = """
        {
          "couponPolicyName": "테스트 쿠폰",
          "couponType": "WELCOME",
          "discountWay": "FIXED",
          "discountAmount": 10000,
          "minOrderAmount": 50000,
          "maxDiscountAmount": 0,
          "validStartDate": null,
          "validEndDate": null,
          "validDays": 30,
          "quantity": null,
          "couponPolicyStatus": "ACTIVE"
        }
        """;
        // when & then
        mockMvc.perform(post("/api/coupons/policies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isCreated());

        Mockito.verify(couponPolicyService).createCouponPolicy(any(CouponPolicyCreateRequest.class));
    }

    @Test
    @DisplayName("POST /api/coupons/policies - 유효성 실패면 400 + 서비스 호출 안 함")
    void createCoupon_invalid_returns400() throws Exception {
        // given: 빈 JSON -> @Valid에 걸리게 의도
        String body = "{}";

        // when & then
        mockMvc.perform(post("/api/coupons/policies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());

        // 서비스 호출되면 안됨
        Mockito.verifyNoInteractions(couponPolicyService);
    }

    @Test
    @DisplayName("GET /api/coupons/policies - 정책 목록 조회: 200 반환 + 서비스 호출")
    void getPolicies_returns200() throws Exception {
        // given
        Mockito.when(couponPolicyService.couponPolices())
                .thenReturn(List.of(Mockito.mock(CouponPolicyResponse.class)));

        // when & then
        mockMvc.perform(get("/api/coupons/policies"))
                .andExpect(status().isOk());

        Mockito.verify(couponPolicyService).couponPolices();
    }


    @Test
    @DisplayName("GET /api/coupons/policies/{id} - 단일 조회: 200 반환 + 서비스 호출")
    void getPolicyDetail_returns200() throws Exception {
        // given
        Long id = 1L;
        Mockito.when(couponPolicyService.couponPolicyDetail(id))
                .thenReturn(Mockito.mock(CouponPolicyResponse.class));

        // when & then
        mockMvc.perform(get("/api/coupons/policies/{id}", id))
                .andExpect(status().isOk());

        Mockito.verify(couponPolicyService).couponPolicyDetail(id);

    }

    @Test
    @DisplayName("PUT /api/coupons/policies/{id} - 정책 수정: 200 반환 + 서비스 호출")
    void updatePolicy_returns200() throws Exception {
        // given
        Long id = 1L;
        Mockito.when(couponPolicyService.updateCouponPolicy(eq(id), any(CouponPolicyUpdateRequest.class)))
                .thenReturn(Mockito.mock(CouponPolicyResponse.class));

        String body = """
        {
          "couponPolicyName": "수정된 쿠폰",
          "couponType": "WELCOME",
          "discountWay": "FIXED",
          "discountAmount": 10000,
          "policyStatus": "ACTIVE"
        }
        """;

        // when & then
        mockMvc.perform(put("/api/coupons/policies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        Mockito.verify(couponPolicyService).updateCouponPolicy(eq(id), any(CouponPolicyUpdateRequest.class));
    }

}