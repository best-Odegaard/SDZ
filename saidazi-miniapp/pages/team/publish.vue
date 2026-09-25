<template>
  <view class="container page-with-footer">
    <!-- 关联赛事 -->
    <view class="card">
      <view class="section-title">关联赛事</view>
      <view class="comp-picker" @tap="pickCompetition">
        <text class="cp-text ellipsis">{{ form.competitionTitle || '选择赛事（选填）' }}</text>
        <text class="cp-arrow">›</text>
      </view>
      <view class="form-tip">关联赛事后，组队会出现在该赛事的「找搭子」列表里</view>
    </view>

    <!-- 组队信息 -->
    <view class="card">
      <view class="section-title">组队信息</view>

      <view class="form-item">
        <view class="form-label">组队标题<text class="req">*</text></view>
        <input
          class="form-input"
          v-model="form.title"
          placeholder="一句话说清你要找什么样的队友"
          maxlength="40"
        />
        <view class="form-counter">{{ form.title.length }}/40</view>
      </view>

      <view class="form-item">
        <view class="form-label">需要人数<text class="req">*</text></view>
        <view class="num-picker">
          <view class="np-btn" @tap="changeCount(-1)">−</view>
          <text class="np-value">{{ form.needCount }}</text>
          <view class="np-btn" @tap="changeCount(1)">＋</view>
          <text class="np-unit">人</text>
        </view>
      </view>

      <view class="form-item">
        <view class="form-label">所需技能（可多选）</view>
        <view class="seg-group">
          <text
            v-for="s in skillOptions"
            :key="s"
            class="seg-item"
            :class="{ active: form.skillsRequired.includes(s) }"
            @tap="toggleSkill(s)"
          >{{ s }}</text>
        </view>
        <view class="custom-skill-row">
          <input
            class="custom-skill-input"
            v-model="customSkill"
            placeholder="自定义技能标签"
            maxlength="10"
          />
          <view class="custom-skill-add" @tap="addCustomSkill">添加</view>
        </view>
      </view>

      <view class="form-item">
        <view class="form-label">项目 / 赛事说明<text class="req">*</text></view>
        <textarea
          class="form-textarea"
          v-model="form.description"
          placeholder="介绍一下目前的分工、进度、以及你希望队友承担的部分"
          maxlength="500"
        />
        <view class="form-counter">{{ form.description.length }}/500</view>
      </view>
    </view>

    <!-- 申请设置 -->
    <view class="card">
      <view class="section-title">申请设置</view>

      <view class="switch-row">
        <view class="flex-1">
          <view class="sr-label">要求申请者附上简历</view>
          <view class="sr-sub">开启后，申请人需提交简历才能申请</view>
        </view>
        <switch :checked="form.requireResume === 1" color="#4A6FE3" @change="onResumeSwitch" />
      </view>

      <view class="form-item">
        <view class="form-label">联系方式<text class="req">*</text></view>
        <input
          class="form-input"
          v-model="form.contact"
          placeholder="微信号 / QQ / 手机号"
          maxlength="40"
        />
        <view class="form-tip">仅在申请通过后展示给队友</view>
      </view>

      <view class="form-item">
        <view class="form-label">备注（选填）</view>
        <input
          class="form-input"
          v-model="form.remark"
          placeholder="例如：优先考虑有获奖经历的同学"
          maxlength="60"
        />
      </view>
    </view>

    <!-- 提交 -->
    <view class="footer-bar">
      <view class="btn-primary" :class="{ disabled: !canSubmit }" @tap="submit">发布组队</view>
    </view>
  </view>
</template>

<script>
/**
 * 发布组队页
 * 参数: competitionId 预选赛事（从赛事详情跳入时携带）
 * 行为: 表单校验 → 提交 → 返回列表
 */
import { teamApi, competitionApi } from '@/utils/api.js'
import { getUserId } from '@/utils/auth.js'

export default {
  data() {
    return {
      form: {
        competitionId: null,
        competitionTitle: '',
        title: '',
        needCount: 2,
        skillsRequired: [],
        description: '',
        contact: '',
        requireResume: 0,
        remark: ''
      },
      skillOptions: ['Python', 'Java', '算法', '数学建模', '前端', '后端', 'UI设计', '数据分析', '论文写作'],
      customSkill: '',
      competitionOptions: [],
      submitting: false
    }
  },
  computed: {
    canSubmit() {
      return (
        this.form.title.trim() &&
        this.form.description.trim() &&
        this.form.contact.trim() &&
        !this.submitting
      )
    }
  },
  onLoad(options) {
    this.loadCompetitionOptions(options.competitionId)
  },
  methods: {
    async loadCompetitionOptions(presetId) {
      try {
        const data = await competitionApi.list({ status: 1, page: 1, size: 20 })
        const rows = Array.isArray(data) ? data : (data.list || data.records || [])
        this.competitionOptions = rows
        if (presetId) {
          const hit = rows.find((c) => String(c.id) === String(presetId))
          if (hit) {
            this.form.competitionId = hit.id
            this.form.competitionTitle = hit.title
          }
        }
      } catch (e) {
        this.competitionOptions = []
      }
    },

    pickCompetition() {
      const that = this
      const items = ['不关联赛事'].concat(this.competitionOptions.map((c) => c.title))
      uni.showActionSheet({
        itemList: items.slice(0, 6),
        success(res) {
          if (res.tapIndex === 0) {
            that.form.competitionId = null
            that.form.competitionTitle = ''
          } else {
            const c = that.competitionOptions[res.tapIndex - 1]
            that.form.competitionId = c.id
            that.form.competitionTitle = c.title
          }
        }
      })
    },

    changeCount(delta) {
      const next = this.form.needCount + delta
      if (next < 1 || next > 20) return
      this.form.needCount = next
    },

    toggleSkill(s) {
      const arr = this.form.skillsRequired
      const i = arr.indexOf(s)
      if (i >= 0) arr.splice(i, 1)
      else {
        if (arr.length >= 6) {
          uni.showToast({ title: '最多选择 6 个技能标签', icon: 'none' })
          return
        }
        arr.push(s)
      }
    },

    addCustomSkill() {
      const s = this.customSkill.trim()
      if (!s) return
      if (this.form.skillsRequired.includes(s)) {
        uni.showToast({ title: '该标签已存在', icon: 'none' })
        return
      }
      if (this.form.skillsRequired.length >= 6) {
        uni.showToast({ title: '最多选择 6 个技能标签', icon: 'none' })
        return
      }
      this.form.skillsRequired.push(s)
      this.customSkill = ''
    },

    onResumeSwitch(e) { this.form.requireResume = e.detail.value ? 1 : 0 },

    async submit() {
      if (!this.canSubmit) {
        if (!this.form.title.trim()) return uni.showToast({ title: '请填写组队标题', icon: 'none' })
        if (!this.form.description.trim()) return uni.showToast({ title: '请填写项目说明', icon: 'none' })
        if (!this.form.contact.trim()) return uni.showToast({ title: '请填写联系方式', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        await teamApi.publish({
          competitionId: this.form.competitionId,
          userId: getUserId(),
          title: this.form.title.trim(),
          needCount: this.form.needCount,
          currentCount: 1,
          skillsRequired: this.form.skillsRequired,
          description: this.form.description.trim(),
          contact: this.form.contact.trim(),
          requireResume: this.form.requireResume,
          remark: this.form.remark.trim(),
          status: 0
        })
        uni.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } catch (e) {
        console.error('发布组队失败', e)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.comp-picker {
  display: flex; align-items: center; justify-content: space-between;
  background: #F7F8FA; border-radius: 12rpx; padding: 24rpx 22rpx;
}
.cp-text { font-size: 27rpx; color: #303133; flex: 1; }
.cp-arrow { font-size: 30rpx; color: #C0C4CC; margin-left: 12rpx; }

.num-picker { display: flex; align-items: center; gap: 28rpx; }
.np-btn {
  width: 64rpx; height: 64rpx; border-radius: 50%; background: #F2F3F5;
  color: #4A6FE3; font-size: 34rpx; display: flex; align-items: center; justify-content: center;
}
.np-value { font-size: 34rpx; font-weight: 700; color: #303133; min-width: 60rpx; text-align: center; }
.np-unit { font-size: 26rpx; color: #909399; }

.custom-skill-row { display: flex; align-items: center; gap: 16rpx; margin-top: 8rpx; }
.custom-skill-input {
  flex: 1; height: 68rpx; background: #F7F8FA; border-radius: 12rpx;
  padding: 0 20rpx; font-size: 26rpx; box-sizing: border-box;
}
.custom-skill-add {
  padding: 0 30rpx; height: 68rpx; line-height: 68rpx;
  background: #ECF0FF; color: #4A6FE3; border-radius: 12rpx; font-size: 26rpx;
}

.switch-row {
  display: flex; align-items: center; gap: 20rpx;
  padding: 24rpx 0; border-bottom: 1rpx solid #F2F3F5;
}
.sr-label { font-size: 28rpx; color: #303133; margin-bottom: 6rpx; }
.sr-sub { font-size: 24rpx; color: #909399; }

.disabled { opacity: 0.55; }
</style>
