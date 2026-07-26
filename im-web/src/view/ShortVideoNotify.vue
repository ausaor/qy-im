<template>
  <div class="short-video-notify" v-loading="loading">
    <div class="notify-header">
      <div class="notify-tabs">
        <span
          v-for="tab in tabs"
          :key="tab.value"
          class="notify-tab"
          :class="{ active: activeRecordType === tab.value }"
          @click="changeTab(tab.value)"
        >{{ tab.label }}</span>
      </div>
      <i class="el-icon-refresh refresh-icon" title="刷新" @click="refresh"></i>
    </div>

    <div v-if="notifyList.length" class="notify-list">
      <div v-for="item in notifyList" :key="item.id" class="notify-item">
        <head-image
          :id="item.operateUserId"
          :url="item.operateUserHeadImage"
          :name="item.operateUserNickname || '?'"
          :size="44"
          radius="50%"
          class="user-avatar"
        ></head-image>

        <div class="notify-content">
          <div class="notify-meta">
            <span class="nickname">{{ item.operateUserNickname || '用户' }}</span>
            <span class="create-time">{{ formatTime(item.createTime) }}</span>
          </div>

          <template v-if="item.actionType === 1">
            <div class="action-text">{{item.shortVideo.isOwner ? '评论了你的作品' : '回复了你的评论'}}</div>
            <div v-if="item.shortVideoComment" class="comment-content">
              <div v-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.TEXT" v-html="$emo.transform(item.shortVideoComment.content)"></div>
              <img v-else-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.IMAGE" :src="parseCommentContent(item.shortVideoComment.content).originUrl" class="comment-image" alt="评论图片" />
              <div v-else-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.WORD_VOICE" class="word-voice" @click="playVoice(parseCommentContent(item.shortVideoComment.content))">
                <span class="word">{{ parseCommentContent(item.shortVideoComment.content).word }}</span>
                <span class="icon iconfont icon-xitongxiaoxi"></span>
              </div>
            </div>
            <div v-if="hasReplyComment(item.shortVideoComment)" class="reply-comment">
              <span class="reply-label">回复 @{{ item.shortVideoComment.replyToUserNickname || '用户' }}：</span>
              <div class="comment-content">
                <div v-if="item.shortVideoComment.replyToCommentType === $enums.MESSAGE_TYPE.TEXT" v-html="$emo.transform(item.shortVideoComment.replyToCommentContent)"></div>
                <img v-else-if="item.shortVideoComment.replyToCommentType === $enums.MESSAGE_TYPE.IMAGE" :src="parseCommentContent(item.shortVideoComment.replyToCommentContent).originUrl" class="comment-image" alt="被回复评论图片" />
                <div v-else-if="item.shortVideoComment.replyToCommentType === $enums.MESSAGE_TYPE.WORD_VOICE" class="word-voice" @click="playVoice(parseCommentContent(item.shortVideoComment.replyToCommentContent))">
                  <span class="word">{{ parseCommentContent(item.shortVideoComment.replyToCommentContent).word }}</span>
                  <span class="icon iconfont icon-xitongxiaoxi"></span>
                </div>
              </div>
            </div>
          </template>

          <template v-else-if="item.actionType === 2">
            <div class="action-text">点赞了你的作品</div>
          </template>

          <template v-else-if="item.actionType === 3">
            <div class="action-text">收藏了你的作品</div>
          </template>

          <template v-else-if="item.actionType === 4">
            <div class="action-text">点赞了你的评论</div>
            <div v-if="item.shortVideoComment" class="comment-content">
              <div v-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.TEXT" v-html="$emo.transform(item.shortVideoComment.content)"></div>
              <img v-else-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.IMAGE" :src="parseCommentContent(item.shortVideoComment.content).originUrl" class="comment-image" alt="评论图片" />
              <div v-else-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.WORD_VOICE" class="word-voice" @click="playVoice(parseCommentContent(item.shortVideoComment.content))">
                <span class="word">{{ parseCommentContent(item.shortVideoComment.content).word }}</span>
                <span class="icon iconfont icon-xitongxiaoxi"></span>
              </div>
            </div>
          </template>
        </div>

        <div class="video-cover">
          <img v-if="item.shortVideo && item.shortVideo.coverUrl" :src="item.shortVideo.coverUrl" alt="短视频封面" />
          <i v-else class="el-icon-video-camera"></i>
        </div>
      </div>

      <div v-if="hasMore" class="load-more" @click="loadMore">
        <span v-if="!loadingMore">加载更多</span>
        <span v-else><i class="el-icon-loading"></i> 加载中...</span>
      </div>
      <div v-else class="no-more">没有更多消息了</div>
    </div>

    <div v-else-if="!loading" class="empty-notify">
      <i class="el-icon-bell"></i>
      <p>暂无短视频消息</p>
    </div>
  </div>
</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'

export default {
  name: 'ShortVideoNotify',
  components: {
    HeadImage
  },
  data() {
    return {
      tabs: [
        { label: '全部', value: null },
        { label: '评论', value: 1 },
        { label: '点赞', value: 2 },
        { label: '收藏', value: 3 }
      ],
      activeRecordType: null,
      notifyList: [],
      pageNo: 1,
      pageSize: 20,
      total: 0,
      loading: false,
      loadingMore: false,
      hasMore: true,
      audio: null
    }
  },
  created() {
    this.fetchNotifyList();
    if (this.$store.getters.getShortVideoNotifyCount() > 0) {
      this.readedAllNotify();
      this.$store.commit("clearShortVideoNotify")
    }
  },
  beforeDestroy() {
    if (this.audio) {
      this.audio.pause()
      this.audio = null
    }
  },
  methods: {
    changeTab(recordType) {
      if (this.activeRecordType === recordType) return
      this.activeRecordType = recordType
      this.notifyList = []
      this.pageNo = 1
      this.total = 0
      this.hasMore = true
      this.fetchNotifyList()
    },
    refresh() {
      if (this.$store.getters.getShortVideoNotifyCount() > 0) {
        this.readedAllNotify();
        this.$store.commit("clearShortVideoNotify")
      }
      this.notifyList = []
      this.pageNo = 1
      this.total = 0
      this.hasMore = true
      this.fetchNotifyList()
    },
    fetchNotifyList() {
      if (this.loading || this.loadingMore) return
      this.loading = true
      this.requestNotifyList(1).then((res) => {
        this.notifyList = res.data || []
        this.total = res.total || 0
        this.pageNo = 1
        this.hasMore = this.notifyList.length < this.total
      }).finally(() => {
        this.loading = false
      })
    },
    loadMore() {
      if (this.loadingMore || !this.hasMore) return
      this.loadingMore = true
      const nextPage = this.pageNo + 1
      this.requestNotifyList(nextPage).then((res) => {
        const list = res.data || []
        this.notifyList.push(...list)
        this.total = res.total || 0
        this.pageNo = nextPage
        this.hasMore = this.notifyList.length < this.total
      }).finally(() => {
        this.loadingMore = false
      })
    },
    requestNotifyList(pageNo) {
      const data = {}
      if (this.activeRecordType !== null) {
        data.recordType = this.activeRecordType
      }
      return this.$http({
        url: '/shortVideoNotify/pageList',
        method: 'post',
        params: {
          pageNo,
          pageSize: this.pageSize
        },
        data
      })
    },
    hasReplyComment(comment) {
      return comment && comment.replyToUserId && comment.replyToCommentContent !== null && comment.replyToCommentContent !== undefined
    },
    parseCommentContent(content) {
      try {
        return JSON.parse(content || '{}')
      } catch (e) {
        return {}
      }
    },
    formatTime(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr.replace(/-/g, '/'))
      const diff = Date.now() - date.getTime()
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      if (diff < 172800000) return '昨天'
      return dateStr.slice(0, 16)
    },
    playVoice(word) {
      if (!word || !word.voice) return
      if (this.audio) this.audio.pause()
      this.audio = new Audio(word.voice)
      this.audio.onended = () => {
        this.audio = null
      }
      this.audio.play().catch(() => {
        this.audio = null
      })
    },
    readedAllNotify() {
      this.$http({
        url: '/shortVideoNotify/readedAll',
        method: 'post'
      })
    }
  }
}
</script>

<style scoped lang="scss">
.short-video-notify {
  height: 100%;
  background: #f5f5f5;
  overflow-y: auto;
}

.notify-header {
  position: sticky;
  top: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  padding: 16px 24px 0;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.notify-tabs {
  display: flex;
  gap: 28px;
}

.refresh-icon {
  margin: 0 2px 14px auto;
  padding: 5px;
  color: #666;
  font-size: 18px;
  cursor: pointer;

  &:hover {
    color: #409eff;
  }
}

.notify-tab {
  position: relative;
  padding-bottom: 14px;
  color: #666;
  font-size: 14px;
  cursor: pointer;

  &.active {
    color: #409eff;
    font-weight: 500;

    &::after {
      position: absolute;
      right: 0;
      bottom: 0;
      left: 0;
      height: 2px;
      background: #409eff;
      content: '';
    }
  }
}

.notify-list {
  max-width: 780px;
  margin: 16px auto;
  background: #fff;
  border-radius: 6px;
}

.notify-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;

  &:last-of-type {
    border-bottom: 0;
  }
}

.user-avatar {
  flex: 0 0 auto;
}

.notify-content {
  flex: 1;
  min-width: 0;
}

.notify-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.nickname {
  overflow: hidden;
  max-width: 180px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.create-time,
.action-text,
.reply-label {
  color: #999;
  font-size: 13px;
}

.action-text {
  margin-bottom: 6px;
}

.comment-content {
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;

  ::v-deep img:not(.comment-image) {
    width: 22px;
    height: 22px;
    margin: 0 2px;
    vertical-align: middle;
  }
}

.comment-image {
  display: block;
  max-width: 160px;
  max-height: 120px;
  border-radius: 4px;
  cursor: pointer;
  object-fit: cover;
}

.word-voice {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
  cursor: pointer;

  .word {
    border-bottom: 1px dashed #409eff;
  }
}

.reply-comment {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  margin-top: 8px;
  padding: 8px 10px;
  background: #f7f8fa;
  border-radius: 4px;

  .reply-label {
    flex: 0 0 auto;
  }
}

.video-cover {
  display: flex;
  flex: 0 0 68px;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 90px;
  overflow: hidden;
  color: #fff;
  background: #e5e5e5;
  border-radius: 4px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.load-more,
.no-more {
  padding: 14px;
  color: #999;
  font-size: 13px;
  text-align: center;
}

.load-more {
  color: #409eff;
  cursor: pointer;
}

.empty-notify {
  padding-top: 160px;
  color: #bbb;
  text-align: center;

  i {
    font-size: 42px;
  }

  p {
    margin-top: 12px;
    font-size: 14px;
  }
}
</style>
