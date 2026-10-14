<template>
  <view class="team-card card">
    <!-- 发布人 -->
    <view class="head">
      <view class="avatar avatar-sm">
        <image v-if="item.publisher && item.publisher.avatarUrl" :src="item.publisher.avatarUrl" mode="aspectFill" />
        <text v-else>{{ avatarTxt }}</text>
      </view>
      <view class="flex-1">
        <view class="nickname ellipsis">{{ publisherName }}</view>
        <view class="sub-info ellipsis">{{ publisherInfo }}</view>
      </view>
      <text class="tag" :class="statusTagClass">{{ statusName }}</text>
    </view>

    <!-- 标题 -->
    <view class="title ellipsis-2">{{ item.title }}</view>

    <!-- 关联赛事 -->
    <view class="comp-line ellipsis" v-if="item.competitionTitle">
      <text class="comp-badge">赛事</text>{{ item.competitionTitle }}
    </view>

    <!-- 技能标签 -->
    <view class="skills" v-if="skills.length">
      <text class="tag" v-for="(s, i) in skills" :key="i" :class="matchTags.includes(s) ? 'tag-green' : ''">{{ s }}</text>
    </view>

    <!-- 底部：人数 / 匹配度 / 时间 -->
    <view class="foot">
      <view class="foot-left">
        <text class="people">还差 {{ lackCount }} 人</text>
        <text class="dot">·</text>
        <text class="time">{{ timeAgo }}</text>
      </view>
      <view class="match" v-if="matchScore !== null">
        <text class="match-num">{{ matchScore }}</text>
        <text class="match-unit">分</text>
      </view>
    </view>
  </view>
</template>

<script>
/**
 * 组队卡片
 * props: item 组队对象 / matchTags 高亮标签数组
 * 事件: tap(item)
 */
import { teamStatusName, fromNow, avatarText } from '@/utils/auth.js'

export default {
  name: 'TeamCard',
  props: {
    item: { type: Object, required: true },
    matchTags: { type: Array, default: () => [] }
  },
  computed: {
    publisherName() {
      return (this.item.publisher && this.item.publisher.nickname) || '匿名同学'
    },
    publisherInfo() {
      const p = this.item.publisher || {}
      return [p.college, p.grade].filter(Boolean).join(' · ') || '—'
    },
    avatarTxt() { return avatarText(this.publisherName) },
    skills() { return this.item.skillsRequired || [] },
    statusName() { return teamStatusName(this.item.status) },
    statusTagClass() {
      return { 0: 'tag-green', 1: 'tag-orange', 2: 'tag-gray' }[Number(this.item.status)] || 'tag-gray'
    },
    lackCount() {
      const need = Number(this.item.needCount) || 0
      const cur = Number(this.item.currentCount) || 0
      const left = need - cur
      return left > 0 ? left : 0
    },
    timeAgo() { return fromNow(this.item.createTime) },
    matchScore() {
      return this.item.matchScore === undefined || this.item.matchScore === null
        ? null
        : Number(this.item.matchScore).toFixed(1)
    }
  }
}
</script>

<style scoped>
.head { display: flex; align-items: center; gap: 16rpx; margin-bottom: 20rpx; }
.nickname { font-size: 28rpx; font-weight: 500; color: #303133; }
.sub-info { font-size: 24rpx; color: #909399; margin-top: 4rpx; }

.title { font-size: 30rpx; font-weight: 600; color: #303133; line-height: 1.45; margin-bottom: 14rpx; }

.comp-line { font-size: 24rpx; color: #606266; background: #F7F8FA; padding: 12rpx 16rpx; border-radius: 10rpx; margin-bottom: 14rpx; }
.comp-badge {
  display: inline-block; font-size: 20rpx; color: #4A6FE3; background: #ECF0FF;
  padding: 2rpx 10rpx; border-radius: 6rpx; margin-right: 10rpx;
}

.skills { margin-bottom: 16rpx; }

.foot { display: flex; align-items: center; justify-content: space-between; }
.foot-left { display: flex; align-items: center; gap: 8rpx; }
.people { font-size: 24rpx; color: #FA8C16; font-weight: 500; }
.dot { color: #DCDFE6; font-size: 22rpx; }
.time { font-size: 24rpx; color: #909399; }

.match { display: flex; align-items: baseline; gap: 2rpx; }
.match-num { font-size: 34rpx; font-weight: 700; color: #4A6FE3; }
.match-unit { font-size: 22rpx; color: #4A6FE3; }
</style>
