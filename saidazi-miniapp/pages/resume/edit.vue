<template>
  <view class="container page-with-footer">
    <!-- 基本信息 -->
    <view class="card">
      <view class="section-title">基本信息</view>

      <view class="form-item">
        <view class="form-label">姓名<text class="req">*</text></view>
        <input class="form-input" v-model="form.name" placeholder="请输入真实姓名" maxlength="20" />
      </view>

      <view class="form-item">
        <view class="form-label">学院 · 专业<text class="req">*</text></view>
        <input
          class="form-input"
          v-model="form.collegeMajor"
          placeholder="如：计算机学院 · 软件工程"
          maxlength="50"
        />
      </view>

      <view class="form-item">
        <view class="form-label">年级<text class="req">*</text></view>
        <view class="seg-group">
          <text
            v-for="g in gradeOptions"
            :key="g"
            class="seg-item"
            :class="{ active: form.grade === g }"
            @tap="form.grade = g"
          >{{ g }}</text>
        </view>
      </view>
    </view>

    <!-- 技能标签 -->
    <view class="card">
      <view class="section-title">技能标签</view>
      <view class="form-tip" style="margin-top:0;margin-bottom:16rpx">
        最多选择 8 个，标签会用于组队智能匹配
      </view>

      <view class="seg-group">
        <text
          v-for="s in skillOptions"
          :key="s"
          class="seg-item"
          :class="{ active: form.skills.includes(s) }"
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

      <view class="selected-skills" v-if="form.skills.length">
        <text class="ss-label">已选：</text>
        <text class="tag tag-green" v-for="(s, i) in form.skills" :key="i">{{ s }}</text>
      </view>
    </view>

    <!-- 参赛经历 -->
    <view class="card">
      <view class="section-title">参赛经历</view>
      <textarea
        class="form-textarea"
        v-model="form.experience"
        placeholder="按时间倒序列出参赛经历，例如：&#10;2025 全国大学生数学建模竞赛 赛区二等奖（负责编程）&#10;2024 校级程序设计大赛 一等奖"
        maxlength="600"
      />
      <view class="form-counter">{{ form.experience.length }}/600</view>
    </view>

    <!-- 个人简介 -->
    <view class="card">
      <view class="section-title">个人简介</view>
      <textarea
        class="form-textarea"
        v-model="form.introduction"
        placeholder="介绍你的方向、擅长的部分，以及希望和什么样的队友合作"
        maxlength="400"
      />
      <view class="form-counter">{{ form.introduction.length }}/400</view>
    </view>

    <!-- 附件简历 -->
    <view class="card">
      <view class="section-title">附件简历（选填）</view>
      <view class="upload-row" v-if="!form.attachmentName" @tap="chooseFile">
        <text class="up-icon">＋</text>
        <text class="up-text">上传 PDF 简历</text>
      </view>
      <view class="file-row" v-else>
        <text class="file-icon">📄</text>
        <text class="file-name ellipsis">{{ form.attachmentName }}</text>
        <text class="file-del" @tap="removeFile">删除</text>
      </view>
      <view class="form-tip">支持 PDF 格式，大小不超过 5MB</view>
    </view>

    <!-- 底部操作 -->
    <view class="footer-bar">
      <view class="btn-plain btn-sm" @tap="goPreview">预览简历</view>
      <view class="btn-primary btn-sm" :class="{ disabled: submitting }" @tap="save">
        {{ submitting ? '保存中…' : '保存简历' }}
      </view>
    </view>
  </view>
</template>

<script>
/**
 * 简历编辑页
 * 行为: 加载已有简历 → 编辑 → 保存；支持上传 PDF 附件
 */
import { resumeApi } from '@/utils/api.js'
import { getUserId } from '@/utils/auth.js'

export default {
  data() {
    return {
      form: {
        userId: null,
        name: '',
        collegeMajor: '',
        grade: '',
        skills: [],
        experience: '',
        introduction: '',
        attachmentUrl: '',
        attachmentName: '',
        status: 1
      },
      gradeOptions: ['大一', '大二', '大三', '大四', '研一', '研二'],
      skillOptions: [
        'Python', 'Java', 'C++', 'Vue', '前端', '后端', '算法',
        '数学建模', '数据分析', 'UI设计', '论文写作', 'MySQL'
      ],
      customSkill: '',
      submitting: false
    }
  },
  onLoad() {
    this.form.userId = getUserId()
    this.loadResume()
  },
  methods: {
    async loadResume() {
      try {
        const data = await resumeApi.my(getUserId())
        if (data && data.id) {
          this.form = {
            ...this.form,
            ...data,
            // 后端 skills 可能是逗号分隔字符串，统一转数组
            skills: this.normalizeSkills(data.skills)
          }
        }
      } catch (e) {
        console.error('加载简历失败', e)
      }
    },

    /** skills 兼容数组 / 逗号分隔字符串 */
    normalizeSkills(v) {
      if (Array.isArray(v)) return v.slice()
      if (typeof v === 'string' && v) return v.split(',').map((s) => s.trim()).filter(Boolean)
      return []
    },

    toggleSkill(s) {
      const arr = this.form.skills
      const i = arr.indexOf(s)
      if (i >= 0) arr.splice(i, 1)
      else {
        if (arr.length >= 8) {
          uni.showToast({ title: '最多选择 8 个技能标签', icon: 'none' })
          return
        }
        arr.push(s)
      }
    },

    addCustomSkill() {
      const s = this.customSkill.trim()
      if (!s) return
      if (this.form.skills.includes(s)) {
        uni.showToast({ title: '该标签已存在', icon: 'none' })
        return
      }
      if (this.form.skills.length >= 8) {
        uni.showToast({ title: '最多选择 8 个技能标签', icon: 'none' })
        return
      }
      this.form.skills.push(s)
      this.customSkill = ''
    },

    chooseFile() {
      const that = this
      uni.chooseFile({
        count: 1,
        extension: ['.pdf'],
        success(res) {
          const file = res.tempFiles && res.tempFiles[0]
          if (!file) return
          if (file.size && file.size > 5 * 1024 * 1024) {
            uni.showToast({ title: '文件不能超过 5MB', icon: 'none' })
            return
          }
          file.__tempPath = file.path || file.tempFilePath
          that.uploadFile(file)
        },
        fail: () => {
          // 微信小程序不支持 chooseFile，降级到 chooseMessageFile
          uni.chooseMessageFile({
            count: 1,
            type: 'file',
            extension: ['.pdf'],
            success(res) {
              const f = res.tempFiles && res.tempFiles[0]
              if (f) that.uploadFile({ ...f, __tempPath: f.path })
            },
            fail: () => uni.showToast({ title: '请从聊天记录中选择文件', icon: 'none' })
          })
        }
      })
    },

    async uploadFile(file) {
      uni.showLoading({ title: '上传中…' })
      try {
        const data = await resumeApi.upload(getUserId(), file.__tempPath)
        this.form.attachmentUrl = (data && (data.url || data.attachmentUrl)) || ''
        this.form.attachmentName = file.name || '简历.pdf'
        uni.showToast({ title: '上传成功', icon: 'success' })
      } catch (e) {
        console.error('上传简历失败', e)
        uni.showToast({ title: '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },

    removeFile() {
      this.form.attachmentUrl = ''
      this.form.attachmentName = ''
      uni.showToast({ title: '已移除附件', icon: 'none' })
    },

    async save() {
      if (!this.form.name.trim()) return uni.showToast({ title: '请填写姓名', icon: 'none' })
      if (!this.form.collegeMajor.trim()) return uni.showToast({ title: '请填写学院专业', icon: 'none' })
      if (!this.form.grade) return uni.showToast({ title: '请选择年级', icon: 'none' })
      if (!this.form.skills.length) return uni.showToast({ title: '请至少选择一个技能标签', icon: 'none' })

      this.submitting = true
      try {
        await resumeApi.save({
          ...this.form,
          // 后端字段为逗号分隔字符串
          skills: this.form.skills.join(','),
          status: 1
        })
        uni.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } catch (e) {
        console.error('保存简历失败', e)
      } finally {
        this.submitting = false
      }
    },

    goPreview() {
      uni.navigateTo({ url: '/pages/resume/preview' })
    }
  }
}
</script>

<style scoped>
.custom-skill-row { display: flex; align-items: center; gap: 16rpx; }
.custom-skill-input {
  flex: 1; height: 68rpx; background: #F7F8FA; border-radius: 12rpx;
  padding: 0 20rpx; font-size: 26rpx; box-sizing: border-box;
}
.custom-skill-add {
  padding: 0 30rpx; height: 68rpx; line-height: 68rpx;
  background: #ECF0FF; color: #4A6FE3; border-radius: 12rpx; font-size: 26rpx;
}

.selected-skills { margin-top: 24rpx; display: flex; align-items: center; flex-wrap: wrap; }
.ss-label { font-size: 24rpx; color: #909399; margin-right: 12rpx; }

.upload-row {
  height: 160rpx; border: 2rpx dashed #DCDFE6; border-radius: 14rpx;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 12rpx; background: #FAFBFC;
}
.up-icon { font-size: 48rpx; color: #C0C4CC; line-height: 1; }
.up-text { font-size: 26rpx; color: #909399; }

.file-row {
  display: flex; align-items: center; gap: 16rpx;
  background: #F7F8FA; border-radius: 14rpx; padding: 24rpx 22rpx;
}
.file-icon { font-size: 36rpx; }
.file-name { flex: 1; font-size: 27rpx; color: #303133; }
.file-del { font-size: 26rpx; color: #FF4D4F; padding: 0 10rpx; }

.disabled { opacity: 0.6; }
</style>
