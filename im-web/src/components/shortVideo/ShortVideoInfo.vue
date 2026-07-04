<template>
  <div class="short-video-info-panel">
    <!-- 关闭按钮 -->
    <div class="panel-close" @click="$emit('close')">
      <i class="el-icon-close"></i>
    </div>

    <!-- 标题栏 -->
    <div class="tab-header">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'detail' }"
        @click="activeTab = 'detail'"
      >详情</div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'comment' }"
        @click="activeTab = 'comment'"
      >评论</div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'works' }"
        @click="activeTab = 'works'"
      >TA的作品</div>
    </div>

    <!-- 详情 Tab -->
    <div v-if="activeTab === 'detail'" class="tab-content detail-tab">
      <div class="detail-author">
        <head-image
          :url="video.headImage"
          :name="video.authorName || '?'"
          :size="48"
          radius="50%"
        ></head-image>
        <span class="author-name">@{{ video.authorName || '用户' + video.userId }}</span>
      </div>
      <div class="detail-item" v-if="video.title">
        <div class="detail-label">标题</div>
        <div class="detail-value">{{ video.title }}</div>
      </div>
      <div class="detail-item">
        <div class="detail-label">发布时间</div>
        <div class="detail-value">{{ formatDate(video.createTime) }}</div>
      </div>
      <div class="detail-item" v-if="video.description">
        <div class="detail-label">描述</div>
        <div class="detail-value">{{ video.description }}</div>
      </div>
    </div>

    <!-- 评论 Tab -->
    <div v-if="activeTab === 'comment'" class="tab-content comment-tab">
      <!-- 文字评论按钮 -->
      <div v-if="!showCommentInput" class="comment-btn-wrapper">
        <div class="comment-text-btn" @click="openCommentInput">发表评论</div>
        <span v-if="!commentForm.characterId" class="comment-setting-btn" @click="chooseCharacterDialogVisible = true">
          <i class="el-icon-setting"></i>
        </span>
        <div v-else class="comment-character-info">
          <head-image :url="commentForm.avatar" :name="commentForm.nickName" :size="24" radius="50%"></head-image>
          <span class="character-clear" @click="clearCommentForm">
            <i class="el-icon-close"></i>
          </span>
        </div>
      </div>

      <!-- 评论输入框 -->
      <div v-if="showCommentInput" class="comment-input-wrapper">
        <div class="input-header">
          <span class="input-title">发表评论</span>
          <span class="input-close" @click="showCommentInput = false">收起 <i class="el-icon-arrow-up"></i></span>
        </div>
        <input-box ref="commentInput" width="100%" :character-id="commentForm.characterId"
                   @send="handleSendComment"
                   @sendWord="handleSendCommentWord"></input-box>
      </div>

      <ShortVideoCommentList ref="commentListRef" :video="video" :commentForm="commentForm" @closeCommentInput="showCommentInput = false" />
    </div>

    <!-- TA的作品 Tab -->
    <div v-if="activeTab === 'works'" class="tab-content works-tab">
      <!-- 加载中 -->
      <div v-if="worksLoading" class="works-loading">
        <i class="el-icon-loading"></i>
      </div>

      <template v-else>
        <!-- 作者信息 -->
        <div class="works-author-info">
          <div class="works-author-name">@{{ video.authorName || '用户' + video.userId }}</div>
          <div class="works-author-stats">
            <span class="stat-item">粉丝 {{ authorInfo ? authorInfo.fansCount : 0 }}</span>
            <span class="stat-divider">|</span>
            <span class="stat-item">获赞 {{ authorInfo ? authorInfo.shortVideoLikedCount : 0 }}</span>
          </div>
        </div>

        <!-- 作品网格 -->
        <div v-if="worksList.length > 0" class="works-grid">
          <div
            v-for="work in worksList"
            :key="work.id"
            class="work-item"
            @click="playWork(work)"
          >
            <div class="work-cover-wrapper">
              <img :src="work.coverUrl" class="work-cover" />
              <!-- 播放中图标 -->
              <div v-if="isCurrentWork(work.id)" class="work-playing-overlay">
                <i class="el-icon-video-play"></i>
              </div>
            </div>
            <div class="work-like-count">
              <i class="iconfont icon-hongxin1"></i>
              <span>{{ work.likeCount || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="no-content">暂无作品</div>
      </template>
    </div>
    <template-character-choose
        :visible.sync="chooseCharacterDialogVisible"
        @close="closeChooseCharacterDialog"
        @confirm="confirmChooseCharacter">
    </template-character-choose>
  </div>
</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'
import InputBox from '@/components/common/InputBox.vue'
import TemplateCharacterChoose from "@/components/template/TemplateCharacterChoose";
import ShortVideoCommentList from '@/components/shortVideo/ShortVideoCommentList.vue'

export default {
  name: 'ShortVideoInfo',
  components: {
    HeadImage,
    InputBox,
    TemplateCharacterChoose,
    ShortVideoCommentList
  },
  props: {
    video: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      activeTab: 'detail',
      showCommentInput: false,
      // 作品Tab
      worksList: [],
      worksLoading: false,
      worksPageNo: 1,
      worksPageSize: 12,
      worksHasMore: true,
      worksTotal: 0,
      authorInfo: null,
      chooseCharacterDialogVisible: false,
      commentForm: {
        characterAvatarId: null,
        characterId: null,
        nickName: '',
        avatar: '',
      },
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'comment') {
        this.getCommentCharacter();
        this.$nextTick(() => {
          if (this.$refs.commentInput) {
            this.$refs.commentInput.view()
          }
        })
      }
      if (val === 'works' && this.worksList.length === 0) {
        this.fetchWorks()
      }
    },
    video() {
      this.showCommentInput = false
      this.activeTab = 'detail'
      // 重置作品数据
      this.worksList = []
      this.worksPageNo = 1
      this.worksTotal = 0
      this.worksHasMore = true
      this.authorInfo = null
    }
  },
  methods: {
    // ==================== 详情相关 ====================
    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const min = String(d.getMinutes()).padStart(2, '0')
      return `${y}-${m}-${day} ${h}:${min}`
    },

    openCommentInput() {
      this.showCommentInput = true
      // 关闭子组件的所有回复输入框
      if (this.$refs.commentListRef) {
        this.$refs.commentListRef.closeAllReplies()
      }
      this.$nextTick(() => {
        if (this.$refs.commentInput) {
          this.$refs.commentInput.view()
        }
      })
    },

    // 发送顶级评论
    handleSendComment(sendObj) {
      if (!sendObj) {
        return
      }
      let content = null;
      if (sendObj.type === this.$enums.MESSAGE_TYPE.IMAGE) {
        content = JSON.stringify(sendObj.content)
      } else {
        content = sendObj.content
      }
      this.$http({
        url: '/shortVideoComment/add',
        method: 'post',
        data: {
          videoId: this.video.id,
          content: content,
          type: sendObj.type,
          characterId: this.commentForm.characterId,
          avatarId: this.commentForm.characterAvatarId
        }
      }).then((res) => {
        this.$message.success('评论成功')
        // 重新加载评论列表
        if (this.$refs.commentListRef) {
          this.$refs.commentListRef.refresh()
        }
      })
    },
    handleSendCommentWord(data) {
      if (!data) {
        return
      }
      let content = JSON.stringify({
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
          videoId: this.video.id,
          content: content,
          type: this.$enums.MESSAGE_TYPE.WORD_VOICE,
          characterId: this.commentForm.characterId,
          avatarId: this.commentForm.characterAvatarId
        }
      }).then((res) => {
        this.$message.success('评论成功')
        // 重新加载评论列表
        if (this.$refs.commentListRef) {
          this.$refs.commentListRef.refresh()
        }
      })
    },



    // ==================== 作品相关 ====================
    fetchAuthorInfo() {
      if (!this.video.objectId || !this.video.type) return
      this.$http({
        url: '/user/findTargetInfo',
        method: 'post',
        data: {
          targetId: this.video.objectId,
          type: this.video.type
        }
      }).then((res) => {
        this.authorInfo = res
      }).catch(() => {})
    },

    fetchWorks() {
      if (this.worksLoading) return
      this.worksLoading = true

      this.$http({
        url: '/shortVideo/recommend',
        method: 'post',
        params: {
          pageNo: this.worksPageNo,
          pageSize: this.worksPageSize
        },
        data: {
          objectId: this.video.objectId,
          type: this.video.type
        }
      }).then((res) => {
        this.worksList = res.data || []
        this.worksTotal = res.total || 0
        this.worksHasMore = this.worksList.length < this.worksTotal
      }).finally(() => {
        this.worksLoading = false
      })

      // 同时获取作者信息
      this.fetchAuthorInfo()
    },

    isCurrentWork(workId) {
      return this.video && this.video.id === workId
    },

    clearCommentForm() {
      this.commentForm = {
        characterAvatarId: null,
        characterId: null,
        nickName: '',
        avatar: '',
      }
    },

    playWork(work) {
      this.$emit('play-video', work)
    },
    closeChooseCharacterDialog() {
      this.chooseCharacterDialogVisible = false;
    },
    confirmChooseCharacter(resultData) {
      if (resultData?.characterAvatar?.id) {
        this.commentForm.characterAvatarId = resultData.characterAvatar.id;
        this.commentForm.nickName = resultData.characterAvatar.level === 0 ? resultData.templateCharacter.name : resultData.characterAvatar.name;
        this.commentForm.avatar = resultData.characterAvatar.avatar;
        this.commentForm.characterId = resultData.templateCharacter.id;
      } else {
        this.commentForm.nickName = resultData.templateCharacter.name;
        this.commentForm.avatar = resultData.templateCharacter.avatar;
        this.commentForm.characterId = resultData.templateCharacter.id;
      }

      this.chooseCharacterDialogVisible = false;
    },
    getCommentCharacter() {
      this.$http({
        url: `/commentCharacter/getCommentCharacter?targetId=${this.video.id}&targetType=shortVideo`,
        method: 'get',
      }).then((res) => {
        this.commentForm.characterAvatarId = res.avatarId;
        this.commentForm.nickName = res.characterName;
        this.commentForm.avatar = res.avatar;
        this.commentForm.characterId = res.characterId;
      })
    }
  }
}
</script>

<style scoped lang="scss">
.short-video-info-panel {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  background: #f5f5f5;
  color: #666;
  z-index: 10;
  transition: all 0.2s;

  &:hover {
    background: #e8e8e8;
    color: #333;
  }

  i {
    font-size: 14px;
  }
}

.tab-header {
  display: flex;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 14px 0;
    font-size: 14px;
    color: #999;
    cursor: pointer;
    position: relative;
    transition: color 0.2s;
    user-select: none;

    &:hover {
      color: #666;
    }

    &.active {
      color: #333;
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        bottom: -1px;
        left: 50%;
        transform: translateX(-50%);
        width: 32px;
        height: 3px;
        background: #409eff;
        border-radius: 2px;
      }
    }
  }
}

.tab-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

// ==================== 详情 Tab ====================
.detail-tab {
  .detail-author {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;

    .author-name {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }

  .detail-item {
    margin-bottom: 16px;

    .detail-label {
      font-size: 12px;
      color: #999;
      margin-bottom: 6px;
    }

    .detail-value {
      font-size: 14px;
      color: #333;
      line-height: 1.6;
      word-break: break-all;
    }
  }
}

// ==================== 评论 Tab ====================
.comment-tab {
  display: flex;
  flex-direction: column;
  padding: 0;

  .comment-btn-wrapper {
    padding: 12px;
    border-bottom: 1px solid #f0f0f0;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 8px;

    .comment-text-btn {
      flex: 1;
      text-align: center;
      padding: 10px 0;
      background: #f5f7fa;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      color: #606266;
      font-size: 14px;
      cursor: pointer;
      user-select: none;
      transition: all 0.2s;

      &:hover {
        color: #409eff;
        border-color: #c6e2ff;
        background: #ecf5ff;
      }
    }

    .comment-setting-btn {
      flex-shrink: 0;
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      border: 1px solid #dcdfe6;
      background: #f5f7fa;
      color: #999;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        color: #409eff;
        border-color: #c6e2ff;
        background: #ecf5ff;
      }

      i {
        font-size: 16px;
      }
    }

    .comment-character-info {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 0 8px 0 4px;
      height: 36px;
      border-radius: 4px;
      border: 1px solid #dcdfe6;
      background: #ecf5ff;
      position: relative;

      .character-name {
        font-size: 12px;
        color: #409eff;
        max-width: 80px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .character-clear {
        width: 16px;
        height: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        background: #ccc;
        color: #fff;
        cursor: pointer;
        font-size: 10px;
        transition: all 0.2s;

        &:hover {
          background: #f56c6c;
        }

        i {
          font-size: 10px;
          font-weight: bold;
        }
      }
    }
  }

  .comment-input-wrapper {
    padding: 12px;
    border-bottom: 1px solid #f0f0f0;
    flex-shrink: 0;

    .input-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .input-title {
        font-size: 13px;
        color: #333;
        font-weight: 500;
      }

      .input-close {
        font-size: 12px;
        color: #999;
        cursor: pointer;

        &:hover {
          color: #409eff;
        }

        i {
          font-size: 12px;
        }
      }
    }
  }

}


// ==================== TA的作品 Tab ====================
.works-tab {
  .works-loading {
    text-align: center;
    padding: 60px 0;
    color: #999;

    i {
      font-size: 24px;
    }
  }

  .works-author-info {
    padding-bottom: 16px;
    margin-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;

    .works-author-name {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin-bottom: 8px;
    }

    .works-author-stats {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;

      .stat-item {
        color: #666;
      }

      .stat-divider {
        color: #ddd;
      }
    }
  }

  .works-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .work-item {
    cursor: pointer;
    border-radius: 6px;
    overflow: hidden;
    transition: transform 0.15s;

    &:hover {
      transform: scale(1.03);
    }
  }

  .work-cover-wrapper {
    position: relative;
    width: 100%;
    padding-top: 133%;
    background: #f0f0f0;
    border-radius: 6px;
    overflow: hidden;

    .work-cover {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .work-playing-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.35);
      display: flex;
      align-items: center;
      justify-content: center;

      i {
        font-size: 28px;
        color: #fff;
      }
    }
  }

  .work-like-count {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 2px 8px;
    font-size: 12px;
    color: #999;

    i {
      font-size: 12px;
    }
  }

  .no-content {
    text-align: center;
    color: #ccc;
    font-size: 14px;
    padding: 60px 0;
  }
}
</style>
