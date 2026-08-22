<template>
  <view class="short-video-page">
    <swiper
        v-if="videoList.length"
        class="video-swiper"
        :class="{ 'comment-open': showCommentPanel }"
        vertical
        :current="renderCurrentIndex"
        :duration="300"
        @change="onVideoChange"
    >
      <swiper-item v-for="({ video, index }) in renderedVideos" :key="video.id" class="video-slide">
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
              :object-fit="'contain'"
              :enable-progress-gesture="false"
              @click="togglePlay(index)"
          />
          <image v-if="!video.videoUrl && video.coverUrl" class="video-cover" :src="video.coverUrl" mode="aspectFit"/>
          <view v-if="index === currentIndex && !isPlaying" class="play-mask" @click="togglePlay(index)">
            <uni-icons type="videocam" size="48" color="#ffffff"/>
          </view>

          <view class="video-info">
            <text class="author-name" @click="toViewUser(video.userId)">@{{ video.nickName || ('用户' + video.userId) }}</text>
            <view v-if="video.createTime || video.address" class="video-meta">
              <text v-if="video.createTime">{{ formatCreateDate(video.createTime) }}</text>
              <text v-if="video.address">{{ video.address }}</text>
            </view>
            <text v-if="video.title" class="video-title">{{ video.title }}</text>
          </view>

          <view class="video-actions" @click.stop>
            <view class="avatar-action" @click="goToUser(video)">
              <image v-if="video.headImage" class="avatar" :src="video.headImage" mode="aspectFill"/>
              <view v-else class="avatar avatar-placeholder" :style="avatarPlaceholderStyle(video)">{{
                  avatarText(video)
                }}
              </view>
              <view v-if="!isFollowed(video)" class="follow-mark" @click.stop="toggleFollow(video)">
                <text>+</text>
              </view>
            </view>
            <view class="action-item" @click="toggleLike(video)">
              <uni-icons :type="video.liked ? 'heart-filled' : 'heart'" size="40"
                         :color="video.liked ? '#f23b54' : '#ffffff'"/>
              <text>{{ video.likeCount || 0 }}</text>
            </view>
            <view class="action-item" @click="openComments(video)">
              <uni-icons type="chatbubble" size="38" color="#ffffff"/>
              <text>{{ video.commentCount || 0 }}</text>
            </view>
            <view class="action-item" @click="toggleFavorite(video)">
              <uni-icons :type="video.favorited ? 'star-filled' : 'star'" size="40"
                         :color="video.favorited ? '#ffd23f' : '#ffffff'"/>
              <text>{{ video.favoriteCount || 0 }}</text>
            </view>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <view v-else-if="loading" class="state-view">
      <uni-icons type="spinner-cycle" size="30" color="#ffffff"/>
      <text>加载中...</text>
    </view>
    <view v-else class="state-view">
      <text>暂无推荐视频</text>
    </view>

    <view v-show="!showCommentPanel" class="top-tabs" @click.stop>
      <view class="back-button" @click="goBack">
        <uni-icons type="back" size="25" color="#ffffff"/>
      </view>
      <view class="tab-list">
        <view class="tab-item" @click="switchTab('friend')">
          <text :class="{ active: activeTab === 'friend' }">好友</text>
          <view v-show="friendShortVideoCount > 0" class="notify-dot"/>
        </view>
        <view class="tab-item" @click="switchTab('follow')">
          <text :class="{ active: activeTab === 'follow' }">关注</text>
          <view v-show="followShortVideoCount > 0" class="notify-dot"/>
        </view>
        <text :class="{ active: activeTab === 'star' }" @click="switchTab('star')">星选</text>
        <text :class="{ active: activeTab === 'recom' }" @click="switchTab('recom')">推荐</text>
        <view class="my-tab" @click="goToMyVideos">
          <text>我的</text>
          <view v-show="shortVideoNotifyCount > 0" class="notify-dot"/>
        </view>
      </view>
    </view>
    <view v-if="loadingMore" class="load-more">
      <uni-icons type="spinner-cycle" size="20" color="#ffffff"/>
    </view>

    <short-video-comment-panel
        :visible="showCommentPanel"
        :video="currentVideo"
        @close="closeComments"
        @comment-count-change="changeCurrentVideoCommentCount"
    />
  </view>
</template>

<script>
import ShortVideoCommentPanel from '../../components/short-video-comment-panel/short-video-comment-panel.vue'

export default {
  components: {ShortVideoCommentPanel},
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
      showCommentPanel: false,
      avatarColors: ['#5daa31', '#c7515a', '#e03697', '#85029b', '#c9b455', '#326eb6'],
      activeTab: 'recom', // 值集：recom，follow，friend，my
    }
  },
  computed: {
    // 只渲染当前播放项及其前后相邻项，其他视频仅保留数据，不创建 video 节点。
    renderedVideos() {
      const start = Math.max(0, this.currentIndex - 1)
      const end = Math.min(this.videoList.length, this.currentIndex + 2)
      return this.videoList.slice(start, end).map((video, offset) => ({
        video,
        index: start + offset
      }))
    },
    // currentIndex 是完整列表的下标；swiper 的 current 是渲染窗口内的下标。
    renderCurrentIndex() {
      return this.currentIndex - Math.max(0, this.currentIndex - 1)
    },
    hasMore() {
      return this.videoList.length < this.total
    },
    mine() {
      return this.userStore.userInfo;
    },
    currentVideo() {
      return this.videoList[this.currentIndex] || {}
    },
    shortVideoNotifyCount() {
      return this.shortVideoStore.getShortVideoNotifyCount()
    },
    friends() {
      return this.friendStore.friends;
    },
    follows() {
      return this.followStore.follows
    },
    shortVideos() {
      return this.shortVideoStore.shortVideoMap;
    },
    friendShortVideoCount() {
      return this.friends.reduce((count, friend) => {
        const videos = this.shortVideos.get(`${friend.id}-user`)
        return count + (videos ? videos.length : 0)
      }, 0)
    },
    followShortVideoCount() {
      return this.follows.reduce((count, follow) => {
        const videos = this.shortVideos.get(`${follow.targetId}-${follow.type}`)
        return count + (videos ? videos.length : 0)
      }, 0)
    }
  },
  created() {
    this.fetchVideos()
  },
  beforeUnmount() {
    this.currentVideoContext().pause()
  },
  methods: {
    formatCreateDate(createTime) {
      return String(createTime).slice(0, 10)
    },
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
      } else if (this.activeTab === 'star') {
        data.section = 'allCharacters'
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
        if (isFirstPage && videos.length) {
          this.playCurrentVideo()
        }
      }).finally(() => {
        this.loading = false
        this.loadingMore = false
      })
    },
    switchTab(tab) {
      if (this.activeTab === tab || this.loading || this.loadingMore) return
      this.currentVideoContext().pause()
      if (tab === 'friend' && this.friendShortVideoCount > 0) {
        this.shortVideoStore.clearFriendShortVideos()
      } else if (tab === 'follow' && this.followShortVideoCount > 0) {
        this.shortVideoStore.clearFollowShortVideos()
      }
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
        url: `/pages/short-video/short-video-my`
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
      const renderedVideo = this.renderedVideos[event.detail.current]
      if (!renderedVideo) return
      const nextIndex = renderedVideo.index
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
        data: {videoId: video.id}
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
        data: {videoId: video.id}
      }).then(() => {
        video.favorited = !favorited
        video.favoriteCount = favorited ? Math.max(0, (video.favoriteCount || 0) - 1) : (video.favoriteCount || 0) + 1
      }).finally(() => {
        this.actioning = false
      })
    },
    openComments(video) {
      if (!video || !video.id) return
      this.showCommentPanel = true
    },
    closeComments() {
      this.showCommentPanel = false
    },
    changeCurrentVideoCommentCount(delta) {
      if (!this.currentVideo.id) return
      this.currentVideo.commentCount = Math.max(0, (this.currentVideo.commentCount || 0) + delta)
    },
    isFollowed(video) {
      return Boolean(video && video.objectId && video.type && this.followStore.isFollow(`${video.objectId}:${video.type}`))
    },
    toggleFollow(video) {
      if (!video || !video.objectId || !video.type || this.actioning) return
      this.actioning = true
      const follow = {targetId: video.objectId, type: video.type}
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
    goToUser(video) {
      if (!video || !video.objectId || !video.type) return
      uni.navigateTo({
        url: `/pages/short-video/short-video-user?targetId=${encodeURIComponent(video.objectId)}&targetType=${encodeURIComponent(video.type)}&videoId=${encodeURIComponent(video.id)}`
      })
    },
    toViewUser(userId) {
      if (userId && userId > 0) {
        uni.navigateTo({
          url: "/pages/common/user-info?id=" + userId
        })
      }
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
      return {backgroundColor: this.avatarColors[hash % this.avatarColors.length]}
    },
    goBack() {
      uni.navigateBack()
    },
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
}

.video-swiper,
.video-slide,
.video-slide-inner {
  width: 100%;
  height: 100%;
}

.video-swiper.comment-open {
  height: 36vh;
}

.video-swiper.comment-open .video-info {
  right: 125rpx;
  bottom: 24rpx;
}

.video-swiper.comment-open .video-actions {
  right: 14rpx;
  bottom: 90rpx;
  gap: 12rpx;
  transform: scale(0.82);
  transform-origin: right bottom;
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

.my-tab {
  position: relative;
}

.tab-item {
  position: relative;
}

.notify-dot {
  position: absolute;
  top: -8rpx;
  right: -12rpx;
  width: 12rpx;
  height: 12rpx;
  border: 2rpx solid rgba(0, 0, 0, 0.45);
  border-radius: 50%;
  background: #f0445d;
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
  bottom: 8%;
  left: 30rpx;
  display: flex;
  flex-direction: column;
  color: #fff;
  text-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.8);
}

.author-name {
  margin-bottom: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
}

.video-meta {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 4rpx;
  color: rgba(255, 255, 255, 0.82);
  font-size: 22rpx;
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
  bottom: 8%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 34rpx;
}

.avatar-action {
  position: relative;
  width: 100rpx;
  height: 116rpx;
}

.avatar {
  width: 94rpx;
  height: 94rpx;
  border: 4rpx solid #fff;
  border-radius: 50%;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36rpx;
}

.follow-mark {
  position: absolute;
  bottom: 0;
  left: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42rpx;
  height: 42rpx;
  border: 4rpx solid #fff;
  border-radius: 50%;
  background: #f23b54;
  color: #fff;
  font-size: 36rpx;
  line-height: 36rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 100rpx;
  min-height: 86rpx;
  color: #fff;
  font-size: 25rpx;
  font-weight: 500;
  text-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.8);
}

.action-item text {
  margin-top: 7rpx;
}

/* rpx 会随屏幕宽度缩放；平板改用 px 上限，避免操作栏随屏幕宽度过度放大。 */
@media screen and (min-width: 768px) {
  .video-actions {
    right: 28px;
    gap: 20px;
  }

  .avatar-action {
    width: 58px;
    height: 70px;
  }

  .avatar {
    width: 54px;
    height: 54px;
  }

  .follow-mark {
    left: 14px;
    width: 26px;
    height: 26px;
    font-size: 22px;
    line-height: 22px;
  }

  .action-item {
    min-width: 60px;
    min-height: 56px;
    font-size: 14px;
  }
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

.comment-panel {
  position: absolute;
  bottom: 0;
  left: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 64vh;
  border-radius: 28rpx 28rpx 0 0;
  background: #fff;
  color: #333;
  animation: comment-slide-up 0.25s ease-out;
}

@keyframes comment-slide-up {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.comment-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 92rpx;
  flex-shrink: 0;
  border-bottom: 1rpx solid #eee;
}

.comment-panel-title {
  font-size: 30rpx;
  font-weight: 600;
}

.comment-panel-close, .comment-panel-placeholder {
  width: 92rpx;
  text-align: center;
}

.comment-list {
  flex: 1;
  min-height: 0;
  padding: 0 26rpx;
  box-sizing: border-box;
}

.comment-state {
  padding: 70rpx 0;
  color: #999;
  text-align: center;
  font-size: 26rpx;
}

.comment-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f1f1f1;
}

.comment-main {
  display: flex;
  align-items: flex-start;
}

.comment-avatar {
  width: 64rpx;
  height: 64rpx;
  flex-shrink: 0;
  border-radius: 50%;
  background: #e6e6e6;
}

.comment-avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #7ca2cd;
  font-size: 26rpx;
}

.comment-body {
  min-width: 0;
  flex: 1;
  margin-left: 18rpx;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 14rpx;
  min-height: 32rpx;
}

.comment-name {
  color: #5b7799;
  font-size: 26rpx;
  font-weight: 500;
}

.comment-time {
  color: #aaa;
  font-size: 22rpx;
}

.reply-to {
  margin-top: 7rpx;
  color: #8a8a8a;
  font-size: 23rpx;
}

.comment-content {
  margin-top: 8rpx;
  color: #222;
  font-size: 28rpx;
  line-height: 1.45;
  word-break: break-all;
}

.comment-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 8rpx;
}

.comment-voice {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 9rpx 16rpx;
  border-radius: 8rpx;
  background: #f1f3f5;
  color: #555;
  font-size: 25rpx;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 28rpx;
  margin-top: 14rpx;
  color: #777;
  font-size: 23rpx;
}

.comment-actions view {
  display: flex;
  align-items: center;
  gap: 5rpx;
}

.delete-comment {
  color: #e25454;
}

.child-entry {
  margin-top: 16rpx;
  color: #6687aa;
  font-size: 24rpx;
}

.child-comments {
  margin-top: 10rpx;
  padding: 0 16rpx;
  border-radius: 8rpx;
  background: #f8f8f8;
}

.child-comment {
  padding: 18rpx 0;
}

.child-avatar {
  width: 54rpx;
  height: 54rpx;
}

.load-comments {
  padding: 24rpx 0 38rpx;
  color: #999;
  text-align: center;
  font-size: 24rpx;
}

.comment-input-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 80rpx;
  margin: 14rpx 24rpx calc(14rpx + env(safe-area-inset-bottom));
  padding: 0 24rpx;
  border-radius: 40rpx;
  background: #f4f5f6;
  color: #999;
  font-size: 26rpx;
}

.comment-character-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  margin-left: 16rpx;
}

.comment-character-setting,
.comment-character-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48rpx;
  height: 48rpx;
}

.comment-character-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  overflow: hidden;
}

.comment-character-clear {
  margin-left: 8rpx;
}
</style>
