# backend

## 当前实现状态

- 当前默认启动配置为 **memory** 模式，接口数据由 `com.kecheng.market.common.store.MarketStore` 提供。
- 现有控制器、服务与返回结构已按统一 `ApiResponse<T>` 组织，首页聚合接口已改为强类型 `HomeDataVo`。
- 已新增后台管理查询骨架接口：
  - `GET /api/admin/users`
  - `GET /api/admin/goods`
  - `GET /api/admin/announcements`

## 默认运行方式

默认配置位于 `src/main/resources/application.yml`，激活 `memory` profile：

- 不依赖 MySQL 即可启动并调试当前接口骨架。
- `application-memory.yml` 会关闭 DataSource / MyBatis-Plus 自动装配，避免空数据库环境启动失败。

## 数据库接入预留点

已经预留以下结构作为后续真实落库的切入点：

- MyBatis-Plus 配置：`src/main/java/com/kecheng/market/config/MybatisPlusConfig.java`
- 实体占位：
  - `src/main/java/com/kecheng/market/user/entity/UserEntity.java`
  - `src/main/java/com/kecheng/market/goods/entity/GoodsEntity.java`
  - `src/main/java/com/kecheng/market/announcement/entity/AnnouncementEntity.java`
- Mapper 占位：
  - `src/main/java/com/kecheng/market/user/mapper/UserMapper.java`
  - `src/main/java/com/kecheng/market/goods/mapper/GoodsMapper.java`
  - `src/main/java/com/kecheng/market/announcement/mapper/AnnouncementMapper.java`

数据库配置已保留在：

- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`

## 后续切换到数据库的推荐步骤

1. 以 `User / Goods / Announcement` 三个模块优先落表。
2. 将 `MarketStore` 中对应查询逻辑逐步迁移到 Mapper + Service 实现。
3. 优先替换后台分页查询接口，再替换登录、商品列表、公告列表等主链路读取接口。
4. 待数据库服务实现完成后，再决定是否将 `memory` 模式保留为开发 mock 模式。

## 说明

当前虽然保留了 `market.storage-mode=mysql` 与 MyBatis-Plus 接入点，但**业务服务层仍以内存实现为主**；也就是说，数据库配置已就位，真正的读写切换还需要后续补齐对应 Service 实现。
