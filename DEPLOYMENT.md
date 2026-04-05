# 校园跳蚤市场部署指南

## 服务器信息
- IP地址：39.104.93.85
- 操作系统：Ubuntu/Debian
- 登录用户：root
- 登录方式：密码登录
- 部署目录：/opt/campus-market

## 部署前提条件

### 本地准备
1. 确保项目文件完整（包含以下目录和文件）：
   - `backend/` - Spring Boot 后端代码
   - `frontend/` - Vue 前端代码
   - `docker/` - Docker 配置文件
   - `docker-compose.yml` - Docker 编排文件
   - `.env.example` - 环境变量示例
   - `deploy.sh` - 部署脚本
   - `data.sql` - 数据库初始化脚本

2. 检查 Docker 配置文件是否正确：
   - `docker/backend/Dockerfile` - 后端 Docker 配置
   - `docker/frontend/Dockerfile` - 前端 Docker 配置
   - `docker/nginx/nginx.conf` - Nginx 配置
   - `docker/mysql/init.sql` - 数据库初始化脚本（已移除建库语句）

## 部署步骤

### 步骤 1：将项目文件上传到服务器

在本地计算机上执行以下命令，将项目文件复制到服务器：

```bash
# 上传整个项目目录到服务器
scp -r ./* root@39.104.93.85:/opt/campus-market/

# 或者如果使用 SSH 密钥认证：
# scp -r ./* root@39.104.93.85:/opt/campus-market/
```

系统会提示输入 root 用户密码，输入后开始传输文件。

### 步骤 2：登录服务器

```bash
ssh root@39.104.93.85
```

输入 root 密码登录。

### 步骤 3：运行部署脚本

登录服务器后，执行以下命令：

```bash
# 进入部署目录
cd /opt/campus-market

# 给部署脚本执行权限
chmod +x deploy.sh

# 运行部署脚本
./deploy.sh
```

### 步骤 4：按照脚本提示操作

部署脚本将执行以下步骤：

1. **安装 Docker 和 Docker Compose**（如果未安装）
2. **创建部署目录** `/opt/campus-market`
3. **配置环境变量** - 脚本会检查 `.env` 文件，如果不存在会从 `.env.example` 创建
4. **构建 Docker 镜像** - 构建后端、前端和 MySQL 镜像
5. **启动服务** - 启动所有容器
6. **验证服务** - 检查服务是否正常运行

**重要提示**：
- 脚本会显示当前 `.env` 文件内容，请确保所有配置正确
- 需要手动确认配置文件是否正确
- 首次构建可能需要较长时间（下载依赖和镜像）

### 步骤 5：验证部署

部署完成后，验证服务是否正常运行：

1. **检查容器状态**：
   ```bash
   cd /opt/campus-market
   docker-compose ps
   ```

   应该看到三个服务（mysql、backend、frontend）都处于 "Up" 状态。

2. **访问应用**：
   - 前端页面：http://39.104.93.85
   - 后端 API：http://39.104.93.85:8080
   - API 文档：http://39.104.93.85:8080/swagger-ui.html

3. **测试登录**：
   - 管理员账号：`admin` / `123456`
   - 普通用户：`zhangsan` / `123456`

## 环境变量配置

部署前需要配置 `.env` 文件，以下为关键配置项：

```env
# MySQL 配置（必须修改）
MYSQL_ROOT_PASSWORD=your_secure_root_password
DB_USERNAME=market_user
DB_PASSWORD=your_secure_password

# 后端配置（必须修改）
JWT_SECRET=your-secure-jwt-secret-key-2025-change-this
SPRING_PROFILES_ACTIVE=prod

# 服务器配置（可选）
SERVER_PORT=8080
```

**安全建议**：
- 使用强密码替换默认密码
- 生成随机的 JWT 密钥（可以使用 `openssl rand -base64 32` 生成）
- 生产环境避免使用默认端口

## 服务管理命令

在部署目录 (`/opt/campus-market`) 下执行：

### 查看服务状态
```bash
docker-compose ps
```

### 查看服务日志
```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mysql

# 实时查看日志
docker-compose logs -f
```

### 停止服务
```bash
docker-compose down
```

### 重启服务
```bash
docker-compose restart
```

### 重新构建并启动服务（代码更新后）
```bash
docker-compose up -d --build
```

### 进入容器
```bash
# 进入后端容器
docker-compose exec backend sh

# 进入 MySQL 容器
docker-compose exec mysql mysql -u market_user -p
```

## 数据备份与恢复

### 备份数据库
```bash
cd /opt/campus-market
docker-compose exec mysql mysqldump -u market_user -p kecheng_campus_market > backup_$(date +%Y%m%d).sql
```

### 恢复数据库
```bash
cd /opt/campus-market
docker-compose exec mysql mysql -u market_user -p kecheng_campus_market < backup.sql
```

### 备份上传文件
```bash
cd /opt/campus-market
tar -czf uploads_backup_$(date +%Y%m%d).tar.gz /var/lib/docker/volumes/market_upload_data
```

## 故障排除

### 常见问题

1. **容器启动失败**
   ```bash
   # 查看详细日志
   docker-compose logs [service-name]
   
   # 常见原因：
   # - 环境变量配置错误
   # - 端口被占用
   # - 数据库连接失败
   ```

2. **前端无法访问后端 API**
   - 检查 Nginx 配置中的代理设置
   - 验证后端服务是否正常运行
   - 检查 CORS 配置

3. **数据库连接失败**
   - 检查 MySQL 容器是否运行
   - 验证数据库用户名和密码
   - 检查网络连接

4. **文件上传失败**
   - 检查上传目录权限
   - 验证磁盘空间
   - 检查文件大小限制

### 重新部署
如果部署失败，可以重新部署：

```bash
cd /opt/campus-market
docker-compose down -v  # 删除容器和卷
rm -f .env              # 删除配置文件
# 重新编辑 .env 文件
./deploy.sh
```

## 生产环境优化建议

1. **安全加固**
   - 配置 SSL/TLS 证书（使用 Let's Encrypt）
   - 修改默认端口
   - 设置防火墙规则
   - 定期更新 Docker 镜像

2. **性能优化**
   - 配置 Nginx 缓存
   - 调整数据库连接池参数
   - 启用 Gzip 压缩

3. **监控与日志**
   - 配置日志轮转
   - 设置监控告警
   - 定期备份数据

4. **高可用性**（可选）
   - 使用 Docker Swarm 或 Kubernetes
   - 配置负载均衡
   - 设置数据库主从复制

## 联系支持

如果遇到问题，请检查：
1. 服务器防火墙是否开放端口（80, 8080）
2. Docker 服务是否正常运行
3. 系统资源是否充足（内存、磁盘）

如需进一步帮助，请联系项目维护人员。

---
*最后更新：2026-04-05*
*部署脚本版本：1.0*