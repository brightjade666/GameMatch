package com.dong.springboot.controller;

import com.dong.springboot.common.Result;
import com.dong.springboot.annotation.LogExecutionTime;
import com.dong.springboot.service.MatchService;
import com.dong.springboot.vo.MatchQueryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class MatchController {

    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

    @Autowired
    private MatchService matchService;

    @GetMapping("/matchAdvanced")
    @LogExecutionTime
    public Result matchAdvanced(MatchQueryDTO query) {
        log.info("鎺ユ敹鍒扮殑鍖归厤鍙傛暟锛歿}", query);
        try {
            List<Map<String, Object>> list = matchService.matchUsers(query);
            return Result.success(list);
        } catch (Exception e) {
            log.error("鍖归厤澶辫触", e);
            return Result.error("鍖归厤澶辫触");
        }
    }
}
