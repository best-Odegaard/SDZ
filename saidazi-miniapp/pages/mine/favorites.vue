<template>
  <view class="container">
    <template v-if="list.length">
      <view class="fav-wrap" v-for="item in list" :key="item.id">
        <competition-card
          :item="item"
          :is-fav="true"
          @tap="goDetail"
          @favorite="confirmRemove"
        />
      </view>

      <view class="bottom-tip">共 {{ list.length }} 个收藏赛事</view>
    </template>

    <empty-state
      v-else-if="!loading"
      icon="⭐"
      text="还没有收藏赛事"
      sub="在赛事详情页点收藏，方便随时查看报名时间"
      btn-text="去逛逛赛事"
      @action="goHome"
    />
  </view>
</template>

<script>
/**
 * 我的收藏页
 * 展示已收藏赛事，支持取消收藏
 */
import { favoriteApi } from '@/utils/api.js'
import { getUserId } from '@/utils/auth.js'
import CompetitionCard from '@/components/competition-card/competition-card.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { CompetitionCard, EmptyState },
  data() {
    return {
      list: [],
      loading: false
    }
  },
  onShow() {
    this.loadList()
  },
  methods: {
    async loadList() {
      this.loading = true
      try {
        const rows = await favoriteApi.list(getUserId())
        // 后端/ mock 返回结构：{ id, competitionId, competition: {...} }
        this.list = (Array.isArray(rows) ? rows : [])
          .map((f) => {
            const c = f.competition
            if (!c) return null
            return { ...c, _favId: f.id }
          })
          .filter(Boolean)
      } catch (e) {
        console.error('加载收藏失败', e)
        this.list = []
      } finally {
        this.loading = false
      }
    },

    confirmRemove(item) {
      const that = this
      uni.showModal({
        title: '取消收藏',
        content: `确定不再收藏《${item.title}》吗？`,
        success(res) {
          if (res.confirm) that.doRemove(item)
        }
      })
    },

    async doRemove(item) {
      try {
        await favoriteApi.remove(getUserId(), item.id)
        this.list = this.list.filter((c) => c.id !== item.id)
        uni.showToast({ title: '已取消收藏', icon: 'none' })
      } catch (e) {
        console.error('取消收藏失败', e)
      }
    },

    goDetail(item) {
      uni.navigateTo({ url: `/pages/competition/detail?id=${item.id}` })
    },
    goHome() {
      uni.switchTab({ url: '/pages/index/index' })
    }
  }
}
</script>

<style scoped>
.bottom-tip { text-align: center; font-size: 24rpx; color: #C0C4CC; padding: 20rpx 0 40rpx; }
</style>
