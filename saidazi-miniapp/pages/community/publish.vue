<template>
  <view class="container page-with-footer">
    <!-- 分类 -->
    <view class="card">
      <view class="section-title">选择分类<text class="req">*</text></view>
      <view class="cat-grid">
        <view
          v-for="c in categories"
          :key="c.value"
          class="cat-box"
          :class="{ active: form.category === c.value }"
          @tap="form.category = c.value"
        >
          <text class="cat-icon">{{ catIcon(c.value) }}</text>
          <text class="cat-label">{{ c.label }}</text>
        </view>
      </view>
    </view>

    <!-- 标题 -->
    <view class="card">
      <view class="form-item">
        <view class="form-label">标题<text class="req">*</text></view>
        <input
          class="form-input"
          v-model="form.title"
          placeholder="一句话概括你想说的内容"
          maxlength="50"
        />
        <view class="form-counter">{{ form.title.length }}/50</view>
      </view>
    </view>

    <!-- 正文 -->
    <view class="card">
      <view class="form-item">
        <view class="form-label">正文<text class="req">*</text></view>
        <textarea
          class="form-textarea"
          v-model="form.content"
          placeholder="详细说说吧～可以分享经验、提问、或者聊聊最近的赛事"
          maxlength="2000"
          style="min-height: 360rpx"
        />
        <view class="form-counter">{{ form.content.length }}/2000</view>
      </view>
    </view>

    <!-- 图片 -->
    <view class="card">
      <view class="section-title">
        添加图片
        <text class="more">最多 3 张</text>
      </view>
      <view class="img-grid">
        <view class="img-item" v-for="(img, i) in form.images" :key="i">
          <image :src="img" mode="aspectFill" @tap="previewImg(i)" />
          <view class="img-del" @tap.stop="removeImg(i)">×</view>
        </view>
        <view class="img-add" v-if="form.images.length < 3" @tap="chooseImage">
          <text class="ia-icon">＋</text>
          <text class="ia-text">{{ form.images.length }}/3</text>
        </view>
      </view>
    </view>

    <!-- 设置 -->
    <view class="card">
      <view class="switch-row">
        <view class="flex-1">
          <view class="sr-label">允许评论</view>
          <view class="sr-sub">关闭后其他同学无法评论这条帖子</view>
        </view>
        <switch :checked="form.allowComment === 1" color="#4A6FE3" @change="onCommentSwitch" />
      </view>
    </view>

    <!-- 提交 -->
    <view class="footer-bar">
      <view class="btn-primary" :class="{ disabled: submitting }" @tap="submit">
        {{ submitting ? '发布中…' : '发布帖子' }}
      </view>
    </view>
  </view>
</template>

<script>
/**
 * 发帖页
 * 行为: 分类 + 标题 + 正文 + 图片（≤3张）→ 发布
 */
import { postApi, fileApi } from '@/utils/api.js'
import { POST_CATEGORIES, getUserId } from '@/utils/auth.js'

export default {
  data() {
    return {
      categories: POST_CATEGORIES,
      form: {
        category: 2,
        title: '',
        content: '',
        images: [],
        allowComment: 1
      },
      submitting: false
    }
  },
  methods: {
    catIcon(v) {
      return { 1: '❓', 2: '📘', 3: '📦', 4: '☕' }[Number(v)] || '💬'
    },

    chooseImage() {
      const remain = 3 - this.form.images.length
      if (remain <= 0) return
      const that = this
      uni.chooseImage({
        count: remain,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success(res) {
          const paths = res.tempFilePaths || []
          that.uploadImages(paths)
        }
      })
    },

    async uploadImages(paths) {
      uni.showLoading({ title: '上传中…' })
      try {
        // 本地开发环境未启动后端时，直接使用本地临时路径预览
        const urls = await fileApi.uploadMultiple(paths)
        const list = (Array.isArray(urls) ? urls : []).map((u) => (typeof u === 'string' ? u : u && u.url)).filter(Boolean)
        this.form.images = this.form.images.concat(list.length ? list : paths)
      } catch (e) {
        console.warn('图片上传失败，降级为本地预览', e)
        this.form.images = this.form.images.concat(paths)
      } finally {
        uni.hideLoading()
      }
    },

    removeImg(i) {
      this.form.images.splice(i, 1)
    },

    previewImg(i) {
      uni.previewImage({ current: i, urls: this.form.images })
    },

    onCommentSwitch(e) {
      this.form.allowComment = e.detail.value ? 1 : 0
    },

    async submit() {
      if (this.submitting) return
      if (!this.form.title.trim()) return uni.showToast({ title: '请填写标题', icon: 'none' })
      if (!this.form.content.trim()) return uni.showToast({ title: '请填写正文内容', icon: 'none' })

      this.submitting = true
      try {
        await postApi.publish({
          userId: getUserId(),
          category: this.form.category,
          title: this.form.title.trim(),
          content: this.form.content.trim(),
          images: this.form.images,
          allowComment: this.form.allowComment
        })
        uni.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } catch (e) {
        console.error('发布帖子失败', e)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.cat-grid { display: flex; flex-wrap: wrap; gap: 20rpx; }
.cat-box {
  width: calc((100% - 40rpx) / 3);
  background: #F7F8FA; border-radius: 14rpx; padding: 28rpx 0;
  display: flex; flex-direction: column; align-items: center; gap: 10rpx;
  border: 2rpx solid transparent; box-sizing: border-box;
}
.cat-box.active { background: #ECF0FF; border-color: #4A6FE3; }
.cat-icon { font-size: 40rpx; }
.cat-label { font-size: 26rpx; color: #606266; }
.cat-box.active .cat-label { color: #4A6FE3; font-weight: 500; }

.img-grid { display: flex; flex-wrap: wrap; gap: 20rpx; }
.img-item { position: relative; width: 200rpx; height: 200rpx; border-radius: 12rpx; overflow: hidden; }
.img-item image { width: 100%; height: 100%; }
.img-del {
  position: absolute; top: 0; right: 0;
  width: 44rpx; height: 44rpx; line-height: 40rpx; text-align: center;
  background: rgba(0, 0, 0, 0.55); color: #fff; font-size: 32rpx;
  border-radius: 0 12rpx 0 12rpx;
}
.img-add {
  width: 200rpx; height: 200rpx; border: 2rpx dashed #DCDFE6; border-radius: 12rpx;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 10rpx; background: #FAFBFC;
}
.ia-icon { font-size: 48rpx; color: #C0C4CC; line-height: 1; }
.ia-text { font-size: 24rpx; color: #909399; }

.switch-row { display: flex; align-items: center; gap: 20rpx; }
.sr-label { font-size: 28rpx; color: #303133; margin-bottom: 6rpx; }
.sr-sub { font-size: 24rpx; color: #909399; }

.req { color: #FF4D4F; margin-left: 6rpx; }
.disabled { opacity: 0.55; }
</style>
