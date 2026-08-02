<template>
  <view class="page mine-follow-fans">
    <nav-bar back>关注与粉丝</nav-bar>

    <view class="content">
      <view class="tabs">
        <view
          class="tab-item"
          :class="{ active: activeTab === 'follow' }"
          @tap="switchTab('follow')"
        >
          <text>关注</text>
          <view class="tab-indicator" />
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'fans' }"
          @tap="switchTab('fans')"
        >
          <text>粉丝</text>
          <view class="tab-indicator" />
        </view>
      </view>

      <view v-if="loading" class="state-view">
        <uni-icons type="spinner-cycle" size="24" color="#7f8c9b" />
        <text>加载中...</text>
      </view>

      <template v-else-if="activeTab === 'follow'">
        <view v-if="followList.length" class="follow-list">
          <view v-for="item in followList" :key="item.id" class="follow-item">
            <head-image :id="item.type === 'user' ? item.targetId : undefined" :url="item.targetAvatar" :name="item.targetName" size="mini" />
            <view class="item-info">
              <view class="item-name">
                <text class="type-star" :style="{ color: getStarColor(item.type) }">★</text>
                <text class="name-text">{{ item.targetName || '未知目标' }}</text>
              </view>
              <text class="item-type">{{ getTypeName(item.type) }}</text>
            </view>
            <button
              class="follow-button"
              :class="{ followed: item.followed !== false }"
              :disabled="isActioning(item, 'follow')"
              @tap.stop="toggleFollow(item, 'follow')"
            >{{ item.followed !== false ? '已关注' : '关注' }}</button>
          </view>
        </view>
        <view v-else class="state-view empty-state">
          <uni-icons type="star" size="30" color="#b8c1ca" />
          <text>暂无关注</text>
        </view>
      </template>

      <template v-else>
        <view v-if="fansList.length" class="follow-list">
          <view v-for="item in fansList" :key="item.id" class="follow-item">
            <head-image :id="item.userId" :url="item.headImage" :name="item.nickName" size="mini" />
            <view class="item-info">
              <text class="item-name name-text">{{ item.nickName || '未知用户' }}</text>
            </view>
            <button
              class="follow-button"
              :class="{ followed: item.followed }"
              :disabled="isActioning(item, 'fans')"
              @tap.stop="toggleFollow(item, 'fans')"
            >{{ item.followed ? '已关注' : '回关' }}</button>
          </view>
        </view>
        <view v-else class="state-view empty-state">
          <uni-icons type="person" size="30" color="#b8c1ca" />
          <text>暂无粉丝</text>
        </view>
      </template>
    </view>
  </view>
</template>

<script>
import NavBar from "../../components/nav-bar/nav-bar.vue";
import HeadImage from "../../components/head-image/head-image.vue";

export default {
  name: "mine-follow-fans",
  components: {HeadImage, NavBar},
  data() {
    return {
      activeTab: 'follow',
      followList: [],
      fansList: [],
      loading: false,
      requestId: 0,
      actioningKeys: {},
    }
  },
  methods: {
    switchTab(tab) {
      if (this.activeTab === tab) return
      this.activeTab = tab
      this.loadList()
    },
    loadList() {
      const requestId = ++this.requestId
      const isFollowTab = this.activeTab === 'follow'
      this.loading = true
      this.$http({
        url: isFollowTab ? '/follow/list' : '/follow/myFans',
        method: 'GET'
      }).then((data) => {
        if (requestId === this.requestId) {
          if (isFollowTab) this.followList = data || []
          else this.fansList = data || []
        }
      }).catch(() => {
        if (requestId === this.requestId) {
          if (isFollowTab) this.followList = []
          else this.fansList = []
        }
      }).finally(() => {
        if (requestId === this.requestId) this.loading = false
      })
    },
    toggleFollow(item, source) {
      const follow = source === 'follow'
        ? {targetId: item.targetId, type: item.type}
        : {targetId: item.userId, type: 'user'}
      const followed = source === 'follow' ? item.followed !== false : item.followed
      const actionKey = `${source}:${follow.targetId}:${follow.type}`
      if (!follow.targetId || this.actioningKeys[actionKey]) return

      this.actioningKeys[actionKey] = true
      this.$http({
        url: followed ? `/follow/cancel?targetId=${follow.targetId}&type=${follow.type}` : '/follow/add',
        method: followed ? 'DELETE' : 'POST',
        data: followed ? {} : follow
      }).then((savedFollow) => {
        item.followed = !followed
        if (followed) this.followStore.removeFollow(follow)
        else this.followStore.addFollow(savedFollow || follow)
        uni.showToast({
          title: followed ? '已取消关注' : '关注成功',
          icon: 'none'
        })
      }).finally(() => {
        delete this.actioningKeys[actionKey]
      })
    },
    isActioning(item, source) {
      const targetId = source === 'follow' ? item.targetId : item.userId
      const type = source === 'follow' ? item.type : 'user'
      return Boolean(this.actioningKeys[`${source}:${targetId}:${type}`])
    },
    getStarColor(type) {
      const colorMap = {
        user: '#409EFF',
        group: '#67C23A',
        character: '#E6A23C',
        template: '#9B59B6'
      }
      return colorMap[type] || '#999999'
    },
    getTypeName(type) {
      const nameMap = {
        user: '用户',
        group: '群组',
        character: '角色',
        template: '群聊模板'
      }
      return nameMap[type] || type || '未知类型'
    }
  },
  onLoad(options) {
    if (['follow', 'fans'].includes(options.tab)) {
      this.activeTab = options.tab
    }
    this.loadList()
  }
}
</script>

<style scoped lang="scss">
.mine-follow-fans {
  min-height: 100vh;
  background: #f7f8fa;
}

.content {

}

.tabs {
  display: flex;
  height: 92rpx;
  background: #ffffff;
  border-bottom: 1rpx solid #edf0f2;
}

.tab-item {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7f8c9b;
  font-size: 30rpx;

  &.active {
    color: #202733;
    font-weight: 600;

    .tab-indicator {
      position: absolute;
      bottom: 0;
      width: 48rpx;
      height: 6rpx;
      border-radius: 4rpx;
      background: #3d6474;
    }
  }
}

.follow-list {
  background: #ffffff;
}

.follow-item {
  display: flex;
  align-items: center;
  min-height: 128rpx;
  padding: 18rpx 30rpx;
  box-sizing: border-box;
  border-bottom: 1rpx solid #f0f2f4;
}

.item-info {
  flex: 1;
  min-width: 0;
  margin-left: 22rpx;
}

.item-name {
  display: flex;
  align-items: center;
  min-width: 0;
  color: #202733;
  font-size: 30rpx;
}

.name-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-star {
  flex: none;
  margin-right: 8rpx;
  font-size: 28rpx;
}

.item-type {
  display: block;
  margin-top: 6rpx;
  color: #98a2ad;
  font-size: 24rpx;
}

.follow-button {
  flex: none;
  min-width: 124rpx;
  height: 58rpx;
  margin: 0;
  padding: 0 18rpx;
  border: 1rpx solid #3d6474;
  border-radius: 30rpx;
  background: #3d6474;
  color: #ffffff;
  font-size: 25rpx;
  line-height: 56rpx;

  &::after {
    border: 0;
  }

  &.followed {
    border-color: #d8dee4;
    background: #ffffff;
    color: #657383;
  }

  &[disabled] {
    opacity: 0.6;
  }
}

.state-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  min-height: 280rpx;
  color: #7f8c9b;
  font-size: 27rpx;
}

.empty-state {
  color: #aeb7c1;
}

</style>
