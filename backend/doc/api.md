# 接口文档

## 基础

除注册、登录、刷新 Token 等白名单接口外，请求需要携带：

```http
Authorization: Bearer <accessToken>
```

| 方法 | 路径 | 作用 |
|---|---|---|
| GET | `/hello` | 测试服务是否启动 |

## 账号

| 方法 | 路径 | 作用 | 参数 |
|---|---|---|
| POST | `/login` | 登录 | `username`, `password` |
| POST | `/register` | 注册 | `username`, `password` |
| POST | `/refresh-token` | 使用 RefreshToken 换取新双 Token | Header：`X-Refresh-Token` |
| POST | `/logout` | 退出登录并使 Redis 登录态失效 | Header：`Authorization` |

## 用户

| 方法 | 路径 | 作用 | 参数 |
|---|---|---|
| GET | `/user/info` | 获取个人资料 | `userId` |
| POST | `/user/update` | 更新个人资料 | `UserInfoVO` |
| GET | `/user/match` | 基础匹配用户 | `gameId` |
| GET | `/user/publicInfo` | 获取公开资料 | `userId` |
| GET | `/user/matchAdvanced` | 高级匹配用户 | `gameId`, `gameName`, `playtimeStart`, `playtimeEnd`, `matchNeed`, `personality`, `excludeUserId` |

## 队伍

| 方法 | 路径 | 作用 | 参数 |
|---|---|---|
| GET | `/team/list` | 获取队伍列表 | `userId` |
| GET | `/team/detail/{teamId}` | 获取队伍详情 | `teamId` |
| GET | `/team/hot/top` | 查询 Redis 热门队伍 Top N | `limit` |
| GET | `/team/hot/rank/{teamId}` | 查询指定队伍热度排名 | `teamId` |
| GET | `/team/match` | 根据当前用户游戏偏好推荐热门队伍 | `limit` |
| POST | `/team/publish` | 发布队伍 | `TeamRecruit` |
| POST | `/team/comment` | 给队伍评论 | `teamId`, `userId`, `content` |
| POST | `/team/apply` | 申请加入队伍 | `TeamApply` |
| GET | `/team/my/applies` | 查看自己队伍收到的申请 | `leaderId` |
| POST | `/team/dissolve` | 解散队伍 | `teamId`, `leaderId` |
| POST | `/team/leave` | 退出队伍 | `teamId`, `userId` |
| POST | `/team/agree` | 同意加入申请 | `id` |
| POST | `/team/reject` | 拒绝加入申请 | `id` |
| GET | `/team/my/joined` | 查看我加入的队伍 | `userId` |
| GET | `/team/created` | 查看我创建的队伍 | `leaderId` |
| POST | `/team/remove` | 队长移除成员 | `teamId`, `leaderId`, `userId` |

## 好友

| 方法 | 路径 | 作用 | 参数 |
|---|---|---|
| POST | `/friend/apply` | 发送好友申请 | `fromUserId`, `toUserId`, `reason` |
| GET | `/friend/applies/received` | 查看收到的申请 | `userId` |
| POST | `/friend/handle` | 处理好友申请 | `applyId`, `action` |
| GET | `/friend/list` | 查看好友列表 | `userId` |
| DELETE | `/friend/delete` | 删除好友 | `userId`, `friendId` |

## 聊天

| 方法 | 路径 | 作用 | 参数 |
|---|---|---|
| GET | `/chat/team/list` | 获取队伍聊天会话 | `userId` |
| GET | `/chat/team/members` | 获取队伍成员 | `teamId` |
| POST | `/chat/team/member/add` | 添加队伍成员 | `teamId`, `userId` |
| POST | `/chat/team/send` | 发送队伍消息 | `teamId`, `fromId`, `content`, `fileUrl`, `msgType` |
| GET | `/chat/team/history` | 查看队伍消息历史 | `teamId` |
| GET | `/chat/private/my` | 获取私聊会话列表 | `userId` |
| GET | `/chat/private/members` | 获取私聊成员 | `userId` |
| POST | `/chat/private/send` | 发送私聊消息 | `fromId`, `toUserId`, `content`, `fileUrl`, `msgType` |
| GET | `/chat/private/history` | 查看私聊历史 | `userId1`, `userId2` |
| GET | `/chat/private/unread-count` | 获取当前用户私聊未读数 | 无 |

## 通知

| 方法 | 路径 | 作用 | 参数 |
|---|---|---|
| GET | `/notification/my` | 查看我的通知 | `userId` |
| GET | `/notification/unread-count` | 获取当前用户通知未读数 | 无 |

## AI

| 方法 | 路径 | 作用 | 参数 |
|---|---|---|
| POST | `/ai/chat` | AI 对话 | `userId`, `msg` |
| GET | `/ai/history` | 查看 AI 历史 | `userId` |

## 管理员

| 方法 | 路径 | 作用 | 参数 |
|---|---|---|
| GET | `/admin/users` | 查看用户列表 | `adminId` |
| GET | `/admin/user/{userId}` | 查看用户详情 | `adminId` |
| PUT | `/admin/user/{userId}/status` | 修改用户状态 | `adminId`, `status` |
| DELETE | `/admin/user/{userId}` | 删除用户 | `adminId` |
| GET | `/admin/teams` | 查看队伍列表 | `adminId` |
| GET | `/admin/team/{teamId}` | 查看队伍详情 | `adminId` |
| DELETE | `/admin/team/{teamId}` | 删除队伍 | `adminId` |
| GET | `/admin/users/search` | 搜索用户 | `adminId`, `keyword` |
| GET | `/admin/teams/search` | 搜索队伍 | `adminId`, `keyword` |
| GET | `/admin/statistics` | 获取统计数据 | `adminId` |

## 文件

| 方法 | 路径 | 作用 | 参数 |
|---|---|---|
| POST | `/file/upload` | 上传文件 | `file` |

## 说明

1. 登录后建议把 `accessToken`、`refreshToken`、`userId`、`teamId`、`applyId` 放到 Postman 环境变量里。
2. 部分接口返回值已按 VO 结构封装。
3. 如果需要，我可以继续补一份更正式的 `README.md` 版接口说明。
