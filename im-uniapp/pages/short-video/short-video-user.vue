<template>
  <view class="short-video-user-page">
    <view class="profile-section">
      <view class="back-button" @tap="goBack">
        <uni-icons type="back" size="22" color="#3d6474"/>
      </view>
      <view class="profile-main">
        <image v-if="targetInfo.headImage" class="avatar" :src="targetInfo.headImage" mode="aspectFill"/>
        <view v-else class="avatar avatar-placeholder">{{ avatarInitial }}</view>

        <view class="profile-info">
          <text class="nickname">{{ targetInfo.nickName || targetInfo.userName || '未知用户' }}</text>
          <view class="statistics">
            <view class="stat-item">
              <text class="stat-count">{{ formatCount(targetInfo.shortVideoLikedCount) }}</text>
              <text class="stat-label">获赞</text>
            </view>
            <view class="stat-item">
              <text class="stat-count">{{ formatCount(targetInfo.fansCount) }}</text>
              <text class="stat-label">粉丝</text>
            </view>
          </view>
        </view>

        <button
          v-if="!isMine"
          class="follow-button"
          :class="{ followed: isFollowed }"
          :disabled="followActioning"
          @tap="toggleFollow"
        >{{ isFollowed ? '已关注' : '关注' }}</button>
      </view>
    </view>

    <view class="works-header">
      <text class="works-title">作品</text>
      <text class="works-count">{{ total }}</text>
    </view>

    <view v-if="loading && !videoList.length" class="state-view">
      <uni-icons type="spinner-cycle" size="26" color="#7f8c9b"/>
      <text>加载中...</text>
    </view>
    <view v-else-if="!videoList.length" class="state-view empty-state">
      <uni-icons type="videocam" size="32" color="#b8c1ca"/>
      <text>暂时还没有作品</text>
    </view>
    <view v-else class="video-grid">
      <view v-for="video in videoList" :key="video.id" class="video-card">
        <image v-if="video.coverUrl" class="video-cover" :src="video.coverUrl" mode="aspectFill" lazy-load/>
        <view v-else class="video-cover cover-placeholder">
          <uni-icons type="videocam" size="30" color="rgba(255,255,255,0.75)"/>
        </view>
        <view class="video-shade"/>
        <view class="video-stat">
          <uni-icons type="heart-filled" size="16" color="#ffffff"/>
          <text>{{ formatCount(video.likeCount) }}</text>
        </view>
      </view>
    </view>

    <view v-if="loadingMore" class="load-more">
      <uni-icons type="spinner-cycle" size="20" color="#7f8c9b"/>
      <text>加载中...</text>
    </view>
    <view v-else-if="videoList.length && !hasMore" class="load-more">没有更多作品了</view>
  </view>
</template>

<script>
export default {
  name: 'short-video-user',
  data() {
    return {
      videoList: [],
      targetId: null,
      targetType: '',
      targetInfo: {},
      pageNo: 1,
      pageSize: 21,
      total: 0,
      loading: false,
      loadingMore: false,
      followActioning: false,
    }
  },
  computed: {
    mine() {
      return this.userStore.userInfo || {}
    },
    followKey() {
      return `${this.targetId}:${this.targetType}`
    },
    isFollowed() {
      return Boolean(this.targetId && this.targetType && this.followStore.isFollow(this.followKey))
    },
    isMine() {
      return this.targetType === 'user' && String(this.targetId) === String(this.mine.id)
    },
    hasMore() {
      return this.videoList.length < this.total
    },
    avatarInitial() {
      const name = this.targetInfo.nickName || this.targetInfo.userName || ''
      return name ? name.charAt(0).toUpperCase() : '?'
    },
  },
  methods: {
    getTargetInfo() {
      return this.$http({
        url: '/user/findTargetInfo',
        method: 'POST',
        data: { targetId: this.targetId, type: this.targetType },
      }).then((data) => {
        this.targetInfo = data || {}
      })
    },
    getTargetWorks() {
      if (this.loading || this.loadingMore || (!this.hasMore && this.videoList.length)) return
      const firstPage = !this.videoList.length
      if (firstPage) this.loading = true
      else this.loadingMore = true

      return this.$http({
        url: `/shortVideo/recommend?pageNo=${this.pageNo}&pageSize=${this.pageSize}`,
        method: 'POST',
        data: { objectId: this.targetId, type: this.targetType },
      }).then((page) => {
        const videos = page.data || []
        this.total = Number(page.total) || 0
        this.videoList.push(...videos)
        this.pageNo += 1
      }).finally(() => {
        this.loading = false
        this.loadingMore = false
      })
    },
    toggleFollow() {
      if (this.followActioning || this.isMine) return
      this.followActioning = true
      const follow = { targetId: this.targetId, type: this.targetType }
      const followed = this.isFollowed
      this.$http({
        url: followed ? `/follow/cancel?targetId=${this.targetId}&type=${this.targetType}` : '/follow/add',
        method: followed ? 'DELETE' : 'POST',
        data: followed ? {} : follow,
      }).then((savedFollow) => {
        if (followed) {
          this.followStore.removeFollow(follow)
          this.targetInfo.fansCount = Math.max(0, (Number(this.targetInfo.fansCount) || 0) - 1)
        } else {
          this.followStore.addFollow(savedFollow || follow)
          this.targetInfo.fansCount = (Number(this.targetInfo.fansCount) || 0) + 1
        }
        uni.showToast({ title: followed ? '已取消关注' : '关注成功', icon: 'none' })
      }).finally(() => {
        this.followActioning = false
      })
    },
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    formatCount(count) {
      const value = Number(count) || 0
      if (value >= 10000) return `${(value / 10000).toFixed(value >= 100000 ? 0 : 1)}w`
      return value
    },
  },
  onLoad(options) {
    this.targetId = options.targetId
    this.targetType = options.targetType || 'user'
    if (!this.targetId) {
      uni.showToast({ title: '缺少用户信息', icon: 'none' })
      return
    }
    this.getTargetInfo()
    this.getTargetWorks()
  },
  onReachBottom() {
    if (this.hasMore) this.getTargetWorks()
  },
}
</script>

<style scoped lang="scss">
.short-video-user-page {
  min-height: 100vh;
  padding-bottom: 36rpx;
  box-sizing: border-box;
  background: #ffffff;
  color: #202733;
}

.profile-section {
  position: relative;
  padding-top: 32rpx;
  background: linear-gradient(135deg, #f0fbfa 0%, #edf6ff 52%, #f7f3ff 100%);
}

.back-button {
  position: absolute;
  top: 18rpx;
  left: 22rpx;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.7);
}

.profile-main {
  display: flex;
  align-items: center;
  min-height: 206rpx;
  padding: 32rpx 36rpx;
  box-sizing: border-box;
}

.avatar {
  flex: none;
  width: 144rpx;
  height: 144rpx;
  border: 4rpx solid #ffffff;
  border-radius: 50%;
  box-sizing: border-box;
  background: #e5f7f4;
  box-shadow: 0 8rpx 20rpx rgba(21, 151, 224, 0.18);
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #287995;
  font-size: 52rpx;
  font-weight: 600;
}

.profile-info {
  min-width: 0;
  flex: 1;
  padding-left: 24rpx;
}

.nickname {
  display: block;
  overflow: hidden;
  color: #202733;
  font-size: 38rpx;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.statistics {
  display: flex;
  margin-top: 18rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  margin-right: 34rpx;
}

.stat-count { font-size: 28rpx; font-weight: 600; }
.stat-label { margin-top: 5rpx; color: #7d8793; font-size: 23rpx; }

.follow-button {
  flex: none;
  min-width: 124rpx;
  height: 58rpx;
  margin: 0 0 0 16rpx;
  padding: 0 18rpx;
  border: 1rpx solid #3d6474;
  border-radius: 30rpx;
  background: #3d6474;
  color: #ffffff;
  font-size: 25rpx;
  line-height: 56rpx;

  &::after { border: 0; }
  &.followed { border-color: #d8dee4; background: #ffffff; color: #657383; }
  &[disabled] { opacity: 0.6; }
}

.works-header {
  display: flex;
  align-items: baseline;
  padding: 30rpx 30rpx 22rpx;
  border-bottom: 1rpx solid #edf0f2;
}

.works-title { font-size: 31rpx; font-weight: 600; }
.works-count { margin-left: 10rpx; color: #98a2ad; font-size: 24rpx; }

.video-grid { display: flex; flex-wrap: wrap; }

.video-card {
  position: relative;
  width: 33.3333%;
  height: 300rpx;
  overflow: hidden;
  border-right: 2rpx solid #ffffff;
  border-bottom: 2rpx solid #ffffff;
  box-sizing: border-box;
  background: #e9edf1;
}

.video-cover { width: 100%; height: 100%; }
.cover-placeholder { display: flex; align-items: center; justify-content: center; }

.video-shade {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 80rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.56));
}

.video-stat {
  position: absolute;
  right: 12rpx;
  bottom: 10rpx;
  display: flex;
  align-items: center;
  color: #ffffff;
  font-size: 22rpx;

  text { margin-left: 5rpx; }
}

.state-view {
  display: flex;
  min-height: 360rpx;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #7f8c9b;
  font-size: 26rpx;

  text { margin-top: 16rpx; }
}

.load-more {
  padding: 26rpx 0;
  color: #98a2ad;
  font-size: 24rpx;
  text-align: center;
}
</style>
