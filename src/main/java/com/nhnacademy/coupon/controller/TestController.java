package com.nhnacademy.coupon.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupons")
public class TestController {

    @GetMapping("/hello")
    public String test(){
        return "test";
    }
}
