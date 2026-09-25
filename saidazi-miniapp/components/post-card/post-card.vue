<template>
  <view class="post-card card" @tap="$emit('tap', item)">
    <!-- 作者 -->
    <view class="head">
      <view class="avatar avatar-sm">
        <image v-if="item.author && item.author.avatarUrl" :src="item.author.avatarUrl" mode="aspectFill" />
        <text v-else>{{ avatarTxt }}</text>
      </view>
      <view class="flex-1">
        <view class="nickname ellipsis">{{ authorName }}</view>
        <view class="sub ellipsis">{{ authorInfo }}</view>
      </view>
      <text class="tag" :class="catClass">{{ catName }}</text>
    </view>

    <!-- 标题 + 内容摘要 -->
    <view class="title ellipsis-2">{{ item.title }}</view>
    <view class="content ellipsis-3" v-if="item.content">{{ item.content }}</view>

    <!-- 图片缩略（最多3张） -->
    <view class="images" v-if="images.length">
      <view class="img-box" v-for="(img, i) in images" :key="i">
        <image :src="img" mode="aspectFill" />
      </view>
    </view>

    <!-- 底部互动 -->
    <view class="foot">
      <text class="time">{{ timeAgo }}</text>
      <view class="actions">
        <view class="act" :class="{ on: item.liked }" @tap.stop="$emit('like', item)">
          <text class="act-icon">{{ item.liked ? '♥' : '♡' }}</text>
          <text class="act-num">{{ item.likeCount || 0 }}</text>
        </view>
        <view class="act" @tap.stop="$emit('comment', item)">
          <text class="act-icon">💬</text>
          <text class="act-num">{{ item.commentCount || 0 }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
/**
 * 帖子卡片
 * props: item 帖子对象
 * 事件: tap(item) / like(item) / comment(item)
 */
import { postCategoryName, fromNow, avatarText } from '@/utils/auth.js'

export default {
  name: 'PostCard',
  props: {
    item: { type: Object, required: true }
  },
  computed: {
    authorName() { return (this.item.author && this.item.author.nickname) || '匿名同学' },
    authorInfo() {
      const a = this.item.author || {}
      return [a.college, a.grade].filter(Boolean).join(' · ') || '—'
    },
    avatarTxt() { return avatarText(this.authorName) },
    catName() { return postCategoryName(this.item.category) },
    catClass() {
      return { 1: 'tag-orange', 2: 'tag-green', 3: '', 4: 'tag-gray' }[Number(this.item.category)] || 'tag-gray'
    },
    images() { return (this.item.images || []).slice(0, 3) },
    timeAgo() { return fromNow(this.item.createTime) }
  }
}
</script>

<style scoped>
.head { display: flex; align-items: center; gap: 16rpx; margin-bottom: 20rpx; }
.nickname { font-size: 28rpx; font-weight: 500; color: #303133; }
.sub { font-size: 24rpx; color: #909399; margin-top: 4rpx; }

.title { font-size: 30rpx; font-weight: 600; color: #303133; line-height: 1.45; margin-bottom: 12rpx; }
.content { font-size: 26rpx; color: #606266; line-height: 1.6; }

.images { display: flex; gap: 12rpx; margin-top: 16rpx; }
.img-box { flex: 1; height: 180rpx; border-radius: 12rpx; overflow: hidden; background: #F2F3F5; }
.img-box image { width: 100%; height: 100%; }

.foot { display: flex; align-items: center; justify-content: space-between; margin-top: 20rpx; }
.time { font-size: 24rpx; color: #909399; }
.actions { display: flex; align-items: center; gap: 36rpx; }
.act { display: flex; align-items: center; gap: 6rpx; }
.act-icon { font-size: 30rpx; color: #909399; }
.act.on .act-icon { color: #FF4D4F; }
.act-num { font-size: 24rpx; color: #909399; }
</style>
