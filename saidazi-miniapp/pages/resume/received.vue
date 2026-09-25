<template>
  <view class="container">
    <!-- 状态筛选 -->
    <view class="filter-bar">
      <text
        v-for="f in filters"
        :key="f.value"
        class="filter-chip"
        :class="{ active: activeFilter === f.value }"
        @tap="switchFilter(f.value)"
      >{{ f.label }}</text>
    </view>

    <template v-if="filtered.length">
      <view class="apply-card card" v-for="item in filtered" :key="item.id">
        <!-- 申请人 -->
        <view class="ac-head" @tap="viewResume(item)">
          <view class="avatar">
            <text>{{ avatarText(item.applicant && item.applicant.nickname) }}</text>
          </view>
          <view class="flex-1">
            <view class="ac-name">{{ item.applicant && item.applicant.nickname }}</view>
            <view class="ac-sub ellipsis">
              {{ [(item.applicant && item.applicant.college), (item.applicant && item.applicant.grade)].filter(Boolean).join(' · ') }}
            </view>
          </view>
          <text class="tag" :class="applyTagClass(item.status)">{{ applyStatusName(item.status) }}</text>
        </view>

        <!-- 匹配度 -->
        <view class="ac-score">
          <match-score :score="item.matchScore" />
        </view>

        <!-- 申请信息 -->
        <view class="ac-meta">
          <text class="am-label">申请组队</text>
          <text class="am-text ellipsis">{{ item.teamTitle }}</text>
        </view>
        <view class="ac-meta">
          <text class="am-label">申请时间</text>
          <text class="am-text">{{ fromNow(item.createTime) }}</text>
        </view>

        <!-- 技能预览 -->
        <view class="ac-skills" v-if="skillsOf(item).length">
          <text class="tag tag-gray" v-for="(s, i) in skillsOf(item)" :key="i">{{ s }}</text>
        </view>

        <!-- 操作 -->
        <view class="ac-actions" v-if="Number(item.status) === 0">
          <view class="btn-plain btn-sm" @tap="process(item, 2)">拒绝</view>
          <view class="btn-primary btn-sm" @tap="process(item, 1)">通过</view>
        </view>
        <view class="ac-actions" v-else>
          <view class="btn-plain btn-sm" @tap="viewResume(item)">查看简历</view>
        </view>
      </view>
    </template>

    <empty-state
      v-else
      icon="📬"
      text="暂无简历投递"
      sub="发布组队后，申请人的简历会出现在这里"
      btn-text="去发布组队"
      @action="goPublish"
    />
  </view>
</template>

<script>
/**
 * 收到的简历页
 * 展示申请人列表，支持查看简历详情、通过 / 拒绝
 */
import { teamApi } from '@/utils/api.js'
import { fromNow, avatarText, applyStatusName, getUserId } from '@/utils/auth.js'
import MatchScore from '@/components/match-score/match-score.vue'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { MatchScore, EmptyState },
  data() {
    return {
      list: [],
      activeFilter: -1,
      filters: [
        { label: '全部', value: -1 },
        { label: '待审核', value: 0 },
        { label: '已通过', value: 1 },
        { label: '已拒绝', value: 2 }
      ],
      loading: false
    }
  },
  computed: {
    filtered() {
      if (this.activeFilter === -1) return this.list
      return this.list.filter((i) => Number(i.status) === this.activeFilter)
    }
  },
  onLoad() {
    this.loadList()
  },
  onShow() {
    if (this.loaded) this.loadList()
    this.loaded = true
  },
  methods: {
    fromNow,
    avatarText,
    applyStatusName,

    async loadList() {
      if (this.loading) return
      this.loading = true
      try {
        const rows = await teamApi.receivedApplications(getUserId())
        this.list = Array.isArray(rows) ? rows : []
      } catch (e) {
        console.error('加载收到的简历失败', e)
        this.list = []
      } finally {
        this.loading = false
      }
    },

    /** 从申请记录里取技能标签 */
    skillsOf(item) {
      const v = item.resume && item.resume.skills
      if (Array.isArray(v)) return v.slice(0, 4)
      if (typeof v === 'string' && v) return v.split(',').map((s) => s.trim()).filter(Boolean).slice(0, 4)
      return []
    },

    applyTagClass(status) {
      return { 0: 'tag-orange', 1: 'tag-green', 2: 'tag-gray' }[Number(status)] || 'tag-gray'
    },

    switchFilter(v) {
      this.activeFilter = v
    },

    /** 查看申请人简历（跳预览页） */
    viewResume(item) {
      const uid = item.applicantId || (item.applicant && item.applicant.id)
      uni.navigateTo({ url: `/pages/resume/preview?userId=${uid}` })
    },

    process(item, status) {
      const that = this
      const action = status === 1 ? '通过' : '拒绝'
      uni.showModal({
        title: `确认${action}`,
        content: `确定${action} ${item.applicant && item.applicant.nickname} 的申请吗？`,
        success(res) {
          if (res.confirm) that.doProcess(item, status)
        }
      })
    },

    async doProcess(item, status) {
      try {
        await teamApi.processApplication({ applicationId: item.id, status })
        item.status = status
        uni.showToast({ title: status === 1 ? '已通过' : '已拒绝', icon: 'success' })
      } catch (e) {
        console.error('处理申请失败', e)
      }
    },

    goPublish() {
      uni.navigateTo({ url: '/pages/team/publish' })
    }
  }
}
</script>

<style scoped>
.filter-bar { background: transparent; padding: 0 0 20rpx; }

.apply-card { padding: 28rpx; }
.ac-head { display: flex; align-items: center; gap: 18rpx; }
.ac-name { font-size: 30rpx; font-weight: 600; color: #303133; }
.ac-sub { font-size: 24rpx; color: #909399; margin-top: 6rpx; }

.ac-score { margin: 24rpx 0 20rpx; }

.ac-meta { display: flex; align-items: center; gap: 14rpx; margin-bottom: 12rpx; }
.am-label { flex-shrink: 0; font-size: 22rpx; color: #909399; background: #F5F6FA; padding: 3rpx 12rpx; border-radius: 6rpx; }
.am-text { font-size: 25rpx; color: #606266; flex: 1; }

.ac-skills { margin-top: 16rpx; }

.ac-actions {
  display: flex; gap: 20rpx; margin-top: 26rpx;
  padding-top: 26rpx; border-top: 1rpx solid #F2F3F5;
}
.ac-actions .btn-plain,
.ac-actions .btn-primary { flex: 1; }
</style>
