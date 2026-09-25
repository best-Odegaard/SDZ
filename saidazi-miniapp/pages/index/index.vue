<template>
  <view class="container page-with-footer-none">
    <!-- 顶部搜索 + 日历入口 -->
    <view class="top-bar">
      <view class="search-input" @tap="goSearch">
        <text class="search-icon">🔍</text>
        <text class="placeholder">搜索赛事名称 / 主办方</text>
      </view>
      <view class="cal-entry" @tap="goCalendar">
        <text class="cal-icon">📅</text>
        <text class="cal-text">日历</text>
      </view>
    </view>

    <!-- 校内 / 全国 Tab -->
    <view class="tabs">
      <view :class="['tab', activeType === 0 ? 'active' : '']" @tap="switchType(0)">校内赛事</view>
      <view :class="['tab', activeType === 1 ? 'active' : '']" @tap="switchType(1)">全国赛事</view>
    </view>

    <!-- 分类筛选 -->
    <scroll-view class="filter-bar" scroll-x :show-scrollbar="false">
      <text
        v-for="c in categories"
        :key="c"
        class="filter-chip"
        :class="{ active: activeCategory === c }"
        @tap="switchCategory(c)"
      >{{ c }}</text>
    </scroll-view>

    <!-- 赛事列表 -->
    <view v-if="loading && !list.length" class="skeleton-wrap">
      <view class="card" v-for="i in 3" :key="i">
        <view class="skeleton" style="height:40rpx;width:70%;margin-bottom:20rpx"></view>
        <view class="skeleton" style="height:32rpx;width:40%;margin-bottom:24rpx"></view>
        <view class="skeleton" style="height:28rpx;width:100%"></view>
      </view>
    </view>

    <template v-else>
      <competition-card
        v-for="item in list"
        :key="item.id"
        :item="item"
        :is-fav="favIds.includes(item.id)"
        @tap="goDetail"
        @favorite="toggleFavorite"
      />

      <empty-state
        v-if="!list.length"
        icon="🔍"
        text="暂无相关赛事"
        sub="换个分类或切换校内 / 全国看看"
      />

      <view class="load-more" v-if="list.length" @tap="loadMore">
        {{ hasMore ? '加载更多' : '— 没有更多了 —' }}
      </view>
    </template>
  </view>
</template>

<script>
import { competitionApi, favoriteApi } from '@/utils/api.js'
import { COMPETITION_CATEGORIES, getUserId } from '@/utils/auth.js'
import CompetitionCard from '@/components/competition-card/competition-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { CompetitionCard, EmptyState },
  data() {
    return {
      activeType: 0,          // 0=校内 1=全国
      categories: COMPETITION_CATEGORIES,
      activeCategory: '全部',
      list: [],
      favIds: [],
      page: 1,
      size: 10,
      total: 0,
      hasMore: true,
      loading: false
    }
  },
  onLoad() {
    this.loadFavorites()
    this.loadList(true)
  },
  onPullDownRefresh() {
    this.loadList(true).finally(() => uni.stopPullDownRefresh())
  },
  onShow() {
    // 从详情页返回时刷新收藏态
    this.loadFavorites()
  },
  methods: {
    async loadList(reset = false) {
      if (this.loading) return
      this.loading = true
      if (reset) { this.page = 1; this.hasMore = true; this.list = [] }
      try {
        const params = {
          type: this.activeType,
          status: 1,
          page: this.page,
          size: this.size
        }
        if (this.activeCategory !== '全部') params.category = this.activeCategory
        const data = await competitionApi.list(params)
        const rows = Array.isArray(data) ? data : (data.list || data.records || [])
        this.total = (data && data.total) || rows.length
        this.list = reset ? rows : this.list.concat(rows)
        this.hasMore = this.list.length < this.total && rows.length > 0
      } catch (e) {
        console.error('加载赛事失败', e)
      } finally {
        this.loading = false
      }
    },

    async loadFavorites() {
      try {
        const rows = await favoriteApi.list(getUserId())
        this.favIds = (Array.isArray(rows) ? rows : []).map((f) => f.competitionId)
      } catch (e) {
        this.favIds = []
      }
    },

    async toggleFavorite(item) {
      const uid = getUserId()
      const isFav = this.favIds.includes(item.id)
      try {
        if (isFav) {
          await favoriteApi.remove(uid, item.id)
          this.favIds = this.favIds.filter((id) => id !== item.id)
          uni.showToast({ title: '已取消收藏', icon: 'none' })
        } else {
          await favoriteApi.add(uid, item.id)
          this.favIds = this.favIds.concat([item.id])
          uni.showToast({ title: '已加入收藏', icon: 'success' })
        }
      } catch (e) {
        console.error('收藏操作失败', e)
      }
    },

    switchType(t) {
      if (this.activeType === t) return
      this.activeType = t
      this.loadList(true)
    },

    switchCategory(c) {
      if (this.activeCategory === c) return
      this.activeCategory = c
      this.loadList(true)
    },

    loadMore() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadList(false)
    },

    goDetail(item) {
      uni.navigateTo({ url: `/pages/competition/detail?id=${item.id}` })
    },
    goSearch() {
      uni.showToast({ title: '搜索页开发中', icon: 'none' })
    },
    goCalendar() {
      uni.navigateTo({ url: '/pages/calendar/index' })
    }
  }
}
</script>

<style scoped>
.top-bar { display: flex; align-items: center; gap: 16rpx; margin-bottom: 20rpx; }
.search-input {
  flex: 1; height: 76rpx; background: #fff; border-radius: 38rpx;
  display: flex; align-items: center; padding: 0 26rpx;
}
.search-icon { font-size: 28rpx; margin-right: 12rpx; opacity: 0.6; }
.placeholder { color: #A8ABB2; font-size: 26rpx; }

.cal-entry {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  width: 96rpx; height: 76rpx; background: #fff; border-radius: 20rpx;
}
.cal-icon { font-size: 28rpx; line-height: 1; }
.cal-text { font-size: 20rpx; color: #4A6FE3; margin-top: 4rpx; }

.skeleton-wrap .card { padding: 28rpx; }
</style>
