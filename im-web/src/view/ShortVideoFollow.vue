<template>
  <div class="follow-page">
    <!-- 左侧关注列表 -->
    <div class="follow-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <span class="header-title" v-if="!sidebarCollapsed">我的关注</span>
        <span class="follow-count" v-if="!sidebarCollapsed">({{ followList.length }})</span>
        <div class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed" :title="sidebarCollapsed ? '展开' : '收起'">
          <i :class="sidebarCollapsed ? 'el-icon-d-arrow-right' : 'el-icon-d-arrow-left'"></i>
        </div>
      </div>

      <div class="search-box" v-if="!sidebarCollapsed">
        <el-input
          v-model="searchText"
          placeholder="搜索关注"
          prefix-icon="el-icon-search"
          clearable
          size="small"
        ></el-input>
      </div>

      <div class="follow-list">
        <short-video-follow-item
          v-for="item in filteredFollowList"
          :key="item.id"
          :item="item"
          :collapsed="sidebarCollapsed"
          :active="isActiveFollow(item)"
          @select="handleFollowClick"
        ></short-video-follow-item>

        <div v-if="filteredFollowList.length === 0" class="empty-list">
          <i class="el-icon-star-off"></i>
          <p>{{ searchText ? '无匹配结果' : '暂无关注' }}</p>
          <p v-if="!searchText" class="empty-hint">在短视频播放页面点击 + 即可关注</p>
        </div>
      </div>
    </div>

    <!-- 右侧短视频播放区 -->
    <div class="follow-video">
      <short-video-play
        ref="videoPlayer"
        :type="currentFollow.type"
        :objectId="currentFollow.objectId"
        :section="'follow'"
        :key="followVideoKey"
      ></short-video-play>
      <div v-if="!currentFollow.objectId" class="no-selection-hint">
        <i class="el-icon-arrow-left"></i>
        <span>点击左侧关注对象查看相关视频</span>
      </div>
    </div>
  </div>
</template>

<script>
import ShortVideoPlay from '@/components/shortVideo/ShortVideoPlay.vue'
import ShortVideoFollowItem from '@/components/shortVideo/ShortVideoFollowItem.vue'
import { mapState } from 'vuex'

export default {
  name: 'ShortVideoFollow',
  components: {
    ShortVideoPlay,
    ShortVideoFollowItem
  },
  data() {
    return {
      currentFollow: {},
      searchText: '',
      sidebarCollapsed: false
    }
  },
  computed: {
    ...mapState({
      followList: state => state.followStore.follows
    }),
    followVideoKey() {
      return `${this.currentFollow.type || ''}_${this.currentFollow.objectId || ''}`
    },
    filteredFollowList() {
      if (!this.searchText) {
        return this.followList
      }
      const keyword = this.searchText.toLowerCase()
      return this.followList.filter(item =>
        item.targetName && item.targetName.toLowerCase().includes(keyword)
      )
    }
  },
  watch: {
    followVideoKey(newVal, oldVal) {
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
      // 确保关注列表已加载后再选中第一个关注对象
      if (this.followList.length === 0) {
        this.$store.dispatch('loadFollow').then(() => {
          this.selectFirstFollow()
        })
      } else {
        this.selectFirstFollow()
      }
    },
    selectFirstFollow() {
      if (this.followList.length > 0 && !this.currentFollow.objectId) {
        this.handleFollowClick(this.followList[0])
      }
    },
    handleFollowClick(item) {
      this.currentFollow = {
        type: item.type,
        objectId: item.targetId,
        targetName: item.targetName,
        targetAvatar: item.targetAvatar
      }
    },
    isActiveFollow(item) {
      return this.currentFollow.objectId === item.targetId &&
             this.currentFollow.type === item.type
    }
  }
}
</script>

<style scoped lang="scss">
.follow-page {
  display: flex;
  height: 100%;
  width: 100%;
  background: #f5f5f5;
}

// 左侧关注列表
.follow-sidebar {
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

  .follow-count {
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

.follow-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px 0;

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

    .empty-hint {
      font-size: 12px;
      color: #bbb;
      margin-top: 6px;
      text-align: center;
    }
  }
}

// 右侧视频区
.follow-video {
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
