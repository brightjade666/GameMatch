package com.donghaoyu.sprintpro.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

//定义内部写的为接口
@RestController
public class FirstController {
    @GetMapping("/index1")
    public String index1(){
      return "index";
    }

    @PostMapping("/index2")
    public String index2(){
        return "index2";
    }

    @PutMapping("/index2")
    public String index3(){
        return "index3";
    }

    @PutMapping("/index4")
    public String index4(){
        return "index4";
    }

    @PutMapping("/index5")
    public String index5(){
        return "index5";
    }
}
