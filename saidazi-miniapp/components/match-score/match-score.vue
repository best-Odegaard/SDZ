<template>
  <view class="match-score">
    <view class="bar-wrap">
      <view class="bar-bg">
        <view class="bar-fill" :style="{ width: percent + '%', background: barColor }"></view>
      </view>
      <text class="score-text" :style="{ color: barColor }">{{ scoreText }}</text>
    </view>
    <view class="tags" v-if="tags.length">
      <text class="tag tag-green" v-for="(t, i) in tags" :key="i">{{ t }}</text>
    </view>
  </view>
</template>

<script>
/**
 * 匹配度进度条
 * props: score 分数(0-100) / tags 共同标签数组
 */
export default {
  name: 'MatchScore',
  props: {
    score: { type: [Number, String], default: 0 },
    tags: { type: Array, default: () => [] }
  },
  computed: {
    percent() {
      const n = Number(this.score) || 0
      return Math.max(0, Math.min(100, n))
    },
    scoreText() {
      return `${Number(this.score || 0).toFixed(1)} 分`
    },
    barColor() {
      const n = this.percent
      if (n >= 85) return '#52C41A'
      if (n >= 70) return '#4A6FE3'
      if (n >= 50) return '#FA8C16'
      return '#909399'
    }
  }
}
</script>

<style scoped>
.bar-wrap { display: flex; align-items: center; gap: 16rpx; }
.bar-bg { flex: 1; height: 14rpx; background: #F2F3F5; border-radius: 7rpx; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 7rpx; transition: width 0.4s ease; }
.score-text { font-size: 26rpx; font-weight: 600; flex-shrink: 0; }
.tags { margin-top: 14rpx; }
</style>
