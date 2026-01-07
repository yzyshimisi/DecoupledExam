import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },
  server: {
    proxy: {
      '/api': {  // 获取请求中带 /api 的请求
        target: 'http://localhost:80',  // 后台服务器的域名和端口
        changeOrigin: true,   // 修改源
      }
    }
  }
})