<template>
  <view class="short-video-page">
    <swiper
      v-if="videoList.length"
      class="video-swiper"
      vertical
      :current="currentIndex"
      :duration="300"
      @change="onVideoChange"
    >
      <swiper-item v-for="(video, index) in videoList" :key="video.id" class="video-slide">
        <view class="video-slide-inner">
          <video
            :id="videoElementId(index)"
            class="video-player"
            :src="video.videoUrl"
            :poster="video.coverUrl"
            :autoplay="index === currentIndex"
            :show-center-play-btn="false"
            :controls="false"
            :loop="true"
            :object-fit="'cover'"
            :enable-progress-gesture="false"
            @click="togglePlay(index)"
          />
          <image v-if="!video.videoUrl && video.coverUrl" class="video-cover" :src="video.coverUrl" mode="aspectFill" />
          <view v-if="index === currentIndex && !isPlaying" class="play-mask" @click="togglePlay(index)">
            <uni-icons type="videocam" size="48" color="#ffffff" />
          </view>

          <view class="video-info">
            <text class="author-name">@{{ video.nickName || video.authorName || ('用户' + video.userId) }}</text>
            <text v-if="video.title" class="video-title">{{ video.title }}</text>
            <text v-if="video.description" class="video-description">{{ video.description }}</text>
          </view>

          <view class="video-actions" @click.stop>
            <view class="avatar-action">
              <image v-if="video.headImage" class="avatar" :src="video.headImage" mode="aspectFill" />
              <view v-else class="avatar avatar-placeholder" :style="avatarPlaceholderStyle(video)">{{ avatarText(video) }}</view>
              <view class="follow-mark" :class="{ followed: isFollowed(video) }" @click="toggleFollow(video)">
                <text>{{ isFollowed(video) ? '-' : '+' }}</text>
              </view>
            </view>
            <view class="action-item" @click="toggleLike(video)">
              <uni-icons :type="video.liked ? 'heart-filled' : 'heart'" size="32" :color="video.liked ? '#f23b54' : '#ffffff'" />
              <text>{{ video.likeCount || 0 }}</text>
            </view>
            <view class="action-item">
              <uni-icons type="chatbubble" size="30" color="#ffffff" />
              <text>{{ video.commentCount || 0 }}</text>
            </view>
            <view class="action-item" @click="toggleFavorite(video)">
              <uni-icons :type="video.favorited ? 'star-filled' : 'star'" size="32" :color="video.favorited ? '#ffd23f' : '#ffffff'" />
              <text>{{ video.favoriteCount || 0 }}</text>
            </view>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <view v-else-if="loading" class="state-view"><uni-icons type="spinner-cycle" size="30" color="#ffffff" /><text>加载中...</text></view>
    <view v-else class="state-view"><text>暂无推荐视频</text></view>

    <view class="top-tabs" @click.stop>
      <view class="back-button" @click="goBack"><uni-icons type="back" size="25" color="#ffffff" /></view>
      <view class="tab-list">
        <text :class="{ active: activeTab === 'friend' }" @click="switchTab('friend')">好友</text>
        <text :class="{ active: activeTab === 'follow' }" @click="switchTab('follow')">关注</text>
        <text :class="{ active: activeTab === 'recom' }" @click="switchTab('recom')">推荐</text>
        <text @click="goToMyVideos">我的</text>
      </view>
    </view>
    <view v-if="loadingMore" class="load-more"><uni-icons type="spinner-cycle" size="20" color="#ffffff" /></view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      videoList: [],
      currentIndex: 0,
      pageNo: 1,
      pageSize: 20,
      total: 0,
      loading: false,
      loadingMore: false,
      isPlaying: true,
      actioning: false,
      pendingPlayVideoIds: [],
      avatarColors: ['#5daa31', '#c7515a', '#e03697', '#85029b', '#c9b455', '#326eb6'],
      activeTab: 'recom' // 值集：recom，follow，friend，my
    }
  },
  computed: {
    hasMore() {
      return this.videoList.length < this.total
    },
    mine() {
      return this.userStore.userInfo;
    },
  },
  created() {
    this.fetchVideos()
  },
  beforeUnmount() {
    this.currentVideoContext().pause()
  },
  methods: {
    videoElementId(index) {
      return `short-video-${index}`
    },
    currentVideoContext() {
      return uni.createVideoContext(this.videoElementId(this.currentIndex), this)
    },
    fetchVideos() {
      if (this.loading || this.loadingMore || (!this.hasMore && this.videoList.length)) return
      const isFirstPage = this.videoList.length === 0
      if (isFirstPage) this.loading = true
      else this.loadingMore = true

      const data = {}
      if (this.activeTab === 'friend') {
        data.section = 'friends'
      } else if (this.activeTab === 'follow') {
        data.section = 'follows'
      }

      this.$http({
        url: `/shortVideo/recommend?pageNo=${this.pageNo}&pageSize=${this.pageSize}`,
        method: 'POST',
        data: data
      }).then((page) => {
        const videos = page.data || []
        this.total = page.total || 0
        this.videoList.push(...videos)
        this.pageNo += 1
        if (isFirstPage && videos.length) this.playCurrentVideo()
      }).finally(() => {
        this.loading = false
        this.loadingMore = false
      })
    },
    switchTab(tab) {
      if (this.activeTab === tab || this.loading || this.loadingMore) return
      this.currentVideoContext().pause()
      this.activeTab = tab
      this.videoList = []
      this.currentIndex = 0
      this.pageNo = 1
      this.total = 0
      this.isPlaying = true
      this.fetchVideos()
    },
    goToMyVideos() {
      this.currentVideoContext().pause()
      uni.navigateTo({
        url: `/pages/short-video/short-video-user?userId=${this.mine.id}`
      })
    },
    playCurrentVideo() {
      this.$nextTick(() => {
        this.currentVideoContext().play()
        this.isPlaying = true
        this.recordPlayCount()
      })
    },
    recordPlayCount() {
      const video = this.videoList[this.currentIndex]
      if (!video || !video.id) return
      const videoId = video.id
      // 同一视频在本次应用运行中仅成功记录一次，接口请求中的视频也不重复提交。
      if (this.pendingPlayVideoIds.includes(videoId) || this.shortVideoStore.hasPlayed(videoId)) return

      this.pendingPlayVideoIds.push(videoId)
      this.$http({
        url: `/shortVideo/addPlayCount/${videoId}`,
        method: 'POST'
      }).then(() => {
        this.shortVideoStore.markPlayed(videoId)
      }).finally(() => {
        const index = this.pendingPlayVideoIds.indexOf(videoId)
        if (index !== -1) this.pendingPlayVideoIds.splice(index, 1)
      })
    },
    onVideoChange(event) {
      const nextIndex = event.detail.current
      if (nextIndex === this.currentIndex) return
      this.currentVideoContext().pause()
      this.currentIndex = nextIndex
      this.playCurrentVideo()
      if (nextIndex >= this.videoList.length - 3 && this.hasMore) this.fetchVideos()
    },
    togglePlay(index) {
      if (index !== this.currentIndex) return
      const context = this.currentVideoContext()
      if (this.isPlaying) context.pause()
      else {
        context.play()
        this.recordPlayCount()
      }
      this.isPlaying = !this.isPlaying
    },
    toggleLike(video) {
      if (!video || !video.id || this.actioning) return
      this.actioning = true
      const liked = video.liked
      this.$http({
        url: liked ? '/shortVideoLike/delete' : '/shortVideoLike/add',
        method: liked ? 'DELETE' : 'POST',
        data: { videoId: video.id }
      }).then(() => {
        video.liked = !liked
        video.likeCount = liked ? Math.max(0, (video.likeCount || 0) - 1) : (video.likeCount || 0) + 1
      }).finally(() => {
        this.actioning = false
      })
    },
    toggleFavorite(video) {
      if (!video || !video.id || this.actioning) return
      this.actioning = true
      const favorited = video.favorited
      this.$http({
        url: favorited ? '/shortVideoFavorite/delete' : '/shortVideoFavorite/add',
        method: favorited ? 'DELETE' : 'POST',
        data: { videoId: video.id }
      }).then(() => {
        video.favorited = !favorited
        video.favoriteCount = favorited ? Math.max(0, (video.favoriteCount || 0) - 1) : (video.favoriteCount || 0) + 1
      }).finally(() => {
        this.actioning = false
      })
    },
    isFollowed(video) {
      return Boolean(video && video.objectId && video.type && this.followStore.isFollow(`${video.objectId}:${video.type}`))
    },
    toggleFollow(video) {
      if (!video || !video.objectId || !video.type || this.actioning) return
      this.actioning = true
      const follow = { targetId: video.objectId, type: video.type }
      const followed = this.isFollowed(video)
      this.$http({
        url: followed ? `/follow/cancel?targetId=${video.objectId}&type=${video.type}` : '/follow/add',
        method: followed ? 'DELETE' : 'POST',
        data: followed ? {} : follow
      }).then((savedFollow) => {
        if (followed) this.followStore.removeFollow(follow)
        else this.followStore.addFollow(savedFollow || follow)
      }).finally(() => {
        this.actioning = false
      })
    },
    avatarText(video) {
      return (video.nickName || video.authorName || '?').slice(0, 1).toUpperCase()
    },
    avatarPlaceholderStyle(video) {
      const name = video.nickName || video.authorName || `用户${video.userId || ''}`
      let hash = 0
      for (let index = 0; index < name.length; index += 1) {
        hash += name.charCodeAt(index)
      }
      return { backgroundColor: this.avatarColors[hash % this.avatarColors.length] }
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped lang="scss">
.short-video-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #000;
  color: #fff;
}

.video-swiper,
.video-slide,
.video-slide-inner {
  width: 100%;
  height: 100%;
}

.video-slide-inner {
  position: relative;
  background: #000;
}

.video-player,
.video-cover {
  width: 100%;
  height: 100%;
}

.video-cover {
  position: absolute;
  top: 0;
  left: 0;
}

.play-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.18);
}

.top-tabs {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  box-sizing: content-box;
  width: 100%;
  height: 130rpx;
  padding-top: env(safe-area-inset-top);
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.45), transparent);
}

.back-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 92rpx;
}

.tab-list {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: space-around;
  padding-right: 20rpx;
  font-size: 30rpx;
  color: rgba(255, 255, 255, 0.75);
}

.tab-list .active {
  position: relative;
  color: #fff;
  font-weight: 600;
}

.tab-list .active::after {
  position: absolute;
  bottom: -14rpx;
  left: 50%;
  width: 30rpx;
  height: 5rpx;
  border-radius: 4rpx;
  background: #fff;
  content: '';
  transform: translateX(-50%);
}

.video-info {
  position: absolute;
  right: 140rpx;
  bottom: calc(54rpx + env(safe-area-inset-bottom));
  left: 30rpx;
  display: flex;
  flex-direction: column;
  text-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.8);
}

.author-name {
  margin-bottom: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
}

.video-title,
.video-description {
  margin-top: 8rpx;
  font-size: 26rpx;
  line-height: 1.45;
}

.video-actions {
  position: absolute;
  right: 24rpx;
  bottom: calc(48rpx + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}

.avatar-action {
  position: relative;
  width: 82rpx;
  height: 96rpx;
}

.avatar {
  width: 76rpx;
  height: 76rpx;
  border: 3rpx solid #fff;
  border-radius: 50%;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
}

.follow-mark {
  position: absolute;
  bottom: 0;
  left: 19rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38rpx;
  height: 38rpx;
  border: 3rpx solid #fff;
  border-radius: 50%;
  background: #22c55e;
  font-size: 32rpx;
  line-height: 32rpx;
}

.follow-mark.followed {
  background: #f23b54;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 82rpx;
  font-size: 23rpx;
  font-weight: 500;
  text-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.8);
}

.action-item text {
  margin-top: 5rpx;
}

.state-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  width: 100%;
  height: 100%;
  color: #fff;
  font-size: 28rpx;
}

.load-more {
  position: absolute;
  right: 30rpx;
  bottom: calc(24rpx + env(safe-area-inset-bottom));
  z-index: 6;
}
</style>
