<template>
  <view class="container">
    <!-- 状态筛选 -->
    <view class="filter-bar">
      <text
        v-for="f in filters"
        :key="f.value"
        class="filter-chip"
        :class="{ active: activeFilter === f.value }"
        @tap="activeFilter = f.value"
      >{{ f.label }}</text>
    </view>

    <template v-if="filtered.length">
      <view class="rem-card card" v-for="item in filtered" :key="item.id">
        <view class="rc-head">
          <text class="rc-type" :class="'rt-' + item.remindType">
            {{ item.remindType === 1 ? '报名开始' : '报名截止' }}
          </text>
          <text class="tag" :class="statusTagClass(item.status)">{{ statusName(item.status) }}</text>
        </view>

        <view class="rc-title ellipsis-2" @tap="goDetail(item)">{{ item.competitionTitle }}</view>

        <view class="rc-time">
          <text class="rt-label">提醒时间</text>
          <text class="rt-value">{{ formatTime(item.remindTime) }}</text>
        </view>

        <view class="rc-countdown" v-if="Number(item.status) === 0">
          <text class="cd-text">{{ countdownText(item) }}</text>
        </view>

        <view class="rc-actions">
          <view class="btn-plain btn-sm" @tap="goDetail(item)">查看赛事</view>
          <view
            class="btn-ghost btn-sm"
            v-if="Number(item.status) === 0"
            @tap="confirmCancel(item)"
          >取消提醒</view>
          <view class="btn-plain btn-sm" v-else @tap="reAdd(item)">重新设置</view>
        </view>
      </view>
    </template>

    <empty-state
      v-else-if="!loading"
      icon="🔔"
      text="暂无提醒"
      sub="在赛事详情页设置提醒，不错过报名时间"
      btn-text="去逛逛赛事"
      @action="goHome"
    />
  </view>
</template>

<script>
/**
 * 提醒管理页
 * 展示报名提醒列表，支持取消 / 重新设置
 */
import { reminderApi } from '@/utils/api.js'
import { formatTime, getUserId } from '@/utils/auth.js'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { EmptyState },
  data() {
    return {
      list: [],
      loading: false,
      activeFilter: -1,
      filters: [
        { label: '全部', value: -1 },
        { label: '待触发', value: 0 },
        { label: '已触发', value: 1 }
      ]
    }
  },
  computed: {
    filtered() {
      if (this.activeFilter === -1) return this.list
      return this.list.filter((i) => Number(i.status) === this.activeFilter)
    }
  },
  onShow() {
    this.loadList()
  },
  methods: {
    formatTime,

    async loadList() {
      this.loading = true
      try {
        const rows = await reminderApi.list(getUserId())
        this.list = Array.isArray(rows) ? rows : []
      } catch (e) {
        console.error('加载提醒失败', e)
        this.list = []
      } finally {
        this.loading = false
      }
    },

    statusName(s) {
      return { 0: '待触发', 1: '已触发', 2: '已取消' }[Number(s)] || '未知'
    },
    statusTagClass(s) {
      return { 0: 'tag-orange', 1: 'tag-green', 2: 'tag-gray' }[Number(s)] || 'tag-gray'
    },

    /** 距提醒时间的倒计时文案 */
    countdownText(item) {
      if (!item.remindTime) return ''
      const t = new Date(item.remindTime.replace(/-/g, '/')).getTime()
      const diff = t - Date.now()
      if (diff <= 0) return '即将触发'
      const days = Math.floor(diff / 86400000)
      const hours = Math.floor((diff % 86400000) / 3600000)
      if (days > 0) return `还有 ${days} 天 ${hours} 小时`
      if (hours > 0) return `还有 ${hours} 小时`
      return `还有 ${Math.max(1, Math.floor(diff / 60000))} 分钟`
    },

    confirmCancel(item) {
      const that = this
      uni.showModal({
        title: '取消提醒',
        content: `确定取消《${item.competitionTitle}》的报名提醒吗？`,
        success(res) {
          if (res.confirm) that.doCancel(item)
        }
      })
    },

    async doCancel(item) {
      try {
        await reminderApi.remove(item.id, getUserId())
        this.list = this.list.filter((i) => i.id !== item.id)
        uni.showToast({ title: '已取消提醒', icon: 'none' })
      } catch (e) {
        console.error('取消提醒失败', e)
      }
    },

    async reAdd(item) {
      try {
        const data = await reminderApi.add({
          userId: getUserId(),
          competitionId: item.competitionId,
          remindType: item.remindType,
          remindTime: item.remindTime
        })
        if (data) {
          item.status = 0
          item.isPushed = 0
        }
        uni.showToast({ title: '已重新设置', icon: 'success' })
      } catch (e) {
        console.error('重新设置提醒失败', e)
      }
    },

    goDetail(item) {
      uni.navigateTo({ url: `/pages/competition/detail?id=${item.competitionId}` })
    },
    goHome() {
      uni.switchTab({ url: '/pages/index/index' })
    }
  }
}
</script>

<style scoped>
.filter-bar { background: transparent; padding: 0 0 20rpx; }

.rem-card { padding: 28rpx; }
.rc-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18rpx; }
.rc-type {
  font-size: 22rpx; padding: 5rpx 16rpx; border-radius: 8rpx;
  background: #ECF0FF; color: #4A6FE3;
}
.rc-type.rt-2 { background: #FFF7E6; color: #FA8C16; }

.rc-title { font-size: 30rpx; font-weight: 600; color: #303133; line-height: 1.45; margin-bottom: 20rpx; }

.rc-time { display: flex; align-items: center; gap: 14rpx; margin-bottom: 16rpx; }
.rt-label { font-size: 22rpx; color: #909399; background: #F5F6FA; padding: 3rpx 12rpx; border-radius: 6rpx; }
.rt-value { font-size: 26rpx; color: #606266; }

.rc-countdown { margin-bottom: 4rpx; }
.cd-text {
  display: inline-block; font-size: 24rpx; color: #52C41A;
  background: #E7F8EE; padding: 6rpx 18rpx; border-radius: 8rpx;
}

.rc-actions {
  display: flex; gap: 20rpx; margin-top: 26rpx;
  padding-top: 26rpx; border-top: 1rpx solid #F2F3F5;
}
.rc-actions .btn-plain,
.rc-actions .btn-ghost { flex: 1; }
</style>
