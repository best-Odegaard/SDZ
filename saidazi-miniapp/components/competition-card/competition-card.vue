<template>
  <view class="comp-card card" @tap="$emit('tap', item)">
    <view class="comp-head">
      <view class="comp-title ellipsis-2">{{ item.title }}</view>
      <view class="fav-btn" @tap.stop="$emit('favorite', item)">
        <text :class="['fav-icon', isFav ? 'active' : '']">{{ isFav ? '★' : '☆' }}</text>
      </view>
    </view>

    <view class="comp-meta">
      <text class="tag" :class="diffClass">{{ diffName }}</text>
      <text class="tag" v-if="item.category">{{ item.category }}</text>
      <text class="tag tag-gray" v-if="item.collegeTag">{{ item.collegeTag }}</text>
      <text class="tag tag-gray" v-if="item.type === 1">全国</text>
    </view>

    <view class="comp-organizer ellipsis">
      <text class="oi-label">主办</text>
      <text class="oi-value">{{ item.organizer || '—' }}</text>
    </view>

    <view class="comp-foot">
      <text class="reg-status" :class="statusClass">{{ regStatus }}</text>
      <text class="reg-time">{{ timeText }}</text>
    </view>
  </view>
</template>

<script>
/**
 * 赛事卡片
 * props: item 赛事对象 / isFav 是否已收藏
 * 事件: tap(item) / favorite(item)
 */
import { calcRegStatus, regStatusClass, difficultyName, difficultyClass, daysToRegEnd } from '@/utils/auth.js'

export default {
  name: 'CompetitionCard',
  props: {
    item: { type: Object, required: true },
    isFav: { type: Boolean, default: false }
  },
  computed: {
    diffName() { return difficultyName(this.item.difficulty) },
    diffClass() { return difficultyClass(this.item.difficulty) },
    regStatus() { return calcRegStatus(this.item) },
    statusClass() { return regStatusClass(calcRegStatus(this.item)) },
    timeText() {
      if (!this.item.regEndTime) return '时间待定'
      const d = daysToRegEnd(this.item)
      if (d === null) return '时间待定'
      if (d < 0) return '报名已结束'
      if (d === 0) return '今天截止'
      return `${d} 天后截止`
    }
  }
}
</script>

<style scoped>
.comp-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 16rpx; }
.comp-title { flex: 1; font-size: 32rpx; font-weight: 600; line-height: 1.45; color: #303133; }
.fav-btn { padding: 0 4rpx; flex-shrink: 0; }
.fav-icon { font-size: 40rpx; color: #C0C4CC; line-height: 1; }
.fav-icon.active { color: #FA8C16; }

.comp-meta { margin: 16rpx 0 14rpx; }

.comp-organizer { font-size: 26rpx; color: #606266; margin-bottom: 16rpx; }
.oi-label {
  display: inline-block;
  font-size: 22rpx;
  color: #909399;
  background: #F5F6FA;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
  margin-right: 12rpx;
}

.comp-foot { display: flex; justify-content: space-between; align-items: center; }
.reg-time { color: #909399; font-size: 24rpx; }
</style>
