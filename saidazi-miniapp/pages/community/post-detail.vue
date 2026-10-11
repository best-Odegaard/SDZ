<template>
  <view class="detail-page" v-if="post.id">
    <!-- 帖子主体 -->
    <view class="card">
      <view class="head">
        <view class="avatar avatar-sm">
          <text>{{ avatarTxt }}</text>
        </view>
        <view class="flex-1">
          <view class="nickname">{{ authorName }}</view>
          <view class="sub">{{ authorInfo }}</view>
        </view>
        <text class="tag" :class="catClass">{{ catName }}</text>
      </view>

      <view class="title">{{ post.title }}</view>
      <view class="content">{{ post.content }}</view>

      <view class="images" v-if="images.length">
        <image
          v-for="(img, i) in images"
          :key="i"
          :src="img"
          mode="widthFix"
          class="p-img"
          @tap="previewImg(i)"
        />
      </view>

      <view class="post-time">{{ formatTime(post.createTime) }}</view>

      <view class="actions">
        <view class="act" :class="{ on: post.liked }" @tap="toggleLike">
          <text class="act-icon">{{ post.liked ? '♥' : '♡' }}</text>
          <text class="act-text">{{ post.liked ? '已赞' : '点赞' }}</text>
          <text class="act-num">{{ post.likeCount || 0 }}</text>
        </view>
        <view class="act">
          <text class="act-icon">💬</text>
          <text class="act-text">评论</text>
          <text class="act-num">{{ post.commentCount || 0 }}</text>
        </view>
      </view>
    </view>

    <!-- 评论列表 -->
    <view class="card">
      <view class="section-title">
        全部评论
        <text class="more" v-if="comments.length">{{ comments.length }} 条</text>
      </view>

      <view v-if="comments.length">
        <view class="comment" v-for="c in comments" :key="c.id">
          <view class="avatar avatar-sm">
            <text>{{ avatarText(c.user && c.user.nickname) }}</text>
          </view>
          <view class="flex-1">
            <view class="c-head">
              <text class="c-name">{{ (c.user && c.user.nickname) || '匿名同学' }}</text>
              <text class="c-time">{{ fromNow(c.createTime) }}</text>
            </view>
            <view class="c-content">{{ c.content }}</view>
          </view>
        </view>
      </view>

      <view v-else class="no-comment">
        <text>还没有人评论，来说两句吧</text>
      </view>
    </view>

    <!-- 底部评论输入 -->
    <view class="comment-bar" v-if="post.allowComment">
      <input
        class="cb-input"
        v-model="commentText"
        placeholder="友善发言，理性讨论"
        maxlength="200"
        confirm-type="send"
        @confirm="submitComment"
      />
      <view class="cb-btn" :class="{ disabled: !commentText.trim() }" @tap="submitComment">发送</view>
    </view>

    <view class="comment-bar closed" v-else>
      <text class="closed-text">该帖已关闭评论</text>
    </view>
  </view>

  <empty-state v-else-if="!loading" icon="⚠️" text="帖子不存在" sub="可能已被作者删除" />
</template>

<script>
/**
 * 帖子详情页
 * 参数: id 帖子ID
 * 行为: 点赞 / 发表评论 / 图片预览
 */
import { postApi } from '@/utils/api.js'
import {
  formatTime, fromNow, postCategoryName, avatarText, getUserId
} from '@/utils/auth.js'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { EmptyState },
  data() {
    return {
      id: null,
      post: {},
      comments: [],
      commentText: '',
      loading: false
    }
  },
  computed: {
    authorName() { return (this.post.author && this.post.author.nickname) || '匿名同学' },
    authorInfo() {
      const a = this.post.author || {}
      return [a.college, a.grade].filter(Boolean).join(' · ') || '—'
    },
    avatarTxt() { return avatarText(this.authorName) },
    catName() { return postCategoryName(this.post.category) },
    catClass() {
      return { 1: 'tag-orange', 2: 'tag-green', 3: '', 4: 'tag-gray' }[Number(this.post.category)] || 'tag-gray'
    },
    images() { return this.post.images || [] }
  },
  onLoad(options) {
    this.id = options.id
    this.loadDetail()
    this.loadComments()
  },
  methods: {
    formatTime,
    fromNow,
    avatarText,

    async loadDetail() {
      if (!this.id) return
      this.loading = true
      try {
        this.post = await postApi.detail(this.id)
        uni.setNavigationBarTitle({ title: this.post.title || '帖子详情' })
      } catch (e) {
        console.error('加载帖子失败', e)
        this.post = {}
      } finally {
        this.loading = false
      }
    },

    async loadComments() {
      if (!this.id) return
      try {
        const rows = await postApi.comments(this.id)
        this.comments = Array.isArray(rows) ? rows : []
      } catch (e) {
        this.comments = []
      }
    },

    async toggleLike() {
      try {
        const res = await postApi.like(this.id, getUserId())
        if (res && res.liked !== undefined) {
          this.post.liked = res.liked
          this.post.likeCount = res.likeCount
        }
      } catch (e) {
        console.error('点赞失败', e)
      }
    },

    async submitComment() {
      const content = this.commentText.trim()
      if (!content) return
      try {
        const item = await postApi.comment(this.id, { userId: getUserId(), content })
        this.comments.push(item)
        this.post.commentCount = this.comments.length
        this.commentText = ''
        uni.showToast({ title: '评论成功', icon: 'success' })
      } catch (e) {
        console.error('评论失败', e)
      }
    },

    previewImg(index) {
      uni.previewImage({ current: index, urls: this.images })
    }
  }
}
</script>

<style scoped>
.detail-page { padding: 20rpx; padding-bottom: 160rpx; }

.head { display: flex; align-items: center; gap: 16rpx; margin-bottom: 24rpx; }
.nickname { font-size: 28rpx; font-weight: 500; color: #303133; }
.sub { font-size: 24rpx; color: #909399; margin-top: 4rpx; }

.title { font-size: 36rpx; font-weight: 700; line-height: 1.45; color: #303133; margin-bottom: 22rpx; }
.content { font-size: 29rpx; color: #303133; line-height: 1.8; white-space: pre-wrap; word-break: break-all; }

.images { margin-top: 24rpx; }
.p-img { width: 100%; border-radius: 12rpx; margin-bottom: 16rpx; }

.post-time { font-size: 24rpx; color: #C0C4CC; margin-top: 24rpx; }

.actions {
  display: flex; gap: 60rpx; margin-top: 28rpx;
  padding-top: 28rpx; border-top: 1rpx solid #F2F3F5;
}
.act { display: flex; align-items: center; gap: 8rpx; }
.act-icon { font-size: 34rpx; color: #909399; }
.act.on .act-icon { color: #FF4D4F; }
.act-text { font-size: 26rpx; color: #606266; }
.act-num { font-size: 26rpx; color: #909399; }

/* 评论 */
.comment { display: flex; gap: 18rpx; padding: 24rpx 0; border-bottom: 1rpx solid #F7F8FA; }
.comment:last-child { border-bottom: none; padding-bottom: 0; }
.c-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10rpx; }
.c-name { font-size: 26rpx; color: #4A6FE3; font-weight: 500; }
.c-time { font-size: 22rpx; color: #C0C4CC; }
.c-content { font-size: 27rpx; color: #303133; line-height: 1.6; word-break: break-all; }

.no-comment { text-align: center; padding: 60rpx 0; font-size: 26rpx; color: #909399; }

/* 评论输入栏 */
.comment-bar {
  position: fixed; left: 0; right: 0; bottom: 0;
  display: flex; align-items: center; gap: 16rpx;
  background: #fff; padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.05);
  z-index: 90;
}
.cb-input {
  flex: 1; height: 72rpx; background: #F5F6FA; border-radius: 36rpx;
  padding: 0 26rpx; font-size: 27rpx; box-sizing: border-box;
}
.cb-btn {
  padding: 0 34rpx; height: 72rpx; line-height: 72rpx;
  background: #4A6FE3; color: #fff; border-radius: 36rpx; font-size: 27rpx;
}
.cb-btn.disabled { background: #A8B9EE; }
.comment-bar.closed { justify-content: center; }
.closed-text { font-size: 26rpx; color: #909399; }
</style>
