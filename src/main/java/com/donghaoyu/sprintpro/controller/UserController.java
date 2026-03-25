package com.donghaoyu.sprintpro.controller;

import com.donghaoyu.sprintpro.entity.TbUser;
import com.donghaoyu.sprintpro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 查询所有
    @GetMapping("/findAll")
    public List<TbUser> findAll() {
        return userService.findAll();
    }

    // 根据ID查询
    @GetMapping("/findById/{id}")
    public TbUser findById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    // 新增
    @PostMapping("/save")
    public TbUser save(@RequestBody TbUser user) {
        return userService.save(user);
    }

    // 删除
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        userService.delete(id);
        return "删除成功";
    }
}