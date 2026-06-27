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
          :name="video.nickName || '?'"
          :size="48"
          radius="50%"
        ></head-image>
        <span class="author-name">@{{ video.nickName || video.authorName || '用户' + video.userId }}</span>
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
        <div class="comment-text-btn" @click="openCommentInput">开始评论</div>
      </div>

      <!-- 评论输入框 -->
      <div v-if="showCommentInput" class="comment-input-wrapper">
        <div class="input-header">
          <span class="input-title">发表评论</span>
          <span class="input-close" @click="showCommentInput = false">收起 <i class="el-icon-arrow-up"></i></span>
        </div>
        <input-box ref="commentInput" width="100%" @send="handleSendComment"></input-box>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list" v-loading="commentLoading">
        <div v-if="commentList.length === 0 && !commentLoading" class="no-comment">
          暂无评论，快来抢沙发吧~
        </div>

        <div
          v-for="item in commentList"
          :key="item.id"
          class="comment-item"
          :class="{ 'sub-comment': item.topReplyCommentId && item.topReplyCommentId > 0 }"
        >
          <!-- 评论主体 -->
          <div class="comment-main">
            <head-image
              :url="item.userAvatar"
              :name="item.userNickname || '?'"
              :size="32"
              radius="50%"
            ></head-image>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-nickname">{{ item.userNickname }}</span>
                <span class="comment-time">{{ formatTime(item.createTime) }}</span>
                <span v-if="item.isOwner" class="comment-delete" @click="handleDelete(item)">删除</span>
              </div>
              <!-- 回复目标 -->
              <div v-if="item.replyToUserId" class="comment-reply-to">
                回复 <span class="reply-nickname">@{{ item.replyToUserNickname }}</span>
              </div>
              <div class="comment-content" v-if="item.type === 0" v-html="$emo.transform(item.content)"></div>
              <div class="comment-content" v-else-if="item.type === 1">
                <img :src="item.content.originUrl || item.content" class="comment-image" @click="previewImage(item.content.originUrl || item.content)" />
              </div>
              <div class="comment-actions">
                <span class="action-like">
                  <i class="iconfont icon-hongxin1"></i>
                  <span v-if="item.likeCount > 0">{{ item.likeCount }}</span>
                </span>
                <span class="action-reply" @click="toggleReply(item)">回复</span>
              </div>

              <!-- 子评论数入口 -->
              <div
                v-if="item.topReplyCommentId === '0' && item.childCommentCount > 0"
                class="child-comment-entry"
                @click="toggleChildComments(item)"
              >
                <span v-if="!item._showChildren">展开 {{ item.childCommentCount }} 条回复</span>
                <span v-else>收起回复</span>
              </div>

              <!-- 子评论列表 -->
              <div v-if="item._showChildren" class="child-comments">
                <div v-if="item._childLoading" class="child-loading">
                  <i class="el-icon-loading"></i> 加载中...
                </div>
                <div
                  v-for="child in (item._children || [])"
                  :key="child.id"
                  class="comment-item sub-comment"
                >
                  <div class="comment-main">
                    <head-image
                      :url="child.userAvatar"
                      :name="child.userNickname || '?'"
                      :size="28"
                      radius="50%"
                    ></head-image>
                    <div class="comment-body">
                      <div class="comment-header">
                        <span class="comment-nickname">{{ child.userNickname }}</span>
                        <span class="comment-time">{{ formatTime(child.createTime) }}</span>
                        <span v-if="child.isOwner" class="comment-delete" @click="handleDelete(child)">删除</span>
                      </div>
                      <div v-if="child.replyToUserId" class="comment-reply-to">
                        回复 <span class="reply-nickname">@{{ child.replyToUserNickname }}</span>
                      </div>
                      <div class="comment-content" v-if="child.type === 0" v-html="$emo.transform(child.content)"></div>
                      <div class="comment-content" v-else-if="child.type === 1">
                        <img :src="child.content.originUrl || child.content" class="comment-image" @click="previewImage(child.content.originUrl || child.content)" />
                      </div>
                      <div class="comment-actions">
                        <span class="action-like">
                          <i class="iconfont icon-hongxin1"></i>
                          <span v-if="child.likeCount > 0">{{ child.likeCount }}</span>
                        </span>
                        <span class="action-reply" @click="toggleReply(child)">回复</span>
                      </div>
                    </div>
                  </div>

                  <!-- 子评论的回复输入框 -->
                  <div v-show="child._showReply" class="reply-input-wrapper">
                    <input-box :ref="'replyInput_' + child.id" width="100%" :placeholder="'回复 ' + child.userNickname" @send="(sendObj) => handleSendReply(child, sendObj)"></input-box>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 顶级评论的回复输入框 -->
          <div v-show="item._showReply && item.topReplyCommentId === '0'" class="reply-input-wrapper">
            <input-box :ref="'replyInput_' + item.id" width="100%" :placeholder="'回复 ' + item.userNickname" @send="(sendObj) => handleSendReply(item, sendObj)"></input-box>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="commentHasMore" class="load-more" @click="loadMoreComments">
          <span v-if="!commentLoadingMore">加载更多</span>
          <span v-else><i class="el-icon-loading"></i> 加载中...</span>
        </div>
      </div>
    </div>

    <!-- TA的作品 Tab -->
    <div v-if="activeTab === 'works'" class="tab-content works-tab">
      <div class="no-content">暂无作品</div>
    </div>
  </div>
</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'
import InputBox from '@/components/common/InputBox.vue'

export default {
  name: 'ShortVideoInfo',
  components: {
    HeadImage,
    InputBox
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
      commentList: [],
      commentPageNo: 1,
      commentPageSize: 50,
      commentTotal: 0,
      commentLoading: false,
      commentLoadingMore: false,
      commentHasMore: true
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'comment' && this.commentList.length === 0) {
        this.fetchComments()
      }
      if (val === 'comment') {
        this.$nextTick(() => {
          if (this.$refs.commentInput) {
            this.$refs.commentInput.view()
          }
        })
      }
    },
    video() {
      this.commentList = []
      this.commentPageNo = 1
      this.commentTotal = 0
      this.commentHasMore = true
      this.showCommentInput = false
      this.activeTab = 'detail'
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

    formatTime(dateStr) {
      if (!dateStr) return ''
      const now = new Date()
      const d = new Date(dateStr)
      const diff = now - d
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 172800000) return '昨天'
      if (diff < 2592000000) return Math.floor(diff / 86400000) + '天前'
      return this.formatDate(dateStr)
    },

    // ==================== 评论相关 ====================
    fetchComments() {
      if (this.commentLoading || this.commentLoadingMore) return
      this.commentLoading = true

      this.$http({
        url: '/shortVideoComment/pageList',
        method: 'post',
        params: {
          currentPage: 1,
          pageSize: this.commentPageSize
        },
        data: {
          videoId: this.video.id
        }
      }).then((res) => {
        const list = res.data || []
        this.commentList = list.map(item => ({ ...item, _showChildren: false, _children: [], _showReply: false, _childLoading: false }))
        this.commentTotal = res.total || 0
        this.commentPageNo = 1
        this.commentHasMore = this.commentList.length < this.commentTotal
      }).finally(() => {
        this.commentLoading = false
      })
    },

    loadMoreComments() {
      if (this.commentLoadingMore || !this.commentHasMore) return
      this.commentLoadingMore = true

      const nextPage = this.commentPageNo + 1
      this.$http({
        url: '/shortVideoComment/pageList',
        method: 'post',
        params: {
          currentPage: nextPage,
          pageSize: this.commentPageSize
        },
        data: {
          videoId: this.video.id
        }
      }).then((res) => {
        const list = res.data || []
        const newItems = list.map(item => ({ ...item, _showChildren: false, _children: [], _showReply: false, _childLoading: false }))
        this.commentList.push(...newItems)
        this.commentPageNo = nextPage
        this.commentTotal = res.total || 0
        this.commentHasMore = this.commentList.length < this.commentTotal
      }).finally(() => {
        this.commentLoadingMore = false
      })
    },

    openCommentInput() {
      this.showCommentInput = true
      // 关闭所有回复输入框
      this.commentList.forEach(c => {
        if (c._showReply) this.$set(c, '_showReply', false)
        if (c._children) {
          c._children.forEach(child => {
            if (child._showReply) this.$set(child, '_showReply', false)
          })
        }
      })
      this.$nextTick(() => {
        if (this.$refs.commentInput) {
          this.$refs.commentInput.view()
        }
      })
    },

    // 发送顶级评论
    handleSendComment(sendObj) {
      this.$http({
        url: '/shortVideoComment/add',
        method: 'post',
        data: {
          videoId: this.video.id,
          content: typeof sendObj.content === 'string' ? sendObj.content : JSON.stringify(sendObj.content),
          type: sendObj.type
        }
      }).then((res) => {
        this.$message.success('评论成功')
        // 重新加载评论列表
        this.commentList = []
        this.commentPageNo = 1
        this.commentHasMore = true
        this.fetchComments()
      })
    },

    // 发送回复
    handleSendReply(parentComment, sendObj) {
      this.$http({
        url: '/shortVideoComment/add',
        method: 'post',
        data: {
          videoId: this.video.id,
          content: typeof sendObj.content === 'string' ? sendObj.content : JSON.stringify(sendObj.content),
          type: sendObj.type,
          replyCommentId: parentComment.id
        }
      }).then((res) => {
        this.$message.success('回复成功')
        if (parentComment.topReplyCommentId === '0') {
          parentComment._children.push({ ...res, isOwner: true })
          parentComment.childCommentCount = (parentComment.childCommentCount || 0) + 1
          parentComment._showChildren = true;
        } else {
          const topComment = this.commentList.find(c => c.id === parentComment.topReplyCommentId)
          if (topComment) {
            topComment._children.push({ ...res, isOwner: true })
            topComment.childCommentCount = (parentComment.childCommentCount || 0) + 1
            topComment._showChildren = true;
          }
        }

        parentComment._showReply = false
      })
    },

    // 删除评论
    handleDelete(comment) {
      this.$confirm('确定删除该评论吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: '/shortVideoComment/delete',
          method: 'delete',
          data: { id: comment.id }
        }).then(() => {
          this.$message.success('删除成功')
          if (comment.topReplyCommentId && comment.topReplyCommentId > 0) {
            const parentComment = this.commentList.find(c => c.id === comment.topReplyCommentId)
            if (parentComment && parentComment._children) {
              const idx = parentComment._children.findIndex(c => c.id === comment.id)
              if (idx >= 0) {
                parentComment._children.splice(idx, 1)
                parentComment.childCommentCount = Math.max(0, (parentComment.childCommentCount || 0) - 1)
              }
            }
          } else {
            const idx = this.commentList.findIndex(c => c.id === comment.id)
            if (idx >= 0) {
              this.commentList.splice(idx, 1)
              this.commentTotal = Math.max(0, this.commentTotal - 1)
            }
          }
        })
      }).catch(() => {})
    },

    // 切换回复输入框
    toggleReply(comment) {
      const willShow = !comment._showReply
      console.log('willShow', willShow)
      // 关闭其他打开的回复输入框和顶部评论输入框
      this.showCommentInput = false
      this.commentList.forEach(c => {
        if (c !== comment && c._showReply) {
          this.$set(c, '_showReply', false)
        }
        if (c._children) {
          c._children.forEach(child => {
            if (child !== comment && child._showReply) {
              this.$set(child, '_showReply', false)
            }
          })
        }
      })
      this.$set(comment, '_showReply', willShow)
      if (willShow) {
        const refKey = 'replyInput_' + comment.id
        this.$nextTick(() => {
          console.log('refKey', refKey)
          const inst = this.$refs[refKey]
          console.log('inst', inst)
          if (inst) {
            const target = Array.isArray(inst) ? inst[0] : inst
            if (target && target.view) target.view()
          }
        })
      }
    },

    // 展开/收起子评论
    toggleChildComments(comment) {
      if (comment._showChildren) {
        this.$set(comment, '_showChildren', false)
        return
      }
      this.$set(comment, '_showChildren', true)
      if (comment._children && comment._children.length > 0) return

      this.$set(comment, '_childLoading', true)
      this.$http({
        url: '/shortVideoComment/pageList',
        method: 'post',
        params: {
          currentPage: 1,
          pageSize: 50
        },
        data: {
          videoId: this.video.id,
          topReplyCommentId: comment.id
        }
      }).then((res) => {
        const list = res.data || []
        this.$set(comment, '_children', list.map(item => ({ ...item, _showReply: false })))
      }).finally(() => {
        this.$set(comment, '_childLoading', false)
      })
    },

    // 预览图片
    previewImage(url) {
      this.$store.commit('showFullImageBox', url)
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

    .comment-text-btn {
      width: 100%;
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

  .comment-list {
    flex: 1;
    overflow-y: auto;
    padding: 0 12px;
  }
}

.no-comment {
  text-align: center;
  color: #ccc;
  font-size: 13px;
  padding: 40px 0;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;

  &.sub-comment {
    padding-left: 12px;
    background: #fafafa;
    border-radius: 4px;
    margin-top: 4px;
  }
}

.comment-main {
  display: flex;
  gap: 10px;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;

  .comment-nickname {
    font-size: 13px;
    color: #999;
  }

  .comment-time {
    font-size: 11px;
    color: #ccc;
  }

  .comment-delete {
    font-size: 12px;
    color: #f56c6c;
    cursor: pointer;
    margin-left: auto;

    &:hover {
      text-decoration: underline;
    }
  }
}

.comment-reply-to {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;

  .reply-nickname {
    color: #409eff;
  }
}

.comment-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  word-break: break-all;

  ::v-deep img {
    height: 22px;
    width: 22px;
    vertical-align: middle;
    margin: 0 2px;
  }

  .comment-image {
    max-width: 120px;
    max-height: 120px;
    border-radius: 4px;
    cursor: pointer;
  }
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 6px;

  .action-like {
    font-size: 12px;
    color: #999;
    cursor: default;
    display: flex;
    align-items: center;
    gap: 4px;

    i {
      font-size: 13px;
    }
  }

  .action-reply {
    font-size: 12px;
    color: #999;
    cursor: pointer;

    &:hover {
      color: #409eff;
    }
  }
}

.child-comment-entry {
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
  margin-top: 6px;
  display: inline-block;

  &:hover {
    text-decoration: underline;
  }
}

.child-comments {
  margin-top: 8px;
  padding-left: 8px;
  border-left: 2px solid #f0f0f0;

  .child-loading {
    font-size: 12px;
    color: #999;
    padding: 8px 0;
  }
}

.reply-input-wrapper {
  margin-top: 8px;
  margin-left: 42px;
}

.load-more {
  text-align: center;
  padding: 12px 0;
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
  user-select: none;

  &:hover {
    text-decoration: underline;
  }
}

// ==================== TA的作品 Tab ====================
.works-tab {
  .no-content {
    text-align: center;
    color: #ccc;
    font-size: 14px;
    padding: 60px 0;
  }
}
</style>
