<template>
  <div class="short-video-recom">
    <!-- 新视频提示条 -->
    <transition name="slide-down">
      <div v-if="showNewVideoTip" class="new-video-tip" @click="viewNewVideos">
        <head-image
          :url="latestVideo.headImage"
          :name="latestVideo.nickName || latestVideo.authorName || '?'"
          :size="28"
        ></head-image>
        <span class="tip-text">{{ tipText }}</span>
        <i class="el-icon-close close-btn" @click.stop="dismissNewVideoTip"></i>
      </div>
    </transition>
    <short-video-play ref="videoPlayer"></short-video-play>
  </div>
</template>

<script>
import ShortVideoPlay from '@/components/shortVideo/ShortVideoPlay.vue'
import HeadImage from '@/components/common/HeadImage.vue'

export default {
  name: 'ShortVideoRecom',
  components: {
    ShortVideoPlay,
    HeadImage
  },
  data() {
    return {
      showNewVideoTip: false,
      pendingNewVideos: []
    }
  },
  computed: {
    newVideoCount() {
      return this.pendingNewVideos.length
    },
    latestVideo() {
      return this.pendingNewVideos[this.pendingNewVideos.length - 1] || {}
    },
    tipText() {
      const count = this.newVideoCount
      const name = this.latestVideo.nickName || this.latestVideo.authorName || '有人'
      if (count <= 1) {
        return `${name} 发布了新的短视频`
      }
      return `${name} 等发布了 ${count} 条新短视频`
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.$refs.videoPlayer.fetchVideos()
    })
  },
  created() {
    // 监听事件
    this.$eventBus.$on('new-short-video', (shortVideo) => {
      if (!shortVideo || !shortVideo.id) return
      // 去重：避免同一视频重复添加
      const exists = this.pendingNewVideos.some(v => v.id === shortVideo.id)
      if (!exists) {
        this.pendingNewVideos.push({ ...shortVideo })
      }
      this.showNewVideoTip = true
    });
  },
  beforeDestroy() {
    // 组件销毁时移除监听，避免内存泄漏
    console.log('ShortVideoRecom beforeDestroy');
    this.$eventBus.$off('new-short-video');
  },
  methods: {
    // 点击查看新视频
    viewNewVideos() {
      if (this.pendingNewVideos.length === 0) return
      const videos = [...this.pendingNewVideos]
      this.pendingNewVideos = []
      this.showNewVideoTip = false
      this.$refs.videoPlayer.addNewVideos(videos)
    },
    // 关闭提示条
    dismissNewVideoTip() {
      this.showNewVideoTip = false
    }
  }
}
</script>

<style scoped lang="scss">
.short-video-recom {
  width: 100%;
  height: 100%;
  position: relative;
}

.new-video-tip {
  position: fixed;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  user-select: none;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  }

  .tip-text {
    white-space: nowrap;
  }

  .close-btn {
    margin-left: 4px;
    font-size: 14px;
    cursor: pointer;
    opacity: 0.7;
    transition: opacity 0.2s;

    &:hover {
      opacity: 1;
    }
  }
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter,
.slide-down-leave-to {
  transform: translateX(-50%) translateY(-20px);
  opacity: 0;
}
</style>
