package com.dong.springboot.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FirstController {
    // 娴嬭瘯鎺ュ彛锛歨ttp://localhost:8081/hello
    @GetMapping("/hello")
    public String hello() {
        return "SpringBoot 鍚姩鎴愬姛锛佹帴鍙ｆ甯歌繍琛岋綖";
    }
}


//分成基础，核心，易错题。