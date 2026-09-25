<template>
  <view class="container">
    <!-- 算法说明 -->
    <view class="algo-tip card">
      <text class="at-icon">✨</text>
      <view class="flex-1">
        <view class="at-title">智能匹配</view>
        <view class="at-desc">基于 Jaccard 相似度，综合技能、赛事兴趣、收藏偏好加权计算</view>
      </view>
    </view>

    <!-- Tab -->
    <view class="tabs">
      <view :class="['tab', tab === 'teams' ? 'active' : '']" @tap="switchTab('teams')">推荐组队</view>
      <view :class="['tab', tab === 'mates' ? 'active' : '']" @tap="switchTab('mates')">推荐队友</view>
    </view>

    <!-- 推荐组队 -->
    <template v-if="tab === 'teams'">
      <view v-if="teams.length">
        <view class="match-card card" v-for="item in teams" :key="item.id" @tap="goTeam(item.id)">
          <view class="mc-head">
            <view class="avatar avatar-sm">
              <text>{{ avatarText(item.publisher && item.publisher.nickname) }}</text>
            </view>
            <view class="flex-1">
              <view class="mc-nick ellipsis">{{ item.publisher && item.publisher.nickname }}</view>
              <view class="mc-sub ellipsis">
                {{ [item.publisher && item.publisher.college, item.publisher && item.publisher.grade].filter(Boolean).join(' · ') }}
              </view>
            </view>
          </view>

          <view class="mc-title ellipsis-2">{{ item.title }}</view>

          <view class="mc-comps" v-if="item.skillsRequired && item.skillsRequired.length">
            <text
              class="tag"
              v-for="(s, i) in item.skillsRequired"
              :key="i"
              :class="(item.matchTags || []).includes(s) ? 'tag-green' : ''"
            >{{ s }}</text>
          </view>

          <view class="mc-score">
            <match-score :score="item.matchScore" :tags="item.matchTags" />
          </view>
        </view>
      </view>
      <empty-state v-else icon="✨" text="暂无匹配结果" sub="完善个人技能标签可以提高匹配度" />
    </template>

    <!-- 推荐队友 -->
    <template v-else>
      <view v-if="mates.length">
        <view class="match-card card" v-for="(item, i) in mates" :key="i">
          <view class="mc-head">
            <view class="avatar">
              <text>{{ avatarText(item.nickname) }}</text>
            </view>
            <view class="flex-1">
              <view class="mc-nick">{{ item.nickname }}</view>
              <view class="mc-sub ellipsis">
                {{ [item.college, item.major, item.grade].filter(Boolean).join(' · ') }}
              </view>
            </view>
            <view class="btn-ghost btn-sm" @tap.stop="invite(item)">邀请</view>
          </view>

          <view class="mc-skills">
            <text
              class="tag"
              v-for="(s, j) in item.skills"
              :key="j"
              :class="(item.matchTags || []).includes(s) ? 'tag-green' : ''"
            >{{ s }}</text>
          </view>

          <view class="mc-target" v-if="item.competitionTitle">
            <text class="t-label">目标赛事</text>
            <text class="t-text ellipsis">{{ item.competitionTitle }}</text>
          </view>

          <view class="mc-score">
            <match-score :score="item.matchScore" />
          </view>
        </view>
      </view>
      <empty-state v-else icon="✨" text="暂无匹配队友" sub="完善个人技能标签可以提高匹配度" />
    </template>
  </view>
</template>

<script>
/**
 * 智能匹配页
 * 参数: competitionId 限定赛事（可选）
 * 展示: 推荐组队 / 推荐队友，含匹配度进度条与共同标签高亮
 */
import { matchApi } from '@/utils/api.js'
import { avatarText, getUserId } from '@/utils/auth.js'
import MatchScore from '@/components/match-score/match-score.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { MatchScore, EmptyState },
  data() {
    return {
      competitionId: null,
      tab: 'teams',
      teams: [],
      mates: [],
      loaded: { teams: false, mates: false }
    }
  },
  onLoad(options) {
    if (options.competitionId) this.competitionId = options.competitionId
    this.loadTeams()
  },
  methods: {
    avatarText,

    async loadTeams() {
      if (this.loaded.teams) return
      try {
        const rows = await matchApi.teams(getUserId(), this.competitionId)
        this.teams = Array.isArray(rows) ? rows : []
      } catch (e) {
        this.teams = []
      } finally {
        this.loaded.teams = true
      }
    },

    async loadMates() {
      if (this.loaded.mates) return
      try {
        const rows = await matchApi.mates(getUserId(), this.competitionId)
        this.mates = Array.isArray(rows) ? rows : []
      } catch (e) {
        this.mates = []
      } finally {
        this.loaded.mates = true
      }
    },

    switchTab(t) {
      if (this.tab === t) return
      this.tab = t
      if (t === 'teams') this.loadTeams()
      else this.loadMates()
    },

    goTeam(id) {
      uni.navigateTo({ url: `/pages/team/detail?id=${id}` })
    },

    invite(mate) {
      uni.showToast({ title: `已向 ${mate.nickname} 发送邀请`, icon: 'none' })
    }
  }
}
</script>

<style scoped>
.algo-tip { display: flex; align-items: center; gap: 18rpx; background: linear-gradient(135deg, #EEF3FF 0%, #F7F9FF 100%); }
.at-icon { font-size: 44rpx; }
.at-title { font-size: 28rpx; font-weight: 600; color: #4A6FE3; margin-bottom: 6rpx; }
.at-desc { font-size: 23rpx; color: #7A88A8; line-height: 1.5; }

.match-card { padding: 28rpx; }
.mc-head { display: flex; align-items: center; gap: 18rpx; margin-bottom: 20rpx; }
.mc-nick { font-size: 28rpx; font-weight: 600; color: #303133; }
.mc-sub { font-size: 24rpx; color: #909399; margin-top: 6rpx; }

.mc-title { font-size: 30rpx; font-weight: 600; color: #303133; line-height: 1.45; margin-bottom: 16rpx; }
.mc-comps { margin-bottom: 20rpx; }
.mc-skills { margin-bottom: 18rpx; }

.mc-target {
  display: flex; align-items: center; gap: 12rpx;
  background: #F7F8FA; border-radius: 10rpx; padding: 14rpx 18rpx; margin-bottom: 20rpx;
}
.t-label { flex-shrink: 0; font-size: 22rpx; color: #909399; }
.t-text { font-size: 25rpx; color: #606266; flex: 1; }

.mc-score { padding-top: 4rpx; border-top: 1rpx solid #F2F3F5; padding-top: 20rpx; }
</style>
