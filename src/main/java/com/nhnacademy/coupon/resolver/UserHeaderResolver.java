package com.nhnacademy.coupon.resolver;

import com.nhnacademy.coupon.annotation.CurrentUserId;
import com.nhnacademy.coupon.util.JwtUtil;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserHeaderResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    public UserHeaderResolver(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @CurrentUserId가 붙어있고, 타입이 Long인 파라미터만 처리
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // NativeWebRequest: HttpServletRequest보다 조금 더 포장된 객체이다.
        // 1. 헤더에서 토큰 꺼내기
        String authorizationHeader = webRequest.getHeader("Authorization");

        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            return null;
        }

        String token = authorizationHeader.substring(7);

        return jwtUtil.getUserId(token);

    }
}
