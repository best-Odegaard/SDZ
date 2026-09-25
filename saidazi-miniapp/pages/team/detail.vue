<template>
  <view class="container page-with-footer" v-if="team.id">
    <!-- 发布人信息 -->
    <view class="card">
      <view class="head">
        <view class="avatar">
          <text>{{ avatarTxt }}</text>
        </view>
        <view class="flex-1">
          <view class="nickname">{{ publisherName }}</view>
          <view class="sub">{{ publisherInfo }}</view>
        </view>
        <text class="tag" :class="statusTagClass">{{ statusName }}</text>
      </view>
      <view class="pub-time">发布于 {{ formatTime(team.createTime) }}</view>
    </view>

    <!-- 组队信息 -->
    <view class="card">
      <view class="title">{{ team.title }}</view>

      <view class="comp-line" v-if="team.competitionTitle">
        <text class="comp-badge">关联赛事</text>
        <text class="comp-text ellipsis">{{ team.competitionTitle }}</text>
      </view>

      <view class="info-grid">
        <view class="ig-item">
          <text class="ig-label">需要人数</text>
          <text class="ig-value">{{ team.needCount }} 人</text>
        </view>
        <view class="ig-item">
          <text class="ig-label">已加入</text>
          <text class="ig-value">{{ team.currentCount }} 人</text>
        </view>
        <view class="ig-item">
          <text class="ig-label">还差</text>
          <text class="ig-value highlight">{{ lackCount }} 人</text>
        </view>
      </view>

      <view class="block">
        <view class="block-label">所需技能</view>
        <view class="tags">
          <text class="tag" v-for="(s, i) in skills" :key="i">{{ s }}</text>
          <text class="text-muted" v-if="!skills.length">不限</text>
        </view>
      </view>

      <view class="block">
        <view class="block-label">项目说明</view>
        <view class="block-text">{{ team.description || '暂无说明' }}</view>
      </view>

      <view class="block" v-if="team.remark">
        <view class="block-label">备注</view>
        <view class="block-text">{{ team.remark }}</view>
      </view>

      <view class="block" v-if="team.requireResume">
        <view class="resume-hint">
          <text class="rh-icon">📄</text>
          <text>该组队要求申请时附带简历</text>
        </view>
      </view>
    </view>

    <!-- 队伍成员 -->
    <view class="card" v-if="team.members && team.members.length">
      <view class="section-title">队伍成员</view>
      <view class="member" v-for="(m, i) in team.members" :key="i">
        <view class="avatar avatar-sm">
          <text>{{ avatarText(m.nickname) }}</text>
        </view>
        <view class="flex-1">
          <view class="m-name">
            {{ m.nickname }}
            <text class="role-badge" :class="m.role === '队长' ? 'leader' : ''">{{ m.role }}</text>
          </view>
          <view class="m-info">{{ [m.college, m.grade].filter(Boolean).join(' · ') || '—' }}</view>
        </view>
      </view>
    </view>

    <!-- 联系方式（仅非发布人可见） -->
    <view class="card" v-if="contactVisible">
      <view class="section-title">联系方式</view>
      <view class="contact-row">
        <text class="contact-text">{{ team.contact || '—' }}</text>
        <view class="btn-ghost btn-sm" @tap="copyContact">复制</view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer-bar">
      <view class="btn-plain btn-sm" @tap="goMatch">看匹配队友</view>
      <view class="btn-primary btn-sm" @tap="openApply">申请加入</view>
    </view>

    <!-- 申请弹窗 -->
    <view class="mask" v-if="showApply" @tap="showApply = false">
      <view class="sheet" @tap.stop>
        <view class="sheet-title">申请加入队伍</view>

        <view class="sheet-item">
          <text class="si-label">附上我的简历</text>
          <switch :checked="attachResume" color="#4A6FE3" @change="onResumeSwitch" />
        </view>

        <view class="sheet-item column">
          <text class="si-label">留言（选填）</text>
          <textarea
            class="sheet-textarea"
            v-model="applyMessage"
            placeholder="简单介绍一下自己，让队长更快了解你"
            maxlength="200"
          />
        </view>

        <view class="btn-primary" @tap="submitApply">提交申请</view>
      </view>
    </view>
  </view>

  <empty-state v-else-if="!loading" icon="⚠️" text="组队不存在" sub="可能已关闭或链接有误" />
</template>

<script>
/**
 * 组队详情页
 * 参数: id 组队ID
 * 行为: 申请加入（可附带简历 + 留言）、查看成员、复制联系方式
 */
import { teamApi } from '@/utils/api.js'
import {
  formatTime, teamStatusName, avatarText, getUserId
} from '@/utils/auth.js'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { EmptyState },
  data() {
    return {
      id: null,
      team: {},
      loading: false,
      showApply: false,
      attachResume: true,
      applyMessage: ''
    }
  },
  computed: {
    publisherName() { return (this.team.publisher && this.team.publisher.nickname) || '匿名同学' },
    publisherInfo() {
      const p = this.team.publisher || {}
      return [p.college, p.major, p.grade].filter(Boolean).join(' · ') || '—'
    },
    avatarTxt() { return avatarText(this.publisherName) },
    skills() { return this.team.skillsRequired || [] },
    statusName() { return teamStatusName(this.team.status) },
    statusTagClass() {
      return { 0: 'tag-green', 1: 'tag-orange', 2: 'tag-gray' }[Number(this.team.status)] || 'tag-gray'
    },
    lackCount() {
      const left = (Number(this.team.needCount) || 0) - (Number(this.team.currentCount) || 0)
      return left > 0 ? left : 0
    },
    isPublisher() { return Number(this.team.userId) === Number(getUserId()) },
    /** 非发布人 + 招募中 才显示联系方式 */
    contactVisible() { return !this.isPublisher },
    canApply() { return !this.isPublisher && Number(this.team.status) === 0 }
  },
  onLoad(options) {
    this.id = options.id
    this.loadDetail()
  },
  methods: {
    formatTime,
    avatarText,

    async loadDetail() {
      if (!this.id) return
      this.loading = true
      try {
        this.team = await teamApi.detail(this.id)
        uni.setNavigationBarTitle({ title: '组队详情' })
      } catch (e) {
        console.error('加载组队详情失败', e)
        this.team = {}
      } finally {
        this.loading = false
      }
    },

    onResumeSwitch(e) { this.attachResume = e.detail.value },

    openApply() {
      if (!this.canApply) {
        uni.showToast({
          title: this.isPublisher ? '这是你发布的组队' : '该组队已停止招募',
          icon: 'none'
        })
        return
      }
      this.showApply = true
    },

    async submitApply() {
      try {
        await teamApi.apply({
          teamId: this.id,
          applicantId: getUserId(),
          withResume: this.attachResume ? 1 : 0,
          message: this.applyMessage
        })
        this.showApply = false
        this.applyMessage = ''
        uni.showToast({ title: '申请已提交', icon: 'success' })
      } catch (e) {
        console.error('申请失败', e)
      }
    },

    copyContact() {
      if (!this.team.contact) return
      uni.setClipboardData({
        data: this.team.contact,
        success: () => uni.showToast({ title: '联系方式已复制', icon: 'none' })
      })
    },

    goMatch() {
      uni.navigateTo({ url: `/pages/team/match?competitionId=${this.team.competitionId || ''}` })
    }
  }
}
</script>

<style scoped>
.head { display: flex; align-items: center; gap: 18rpx; }
.nickname { font-size: 30rpx; font-weight: 600; color: #303133; }
.sub { font-size: 24rpx; color: #909399; margin-top: 6rpx; }
.pub-time { font-size: 24rpx; color: #C0C4CC; margin-top: 20rpx; }

.title { font-size: 34rpx; font-weight: 700; color: #303133; line-height: 1.45; margin-bottom: 20rpx; }

.comp-line {
  display: flex; align-items: center; gap: 12rpx;
  background: #F7F8FA; border-radius: 10rpx; padding: 14rpx 16rpx; margin-bottom: 24rpx;
}
.comp-badge {
  flex-shrink: 0; font-size: 20rpx; color: #4A6FE3; background: #ECF0FF;
  padding: 3rpx 10rpx; border-radius: 6rpx;
}
.comp-text { font-size: 25rpx; color: #606266; flex: 1; }

.info-grid {
  display: flex; background: #F7F8FA; border-radius: 14rpx;
  padding: 24rpx 0; margin-bottom: 24rpx;
}
.ig-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 10rpx; }
.ig-label { font-size: 24rpx; color: #909399; }
.ig-value { font-size: 32rpx; font-weight: 700; color: #303133; }
.ig-value.highlight { color: #FA8C16; }

.block { margin-bottom: 24rpx; }
.block:last-child { margin-bottom: 0; }
.block-label { font-size: 26rpx; color: #909399; margin-bottom: 12rpx; }
.block-text { font-size: 27rpx; color: #606266; line-height: 1.7; white-space: pre-wrap; }
.tags { display: flex; flex-wrap: wrap; }

.resume-hint {
  display: flex; align-items: center; gap: 12rpx;
  background: #FFF7E6; color: #FA8C16; font-size: 25rpx;
  padding: 18rpx 22rpx; border-radius: 12rpx;
}
.rh-icon { font-size: 28rpx; }

/* 成员 */
.member { display: flex; align-items: center; gap: 18rpx; padding: 20rpx 0; border-bottom: 1rpx solid #F2F3F5; }
.member:last-child { border-bottom: none; padding-bottom: 0; }
.m-name { font-size: 28rpx; font-weight: 500; color: #303133; display: flex; align-items: center; gap: 12rpx; }
.role-badge {
  font-size: 20rpx; padding: 2rpx 10rpx; border-radius: 6rpx;
  background: #F2F3F5; color: #909399; font-weight: 400;
}
.role-badge.leader { background: #ECF0FF; color: #4A6FE3; }
.m-info { font-size: 24rpx; color: #909399; margin-top: 6rpx; }

/* 联系方式 */
.contact-row { display: flex; align-items: center; justify-content: space-between; gap: 20rpx; }
.contact-text { font-size: 28rpx; color: #303133; flex: 1; }

/* 申请弹窗 */
.mask {
  position: fixed; inset: 0; background: rgba(0, 0, 0, 0.45);
  display: flex; align-items: flex-end; z-index: 100;
}
.sheet {
  width: 100%; background: #fff; border-radius: 28rpx 28rpx 0 0;
  padding: 36rpx 32rpx calc(36rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}
.sheet-title { font-size: 32rpx; font-weight: 600; text-align: center; margin-bottom: 36rpx; color: #303133; }
.sheet-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 24rpx 0; border-bottom: 1rpx solid #F2F3F5;
}
.sheet-item.column { flex-direction: column; align-items: stretch; gap: 16rpx; }
.si-label { font-size: 28rpx; color: #303133; }
.sheet-textarea {
  width: 100%; height: 180rpx; background: #F7F8FA; border-radius: 12rpx;
  padding: 20rpx; font-size: 27rpx; box-sizing: border-box;
}
.sheet .btn-primary { margin-top: 40rpx; }
</style>
