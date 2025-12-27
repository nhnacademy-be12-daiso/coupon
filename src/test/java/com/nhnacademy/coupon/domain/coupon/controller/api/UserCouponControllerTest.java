package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.request.usage.BatchCouponUseRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponCancelRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.SingleCouponApplyRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.usage.SingleCouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.UserCouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCouponController.class)
class UserCouponControllerTest {

    @MockitoBean(name = "jpaAuditingHandler")
    Object jpaAuditingHandler;

    @MockitoBean(name = "jpaMappingContext")
    Object jpaMappingContext;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserCouponService userCouponService;

    @Test
    @DisplayName("GET /api/coupons/users - 내 쿠폰 목록 조회: 200 + 서비스 호출")
    void getUserCoupons_returns200() throws Exception {
        Long userId = 100L;

        Mockito.when(userCouponService.getUserCoupons(userId))
                .thenReturn(List.of(Mockito.mock(UserCouponResponse.class)));

        mockMvc.perform(get("/api/coupons/users")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());

        verify(userCouponService).getUserCoupons(userId);
    }

    @Test
    @DisplayName("GET /api/coupons/users - X-User-Id 없으면 400")
    void getUserCoupons_withoutHeader_returns400() throws Exception {
        mockMvc.perform(get("/api/coupons/users"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(userCouponService);
    }

    @Test
    @DisplayName("GET /api/coupons/available - bookId 없으면(마이페이지/전체) 200 + 서비스 호출")
    void getAvailableCoupons_withoutBookId_returns200() throws Exception {
        Long userId = 100L;

        Mockito.when(userCouponService.getAvailableCoupons(userId, null))
                .thenReturn(List.of(Mockito.mock(UserCouponResponse.class)));

        mockMvc.perform(get("/api/coupons/available")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());

        verify(userCouponService).getAvailableCoupons(userId, null);
    }

    @Test
    @DisplayName("GET /api/coupons/available - bookId 있으면 200 + 서비스 호출")
    void getAvailableCoupons_withBookId_returns200() throws Exception {
        Long userId = 100L;
        Long bookId = 200L;

        Mockito.when(userCouponService.getAvailableCoupons(userId, bookId))
                .thenReturn(List.of(Mockito.mock(UserCouponResponse.class)));

        mockMvc.perform(get("/api/coupons/available")
                        .header("X-User-Id", userId)
                        .param("bookId", String.valueOf(bookId)))
                .andExpect(status().isOk());

        verify(userCouponService).getAvailableCoupons(userId, bookId);
    }

    @Test
    @DisplayName("POST /api/coupons/calculate - 단일 쿠폰 적용 계산: 200 + 서비스 호출")
    void calculateSingleCoupon_returns200() throws Exception {
        Long userId = 100L;

        Mockito.when(userCouponService.calculateSingleCoupon(eq(userId), any(SingleCouponApplyRequest.class)))
                .thenReturn(Mockito.mock(SingleCouponApplyResponse.class));

        String body = """
                {
                  "bookId": 200,
                  "bookPrice":3000,
                  "quantity": 2,
                  "userCouponId": 300
                }
                """;

        mockMvc.perform(post("/api/coupons/calculate")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(userCouponService).calculateSingleCoupon(eq(userId), any(SingleCouponApplyRequest.class));
    }

    @Test
    @DisplayName("POST /api/coupons/calculate - 바디가 @Valid에 걸리면 400 + 서비스 호출 안 함")
    void calculateSingleCoupon_invalid_returns400() throws Exception {
        Long userId = 100L;

        mockMvc.perform(post("/api/coupons/calculate")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(userCouponService);
    }

    @Test
    @DisplayName("POST /api/coupons/use-batch - 쿠폰 일괄 사용: 200 + 서비스 호출")
    void useCouponsBatch_returns200() throws Exception {
        Long userId = 100L;

        // void 메서드는 stubbing 필요 없음 (예외만 안 던지면 OK)

        String body = """
        {
          "orderId": 999,
          "items": [
            {
              "userCouponId": 1,
              "bookId": 200
            }
          ]
        }
        """;

        mockMvc.perform(post("/api/coupons/use-batch")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(userCouponService).useCoupons(eq(userId), any(BatchCouponUseRequest.class));
    }

    @Test
    @DisplayName("POST /api/coupons/use-cancel - 쿠폰 사용 취소: 200 + 서비스 호출")
    void cancelCoupons_returns200() throws Exception {
        Long userId = 100L;

        String body = """
        {
          "orderId": 999,
          "cancel" : "cancel reason",
          "userCouponIds": [
            1,2,3
          ]
        }
        """;

        mockMvc.perform(post("/api/coupons/use-cancel")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(userCouponService).cancelCouponUsage(eq(userId), any(CouponCancelRequest.class));
    }
}