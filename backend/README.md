# Backend

校园跳蚤市场后端基于 `Spring Boot 3 + MyBatis-Plus + MySQL`。

## 当前状态

- 默认运行模式：`mysql`
- 备用演示模式：`memory`
- 主业务数据支持 MySQL 持久化
- 已接入接口测试、单元测试，以及 MySQL/Testcontainers 集成测试
- 已补充请求追踪、操作日志、全局异常处理和权限边界校验

## 运行方式

### 1. 本地启动 MySQL

推荐直接使用项目根目录的 Docker Compose：

```powershell
cd E:\毕设
docker compose up -d mysql
```

### 2. 启动后端

```powershell
cd E:\毕设\backend
E:\apache-maven-3.9.12-bin\apache-maven-3.9.12\bin\mvn.cmd -s maven-local-settings.xml spring-boot:run
```

默认读取：

- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`

本地开发默认数据库配置：

- `DB_URL=jdbc:mysql://127.0.0.1:3306/kecheng_campus_market?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai`
- `DB_USERNAME=market_user`
- `DB_PASSWORD=market_password`

### 3. 如需切回内存模式

```powershell
E:\apache-maven-3.9.12-bin\apache-maven-3.9.12\bin\mvn.cmd -s maven-local-settings.xml spring-boot:run "-Dspring-boot.run.profiles=memory"
```

## 测试

运行全部测试：

```powershell
cd E:\毕设\backend
E:\apache-maven-3.9.12-bin\apache-maven-3.9.12\bin\mvn.cmd -s maven-local-settings.xml test
```

说明：

- 普通接口测试默认使用 `memory` profile，速度更快
- `MysqlPersistenceIntegrationTest` 使用 Testcontainers + MySQL
- 当前机器如果没有可用 Docker 环境，MySQL 集成测试会自动跳过

## 目录说明

- `src/main/java/com/kecheng/market/common/store/MarketPersistenceService.java`
  MySQL 持久化主实现
- `src/main/java/com/kecheng/market/common/store/MarketStore.java`
  memory 备用实现
- `src/main/java/com/kecheng/market/common/store/StorageAccessSupport.java`
  统一收口 storage mode 切换逻辑
- `src/test/java/com/kecheng/market/MysqlPersistenceIntegrationTest.java`
  MySQL 持久化集成测试

## 当前建议

- 主线开发继续基于 MySQL 持久化推进
- `memory` 仅保留为演示或无数据库环境下的备用 profile
- 若准备部署到服务器，优先检查 `.env`、MySQL 连接和 `JWT_SECRET`
