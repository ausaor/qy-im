<template>
  <view class="short-video-page">
    <swiper
        v-if="videoList.length"
        class="video-swiper"
        :class="{ 'comment-open': showCommentPanel }"
        vertical
        :current="currentIndex"
        :duration="300"
        @change="onVideoChange"
    >
      <swiper-item v-for="(video, index) in videoList" :key="video.id" class="video-slide">
        <view class="video-slide-inner">
          <video
              :id="videoElementId(index)"
              class="video-player"
              :src="video.videoUrl"
              :poster="video.coverUrl"
              :autoplay="index === currentIndex"
              :show-center-play-btn="false"
              :controls="false"
              :loop="true"
              :object-fit="'contain'"
              :enable-progress-gesture="false"
              @click="togglePlay(index)"
          />
          <image v-if="!video.videoUrl && video.coverUrl" class="video-cover" :src="video.coverUrl" mode="aspectFit"/>
          <view v-if="index === currentIndex && !isPlaying" class="play-mask" @click="togglePlay(index)">
            <uni-icons type="videocam" size="48" color="#ffffff"/>
          </view>

          <view class="video-info">
            <text class="author-name">@{{ video.nickName || video.authorName || ('用户' + video.userId) }}</text>
            <text v-if="video.title" class="video-title">{{ video.title }}</text>
            <text v-if="video.description" class="video-description">{{ video.description }}</text>
          </view>

          <view class="video-actions" @click.stop>
            <view class="avatar-action">
              <image v-if="video.headImage" class="avatar" :src="video.headImage" mode="aspectFill"/>
              <view v-else class="avatar avatar-placeholder" :style="avatarPlaceholderStyle(video)">{{
                  avatarText(video)
                }}
              </view>
              <view v-if="!isFollowed(video)" class="follow-mark" @click="toggleFollow(video)">
                <text>+</text>
              </view>
            </view>
            <view class="action-item" @click="toggleLike(video)">
              <uni-icons :type="video.liked ? 'heart-filled' : 'heart'" size="32"
                         :color="video.liked ? '#f23b54' : '#ffffff'"/>
              <text>{{ video.likeCount || 0 }}</text>
            </view>
            <view class="action-item" @click="openComments(video)">
              <uni-icons type="chatbubble" size="30" color="#ffffff"/>
              <text>{{ video.commentCount || 0 }}</text>
            </view>
            <view class="action-item" @click="toggleFavorite(video)">
              <uni-icons :type="video.favorited ? 'star-filled' : 'star'" size="32"
                         :color="video.favorited ? '#ffd23f' : '#ffffff'"/>
              <text>{{ video.favoriteCount || 0 }}</text>
            </view>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <view v-else-if="loading" class="state-view">
      <uni-icons type="spinner-cycle" size="30" color="#ffffff"/>
      <text>加载中...</text>
    </view>
    <view v-else class="state-view">
      <text>暂无推荐视频</text>
    </view>

    <view v-show="!showCommentPanel" class="top-tabs" @click.stop>
      <view class="back-button" @click="goBack">
        <uni-icons type="back" size="25" color="#ffffff"/>
      </view>
      <view class="tab-list">
        <view class="tab-item" @click="switchTab('friend')">
          <text :class="{ active: activeTab === 'friend' }">好友</text>
          <view v-show="friendShortVideoCount > 0" class="notify-dot"/>
        </view>
        <view class="tab-item" @click="switchTab('follow')">
          <text :class="{ active: activeTab === 'follow' }">关注</text>
          <view v-show="followShortVideoCount > 0" class="notify-dot"/>
        </view>
        <text :class="{ active: activeTab === 'star' }" @click="switchTab('star')">星选</text>
        <text :class="{ active: activeTab === 'recom' }" @click="switchTab('recom')">推荐</text>
        <view class="my-tab" @click="goToMyVideos">
          <text>我的</text>
          <view v-show="shortVideoNotifyCount > 0" class="notify-dot"/>
        </view>
      </view>
    </view>
    <view v-if="loadingMore" class="load-more">
      <uni-icons type="spinner-cycle" size="20" color="#ffffff"/>
    </view>

    <view v-if="showCommentPanel" class="comment-panel">
      <view class="comment-panel-header">
        <view class="comment-panel-close" @click="closeComments">
          <uni-icons type="closeempty" size="26" color="#333333"/>
        </view>
        <text class="comment-panel-title">评论 {{ currentVideo.commentCount || 0 }}</text>
        <view class="comment-panel-placeholder"/>
      </view>
      <scroll-view class="comment-list" scroll-y @scrolltolower="loadMoreComments">
        <view v-if="commentLoading && !commentList.length" class="comment-state">加载中...</view>
          <view v-else-if="!commentList.length" class="comment-state">暂无评论，快来抢沙发吧~</view>
        <view v-for="comment in commentList" :key="comment.id" class="comment-item">
          <view class="comment-main">
            <head-image class="comment-avatar" :id="comment.userId" :name="comment.userNickname || '用户'"
                        :url="comment.userAvatar" :size="64"/>
            <view class="comment-body">
              <view class="comment-header">
                <text class="comment-name">{{ comment.userNickname || '用户' }}</text>
                <text class="comment-time">{{ formatCommentTime(comment.createTime) }}</text>
              </view>
              <view v-if="comment.replyToUserId" class="reply-to">回复 @{{
                  comment.replyToUserNickname || '用户'
                }}
              </view>
              <view class="comment-content" @click="replyComment(comment)">
                <rich-text v-if="comment.type === 0" :nodes="commentTextNodes(comment.content)"/>
                <image v-else-if="comment.type === 1" class="comment-image" :src="commentImage(comment.content)"
                       mode="aspectFill" @click.stop="previewCommentImage(comment.content)"/>
                <view v-else-if="comment.type === 5" class="comment-voice"
                      @click.stop="playCommentVoice(comment.content)">
                  <text>{{ commentVoice(comment.content).word || '语音台词' }}</text>
                  <uni-icons type="sound" size="18" color="#666666"/>
                </view>
              </view>
              <view class="comment-actions">
                <view @click="likeComment(comment)">
                  <uni-icons :type="isCommentLiked(comment.id) ? 'heart-filled' : 'heart'" size="18"
                             :color="isCommentLiked(comment.id) ? '#f23b54' : '#777777'"/>
                  <text v-if="comment.likeCount">{{ comment.likeCount }}</text>
                </view>
                <text @click="replyComment(comment)">回复</text>
                <text v-if="comment.isOwner" class="delete-comment" @click="deleteComment(comment)">删除</text>
              </view>
              <view v-if="comment.childCommentCount > 0" class="child-entry" @click="toggleChildren(comment)">
                {{ comment._showChildren ? '收起回复' : `展开 ${comment.childCommentCount} 条回复` }}
              </view>
              <view v-if="comment._showChildren" class="child-comments">
                <view v-if="comment._childLoading" class="comment-state">加载中...</view>
                <view v-for="child in comment._children" :key="child.id" class="comment-item child-comment">
                  <view class="comment-main">
                    <head-image class="comment-avatar child-avatar" :id="child.userId"
                                :name="child.userNickname || '用户'" :url="child.userAvatar" :size="54"/>
                    <view class="comment-body">
                      <view class="comment-header">
                        <text class="comment-name">{{ child.userNickname || '用户' }}</text>
                        <text class="comment-time">{{ formatCommentTime(child.createTime) }}</text>
                      </view>
                      <view v-if="child.replyToUserId" class="reply-to">回复 @{{
                          child.replyToUserNickname || '用户'
                        }}
                      </view>
                      <view class="comment-content" @click="replyComment(child)">
                        <rich-text v-if="child.type === 0" :nodes="commentTextNodes(child.content)"/>
                        <image v-else-if="child.type === 1" class="comment-image" :src="commentImage(child.content)"
                               mode="aspectFill" @click.stop="previewCommentImage(child.content)"/>
                        <view v-else-if="child.type === 5" class="comment-voice"
                              @click.stop="playCommentVoice(child.content)">
                          <text>{{ commentVoice(child.content).word || '语音台词' }}</text>
                          <uni-icons type="sound" size="18" color="#666666"/>
                        </view>
                      </view>
                      <view class="comment-actions">
                        <view @click="likeComment(child)">
                          <uni-icons :type="isCommentLiked(child.id) ? 'heart-filled' : 'heart'" size="18"
                                     :color="isCommentLiked(child.id) ? '#f23b54' : '#777777'"/>
                          <text v-if="child.likeCount">{{ child.likeCount }}</text>
                        </view>
                        <text @click="replyComment(child)">回复</text>
                        <text v-if="child.isOwner" class="delete-comment" @click="deleteComment(child)">删除</text>
                      </view>
                    </view>
                  </view>
                </view>
                <view v-if="comment._childHasMore" class="child-entry" @click="loadMoreChildren(comment)">
                  {{ comment._childLoadingMore ? '加载中...' : '加载更多回复' }}
                </view>
              </view>
            </view>
          </view>
        </view>
        <view v-if="commentHasMore" class="load-comments">{{ commentLoadingMore ? '加载中...' : '上拉加载更多' }}</view>
      </scroll-view>
      <view class="comment-input-trigger" @click="openCommentInput">
        <text>{{ commentPlaceholder }}</text>
        <view class="comment-character-actions" @click.stop>
          <view v-if="!commentForm.characterId" class="comment-character-setting" @click="showGroupTemplatesPopup">
            <uni-icons type="gear" size="22" color="#777777"/>
          </view>
          <template v-else>
            <head-image class="comment-character-avatar" :id="commentForm.characterId" :url="commentForm.avatar"
                        :name="commentForm.nickName" :size="48" @click="showGroupTemplatesPopup"/>
            <view class="comment-character-clear" @click="clearCommentCharacter">
              <uni-icons type="closeempty" size="20" color="#999999"/>
            </view>
          </template>
        </view>
      </view>
    </view>
    <comment-box ref="commentBox" :comment-placeholder="commentPlaceholder" :character-id="commentForm.characterId" @submit="submitComment"
                 @send-img="sendCommentImage" @send-word="sendCommentWord"/>
    <group-template-list ref="groupTemplateListRef" :group-templates="groupTemplates" @confirm="chooseGroupTemplate"></group-template-list>
    <character-list ref="characterListRef" :characters="characters" @confirm="chooseCharacter" @more="moreCharacterAvatars"></character-list>
    <character-avatar-list  ref="characterAvatarListRef" :character-avatars="characterAvatars" @confirm="chooseCharacterAvatar"></character-avatar-list>
  </view>
</template>

<script>
import CommentBox from '../../components/comment-box/comment-box.vue'
import HeadImage from '../../components/head-image/head-image.vue'
import GroupTemplateList from "../../components/group-template-list/group-template-list.vue";
import CharacterList from "../../components/character-list/character-list.vue";
import CharacterAvatarList from "../../components/character-avatar-list/character-avatar-list.vue";

export default {
  components: {CommentBox, HeadImage, GroupTemplateList, CharacterList, CharacterAvatarList},
  data() {
    return {
      videoList: [],
      currentIndex: 0,
      pageNo: 1,
      pageSize: 20,
      total: 0,
      loading: false,
      loadingMore: false,
      isPlaying: true,
      actioning: false,
      pendingPlayVideoIds: [],
      showCommentPanel: false,
      commentList: [],
      commentPageNo: 1,
      commentTotal: 0,
      commentLoading: false,
      commentLoadingMore: false,
      commentPlaceholder: '说点什么...',
      replyingComment: null,
      commentActioning: false,
      commentAudio: null,
      avatarColors: ['#5daa31', '#c7515a', '#e03697', '#85029b', '#c9b455', '#326eb6'],
      activeTab: 'recom', // 值集：recom，follow，friend，my
      groupTemplates: [],
      characters: [],
      characterAvatars: [],
      commentForm: {
        characterAvatarId: null,
        characterId: null,
        nickName: '',
        avatar: '',
      },
    }
  },
  computed: {
    hasMore() {
      return this.videoList.length < this.total
    },
    mine() {
      return this.userStore.userInfo;
    },
    currentVideo() {
      return this.videoList[this.currentIndex] || {}
    },
    commentHasMore() {
      return this.commentList.length < this.commentTotal
    },
    shortVideoNotifyCount() {
      return this.shortVideoStore.getShortVideoNotifyCount()
    },
    friends() {
      return this.friendStore.friends;
    },
    follows() {
      return this.followStore.follows
    },
    shortVideos() {
      return this.shortVideoStore.shortVideoMap;
    },
    friendShortVideoCount() {
      return this.friends.reduce((count, friend) => {
        const videos = this.shortVideos.get(`${friend.id}-user`)
        return count + (videos ? videos.length : 0)
      }, 0)
    },
    followShortVideoCount() {
      return this.follows.reduce((count, follow) => {
        const videos = this.shortVideos.get(`${follow.targetId}-${follow.type}`)
        return count + (videos ? videos.length : 0)
      }, 0)
    }
  },
  created() {
    this.fetchVideos()
  },
  beforeUnmount() {
    this.currentVideoContext().pause()
    if (this.commentAudio) this.commentAudio.destroy()
  },
  methods: {
    videoElementId(index) {
      return `short-video-${index}`
    },
    currentVideoContext() {
      return uni.createVideoContext(this.videoElementId(this.currentIndex), this)
    },
    fetchVideos() {
      if (this.loading || this.loadingMore || (!this.hasMore && this.videoList.length)) return
      const isFirstPage = this.videoList.length === 0
      if (isFirstPage) this.loading = true
      else this.loadingMore = true

      const data = {}
      if (this.activeTab === 'friend') {
        data.section = 'friends'
      } else if (this.activeTab === 'follow') {
        data.section = 'follows'
      } else if (this.activeTab === 'star') {
        data.section = 'allCharacters'
      }

      this.$http({
        url: `/shortVideo/recommend?pageNo=${this.pageNo}&pageSize=${this.pageSize}`,
        method: 'POST',
        data: data
      }).then((page) => {
        const videos = page.data || []
        this.total = page.total || 0
        this.videoList.push(...videos)
        this.pageNo += 1
        if (isFirstPage && videos.length) {
          this.playCurrentVideo()
          this.getCommentCharacter(videos[this.currentIndex].id)
        }
      }).finally(() => {
        this.loading = false
        this.loadingMore = false
      })
    },
    switchTab(tab) {
      if (this.activeTab === tab || this.loading || this.loadingMore) return
      this.currentVideoContext().pause()
      if (tab === 'friend' && this.friendShortVideoCount > 0) {
        this.shortVideoStore.clearFriendShortVideos()
      } else if (tab === 'follow' && this.followShortVideoCount > 0) {
        this.shortVideoStore.clearFollowShortVideos()
      }
      this.activeTab = tab
      this.videoList = []
      this.currentIndex = 0
      this.pageNo = 1
      this.total = 0
      this.isPlaying = true
      this.fetchVideos()
    },
    goToMyVideos() {
      this.currentVideoContext().pause()
      uni.navigateTo({
        url: `/pages/short-video/short-video-my`
      })
    },
    playCurrentVideo() {
      this.$nextTick(() => {
        this.currentVideoContext().play()
        this.isPlaying = true
        this.recordPlayCount()
      })
    },
    recordPlayCount() {
      const video = this.videoList[this.currentIndex]
      if (!video || !video.id) return
      const videoId = video.id
      // 同一视频在本次应用运行中仅成功记录一次，接口请求中的视频也不重复提交。
      if (this.pendingPlayVideoIds.includes(videoId) || this.shortVideoStore.hasPlayed(videoId)) return

      this.pendingPlayVideoIds.push(videoId)
      this.$http({
        url: `/shortVideo/addPlayCount/${videoId}`,
        method: 'POST'
      }).then(() => {
        this.shortVideoStore.markPlayed(videoId)
      }).finally(() => {
        const index = this.pendingPlayVideoIds.indexOf(videoId)
        if (index !== -1) this.pendingPlayVideoIds.splice(index, 1)
      })
    },
    onVideoChange(event) {
      const nextIndex = event.detail.current
      if (nextIndex === this.currentIndex) return
      this.currentVideoContext().pause()
      this.currentIndex = nextIndex
      this.playCurrentVideo()
      this.getCommentCharacter(this.currentVideo.id)
      if (nextIndex >= this.videoList.length - 3 && this.hasMore) this.fetchVideos()
    },
    togglePlay(index) {
      if (index !== this.currentIndex) return
      const context = this.currentVideoContext()
      if (this.isPlaying) context.pause()
      else {
        context.play()
        this.recordPlayCount()
      }
      this.isPlaying = !this.isPlaying
    },
    toggleLike(video) {
      if (!video || !video.id || this.actioning) return
      this.actioning = true
      const liked = video.liked
      this.$http({
        url: liked ? '/shortVideoLike/delete' : '/shortVideoLike/add',
        method: liked ? 'DELETE' : 'POST',
        data: {videoId: video.id}
      }).then(() => {
        video.liked = !liked
        video.likeCount = liked ? Math.max(0, (video.likeCount || 0) - 1) : (video.likeCount || 0) + 1
      }).finally(() => {
        this.actioning = false
      })
    },
    toggleFavorite(video) {
      if (!video || !video.id || this.actioning) return
      this.actioning = true
      const favorited = video.favorited
      this.$http({
        url: favorited ? '/shortVideoFavorite/delete' : '/shortVideoFavorite/add',
        method: favorited ? 'DELETE' : 'POST',
        data: {videoId: video.id}
      }).then(() => {
        video.favorited = !favorited
        video.favoriteCount = favorited ? Math.max(0, (video.favoriteCount || 0) - 1) : (video.favoriteCount || 0) + 1
      }).finally(() => {
        this.actioning = false
      })
    },
    openComments(video) {
      if (!video || !video.id) return
      this.showCommentPanel = true
      this.resetComments()
    },
    closeComments() {
      this.showCommentPanel = false
      this.replyingComment = null
      this.commentPlaceholder = '说点什么...'
    },
    resetComments() {
      this.commentList = []
      this.commentPageNo = 1
      this.commentTotal = 0
      this.fetchComments()
    },
    commentRequest(pageNo, data) {
      return this.$http({
        url: '/shortVideoComment/pageList',
        method: 'POST',
        params: {currentPage: pageNo, pageSize: 20},
        data
      })
    },
    decorateComment(comment) {
      return {
        ...comment,
        _showChildren: false,
        _children: [],
        _childPageNo: 1,
        _childTotal: 0,
        _childLoading: false,
        _childLoadingMore: false,
        _childHasMore: false
      }
    },
    fetchComments() {
      if (this.commentLoading || !this.currentVideo.id) return
      this.commentLoading = true
      this.commentRequest(1, {videoId: this.currentVideo.id}).then((page) => {
        this.commentList = (page.data || []).map(this.decorateComment)
        this.commentTotal = page.total || 0
        this.commentPageNo = 1
      }).finally(() => {
        this.commentLoading = false
      })
    },
    loadMoreComments() {
      if (this.commentLoadingMore || !this.commentHasMore) return
      this.commentLoadingMore = true
      const nextPage = this.commentPageNo + 1
      this.commentRequest(nextPage, {videoId: this.currentVideo.id}).then((page) => {
        this.commentList.push(...(page.data || []).map(this.decorateComment))
        this.commentTotal = page.total || this.commentTotal
        this.commentPageNo = nextPage
      }).finally(() => {
        this.commentLoadingMore = false
      })
    },
    toggleChildren(comment) {
      if (comment._showChildren) {
        comment._showChildren = false;
        return
      }
      comment._showChildren = true
      if (!comment._children.length) this.fetchChildren(comment, 1)
    },
    fetchChildren(comment, pageNo) {
      if (comment._childLoading || comment._childLoadingMore) return
      if (pageNo === 1) comment._childLoading = true
      else comment._childLoadingMore = true
      this.commentRequest(pageNo, {videoId: this.currentVideo.id, topReplyCommentId: comment.id}).then((page) => {
        const children = page.data || []
        if (pageNo === 1) comment._children = children
        else comment._children.push(...children)
        comment._childPageNo = pageNo
        comment._childTotal = page.total || 0
        comment._childHasMore = comment._children.length < comment._childTotal
      }).finally(() => {
        comment._childLoading = false;
        comment._childLoadingMore = false
      })
    },
    loadMoreChildren(comment) {
      if (comment._childHasMore) this.fetchChildren(comment, comment._childPageNo + 1)
    },
    openCommentInput() {
      this.replyingComment = null;
      this.commentPlaceholder = '说点什么...';
      this.$refs.commentBox.open()
    },
    replyComment(comment) {
      this.replyingComment = comment;
      this.commentPlaceholder = `回复 ${comment.userNickname || '用户'}...`;
      this.$refs.commentBox.open()
    },
    submitComment(content) {
      this.addComment(content, 0)
    },
    sendCommentImage(file) {
      if (file) this.addComment(JSON.stringify({originUrl: file.url, name: file.name}), 1)
    },
    sendCommentWord(word) {
      if (word) this.addComment(JSON.stringify({
        id: word.id,
        templateGroupId: word.templateGroupId,
        characterId: word.characterId,
        characterName: word.characterName,
        word: word.word,
        voice: word.voice
      }), 5)
    },
    addComment(content, type) {
      if (!this.currentVideo.id || this.commentActioning) return
      this.commentActioning = true
      const parent = this.replyingComment
      this.$http({
        url: '/shortVideoComment/add',
        method: 'POST',
        data: {
          videoId: this.currentVideo.id, content, type, replyCommentId: parent ? parent.id : null,
          characterId: this.commentForm.characterId,
          avatarId: this.commentForm.characterAvatarId
        }
      }).then((created) => {
        if (parent) {
          const top = String(parent.topReplyCommentId || 0) === '0' ? parent : this.commentList.find(item => String(item.id) === String(parent.topReplyCommentId))
          if (top) {
            top._children.push(created);
            top._showChildren = true;
            top.childCommentCount = (top.childCommentCount || 0) + 1
          }
        } else {
          this.commentList.unshift(this.decorateComment(created))
        }
        this.currentVideo.commentCount = (this.currentVideo.commentCount || 0) + 1
        uni.showToast({title: '评论成功', icon: 'none'})
        this.$refs.commentBox.cancel()
        this.$refs.commentBox.clear()
        this.replyingComment = null
        this.commentPlaceholder = '说点什么...'
      }).finally(() => {
        this.commentActioning = false
      })
    },
    isCommentLiked(commentId) {
      return this.shortVideoStore.isCommentLiked(commentId)
    },
    likeComment(comment) {
      if (!comment || this.isCommentLiked(comment.id)) return
      this.$http({url: `/shortVideoComment/addCommentLike/${comment.id}`, method: 'POST'}).then(() => {
        this.shortVideoStore.markCommentLiked(comment.id);
        comment.likeCount = (comment.likeCount || 0) + 1
      })
    },
    deleteComment(comment) {
      uni.showModal({
        title: '删除评论', content: '确定删除这条评论吗？', success: ({confirm}) => {
          if (!confirm) return
          this.$http({url: '/shortVideoComment/delete', method: 'DELETE', data: {id: comment.id}}).then(() => {
            const top = this.commentList.find(item => String(item.id) === String(comment.topReplyCommentId))
            if (top) {
              top._children = top._children.filter(item => String(item.id) !== String(comment.id));
              top.childCommentCount = Math.max(0, (top.childCommentCount || 0) - 1)
            } else {
              this.commentList = this.commentList.filter(item => String(item.id) !== String(comment.id));
              this.commentTotal = Math.max(0, this.commentTotal - 1)
            }
            this.currentVideo.commentCount = Math.max(0, (this.currentVideo.commentCount || 0) - 1)
          })
        }
      })
    },
    parseCommentContent(content) {
      try {
        return JSON.parse(content)
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
      const url = this.commentImage(content);
      if (url) uni.previewImage({urls: [url]})
    },
    playCommentVoice(content) {
      const voice = this.commentVoice(content).voice;
      if (!voice) return;
      if (this.commentAudio) this.commentAudio.destroy();
      this.commentAudio = uni.createInnerAudioContext();
      this.commentAudio.src = voice;
      this.commentAudio.play()
    },
    formatCommentTime(value) {
      if (!value) return '';
      const time = new Date(value.replace(/-/g, '/')).getTime();
      const diff = Date.now() - time;
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      return value.slice(5, 16)
    },
    isFollowed(video) {
      return Boolean(video && video.objectId && video.type && this.followStore.isFollow(`${video.objectId}:${video.type}`))
    },
    toggleFollow(video) {
      if (!video || !video.objectId || !video.type || this.actioning) return
      this.actioning = true
      const follow = {targetId: video.objectId, type: video.type}
      const followed = this.isFollowed(video)
      this.$http({
        url: followed ? `/follow/cancel?targetId=${video.objectId}&type=${video.type}` : '/follow/add',
        method: followed ? 'DELETE' : 'POST',
        data: followed ? {} : follow
      }).then((savedFollow) => {
        if (followed) this.followStore.removeFollow(follow)
        else this.followStore.addFollow(savedFollow || follow)
      }).finally(() => {
        this.actioning = false
      })
    },
    avatarText(video) {
      return (video.nickName || video.authorName || '?').slice(0, 1).toUpperCase()
    },
    avatarPlaceholderStyle(video) {
      const name = video.nickName || video.authorName || `用户${video.userId || ''}`
      let hash = 0
      for (let index = 0; index < name.length; index += 1) {
        hash += name.charCodeAt(index)
      }
      return {backgroundColor: this.avatarColors[hash % this.avatarColors.length]}
    },
    goBack() {
      uni.navigateBack()
    },
    getCommentCharacter(videoId) {
      this.clearCommentCharacter()
      if (!videoId) return
      const targetVideoId = String(videoId)
      this.$http({
        url: `/commentCharacter/getCommentCharacter?targetId=${videoId}&targetType=shortVideo`,
        method: 'get',
      }).then((res) => {
        if (!res || String(this.currentVideo.id) !== targetVideoId) return
        this.commentForm.characterAvatarId = res.avatarId;
        this.commentForm.nickName = res.characterName;
        this.commentForm.avatar = res.avatar;
        this.commentForm.characterId = res.characterId;
      })
    },
    clearCommentCharacter() {
      this.commentForm.characterAvatarId = null;
      this.commentForm.characterId = null;
      this.commentForm.nickName = '';
      this.commentForm.avatar = '';
    },
    showGroupTemplatesPopup() {
      if (!this.groupTemplates || this.groupTemplates.length === 0) {
        this.queryGroupTemplateList();
      }
      this.$refs.groupTemplateListRef.open();
    },
    async queryGroupTemplateList() {
      await this.$http({
        url: "/templateGroup/list",
        method: 'get',
        params: ''
      }).then(data => {
        this.groupTemplates = data;
      })
    },
    chooseGroupTemplate(groupTemplate) {
      this.$refs.groupTemplateListRef.cancel();
      if (groupTemplate) {
        this.queryCharacterList(groupTemplate.id);
        this.$refs.characterListRef.open();
      }
    },
    async queryCharacterList(templateGroupId) {
      await this.$http({
        url: `/templateCharacter/list/${templateGroupId}`,
        method: 'get'
      }).then(result => {
        this.characters = result;
      });
    },
    chooseCharacter(character) {
      this.$refs.characterListRef.cancel();
      this.commentForm.characterId = character.id;
      this.commentForm.nickName = character.name;
      this.commentForm.avatar = character.avatar;
    },
    async moreCharacterAvatars(character) {
      this.commentForm.characterId = character.id;
      this.commentForm.nickName = character.name;
      this.commentForm.avatar = character.avatar;
      await this.queryCharacterAvatars(character.id);
      this.$refs.characterAvatarListRef.open();
    },
    async queryCharacterAvatars(templateCharacterId) {
      await this.$http({
        url: `/characterAvatar/list/${templateCharacterId}`,
        method: 'get'
      }).then((data) => {
        this.characterAvatars = data;
      });
    },
    chooseCharacterAvatar(characterAvatar) {
      this.commentForm.avatar = characterAvatar.avatar;
      this.commentForm.characterAvatarId = characterAvatar.id;
      if (characterAvatar.level !== 0) {
        this.commentForm.nickName = characterAvatar.name;
      }
    },
  }
}
</script>

<style scoped lang="scss">
.short-video-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #000;
}

.video-swiper,
.video-slide,
.video-slide-inner {
  width: 100%;
  height: 100%;
}

.video-swiper.comment-open {
  height: 36vh;
}

.video-swiper.comment-open .video-info {
  right: 125rpx;
  bottom: 24rpx;
}

.video-swiper.comment-open .video-actions {
  right: 14rpx;
  bottom: 20rpx;
  gap: 12rpx;
  transform: scale(0.82);
  transform-origin: right bottom;
}

.video-slide-inner {
  position: relative;
  background: #000;
}

.video-player,
.video-cover {
  width: 100%;
  height: 100%;
}

.video-cover {
  position: absolute;
  top: 0;
  left: 0;
}

.play-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.18);
}

.top-tabs {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  box-sizing: content-box;
  width: 100%;
  height: 130rpx;
  padding-top: env(safe-area-inset-top);
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.45), transparent);
}

.back-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 92rpx;
}

.tab-list {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: space-around;
  padding-right: 20rpx;
  font-size: 30rpx;
  color: rgba(255, 255, 255, 0.75);
}

.my-tab {
  position: relative;
}

.tab-item {
  position: relative;
}

.notify-dot {
  position: absolute;
  top: -8rpx;
  right: -12rpx;
  width: 12rpx;
  height: 12rpx;
  border: 2rpx solid rgba(0, 0, 0, 0.45);
  border-radius: 50%;
  background: #f0445d;
}

.tab-list .active {
  position: relative;
  color: #fff;
  font-weight: 600;
}

.tab-list .active::after {
  position: absolute;
  bottom: -14rpx;
  left: 50%;
  width: 30rpx;
  height: 5rpx;
  border-radius: 4rpx;
  background: #fff;
  content: '';
  transform: translateX(-50%);
}

.video-info {
  position: absolute;
  right: 140rpx;
  bottom: calc(54rpx + env(safe-area-inset-bottom));
  left: 30rpx;
  display: flex;
  flex-direction: column;
  color: #fff;
  text-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.8);
}

.author-name {
  margin-bottom: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
}

.video-title,
.video-description {
  margin-top: 8rpx;
  font-size: 26rpx;
  line-height: 1.45;
}

.video-actions {
  position: absolute;
  right: 24rpx;
  bottom: calc(48rpx + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}

.avatar-action {
  position: relative;
  width: 82rpx;
  height: 96rpx;
}

.avatar {
  width: 76rpx;
  height: 76rpx;
  border: 3rpx solid #fff;
  border-radius: 50%;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 30rpx;
}

.follow-mark {
  position: absolute;
  bottom: 0;
  left: 19rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38rpx;
  height: 38rpx;
  border: 3rpx solid #fff;
  border-radius: 50%;
  background: #f23b54;
  color: #fff;
  font-size: 32rpx;
  line-height: 32rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 82rpx;
  color: #fff;
  font-size: 23rpx;
  font-weight: 500;
  text-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.8);
}

.action-item text {
  margin-top: 5rpx;
}

.state-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  width: 100%;
  height: 100%;
  color: #fff;
  font-size: 28rpx;
}

.load-more {
  position: absolute;
  right: 30rpx;
  bottom: calc(24rpx + env(safe-area-inset-bottom));
  z-index: 6;
}

.comment-panel {
  position: absolute;
  bottom: 0;
  left: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 64vh;
  border-radius: 28rpx 28rpx 0 0;
  background: #fff;
  color: #333;
  animation: comment-slide-up 0.25s ease-out;
}

@keyframes comment-slide-up {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.comment-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 92rpx;
  flex-shrink: 0;
  border-bottom: 1rpx solid #eee;
}

.comment-panel-title {
  font-size: 30rpx;
  font-weight: 600;
}

.comment-panel-close, .comment-panel-placeholder {
  width: 92rpx;
  text-align: center;
}

.comment-list {
  flex: 1;
  min-height: 0;
  padding: 0 26rpx;
  box-sizing: border-box;
}

.comment-state {
  padding: 70rpx 0;
  color: #999;
  text-align: center;
  font-size: 26rpx;
}

.comment-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f1f1f1;
}

.comment-main {
  display: flex;
  align-items: flex-start;
}

.comment-avatar {
  width: 64rpx;
  height: 64rpx;
  flex-shrink: 0;
  border-radius: 50%;
  background: #e6e6e6;
}

.comment-avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #7ca2cd;
  font-size: 26rpx;
}

.comment-body {
  min-width: 0;
  flex: 1;
  margin-left: 18rpx;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 14rpx;
  min-height: 32rpx;
}

.comment-name {
  color: #5b7799;
  font-size: 26rpx;
  font-weight: 500;
}

.comment-time {
  color: #aaa;
  font-size: 22rpx;
}

.reply-to {
  margin-top: 7rpx;
  color: #8a8a8a;
  font-size: 23rpx;
}

.comment-content {
  margin-top: 8rpx;
  color: #222;
  font-size: 28rpx;
  line-height: 1.45;
  word-break: break-all;
}

.comment-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 8rpx;
}

.comment-voice {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 9rpx 16rpx;
  border-radius: 8rpx;
  background: #f1f3f5;
  color: #555;
  font-size: 25rpx;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 28rpx;
  margin-top: 14rpx;
  color: #777;
  font-size: 23rpx;
}

.comment-actions view {
  display: flex;
  align-items: center;
  gap: 5rpx;
}

.delete-comment {
  color: #e25454;
}

.child-entry {
  margin-top: 16rpx;
  color: #6687aa;
  font-size: 24rpx;
}

.child-comments {
  margin-top: 10rpx;
  padding: 0 16rpx;
  border-radius: 8rpx;
  background: #f8f8f8;
}

.child-comment {
  padding: 18rpx 0;
}

.child-avatar {
  width: 54rpx;
  height: 54rpx;
}

.load-comments {
  padding: 24rpx 0 38rpx;
  color: #999;
  text-align: center;
  font-size: 24rpx;
}

.comment-input-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 80rpx;
  margin: 14rpx 24rpx calc(14rpx + env(safe-area-inset-bottom));
  padding: 0 24rpx;
  border-radius: 40rpx;
  background: #f4f5f6;
  color: #999;
  font-size: 26rpx;
}

.comment-character-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  margin-left: 16rpx;
}

.comment-character-setting,
.comment-character-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48rpx;
  height: 48rpx;
}

.comment-character-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  overflow: hidden;
}

.comment-character-clear {
  margin-left: 8rpx;
}
</style>
