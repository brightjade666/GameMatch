package com.dong.springboot.controller;

import com.dong.springboot.entity.User;
import com.dong.springboot.service.LoginTokenService;
import com.dong.springboot.service.LoginTokenService.TokenPair;
import com.dong.springboot.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class UserController {

    private final UserService userService;
    private final LoginTokenService loginTokenService;

    public UserController(UserService userService, LoginTokenService loginTokenService) {
        this.userService = userService;
        this.loginTokenService = loginTokenService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        String username = user.getUsername();
        String password = user.getPassword();
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            result.put("code", 500);
            result.put("msg", "用户名或密码不能为空");
            return result;
        }

        User loginUser = userService.findByUsername(username);
        if (loginUser == null || !password.equals(loginUser.getPassword())) {
            result.put("code", 500);
            result.put("msg", "用户名或密码错误");
            return result;
        }

        if (loginUser.getStatus() != null && loginUser.getStatus() == 0) {
            result.put("code", 500);
            result.put("msg", "账号已被禁用，请联系管理员");
            return result;
        }

        TokenPair tokenPair = loginTokenService.issueTokenPair(loginUser);
        result.put("code", 200);
        result.put("msg", "登录成功");
        result.put("user_id", loginUser.getUserId());
        result.put("username", loginUser.getUsername());
        result.put("role", loginUser.getRole());
        result.put("token", tokenPair.getAccessToken());
        result.put("access_token", tokenPair.getAccessToken());
        result.put("refresh_token", tokenPair.getRefreshToken());
        result.put("access_expires_in", tokenPair.getAccessTtl().getSeconds());
        result.put("refresh_expires_in", tokenPair.getRefreshTtl().getSeconds());
        return result;
    }

    @PostMapping("/refresh-token")
    public Map<String, Object> refreshToken(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String refreshToken = resolveRefreshToken(request);
        TokenPair tokenPair = loginTokenService.refreshToken(refreshToken);
        if (tokenPair == null) {
            result.put("code", 401);
            result.put("msg", "刷新失败，请重新登录");
            return result;
        }

        result.put("code", 200);
        result.put("msg", "刷新成功");
        result.put("access_token", tokenPair.getAccessToken());
        result.put("refresh_token", tokenPair.getRefreshToken());
        result.put("access_expires_in", tokenPair.getAccessTtl().getSeconds());
        result.put("refresh_expires_in", tokenPair.getRefreshTtl().getSeconds());
        return result;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String token = resolveAccessToken(request);
        if (token == null || token.isBlank()) {
            token = resolveRefreshToken(request);
        }
        loginTokenService.removeToken(token);
        result.put("code", 200);
        result.put("msg", "退出成功");
        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        if (user.getUsername() == null || user.getPassword() == null) {
            result.put("code", 500);
            result.put("msg", "用户名或密码不能为空");
            return result;
        }

        User exist = userService.findByUsername(user.getUsername());
        if (exist != null) {
            result.put("code", 500);
            result.put("msg", "用户名已存在");
            return result;
        }

        userService.save(user);
        result.put("code", 200);
        result.put("msg", "注册成功，请登录");
        return result;
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        token = request.getHeader("token");
        if (token != null && !token.isBlank()) {
            return token;
        }
        return request.getHeader("X-Access-Token");
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        String token = request.getHeader("X-Refresh-Token");
        if (token != null && !token.isBlank()) {
            return token;
        }
        token = request.getHeader("refreshToken");
        if (token != null && !token.isBlank()) {
            return token;
        }
        return request.getParameter("refreshToken");
    }
}
