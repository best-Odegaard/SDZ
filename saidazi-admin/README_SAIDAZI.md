# 赛搭子管理端 (saidazi-admin + saidazi-admin-ui)

基于 **RuoYi-Vue** (后端 Spring Boot 2.5 + Spring Security) + **RuoYi-Vue3** (前端 Vue3 + Element Plus + Vite)。

与用户端 `saidazi-server` 共享同一 MySQL `saidazi` 数据库：业务表 (competition/team/post/...) + 若依系统表 (sys_user/sys_role/sys_menu/...) 共存。

## 端口规划

| 服务 | 端口 | 说明 |
|------|------|------|
| saidazi-server (用户端 API) | 8080 | 面向 Uniapp 小程序 |
| saidazi-admin (RuoYi 后端) | 8081 | 管理端 API |
| saidazi-admin-ui (RuoYi-Vue3 前端) | 81 | 管理端页面 (vite dev) |
| MySQL | 3306 | 共享库 `saidazi` |
| Redis | 6379 | 若依依赖 (验证码/令牌/字典) |

## 一、初始化数据库

```bash
# 1) 创建库并导入业务表 + 测试数据
mysql -u root -p < sql/saidazi_init.sql

# 2) 导入若依系统表 (sys_* / gen_*)
mysql -u root -p saidazi < sql/ry_20250522.sql

# 3) 导入 Quartz 定时任务表 (QRTZ_*)
mysql -u root -p saidazi < sql/quartz.sql
```

> 三套表命名互不冲突：业务表无前缀，若依系统表以 `sys_`/`gen_` 开头，Quartz 表以 `QRTZ_` 开头。

## 二、启动后端 (saidazi-admin)

已修改的配置：
- `ruoyi-admin/src/main/resources/application.yml`：端口 `8081`，文件路径 `D:/saidazi/uploads`，Redis 密码清空
- `ruoyi-admin/src/main/resources/application-druid.yml`：数据源指向 `jdbc:mysql://localhost:3306/saidazi`，用户 `root`，密码 `123456`

```bash
cd saidazi-admin
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
# 或 IDE 运行 RuoYiApplication.main()
```

启动后访问：`http://localhost:8081`（Swagger）、Druid 监控 `/druid`（ruoyi/123456）。

默认管理员：`admin / admin123`（来自 ry_20250522.sql）。

## 三、启动前端 (saidazi-admin-ui)

已修改的配置：
- `.env.development` / `.env.production`：标题改为「赛搭子管理端」
- `vite.config.js`：dev 端口 `81`，代理 `/dev-api -> http://localhost:8081`

```bash
cd saidazi-admin-ui
npm install --registry=https://registry.npmmirror.com
npm run dev
```

访问：`http://localhost:81`，使用 `admin / admin123` 登录。

## 四、依赖说明

- **Redis**：若依**强依赖** Redis（登录验证码、JWT 令牌缓存、字典缓存）。本地需安装并启动 Redis：
  - Windows: 下载 https://github.com/tporadowski/redis/releases，运行 `redis-server.exe`
  - 或使用 Memurai / WSL Redis
- **JDK**：项目声明 Java 1.8，实测 JDK 17 也可编译通过。
- **MySQL**：5.7+ / 8.0 均可。

## 五、与用户端 (saidazi-server) 的关系

- 两端**不相互调用**，仅通过共享 MySQL `saidazi` 库的业务表交换数据。
- 管理端在 `competition` 表录入赛事 → 用户端读取展示。
- 用户端原有简易 `AdminController` 在若依管理端上线后**弃用**（保留代码，不再维护）。
- 用户端 `admin` 表与若依 `sys_user` 表**各自独立**，互不干扰。