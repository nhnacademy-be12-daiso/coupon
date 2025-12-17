package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.query.BookCouponQuery;
import com.nhnacademy.coupon.domain.coupon.dto.response.book.BookCategoryResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon.CategoryCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import com.nhnacademy.coupon.global.client.BookServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryCouponController.class)
class CategoryCouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookServiceClient bookServiceClient;

    @MockitoBean
    private CouponPolicyService couponPolicyService;

    @Test
    @DisplayName("GET /api/coupons/books/{bookId}/downloadable - 다운로드 가능한 쿠폰 조회: 200 + 두 서비스 호출 + Query 값 검증")
    void getDownloadableCoupons_returns200_andBuildsQueryCorrectly() throws Exception {
        // given
        long bookId = 200L;
        long userId = 100L;

        BookCategoryResponse bookCategory = new BookCategoryResponse(bookId, 10L, 20L);
        Mockito.when(bookServiceClient.getBookCategory(bookId)).thenReturn(bookCategory);

        Mockito.when(couponPolicyService.getAvailableCouponsForBook(any(BookCouponQuery.class)))
                .thenReturn(List.of(Mockito.mock(CategoryCouponResponse.class)));

        // when & then
        mockMvc.perform(get("/api/coupons/books/{bookId}/downloadable", bookId)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());

        // bookServiceClient 호출 검증
        Mockito.verify(bookServiceClient).getBookCategory(bookId);

        // couponPolicyService에 넘어간 query 검증
        ArgumentCaptor<BookCouponQuery> captor = ArgumentCaptor.forClass(BookCouponQuery.class);
        Mockito.verify(couponPolicyService).getAvailableCouponsForBook(captor.capture());

        BookCouponQuery passed = captor.getValue();
        assertThat(passed.getUserId()).isEqualTo(userId);
        assertThat(passed.getBookId()).isEqualTo(bookId);
        assertThat(passed.getPrimaryCategoryId()).isEqualTo(10L);
        assertThat(passed.getSecondaryCategoryId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("GET /api/coupons/books/{bookId}/downloadable - X-User-Id 없으면 400 + 서비스 호출 없음")
    void getDownloadableCoupons_withoutHeader_returns400() throws Exception {
        long bookId = 200L;

        mockMvc.perform(get("/api/coupons/books/{bookId}/downloadable", bookId))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookServiceClient);
        Mockito.verifyNoInteractions(couponPolicyService);
    }

    @Test
    @DisplayName("GET /api/coupons/books/{bookId}/downloadable - 카테고리 null이어도 200 (Query에 null 전달)")
    void getDownloadableCoupons_categoryNull_stillOk() throws Exception {
        long bookId = 200L;
        long userId = 100L;

        // primary/secondary 둘 다 null
        Mockito.when(bookServiceClient.getBookCategory(bookId))
                .thenReturn(new BookCategoryResponse(bookId, null, null));

        Mockito.when(couponPolicyService.getAvailableCouponsForBook(any(BookCouponQuery.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/coupons/books/{bookId}/downloadable", bookId)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());

        ArgumentCaptor<BookCouponQuery> captor = ArgumentCaptor.forClass(BookCouponQuery.class);
        Mockito.verify(couponPolicyService).getAvailableCouponsForBook(captor.capture());

        BookCouponQuery passed = captor.getValue();
        assertThat(passed.getPrimaryCategoryId()).isNull();
        assertThat(passed.getSecondaryCategoryId()).isNull();
    }
}
