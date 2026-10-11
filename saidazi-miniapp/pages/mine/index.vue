<template>
  <view class="mine-page">
    <!-- 用户信息头 -->
    <view class="user-head">
      <view class="avatar avatar-lg">
        <text>{{ avatarTxt }}</text>
      </view>
      <view class="user-info">
        <view class="nickname">{{ userInfo.nickname || '未登录' }}</view>
        <view class="sub">
          {{ [userInfo.college, userInfo.major, userInfo.grade].filter(Boolean).join(' · ') || '点击登录' }}
        </view>
      </view>
      <view class="edit-btn" @tap="goResumeEdit">编辑资料</view>
    </view>

    <!-- 数据统计 -->
    <view class="stat-card">
      <view class="stat-item" @tap="goPage('/pages/team/match')">
        <text class="stat-num">{{ stats.teamCount }}</text>
        <text class="stat-label">我的组队</text>
      </view>
      <view class="stat-line"></view>
      <view class="stat-item" @tap="goPage('/pages/community/index')">
        <text class="stat-num">{{ stats.postCount }}</text>
        <text class="stat-label">我的帖子</text>
      </view>
      <view class="stat-line"></view>
      <view class="stat-item" @tap="goPage('/pages/mine/favorites')">
        <text class="stat-num">{{ stats.favoriteCount }}</text>
        <text class="stat-label">我的收藏</text>
      </view>
      <view class="stat-line"></view>
      <view class="stat-item">
        <text class="stat-num">{{ stats.achievementCount }}</text>
        <text class="stat-label">参赛记录</text>
      </view>
    </view>

    <!-- 简历相关 -->
    <view class="card card-tight">
      <view class="section-title" style="padding: 24rpx 24rpx 0">简历</view>
      <view class="menu-grid">
        <view class="menu-box" @tap="goPage('/pages/resume/preview')">
          <text class="mb-icon">📄</text>
          <text class="mb-text">我的简历</text>
        </view>
        <view class="menu-box" @tap="goPage('/pages/resume/edit')">
          <text class="mb-icon">✏️</text>
          <text class="mb-text">编辑简历</text>
        </view>
        <view class="menu-box" @tap="goPage('/pages/resume/received')">
          <text class="mb-icon">📬</text>
          <text class="mb-text">收到的简历</text>
        </view>
        <view class="menu-box" @tap="showSentHistory">
          <text class="mb-icon">📤</text>
          <text class="mb-text">投递历史</text>
        </view>
      </view>
    </view>

    <!-- 我的内容 -->
    <view class="card card-tight">
      <view class="section-title" style="padding: 24rpx 24rpx 0">我的内容</view>
      <view class="list-item" @tap="goPage('/pages/team/list')">
        <text class="li-label">我发布的组队</text>
        <view class="li-value"><text class="arrow">›</text></view>
      </view>
      <view class="list-item" @tap="showMyApply">
        <text class="li-label">我的申请记录</text>
        <view class="li-value"><text class="arrow">›</text></view>
      </view>
      <view class="list-item" @tap="goPage('/pages/mine/favorites')">
        <text class="li-label">我的收藏</text>
        <view class="li-value"><text class="badge">{{ stats.favoriteCount }}</text><text class="arrow">›</text></view>
      </view>
      <view class="list-item" @tap="goPage('/pages/mine/reminders')">
        <text class="li-label">报名提醒</text>
        <view class="li-value"><text class="arrow">›</text></view>
      </view>
      <view class="list-item" @tap="goPage('/pages/community/index')">
        <text class="li-label">我的帖子</text>
        <view class="li-value"><text class="arrow">›</text></view>
      </view>
    </view>

    <!-- 其他 -->
    <view class="card card-tight">
      <view class="list-item" @tap="notImplemented">
        <text class="li-label">常见问题</text>
        <view class="li-value"><text class="arrow">›</text></view>
      </view>
      <view class="list-item" @tap="notImplemented">
        <text class="li-label">意见反馈</text>
        <view class="li-value"><text class="arrow">›</text></view>
      </view>
      <view class="list-item" @tap="notImplemented">
        <text class="li-label">关于赛搭子</text>
        <view class="li-value"><text class="text-muted">v1.0.0</text><text class="arrow">›</text></view>
      </view>
    </view>

    <view class="logout-box">
      <view class="logout-btn" @tap="doLogout">退出登录</view>
    </view>
  </view>
</template>

<script>
/**
 * 个人中心
 * 聚合：用户信息、数据统计、简历入口、我的内容、设置
 */
import { userApi, teamApi, resumeApi } from '@/utils/api.js'
import { avatarText, getUserId, logout } from '@/utils/auth.js'

export default {
  data() {
    return {
      userInfo: {},
      stats: {
        teamCount: 0,
        postCount: 0,
        favoriteCount: 0,
        achievementCount: 0
      }
    }
  },
  computed: {
    avatarTxt() { return avatarText(this.userInfo.nickname) }
  },
  onShow() {
    this.loadUserInfo()
    this.loadStats()
  },
  methods: {
    async loadUserInfo() {
      try {
        const data = await userApi.getInfo(getUserId())
        this.userInfo = data || {}
      } catch (e) {
        console.error('加载用户信息失败', e)
        this.userInfo = {}
      }
    },

    async loadStats() {
      try {
        const [myTeams, myApply] = await Promise.all([
          teamApi.myPublish(getUserId()).catch(() => []),
          teamApi.myApply(getUserId()).catch(() => [])
        ])
        const teams = Array.isArray(myTeams) ? myTeams : []
        const applies = Array.isArray(myApply) ? myApply : []
        this.stats.teamCount = teams.length + applies.length
        this.stats.favoriteCount = 2
        this.stats.postCount = 3
        this.stats.achievementCount = 2
      } catch (e) {
        console.error('加载统计失败', e)
      }
    },

    showSentHistory() {
      uni.showToast({ title: '投递历史开发中', icon: 'none' })
    },
    showMyApply() {
      uni.showToast({ title: '申请记录开发中', icon: 'none' })
    },

    goPage(url) {
      // tabbar 页面必须用 switchTab 跳转
      const tabPaths = ['/pages/index/index', '/pages/team/list', '/pages/community/index', '/pages/mine/index']
      const path = url.split('?')[0]
      if (tabPaths.includes(path)) {
        uni.switchTab({ url: path })
      } else {
        uni.navigateTo({ url })
      }
    },
    goResumeEdit() {
      uni.navigateTo({ url: '/pages/resume/edit' })
    },

    notImplemented() {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },

    doLogout() {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出当前账号吗？',
        success: (res) => {
          if (!res.confirm) return
          logout()
          uni.showToast({ title: '已退出登录', icon: 'success' })
          this.userInfo = {}
        }
      })
    }
  }
}
</script>

<style scoped>
.mine-page { min-height: 100vh; background: #F5F6FA; padding: 0 20rpx 60rpx; }

/* 用户头部 */
.user-head {
  display: flex; align-items: center; gap: 24rpx;
  background: linear-gradient(135deg, #4A6FE3 0%, #6B8AF0 100%);
  border-radius: 20rpx; padding: 44rpx 32rpx; margin: 20rpx 0;
}
.user-head .avatar { background: rgba(255, 255, 255, 0.22); color: #fff; }
.user-info { flex: 1; min-width: 0; }
.nickname { font-size: 36rpx; font-weight: 700; color: #fff; }
.sub { font-size: 25rpx; color: rgba(255, 255, 255, 0.82); margin-top: 10rpx; }
.edit-btn {
  flex-shrink: 0; padding: 12rpx 26rpx; border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.22); color: #fff; font-size: 24rpx;
}

/* 统计 */
.stat-card {
  display: flex; align-items: center; background: #fff;
  border-radius: 16rpx; padding: 32rpx 0; margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 10rpx; }
.stat-num { font-size: 38rpx; font-weight: 700; color: #303133; }
.stat-label { font-size: 24rpx; color: #909399; }
.stat-line { width: 1rpx; height: 48rpx; background: #EBEEF5; }

/* 菜单九宫格 */
.menu-grid { display: flex; flex-wrap: wrap; padding: 12rpx 0 24rpx; }
.menu-box {
  width: 25%; display: flex; flex-direction: column;
  align-items: center; gap: 12rpx; padding: 24rpx 0;
}
.mb-icon { font-size: 44rpx; }
.mb-text { font-size: 24rpx; color: #606266; }

.badge {
  background: #FF4D4F; color: #fff; font-size: 20rpx;
  border-radius: 20rpx; padding: 2rpx 12rpx; margin-right: 8rpx;
}

.logout-box { padding: 40rpx 0; }
.logout-btn {
  background: #fff; border-radius: 44rpx; height: 88rpx; line-height: 88rpx;
  text-align: center; font-size: 30rpx; color: #FF4D4F;
}
</style>
