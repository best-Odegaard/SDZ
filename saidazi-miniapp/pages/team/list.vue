<template>
  <view class="container">
    <!-- 顶部：赛事筛选 + 搜索 -->
    <view class="top-bar">
      <view class="comp-picker" @tap="showCompPicker">
        <text class="cp-text ellipsis">{{ competitionTitle || '全部赛事' }}</text>
        <text class="cp-arrow">▾</text>
      </view>
      <view class="search-btn" @tap="focusSearch">
        <text class="sb-icon">🔍</text>
      </view>
    </view>

    <!-- 搜索输入 -->
    <view class="search-row" v-if="searchVisible">
      <input
        class="search-input-box"
        v-model="keyword"
        placeholder="搜索组队标题 / 技能标签"
        confirm-type="search"
        :focus="searchVisible"
        @confirm="doSearch"
      />
      <text class="search-cancel" @tap="cancelSearch">取消</text>
    </view>

    <!-- 技能筛选 -->
    <scroll-view class="filter-bar" scroll-x :show-scrollbar="false">
      <text
        v-for="s in skillOptions"
        :key="s"
        class="filter-chip"
        :class="{ active: activeSkill === s }"
        @tap="switchSkill(s)"
      >{{ s }}</text>
    </scroll-view>

    <!-- 组队列表 -->
    <view v-if="loading && !list.length">
      <view class="card" v-for="i in 3" :key="i">
        <view class="skeleton" style="height:60rpx;width:50%;margin-bottom:20rpx"></view>
        <view class="skeleton" style="height:40rpx;width:80%;margin-bottom:20rpx"></view>
        <view class="skeleton" style="height:28rpx;width:100%"></view>
      </view>
    </view>

    <template v-else>
      <team-card v-for="item in list" :key="item.id" :item="item" @tap="goDetail" />

      <empty-state
        v-if="!list.length"
        icon="👥"
        text="暂无组队信息"
        sub="换个条件看看，或者自己发一个"
        btn-text="发布组队"
        @action="goPublish"
      />

      <view class="load-more" v-if="list.length" @tap="loadMore">
        {{ hasMore ? '加载更多' : '— 没有更多了 —' }}
      </view>
    </template>

    <!-- 悬浮发布按钮 -->
    <view class="fab" @tap="goPublish">
      <text class="fab-icon">＋</text>
    </view>
  </view>
</template>

<script>
import { teamApi, competitionApi } from '@/utils/api.js'
import TeamCard from '@/components/team-card/team-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { TeamCard, EmptyState },
  data() {
    return {
      competitionId: null,
      competitionTitle: '',
      keyword: '',
      searchVisible: false,
      activeSkill: '全部',
      skillOptions: ['全部', 'Python', '算法', '数学建模', 'Vue', 'UI设计', '篮球'],
      list: [],
      competitionOptions: [],
      page: 1,
      size: 10,
      total: 0,
      hasMore: true,
      loading: false
    }
  },
  onLoad(options) {
    if (options.competitionId) this.competitionId = options.competitionId
    this.loadCompetitionOptions()
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
        const params = { status: 0, page: this.page, size: this.size }
        if (this.competitionId) params.competitionId = this.competitionId
        if (this.activeSkill !== '全部') params.skill = this.activeSkill
        const data = await teamApi.list(params)
        const rows = Array.isArray(data) ? data : (data.list || data.records || [])
        this.total = (data && data.total) || rows.length
        this.list = reset ? rows : this.list.concat(rows)
        this.hasMore = this.list.length < this.total && rows.length > 0
      } catch (e) {
        console.error('加载组队失败', e)
      } finally {
        this.loading = false
      }
    },

    /** 加载赛事下拉选项（全部 + 已发布赛事） */
    async loadCompetitionOptions() {
      try {
        const data = await competitionApi.list({ status: 1, page: 1, size: 20 })
        const rows = Array.isArray(data) ? data : (data.list || data.records || [])
        this.competitionOptions = rows
        if (this.competitionId) {
          const hit = rows.find((c) => String(c.id) === String(this.competitionId))
          this.competitionTitle = hit ? hit.title : ''
        }
      } catch (e) {
        this.competitionOptions = []
      }
    },

    showCompPicker() {
      const that = this
      const items = ['全部赛事'].concat(this.competitionOptions.map((c) => c.title))
      uni.showActionSheet({
        itemList: items.slice(0, 6),      // 小程序 actionSheet 上限 6 项
        success(res) {
          if (res.tapIndex === 0) {
            that.competitionId = null
            that.competitionTitle = ''
          } else {
            const c = that.competitionOptions[res.tapIndex - 1]
            that.competitionId = c.id
            that.competitionTitle = c.title
          }
          that.loadList(true)
        }
      })
    },

    focusSearch() { this.searchVisible = true },
    cancelSearch() {
      this.searchVisible = false
      if (this.keyword) { this.keyword = ''; this.loadList(true) }
    },
    doSearch() {
      this.activeSkill = '全部'
      this.loadList(true)
    },

    switchSkill(s) {
      if (this.activeSkill === s) return
      this.activeSkill = s
      this.loadList(true)
    },

    loadMore() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadList(false)
    },

    goDetail(item) {
      uni.navigateTo({ url: `/pages/team/detail?id=${item.id}` })
    },
    goPublish() {
      const q = this.competitionId ? `?competitionId=${this.competitionId}` : ''
      uni.navigateTo({ url: `/pages/team/publish${q}` })
    }
  }
}
</script>

<style scoped>
.top-bar { display: flex; align-items: center; gap: 16rpx; margin-bottom: 20rpx; }
.comp-picker {
  flex: 1; height: 76rpx; background: #fff; border-radius: 38rpx;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 26rpx;
}
.cp-text { font-size: 27rpx; color: #303133; flex: 1; }
.cp-arrow { font-size: 24rpx; color: #909399; margin-left: 12rpx; }
.search-btn {
  width: 76rpx; height: 76rpx; background: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.sb-icon { font-size: 30rpx; opacity: 0.7; }

.search-row { display: flex; align-items: center; gap: 16rpx; margin-bottom: 20rpx; }
.search-input-box {
  flex: 1; height: 76rpx; background: #fff; border-radius: 38rpx;
  padding: 0 28rpx; font-size: 27rpx; box-sizing: border-box;
}
.search-cancel { font-size: 28rpx; color: #4A6FE3; flex-shrink: 0; }

.filter-bar { margin-bottom: 20rpx; padding: 0; background: transparent; }

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
