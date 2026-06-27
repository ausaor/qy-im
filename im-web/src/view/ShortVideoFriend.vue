<template>
  <div class="friend-page">
    <!-- 左侧好友列表 -->
    <div class="friend-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <span class="header-title" v-if="!sidebarCollapsed">好友列表</span>
        <span class="friend-count" v-if="!sidebarCollapsed">({{ activeFriends.length }})</span>
        <div class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed" :title="sidebarCollapsed ? '展开' : '收起'">
          <i :class="sidebarCollapsed ? 'el-icon-d-arrow-right' : 'el-icon-d-arrow-left'"></i>
        </div>
      </div>

      <div class="search-box" v-if="!sidebarCollapsed">
        <el-input
          v-model="searchText"
          placeholder="搜索好友"
          prefix-icon="el-icon-search"
          clearable
          size="small"
        ></el-input>
      </div>

      <div class="friend-list">
        <div
          v-for="item in filteredFriends"
          :key="item.id"
          class="friend-item"
          :class="{ active: currentFriend.id === item.id }"
          @click="handleFriendClick(item)"
        >
          <div class="friend-avatar">
            <head-image
              :url="item.headImage"
              :name="item.nickName"
              :size="sidebarCollapsed ? 42 : 36"
              :online="item.online"
            ></head-image>
          </div>
          <div class="friend-info" v-if="!sidebarCollapsed">
            <div class="friend-name">
              {{ item.friendRemark || item.nickName }}
            </div>
            <div class="friend-signature" v-if="item.signature">{{ item.signature }}</div>
          </div>
        </div>

        <div v-if="filteredFriends.length === 0" class="empty-list">
          <i class="el-icon-user"></i>
          <p>{{ searchText ? '无匹配好友' : '暂无好友' }}</p>
        </div>
      </div>
    </div>

    <!-- 右侧短视频播放区 -->
    <div class="friend-video">
      <short-video-play
        ref="videoPlayer"
        :userId="currentFriend.id"
        :section="'friend'"
        :key="friendVideoKey"
      ></short-video-play>
      <div v-if="!currentFriend.id" class="no-selection-hint">
        <i class="el-icon-arrow-left"></i>
        <span>点击左侧好友查看其视频</span>
      </div>
    </div>
  </div>
</template>

<script>
import ShortVideoPlay from '@/components/shortVideo/ShortVideoPlay.vue'
import HeadImage from '@/components/common/HeadImage.vue'
import { mapState } from 'vuex'

export default {
  name: 'ShortVideoFriend',
  components: {
    ShortVideoPlay,
    HeadImage
  },
  data() {
    return {
      currentFriend: {},
      searchText: '',
      sidebarCollapsed: false
    }
  },
  computed: {
    ...mapState({
      storeFriends: state => state.friendStore.friends
    }),
    friendVideoKey() {
      return 'friend_' + (this.currentFriend.id || 'none')
    },
    activeFriends() {
      return this.storeFriends.filter(f => !f.deleted)
    },
    filteredFriends() {
      if (!this.searchText) {
        return this.activeFriends
      }
      const keyword = this.searchText.toLowerCase()
      return this.activeFriends.filter(item => {
        const name = (item.friendRemark || item.nickName || '').toLowerCase()
        return name.includes(keyword)
      })
    }
  },
  watch: {
    friendVideoKey(newVal, oldVal) {
      if (newVal && newVal !== oldVal) {
        this.$nextTick(() => {
          if (this.$refs.videoPlayer) {
            this.$refs.videoPlayer.fetchVideos()
          }
        })
      }
    }
  },
  created() {
    this.initPage()
  },
  methods: {
    initPage() {
      // 确保好友列表已加载
      if (this.activeFriends.length === 0) {
        this.$store.dispatch('loadFriend').then(() => {
          this.selectFirstFriend()
        })
      } else {
        this.selectFirstFriend()
      }
    },
    selectFirstFriend() {
      if (this.activeFriends.length > 0 && !this.currentFriend.id) {
        this.handleFriendClick(this.activeFriends[0])
      }
    },
    handleFriendClick(item) {
      this.currentFriend = { ...item }
    }
  }
}

</script>


<style scoped lang="scss">
.friend-page {
  display: flex;
  height: 100%;
  width: 100%;
  background: #f5f5f5;
}

// 左侧好友列表
.friend-sidebar {
  width: 280px;
  min-width: 280px;
  height: 100%;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease, min-width 0.25s ease;
  overflow: hidden;

  &.collapsed {
    width: 68px;
    min-width: 68px;
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  gap: 6px;

  .header-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .friend-count {
    font-size: 13px;
    color: #999;
  }

  .collapse-btn {
    margin-left: auto;
    width: 28px;
    height: 28px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #666;
    transition: background 0.2s;
    flex-shrink: 0;

    &:hover {
      background: #f0f0f0;
      color: #333;
    }

    i {
      font-size: 14px;
    }
  }
}

.search-box {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;

  ::v-deep .el-input__inner {
    border-radius: 18px;
  }
}

.friend-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px 0;

  .friend-item {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    cursor: pointer;
    transition: background 0.15s;
    gap: 10px;

    &:hover {
      background: #f5f7fa;
    }

    &.active {
      background: #ecf5ff;

      .friend-name {
        color: #409EFF;
      }
    }
  }

  .friend-avatar {
    flex-shrink: 0;
  }

  .friend-info {
    flex: 1;
    min-width: 0;
    overflow: hidden;
  }

  .friend-name {
    font-size: 14px;
    color: #303133;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .friend-signature {
    font-size: 12px;
    color: #999;
    margin-top: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .empty-list {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: #999;

    i {
      font-size: 36px;
      margin-bottom: 10px;
      color: #ddd;
    }

    p {
      font-size: 13px;
      margin: 0;
    }
  }
}

// 右侧视频区
.friend-video {
  flex: 1;
  height: 100%;
  position: relative;
  background: #000;

  .no-selection-hint {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: rgba(255, 255, 255, 0.5);
    font-size: 15px;
    display: flex;
    align-items: center;
    gap: 8px;
    z-index: 1;

    i {
      font-size: 20px;
    }
  }
}
</style>