# GameMatch 项目总览

GameMatch 是一个面向游戏社交与组队匹配场景的前后端分离项目。

`main` 分支用于放置项目说明和仓库导航信息，不直接承载前后端实现代码。  
实际开发与运行请进入对应业务分支。

## 分支说明

| 分支 | 用途 | 技术栈 |
| --- | --- | --- |
| `main` | 项目总览、使用说明、仓库入口 | Markdown |
| `dev` | 后端服务 | Spring Boot、JPA、MySQL、Java 17 |
| `front-end` | 前端应用 | Express、Pug、Node.js |

## 项目功能

当前项目主要包含以下功能：

- 用户注册与登录
- 用户资料与公开信息展示
- 战队招募与战队管理
- 好友申请与好友列表管理
- 私聊与战队聊天
- 通知管理
- 管理员端管理能力
- AI 聊天能力
- 文件上传

## 后端运行方式

切换到后端分支：

```bash
git checkout dev
```

后端默认端口：

```text
8081
```

本地启动命令：

```powershell
.\mvnw.cmd compile
.\mvnw.cmd spring-boot:run
```

后端运行依赖：

- Java 17
- MySQL
- 本地数据库 `sprintpro_db`

当前后端数据库默认配置：

```text
jdbc:mysql://localhost:3306/sprintpro_db
username: root
```

## 前端运行方式

切换到前端分支：

```bash
git checkout front-end
```

前端默认端口：

```text
3000
```

本地启动命令：

```bash
npm install
npm start
```

前端主要技术：

- Express 4
- Pug
- Morgan
- Cookie Parser

## 本地联调建议

1. 先在 `dev` 分支启动后端服务。
2. 再在 `front-end` 分支启动前端服务。
3. 浏览器访问 `http://localhost:3000`。
4. 前端对接后端地址 `http://localhost:8081`。

## 当前仓库状态

- `dev` 分支已放置后端项目代码
- `front-end` 分支已放置前端项目代码
- `main` 分支仅保留项目说明与仓库导航

## 说明

- 不建议把前后端代码直接混放在 `main` 分支
- 日常功能开发请在 `dev` 和 `front-end` 分支中进行
