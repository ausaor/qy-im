<template>
  <div class="short-video-my">
    <!-- 用户信息区域 -->
    <div class="user-profile-section">
      <div class="profile-header">
        <div class="avatar-section">
          <head-image 
            :url="userInfo.headImage" 
            :name="userInfo.nickName" 
            :size="80"
            radius="50%"
          ></head-image>
        </div>
        <div class="user-basic-info">
          <div class="nick-name">{{ userInfo.nickName }}</div>
          <div class="signature" v-if="userInfo.signature">{{ userInfo.signature }}</div>
        </div>
      </div>
      
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-value">{{ followCount }}</div>
          <div class="stat-label">关注</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ fansCount }}</div>
          <div class="stat-label">粉丝</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ likeCount }}</div>
          <div class="stat-label">获赞</div>
        </div>
      </div>
    </div>

    <!-- Tab导航栏 -->
    <div class="tab-bar">
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'works' }"
        @click="activeTab = 'works'"
      >
        <span>作品</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'liked' }"
        @click="activeTab = 'liked'"
      >
        <span>喜欢</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'favorite' }"
        @click="activeTab = 'favorite'"
      >
        <span>收藏</span>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="video-list">
      <div v-loading="loading" class="video-grid">
        <div 
          v-for="video in videoList" 
          :key="video.id" 
          class="video-item"
          @click="handleVideoClick(video)"
        >
          <div class="video-cover">
            <img :src="video.coverUrl" alt="视频封面" />
            <div class="play-icon">
              <i class="el-icon-video-play"></i>
            </div>
            <div class="duration" v-if="video.duration">{{ formatDuration(video.duration) }}</div>
          </div>
          <div class="video-info">
            <div class="video-title" :title="video.title">{{ video.title || '未命名' }}</div>
            <div class="video-stats">
              <span><i class="el-icon-view"></i> {{ video.playCount || 0 }}</span>
              <span><i class="el-icon-star-on"></i> {{ video.likeCount || 0 }}</span>
            </div>
          </div>
        </div>
        
        <div v-if="!loading && videoList.length === 0" class="empty-state">
          <i class="el-icon-video-camera"></i>
          <p>暂无{{ getEmptyText() }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'

export default {
  name: 'ShortVideoMy',
  components: {
    HeadImage
  },
  data() {
    return {
      userInfo: {},
      activeTab: 'works',
      videoList: [],
      loading: false,
      followCount: 0,
      fansCount: 0,
      likeCount: 0
    }
  },
  created() {
    this.loadUserInfo()
    this.loadVideoList()
  },
  watch: {
    activeTab() {
      this.loadVideoList()
    }
  },
  methods: {
    // 加载用户信息
    loadUserInfo() {
      this.$http({
        url: '/user/self',
        method: 'get'
      }).then((user) => {
        this.userInfo = user
        // TODO: 这里需要调用关注、粉丝、获赞数量的接口
        // 暂时设置为0，后续需要根据实际接口调整
        this.followCount = 0
        this.fansCount = 0
        this.likeCount = 0
      })
    },
    
    // 加载视频列表
    loadVideoList() {
      this.loading = true
      let url = ''
      
      switch(this.activeTab) {
        case 'works':
          url = '/shortVideo/my'
          break
        case 'liked':
          url = '/shortVideo/myLiked'
          break
        case 'favorite':
          url = '/shortVideo/myFavorite'
          break
      }
      
      this.$http({
        url: url,
        method: 'post',
        data: {}
      }).then((data) => {
        this.videoList = data || []
      }).catch(() => {
        this.videoList = []
      }).finally(() => {
        this.loading = false
      })
    },
    
    // 格式化时长
    formatDuration(seconds) {
      if (!seconds) return '00:00'
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
    },
    
    // 获取空状态文本
    getEmptyText() {
      switch(this.activeTab) {
        case 'works': return '作品'
        case 'liked': return '喜欢的视频'
        case 'favorite': return '收藏的视频'
        default: return '内容'
      }
    },
    
    // 点击视频
    handleVideoClick(video) {
      // TODO: 实现视频播放逻辑
      console.log('点击视频:', video)
      this.$message.info('视频播放功能待实现')
    }
  }
}
</script>

<style scoped lang="scss">
.short-video-my {
  min-height: 100vh;
  background: #f5f7fa;
}

// 用户信息区域
.user-profile-section {
  background: white;
  padding: 24px 20px 16px;
  border-bottom: 1px solid #ebeef5;
  
  .profile-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 18px;
    
    .avatar-section {
      flex-shrink: 0;
    }
    
    .user-basic-info {
      flex: 1;
      overflow: hidden;
      
      .nick-name {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 6px;
      }
      
      .signature {
        font-size: 13px;
        color: #909399;
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
  
  .stats-row {
    display: flex;
    align-items: center;
    
    .stat-item {
      flex: 1;
      text-align: center;
      
      .stat-value {
        font-size: 20px;
        font-weight: 700;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 13px;
        color: #909399;
      }
    }
  }
}

// Tab导航栏
.tab-bar {
  display: flex;
  background: white;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 10;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 14px 0;
    cursor: pointer;
    font-size: 15px;
    color: #909399;
    font-weight: 500;
    position: relative;
    transition: color 0.2s ease;
    
    &:hover {
      color: #303133;
    }
    
    &.active {
      color: #303133;
      font-weight: 600;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 24px;
        height: 3px;
        background: #333;
        border-radius: 2px;
      }
    }
  }
}

// 视频列表
.video-list {
  padding: 12px;
  
  .video-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 2px;
    min-height: 300px;
  }
  
  .video-item {
    cursor: pointer;
    position: relative;
    
    .video-cover {
      position: relative;
      width: 100%;
      padding-top: 133.33%;
      overflow: hidden;
      background: #e8e8e8;
      
      img {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .play-icon {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 44px;
        height: 44px;
        background: rgba(0, 0, 0, 0.45);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.2s ease;
        
        i {
          font-size: 22px;
          color: white;
          margin-left: 2px;
        }
      }
      
      .duration {
        position: absolute;
        bottom: 6px;
        right: 6px;
        background: rgba(0, 0, 0, 0.6);
        color: white;
        padding: 1px 6px;
        border-radius: 3px;
        font-size: 11px;
      }
    }
    
    &:hover .play-icon {
      opacity: 1;
    }
    
    .video-info {
      padding: 8px 10px 10px;
      background: white;
      
      .video-title {
        font-size: 13px;
        color: #303133;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        line-height: 1.4;
      }
      
      .video-stats {
        display: flex;
        gap: 10px;
        font-size: 12px;
        color: #c0c4cc;
        
        span {
          display: flex;
          align-items: center;
          gap: 3px;
          
          i {
            font-size: 13px;
          }
        }
      }
    }
  }
  
  .empty-state {
    grid-column: 1 / -1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    color: #c0c4cc;
    
    i {
      font-size: 48px;
      margin-bottom: 12px;
      opacity: 0.4;
    }
    
    p {
      font-size: 14px;
      margin: 0;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .video-list .video-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 1px;
  }
}

@media (max-width: 480px) {
  .user-profile-section {
    padding: 18px 14px 12px;
    
    .profile-header {
      gap: 12px;
      margin-bottom: 14px;
      
      .nick-name {
        font-size: 18px;
      }
    }
    
    .stats-row .stat-item .stat-value {
      font-size: 18px;
    }
  }
  
  .tab-bar .tab-item {
    font-size: 14px;
    padding: 12px 0;
  }
}
</style>