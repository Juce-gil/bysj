# 部署指南

本项目推荐通过 `docker compose` 部署，默认包含：

- `mysql`
- `backend`
- `frontend`

## 部署前准备

确保服务器已安装：

- Docker
- Docker Compose 或 `docker compose`
- `curl`

确保以下文件已上传到服务器部署目录，例如 `/opt/campus-market`：

- `backend/`
- `frontend/`
- `docker/`
- `docker-compose.yml`
- `.env.example`
- `deploy.sh`

## 环境变量

第一次部署前，先准备 `.env`：

```env
MYSQL_ROOT_PASSWORD=请替换为强密码
DB_USERNAME=market_user
DB_PASSWORD=请替换为强密码
JWT_SECRET=请替换为随机长字符串
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

注意：

- `DB_PASSWORD` 和 `JWT_SECRET` 不要保留示例占位值
- 后端在 `prod` profile 下会做启动前校验，配置错误会直接失败而不是带病启动

## 启动方式

### 方式一：使用脚本

```bash
cd /opt/campus-market
chmod +x deploy.sh
./deploy.sh
```

### 方式二：手动启动

```bash
cd /opt/campus-market
docker compose build
docker compose up -d
```

## 健康检查

部署完成后，重点检查：

```bash
docker compose ps
docker compose logs --tail=100 backend
curl http://localhost:8080/actuator/health
```

当前后端容器健康检查基于：

- `http://localhost:8080/actuator/health`

## 访问地址

- 前端：`http://服务器IP`
- 后端：`http://服务器IP:8080`
- Swagger / Knife4j：`http://服务器IP:8080/swagger-ui.html`

## 常用运维命令

查看状态：

```bash
docker compose ps
```

查看日志：

```bash
docker compose logs -f
docker compose logs -f backend
docker compose logs -f mysql
```

重启服务：

```bash
docker compose restart
```

重新构建并启动：

```bash
docker compose up -d --build
```

停止服务：

```bash
docker compose down
```

## 数据与文件

- MySQL 数据保存在 `mysql_data` 卷
- 上传文件保存在 `upload_data` 卷

备份数据库示例：

```bash
docker compose exec mysql mysqldump -u market_user -p kecheng_campus_market > backup.sql
```

## 建议

- 生产环境优先通过反向代理和 HTTPS 暴露服务
- 定期备份 MySQL 数据和上传目录
- 如果后续继续演进，建议把初始化脚本进一步迁移到正式 migration 工具
