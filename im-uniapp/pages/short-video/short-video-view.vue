<template>
  <view class="short-video-page">
    <swiper v-if="videoList.length" class="video-swiper" :class="{ 'comment-open': showCommentPanel }" vertical
            :current="renderCurrentIndex" :duration="300" @change="onVideoChange">
      <swiper-item v-for="({ video, index }) in renderedVideos" :key="video.id" class="video-slide">
        <view class="video-slide-inner">
          <video :id="videoElementId(index)" class="video-player" :src="video.videoUrl" :poster="video.coverUrl"
                 :autoplay="index === currentIndex" :show-center-play-btn="false" :controls="false" :loop="true"
                 object-fit="contain" :enable-progress-gesture="false" @click="togglePlay(index)"/>
          <image v-if="!video.videoUrl && video.coverUrl" class="video-cover" :src="video.coverUrl" mode="aspectFit"/>
          <view v-if="index === currentIndex && !isPlaying" class="play-mask" @click="togglePlay(index)">
            <uni-icons type="videocam" size="48" color="#ffffff"/>
          </view>

          <view class="video-info">
            <text class="author-name" @click="toViewUser(video.userId)">@{{ video.nickName  || ('用户' + video.userId) }}</text>
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
            <view v-if="isVideoOwner(video)" class="action-item more-action" @click="openMoreActions(video)">
              <uni-icons type="more-filled" size="32" color="#ffffff"/>
            </view>
          </view>
        </view>
      </swiper-item>
    </swiper>
    <view v-else class="state-view">
      <text>暂无视频</text>
    </view>
    <view v-show="!showCommentPanel" class="top-bar">
      <view class="back-button" @click="goBack">
        <uni-icons type="back" size="25" color="#ffffff"/>
      </view>
    </view>
    <short-video-comment-panel :visible="showCommentPanel" :video="currentVideo" @close="closeComments"
                               @comment-count-change="changeCurrentVideoCommentCount"/>
    <uni-popup ref="morePopup" type="bottom" :safe-area="false" @maskClick="closeMoreActions">
      <view class="more-actions-popup">
        <view class="more-actions-close" @click="closeMoreActions">
          <uni-icons type="closeempty" size="24" color="#687385"/>
        </view>
        <view class="more-action-buttons">
          <view class="more-action-item" @click="editCurrentVideo">
            <view class="more-action-button">
              <uni-icons type="compose" size="28" color="#202733"/>
            </view>
            <text>编辑</text>
          </view>
          <view class="more-action-item delete-action" @click="confirmDeleteCurrentVideo">
            <view class="more-action-button">
              <uni-icons type="trash" size="28" color="#f23b54"/>
            </view>
            <text>删除</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import ShortVideoCommentPanel from '../../components/short-video-comment-panel/short-video-comment-panel.vue'

export default {
  name: 'short-video-view',
  components: {ShortVideoCommentPanel},
  data() {
    return {
      currentIndex: 0, isPlaying: true, actioning: false, deleting: false, pendingPlayVideoIds: [], showCommentPanel: false,
      initialVideoId: '', avatarColors: ['#5daa31', '#c7515a', '#e03697', '#85029b', '#c9b455', '#326eb6'],
    }
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
    videoList() {
      return this.shortVideoStore.objectShortVideos || []
    },
    // 只保留当前视频及相邻视频的节点，避免同时加载完整列表的视频资源。
    renderedVideos() {
      const start = Math.max(0, this.currentIndex - 1)
      const end = Math.min(this.videoList.length, this.currentIndex + 2)
      return this.videoList.slice(start, end).map((video, offset) => ({
        video,
        index: start + offset
      }))
    },
    // Swiper 下标基于局部渲染窗口，currentIndex 保持为完整视频列表下标。
    renderCurrentIndex() {
      return this.currentIndex - Math.max(0, this.currentIndex - 1)
    },
    currentVideo() {
      return this.videoList[this.currentIndex] || {}
    },
  },
  watch: {
    videoList: {
      handler(videos) {
        if (!videos.length) return
        const index = videos.findIndex(video => String(video.id) === String(this.initialVideoId))
        this.currentIndex = index >= 0 ? index : 0
        this.playCurrentVideo()
      },
    },
  },
  onLoad(options) {
    this.initialVideoId = options.videoId || '';
    this.locateInitialVideo()
  },
  beforeUnmount() {
    this.currentVideoContext().pause()
  },
  methods: {
    formatCreateDate(createTime) {
      return String(createTime).slice(0, 10)
    },
    locateInitialVideo() {
      const index = this.videoList.findIndex(video => String(video.id) === String(this.initialVideoId))
      this.currentIndex = index >= 0 ? index : 0
      if (this.videoList.length) this.playCurrentVideo()
    },
    videoElementId(index) {
      return `short-video-view-${index}`
    },
    currentVideoContext() {
      return uni.createVideoContext(this.videoElementId(this.currentIndex), this)
    },
    playCurrentVideo() {
      this.$nextTick(() => {
        this.currentVideoContext().play();
        this.isPlaying = true;
        this.recordPlayCount()
      })
    },
    recordPlayCount() {
      const video = this.currentVideo
      if (!video || !video.id || this.pendingPlayVideoIds.includes(video.id) || this.shortVideoStore.hasPlayed(video.id)) return
      this.pendingPlayVideoIds.push(video.id)
      this.$http({
        url: `/shortVideo/addPlayCount/${video.id}`,
        method: 'POST'
      }).then(() => this.shortVideoStore.markPlayed(video.id)).finally(() => {
        const index = this.pendingPlayVideoIds.indexOf(video.id)
        if (index !== -1) this.pendingPlayVideoIds.splice(index, 1)
      })
    },
    onVideoChange(event) {
      const renderedVideo = this.renderedVideos[event.detail.current]
      if (!renderedVideo) return
      const nextIndex = renderedVideo.index
      if (nextIndex === this.currentIndex) return
      this.currentVideoContext().pause();
      this.currentIndex = nextIndex;
      this.playCurrentVideo()
    },
    togglePlay(index) {
      if (index !== this.currentIndex) return
      const context = this.currentVideoContext()
      if (this.isPlaying) context.pause()
      else {
        context.play();
        this.recordPlayCount()
      }
      this.isPlaying = !this.isPlaying
    },
    toggleLike(video) {
      this.toggleVideoAction(video, 'liked', 'likeCount', '/shortVideoLike/add', '/shortVideoLike/delete')
    },
    toggleFavorite(video) {
      this.toggleVideoAction(video, 'favorited', 'favoriteCount', '/shortVideoFavorite/add', '/shortVideoFavorite/delete')
    },
    isVideoOwner(video) {
      return Boolean(video && video.userId != null && this.mine && this.mine.id != null
        && String(video.userId) === String(this.mine.id))
    },
    openMoreActions(video) {
      if (!this.isVideoOwner(video)) return
      this.$refs.morePopup.open()
    },
    closeMoreActions() {
      this.$refs.morePopup.close()
    },
    editCurrentVideo() {
      const video = this.currentVideo
      if (!this.isVideoOwner(video)) return
      this.closeMoreActions()
      uni.navigateTo({url: `/pages/short-video/short-video-edit?videoId=${encodeURIComponent(video.id)}`})
    },
    confirmDeleteCurrentVideo() {
      const video = this.currentVideo
      if (!this.isVideoOwner(video) || this.deleting) return
      this.closeMoreActions()
      uni.showModal({
        title: '删除作品',
        content: '确定删除这个作品吗？删除后不可恢复。',
        confirmText: '删除',
        confirmColor: '#f23b54',
        success: ({confirm}) => {
          if (!confirm) return
          this.deleteCurrentVideo(video)
        },
      })
    },
    deleteCurrentVideo(video) {
      if (!video || !video.id || this.deleting) return
      this.deleting = true
      uni.showLoading({title: '删除中...', mask: true})
      this.$http({url: '/shortVideo/delete', method: 'DELETE', data: {id: video.id}}).then(() => {
        const index = this.videoList.findIndex(item => String(item.id) === String(video.id))
        if (index !== -1) this.videoList.splice(index, 1)
        uni.showToast({title: '删除成功', icon: 'success'})
        setTimeout(() => uni.navigateBack(), 500)
      }).finally(() => {
        this.deleting = false
        uni.hideLoading()
      })
    },
    toggleVideoAction(video, field, countField, addUrl, deleteUrl) {
      if (!video || !video.id || this.actioning) return
      this.actioning = true
      const active = video[field]
      this.$http({
        url: active ? deleteUrl : addUrl,
        method: active ? 'DELETE' : 'POST',
        data: {videoId: video.id}
      }).then(() => {
        video[field] = !active
        video[countField] = active ? Math.max(0, (video[countField] || 0) - 1) : (video[countField] || 0) + 1
      }).finally(() => {
        this.actioning = false
      })
    },
    openComments(video) {
      if (video && video.id) this.showCommentPanel = true
    },
    closeComments() {
      this.showCommentPanel = false
    },
    changeCurrentVideoCommentCount(delta) {
      if (this.currentVideo.id) this.currentVideo.commentCount = Math.max(0, (this.currentVideo.commentCount || 0) + delta)
    },
    isFollowed(video) {
      return Boolean(video && video.objectId && video.type && this.followStore.isFollow(`${video.objectId}:${video.type}`))
    },
    toggleFollow(video) {
      if (!video || !video.objectId || !video.type || this.actioning) return
      this.actioning = true
      const follow = {targetId: video.objectId, type: video.type}, followed = this.isFollowed(video)
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
      uni.navigateTo({url: `/pages/short-video/short-video-user?targetId=${encodeURIComponent(video.objectId)}&targetType=${encodeURIComponent(video.type)}`})
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
      let hash = 0;
      for (let index = 0; index < name.length; index += 1) hash += name.charCodeAt(index)
      return {backgroundColor: this.avatarColors[hash % this.avatarColors.length]}
    },
    goBack() {
      uni.navigateBack()
    },
  },
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

.video-swiper, .video-slide, .video-slide-inner {
  width: 100%;
  height: 100%;
}

.video-swiper.comment-open {
  height: 36vh;
}

.video-slide-inner {
  position: relative;
  background: #000;
}

.video-player, .video-cover {
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
  background: rgba(0, 0, 0, .18);
}

.top-bar {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 5;
  box-sizing: content-box;
  width: 100%;
  height: 130rpx;
  padding-top: env(safe-area-inset-top);
  background: linear-gradient(180deg, rgba(0, 0, 0, .45), transparent);
}

.back-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 92rpx;
  height: 100%;
}

.video-info {
  position: absolute;
  right: 140rpx;
  bottom: 8%;
  left: 30rpx;
  display: flex;
  flex-direction: column;
  color: #fff;
  text-shadow: 0 1rpx 4rpx rgba(0, 0, 0, .8);
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
  color: rgba(255, 255, 255, .82);
  font-size: 22rpx;
}

.video-title, .video-description {
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
  text-shadow: 0 1rpx 4rpx rgba(0, 0, 0, .8);
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

.more-action {
  margin-top: 4rpx;
}

.more-actions-popup {
  position: relative;
  overflow: hidden;
  padding: 48rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
  background: #fff;
}

.more-actions-close {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
}

.more-action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 48rpx;
}

.more-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  color: #202733;
  font-size: 26rpx;
}

.more-action-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: #f1f3f6;
  color: #202733;
}

.delete-action .more-action-button {
  background: #fff0f2;
}

.delete-action {
  color: #f23b54;
}

.state-view {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #fff;
  font-size: 28rpx;
}

.comment-open .video-info {
  right: 125rpx;
  bottom: 24rpx;
}

.comment-open .video-actions {
  right: 14rpx;
  bottom: 90rpx;
  gap: 12rpx;
  transform: scale(.82);
  transform-origin: right bottom;
}
</style>
