package com.dong.springboot.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FirstController {
    // 测试接口：http://localhost:8081/hello
    @GetMapping("/hello")
    public String hello() {
        return "SpringBoot 启动成功！接口正常运行～";
    }
}
