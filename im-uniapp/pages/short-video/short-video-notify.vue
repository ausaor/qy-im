<template>
  <view class="page short-video-notify">
    <nav-bar back>短视频消息</nav-bar>

    <scroll-view class="notify-list" scroll-y @scrolltolower="loadMore">
      <view v-if="loading && !notifyList.length" class="state-view">加载中...</view>
      <view v-else-if="!notifyList.length" class="state-view">暂无短视频消息</view>

      <view v-for="item in notifyList" :key="item.id" class="notify-item">
        <head-image
            class="user-avatar"
            :id="item.operateUserId"
            :url="item.operateUserHeadImage"
            :name="item.operateUserNickname || '用户'"
            :size="76"
        />

        <view class="notify-content">
          <view class="notify-meta">
            <text class="nickname">{{ item.operateUserNickname || '用户' }}</text>
            <text class="create-time">{{ formatTime(item.createTime) }}</text>
          </view>

          <view class="action-text">{{ actionText(item) }}</view>
          <template v-if="item.actionType === 1 && item.shortVideoComment">
            <rich-text v-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.TEXT"
                       class="comment-content" :nodes="commentTextNodes(item.shortVideoComment.content)"/>
            <image v-else-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.IMAGE"
                   class="comment-image" :src="commentImage(item.shortVideoComment.content)" mode="aspectFill"
                   @click="previewCommentImage(item.shortVideoComment.content)"/>
            <view v-else-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.WORD_VOICE" class="comment-voice"
                  @click="playVoice(commentVoice(item.shortVideoComment.content))">
              <text>{{ commentVoice(item.shortVideoComment.content).word || '语音台词' }}</text>
              <uni-icons type="sound" size="18" color="#666666"/>
            </view>
            <view v-if="hasReplyComment(item.shortVideoComment)" class="reply-comment">
              <text class="reply-label">回复 @{{ item.shortVideoComment.replyToUserNickname || '用户' }}：</text>
              <rich-text v-if="item.shortVideoComment.replyToCommentType === $enums.MESSAGE_TYPE.TEXT"
                         class="comment-content"
                         :nodes="commentTextNodes(item.shortVideoComment.replyToCommentContent)"/>
              <image v-else-if="item.shortVideoComment.replyToCommentType === $enums.MESSAGE_TYPE.IMAGE"
                     class="comment-image" :src="commentImage(item.shortVideoComment.replyToCommentContent)"
                     mode="aspectFill"
                     @click="previewCommentImage(item.shortVideoComment.replyToCommentContent)"/>
              <view v-else-if="item.shortVideoComment.replyToCommentType === $enums.MESSAGE_TYPE.WORD_VOICE"
                    class="comment-voice"
                    @click="playVoice(commentVoice(item.shortVideoComment.replyToCommentContent))">
                <text>{{ commentVoice(item.shortVideoComment.replyToCommentContent).word || '语音台词' }}</text>
                <uni-icons type="sound" size="18" color="#666666"/>
              </view>
            </view>
          </template>
          <template v-else-if="item.actionType === 4 && item.shortVideoComment">
            <rich-text v-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.TEXT"
                       class="comment-content" :nodes="commentTextNodes(item.shortVideoComment.content)"/>
            <image v-else-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.IMAGE"
                   class="comment-image" :src="commentImage(item.shortVideoComment.content)" mode="aspectFill"
                   @click="previewCommentImage(item.shortVideoComment.content)"/>
            <view v-else-if="item.shortVideoComment.type === $enums.MESSAGE_TYPE.WORD_VOICE" class="comment-voice"
                  @click="playVoice(commentVoice(item.shortVideoComment.content))">
              <text>{{ commentVoice(item.shortVideoComment.content).word || '语音台词' }}</text>
              <uni-icons type="sound" size="18" color="#666666"/>
            </view>
          </template>
        </view>

        <image v-if="item.shortVideo && item.shortVideo.coverUrl" class="video-cover"
               :src="item.shortVideo.coverUrl" mode="aspectFill"/>
        <view v-else class="video-cover cover-placeholder">
          <uni-icons type="videocam" size="24" color="#999999"/>
        </view>
      </view>

      <view v-if="notifyList.length" class="load-state">
        <text v-if="loadingMore">加载中...</text>
        <text v-else-if="hasMore">上拉加载更多</text>
        <text v-else>没有更多消息了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import HeadImage from '../../components/head-image/head-image.vue'
import NavBar from '../../components/nav-bar/nav-bar.vue'

export default {
  name: 'short-video-notify',
  components: {HeadImage, NavBar},
  data() {
    return {
      notifyList: [],
      pageNo: 1,
      pageSize: 20,
      total: 0,
      loading: false,
      loadingMore: false,
      voiceAudio: null
    }
  },
  computed: {
    hasMore() {
      return this.notifyList.length < this.total
    }
  },
  onLoad() {
    this.fetchNotifyList()
    if (this.shortVideoStore.getShortVideoNotifyCount() > 0) {
      this.readAllNotify()
      this.shortVideoStore.clearShortVideoNotify()
    }
  },
  onUnload() {
    if (this.voiceAudio) this.voiceAudio.destroy()
  },
  methods: {
    fetchNotifyList() {
      if (this.loading) return
      this.loading = true
      this.requestNotifyList(1).then((page) => {
        this.notifyList = page.data || []
        this.total = page.total || 0
        this.pageNo = 1
      }).finally(() => {
        this.loading = false
      })
    },
    loadMore() {
      if (this.loading || this.loadingMore || !this.hasMore) return
      this.loadingMore = true
      const nextPage = this.pageNo + 1
      this.requestNotifyList(nextPage).then((page) => {
        this.notifyList.push(...(page.data || []))
        this.total = page.total || this.total
        this.pageNo = nextPage
      }).finally(() => {
        this.loadingMore = false
      })
    },
    requestNotifyList(pageNo) {
      return this.$http({
        url: `/shortVideoNotify/pageList?pageNo=${pageNo}&pageSize=${this.pageSize}`,
        method: 'POST',
        data: {}
      })
    },
    readAllNotify() {
      this.$http({url: '/shortVideoNotify/readedAll', method: 'POST'})
    },
    actionText(item) {
      if (item.actionType === 1) return item.shortVideo && item.shortVideo.isOwner ? '评论了你的作品' : '回复了你的评论'
      if (item.actionType === 2) return '点赞了你的作品'
      if (item.actionType === 3) return '收藏了你的作品'
      if (item.actionType === 4) return '点赞了你的评论'
      return '与你互动'
    },
    hasReplyComment(comment) {
      return comment && comment.replyToUserId && comment.replyToCommentContent !== null && comment.replyToCommentContent !== undefined
    },
    parseCommentContent(content) {
      try {
        return JSON.parse(content || '{}')
      } catch (_) {
        return {}
      }
    },
    commentImage(content) {
      return this.parseCommentContent(content).originUrl || ''
    },
    commentVoice(content) {
      return this.parseCommentContent(content)
    },
    commentTextNodes(content) {
      return this.$emo.transformOriginal(content || '', 'emoji-small').replace(/\n/g, '<br>')
    },
    previewCommentImage(content) {
      const url = this.parseCommentContent(content).originUrl
      if (url) uni.previewImage({urls: [url]})
    },
    playVoice(voice) {
      if (!voice || !voice.voice) return
      if (this.voiceAudio) this.voiceAudio.destroy()
      this.voiceAudio = uni.createInnerAudioContext()
      this.voiceAudio.src = voice.voice
      this.voiceAudio.play()
    },
    formatTime(value) {
      if (!value) return ''
      const time = new Date(value.replace(/-/g, '/')).getTime()
      const diff = Date.now() - time
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      return value.slice(5, 16)
    }
  }
}
</script>

<style scoped lang="scss">
.short-video-notify {
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.notify-list {
  flex: 1;
  box-sizing: border-box;
}

.notify-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 26rpx 24rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.user-avatar {
  flex: none;
}

.notify-content {
  min-width: 0;
  flex: 1;
  color: #333;
  font-size: 28rpx;
}

.notify-meta {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-bottom: 8rpx;
}

.nickname {
  max-width: 240rpx;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.create-time {
  color: #999;
  font-size: 22rpx;
}

.action-text {
  margin-bottom: 8rpx;
  color: #666;
}

.comment-content {
  color: #333;
  line-height: 1.5;
  word-break: break-all;
}

.comment-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
}

.comment-voice {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  padding: 8rpx 14rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
}

.reply-comment {
  margin-top: 10rpx;
  padding: 12rpx;
  background: #f7f7f7;
  border-radius: 8rpx;
}

.reply-label {
  color: #777;
}

.video-cover {
  width: 120rpx;
  height: 160rpx;
  flex: none;
  border-radius: 8rpx;
  background: #eee;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}

.state-view, .load-state {
  padding: 60rpx 24rpx;
  color: #999;
  font-size: 28rpx;
  text-align: center;
}

.load-state {
  padding: 24rpx;
}
</style>
