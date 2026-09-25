<template>
  <view class="container page-with-footer" v-if="resume.name">
    <!-- 简历头部 -->
    <view class="card resume-head">
      <view class="avatar avatar-lg">
        <text>{{ avatarTxt }}</text>
      </view>
      <view class="rh-name">{{ resume.name }}</view>
      <view class="rh-sub">{{ resume.collegeMajor }}</view>
      <view class="rh-grade">{{ resume.grade }}</view>
      <view class="rh-status" v-if="resume.status === 1">
        <text class="tag tag-green">简历已完成</text>
      </view>
    </view>

    <!-- 技能标签 -->
    <view class="card">
      <view class="section-title">技能标签</view>
      <view class="tags">
        <text class="tag" v-for="(s, i) in skills" :key="i">{{ s }}</text>
        <text class="text-muted" v-if="!skills.length">暂无</text>
      </view>
    </view>

    <!-- 参赛经历 -->
    <view class="card">
      <view class="section-title">参赛经历</view>
      <view class="rich-text">{{ resume.experience || '暂无' }}</view>
    </view>

    <!-- 个人简介 -->
    <view class="card">
      <view class="section-title">个人简介</view>
      <view class="rich-text">{{ resume.introduction || '暂无' }}</view>
    </view>

    <!-- 附件简历 -->
    <view class="card" v-if="resume.attachmentName">
      <view class="section-title">附件简历</view>
      <view class="file-row" @tap="openFile">
        <text class="file-icon">📄</text>
        <view class="flex-1">
          <view class="file-name ellipsis">{{ resume.attachmentName }}</view>
          <view class="file-sub">点击预览</view>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="update-time">最近更新：{{ formatTime(resume.updateTime) || '—' }}</view>

    <!-- 底部操作 -->
    <view class="footer-bar">
      <view class="btn-plain btn-sm" @tap="copyText">复制简历</view>
      <view class="btn-primary btn-sm" @tap="goEdit">编辑简历</view>
    </view>
  </view>

  <empty-state
    v-else-if="!loading"
    icon="📝"
    text="还没有简历"
    sub="填一份简历，申请组队更有优势"
    btn-text="去填写"
    @action="goEdit"
  />
</template>

<script>
/**
 * 简历预览页
 * 参数: userId（查看他人简历时携带，不传则看自己的）
 */
import { resumeApi } from '@/utils/api.js'
import { formatTime, avatarText, getUserId } from '@/utils/auth.js'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { EmptyState },
  data() {
    return {
      userId: null,
      resume: {},
      loading: false
    }
  },
  computed: {
    avatarTxt() { return avatarText(this.resume.name) },
    skills() {
      const v = this.resume.skills
      if (Array.isArray(v)) return v
      if (typeof v === 'string' && v) return v.split(',').map((s) => s.trim()).filter(Boolean)
      return []
    }
  },
  onLoad(options) {
    this.userId = options.userId || null
    this.loadResume()
  },
  methods: {
    formatTime,

    async loadResume() {
      this.loading = true
      try {
        this.resume = this.userId
          ? await resumeApi.getByUserId(this.userId)
          : await resumeApi.my(getUserId())
        if (!this.resume) this.resume = {}
      } catch (e) {
        console.error('加载简历失败', e)
        this.resume = {}
      } finally {
        this.loading = false
      }
    },

    /** 小程序内无法直接预览附件时，复制链接由用户自行打开 */
    openFile() {
      const url = this.resume.attachmentUrl
      if (!url) {
        uni.setClipboardData({ data: this.resume.attachmentName || '', success: () => uni.showToast({ title: '文件名已复制', icon: 'none' }) })
        return
      }
      uni.setClipboardData({
        data: url,
        success: () => uni.showToast({ title: '简历链接已复制', icon: 'none' })
      })
    },

    copyText() {
      const r = this.resume
      const text = [
        `${r.name}　${r.collegeMajor}　${r.grade}`,
        ``,
        `【技能标签】`,
        this.skills.join('、') || '暂无',
        ``,
        `【参赛经历】`,
        r.experience || '暂无',
        ``,
        `【个人简介】`,
        r.introduction || '暂无'
      ].join('\n')
      uni.setClipboardData({
        data: text,
        success: () => uni.showToast({ title: '简历内容已复制', icon: 'none' })
      })
    },

    goEdit() {
      uni.navigateTo({ url: '/pages/resume/edit' })
    }
  }
}
</script>

<style scoped>
.resume-head { display: flex; flex-direction: column; align-items: center; padding: 48rpx 24rpx; }
.rh-name { font-size: 38rpx; font-weight: 700; color: #303133; margin-top: 24rpx; }
.rh-sub { font-size: 27rpx; color: #606266; margin-top: 12rpx; }
.rh-grade { font-size: 25rpx; color: #909399; margin-top: 8rpx; }
.rh-status { margin-top: 20rpx; }

.tags { display: flex; flex-wrap: wrap; }
.rich-text { font-size: 27rpx; color: #606266; line-height: 1.75; white-space: pre-wrap; word-break: break-all; }

.file-row {
  display: flex; align-items: center; gap: 18rpx;
  background: #F7F8FA; border-radius: 14rpx; padding: 24rpx 22rpx;
}
.file-icon { font-size: 44rpx; }
.file-name { font-size: 27rpx; color: #303133; }
.file-sub { font-size: 23rpx; color: #909399; margin-top: 6rpx; }
.arrow { color: #C0C4CC; font-size: 32rpx; }

.update-time { text-align: center; font-size: 24rpx; color: #C0C4CC; padding: 20rpx 0 40rpx; }
</style>
