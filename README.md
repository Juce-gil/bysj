# 科成校园跳蚤市场

基于 **Spring Boot + Vue 3** 的校园二手交易平台毕业设计项目，面向单校场景，支持校园闲置商品发布、求购信息发布、线下预约面交、收藏、评论、公告与后台管理。

---

## 1. 项目简介

- 项目名称：基于 Spring Boot 和 Vue 的校园二手交易平台设计与实现
- 网站名称：校园跳蚤市场
- 学校名称：科成
- 技术定位：前后端分离、毕业设计项目、非商用
- 交易模式：线上发布信息，线下当面交易

---

## 2. 技术栈

### 后端
- Java 17
- Spring Boot 3.3.5
- Maven 3.9.x
- MyBatis-Plus（项目内已集成）
- Knife4j / OpenAPI
- 当前默认运行模式：`mysql`（动态数据默认持久化）

### 前端
- Vue 3
- Vite 4
- TypeScript
- Element Plus
- Pinia
- Axios
- Sass

---

## 3. 当前已实现模块

### 前台用户端
- 登录 / 注册
- 首页
- 商品广场
- 商品详情
- 求购大厅
- 求购详情
- 公告中心
- 公告详情
- 我的商品 / 我的求购
- 我的收藏
- 我的预约
- 我的通知
- 我的资料

### 业务功能
- 商品发布 / 编辑 / 下架 / 重新上架 / 删除
- 求购发布 / 编辑 / 关闭 / 重新开放 / 删除
- 商品多图上传与展示
- 求购可选多图上传与展示
- 商品收藏
- 商品评论
- 预约面交
- 站内通知

### 后台管理
- 管理员首页
- 用户管理
- 商品管理
- 公告管理

---

## 4. 项目结构

```text
E:\毕设
├─ backend   # Spring Boot 后端
├─ frontend  # Vue 3 前端
├─ tools     # 辅助工具目录
└─ data.sql  # 数据库脚本（保留）
```

---

## 5. 运行方式

### 5.1 启动后端

进入目录：

```powershell
cd E:\毕设\backend
```

先启动本地 MySQL（推荐直接使用项目自带 Docker Compose）：

```powershell
cd E:\毕设
docker compose up -d mysql
```

再启动后端：

```powershell
mvn spring-boot:run
```

默认端口：
- 后端：`http://127.0.0.1:8080`
- Swagger：`http://127.0.0.1:8080/swagger-ui.html`

> 当前 `application.yml` 默认启用 `mysql` 模式，动态数据会落到 MySQL 中，重启后仍会保留。
> `application-dev.yml` 的默认账号已与 `docker-compose.yml` 对齐，直接起 `mysql` 服务后即可本地联调。
> 如果只想临时回到内存模式，可显式使用 `memory` profile。

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=memory"
```

### 5.2 启动前端

进入目录：

```powershell
cd E:\毕设\frontend
npm install
npm run dev
```

默认端口：
- 前端：`http://127.0.0.1:5173`

前端开发代理：
- `/api` -> `http://127.0.0.1:8080`
- `/uploads` -> `http://127.0.0.1:8080`

---

## 6. 默认演示账号

### 管理员
- 账号：`admin`
- 密码：`123456`

### 普通用户
- `zhangsan / 123456`
- `lisi / 123456`
- `wangwu / 123456`

> 其余演示账号可结合你后续补充的数据继续使用。

---

## 7. 当前工程状态说明

### 已验证通过
- 后端 `mvn compile` 通过
- 前端 `npm run build` 通过

### 当前默认定位
本项目当前更适合：
- 毕业设计演示
- 功能展示
- 论文撰写与答辩说明

暂不建议直接作为商用上线版本。

---

## 8. 当前已知限制

1. 本地开发和部署前需要先准备可用的 MySQL 实例，否则后端无法以默认持久化模式启动。
2. MySQL/Testcontainers 集成测试在没有 Docker 运行环境的机器上会自动跳过。
3. 后台管理模块以演示可用为主，细节体验仍可继续优化。
4. 安全性为毕业设计级别，尚未做商用级强化。

---

## 9. 后续优化方向

- 扩展 MySQL/Testcontainers 集成测试覆盖面
- 完善接口测试 / 单元测试
- 补充操作日志、异常追踪、权限边界
- 继续统一后台视觉风格
- 增强答辩演示数据与演示脚本
