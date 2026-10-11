/** App.vue - 赛搭子用户端 */
<script>
import { setLoginGate } from './utils/request.js'
import { wxLogin } from './utils/auth.js'

export default {
  onLaunch() {
    console.log('赛搭子小程序启动')
    // 启动时尝试恢复登录态
    const token = uni.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo) {
      this.globalData.userInfo = JSON.parse(userInfo)
    }
    // 静默登录门闩：无 token 则先登录；所有接口请求会等待此 Promise
    if (token && userInfo) {
      setLoginGate(Promise.resolve())
    } else {
      setLoginGate(
        wxLogin()
          .then((info) => {
            this.globalData.userInfo = info
          })
          .catch((e) => {
            console.warn('静默登录失败，继续以游客身份加载', e)
          })
      )
    }
  },
  globalData: {
    token: '',
    userInfo: null,
    apiBaseUrl: 'http://134.175.90.165:8080/api'
  }
}
</script>

<style>
/* ==========================================================
   赛搭子 - 全局设计系统
   色板 / 排版 / 通用组件类，所有页面复用
   ========================================================== */

/* ---------- 基础 ---------- */
page {
  background-color: #F5F6FA;
  font-size: 28rpx;
  color: #303133;
  line-height: 1.5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.container { padding: 20rpx; }
.container-flush { padding: 0; }

/* ---------- 通用卡片 ---------- */
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}
.card-tight { padding: 0; overflow: hidden; }

/* 分组标题 */
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
  margin: 8rpx 0 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.section-title .more { font-size: 24rpx; color: #909399; font-weight: 400; }
.section-title::before {
  content: '';
  display: inline-block;
  width: 6rpx;
  height: 28rpx;
  background: #4A6FE3;
  border-radius: 3rpx;
  margin-right: 14rpx;
}
.section-title.plain::before { display: none; }

/* ---------- 按钮 ---------- */
.btn-primary {
  background: #4A6FE3;
  color: #fff;
  border-radius: 44rpx;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 500;
}
.btn-primary[disabled] { background: #a8b9ee; color: #fff; }

.btn-primary.btn-sm { height: 60rpx; line-height: 60rpx; font-size: 26rpx; padding: 0 32rpx; }

.btn-ghost {
  background: #ECF0FF;
  color: #4A6FE3;
  border-radius: 44rpx;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 500;
}
.btn-ghost.btn-sm { height: 60rpx; line-height: 60rpx; font-size: 26rpx; padding: 0 32rpx; }

.btn-plain {
  background: #fff;
  color: #606266;
  border: 1rpx solid #DCDFE6;
  border-radius: 44rpx;
  height: 80rpx;
  line-height: 78rpx;
  text-align: center;
  font-size: 30rpx;
}
.btn-plain.btn-sm { height: 60rpx; line-height: 58rpx; font-size: 26rpx; padding: 0 32rpx; }

/* 底部固定操作条 */
.footer-bar {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  background: #fff;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.05);
  z-index: 90;
}
.footer-bar .btn-primary,
.footer-bar .btn-ghost,
.footer-bar .btn-plain { flex: 1; }
.page-with-footer { padding-bottom: 160rpx; }

/* ---------- 标签 ---------- */
.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  background: #ECF0FF;
  color: #4A6FE3;
  margin-right: 10rpx;
  line-height: 1.6;
}
.tag:last-child { margin-right: 0; }
.tag-diff-1 { background: #E7F8EE; color: #52C41A; }
.tag-diff-2 { background: #FFF7E6; color: #FA8C16; }
.tag-diff-3 { background: #FFF1F0; color: #FF4D4F; }
.tag-gray { background: #F2F3F5; color: #909399; }
.tag-green { background: #E7F8EE; color: #52C41A; }
.tag-orange { background: #FFF7E6; color: #FA8C16; }
.tag-red { background: #FFF1F0; color: #FF4D4F; }

/* 可点击 / 可关闭标签 */
.tag-selectable {
  display: inline-block;
  padding: 10rpx 28rpx;
  border-radius: 32rpx;
  font-size: 26rpx;
  background: #F2F3F5;
  color: #606266;
  margin: 0 16rpx 16rpx 0;
}
.tag-selectable.active { background: #ECF0FF; color: #4A6FE3; font-weight: 500; }

/* ---------- 列表行 ---------- */
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  background: #fff;
  border-bottom: 1rpx solid #F2F3F5;
}
.list-item:last-child { border-bottom: none; }
.list-item .li-label { color: #303133; font-size: 28rpx; }
.list-item .li-value { color: #909399; font-size: 26rpx; display: flex; align-items: center; gap: 8rpx; }
.list-item .arrow { color: #C0C4CC; font-size: 26rpx; margin-left: 8rpx; }

/* ---------- 表单 ---------- */
.form-item { padding: 24rpx; background: #fff; border-bottom: 1rpx solid #F2F3F5; }
.form-item:last-child { border-bottom: none; }
.form-label {
  font-size: 28rpx;
  color: #303133;
  margin-bottom: 14rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
}
.form-label .req { color: #FF4D4F; margin-left: 6rpx; }
.form-input {
  width: 100%;
  height: 76rpx;
  background: #F7F8FA;
  border-radius: 12rpx;
  padding: 0 22rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  color: #303133;
}
.form-textarea {
  width: 100%;
  min-height: 200rpx;
  background: #F7F8FA;
  border-radius: 12rpx;
  padding: 20rpx 22rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  color: #303133;
  line-height: 1.6;
}
.form-tip { font-size: 24rpx; color: #909399; margin-top: 12rpx; }
.form-counter { font-size: 24rpx; color: #C0C4CC; text-align: right; margin-top: 8rpx; }

/* 分段选择器 */
.seg-group { display: flex; flex-wrap: wrap; }
.seg-item {
  padding: 14rpx 30rpx;
  border-radius: 12rpx;
  background: #F7F8FA;
  color: #606266;
  font-size: 26rpx;
  margin: 0 16rpx 16rpx 0;
}
.seg-item.active { background: #4A6FE3; color: #fff; }

/* ---------- 头像 ---------- */
.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: #ECF0FF;
  color: #4A6FE3;
  font-size: 34rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.avatar-sm { width: 60rpx; height: 60rpx; font-size: 26rpx; }
.avatar-lg { width: 128rpx; height: 128rpx; font-size: 48rpx; }
.avatar img { width: 100%; height: 100%; border-radius: 50%; }

/* ---------- 筛选栏 ---------- */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 20rpx;
  background: #fff;
  overflow-x: auto;
  white-space: nowrap;
}
.filter-chip {
  flex-shrink: 0;
  padding: 10rpx 26rpx;
  border-radius: 30rpx;
  background: #F2F3F5;
  color: #606266;
  font-size: 26rpx;
}
.filter-chip.active { background: #ECF0FF; color: #4A6FE3; font-weight: 500; }

/* 顶部 Tab */
.tabs { display: flex; background: #fff; border-radius: 16rpx; margin-bottom: 20rpx; overflow: hidden; }
.tabs .tab { flex: 1; text-align: center; height: 80rpx; line-height: 80rpx; font-size: 28rpx; color: #606266; }
.tabs .tab.active { color: #4A6FE3; font-weight: 600; position: relative; }
.tabs .tab.active::after {
  content: ''; position: absolute; bottom: 0; left: 30%; right: 30%;
  height: 6rpx; background: #4A6FE3; border-radius: 3rpx;
}

/* 滚动 Tab（社区分类） */
.scroll-tabs { display: flex; background: #fff; padding: 0 20rpx; white-space: nowrap; }
.scroll-tabs .st-item {
  flex-shrink: 0;
  padding: 26rpx 28rpx;
  font-size: 28rpx;
  color: #606266;
  position: relative;
}
.scroll-tabs .st-item.active { color: #4A6FE3; font-weight: 600; }
.scroll-tabs .st-item.active::after {
  content: ''; position: absolute; bottom: 8rpx; left: 50%; transform: translateX(-50%);
  width: 40rpx; height: 6rpx; background: #4A6FE3; border-radius: 3rpx;
}

/* ---------- 搜索框 ---------- */
.search-bar { display: flex; align-items: center; gap: 16rpx; margin-bottom: 20rpx; }
.search-input {
  flex: 1;
  height: 72rpx;
  background: #fff;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  color: #909399;
  font-size: 26rpx;
}

/* ---------- 空状态 / 加载 ---------- */
.empty { text-align: center; padding: 120rpx 40rpx; color: #909399; }
.empty .empty-icon { font-size: 88rpx; opacity: 0.35; display: block; margin-bottom: 24rpx; }
.empty .empty-text { font-size: 28rpx; margin-bottom: 12rpx; color: #909399; }
.empty .empty-sub { font-size: 24rpx; color: #C0C4CC; }

.load-more { text-align: center; color: #909399; padding: 30rpx 0; font-size: 26rpx; }

/* 骨架屏 */
.skeleton { background: #EEF0F4; border-radius: 8rpx; animation: sk 1.2s ease-in-out infinite; }
@keyframes sk { 0%, 100% { opacity: 1; } 50% { opacity: 0.55; } }

/* ---------- 报名状态 ---------- */
.reg-status { font-size: 26rpx; font-weight: 600; }
.reg-ing { color: #52C41A; }
.reg-wait { color: #909399; }
.reg-end { color: #C0C4CC; }

/* ---------- 工具类 ---------- */
.divider { height: 1rpx; background: #EBEEF5; margin: 20rpx 0; }
.ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ellipsis-2 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.ellipsis-3 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}
.flex { display: flex; align-items: center; }
.flex-between { display: flex; align-items: center; justify-content: space-between; }
.flex-1 { flex: 1; min-width: 0; }
.text-muted { color: #909399; font-size: 24rpx; }
.text-main { color: #303133; }
.text-primary { color: #4A6FE3; }
.mt-8 { margin-top: 8rpx; }
.mt-16 { margin-top: 16rpx; }
.mt-20 { margin-top: 20rpx; }
.mb-16 { margin-bottom: 16rpx; }
.gap-16 { gap: 16rpx; }
</style>
