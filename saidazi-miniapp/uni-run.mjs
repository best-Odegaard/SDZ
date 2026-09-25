#!/usr/bin/env node
/**
 * uni-app 启动包装器
 *
 * 为什么需要它：
 * uni-app 的 CLI 默认从 `src/` 目录读取源码，但本项目源码与 index.html
 * 都放在项目根目录。CLI 在加载 vite.config.js **之前** 就会读取
 * UNI_INPUT_DIR / UNI_OUTPUT_DIR 环境变量，所以无法在 vite.config.js 里设置，
 * 必须在启动 Node 进程之前注入。
 *
 * 这个脚本用 Node 原生方式设置环境变量后再拉起 uni CLI，
 * 避免依赖 cross-env / shell 的 set 命令（受限环境下可能不可用）。
 */
import { spawn } from 'node:child_process'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))
const projectRoot = __dirname

const uniBin = resolve(projectRoot, 'node_modules', '@dcloudio', 'vite-plugin-uni', 'bin', 'uni.js')

const env = {
  ...process.env,
  // 源码就在项目根目录
  UNI_INPUT_DIR: projectRoot,
  UNI_OUTPUT_DIR: resolve(projectRoot, 'dist', process.env.NODE_ENV === 'production' ? 'build' : 'dev')
}

const child = spawn(process.execPath, [uniBin, ...process.argv.slice(2)], {
  stdio: 'inherit',
  env,
  cwd: projectRoot
})

child.on('exit', (code, signal) => {
  if (signal) process.kill(process.pid, signal)
  else process.exit(code ?? 0)
})
