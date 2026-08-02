<template>
  <view class="short-video-my-page">
    <view class="profile-section">
      <view class="profile-main">
        <view class="back-button" @tap="goBack">
          <uni-icons type="back" size="22" color="#3d6474"/>
        </view>
        <view class="notify-button" @tap="goToNotify">
          <uni-icons type="chatbubble" size="22" color="#3d6474"/>
          <view v-if="shortVideoNotifyCount" class="notify-badge">{{ shortVideoNotifyCount > 99 ? '99+' : shortVideoNotifyCount }}</view>
        </view>
        <view class="avatar-ring">
          <image v-if="userInfo.headImage" class="avatar" :src="userInfo.headImage" mode="aspectFill"/>
          <view v-else class="avatar avatar-placeholder">{{ avatarInitial }}</view>
          <view class="avatar-add" @tap="goToVideoEdit">
            <uni-icons type="plus" size="17" color="#ffffff"/>
          </view>
        </view>

        <view class="profile-info">
          <view class="name-line">
            <text class="nickname">{{ userInfo.nickName || userInfo.userName || '未设置昵称' }}</text>
            <text v-if="isMale" class="iconfont icon-man sex-icon male"></text>
            <text v-else-if="isFemale" class="iconfont icon-girl sex-icon female"></text>
          </view>
          <text class="signature">{{ userInfo.signature || '这个人很懒，暂未填写签名' }}</text>
        </view>
      </view>

      <view class="statistics">
        <view class="stat-item">
          <text class="stat-count">{{ formatCount(likeCount) }}</text>
          <text class="stat-label">获赞</text>
        </view>
        <view class="stat-item stat-clickable" @tap="goToFollowFans">
          <text class="stat-count">{{ formatCount(followCount) }}</text>
          <text class="stat-label">关注</text>
        </view>
        <view class="stat-item stat-clickable" @tap="goToFollowFans">
          <text class="stat-count">{{ formatCount(fansCount) }}</text>
          <text class="stat-label">粉丝</text>
        </view>
      </view>
    </view>

    <view class="video-tabs">
      <view class="tab-item" :class="{ active: activeTab === 'works' }" @tap="switchTab('works')">
        <text>作品</text>
        <view class="tab-indicator"/>
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'favorite' }" @tap="switchTab('favorite')">
        <text>收藏</text>
        <view class="tab-indicator"/>
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'liked' }" @tap="switchTab('liked')">
        <text>喜欢</text>
        <view class="tab-indicator"/>
      </view>
    </view>

    <view v-if="loading" class="state-view">
      <uni-icons type="spinner-cycle" size="24" color="#7f8c9b"/>
      <text>加载中...</text>
    </view>
    <view v-else-if="!videoList.length" class="state-view empty-state">
      <uni-icons :type="activeTab === 'liked' ? 'heart' : 'videocam'" size="30" color="#b8c1ca"/>
      <text>{{ emptyText }}</text>
    </view>
    <view v-else class="video-grid">
      <view v-for="video in videoList" :key="video.id" class="video-card">
        <image v-if="video.coverUrl" class="video-cover" :src="video.coverUrl" mode="aspectFill"/>
        <view v-else class="video-cover cover-placeholder">
          <uni-icons type="videocam" size="30" color="rgba(255,255,255,0.75)"/>
        </view>
        <view class="video-shade"/>
        <view v-if="video.status === '1'" class="video-audit-status auditing">审核中</view>
        <view v-else-if="video.status === '3'" class="video-audit-status rejected">未通过审核</view>
        <view v-if="activeTab === 'works'" class="video-edit" @tap.stop="goToVideoEdit(video.id)">
          <uni-icons type="compose" size="18" color="#ffffff"/>
        </view>
        <view class="video-stat">
          <uni-icons
              :type="activeTab === 'works' ? 'videocam' : 'heart-filled'"
              size="16"
              :color="activeTab === 'liked' ? '#f0445d' : '#ffffff'"
          />
          <text>{{ activeTab === 'works' ? (video.playCount || 0) : (video.likeCount || 0) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: "short-video-my",
  data() {
    return {
      userInfo: {},
      followCount: 0,
      fansCount: 0,
      likeCount: 0,
      activeTab: 'works',
      videoList: [],
      loading: false,
      requestId: 0,
    }
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
    shortVideoNotifyCount() {
      return this.shortVideoStore.getShortVideoNotifyCount()
    },
    avatarInitial() {
      const name = this.userInfo.nickName || this.userInfo.userName || ''
      return name ? name.charAt(0).toUpperCase() : '我'
    },
    isMale() {
      return this.userInfo.sex === 0 || this.userInfo.sex === '0'
    },
    isFemale() {
      return this.userInfo.sex === 1 || this.userInfo.sex === '1'
    },
    emptyText() {
      const textMap = {
        works: '暂时还没有作品',
        favorite: '暂时还没有收藏的视频',
        liked: '暂时还没有喜欢的视频',
      }
      return textMap[this.activeTab]
    },
  },
  methods: {
    loadUserInfo() {
      this.$http({
        url: '/user/self',
        method: 'get'
      }).then((user) => {
        this.userInfo = user || {}
        this.followCount = this.userInfo.followCount || 0
        this.fansCount = this.userInfo.fansCount || 0
        this.likeCount = this.userInfo.shortVideoLikedCount || this.userInfo.likeCount || 0
      })
    },
    switchTab(tab) {
      if (this.activeTab === tab) return
      this.activeTab = tab
      this.videoList = []
      this.loadVideoList()
    },
    goToVideoEdit(videoId) {
      const query = videoId !== undefined && videoId !== null ? `?videoId=${encodeURIComponent(videoId)}` : ''
      uni.navigateTo({
        url: `/pages/short-video/short-video-edit${query}`
      })
    },
    goToFollowFans() {
      uni.navigateTo({
        url: '/pages/mine/mine-follow-fans'
      })
    },
    goToNotify() {
      uni.navigateTo({
        url: '/pages/short-video/short-video-notify'
      })
    },
    goBack() {
      uni.navigateBack({
        delta: 1
      })
    },
    formatCount(count) {
      const value = Number(count) || 0
      if (value >= 10000) return `${(value / 10000).toFixed(value >= 100000 ? 0 : 1)}w`
      return value
    },

    // 根据当前页签加载作品、收藏或喜欢的视频。
    loadVideoList() {
      const urlMap = {
        works: '/shortVideo/my',
        favorite: '/shortVideo/myFavorite',
        liked: '/shortVideo/myLiked',
      }
      const currentRequestId = ++this.requestId
      this.loading = true

      this.$http({
        url: urlMap[this.activeTab],
        method: 'post',
        data: this.activeTab === 'works' ? {} : undefined,
      }).then((data) => {
        if (currentRequestId === this.requestId) {
          this.videoList = data || []
        }
      }).catch(() => {
        if (currentRequestId === this.requestId) {
          this.videoList = []
        }
      }).finally(() => {
        if (currentRequestId === this.requestId) {
          this.loading = false
        }
      })
    },
  },
  onLoad() {
    this.loadUserInfo()
    this.loadVideoList()
  }
}
</script>

<style scoped lang="scss">
.short-video-my-page {
  min-height: 100vh;
  box-sizing: border-box;
  background: #ffffff;
  color: #202733;
}

.profile-section {
  padding: 0;
}

.profile-main {
  position: relative;
  display: flex;
  align-items: center;
  padding: 32rpx 36rpx;
  border-top: 1rpx solid #dceff3;
  border-bottom: 1rpx solid #dceff3;
  background: linear-gradient(135deg, #f0fbfa 0%, #edf6ff 52%, #f7f3ff 100%);
}

.back-button {
  position: absolute;
  top: 18rpx;
  right: 22rpx;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.65);
}

.notify-button {
  position: absolute;
  top: 18rpx;
  right: 92rpx;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.65);
}

.notify-badge {
  position: absolute;
  top: -8rpx;
  right: -14rpx;
  min-width: 28rpx;
  height: 28rpx;
  padding: 0 5rpx;
  border: 2rpx solid #ffffff;
  border-radius: 16rpx;
  box-sizing: border-box;
  background: #f0445d;
  color: #ffffff;
  font-size: 18rpx;
  line-height: 24rpx;
  text-align: center;
}

.avatar-ring {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 164rpx;
  height: 164rpx;
  padding: 6rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #23d6d2 0%, #168cf1 100%);
  box-shadow: 0 8rpx 20rpx rgba(21, 151, 224, 0.2);
}

.avatar {
  width: 164rpx;
  height: 164rpx;
  border: 4rpx solid #ffffff;
  border-radius: 50%;
  box-sizing: border-box;
  background: linear-gradient(135deg, #e5f7f4, #dbeafe 100%);
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #287995;
  font-size: 56rpx;
  font-weight: 600;
}

.avatar-add {
  position: absolute;
  right: -3rpx;
  bottom: 4rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46rpx;
  height: 46rpx;
  border: 4rpx solid #ffffff;
  border-radius: 50%;
  box-sizing: border-box;
  background: #22c55e;
}

.profile-info {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  padding-left: 28rpx;
}

.name-line {
  display: flex;
  align-items: center;
}

.nickname {
  overflow: hidden;
  max-width: 420rpx;
  font-size: 38rpx;
  font-weight: 600;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sex-icon {
  margin-left: 10rpx;
  font-size: 30rpx;
}

.male { color: #3b9df8; }
.female { color: #f05e91; }

.signature {
  overflow: hidden;
  margin-top: 12rpx;
  color: #7d8793;
  font-size: 26rpx;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.statistics {
  display: flex;
  margin-top: 0;
  padding: 30rpx 36rpx;
  background: #ffffff;
}

.stat-item {
  display: flex;
  min-width: 140rpx;
  flex-direction: column;
}

.stat-count {
  color: #1f2937;
  font-size: 32rpx;
  font-weight: 600;
}

.stat-label {
  margin-top: 7rpx;
  color: #8b96a3;
  font-size: 24rpx;
}

.video-tabs {
  display: flex;
  height: 92rpx;
  border-top: 1rpx solid #f3f5f7;
  border-bottom: 1rpx solid #edf0f2;
}

.tab-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  color: #8b96a3;
  font-size: 29rpx;
}

.tab-item.active {
  color: #1f2937;
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 38rpx;
  height: 5rpx;
  border-radius: 4rpx 4rpx 0 0;
  background: transparent;
}

.tab-item.active .tab-indicator {
  background: linear-gradient(90deg, #20c9c5, #238df2);
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rpx;
  padding: 2rpx 0 0;
}

.video-card {
  position: relative;
  height: 0;
  padding-bottom: 132%;
  overflow: hidden;
  background: #e7edf1;
}

.video-cover {
  position: absolute;
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #82b7cb, #667eea);
}

.video-shade {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 80rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.56));
}

.video-audit-status {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  z-index: 1;
  padding: 6rpx 12rpx;
  border-radius: 6rpx;
  color: #ffffff;
  font-size: 20rpx;
  line-height: 1.35;
}

.video-audit-status.auditing {
  background: rgba(245, 158, 11, 0.9);
}

.video-audit-status.rejected {
  background: rgba(239, 68, 68, 0.9);
}

.video-edit {
  position: absolute;
  right: 12rpx;
  bottom: 12rpx;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
}

.video-stat {
  position: absolute;
  bottom: 14rpx;
  left: 14rpx;
  display: flex;
  align-items: center;
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 500;
  text-shadow: 0 1rpx 3rpx rgba(0, 0, 0, 0.4);
}

.video-stat text {
  margin-left: 6rpx;
}

.state-view {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360rpx;
  flex-direction: column;
  color: #8b96a3;
  font-size: 26rpx;
}

.state-view text {
  margin-top: 16rpx;
}
</style>
