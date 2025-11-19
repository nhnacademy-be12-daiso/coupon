package com.nhnacademy.coupon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/coupons/test")
public class CouponIssueTestController {

    @GetMapping
    public String test(){
        return "test";
    }
}
