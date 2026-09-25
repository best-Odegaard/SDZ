<template>
  <view class="community-page">
    <!-- 分类 Tab -->
    <scroll-view class="scroll-tabs" scroll-x :show-scrollbar="false">
      <text
        v-for="c in categories"
        :key="c.value"
        class="st-item"
        :class="{ active: activeCat === c.value }"
        @tap="switchCat(c.value)"
      >{{ c.label }}</text>
    </scroll-view>

    <view class="body">
      <!-- 帖子列表 -->
      <template v-if="list.length">
        <post-card
          v-for="item in list"
          :key="item.id"
          :item="item"
          @tap="goDetail"
          @like="toggleLike"
        />

        <view class="load-more" @tap="loadMore">
          {{ hasMore ? '加载更多' : '— 没有更多了 —' }}
        </view>
      </template>

      <empty-state
        v-else-if="!loading"
        icon="💬"
        text="这个分类还没有帖子"
        sub="来发第一帖，聊聊你的竞赛心得"
        btn-text="去发帖"
        @action="goPublish"
      />
    </view>

    <!-- 悬浮发帖按钮 -->
    <view class="fab" @tap="goPublish">
      <text class="fab-icon">＋</text>
    </view>
  </view>
</template>

<script>
import { postApi } from '@/utils/api.js'
import { POST_CATEGORIES, getUserId } from '@/utils/auth.js'
import PostCard from '@/components/post-card/post-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { PostCard, EmptyState },
  data() {
    return {
      categories: [{ value: 0, label: '全部' }].concat(POST_CATEGORIES),
      activeCat: 0,
      list: [],
      page: 1,
      size: 10,
      total: 0,
      hasMore: true,
      loading: false
    }
  },
  onLoad() {
    this.loadList(true)
  },
  onPullDownRefresh() {
    this.loadList(true).finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    async loadList(reset = false) {
      if (this.loading) return
      this.loading = true
      if (reset) { this.page = 1; this.hasMore = true; this.list = [] }
      try {
        const params = { page: this.page, size: this.size }
        if (this.activeCat) params.category = this.activeCat
        const data = await postApi.list(params)
        const rows = Array.isArray(data) ? data : (data.list || data.records || [])
        this.total = (data && data.total) || rows.length
        this.list = reset ? rows : this.list.concat(rows)
        this.hasMore = this.list.length < this.total && rows.length > 0
      } catch (e) {
        console.error('加载帖子失败', e)
      } finally {
        this.loading = false
      }
    },

    switchCat(v) {
      if (this.activeCat === v) return
      this.activeCat = v
      this.loadList(true)
    },

    loadMore() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadList(false)
    },

    async toggleLike(item) {
      try {
        const res = await postApi.like(item.id, getUserId())
        if (res && res.liked !== undefined) {
          item.liked = res.liked
          item.likeCount = res.likeCount
        }
      } catch (e) {
        console.error('点赞失败', e)
      }
    },

    goDetail(item) {
      uni.navigateTo({ url: `/pages/community/post-detail?id=${item.id}` })
    },
    goPublish() {
      uni.navigateTo({ url: '/pages/community/publish' })
    }
  }
}
</script>

<style scoped>
.community-page { min-height: 100vh; background: #F5F6FA; }
.body { padding: 20rpx; }

.fab {
  position: fixed; right: 40rpx; bottom: 60rpx;
  width: 104rpx; height: 104rpx; border-radius: 50%;
  background: #4A6FE3; color: #fff;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(74, 111, 227, 0.4);
  z-index: 80;
}
.fab-icon { font-size: 52rpx; line-height: 1; margin-top: -4rpx; }
</style>
