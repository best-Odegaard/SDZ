import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  server: {
    // 监听所有网卡，兼容内置预览面板 / 代理访问（仅 127.0.0.1 时部分面板连不上）
    host: true,
    port: 3000,
    strictPort: true,
    open: false,
    // 允许通过代理域名访问（内置面板可能用非 localhost 域名透传）
    allowedHosts: true,
    // 关闭 HMR 的 host 校验，避免代理场景下 websocket 连接失败
    hmr: {
      clientPort: 3000
    }
  }
})
