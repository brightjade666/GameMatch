package com.dong.springboot.interceptor;

import com.dong.springboot.dto.LoginUserCacheDTO;
import com.dong.springboot.service.LoginTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private LoginTokenService loginTokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isBlank()) {
            token = request.getHeader("token");
        }

        LoginUserCacheDTO user = loginTokenService.getLoginUser(token);
        if (user == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
            return false;
        }

        request.setAttribute("loginUser", user);
        request.setAttribute("loginToken", token);
        loginTokenService.refreshToken(token);
        return true;
    }
}
