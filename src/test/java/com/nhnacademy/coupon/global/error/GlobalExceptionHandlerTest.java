package com.nhnacademy.coupon.global.error;

import com.nhnacademy.coupon.domain.coupon.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test/path");
    }

    @Test
    @DisplayName("CouponPolicyNotFoundException - 404 응답")
    void handleNotFound_couponPolicyNotFound() {
        var exception = new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다");

        var response = exceptionHandler.handleNotFound(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("쿠폰 정책을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("UserCouponNotFoundException - 404 응답")
    void handleNotFound_userCouponNotFound() {
        var exception = new UserCouponNotFoundException(1L);

        var response = exceptionHandler.handleNotFound(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("쿠폰");
    }

    @Test
    @DisplayName("InvalidCouponException - 400 응답")
    void handleInvalidCoupon() {
        var exception = new InvalidCouponException("만료된 쿠폰입니다");

        var response = exceptionHandler.handleInvalidCoupon(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("만료된 쿠폰입니다");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException - 400 응답 + 필드 에러")
    void handleValidationExceptions() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("object", "field", "필수 값입니다");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        var exception = new MethodArgumentNotValidException(null, bindingResult);

        var response = exceptionHandler.handleValidationExceptions(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("요청 값 검증에 실패했습니다.");
    }

    @Test
    @DisplayName("MissingRequestHeaderException - 400 응답")
    void handleMissingHeader() {
        var exception = new MissingRequestHeaderException("X-User-Id", null);

        var response = exceptionHandler.handleMissingHeader(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("필수 헤더가 누락되었습니다");
    }

    @Test
    @DisplayName("HttpMessageNotReadableException - 400 응답")
    void handleJsonParseError() {
        var exception = mock(HttpMessageNotReadableException.class);

        var response = exceptionHandler.handleJsonParseError(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("요청 본문(JSON)을 읽을 수 없습니다.");
    }

    @Test
    @DisplayName("DuplicateCouponException - 409 응답")
    void handleDuplicateCoupon() {
        var exception = new DuplicateCouponException("이미 발급된 쿠폰입니다");

        var response = exceptionHandler.handleDuplicateCoupon(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).isEqualTo("이미 발급된 쿠폰입니다");
    }

    @Test
    @DisplayName("Exception - 500 응답")
    void handleException() {
        var exception = new RuntimeException("예상치 못한 에러");

        var response = exceptionHandler.handleException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("예상치 못한 오류가 발생했습니다.");
    }


}