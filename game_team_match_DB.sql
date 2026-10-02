--  新建数据库
CREATE DATABASE IF NOT EXISTS game_team_match DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--  选中数据库（必须执行，解决1046错误）
USE game_team_match;

-- =============================================
-- 用户表（已补全性别、年龄、联系方式，满足个人信息必填需求）
-- =============================================
CREATE TABLE user (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    gender TINYINT DEFAULT 0 COMMENT '0=不显示 1=男 2=女',
    age INT DEFAULT NULL COMMENT '年龄',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像',
    contact VARCHAR(100) NOT NULL DEFAULT '' UNIQUE,
    game_id INT DEFAULT 0 COMMENT '常玩游戏ID',
    game_rank VARCHAR(30) DEFAULT '' COMMENT '游戏段位',
    introduction TEXT COMMENT '个人简介',
    role TINYINT DEFAULT 0 COMMENT '0=普通用户 1=队长 9=管理员',
    status TINYINT DEFAULT 1 COMMENT '1=正常 0=禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='用户表';

-- =============================================
-- 游戏表
-- =============================================
CREATE TABLE game (
    game_id INT PRIMARY KEY AUTO_INCREMENT,
    game_name VARCHAR(50) NOT NULL COMMENT '游戏名称',
    game_icon VARCHAR(255) DEFAULT '' COMMENT '游戏图标',
    status TINYINT DEFAULT 1 COMMENT '1=启用 0=禁用'
) ENGINE=InnoDB COMMENT='游戏表';

-- =============================================
-- 队伍招募表
-- =============================================
CREATE TABLE team_recruit (
    team_id INT PRIMARY KEY AUTO_INCREMENT,
    game_id INT NOT NULL,
    team_name VARCHAR(100) NOT NULL,
    team_cover VARCHAR(255) DEFAULT '',
    team_need VARCHAR(100) DEFAULT '',
    team_desc TEXT,
    leader_id INT NOT NULL,
    need_num INT DEFAULT 1,
    current_num INT DEFAULT 1,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (game_id) REFERENCES game(game_id),
    FOREIGN KEY (leader_id) REFERENCES user(user_id)
) ENGINE=InnoDB COMMENT='队伍招募表';

-- =============================================
-- 队伍成员表
-- =============================================
CREATE TABLE team_member (
    id INT PRIMARY KEY AUTO_INCREMENT,
    team_id INT NOT NULL,
    user_id INT NOT NULL,
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (team_id, user_id),
    FOREIGN KEY (team_id) REFERENCES team_recruit(team_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB COMMENT='队伍成员表';

-- 申请加入表
CREATE TABLE team_apply (
    id INT PRIMARY KEY AUTO_INCREMENT,
    team_id INT NOT NULL,        -- 申请的队伍ID
    user_id INT NOT NULL,        -- 申请人ID
    status INT DEFAULT 0,        -- 0=待审核 1=同意 2=拒绝
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY (team_id, user_id),
    FOREIGN KEY (team_id) REFERENCES team_recruit(team_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB COMMENT='队伍申请表';

-- =============================================
-- 用户游戏档案表（已补全战绩、胜率、开黑需求，满足匹配需求）
-- =============================================
CREATE TABLE user_profile (
    profile_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE COMMENT '关联用户ID',
    game_id INT NOT NULL COMMENT '常玩游戏ID',
    game_rank VARCHAR(30) NOT NULL COMMENT '游戏段位',
    main_position VARCHAR(20) COMMENT '主位置（上单/中单/ADC等）',
    play_time VARCHAR(50) COMMENT '常玩时间段（晚上/周末/深夜）',
    personality VARCHAR(50) COMMENT '游戏风格（娱乐/上分/指挥/心态好）',
    preferred_mode VARCHAR(30) COMMENT '擅长模式（排位/娱乐/五排）',
    favorite_heroes VARCHAR(100) COMMENT '常用英雄',
    win_rate DECIMAL(5,2) DEFAULT NULL COMMENT '胜率（%）',
    total_matches INT DEFAULT NULL COMMENT '总场次',
    team_requirement TEXT DEFAULT NULL COMMENT '开黑需求（如：晚上8点排位，心态好优先）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (game_id) REFERENCES game(game_id)
) ENGINE=InnoDB COMMENT='用户游戏档案表';
ALTER TABLE user_profile AUTO_INCREMENT = 1;

-- =============================================
-- 用户关注表
-- =============================================
CREATE TABLE user_follow (
    follow_id INT PRIMARY KEY AUTO_INCREMENT,
    follower_id INT NOT NULL COMMENT '关注者ID',
    followed_id INT NOT NULL COMMENT '被关注者ID',
    follow_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (follower_id, followed_id),
    FOREIGN KEY (follower_id) REFERENCES user(user_id),
    FOREIGN KEY (followed_id) REFERENCES user(user_id)
) ENGINE=InnoDB COMMENT='用户关注表';

-- =============================================
-- 匹配申请表
-- =============================================
CREATE TABLE match_request (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL COMMENT '发起申请的用户ID',
    receiver_id INT NOT NULL COMMENT '接收申请的用户ID',
    message VARCHAR(255) COMMENT '附言（想一起玩的原因）',
    status TINYINT DEFAULT 0 COMMENT '0=待处理 1=已同意 2=已拒绝',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    handle_time DATETIME NULL,
    FOREIGN KEY (sender_id) REFERENCES user(user_id),
    FOREIGN KEY (receiver_id) REFERENCES user(user_id)
) ENGINE=InnoDB COMMENT='匹配申请表';

-- =============================================
-- 车队公告表
-- =============================================
CREATE TABLE team_announcement (
    anno_id INT PRIMARY KEY AUTO_INCREMENT,
    team_id INT NOT NULL COMMENT '关联队伍ID',
    content TEXT NOT NULL COMMENT '公告内容（游戏ID、语音频道等）',
    creator_id INT NOT NULL COMMENT '发布者ID（队长）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES team_recruit(team_id),
    FOREIGN KEY (creator_id) REFERENCES user(user_id)
) ENGINE=InnoDB COMMENT='车队公告表';

-- =============================================
-- 队内聊天表
-- =============================================
CREATE TABLE team_chat (
    chat_id INT PRIMARY KEY AUTO_INCREMENT,
    team_id INT NOT NULL COMMENT '队伍ID',
    sender_id INT NOT NULL COMMENT '发送者ID',
    message TEXT NOT NULL COMMENT '聊天内容',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES team_recruit(team_id),
    FOREIGN KEY (sender_id) REFERENCES user(user_id)
) ENGINE=InnoDB COMMENT='队内聊天表';

-- =============================================
-- 队友评价表
-- =============================================
CREATE TABLE teammate_review (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    reviewer_id INT NOT NULL COMMENT '评价者ID',
    reviewed_id INT NOT NULL COMMENT '被评价者ID',
    team_id INT NOT NULL COMMENT '关联队伍ID',
    score TINYINT NOT NULL COMMENT '评分（1-5）',
    tags VARCHAR(100) COMMENT '评价标签（靠谱/坑/心态好/配合强）',
    content TEXT COMMENT '评价内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (reviewer_id, reviewed_id, team_id),
    FOREIGN KEY (reviewer_id) REFERENCES user(user_id),
    FOREIGN KEY (reviewed_id) REFERENCES user(user_id),
    FOREIGN KEY (team_id) REFERENCES team_recruit(team_id)
) ENGINE=InnoDB COMMENT='队友评价表';

-- =============================================
-- 约玩记录表
-- =============================================
CREATE TABLE play_invite (
    invite_id INT PRIMARY KEY AUTO_INCREMENT,
    inviter_id INT NOT NULL COMMENT '邀请者ID',
    invitee_id INT NOT NULL COMMENT '被邀请者ID',
    team_id INT NULL COMMENT '关联队伍（可选）',
    play_time DATETIME NOT NULL COMMENT '约定开黑时间',
    status TINYINT DEFAULT 0 COMMENT '0=待确认 1=已接受 2=已拒绝',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inviter_id) REFERENCES user(user_id),
    FOREIGN KEY (invitee_id) REFERENCES user(user_id),
    FOREIGN KEY (team_id) REFERENCES team_recruit(team_id)
) ENGINE=InnoDB COMMENT='约玩记录表';

-- 评论表
CREATE TABLE team_comment (
    id INT PRIMARY KEY AUTO_INCREMENT,
    team_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (team_id) REFERENCES team_recruit(team_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB COMMENT='队伍评论表';


--  系统通知表 
CREATE TABLE system_notification (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    type VARCHAR(30) DEFAULT 'system',
    related_id INT DEFAULT NULL,
    is_read TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- 聊天消息表
CREATE TABLE chat_message (
    id INT PRIMARY KEY AUTO_INCREMENT,
    team_id INT NOT NULL,
    sender_id INT NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(10) DEFAULT 'text',   -- text 或 image
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES team_recruit(team_id),
    FOREIGN KEY (sender_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========================== 聊天系统核心表 ==========================

-- 好友关系表:存储用户之间的好友、拉黑关系
CREATE TABLE friend (
    id INT PRIMARY KEY AUTO_INCREMENT,        -- 主键ID
    user_id INT NOT NULL,                     -- 当前用户ID
    friend_id INT NOT NULL,                   -- 好友ID
    status TINYINT DEFAULT 1,                 -- 状态 1正常 2拉黑
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 添加时间
    reason VARCHAR(255) DEFAULT NULL COMMENT '申请原因',
    UNIQUE KEY uk_user_friend (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (friend_id) REFERENCES user(user_id)
) ENGINE=InnoDB;

-- 私聊消息表:存储用户一对一聊天记录（文字、图片、视频）
CREATE TABLE private_message (
    id INT PRIMARY KEY AUTO_INCREMENT,        -- 消息ID
    from_id INT NOT NULL,                     -- 发送人ID
    to_id INT NOT NULL,                       -- 接收人ID
    content TEXT,                             -- 消息内容
    msg_type VARCHAR(20) DEFAULT 'text',       -- 消息类型 text/image/video
    file_url VARCHAR(255) DEFAULT '',         -- 文件地址
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 发送时间
    is_read TINYINT DEFAULT 0,                 -- 是否已读 0否1是

    FOREIGN KEY (from_id) REFERENCES user(user_id),
    FOREIGN KEY (to_id) REFERENCES user(user_id),
    INDEX idx_from_to (from_id, to_id)
) ENGINE=InnoDB;



-- 队伍聊天消息表：存储游戏队伍内的聊天记录
CREATE TABLE team_message (
    id INT PRIMARY KEY AUTO_INCREMENT,        -- 消息ID
    team_id INT NOT NULL,                    -- 队伍ID
    from_id INT NOT NULL,                    -- 发送人ID
    content TEXT,                           -- 内容
    msg_type VARCHAR(20) DEFAULT 'text',     -- 消息类型
    file_url VARCHAR(255) DEFAULT '',        -- 文件地址
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 发送时间

    FOREIGN KEY (team_id) REFERENCES team_recruit(team_id),
    FOREIGN KEY (from_id) REFERENCES user(user_id),
    INDEX idx_team (team_id)
) ENGINE=InnoDB;

-- AI聊天记录表
CREATE TABLE ai_chat_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    user_msg TEXT NULL COMMENT '用户消息',
    ai_reply TEXT NULL COMMENT 'AI回复',
    chat_type VARCHAR(20) DEFAULT 'doubao',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天记录表';

CREATE TABLE admin_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    admin_id INT NOT NULL COMMENT '管理员用户ID',
    admin_name VARCHAR(50) NOT NULL COMMENT '管理员用户名',
    action_type VARCHAR(30) NOT NULL COMMENT '操作类型（USER_DELETE/USER_DISABLE/TEAM_DELETE等）',
    target_id INT NOT NULL COMMENT '被操作对象ID',
    target_name VARCHAR(100) NOT NULL COMMENT '对象名称',
    detail TEXT COMMENT '操作描述',
    ip_address VARCHAR(50) COMMENT '操作IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_action_type (action_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';



-- 游戏
INSERT INTO game (game_name, game_icon, status)
VALUES
('王者荣耀', '', 1),
('英雄联盟', '', 1),
('和平精英', '', 1),
('原神', '', 1);

-- 用户（密码：123456）
INSERT INTO user
(user_id, username, password, gender, age, avatar, contact, game_id, game_rank, introduction, role, status, create_time, update_time)
VALUES
(1, 'user1', '123456', 1, 20, '', '11111', 1, '星耀', '好相处，不摆烂', 0, 1, NOW(), NOW()),
(2, 'user2', '123456', 1, 19, '', '22222', 2, '铂金', '意识在线，不坑', 0, 1, NOW(), NOW()),
(3, 'user3', '123456', 2, 18, '', '33333', 1, '钻石', '温柔辅助，会配合', 0, 1, NOW(), NOW()),
(4, 'admin', '123456', 1, 21, '', 'admin', 3, '', '长期在线，好说话', 1, 1, NOW(), NOW());


-- 队伍招募
INSERT INTO team_recruit
(team_id, game_id, team_name, team_cover, team_need, team_desc, leader_id, need_num, current_num, status, create_time)
VALUES
(1, 1, '王者五排上分车', '', '缺辅助/中单', '每晚8点开车，心态好优先，不骂人', 1, 5, 2, 1, NOW()),
(2, 2, 'LOL灵活组排', '', '缺上单', '能指挥，会打团，拒绝摆烂', 2, 5, 1, 1, NOW()),
(3, 1, '娱乐开黑小队', '', '女生优先', '输赢无所谓，主打开心', 3, 4, 2, 1, NOW()),
(4, 3, '原神找长期队友', '', '萌新大佬都行', '一起刷本、做任务', 4, 4, 1, 1, NOW());

-- 队伍成员
INSERT INTO team_member (team_id, user_id, join_time)
VALUES
(1, 1, NOW()),
(1, 3, NOW()),
(2, 2, NOW()),
(3, 3, NOW()),
(4, 4, NOW());

-- 评论
INSERT INTO team_comment (team_id, user_id, content, create_time)
VALUES
(1, 3, '我可以玩辅助！', NOW()),
(1, 2, '缺人吗？我中单稳', NOW()),
(2, 1, '上单绝活哥求上车', NOW()),
(3, 4, '可以一起玩吗～', NOW()),
(4, 1, '萌新求带！', NOW());

SELECT * FROM user;
SELECT * FROM user_profile WHERE user_id=1;
-- 插入10条用户数据，user_id 对应 user_profile 的 1001-1010，字段完全匹配你的表结构
INSERT INTO user (user_id, username, password, gender, age, avatar, contact, game_id, game_rank, introduction, role, status)
VALUES 
(1001, 'player1001', 'e10adc3949ba59abbe56e057f20f883e', 1, 22, 'avatar1001.jpg', 'wx123456', 1, '荣耀王者', '中单法王，在线冲分', 0, 1),
(1002, 'player1002', 'e10adc3949ba59abbe56e057f20f883e', 2, 20, 'avatar1002.jpg', 'wx789012', 1, '星耀Ⅰ', '娱乐型ADC，不搞心态', 0, 1),
(1003, 'player1003', 'e10adc3949ba59abbe56e057f20f883e', 1, 25, 'avatar1003.jpg', 'wx345678', 2, '钻石Ⅱ', '上单抗压，指挥全局', 1, 1),
(1004, 'player1004', 'e10adc3949ba59abbe56e057f20f883e', 2, 19, 'avatar1004.jpg', 'wx901234', 2, '铂金Ⅰ', '辅助混子，深夜在线', 0, 1),
(1005, 'player1005', 'e10adc3949ba59abbe56e057f20f883e', 1, 24, 'avatar1005.jpg', 'wx567890', 3, '王者', '野王带飞，稳定上分', 0, 1),
(1006, 'player1006', 'e10adc3949ba59abbe56e057f20f883e', 2, 21, 'avatar1006.jpg', 'wx234567', 3, '星耀Ⅲ', '中路萌妹，娱乐五排', 0, 1),
(1007, 'player1007', 'e10adc3949ba59abbe56e057f20f883e', 1, 23, 'avatar1007.jpg', 'wx890123', 1, '钻石Ⅰ', 'ADC稳如狗，缺辅助', 0, 1),
(1008, 'player1008', 'e10adc3949ba59abbe56e057f20f883e', 1, 26, 'avatar1008.jpg', 'wx456789', 2, '星耀Ⅱ', '上单养老，不喷不送', 0, 1),
(1009, 'player1009', 'e10adc3949ba59abbe56e057f20f883e', 1, 27, 'avatar1009.jpg', 'wx012345', 3, '荣耀王者', '野区霸主，冲分车队', 1, 1),
(1010, 'player1010', 'e10adc3949ba59abbe56e057f20f883e', 2, 18, 'avatar1010.jpg', 'wx678901', 1, '铂金Ⅱ', '辅助猛女，五排娱乐', 0, 1);
-- 测试数据 
INSERT INTO user_profile (user_id, game_id, game_rank, main_position, play_time, personality, preferred_mode, favorite_heroes, win_rate, total_matches, team_requirement)
VALUES (1001, 1, '荣耀王者', '中单', '晚上', '上分', '排位', '貂蝉、诸葛亮', 62.5, 120, '晚上8点开黑，心态好');
INSERT INTO user_profile (user_id, game_id, game_rank, main_position, play_time, personality, preferred_mode, favorite_heroes, win_rate, total_matches, team_requirement)
VALUES (1002, 1, '星耀Ⅰ', 'ADC', '周末', '娱乐', '五排', '马可波罗、公孙离', 58.2, 200, '周末一起娱乐');
INSERT INTO user_profile (user_id, game_id, game_rank, main_position, play_time, personality, preferred_mode, favorite_heroes, win_rate, total_matches, team_requirement)
VALUES (1003, 2, '钻石Ⅱ', '上单', '晚上', '指挥', '排位', '吕布、李信', 51.3, 150, '缺上单，指挥型选手');
INSERT INTO user_profile (user_id, game_id, game_rank, main_position, play_time, personality, preferred_mode, favorite_heroes, win_rate, total_matches, team_requirement)
VALUES (1004, 2, '铂金Ⅰ', '辅助', '深夜', '心态好', '娱乐', '蔡文姬、张飞', 49.8, 90, '深夜娱乐，不喷人');
INSERT INTO user_profile (user_id, game_id, game_rank, main_position, play_time, personality, preferred_mode, favorite_heroes, win_rate, total_matches, team_requirement)
VALUES (1005, 3, '王者', '打野', '晚上', '上分', '排位', '澜、裴擒虎', 68.9, 320, '稳定上分车队');
INSERT INTO user_profile (user_id, game_id, game_rank, main_position, play_time, personality, preferred_mode, favorite_heroes, win_rate, total_matches, team_requirement)
VALUES (1006, 3, '星耀Ⅲ', '中单', '周末', '娱乐', '五排', '妲己、安琪拉', 53.1, 110, '周末五排开车');
INSERT INTO user_profile (user_id, game_id, game_rank, main_position, play_time, personality, preferred_mode, favorite_heroes, win_rate, total_matches, team_requirement)
VALUES (1007, 1, '钻石Ⅰ', 'ADC', '晚上', '上分', '排位', '狄仁杰、伽罗', 55.0, 166, '缺辅助，稳点来');
INSERT INTO user_profile (user_id, game_id, game_rank, main_position, play_time, personality, preferred_mode, favorite_heroes, win_rate, total_matches, team_requirement)
VALUES (1008, 2, '星耀Ⅱ', '上单', '深夜', '心态好', '娱乐', '亚瑟、程咬金', 47.2, 88, '深夜养老玩家');
INSERT INTO user_profile (user_id, game_id, game_rank, main_position, play_time, personality, preferred_mode, favorite_heroes, win_rate, total_matches, team_requirement)
VALUES (1009, 3, '荣耀王者', '打野', '晚上', '指挥', '排位', '韩信、镜', 70.1, 425, '冲分车队，缺队友');
INSERT INTO user_profile (user_id, game_id, game_rank, main_position, play_time, personality, preferred_mode, favorite_heroes, win_rate, total_matches, team_requirement)
VALUES (1010, 1, '铂金Ⅱ', '辅助', '周末', '娱乐', '五排', '牛魔、盾山', 50.0, 130, '娱乐为主，开心就好');

-- 插入游戏（王者荣耀）
INSERT INTO game (game_name, game_icon, status) VALUES 
('王者荣耀','',1),('和平精英','',1),('英雄联盟','',1);

-- 插入 10 个测试用户
INSERT INTO user (username,password,gender,age,avatar,contact,game_id,game_rank,introduction,role,status) VALUES
('player1','123456',1,20,'','p1',1,'王者','',0,1),
('player2','123456',2,21,'','p2',1,'王者','',0,1),
('player3','123456',1,22,'','p3',1,'星耀','',0,1),
('player4','123456',1,19,'','p4',1,'王者','',0,1),
('player5','123456',2,23,'','p5',1,'王者','',0,1),
('player6','123456',1,24,'','p6',1,'星耀','',0,1),
('player7','123456',2,20,'','p7',1,'王者','',0,1),
('player8','123456',1,21,'','p8',1,'星耀','',0,1),
('player9','123456',2,22,'','p9',1,'王者','',0,1),
('player10','123456',1,23,'','p10',1,'星耀','',0,1);

-- 插入 游戏档案（匹配核心数据！）
INSERT INTO user_profile (user_id,game_id,game_rank,main_position,play_time,personality,preferred_mode,favorite_heroes,win_rate,total_matches,team_requirement) VALUES
-- 1号：全匹配（18:00~22:00 + 开黑上分 + 稳健）
(1,1,'王者','打野','18:00~22:00','稳健','排位','澜',65.5,200,'开黑上分'),

-- 2号：全匹配（和1号一模一样）
(2,1,'王者','中路','18:00~22:00','稳健','排位','貂蝉',68.2,250,'开黑上分'),

-- 3号：需求相同、时间相同、性格不同
(3,1,'星耀','边路','18:00~22:00','活泼','排位','马超',54.1,180,'开黑上分'),

-- 4号：需求相同、时间不同、性格不同
(4,1,'王者','辅助','19:00~23:00','佛系','排位','张飞',61.4,300,'开黑上分'),

-- 5号：需求不同、时间相同、性格相同
(5,1,'王者','射手','18:00~22:00','稳健','娱乐','伽罗',57.7,220,'娱乐休闲'),

-- 6号：需求不同、时间不同、性格相同
(6,1,'星耀','打野','20:00~24:00','稳健','娱乐','镜',52.3,150,'娱乐休闲'),

-- 7号：全匹配（第三）
(7,1,'王者','中单','18:00~22:00','稳健','排位','不知火舞',70.0,350,'开黑上分'),

-- 8号：只有时间匹配
(8,1,'星耀','游走','18:00~22:00','安静','娱乐','蔡文姬',49.9,120,'聊天交友'),

-- 9号：只有需求匹配
(9,1,'王者','边路','12:00~16:00','好胜','排位','吕布',63.2,280,'开黑上分'),

-- 10号：只有性格匹配
(10,1,'星耀','打野','10:00~14:00','稳健','娱乐','玄策',55.8,190,'萌新带飞');

-- 查王者荣耀的gameId（确认你的game表中王者荣耀的ID）
SELECT game_id, game_name FROM game WHERE game_name = '王者荣耀';

-- 查该游戏下的用户档案
SELECT * FROM user_profile WHERE game_id = (SELECT game_id FROM game WHERE game_name = '王者荣耀');

-- 查该游戏下的用户
SELECT * FROM user WHERE user_id IN (SELECT user_id FROM user_profile WHERE game_id = (SELECT game_id FROM game WHERE game_name = '王者荣耀'));