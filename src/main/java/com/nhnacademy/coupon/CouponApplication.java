package com.nhnacademy.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
public class CouponApplication {

	public static void main(String[] args)   {
		SpringApplication.run(CouponApplication.class, args);
	}

}