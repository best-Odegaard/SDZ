<template>
  <view class="container detail-page" v-if="detail.id">
    <!-- 顶部蓝色渐变横幅：赛事名称 -->
    <view class="hero">
      <view class="hero-title">{{ detail.title }}</view>
    </view>

    <!-- 信息卡片：状态 + 倒计时 + 三栏信息 -->
    <view class="info-card card">
      <view class="tag-row">
        <text class="tag-scope">{{ detail.type === 0 ? '校内' : '全国' }}</text>
        <text class="tag-status" :class="statusClass">{{ regStatus }}</text>
        <text class="tag-cat" v-if="detail.category">{{ detail.category }}</text>
      </view>

      <view class="countdown" v-if="countdownText">
        <text>{{ countdownText }}</text>
      </view>

      <view class="info-grid">
        <view class="info-item">
          <text class="info-label">主办单位</text>
          <text class="info-value">{{ detail.organizer || '待公布' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">比赛时间</text>
          <text class="info-value">{{ cnDate(detail.matchTime) || '待公布' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">报名时间</text>
          <text class="info-value">{{ regTimeView || '待公布' }}</text>
        </view>
      </view>
    </view>

    <!-- 赛事简介 -->
    <view class="section">
      <view class="section-title">赛事简介：</view>
      <view class="rich-text">{{ detail.description || '暂无简介' }}</view>
    </view>

    <!-- 报名信息 -->
    <view class="section">
      <view class="section-title">报名信息：</view>
      <view class="rich-text">{{ detail.regInfo || '暂无报名信息' }}</view>
      <view class="copy-tip" v-if="detail.regInfo" @tap="copyRegInfo">点击复制报名信息</view>
    </view>

    <!-- 本校往届获奖成果库 -->
    <view class="section">
      <view class="section-title">本校往届获奖成果库：</view>
      <view v-if="achievements.length">
        <view class="ach-item" v-for="a in achievements" :key="a.id">
          <view class="ach-head">
            <text class="ach-team">{{ a.teamName }}</text>
            <text class="tag-award">{{ a.award }}</text>
          </view>
          <view class="ach-intro">{{ a.introduction }}</view>
          <view class="ach-foot">
            <text class="ach-members">队员：{{ a.members }}</text>
            <text class="ach-year">{{ a.year }}</text>
          </view>
        </view>
      </view>
      <view class="ach-empty" v-else>暂无往届成果，期待你们的身影 ~</view>
    </view>

    <!-- 底部操作条：收藏 / 提醒 / 找搭子 -->
    <view class="footer-bar">
      <view class="foot-icon" @tap="toggleFavorite">
        <text :class="['foot-ico', isFav ? 'on' : '']">{{ isFav ? '★' : '☆' }}</text>
      </view>
      <view class="foot-icon" @tap="openReminder">
        <text class="foot-ico">⏰</text>
      </view>
      <view class="foot-btn" @tap="goTeamList">找搭子</view>
    </view>
  </view>

  <view class="container detail-page" v-else-if="!loading">
    <empty-state icon="⚠️" text="赛事不存在" sub="可能已下架或链接有误" />
  </view>
</template>

<script>
/**
 * 赛事详情页（按 UI 设计稿实现）
 * 参数: id 赛事ID
 * 结构: 蓝色横幅标题 → 状态/倒计时/三栏信息卡 → 赛事简介 → 报名信息 → 本校往届获奖成果库 → 底部操作条
 * 行为: 收藏、设置提醒、跳组队列表
 */
import { competitionApi, favoriteApi, reminderApi, achievementApi } from '@/utils/api.js'
import {
  formatTime, calcRegStatus, regStatusClass, daysToRegEnd, getUserId
} from '@/utils/auth.js'
import EmptyState from '@/components/empty-state/empty-state.vue'

export default {
  components: { EmptyState },
  data() {
    return {
      id: null,
      detail: {},
      achievements: [],
      isFav: false,
      loading: false
    }
  },
  computed: {
    regStatus() { return calcRegStatus(this.detail) },
    statusClass() { return regStatusClass(calcRegStatus(this.detail)) },
    /** 报名时间展示：起止 / 仅截止 / 仅开始 */
    regTimeView() {
      const s = this.detail.regStartTime
      const e = this.detail.regEndTime
      if (s && e) return `${this.cnDate(s)} - ${this.cnDate(e)}`
      if (e) return `${this.cnDate(e)} 截止`
      if (s) return `${this.cnDate(s)} 起`
      return ''
    },
    /** 倒计时文案：报名中 → 距截止；未开始 → 距开始；已截止 → 已结束 */
    countdownText() {
      const c = this.detail
      if (!c || !c.id) return ''
      const status = calcRegStatus(c)
      const day = 86400000
      const parse = (s) => (s ? new Date(String(s).replace(/-/g, '/')).getTime() : 0)
      if (status === '报名中') {
        if (!c.regEndTime) return ''
        const d = Math.ceil((parse(c.regEndTime) - Date.now()) / day)
        if (d <= 0) return '今天报名截止'
        return `距离报名截止还有 ${d} 天`
      }
      if (status === '未开始') {
        if (!c.regStartTime) return ''
        const d = Math.ceil((parse(c.regStartTime) - Date.now()) / day)
        if (d <= 0) return '报名即将开始'
        return `距离报名开始还有 ${d} 天`
      }
      return '报名已结束'
    }
  },
  onLoad(options) {
    this.id = options.id || options.competitionId
    this.loadDetail()
    this.loadAchievements()
    this.checkFavorite()
  },
  methods: {
    formatTime,

    /**
     * 服务端部分赛事把「报名信息」混在 description 长通知里，且 regInfo / 时间字段为 null，
     * 这里统一做一次拆解：含“报名”标题的章节 → 报名信息，其余 → 赛事简介；并兜底抽取日期。
     */
    normalizeDetail(raw) {
      const d = { ...(raw || {}) }
      const { intro, reg } = this.splitNotice(d.description)
      if (!d.regInfo && reg) d.regInfo = reg
      if (reg && intro) d.description = intro
      // 报名信息章节优先，命中不到再去简介里找
      const dates = this.extractDates(`${d.regInfo || ''} ${d.description || ''}`)
      if (!d.regEndTime && dates.regEnd) d.regEndTime = dates.regEnd
      if (!d.matchTime && dates.match) d.matchTime = dates.match
      return d
    },

    /** 按「一、二、三…」切分通知，标题含“报名”的章节归入报名信息 */
    splitNotice(text) {
      const t = String(text || '')
      if (!t) return { intro: '', reg: '' }
      const marks = []
      const re = /[一二三四五六七八九十]{1,3}、/g
      let m
      while ((m = re.exec(t))) marks.push(m.index)
      if (!marks.length) return { intro: t, reg: '' }

      const introParts = [t.slice(0, marks[0])]
      const regParts = []
      for (let i = 0; i < marks.length; i++) {
        const seg = t.slice(marks[i], i + 1 < marks.length ? marks[i + 1] : t.length)
        // 章节标题：去掉序号后截到第一个子项标记，只取前 4 字判断（避免“赛程安排报名阶段…”误判）
        const title = seg.slice(seg.indexOf('、') + 1).split(/[（(0-9０-９]/)[0].slice(0, 4)
        if (title.indexOf('报名') > -1) {
          regParts.push(seg.replace(/^[一二三四五六七八九十]{1,3}、\s*/, ''))
        } else {
          introParts.push(seg)
        }
      }
      // 报名信息按「（一）（二）…」换行，便于阅读
      const reg = regParts.join('\n').split(/(?=（[一二三四五六七八九十]+）)/).join('\n').trim()
      return { intro: introParts.join(''), reg }
    },

    /** 服务端时间为空时，从通知正文里兜底抽取日期 */
    extractDates(text) {
      const t = String(text || '')
      const toISO = (s) => {
        const m = String(s || '').match(/(\d{4})\s*年\s*(\d{1,2})\s*月\s*(\d{1,2})\s*日/)
        if (!m) return ''
        const p = (n) => (Number(n) < 10 ? '0' + Number(n) : String(Number(n)))
        return `${m[1]}-${p(m[2])}-${p(m[3])}`
      }
      const grab = (re) => {
        const mm = t.match(re)
        return mm ? toISO(mm[1]) : ''
      }
      return {
        regEnd: grab(/报名(?:截止)?时间[：:]\s*([^。；;，,（(]{2,25})/),
        match: grab(/比赛时间[：:]\s*([^。；;，,（(]{2,25})/)
      }
    },

    /** 2026-09-20 → 2026年9月20日 */
    cnDate(dateStr) {
      const ymd = formatTime(dateStr, false)
      if (!ymd) return ''
      const [y, m, d] = ymd.split('-')
      return `${y}年${Number(m)}月${Number(d)}日`
    },

    async loadDetail() {
      if (!this.id) return
      this.loading = true
      try {
        const raw = await competitionApi.detail(this.id) || {}
        this.detail = this.normalizeDetail(raw)
      } catch (e) {
        console.error('加载赛事详情失败', e)
        this.detail = {}
      } finally {
        this.loading = false
      }
    },

    async loadAchievements() {
      try {
        const rows = await achievementApi.list(this.id)
        this.achievements = Array.isArray(rows) ? rows : []
      } catch (e) {
        this.achievements = []
      }
    },

    async checkFavorite() {
      try {
        this.isFav = await favoriteApi.check(getUserId(), this.id)
      } catch (e) {
        this.isFav = false
      }
    },

    async toggleFavorite() {
      const uid = getUserId()
      try {
        if (this.isFav) {
          await favoriteApi.remove(uid, this.id)
          this.isFav = false
          uni.showToast({ title: '已取消收藏', icon: 'none' })
        } else {
          await favoriteApi.add(uid, this.id)
          this.isFav = true
          uni.showToast({ title: '已加入收藏', icon: 'success' })
        }
      } catch (e) {
        console.error('收藏操作失败', e)
      }
    },

    /** 设置提醒：选择提醒时机 */
    openReminder() {
      if (!this.detail.regEndTime) {
        uni.showToast({ title: '该赛事暂无报名时间', icon: 'none' })
        return
      }
      const that = this
      uni.showActionSheet({
        itemList: ['报名开始时提醒我', '报名截止前提醒我'],
        success(res) {
          that.submitReminder(res.tapIndex + 1)
        }
      })
    },

    async submitReminder(remindType) {
      try {
        await reminderApi.add({
          userId: getUserId(),
          competitionId: this.id,
          remindType,
          remindTime: remindType === 1 ? this.detail.regStartTime : this.detail.regEndTime
        })
        uni.showToast({ title: '提醒设置成功', icon: 'success' })
      } catch (e) {
        console.error('设置提醒失败', e)
      }
    },

    copyRegInfo() {
      if (!this.detail.regInfo) return
      uni.setClipboardData({
        data: this.detail.regInfo,
        success: () => uni.showToast({ title: '已复制报名信息', icon: 'none' })
      })
    },

    goTeamList() {
      uni.navigateTo({ url: `/pages/team/list?competitionId=${this.id}` })
    },
    goTeamPublish() {
      uni.navigateTo({ url: `/pages/team/publish?competitionId=${this.id}` })
    }
  }
}
</script>

<style scoped>
.detail-page { padding-bottom: 160rpx; }

/* ===== 顶部蓝色渐变横幅 ===== */
.hero {
  margin: -20rpx -20rpx 0;
  padding: 46rpx 40rpx 90rpx;
  background: linear-gradient(160deg, #6E8DF5 0%, #4A6FE3 60%, #3D5FD6 100%);
  border-radius: 0 0 0 0;
}
.hero-title {
  font-size: 38rpx; font-weight: 700; color: #fff; line-height: 1.5;
  text-align: center;
  display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2;
  overflow: hidden;
}

/* ===== 信息卡片（悬浮在横幅之上） ===== */
.info-card {
  margin-top: -64rpx;
  position: relative; z-index: 2;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 30rpx rgba(74, 111, 227, 0.12);
}
.tag-row { display: flex; align-items: center; flex-wrap: wrap; gap: 14rpx; }
.tag-scope, .tag-status, .tag-cat {
  font-size: 24rpx; padding: 6rpx 20rpx; border-radius: 8rpx; line-height: 1.4;
}
.tag-scope { color: #4A6FE3; background: #ECF0FF; font-weight: 600; }
.tag-cat { color: #909399; background: #F5F6FA; }
.tag-status.reg-ing  { color: #fff; background: #FF7A45; font-weight: 600; }
.tag-status.reg-wait { color: #fff; background: #FAAD14; font-weight: 600; }
.tag-status.reg-end  { color: #909399; background: #F0F1F5; font-weight: 600; }

.countdown {
  margin-top: 20rpx; font-size: 28rpx; font-weight: 600; color: #FF7A45;
}
.countdown text { color: #FF7A45; }

/* 三栏信息 */
.info-grid {
  display: flex; gap: 16rpx; margin-top: 26rpx;
}
.info-item {
  flex: 1; min-width: 0;
  background: #F7F8FC; border-radius: 12rpx; padding: 18rpx 16rpx;
  display: flex; flex-direction: column; gap: 10rpx;
}
.info-label { font-size: 22rpx; color: #909399; flex-shrink: 0; }
.info-value {
  font-size: 24rpx; color: #303133; line-height: 1.5; font-weight: 500;
  display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 3;
  overflow: hidden; word-break: break-all;
}

/* ===== 通用小节 ===== */
.section { margin-top: 30rpx; }
.section-title { font-size: 30rpx; font-weight: 700; color: #303133; margin-bottom: 16rpx; }
.rich-text {
  font-size: 27rpx; color: #606266; line-height: 1.8;
  white-space: pre-wrap; word-break: break-all;
}
.copy-tip {
  margin-top: 20rpx; text-align: center; font-size: 26rpx; color: #4A6FE3;
  background: #ECF0FF; border-radius: 12rpx; padding: 18rpx;
}

/* ===== 往届成果 ===== */
.ach-item { padding: 22rpx 0; border-bottom: 1rpx solid #F2F3F5; }
.ach-item:last-child { border-bottom: none; padding-bottom: 0; }
.ach-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12rpx; }
.ach-team { font-size: 28rpx; font-weight: 600; color: #303133; }
.tag-award {
  font-size: 22rpx; color: #B8860B; background: #FFF7E6;
  padding: 4rpx 16rpx; border-radius: 8rpx; flex-shrink: 0;
}
.ach-intro { font-size: 26rpx; color: #606266; line-height: 1.6; margin-bottom: 12rpx; }
.ach-foot { display: flex; justify-content: space-between; font-size: 24rpx; color: #909399; }
.ach-empty {
  padding: 40rpx 0 10rpx; text-align: center; font-size: 26rpx; color: #C0C4CC;
}

/* ===== 底部操作条 ===== */
.footer-bar {
  position: fixed; left: 0; right: 0; bottom: 0; z-index: 99;
  display: flex; align-items: center; gap: 20rpx;
  padding: 18rpx 30rpx calc(18rpx + env(safe-area-inset-bottom));
  background: linear-gradient(90deg, #4A6FE3 0%, #3D5FD6 100%);
}
.foot-icon {
  width: 76rpx; height: 76rpx; border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.foot-ico { font-size: 36rpx; color: #fff; line-height: 1; }
.foot-ico.on { color: #FFD666; }
.foot-btn {
  flex: 1; height: 76rpx; border-radius: 38rpx;
  background: #fff; color: #4A6FE3;
  font-size: 30rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
</style>
