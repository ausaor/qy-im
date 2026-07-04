<template>
  <div class="short-video-play-container">
    <div class="short-video-play" @wheel="handleWheel">
      <!-- 加载状态 -->
      <div v-if="loading && videoList.length === 0" class="loading-container">
        <i class="el-icon-loading"></i>
        <span>加载中...</span>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && videoList.length === 0" class="empty-container">
        <i class="el-icon-video-camera"></i>
        <p>暂无推荐视频</p>
      </div>

      <!-- 视频播放区 -->
      <template v-else>
        <div class="video-wrapper">
          <video
              ref="videoPlayer"
              :key="currentVideo.id"
              :src="currentVideo.videoUrl"
              :poster="currentVideo.coverUrl"
              class="video-player"
              loop
              playsinline
              webkit-playsinline
              @loadedmetadata="onVideoLoaded"
              @click="togglePlay"
          ></video>

          <!-- 播放/暂停遮罩 -->
          <div v-if="!isPlaying" class="play-overlay" @click="togglePlay">
            <i class="el-icon-video-play"></i>
          </div>

          <!-- 左下角信息区 -->
          <div class="video-info-left">
            <div class="author-name">@{{ currentVideo.nickName || currentVideo.authorName || '用户' + currentVideo.userId }}</div>
            <div class="publish-date">{{ formatDate(currentVideo.createTime) }}</div>
            <div class="video-title">{{ currentVideo.title || '无标题' }}</div>
          </div>

          <!-- 右侧操作区 -->
          <div class="video-actions-right">
            <div class="action-item avatar-item" @click.stop>
              <head-image
                  :url="currentVideo.headImage"
                  :name="currentVideo.authorName || '?'"
                  :size="48"
              ></head-image>
              <div
                class="follow-btn"
                :class="{ followed: videoFollowed }"
                @click.stop="videoFollowed ? cancelFollow(currentVideo) : addFollow(currentVideo)"
              >
                {{ videoFollowed ? '-' : '+' }}
              </div>
            </div>
            <div class="action-item" :class="{ active: currentVideo.liked }" @click.stop="handleLike">
              <i :class="currentVideo.liked ? 'iconfont icon-aixin' : 'iconfont icon-hongxin1'"></i>
              <span class="action-count">{{ currentVideo.likeCount || 0 }}</span>
            </div>
            <div class="action-item" @click.stop="handleComment">
              <i class="el-icon-chat-line-round"></i>
              <span class="action-count">{{ currentVideo.commentCount || 0 }}</span>
            </div>
            <div class="action-item favorite-item" :class="{ active: currentVideo.favorited }" @click.stop="handleFavorite">
              <i :class="currentVideo.favorited ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
              <span class="action-count">{{ currentVideo.favoriteCount || 0 }}</span>
            </div>
          </div>

          <!-- 上下切换按钮 -->
          <div class="nav-buttons">
            <div
                class="nav-btn nav-prev"
                :class="{ disabled: currentIndex <= 0 }"
                @click="prevVideo"
            >
              <i class="el-icon-arrow-up"></i>
            </div>
            <div
                class="nav-btn nav-next"
                :class="{ disabled: currentIndex >= videoList.length - 1 && !hasMore }"
                @click="nextVideo"
            >
              <i class="el-icon-arrow-down"></i>
            </div>
          </div>

          <!-- 底部加载提示 -->
          <div v-if="loadingMore" class="loading-more">
            <i class="el-icon-loading"></i>
          </div>
        </div>
      </template>
    </div>
    <transition name="slide-right">
      <div v-if="showInfo" class="short-video-info">
        <short-video-info :video="currentVideo" @close="showInfo = false" @play-video="onPlayVideo"></short-video-info>
      </div>
    </transition>
  </div>

</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'
import ShortVideoInfo from '@/components/shortVideo/ShortVideoInfo.vue'

export default {
  name: 'ShortVideoPlay',
  components: {
    HeadImage,
    ShortVideoInfo
  },
  props: {
    type: {
      type: String,
      default: null
    },
    objectId: {
      type: Number,
      default: null
    },
    userId: {
      type: Number,
      default: null
    },
    section: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      videoList: [],
      currentIndex: 0,
      pageNo: 1,
      pageSize: 20,
      total: 0,
      loading: false,
      loadingMore: false,
      hasMore: true,
      isPlaying: false,
      showInfo: false
    }
  },
  computed: {
    currentVideo() {
      return this.videoList[this.currentIndex] || {}
    },
    videoFollowed() {
      const video = this.currentVideo
      if (!video || !video.id) return false
      const key = `${video.objectId}:${video.type}`
      return video.followed || this.$store.getters.isFollowed(key)
    }
  },
  created() {
    //this.fetchVideos()
  },
  beforeDestroy() {
    // 组件销毁前停止视频播放并清理资源
    const video = this.$refs.videoPlayer
    if (video) {
      video.pause()
      // 移除 src 防止后台继续加载
      video.removeAttribute('src')
      video.load()
    }
    this.isPlaying = false
    this._pendingPlayCount = null
  },
  methods: {
    async fetchVideos() {
      if (this.loading || this.loadingMore) return

      if (this.videoList.length === 0) {
        this.loading = true
      } else {
        this.loadingMore = true
      }

      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }

      const data = {}
      if (this.type) {
        data.type = this.type
      }
      if (this.objectId) {
        data.objectId = this.objectId
      }
      if (this.userId) {
        data.userId = this.userId
      }
      if (this.section) {
        data.section = this.section
      }

      this.$http({
        url: '/shortVideo/recommend',
        method: 'post',
        params: params,
        data: data
      }).then((res) => {
        const newVideos = res.data || []
        this.total = res.total || 0

        this.videoList.push(...newVideos)
        this.pageNo++

        this.hasMore = this.videoList.length < this.total

        if (this.videoList.length === newVideos.length && this.videoList.length > 0) {
          this.$nextTick(() => {
            this.playVideo()
          })
        }
      }).catch(() => {
      }).finally(() => {
        this.loading = false
        this.loadingMore = false
      })
    },

    playVideo() {
      const video = this.$refs.videoPlayer
      if (video) {
        video.play().then(() => {
          this.isPlaying = true
        }).catch(() => {})
      }
      // 记录播放次数（同一视频仅记录一次）
      this.recordPlayCount()
    },

    recordPlayCount() {
      const videoId = this.currentVideo.id
      if (!videoId) return
      // 已在播放中等待响应，避免重复调用
      if (this._pendingPlayCount === videoId) return
      // 已记录过播放，跳过
      if (this.$store.getters.hasPlayed(videoId)) return

      this._pendingPlayCount = videoId
      this.$http({
        url: `/shortVideo/addPlayCount/${videoId}`,
        method: 'post'
      }).then(() => {
        this.$store.commit('markPlayed', videoId)
      }).finally(() => {
        this._pendingPlayCount = null
      })
    },

    pauseVideo() {
      const video = this.$refs.videoPlayer
      if (video) {
        video.pause()
        this.isPlaying = false
      }
    },

    togglePlay() {
      if (this.isPlaying) {
        this.pauseVideo()
      } else {
        this.playVideo()
      }
    },

    onVideoLoaded() {
      this.playVideo()
    },

    switchVideo(newIndex) {
      if (newIndex < 0 || newIndex >= this.videoList.length) return

      this.pauseVideo()
      this.currentIndex = newIndex
      this.$nextTick(() => {
        this.playVideo()
      })

      if (newIndex >= this.videoList.length - 3 && this.hasMore) {
        this.fetchVideos()
      }
    },

    prevVideo() {
      if (this.currentIndex > 0) {
        this.switchVideo(this.currentIndex - 1)
      }
    },

    nextVideo() {
      if (this.currentIndex < this.videoList.length - 1) {
        this.switchVideo(this.currentIndex + 1)
      }
    },

    handleWheel(event) {
      event.preventDefault()
      if (event.deltaY > 0) {
        this.nextVideo()
      } else {
        this.prevVideo()
      }
    },

    handleLike() {
      const video = this.currentVideo
      if (!video.id) return

      if (video.liked) {
        this.$http({
          url: '/shortVideoLike/delete',
          method: 'delete',
          data: { videoId: video.id }
        }).then(() => {
          video.liked = false
          video.likeCount = Math.max(0, (video.likeCount || 0) - 1)
        })
      } else {
        this.$http({
          url: '/shortVideoLike/add',
          method: 'post',
          data: { videoId: video.id }
        }).then(() => {
          video.liked = true
          video.likeCount = (video.likeCount || 0) + 1
        })
      }
    },

    handleComment() {
      this.showInfo = true
    },

    handleFavorite() {
      const video = this.currentVideo
      if (!video.id) return

      if (video.favorited) {
        this.$http({
          url: '/shortVideoFavorite/delete',
          method: 'delete',
          data: { videoId: video.id }
        }).then(() => {
          video.favorited = false
          video.favoriteCount = Math.max(0, (video.favoriteCount || 0) - 1)
        })
      } else {
        this.$http({
          url: '/shortVideoFavorite/add',
          method: 'post',
          data: { videoId: video.id }
        }).then(() => {
          video.favorited = true
          video.favoriteCount = (video.favoriteCount || 0) + 1
        })
      }
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },
    addFollow(shortVideo) {
      this.$http({
        url: '/follow/add',
        method: 'post',
        data: { targetId: shortVideo.objectId, type: shortVideo.type}
      }).then(() => {
        shortVideo.followed = true;
        this.$store.commit('markFollowed', `${shortVideo.objectId}:${shortVideo.type}`)
      })
    },
    cancelFollow(shortVideo) {
      this.$http({
        url: `/follow/cancel?targetId=${shortVideo.objectId}&type=${shortVideo.type}`,
        method: 'delete',
      }).then(() => {
        shortVideo.followed = false;
        this.$store.commit('unmarkFollowed', `${shortVideo.objectId}:${shortVideo.type}`)
      })
    },

    onPlayVideo(work) {
      if (!work || !work.id) return
      // 检查视频列表中是否已存在该作品
      const existIndex = this.videoList.findIndex(v => v.id === work.id)
      if (existIndex >= 0) {
        this.switchVideo(existIndex)
        return
      }
      // 插入到当前索引之后并切换到它
      this.pauseVideo()
      const insertIndex = this.currentIndex + 1
      this.videoList.splice(insertIndex, 0, { ...work })
      this.currentIndex = insertIndex
      this.$nextTick(() => {
        this.playVideo()
      })
    }
  }
}
</script>

<style scoped lang="scss">
.short-video-play-container {
  display: flex;
  width: 100%;
  height: 100%;
}

.short-video-play {
  flex: 1;
  height: 100%;
  background: #000;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.short-video-info {
  width: 420px;
  min-width: 380px;
  height: 100%;
  background: #fff;
  overflow-y: auto;
  border-left: 1px solid #e8e8e8;
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s ease;
}

.slide-right-enter,
.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;

  i {
    font-size: 48px;
    margin-bottom: 12px;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}

.video-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
  cursor: pointer;
}

.play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 72px;
  height: 72px;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 5;

  i {
    font-size: 32px;
    color: #fff;
    margin-left: 4px;
  }
}

.video-info-left {
  position: absolute;
  bottom: 24px;
  left: 24px;
  color: #fff;
  z-index: 5;
  max-width: 50%;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);

  .author-name {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 4px;
  }

  .publish-date {
    font-size: 12px;
    color: #ccc;
    margin-bottom: 6px;
  }

  .video-title {
    font-size: 14px;
    line-height: 1.4;
    word-break: break-all;
  }
}

.video-actions-right {
  position: absolute;
  right: 16px;
  bottom: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  z-index: 5;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    user-select: none;

    i {
      font-size: 32px;
      color: #fff;
      text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
      transition: color 0.2s, transform 0.15s;
    }

    &:hover i {
      transform: scale(1.15);
    }

    &.active i {
      color: #FF5E5B;
    }

    &.favorite-item.active i {
      color: #FFD700;
    }

    &.avatar-item {
      cursor: default;
      position: relative;

      &:hover i {
        transform: none;
      }
    }

    .follow-btn {
      position: absolute;
      bottom: -6px;
      left: 50%;
      transform: translateX(-50%);
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: #67c23a;
      color: #fff;
      font-size: 14px;
      font-weight: bold;
      line-height: 20px;
      text-align: center;
      cursor: pointer;
      z-index: 1;

      &.followed {
        background: #f56c6c;
      }
    }
  }

  .action-count {
    font-size: 12px;
    color: #fff;
    margin-top: 4px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  }
}

.nav-buttons {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 20px;
  z-index: 5;
}

.nav-btn {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.4);
  }

  &.disabled {
    opacity: 0.3;
    cursor: not-allowed;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }
  }

  i {
    font-size: 18px;
    color: #fff;
  }
}

.loading-more {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 5;

  i {
    font-size: 20px;
    color: #fff;
  }
}
</style>
