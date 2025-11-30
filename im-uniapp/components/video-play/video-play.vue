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
              :autoplay="true"
              @error="handleVideoError"
              @loadeddata="handleLoadedData"
              @play="onPlay"
              @pause="onPause"
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
      videoId: `video-${Date.now()}`,
      isPlaying: false,
      videoContext: null
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          // 弹窗显示时，自动播放视频
          this.playVideo()
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
    // 播放视频
    playVideo() {
      try {
        this.videoContext = uni.createVideoContext(this.videoId, this)
        if (this.videoContext) {
          this.videoContext.volume(0.5) // 设置音量为50%
          this.videoContext.play()
        }
      } catch (e) {
        console.warn('playVideo error', e)
      }
    },
    // 暂停视频
    pauseVideo() {
      try {
        if (this.videoContext) {
          this.videoContext.pause()
        }
      } catch (e) {
        console.warn('pauseVideo error', e)
      }
    },
    // 视频加载完成处理
    handleLoadedData() {
      // 视频加载完成后自动播放
      if (this.visible && !this.isPlaying) {
        // 延迟一小段时间再播放，确保视频完全加载
        setTimeout(() => {
          this.playVideo()
        }, 200)
      }
    },
    // 视频开始播放事件
    onPlay() {
      this.isPlaying = true
      // 确保在播放时设置音量
      if (this.videoContext) {
        this.videoContext.volume(0.5)
      }
    },
    // 视频暂停播放事件
    onPause() {
      this.isPlaying = false
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
    width: 100%;
    height: 100%;
    max-width: 100%;
  }

  &__content {
    position: relative;
    width: 100%;
    height: 100%;
    border-radius: 0;
    overflow: hidden;
    background-color: #000;
  }

  &__video-wrapper {
    width: 100%;
    height: 100%;
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