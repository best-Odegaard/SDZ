<template>
  <view class="container" v-if="detail.id">
    <!-- 头部信息 -->
    <view class="card">
      <view class="title">{{ detail.title }}</view>
      <view class="meta">
        <text class="tag" :class="diffClass">{{ diffName }}</text>
        <text class="tag" v-if="detail.category">{{ detail.category }}</text>
        <text class="tag tag-gray" v-if="detail.collegeTag">{{ detail.collegeTag }}</text>
        <text class="tag tag-gray">{{ detail.type === 0 ? '校内' : '全国' }}</text>
      </view>
      <view class="organizer">
        <text class="label">主办单位</text>
        <text class="value">{{ detail.organizer || '—' }}</text>
      </view>
      <view class="stats">
        <text class="stat">{{ detail.viewCount || 0 }} 次浏览</text>
        <text class="dot">·</text>
        <text class="stat">{{ detail.favoriteCount || 0 }} 人收藏</text>
      </view>
    </view>

    <!-- 时间节点 -->
    <view class="card">
      <view class="section-title">时间节点</view>
      <view class="timeline">
        <view class="tl-item">
          <view class="tl-dot start"></view>
          <view class="tl-body">
            <view class="tl-label">报名开始</view>
            <view class="tl-time">{{ formatTime(detail.regStartTime) || '待公布' }}</view>
          </view>
        </view>
        <view class="tl-item">
          <view class="tl-dot" :class="regDotClass"></view>
          <view class="tl-body">
            <view class="tl-label">
              报名截止
              <text class="reg-status" :class="statusClass">{{ regStatus }}</text>
            </view>
            <view class="tl-time">{{ formatTime(detail.regEndTime) || '待公布' }}</view>
          </view>
        </view>
        <view class="tl-item last">
          <view class="tl-dot match"></view>
          <view class="tl-body">
            <view class="tl-label">比赛时间</view>
            <view class="tl-time">{{ formatTime(detail.matchTime) || '待公布' }}</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 赛事简介 -->
    <view class="card">
      <view class="section-title">赛事简介</view>
      <view class="rich-text">{{ detail.description || '暂无简介' }}</view>
    </view>

    <!-- 报名方式 -->
    <view class="card">
      <view class="section-title">报名方式</view>
      <view class="rich-text">{{ detail.regInfo || '暂无说明' }}</view>
      <view class="copy-tip" @tap="copyRegInfo">点击复制报名信息</view>
    </view>

    <!-- 组队入口 -->
    <view class="card">
      <view class="section-title">找搭子</view>
      <view class="team-entry">
        <view class="te-box" @tap="goTeamList">
          <text class="te-icon">👥</text>
          <text class="te-title">看搭子</text>
          <text class="te-sub">浏览本赛事组队</text>
        </view>
        <view class="te-box" @tap="goTeamPublish">
          <text class="te-icon">✏️</text>
          <text class="te-title">找搭子</text>
          <text class="te-sub">发布组队需求</text>
        </view>
      </view>
    </view>

    <!-- 往届成果 -->
    <view class="card" v-if="achievements.length">
      <view class="section-title">校内往届成果</view>
      <view class="ach-item" v-for="a in achievements" :key="a.id">
        <view class="ach-head">
          <text class="ach-team">{{ a.teamName }}</text>
          <text class="tag tag-green">{{ a.award }}</text>
        </view>
        <view class="ach-intro">{{ a.introduction }}</view>
        <view class="ach-foot">
          <text class="ach-members">队员：{{ a.members }}</text>
          <text class="ach-year">{{ a.year }}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作条 -->
    <view class="footer-bar">
      <view class="btn-plain btn-sm fav-foot" @tap="toggleFavorite">
        <text :class="['fav-ico', isFav ? 'on' : '']">{{ isFav ? '★' : '☆' }}</text>
        <text>{{ isFav ? '已收藏' : '收藏' }}</text>
      </view>
      <view class="btn-ghost btn-sm" @tap="openReminder">设置提醒</view>
      <view class="btn-primary btn-sm" @tap="goTeamList">找搭子</view>
    </view>
  </view>

  <empty-state v-else-if="!loading" icon="⚠️" text="赛事不存在" sub="可能已下架或链接有误" />
</template>

<script>
/**
 * 赛事详情页
 * 参数: id 赛事ID
 * 行为: 收藏、设置提醒、跳组队、展示往届成果
 */
import { competitionApi, favoriteApi, reminderApi, achievementApi } from '@/utils/api.js'
import {
  formatTime, calcRegStatus, regStatusClass, difficultyName, difficultyClass,
  getUserId
} from '@/utils/auth.js'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { EmptyState },
  data() {
    return {
      id: null,
      detail: {},
      achievements: [],
      isFav: false,
      loading: false
    }
  },
  computed: {
    diffName() { return difficultyName(this.detail.difficulty) },
    diffClass() { return difficultyClass(this.detail.difficulty) },
    regStatus() { return calcRegStatus(this.detail) },
    statusClass() { return regStatusClass(calcRegStatus(this.detail)) },
    regDotClass() {
      const s = calcRegStatus(this.detail)
      return { '报名中': 'ing', '未开始': 'wait', '已截止': 'end' }[s] || 'wait'
    }
  },
  onLoad(options) {
    this.id = options.id || options.competitionId
    this.loadDetail()
    this.loadAchievements()
    this.checkFavorite()
  },
  methods: {
    formatTime,

    async loadDetail() {
      if (!this.id) return
      this.loading = true
      try {
        this.detail = await competitionApi.detail(this.id)
        uni.setNavigationBarTitle({ title: this.detail.title || '赛事详情' })
      } catch (e) {
        console.error('加载赛事详情失败', e)
        this.detail = {}
      } finally {
        this.loading = false
      }
    },

    async loadAchievements() {
      try {
        const rows = await achievementApi.list(this.id)
        this.achievements = Array.isArray(rows) ? rows : []
      } catch (e) {
        this.achievements = []
      }
    },

    async checkFavorite() {
      try {
        this.isFav = await favoriteApi.check(getUserId(), this.id)
      } catch (e) {
        this.isFav = false
      }
    },

    async toggleFavorite() {
      const uid = getUserId()
      try {
        if (this.isFav) {
          await favoriteApi.remove(uid, this.id)
          this.isFav = false
          uni.showToast({ title: '已取消收藏', icon: 'none' })
        } else {
          await favoriteApi.add(uid, this.id)
          this.isFav = true
          uni.showToast({ title: '已加入收藏', icon: 'success' })
        }
      } catch (e) {
        console.error('收藏操作失败', e)
      }
    },

    /** 设置提醒：选择提醒时机 */
    openReminder() {
      if (!this.detail.regEndTime) {
        uni.showToast({ title: '该赛事暂无报名时间', icon: 'none' })
        return
      }
      const that = this
      uni.showActionSheet({
        itemList: ['报名开始时提醒我', '报名截止前提醒我'],
        success(res) {
          that.submitReminder(res.tapIndex + 1)
        }
      })
    },

    async submitReminder(remindType) {
      try {
        await reminderApi.add({
          userId: getUserId(),
          competitionId: this.id,
          remindType,
          remindTime: remindType === 1 ? this.detail.regStartTime : this.detail.regEndTime
        })
        uni.showToast({ title: '提醒设置成功', icon: 'success' })
      } catch (e) {
        console.error('设置提醒失败', e)
      }
    },

    copyRegInfo() {
      if (!this.detail.regInfo) return
      uni.setClipboardData({
        data: this.detail.regInfo,
        success: () => uni.showToast({ title: '已复制报名信息', icon: 'none' })
      })
    },

    goTeamList() {
      uni.navigateTo({ url: `/pages/team/list?competitionId=${this.id}` })
    },
    goTeamPublish() {
      uni.navigateTo({ url: `/pages/team/publish?competitionId=${this.id}` })
    }
  }
}
</script>

<style scoped>
.title { font-size: 38rpx; font-weight: 700; line-height: 1.4; color: #303133; margin-bottom: 20rpx; }
.meta { margin-bottom: 24rpx; }

.organizer { display: flex; align-items: center; gap: 14rpx; margin-bottom: 16rpx; }
.organizer .label {
  font-size: 22rpx; color: #909399; background: #F5F6FA;
  padding: 4rpx 14rpx; border-radius: 6rpx; flex-shrink: 0;
}
.organizer .value { font-size: 26rpx; color: #303133; flex: 1; }

.stats { font-size: 24rpx; color: #909399; display: flex; align-items: center; gap: 10rpx; }
.dot { color: #DCDFE6; }

/* 时间轴 */
.timeline { padding-left: 8rpx; }
.tl-item { display: flex; gap: 20rpx; position: relative; padding-bottom: 32rpx; }
.tl-item.last { padding-bottom: 0; }
.tl-item::before {
  content: ''; position: absolute; left: 11rpx; top: 28rpx; bottom: 0;
  width: 2rpx; background: #EBEEF5;
}
.tl-item.last::before { display: none; }
.tl-dot {
  width: 24rpx; height: 24rpx; border-radius: 50%; flex-shrink: 0;
  margin-top: 6rpx; background: #DCDFE6;
}
.tl-dot.start { background: #909399; }
.tl-dot.ing { background: #52C41A; }
.tl-dot.wait { background: #FA8C16; }
.tl-dot.end { background: #C0C4CC; }
.tl-dot.match { background: #4A6FE3; }
.tl-body { flex: 1; }
.tl-label { font-size: 28rpx; color: #303133; font-weight: 500; margin-bottom: 6rpx; }
.tl-label .reg-status { margin-left: 12rpx; font-size: 24rpx; }
.tl-time { font-size: 26rpx; color: #606266; }

/* 富文本 */
.rich-text { font-size: 27rpx; color: #606266; line-height: 1.75; white-space: pre-wrap; word-break: break-all; }
.copy-tip {
  margin-top: 20rpx; text-align: center; font-size: 26rpx; color: #4A6FE3;
  background: #ECF0FF; border-radius: 12rpx; padding: 18rpx;
}

/* 组队入口 */
.team-entry { display: flex; gap: 20rpx; }
.te-box {
  flex: 1; background: #F7F8FA; border-radius: 16rpx; padding: 32rpx 20rpx;
  display: flex; flex-direction: column; align-items: center;
}
.te-icon { font-size: 48rpx; margin-bottom: 12rpx; }
.te-title { font-size: 28rpx; font-weight: 600; color: #303133; margin-bottom: 6rpx; }
.te-sub { font-size: 22rpx; color: #909399; }

/* 往届成果 */
.ach-item { padding: 22rpx 0; border-bottom: 1rpx solid #F2F3F5; }
.ach-item:last-child { border-bottom: none; padding-bottom: 0; }
.ach-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12rpx; }
.ach-team { font-size: 28rpx; font-weight: 600; color: #303133; }
.ach-intro { font-size: 26rpx; color: #606266; line-height: 1.6; margin-bottom: 12rpx; }
.ach-foot { display: flex; justify-content: space-between; font-size: 24rpx; color: #909399; }

/* 底部 */
.fav-foot { display: flex; align-items: center; justify-content: center; gap: 8rpx; flex: 0 0 auto; width: 180rpx; }
.fav-ico { font-size: 32rpx; color: #C0C4CC; }
.fav-ico.on { color: #FA8C16; }
</style>
