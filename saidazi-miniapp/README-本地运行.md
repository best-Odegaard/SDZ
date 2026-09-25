# 赛搭子小程序 —— 本地运行指南

> 更新日期：2026-09-23
> 项目路径：`D:\geek\赛搭子\saidazi-miniapp`
> 状态：**已在本地跑通**，H5 服务可正常访问（16/16 页面编译通过）

---

## 〇、目录结构说明（重要）

本项目源码**全部在项目根目录**，通过 `uni-run.mjs` 包装脚本注入
`UNI_INPUT_DIR` 让 uni-app 从根目录读取源码（而不是默认的 `src/`）：

```
saidazi-miniapp\
├── App.vue                 全局样式 = 设计系统
├── main.js                 应用入口（createSSRApp）
├── pages.json              路由 + TabBar 配置
├── manifest.json           多端配置
├── pages\                  16 个页面
├── components\             6 个公共组件（easycom 自动注册）
├── utils\                  api / request / mock / auth
├── static\tabbar\          TabBar 图标（8 张 PNG）
├── index.html              H5 端 Vite 入口页
├── vite.config.js          Vite 配置
├── uni-run.mjs             启动包装器（注入 UNI_INPUT_DIR）
├── pnpm-workspace.yaml     pnpm 12 配置（allowBuilds / nodeLinker）
└── package.json
```

> **为什么不放在 `src\`？**
> uni-app CLI 默认从 `src\` 读源码，但那样会让 Vite 的 root 与源码目录不一致，
> 导致 `index.html` 里的 `/main.js` 解析到错误位置、页面白屏。
> 本项目的做法是**源码和 `index.html` 都放根目录**，用 `uni-run.mjs` 告诉 uni-app
> 「源码就在根目录」。这样路径完全一致，不需要额外的目录结构约定。
>
> ⚠️ 因此 **必须用 `pnpm dev:h5` 启动**（`package.json` 的 script 已指向 `uni-run.mjs`），
> 不要直接跑 `uni` 命令。

---

## 一、快速开始（H5 网页版，推荐）

**适用场景**：只想看界面效果 / 调 UI，不想装微信开发者工具。

```bash
cd D:\geek\赛搭子\saidazi-miniapp

# 1. 安装依赖（首次执行一次即可）
pnpm install

# 2. 启动 H5 开发服务器
pnpm dev:h5
```

启动成功后终端输出：

```
  vite v5.2.8 dev server running at:

  ➜  Local:   http://127.0.0.1:5173/

  ready in 1324ms.
```

浏览器打开 **`http://127.0.0.1:5173/`** 即可。

> 端口被占用时 Vite 会自动递增（5174、5175…），以终端实际输出为准。

### ⚠️ 本机环境特别注意（踩过的坑）

本机是**受限终端环境**，直接用 `pnpm` 命令可能失败。以下三个问题都遇到过，按顺序处理即可：

**问题 1：`pnpm` 报 `sed: command not found`**

受限 shell 里 pnpm 的包装脚本不可用，**直接调用原生 exe**：

```bash
"D:/NodeJS/node_modules/pnpm/pnpm.exe" install
```

**问题 2：`ERR_PNPM_IGNORED_BUILDS`（安装中断，node_modules 为空）**

pnpm 12 默认拒绝执行第三方构建脚本，会**直接中断安装**。
解决办法是在 `pnpm-workspace.yaml` 里批准（该文件已配好，勿删）：

```yaml
allowBuilds:
  core-js: true
  esbuild: true
nodeLinker: hoisted
```

> `package.json` 里的 `pnpm.onlyBuiltDependencies` 在 pnpm 12 **已失效**，不要用。

**问题 3：安装报 `Done` 但包目录是空的**（最常见、最难查）

症状：日志显示 `added 743, done`、退出码 0，但启动时报
`Cannot find module '.../vite-plugin-uni/bin/uni.js'`。

原因：受限环境**禁止创建符号链接**，pnpm 默认的 link 机制失败后静默降级成空目录。

解决办法：使用 **`nodeLinker: hoisted`**（已写入 `pnpm-workspace.yaml`），
安装后用下面命令验证非空：

```bash
# 应该输出 6（而不是 0）
ls node_modules/@dcloudio/vite-plugin-uni | wc -l
```

**问题 4：`esbuild` 原生二进制缺失**

`esbuild` 需要平台专属的 `@esbuild/win32-x64`，已显式加入 devDependencies。
若仍报 `Failed to find package "@esbuild/win32-x64"`：

```bash
"D:/NodeJS/node_modules/pnpm/pnpm.exe" add -D @esbuild/win32-x64@0.20.2
```

> Vite 5 强依赖 esbuild 二进制，缺失会直接启动失败。

**问题 5：`.pnpm-store` 写入权限告警**

```
Failed to create file at "D:\.pnpm-store\..." 拒绝访问。(os error 5)
```

pnpm 会自动重试，通常不阻塞。若持续失败，改存储目录：

```bash
"D:/NodeJS/node_modules/pnpm/pnpm.exe" install --store-dir "D:\.pnpm-store" --force
```

> 💡 `registry.npmmirror.com` 单个请求耗时 10–55 秒属正常（网络问题），
> 看到 `Progress:` 数字在涨就说明在推进，不是卡死。

---

## 二、微信小程序版

**适用场景**：需要在微信开发者工具里预览真机效果。

```bash
cd D:\geek\赛搭子\saidazi-miniapp

# 1. 安装依赖
pnpm install

# 2. 编译为小程序产物（watch 模式，改代码自动重新编译）
pnpm dev:mp-weixin
```

编译产物目录：`dist\dev\mp-weixin`

**3. 打开微信开发者工具**
- 选择「导入项目」
- 目录选择：`D:\geek\赛搭子\saidazi-miniapp\dist\dev\mp-weixin`
- AppID：选择「测试号」或填入你自己的小程序 AppID
- 导入后即可预览

> ⚠️ 首次导入若提示「未找到 app.json」，说明编译还没跑完，等终端输出完成提示后重新导入。

**生产构建**（正式发布用）：
```bash
pnpm build:mp-weixin   # 产物在 dist\build\mp-weixin
```

---

## 三、是否需要启动后端？

**不需要。** 当前 `utils\request.js` 中：

```js
export const USE_MOCK = true
```

Mock 模式已开启，所有接口请求都会走 `utils\mock.js` 的本地假数据，不依赖 `saidazi-server`。

想要切换为真实后端：把该值改成 `false`，并确保：
- `saidazi-server` 已启动在 `http://localhost:8080`
- `request.js` 中 `BASE_URL` 指向正确地址

> 页面代码无需任何改动 —— Mock 与真实接口的返回结构完全一致。

---

## 四、常见问题

### 1. 安装报「版本找不到」

`package.json` 里的 `@dcloudio/*` 必须使用带日期后缀的版本号（如 `3.0.0-4080720251210001`），
**不能**写成 `^3.0.0` —— uni-app 的 Vue3 版本在 npm 上没有纯语义化版本号。

目前配置已修正，若被改回 `^3.0.0` 会出现解析失败。

### 2. H5 能跑但小程序编译报错「找不到 tabbar 图标」

TabBar 图标必须是**真实存在的 PNG 文件**，位于 `static\tabbar\`：
```
home.png / home-active.png
team.png / team-active.png
community.png / community-active.png
mine.png / mine-active.png
```
小程序不支持 SVG 或字体图标作为 tabBar 图标，也**不能删掉这些文件**（缺失会导致编译直接失败）。
当前这些是脚本生成的几何占位图，可替换为正式设计稿，保持文件名一致即可。

### 3. 页面白屏（最难查的坑）⚠️

**症状**：浏览器打开 `http://127.0.0.1:5173/` 一片空白，Vite 日志报
`Failed to load url /main.js (resolved id: /main.js). Does the file exist?`

**原因**：Vite 的 root 与 uni-app 的源码目录不一致，导致 `index.html` 里的
`/main.js` 解析到了错误位置。

**排查方法**：请求任意模块，如果返回内容和 `index.html` 一样大（都是 HTML），
说明所有模块都被 fallback 了 —— 这是路径不匹配的典型特征：

```bash
# 应该返回编译后的 JS（几万字节），而不是 HTML
curl http://127.0.0.1:5173/main.js
```

**本项目已解决**：用 `uni-run.mjs` 注入 `UNI_INPUT_DIR` 指向项目根目录，
让 uni-app 与 Vite 的源码目录统一。

> ⚠️ 如果直接用 `npx uni` 或全局 `uni` 命令启动，会绕过包装脚本，白屏问题会重现。
> **请始终用 `pnpm dev:h5`。**

### 4. 页面白屏 / 控制台报 `uni is not defined`

H5 模式下 `uni` 对象由 uni-app 运行时注入，若在浏览器控制台手动调用会报错，属正常现象。
排查时请通过页面交互验证，不要直接在 Console 里调 `uni.xxx`。

### 5. 修改了 `pages.json` 后新页面打不开

`pages.json` 变更需要重启开发服务器（`Ctrl+C` 后重新 `pnpm dev:h5`）。

### 6. 端口被占用

Vite 会自动切换到下一个可用端口（5174、5175…），看终端输出即可。也可显式指定：
```bash
pnpm dev:h5 -- --port 3000
```

> ⚠️ 有时旧进程会僵死占着 5173，新服务默默起在 5174，导致你访问 5173 看到的是旧版本。
> 遇到「改了代码没生效」先确认端口：

```bash
netstat -ano | findstr :5173
```

### 7. 报错 `ENOENT ... src\manifest.json`

说明 `UNI_INPUT_DIR` 没生效（uni-app 回退到了默认的 `src\`）。
请确认用 `pnpm dev:h5` 启动（走 `uni-run.mjs`），而不是直接运行 `uni`。

### 8. 报错 `Failed to resolve entry for package` 或启动即退出

确认项目根目录下这两个文件存在（缺失会直接启动失败）：

| 文件 | 作用 |
|---|---|
| `index.html` | H5 端 Vite 入口页（挂载 `<div id="app">`、引入 `main.js`） |
| `vite.config.js` | 注册 `@dcloudio/vite-plugin-uni` 插件 + dev server 配置 |
| `uni-run.mjs` | 启动包装器（注入 `UNI_INPUT_DIR`，**必需**） |

---

## 五、目录速览

```
saidazi-miniapp\
├── pages\                      16 个页面
│   ├── index\index.vue             首页（校内/全国 Tab）
│   ├── competition\detail.vue      赛事详情
│   ├── calendar\index.vue          赛事日历
│   ├── team\                       搭子：list / detail / publish / match
│   ├── resume\                     简历：edit / preview / received
│   ├── community\                  社区：index / post-detail / publish
│   └── mine\                       我的：index / favorites / reminders
├── components\                 6 个公共组件（easycom 自动注册）
├── utils\
│   ├── api.js                  接口封装（全部接口集中在此）
│   ├── request.js              请求封装 + USE_MOCK 开关
│   ├── mock.js                 本地假数据（界面开发用）
│   └── auth.js                 登录态 + 展示层工具函数
├── static\tabbar\              TabBar 图标（8 张 PNG）
├── App.vue                     全局样式 = 设计系统
├── main.js                     应用入口（createSSRApp）
├── pages.json                  路由 + TabBar 配置
├── manifest.json               多端配置
├── index.html                  H5 端 Vite 入口页
├── vite.config.js              Vite 配置
├── uni-run.mjs                 启动包装器（注入 UNI_INPUT_DIR）
├── pnpm-workspace.yaml         pnpm 12 配置（allowBuilds / nodeLinker）
└── package.json
```

---

## 六、切换真实后端的完整步骤

1. `saidazi-server` 启动（端口 8080，数据库 MySQL `saidazi`）
2. `utils\request.js` → `USE_MOCK = false`
3. 确认 `BASE_URL = 'http://localhost:8080/api'`
4. 小程序端需在微信公众平台配置「request 合法域名」；开发阶段可在微信开发者工具中勾选
   「不校验合法域名」

> 若走 H5，浏览器直接请求 `localhost:8080` 会遇到 CORS 跨域问题，
> 需在 `saidazi-server` 侧添加 CORS 配置或通过 Vite 代理转发。

---

## 七、已验证的运行状态

| 检查项 | 结果 |
|---|---|
| H5 开发服务器启动 | ✅ `ready in 1339ms` @ `http://127.0.0.1:5173/` |
| 根路径 HTML 入口 | ✅ 正确引用 `/main.js` |
| `16` 个页面编译 | ✅ **16/16 通过，0 fallback，0 错误** |
| `main.js` / `App.vue` | ✅ 编译正常（1201 / 15789 bytes） |
| `utils/mock.js` 打包 | ✅ 211 KB（Mock 数据已生效） |
| `@dcloudio/vite-plugin-uni` | ✅ `bin/uni.js` 存在 |
| `@esbuild/win32-x64` | ✅ `esbuild.exe` 存在 |
| 后端依赖 | ✅ 无需启动（`USE_MOCK = true`） |

**验证命令**（服务启动后执行，确认模块真被编译而不是 fallback）：

```bash
curl -s http://127.0.0.1:5173/main.js | wc -c      # 应为 1200 左右，不是 817
curl -s http://127.0.0.1:5173/pages/index/index.vue | wc -c   # 应为 39000 左右
```

> 若返回值都是 ~817（等于 `index.html` 的大小），说明模块被 fallback 了，
> 参见「常见问题 3. 页面白屏」。
