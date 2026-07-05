<template>
  <div v-if="visible" class="short-video-float-overlay" @click.self="close">
    <div class="short-video-float-panel">
      <!-- 关闭按钮 -->
      <div class="float-close-btn" @click="close">
        <i class="el-icon-close"></i>
      </div>

      <!-- 顶部标签栏，z-index置于视频之上 -->
      <div class="float-tabs">
        <div
          class="float-tab-item"
          :class="{ active: activeTab === 'follow' }"
          @click="switchTab('follow')"
        >关注</div>
        <div
          class="float-tab-item"
          :class="{ active: activeTab === 'friend' }"
          @click="switchTab('friend')"
        >好友</div>
        <div
          class="float-tab-item"
          :class="{ active: activeTab === 'recom' }"
          @click="switchTab('recom')"
        >推荐</div>
        <div
          class="float-tab-item"
          :class="{ active: activeTab === 'mine' }"
          @click="switchTab('mine')"
        >我的</div>
      </div>

      <!-- 视频播放区域 -->
      <div class="float-video-area" @wheel.prevent="handleWheel">
        <!-- 加载状态 -->
        <div v-if="loading && videoList.length === 0" class="float-loading">
          <i class="el-icon-loading"></i>
          <span>加载中...</span>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading && videoList.length === 0" class="float-empty">
          <i class="el-icon-video-camera"></i>
          <p>暂无视频</p>
        </div>

        <!-- 视频播放 -->
        <template v-else>
          <video
            ref="videoPlayer"
            :key="currentVideo.id"
            :src="currentVideo.videoUrl"
            :poster="currentVideo.coverUrl"
            class="float-video-player"
            loop
            playsinline
            webkit-playsinline
            @loadedmetadata="onVideoLoaded"
            @click="togglePlay"
          ></video>

          <!-- 播放/暂停遮罩 -->
          <div v-if="!isPlaying" class="float-play-overlay" @click="togglePlay">
            <i class="el-icon-video-play"></i>
          </div>

          <!-- 左下角信息区 -->
          <div class="float-video-info-left">
            <div class="float-author-name">@{{ currentVideo.nickName || currentVideo.authorName || '用户' + currentVideo.userId }}</div>
            <div class="float-publish-date">{{ formatDate(currentVideo.createTime) }}</div>
            <div class="float-video-title">{{ currentVideo.title || '无标题' }}</div>
          </div>

          <!-- 右下角操作区：头像、点赞、评论、收藏 -->
          <div class="float-actions-right">
            <div class="float-action-item float-avatar-item" @click.stop>
              <head-image
                :url="currentVideo.headImage"
                :name="currentVideo.authorName || '?'"
                :size="44"
              ></head-image>
              <div
                class="float-follow-btn"
                :class="{ followed: videoFollowed }"
                @click.stop="videoFollowed ? cancelFollow(currentVideo) : addFollow(currentVideo)"
              >
                {{ videoFollowed ? '-' : '+' }}
              </div>
            </div>
            <div class="float-action-item" :class="{ active: currentVideo.liked }" @click.stop="handleLike">
              <i :class="currentVideo.liked ? 'iconfont icon-aixin' : 'iconfont icon-hongxin1'"></i>
              <span class="float-action-count">{{ currentVideo.likeCount || 0 }}</span>
            </div>
            <div class="float-action-item" @click.stop="handleComment">
              <i class="el-icon-chat-line-round"></i>
              <span class="float-action-count">{{ currentVideo.commentCount || 0 }}</span>
            </div>
            <div class="float-action-item float-favorite-item" :class="{ active: currentVideo.favorited }" @click.stop="handleFavorite">
              <i :class="currentVideo.favorited ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
              <span class="float-action-count">{{ currentVideo.favoriteCount || 0 }}</span>
            </div>
          </div>

          <!-- 上下切换按钮 -->
          <div class="float-nav-buttons">
            <div
              class="float-nav-btn"
              :class="{ disabled: currentIndex <= 0 }"
              @click="prevVideo"
            >
              <i class="el-icon-arrow-up"></i>
            </div>
            <div
              class="float-nav-btn"
              :class="{ disabled: currentIndex >= videoList.length - 1 && !hasMore }"
              @click="nextVideo"
            >
              <i class="el-icon-arrow-down"></i>
            </div>
          </div>

          <!-- 底部加载提示 -->
          <div v-if="loadingMore" class="float-loading-more">
            <i class="el-icon-loading"></i>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'

export default {
  name: 'ShortVideoFloat',
  components: {
    HeadImage
  },
  props: {
    showFloat: {
      type: Boolean,
      default: false
    },
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
    },
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
      activeTab: this.tab
    }
  },
  computed: {
    visible() {
      return this.showFloat
    },
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
  watch: {
    showFloat(val) {
      if (val) {
        this.activeTab = this.tab
        if (this.activeTab === 'recom') {
          this.resetAndFetch()
        }
      } else {
        this.cleanup()
      }
    }
  },
  beforeDestroy() {
    this.cleanup()
  },
  methods: {
    close() {
      this.$emit('update:showFloat', false)
    },

    switchTab(tab) {
      this.activeTab = tab
      if (tab === 'recom') {
        this.resetAndFetch()
      }
      // 关注、好友、我的 三个页签功能先闲置
    },

    resetAndFetch() {
      this.pauseVideo()
      this.videoList = []
      this.currentIndex = 0
      this.pageNo = 1
      this.total = 0
      this.hasMore = true
      this.fetchVideos()
    },

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
      this.recordPlayCount()
    },

    recordPlayCount() {
      const videoId = this.currentVideo.id
      if (!videoId) return
      if (this._pendingPlayCount === videoId) return
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

      // 播放到倒数第3个时预加载更多
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
      // 点击评论数时先不加任何操作
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

    addFollow(shortVideo) {
      this.$http({
        url: '/follow/add',
        method: 'post',
        data: { targetId: shortVideo.objectId, type: shortVideo.type }
      }).then(() => {
        shortVideo.followed = true
        this.$store.commit('markFollowed', `${shortVideo.objectId}:${shortVideo.type}`)
      })
    },

    cancelFollow(shortVideo) {
      this.$http({
        url: `/follow/cancel?targetId=${shortVideo.objectId}&type=${shortVideo.type}`,
        method: 'delete'
      }).then(() => {
        shortVideo.followed = false
        this.$store.commit('unmarkFollowed', `${shortVideo.objectId}:${shortVideo.type}`)
      })
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },

    cleanup() {
      const video = this.$refs.videoPlayer
      if (video) {
        video.pause()
        video.removeAttribute('src')
        video.load()
      }
      this.isPlaying = false
      this._pendingPlayCount = null
    }
  }
}
</script>

<style scoped lang="scss">
.short-video-float-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.55);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.short-video-float-panel {
  position: relative;
  width: 400px;
  height: 75vh;
  max-height: 720px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.45);
  background: #000;
}

// 关闭按钮
.float-close-btn {
  position: absolute;
  top: 10px;
  right: 12px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 20;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.45);
  }

  i {
    font-size: 14px;
    color: #fff;
  }
}

// 顶部标签栏 - z-index置于视频之上
.float-tabs {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0));
  z-index: 10;
}

.float-tab-item {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  padding: 4px 8px;
  position: relative;
  user-select: none;
  transition: color 0.2s, font-weight 0.2s;

  &:hover {
    color: #fff;
  }

  &.active {
    color: #fff;
    font-weight: 600;
    font-size: 16px;

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 50%;
      transform: translateX(-50%);
      width: 20px;
      height: 3px;
      border-radius: 2px;
      background: #fff;
    }
  }
}

// 视频区域
.float-video-area {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.float-loading,
.float-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;

  i {
    font-size: 40px;
    margin-bottom: 10px;
  }

  p {
    font-size: 13px;
    margin: 0;
  }
}

.float-video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
  cursor: pointer;
}

.float-play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 5;

  i {
    font-size: 28px;
    color: #fff;
    margin-left: 3px;
  }
}

// 左下角信息
.float-video-info-left {
  position: absolute;
  bottom: 20px;
  left: 16px;
  color: #fff;
  z-index: 5;
  max-width: 55%;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);

  .float-author-name {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 3px;
  }

  .float-publish-date {
    font-size: 11px;
    color: #ccc;
    margin-bottom: 4px;
  }

  .float-video-title {
    font-size: 13px;
    line-height: 1.4;
    word-break: break-all;
  }
}

// 右下角操作区
.float-actions-right {
  position: absolute;
  right: 12px;
  bottom: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  z-index: 5;

  .float-action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    user-select: none;

    i {
      font-size: 30px;
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

    &.float-favorite-item.active i {
      color: #FFD700;
    }

    &.float-avatar-item {
      position: relative;
      cursor: default;

      &:hover i {
        transform: none;
      }
    }
  }

  .float-follow-btn {
    position: absolute;
    bottom: -8px;
    left: 50%;
    transform: translateX(-50%);
    width: 18px;
    height: 18px;
    border-radius: 50%;
    background: #67c23a;
    color: #fff;
    font-size: 12px;
    font-weight: bold;
    line-height: 18px;
    text-align: center;
    cursor: pointer;
    z-index: 1;

    &.followed {
      background: #f56c6c;
    }
  }

  .float-action-count {
    font-size: 11px;
    color: #fff;
    margin-top: 3px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  }
}

// 上下切换按钮
.float-nav-buttons {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 16px;
  z-index: 5;
}

.float-nav-btn {
  width: 32px;
  height: 32px;
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
    font-size: 16px;
    color: #fff;
  }
}

// 底部加载
.float-loading-more {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 5;

  i {
    font-size: 18px;
    color: #fff;
  }
}
</style>