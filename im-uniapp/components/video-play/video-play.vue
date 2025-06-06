<template>
  <view class="video-popup" :class="{ 'video-popup--visible': visible }">
    <view class="video-popup__mask" @tap="handleClose"></view>
    <view class="video-popup__container">
      <view class="video-popup__content">
        <view class="video-popup__video-wrapper">
          <video
              :id="videoId"
              class="video-popup__video"
              :src="videoUrl"
              :poster="coverUrl"
              :controls="true"
              :show-center-play-btn="true"
              :enable-progress-gesture="true"
              :show-fullscreen-btn="true"
              :show-play-btn="true"
              :show-progress="true"
              :object-fit="objectFit"
              @error="handleVideoError"
          ></video>
        </view>
        <view class="video-popup__close" @tap="handleClose">
          <text class="video-popup__close-icon">×</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'VideoPopup',
  props: {
    // 视频链接
    videoUrl: {
      type: String,
      required: true
    },
    // 视频封面图片
    coverUrl: {
      type: String,
      default: ''
    },
    // 是否显示弹窗
    visible: {
      type: Boolean,
      default: false
    },
    // 视频填充模式
    objectFit: {
      type: String,
      default: 'contain', // contain, fill, cover
      validator: (value) => ['contain', 'fill', 'cover'].includes(value)
    }
  },
  data() {
    return {
      videoId: `video-${Date.now()}`
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          // 弹窗显示时，可以执行一些初始化操作
        })
      } else {
        // 弹窗关闭时，暂停视频播放
        this.pauseVideo()
      }
    }
  },
  methods: {
    // 关闭弹窗
    handleClose() {
      this.pauseVideo()
      this.$emit('update:visible', false)
      this.$emit('close')
    },
    // 暂停视频
    pauseVideo() {
      try {
        const videoContext = uni.createVideoContext(this.videoId, this)
        if (videoContext) {
          videoContext.pause()
        }
      } catch (e) {
        console.warn('pauseVideo error', e)
      }
    },
    // 视频错误处理
    handleVideoError(e) {
      console.error('Video error:', e)
      uni.showToast({
        title: '视频加载失败',
        icon: 'none'
      })
      this.$emit('error', e)
    }
  }
}
</script>

<style lang="scss">
.video-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;

  &--visible {
    opacity: 1;
    visibility: visible;
  }

  &__mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.7);
  }

  &__container {
    position: relative;
    z-index: 1000;
    width: 90%;
    max-width: 750rpx;
  }

  &__content {
    position: relative;
    width: 100%;
    border-radius: 12rpx;
    overflow: hidden;
    background-color: #000;
  }

  &__video-wrapper {
    width: 100%;
    /* 16:9 aspect ratio */
    padding-top: 56.25%;
    position: relative;
  }

  &__video {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }

  &__close {
    position: absolute;
    top: 20rpx;
    right: 20rpx;
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    z-index: 1001;
  }

  &__close-icon {
    color: #fff;
    font-size: 40rpx;
    line-height: 1;
  }
}
</style>
