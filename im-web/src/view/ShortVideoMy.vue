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
        <div class="publish-section">
          <el-button
            type="primary"
            icon="el-icon-edit"
            size="small"
            @click="handlePublish"
          >发布</el-button>
        </div>
      </div>
      
      <div class="stats-row">
        <div class="stat-item stat-clickable" @click="openFollowDialog('follow')">
          <div class="stat-value">{{ followCount }}</div>
          <div class="stat-label">关注</div>
        </div>
        <div class="stat-item stat-clickable" @click="openFollowDialog('fans')">
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
      <div class="batch-btn" @click="toggleBatchMode">
        <span>{{ batchMode ? '退出管理' : '批量管理' }}</span>
      </div>
    </div>

    <!-- 批量管理操作栏 -->
    <div class="batch-action-bar" v-if="batchMode">
      <div class="batch-select-all">
        <el-checkbox :value="isAllSelected" :indeterminate="isIndeterminate" @change="handleSelectAll" />
        <span>全选</span>
      </div>
      <div class="batch-selected-count">已选{{ selectedVideos.length }}个{{ getBatchCountText() }}</div>
      <div class="batch-actions">
        <template v-if="activeTab === 'works'">
          <span class="batch-action-btn" @click="handleScopeSetting"><i class="el-icon-setting"></i>权限设置</span>
          <span class="batch-action-btn danger" @click="handleBatchDelete"><i class="el-icon-delete-solid"></i>删除</span>
        </template>
        <template v-if="activeTab === 'liked'">
          <span class="batch-action-btn danger" @click="handleBatchUnlike"><i class="el-icon-delete-solid"></i>取消喜欢</span>
        </template>
        <template v-if="activeTab === 'favorite'">
          <span class="batch-action-btn danger" @click="handleBatchUnfavorite"><i class="el-icon-delete-solid"></i>取消收藏</span>
        </template>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="video-list">
      <div v-loading="loading" class="video-grid">
        <div 
          v-for="video in videoList" 
          :key="video.id" 
          class="video-item"
          :class="{ 'batch-mode': batchMode, 'checked': isVideoSelected(video.id) }">
          <div v-if="batchMode" class="video-checkbox" @click.stop="handleCheckChange(video)">
            <el-checkbox :value="isVideoSelected(video.id)" />
          </div>
          <div class="video-cover" @click="handleVideoClick(video)">
            <img :src="video.coverUrl" alt="视频封面" />
            <div class="status-badge" v-if="video.status !== 2">
              {{ getStatusText(video.status) }}
            </div>
            <div class="play-icon">
              <i class="el-icon-video-play"></i>
            </div>
            <div class="duration" v-if="video.duration">{{ formatDuration(video.duration) }}</div>
          </div>
          <div class="video-info">
            <div class="video-title" :title="video.title">{{ video.title || '未命名' }}</div>
            <div class="video-stats">
              <span><i class="el-icon-view"></i> {{ video.playCount || 0 }}</span>
              <span @click="viewLikedUsers(video)"><i class="iconfont icon-aixin"></i> {{ video.likeCount || 0 }}</span>
              <span @click="viewFavoriteUsers(video)"><i class="el-icon-star-on"></i> {{ video.favoriteCount || 0 }}</span>
              <span @click="viewVideoComment(video)"><i class="iconfont icon-xiaoxi"></i> {{ video.commentCount || 0 }}</span>
            </div>
            <div class="edit-btn" v-if="activeTab === 'works'" @click.stop="handleEditVideo(video)">
              <i class="el-icon-edit"></i>
            </div>
          </div>
        </div>
        
        <div v-if="!loading && videoList.length === 0" class="empty-state">
          <i class="el-icon-video-camera"></i>
          <p>暂无{{ getEmptyText() }}</p>
        </div>
      </div>
    </div>
    <!-- 短视频编辑弹窗 -->
    <ShortVideoEdit
        :visible="editVisible"
        :video-id="editVideoId"
        type="user"
        @close="handleEditClose"
        @refresh="handleRefresh"
    />

    <!-- 权限设置弹窗 -->
    <el-dialog
      title="权限设置"
      :visible.sync="scopeVisible"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-radio-group v-model="selectedScope" class="scope-radio-group">
        <el-radio :label="9">公开</el-radio>
        <el-radio :label="3">关注可见</el-radio>
        <el-radio :label="2">好友可见</el-radio>
        <el-radio :label="1">私密</el-radio>
      </el-radio-group>
      <span slot="footer" class="dialog-footer">
        <el-button @click="scopeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleScopeConfirm">确定</el-button>
      </span>
    </el-dialog>

    <!-- 关注/粉丝弹窗 -->
    <el-dialog
      :title="followDialogTitle"
      :visible.sync="followDialogVisible"
      width="480px"
      :close-on-click-modal="true"
      @close="handleFollowDialogClose"
    >
      <div class="follow-tabs">
        <div
          class="follow-tab-item"
          :class="{ active: followTabActive === 'follow' }"
          @click="switchFollowTab('follow')"
        >关注 {{ this.followCount }}</div>
        <div
          class="follow-tab-item"
          :class="{ active: followTabActive === 'fans' }"
          @click="switchFollowTab('fans')"
        >粉丝 {{ fansDialogList.length }}</div>
      </div>

      <div class="follow-list-content" v-loading="followLoading">
        <!-- 关注列表 -->
        <template v-if="followTabActive === 'follow'">
          <div
            v-for="item in followDialogList"
            :key="item.id"
            class="follow-list-item"
          >
            <head-image :url="item.targetAvatar" :name="item.targetName" :size="44"></head-image>
            <div class="follow-item-info">
              <div class="follow-item-name">
                <span class="type-star" :style="{ color: getStarColor(item.type) }">★</span>
                {{ item.targetName }}
              </div>
              <div class="follow-item-type">{{ getTypeName(item.type) }}</div>
            </div>
            <el-button
              v-if="item.followed"
              size="small"
              type="default"
              @click="handleCancelFollowInDialog(item, 'follow')"
            >已关注</el-button>
            <el-button
              v-else
              size="small"
              type="primary"
              @click="handleAddFollowInDialog(item, 'follow')"
            >关注</el-button>
          </div>
          <div v-if="followDialogList.length === 0" class="empty-follow">
            <p>暂无关注</p>
          </div>
        </template>

        <!-- 粉丝列表 -->
        <template v-if="followTabActive === 'fans'">
          <div
            v-for="item in fansDialogList"
            :key="item.id"
            class="follow-list-item"
          >
            <head-image :url="item.headImage" :name="item.nickName" :size="44"></head-image>
            <div class="follow-item-info">
              <div class="follow-item-name">{{ item.nickName }}</div>
            </div>
            <el-button
              v-if="item.followed"
              size="small"
              type="default"
              @click="handleCancelFollowInDialog(item, 'fans')"
            >已关注</el-button>
            <el-button
              v-else
              size="small"
              type="primary"
              @click="handleAddFollowInDialog(item, 'fans')"
            >回关</el-button>
          </div>
          <div v-if="fansDialogList.length === 0" class="empty-follow">
            <p>暂无粉丝</p>
          </div>
        </template>
      </div>
    </el-dialog>

    <video-play ref="videoPlay"
                :videoUrl="videoUrl"
                :posterUrl="posterUrl"
                :video-height="videoHeight"
                :video-width="videoWidth"
                @close="closeVideoPlay"></video-play>
    <el-drawer
        v-if="drawerVisible"
        title=""
        :visible.sync="drawerVisible"
        :with-header="false">
      <div class="comment-drawer-content">
        <div v-if="!showCommentInput" class="comment-btn-wrapper">
          <div class="comment-text-btn" @click="openCommentInput">发表评论</div>
          <span v-if="!commentForm.characterId" class="comment-setting-btn" @click="chooseCharacterDialogVisible = true">
            <i class="el-icon-setting"></i>
          </span>
          <div v-else class="comment-character-info">
            <head-image :url="commentForm.avatar" :name="commentForm.nickName" :size="24" radius="50%"></head-image>
            <span class="character-clear" @click="clearCommentForm"><i class="el-icon-close"></i></span>
          </div>
        </div>
        <div v-if="showCommentInput" class="comment-input-wrapper">
          <div class="input-header">
            <span class="input-title">发表评论</span>
            <span class="input-close" @click="showCommentInput = false">收起 <i class="el-icon-arrow-up"></i></span>
          </div>
          <input-box ref="commentInput" width="100%" :character-id="commentForm.characterId"
                     @send="handleSendComment" @sendWord="handleSendCommentWord"></input-box>
        </div>
        <ShortVideoCommentList ref="commentListRef" :video="curVideo" :commentForm="commentForm" @closeCommentInput="showCommentInput = false" />
      </div>
    </el-drawer>

    <template-character-choose
        :visible.sync="chooseCharacterDialogVisible"
        @close="closeChooseCharacterDialog"
        @confirm="confirmChooseCharacter">
    </template-character-choose>

    <!-- 点赞/收藏用户列表抽屉 -->
    <el-drawer
        title=""
        :visible.sync="userListDrawerVisible"
        :with-header="false"
        :size="'20%'"
        @close="userListData = []">
      <div class="user-list-drawer">
        <div class="user-list-header">
          <i class="el-icon-arrow-left back-btn" @click="userListDrawerVisible = false"></i>
          <span class="user-list-title">{{ userListTitle }}</span>
        </div>
        <div class="user-list-content" v-loading="userListLoading">
          <div
            v-for="item in userListData"
            :key="item.id"
            class="user-list-item"
          >
            <head-image :url="item.headImage" :name="item.nickName" :size="44"></head-image>
            <div class="user-list-item-info">
              <div class="user-list-item-name">{{ item.nickName }}</div>
            </div>
            <span
              v-if="!isCurrentUser(item)"
              class="user-follow-button"
              :class="{ followed: isUserFollowed(item), loading: isUserFollowActioning(item) }"
              @click="toggleUserFollow(item)"
            >{{ isUserFollowed(item) ? '已关注' : '关注' }}</span>
          </div>
          <div v-if="!userListLoading && userListData.length === 0" class="empty-user-list">
            <p>暂无数据</p>
          </div>
          <div v-if="!userListLoading && userListData.length > 0 && userListData.length < userListTotal" class="load-more-wrapper" @click="loadMoreUsers">
            <span>加载更多</span>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'
import ShortVideoEdit from '@/components/shortVideo/ShortVideoEdit.vue'
import VideoPlay from '@/components/common/VideoPlay.vue'
import InputBox from '@/components/common/InputBox.vue'
import TemplateCharacterChoose from '@/components/template/TemplateCharacterChoose'
import ShortVideoCommentList from '@/components/shortVideo/ShortVideoCommentList.vue'

export default {
  name: 'ShortVideoMy',
  components: {
    HeadImage,
    ShortVideoEdit,
    VideoPlay,
    InputBox,
    TemplateCharacterChoose,
    ShortVideoCommentList,
  },
  data() {
    return {
      userInfo: {},
      activeTab: 'works',
      videoList: [],
      loading: false,
      followCount: 0,
      fansCount: 0,
      likeCount: 0,
      editVisible: false,
      editVideoId: null,
      batchMode: false,
      selectedVideos: [],
      videoUrl: '',
      posterUrl: '',
      videoWidth: 0,
      videoHeight: 0,
      scopeVisible: false,
      selectedScope: 9,
      followDialogVisible: false,
      followTabActive: 'follow',
      followDialogList: [],
      fansDialogList: [],
      followLoading: false,
      drawerVisible: false,
      curVideo: {},
      commentForm: {
        characterAvatarId: null,
        characterId: null,
        nickName: '',
        avatar: '',
      },
      showCommentInput: false,
      chooseCharacterDialogVisible: false,
      userListDrawerVisible: false,
      userListTitle: '',
      userListData: [],
      userListLoading: false,
      userListPageNo: 1,
      userListTotal: 0,
      userListVideoId: null,
      userListType: '',
      userFollowActioning: {},
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
  computed: {
    isAllSelected() {
      return this.videoList.length > 0 && this.selectedVideos.length === this.videoList.length
    },
    isIndeterminate() {
      return this.selectedVideos.length > 0 && this.selectedVideos.length < this.videoList.length
    },
    followDialogTitle() {
      return this.followTabActive === 'follow' ? '我的关注' : '我的粉丝'
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
        this.followCount = this.userInfo.followCount || 0
        this.fansCount = this.userInfo.fansCount || 0
        this.likeCount = this.userInfo.likeCount || 0
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
    
    // 批量管理模式切换
    toggleBatchMode() {
      this.batchMode = !this.batchMode
      if (!this.batchMode) {
        this.selectedVideos = []
      }
    },

    // 判断视频是否被选中
    isVideoSelected(videoId) {
      return this.selectedVideos.includes(videoId)
    },

    // 复选框勾选/取消
    handleCheckChange(video) {
      const idx = this.selectedVideos.indexOf(video.id)
      if (idx > -1) {
        this.selectedVideos.splice(idx, 1)
      } else {
        this.selectedVideos.push(video.id)
      }
    },

    // 点击视频
    handleVideoClick(video) {
      if (this.batchMode) {
        this.handleCheckChange(video)
        return
      }
      console.log('点击视频:', video)
      this.videoUrl = video.videoUrl;
      this.posterUrl = video.coverUrl;
      this.videoWidth = video.width;
      this.videoHeight = video.height;
      this.$refs.videoPlay.onPlayVideo()
    },

    // 打开发布弹窗
    handlePublish() {
      this.editVideoId = null
      this.editVisible = true
    },

    // 关闭编辑弹窗
    handleEditClose() {
      this.editVisible = false
      this.editVideoId = null
    },

    // 刷新列表
    handleRefresh() {
      this.editVisible = false
      this.editVideoId = null
      this.loadVideoList()
    },

    // 获取状态文本
    getStatusText(status) {
      const map = { 0: '草稿', 1: '审核中', 3: '未通过审核' }
      return map[status] || ''
    },

    // 编辑视频
    handleEditVideo(video) {
      this.editVideoId = video.id
      this.editVisible = true
    },

    // 全选/取消全选
    handleSelectAll(checked) {
      if (checked) {
        this.selectedVideos = this.videoList.map(v => v.id)
      } else {
        this.selectedVideos = []
      }
    },

    // 获取批量计数文本
    getBatchCountText() {
      switch(this.activeTab) {
        case 'works': return '作品'
        case 'liked': return '喜欢的作品'
        case 'favorite': return '收藏的作品'
        default: return ''
      }
    },

    // 批量删除作品
    handleBatchDelete() {
      if (this.selectedVideos.length === 0) {
        this.$message.warning('请先选择作品')
        return
      }
      this.$confirm(`确定要删除选中的${this.selectedVideos.length}个作品吗？删除后不可恢复。`, '删除确认', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: '/shortVideo/batchDelete',
          method: 'post',
          data: { ids: this.selectedVideos }
        }).then(() => {
          this.$message.success('删除成功')
          this.selectedVideos = []
          this.loadVideoList()
        })
      }).catch(() => {})
    },

    // 批量取消喜欢
    handleBatchUnlike() {
      if (this.selectedVideos.length === 0) {
        this.$message.warning('请先选择作品')
        return
      }
      this.$confirm(`确定要取消喜欢选中的${this.selectedVideos.length}个作品吗？`, '取消喜欢确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: '/shortVideoLike/batchDelete',
          method: 'delete',
          data: { videoIds: this.selectedVideos }
        }).then(() => {
          this.$message.success('已取消喜欢')
          this.selectedVideos = []
          this.loadVideoList()
        })
      }).catch(() => {})
    },

    // 批量取消收藏
    handleBatchUnfavorite() {
      if (this.selectedVideos.length === 0) {
        this.$message.warning('请先选择作品')
        return
      }
      this.$confirm(`确定要取消收藏选中的${this.selectedVideos.length}个作品吗？`, '取消收藏确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: '/shortVideoFavorite/batchDelete',
          method: 'delete',
          data: { videoIds: this.selectedVideos }
        }).then(() => {
          this.$message.success('已取消收藏')
          this.selectedVideos = []
          this.loadVideoList()
        })
      }).catch(() => {})
    },

    // 打开权限设置弹窗
    handleScopeSetting() {
      if (this.selectedVideos.length === 0) {
        this.$message.warning('请先选择作品')
        return
      }
      this.selectedScope = 9
      this.scopeVisible = true
    },

    // 确认权限设置
    handleScopeConfirm() {
      this.$http({
        url: '/shortVideo/batchUpdateScope',
        method: 'post',
        data: { ids: this.selectedVideos, scope: this.selectedScope }
      }).then(() => {
        this.$message.success('权限设置成功')
        this.scopeVisible = false
        this.selectedVideos = []
        this.loadVideoList()
      })
    },

    closeVideoPlay() {
      this.videoUrl = '';
      this.posterUrl = '';
    },

    // ========== 关注/粉丝弹窗相关方法 ==========
    openFollowDialog(tab) {
      this.followTabActive = tab
      this.followDialogVisible = true
      this.loadFollowDialogData()
    },
    switchFollowTab(tab) {
      this.followTabActive = tab
      if (tab === 'follow' && this.followDialogList.length === 0) {
        this.fetchFollowDialogList()
      } else if (tab === 'fans' && this.fansDialogList.length === 0) {
        this.fetchFansDialogList()
      }
    },
    loadFollowDialogData() {
      this.fetchFollowDialogList()
      this.fetchFansDialogList()
    },
    fetchFollowDialogList() {
      this.followLoading = true
      this.$http({
        url: '/follow/list',
        method: 'get'
      }).then((data) => {
        this.followDialogList = data || []
      }).catch(() => {
        this.followDialogList = []
      }).finally(() => {
        this.followLoading = false
      })
    },
    fetchFansDialogList() {
      this.followLoading = true
      this.$http({
        url: '/follow/myFans',
        method: 'get'
      }).then((data) => {
        this.fansDialogList = data || []
      }).catch(() => {
        this.fansDialogList = []
      }).finally(() => {
        this.followLoading = false
      })
    },
    handleAddFollowInDialog(item, source) {
      let targetId = null;
      let type = null;
      if (source === 'follow') {
        targetId = item.targetId
        type = item.type
      } else if (source === 'fans') {
        targetId = item.userId
        type = 'user'
      }
      this.$http({
        url: '/follow/add',
        method: 'post',
        data: { targetId: targetId, type: type }
      }).then(() => {
        this.$message.success('关注成功')
        item.followed = true
        if (source === 'fans') {
          this.followDialogList = []
          this.fetchFollowDialogList()
          this.followCount++
        } else if (source === 'follow') {
          this.followCount++
        }
      })
    },
    handleCancelFollowInDialog(item, source) {
      let targetId = null;
      let type = null;
      if (source === 'follow') {
        targetId = item.targetId
        type = item.type
      } else if (source === 'fans') {
        targetId = item.userId
        type = 'user'
      }
      this.$http({
        url: '/follow/cancel',
        method: 'delete',
        params: { targetId: targetId, type: type }
      }).then(() => {
        this.$message.success('已取消关注')
        item.followed = false
        if (source === 'follow') {
          let newFollowCount = this.followDialogList.filter(i => i.followed).length
          console.log('newFollowCount', newFollowCount)
          this.followCount = newFollowCount
        } else if (source === 'fans') {
          this.followDialogList = []
          this.fetchFollowDialogList()
          this.followCount = Math.max(0, this.followCount - 1)
        }
      })
    },
    getStarColor(type) {
      const colorMap = {
        user: '#409EFF',
        group: '#67C23A',
        character: '#E6A23C',
        template: '#9B59B6'
      }
      return colorMap[type] || '#999'
    },
    getTypeName(type) {
      const nameMap = {
        user: '用户',
        group: '群组',
        character: '角色',
        template: '群聊模板'
      }
      return nameMap[type] || type
    },
    handleFollowDialogClose() {
      this.followDialogVisible = false
      this.followTabActive = 'follow'
    },
    viewVideoComment(video) {
      this.curVideo = video
      this.getCommentCharacter();
      this.drawerVisible = true;
    },
    openCommentInput() {
      this.showCommentInput = true
      if (this.$refs.commentListRef) {
        this.$refs.commentListRef.closeAllReplies()
      }
      this.$nextTick(() => {
        if (this.$refs.commentInput) {
          this.$refs.commentInput.view()
        }
      })
    },
    handleSendComment(sendObj) {
      if (!sendObj) return
      const content = sendObj.type === this.$enums.MESSAGE_TYPE.IMAGE
        ? JSON.stringify(sendObj.content)
        : sendObj.content
      this.$http({
        url: '/shortVideoComment/add',
        method: 'post',
        data: {
          videoId: this.curVideo.id,
          content,
          type: sendObj.type,
          characterId: this.commentForm.characterId,
          avatarId: this.commentForm.characterAvatarId
        }
      }).then(() => {
        this.$message.success('评论成功')
        if (this.$refs.commentListRef) this.$refs.commentListRef.refresh()
      })
    },
    handleSendCommentWord(data) {
      if (!data) return
      const content = JSON.stringify({
        id: data.id,
        templateGroupId: data.templateGroupId,
        characterId: data.characterId,
        characterName: data.characterName,
        word: data.word,
        voice: data.voice
      })
      this.$http({
        url: '/shortVideoComment/add',
        method: 'post',
        data: {
          videoId: this.curVideo.id,
          content,
          type: this.$enums.MESSAGE_TYPE.WORD_VOICE,
          characterId: this.commentForm.characterId,
          avatarId: this.commentForm.characterAvatarId
        }
      }).then(() => {
        this.$message.success('评论成功')
        if (this.$refs.commentListRef) this.$refs.commentListRef.refresh()
      })
    },
    clearCommentForm() {
      this.commentForm = {
        characterAvatarId: null,
        characterId: null,
        nickName: '',
        avatar: '',
      }
    },
    closeChooseCharacterDialog() {
      this.chooseCharacterDialogVisible = false
    },
    confirmChooseCharacter(resultData) {
      if (resultData?.characterAvatar?.id) {
        this.commentForm.characterAvatarId = resultData.characterAvatar.id
        this.commentForm.nickName = resultData.characterAvatar.level === 0 ? resultData.templateCharacter.name : resultData.characterAvatar.name
        this.commentForm.avatar = resultData.characterAvatar.avatar
        this.commentForm.characterId = resultData.templateCharacter.id
      } else {
        this.commentForm.nickName = resultData.templateCharacter.name
        this.commentForm.avatar = resultData.templateCharacter.avatar
        this.commentForm.characterId = resultData.templateCharacter.id
      }
      this.chooseCharacterDialogVisible = false
    },
    getCommentCharacter() {
      this.$http({
        url: `/commentCharacter/getCommentCharacter?targetId=${this.curVideo.id}&targetType=shortVideo`,
        method: 'get',
      }).then((res) => {
        this.commentForm.characterAvatarId = res.avatarId;
        this.commentForm.nickName = res.characterName;
        this.commentForm.avatar = res.avatar;
        this.commentForm.characterId = res.characterId;
      })
    },
    viewLikedUsers(video) {
      if (!video.isOwner) {
        return
      }
      this.userListTitle = '点赞用户'
      this.userListType = 'like'
      this.userListVideoId = video.id
      this.userListPageNo = 1
      this.userListData = []
      this.userListLoading = true
      this.$http({
        url: '/shortVideoLike/pageShortVideoLikeUser',
        method: 'post',
        data: { videoId: video.id },
        params: { pageSize: 50, currentPage: 1 }
      }).then((res) => {
        this.userListData = res.data || []
        this.userListTotal = res.total || 0
      }).catch(() => {
        this.userListData = []
        this.userListTotal = 0
      }).finally(() => {
        this.userListLoading = false
      })
      this.userListDrawerVisible = true
    },
    viewFavoriteUsers(video) {
      if (!video.isOwner) {
        return
      }
      this.userListTitle = '收藏用户'
      this.userListType = 'favorite'
      this.userListVideoId = video.id
      this.userListPageNo = 1
      this.userListData = []
      this.userListLoading = true
      this.$http({
        url: '/shortVideoFavorite/pageShortVideoFavoritesUser',
        method: 'post',
        data: { videoId: video.id },
        params: { pageSize: 50, currentPage: 1 }
      }).then((res) => {
        this.userListData = res.data || []
        this.userListTotal = res.total || 0
      }).catch(() => {
        this.userListData = []
        this.userListTotal = 0
      }).finally(() => {
        this.userListLoading = false
      })
      this.userListDrawerVisible = true
    },
    loadMoreUsers() {
      if (this.userListLoading) return
      this.userListPageNo++
      this.userListLoading = true
      const url = this.userListType === 'like'
        ? '/shortVideoLike/pageShortVideoLikeUser'
        : '/shortVideoFavorite/pageShortVideoFavoritesUser'
      this.$http({
        url: url,
        method: 'post',
        data: { videoId: this.userListVideoId },
        params: { pageSize: 50, currentPage: this.userListPageNo }
      }).then((res) => {
        this.userListData = this.userListData.concat(res.data || [])
        this.userListTotal = res.total || 0
      }).catch(() => {
        this.userListPageNo--
      }).finally(() => {
        this.userListLoading = false
      })
    },
    isCurrentUser(item) {
      return Boolean(item && item.userId != null && this.userInfo && this.userInfo.id != null
        && String(item.userId) === String(this.userInfo.id))
    },
    isUserFollowed(item) {
      return Boolean(item && item.userId != null && this.$store.getters.isFollowed(`${item.userId}:user`))
    },
    isUserFollowActioning(item) {
      return Boolean(item && this.userFollowActioning[`user:${item.userId}`])
    },
    toggleUserFollow(item) {
      if (!item || !item.userId || this.isCurrentUser(item) || this.isUserFollowActioning(item)) return
      const follow = {targetId: item.userId, type: 'user'}
      const followed = this.isUserFollowed(item)
      const actionKey = `user:${item.userId}`
      this.$set(this.userFollowActioning, actionKey, true)
      this.$http({
        url: followed ? '/follow/cancel' : '/follow/add',
        method: followed ? 'delete' : 'post',
        params: followed ? {targetId: item.userId, type: 'user'} : {},
        data: followed ? {} : follow
      }).then((savedFollow) => {
        if (followed) this.$store.commit('removeFollow', follow)
        else this.$store.commit('addFollow', savedFollow || follow)
        this.$message.success(followed ? '已取消关注' : '关注成功')
      }).finally(() => {
        this.$delete(this.userFollowActioning, actionKey)
      })
    },
  }
}
</script>

<style scoped lang="scss">
.short-video-my {
  min-height: 100vh;
  background: #f5f7fa;
}

.comment-drawer-content {
  .comment-btn-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    border-bottom: 1px solid #f0f0f0;
  }

  .comment-text-btn {
    flex: 1;
    padding: 10px 0;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #f5f7fa;
    color: #606266;
    font-size: 14px;
    text-align: center;
    cursor: pointer;

    &:hover {
      color: #409eff;
      border-color: #c6e2ff;
      background: #ecf5ff;
    }
  }

  .comment-setting-btn, .comment-character-info {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 36px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #f5f7fa;
  }

  .comment-setting-btn {
    width: 36px;
    color: #999;
    cursor: pointer;

    &:hover {
      color: #409eff;
      border-color: #c6e2ff;
      background: #ecf5ff;
    }
  }

  .comment-character-info {
    gap: 6px;
    padding: 0 8px 0 4px;
    background: #ecf5ff;
  }

  .character-clear {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: #ccc;
    color: #fff;
    cursor: pointer;
    font-size: 10px;

    &:hover { background: #f56c6c; }
  }

  .comment-input-wrapper {
    padding: 12px;
    border-bottom: 1px solid #f0f0f0;
  }

  .input-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .input-title { font-size: 13px; color: #333; font-weight: 500; }
  .input-close { font-size: 12px; color: #999; cursor: pointer; }
  .input-close:hover { color: #409eff; }
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

    .publish-section {
      flex-shrink: 0;
      margin-left: auto;
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

      &.stat-clickable {
        cursor: pointer;
        border-radius: 8px;
        padding: 8px 0;
        transition: background 0.2s;

        &:hover {
          background: #f5f7fa;
        }
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
  
  .batch-btn {
    flex-shrink: 0;
    padding: 14px 16px;
    cursor: pointer;
    font-size: 14px;
    color: #409eff;
    font-weight: 500;
    white-space: nowrap;
    transition: color 0.2s ease;
    
    &:hover {
      color: #66b1ff;
    }
  }
}

// 批量管理操作栏
.batch-action-bar {
  display: flex;
  align-items: center;
  background: white;
  padding: 10px 16px;
  border-bottom: 1px solid #ebeef5;
  
  .batch-select-all {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    font-size: 14px;
    color: #303133;
    flex-shrink: 0;
  }
  
  .batch-selected-count {
    font-size: 13px;
    color: #909399;
    padding-left: 16px;
  }
  
  .batch-actions {
    display: flex;
    gap: 12px;
    flex-shrink: 0;
    margin-left: 16px;
    
    .batch-action-btn {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #606266;
      cursor: pointer;
      white-space: nowrap;
      
      i {
        font-size: 14px;
      }
      
      &:hover {
        color: #409eff;
      }
      
      &.danger {
        color: #f56c6c;
        
        &:hover {
          color: #f89898;
        }
      }
    }
  }
}

// 权限弹窗
.scope-radio-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 10px 0;
}

// 视频列表
.video-list {
  padding: 12px;
  
  .video-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 2px;
    min-height: 300px;
  }
  
  .video-item {
    cursor: pointer;
    position: relative;
    width: calc((100% - 10px) / 6);
    
    .video-checkbox {
      position: absolute;
      top: 6px;
      right: 6px;
      z-index: 5;
    }
    
    &.batch-mode.checked {
      .video-cover::after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(64, 158, 255, 0.15);
        z-index: 4;
      }
    }
    
    .video-cover {
      position: relative;
      width: 100%;
      padding-top: 100%;
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
      position: relative;
      
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

  .status-badge {
    position: absolute;
    top: 0;
    left: 0;
    background: rgba(0, 0, 0, 0.55);
    color: #fff;
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 0 0 6px 0;
    z-index: 3;
  }

  .edit-btn {
    position: absolute;
    top: 50%;
    right: 8px;
    transform: translateY(-50%);
    z-index: 5;
    width: 28px;
    height: 28px;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s ease;
    cursor: pointer;

    i {
      font-size: 14px;
      color: #fff;
    }
  }

  .video-item:hover .edit-btn {
    opacity: 1;
  }
  
  .empty-state {
    width: 100%;
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

// 关注/粉丝弹窗样式
.follow-tabs {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;

  .follow-tab-item {
    flex: 1;
    text-align: center;
    padding: 12px 0;
    font-size: 15px;
    color: #909399;
    cursor: pointer;
    position: relative;
    font-weight: 500;
    transition: color 0.2s;

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
        width: 32px;
        height: 3px;
        background: #333;
        border-radius: 2px;
      }
    }
  }
}

.follow-list-content {
  max-height: 400px;
  overflow-y: auto;

  .follow-list-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
    gap: 12px;

    & + .follow-list-item {
      border-top: 1px solid #f5f5f5;
    }

    .follow-item-info {
      flex: 1;
      min-width: 0;
      overflow: hidden;

      .follow-item-name {
        font-size: 14px;
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        display: flex;
        align-items: center;
        gap: 4px;

        .type-star {
          font-size: 14px;
          flex-shrink: 0;
        }
      }

      .follow-item-type {
        font-size: 12px;
        color: #999;
        margin-top: 2px;
      }
    }
  }

  .empty-follow {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: #c0c4cc;
    font-size: 14px;

    p {
      margin: 0;
    }
  }
}

// 用户列表抽屉样式
.user-list-drawer {
  .user-list-header {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #ebeef5;
    
    .back-btn {
      font-size: 20px;
      cursor: pointer;
      color: #303133;
      margin-right: 12px;
      
      &:hover {
        color: #409EFF;
      }
    }
    
    .user-list-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .user-list-content {
    max-height: 500px;
    overflow-y: auto;
    
    .user-list-item {
      display: flex;
      align-items: center;
      padding: 12px 20px;
      gap: 12px;
      
      & + .user-list-item {
        border-top: 1px solid #f5f5f5;
      }
      
      .user-list-item-info {
        flex: 1;
        min-width: 0;
        
        .user-list-item-name {
          font-size: 14px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .user-follow-button {
        flex-shrink: 0;
        min-width: 52px;
        padding: 6px 10px;
        border-radius: 14px;
        background: #f23b54;
        color: #fff;
        font-size: 12px;
        line-height: 1;
        text-align: center;
        cursor: pointer;

        &.followed {
          background: #b8b8b8;
        }

        &.loading {
          cursor: not-allowed;
          opacity: 0.65;
        }
      }
    }
    
    .empty-user-list {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      color: #c0c4cc;
      font-size: 14px;
      
      p {
        margin: 0;
      }
    }
    
    .load-more-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 16px 20px;
      cursor: pointer;
      color: #409eff;
      font-size: 14px;
      
      &:hover {
        color: #66b1ff;
      }
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .video-list .video-grid {
    gap: 1px;
  }
  .video-list .video-item {
    width: calc((100% - 4px) / 5);
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
