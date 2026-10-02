package com.dong.springboot.controller;

import com.dong.springboot.mapper.UserProfileMapper;
import com.dong.springboot.entity.UserProfile;
import com.dong.springboot.service.UserInfoService;
import com.dong.springboot.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
// 馃憞 鍒犳帀鎵€鏈?@CrossOrigin 娉ㄨВ锛侊紒锛?
public class UserInfoController {

    private final UserInfoService userInfoService;

    @Autowired
    private UserProfileMapper UserProfileMapper;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @GetMapping("/info")
    public Map<String, Object> info(@RequestParam Integer userId) {
        UserInfoVO vo = userInfoService.getUserInfo(userId);

        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "success");
        map.put("data", vo);
        return map;
    }

    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody UserInfoVO vo) {
        userInfoService.updateUserInfo(vo);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "淇濆瓨鎴愬姛");
        return map;
    }

    @GetMapping("/match")
    public Map<String, Object> match(@RequestParam Integer gameId) {
        Map<String, Object> map = new HashMap<>();

        // 鏌ヨ鏁版嵁搴撲腑game_id鍖归厤鐨勮褰?
        List<UserProfile> profileList = UserProfileMapper.findByGameId(gameId);

        // 鎻愬彇user_id
        List<Integer> userIdList = profileList.stream()
                .map(UserProfile::getUserId)
                .collect(Collectors.toList());

        map.put("code", 200);
        map.put("msg", "鍖归厤鎴愬姛");
        map.put("data", userIdList);
        return map;
    }
}