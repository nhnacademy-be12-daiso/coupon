package com.nhnacademy.coupon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/coupons")
public class TestController {

    @GetMapping("/hello")
    public String test(){
        return "test";
    }
}
