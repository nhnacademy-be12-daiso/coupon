package com.nhnacademy.coupon.controller;

import com.nhnacademy.coupon.annotation.CurrentUserId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupons")
public class TestController {

    @GetMapping("/hello")
    public String test(@CurrentUserId Long userId){
        return "인증성공 토큰에서 추출한 User ID: " + userId;
    }
}
