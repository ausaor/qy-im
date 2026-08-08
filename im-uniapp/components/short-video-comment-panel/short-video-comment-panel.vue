<template>
  <view v-if="visible" class="comment-panel">
    <view class="comment-panel-header">
      <view class="comment-panel-close" @click="close">
        <uni-icons type="closeempty" size="26" color="#333"/>
      </view>
      <view class="comment-tabs">
        <text :class="['comment-tab', {active: activeTab === 'comment'}]" @click="selectTab('comment')">
          评论 {{ video.commentCount || 0 }}
        </text>
        <template v-if="isVideoOwner">
          <text :class="['comment-tab', {active: activeTab === 'favorite'}]" @click="selectTab('favorite')">
            收藏 {{ video.favoriteCount || 0 }}
          </text>
          <text :class="['comment-tab', {active: activeTab === 'like'}]" @click="selectTab('like')">
            喜欢 {{ video.likeCount || 0 }}
          </text>
        </template>
      </view>
      <view class="comment-panel-close"/>
    </view>
    <scroll-view class="comment-list" scroll-y @scrolltolower="loadMore">
      <template v-if="activeTab === 'comment'">
      <view v-if="commentLoading && !commentList.length" class="comment-state">加载中...</view>
      <view v-else-if="!commentList.length" class="comment-state">暂无评论，快来抢沙发吧~</view>
      <view v-for="comment in commentList" :key="comment.id" class="comment-item">
        <view class="comment-main">
          <head-image class="comment-avatar" :id="comment.userId" :name="comment.userNickname || '用户'"
                      :url="comment.userAvatar" :size="64"/>
          <view class="comment-body">
            <view>
              <text class="comment-name">{{ comment.userNickname || '用户' }}</text>
              <text v-if="isVideoAuthor(comment.userId)" class="comment-author">作者</text>
              <text class="comment-time">{{ formatCommentTime(comment.createTime) }}</text>
            </view>
            <view v-if="comment.replyToUserId" class="reply-to">回复 @{{ comment.replyToUserNickname || '用户' }}</view>
            <view class="comment-content" @click="replyComment(comment)">
              <rich-text v-if="comment.type === 0" :nodes="commentTextNodes(comment.content)"/>
              <image v-else-if="comment.type === 1" class="comment-image" :src="commentImage(comment.content)"
                     mode="aspectFill" @click.stop="previewCommentImage(comment.content)"/>
              <view v-else-if="comment.type === 5" class="comment-voice"
                    @click.stop="playCommentVoice(comment.content)">{{
                  commentVoice(comment.content).word || '语音台词'
                }}
                <uni-icons type="sound" size="18"/>
              </view>
            </view>
            <view class="comment-actions">
              <view @click="likeComment(comment)">
                <uni-icons :type="isCommentLiked(comment.id) ? 'heart-filled' : 'heart'" size="18"
                           :color="isCommentLiked(comment.id) ? '#f23b54' : '#777'"/>
                <text v-if="comment.likeCount">{{ comment.likeCount }}</text>
              </view>
              <text @click="replyComment(comment)">回复</text>
              <text v-if="comment.isOwner" class="delete-comment" @click="deleteComment(comment)">删除</text>
            </view>
          </view>
        </view>
        <view v-if="comment.childCommentCount > 0" class="child-entry" @click="toggleChildren(comment)">
          {{ comment._showChildren ? '收起回复' : `展开 ${comment.childCommentCount} 条回复` }}
        </view>
        <view v-if="comment._showChildren" class="child-comments">
          <view v-if="comment._childLoading" class="comment-state">加载中...</view>
          <view v-for="child in comment._children" :key="child.id" class="comment-main child-comment">
            <head-image class="comment-avatar" :id="child.userId" :name="child.userNickname || '用户'"
                        :url="child.userAvatar" :size="54"/>
            <view class="comment-body">
              <view>
                <text class="comment-name">{{ child.userNickname || '用户' }}</text>
                <text v-if="isVideoAuthor(child.userId)" class="comment-author">作者</text>
                <text class="comment-time">{{ formatCommentTime(child.createTime) }}</text>
              </view>
              <view v-if="child.replyToUserId" class="reply-to">回复 @{{ child.replyToUserNickname || '用户' }}</view>
              <view class="comment-content" @click="replyComment(child)">
                <rich-text v-if="child.type === 0" :nodes="commentTextNodes(child.content)"/>
                <image v-else-if="child.type === 1" class="comment-image" :src="commentImage(child.content)"
                       mode="aspectFill" @click.stop="previewCommentImage(child.content)"/>
                <view v-else-if="child.type === 5" class="comment-voice" @click.stop="playCommentVoice(child.content)">
                  {{ commentVoice(child.content).word || '语音台词' }}
                  <uni-icons type="sound" size="18"/>
                </view>
              </view>
              <view class="comment-actions">
                <view @click="likeComment(child)">
                  <uni-icons :type="isCommentLiked(child.id) ? 'heart-filled' : 'heart'" size="18"
                             :color="isCommentLiked(child.id) ? '#f23b54' : '#777'"/>
                  <text v-if="child.likeCount">{{ child.likeCount }}</text>
                </view>
                <text @click="replyComment(child)">回复</text>
                <text v-if="child.isOwner" class="delete-comment" @click="deleteComment(child)">删除</text>
              </view>
            </view>
          </view>
          <view v-if="comment._childHasMore" class="child-entry" @click="loadMoreChildren(comment)">
            {{ comment._childLoadingMore ? '加载中...' : '加载更多回复' }}
          </view>
        </view>
      </view>
      <view v-if="commentHasMore" class="load-comments">{{ commentLoadingMore ? '加载中...' : '上拉加载更多' }}</view>
      </template>
      <template v-else>
        <view v-if="userListLoading && !currentUserList.length" class="comment-state">加载中...</view>
        <view v-else-if="!currentUserList.length" class="comment-state">暂无{{ activeTab === 'favorite' ? '收藏' : '喜欢' }}用户</view>
        <view v-for="user in currentUserList" :key="user.id" class="user-item">
          <head-image class="user-avatar" :id="user.userId" :name="user.nickName || '用户'" :url="user.headImage" :size="64"/>
          <text class="user-name">{{ user.nickName || '用户' }}</text>
          <button v-if="!isMineUser(user)" class="follow-button" :class="{followed: isFollowingUser(user)}"
                  :disabled="isFollowActioning(user)" @click.stop="toggleFollowUser(user)">
            {{ isFollowingUser(user) ? '已关注' : '关注' }}
          </button>
        </view>
        <view v-if="currentUserHasMore" class="load-comments">{{ userListLoadingMore ? '加载中...' : '上拉加载更多' }}</view>
      </template>
    </scroll-view>
    <view v-if="activeTab === 'comment'" class="comment-input-trigger" @click="openCommentInput">
      <text>{{ commentPlaceholder }}</text>
      <view class="comment-character-actions" @click.stop>
        <view v-if="!commentForm.characterId" @click="showGroupTemplatesPopup">
          <uni-icons type="gear" size="22" color="#777"/>
        </view>
        <template v-else>
          <head-image class="comment-character-avatar" :id="commentForm.characterId" :url="commentForm.avatar"
                      :name="commentForm.nickName" :size="48" @click="showGroupTemplatesPopup"/>
          <uni-icons type="closeempty" size="20" color="#999" @click="clearCommentCharacter"/>
        </template>
      </view>
    </view>
    <comment-box ref="commentBox" :comment-placeholder="commentPlaceholder" :character-id="commentForm.characterId"
                 @submit="submitComment" @send-img="sendCommentImage" @send-word="sendCommentWord"/>
    <group-template-list ref="groupTemplateListRef" :group-templates="groupTemplates" @confirm="chooseGroupTemplate"/>
    <character-list ref="characterListRef" :characters="characters" @confirm="chooseCharacter"
                    @more="moreCharacterAvatars"/>
    <character-avatar-list ref="characterAvatarListRef" :character-avatars="characterAvatars"
                           @confirm="chooseCharacterAvatar"/>
  </view>
</template>

<script>
import CommentBox from '../comment-box/comment-box.vue'
import HeadImage from '../head-image/head-image.vue'
import GroupTemplateList from '../group-template-list/group-template-list.vue'
import CharacterList from '../character-list/character-list.vue'
import CharacterAvatarList from '../character-avatar-list/character-avatar-list.vue'


export default {
  name: 'short-video-comment-panel',
  components: {CommentBox, HeadImage, GroupTemplateList, CharacterList, CharacterAvatarList},
  props: {visible: Boolean, video: {type: Object, default: () => ({})}},
  emits: ['close', 'comment-count-change'],
  data: () => ({
    commentList: [],
    commentPageNo: 1,
    commentTotal: 0,
    commentLoading: false,
    commentLoadingMore: false,
    activeTab: 'comment',
    favoriteUserList: [],
    favoriteUserPageNo: 1,
    favoriteUserTotal: 0,
    likeUserList: [],
    likeUserPageNo: 1,
    likeUserTotal: 0,
    userListLoading: false,
    userListLoadingMore: false,
    followActioningKeys: {},
    commentPlaceholder: '说点什么...',
    replyingComment: null,
    commentActioning: false,
    commentAudio: null,
    groupTemplates: [],
    characters: [],
    characterAvatars: [],
    commentForm: {characterAvatarId: null, characterId: null, nickName: '', avatar: ''}
  }),
  computed: {
    commentHasMore() {
      return this.commentList.length < this.commentTotal
    },
    mine() {
      return this.userStore.userInfo;
    },
    isVideoOwner() {
      return this.mine && this.mine.id != null && this.video && this.video.userId != null
        && String(this.mine.id) === String(this.video.userId)
    },
    currentUserList() {
      return this.activeTab === 'favorite' ? this.favoriteUserList : this.likeUserList
    },
    currentUserTotal() {
      return this.activeTab === 'favorite' ? this.favoriteUserTotal : this.likeUserTotal
    },
    currentUserHasMore() {
      return this.currentUserList.length < this.currentUserTotal
    },
  },
  watch: {
    visible(open) {
      if (open) {
        this.activeTab = 'comment';
        this.resetComments();
        this.resetUserLists();
        this.getCommentCharacter(this.video.id)
      }
    }, 'video.id'(id) {
      if (this.visible) {
        this.activeTab = 'comment';
        this.resetComments();
        this.resetUserLists();
        this.getCommentCharacter(id)
      }
    }
  },
  beforeUnmount() {
    if (this.commentAudio) this.commentAudio.destroy()
  },
  methods: {
    close() {
      this.replyingComment = null;
      this.commentPlaceholder = '说点什么...';
      this.$emit('close')
    },
    resetComments() {
      this.commentList = [];
      this.commentPageNo = 1;
      this.commentTotal = 0;
      this.fetchComments()
    },
    resetUserLists() {
      this.favoriteUserList = [];
      this.favoriteUserPageNo = 1;
      this.favoriteUserTotal = 0;
      this.likeUserList = [];
      this.likeUserPageNo = 1;
      this.likeUserTotal = 0
    },
    selectTab(tab) {
      if (tab !== 'comment' && !this.isVideoOwner) return;
      this.activeTab = tab;
      if (tab !== 'comment' && !this.currentUserList.length) this.fetchUserList(1)
    },
    loadMore() {
      if (this.activeTab === 'comment') this.loadMoreComments();
      else this.loadMoreUserList()
    },
    request(pageNo, data) {
      return this.$http({
        url: '/shortVideoComment/pageList',
        method: 'POST',
        params: {currentPage: pageNo, pageSize: 20},
        data
      })
    }, decorate(comment) {
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
      if (this.commentLoading || !this.video.id) return;
      this.commentLoading = true;
      this.request(1, {videoId: this.video.id}).then(page => {
        this.commentList = (page.data || []).map(this.decorate);
        this.commentTotal = page.total || 0;
        this.commentPageNo = 1
      }).finally(() => {
        this.commentLoading = false
      })
    },
    loadMoreComments() {
      if (this.commentLoadingMore || !this.commentHasMore) return;
      this.commentLoadingMore = true;
      const pageNo = this.commentPageNo + 1;
      this.request(pageNo, {videoId: this.video.id}).then(page => {
        this.commentList.push(...(page.data || []).map(this.decorate));
        this.commentTotal = page.total || this.commentTotal;
        this.commentPageNo = pageNo
      }).finally(() => {
        this.commentLoadingMore = false
      })
    },
    userListConfig() {
      return this.activeTab === 'favorite'
        ? {url: '/shortVideoFavorite/pageShortVideoFavoritesUser', list: 'favoriteUserList', pageNo: 'favoriteUserPageNo', total: 'favoriteUserTotal'}
        : {url: '/shortVideoLike/pageShortVideoLikeUser', list: 'likeUserList', pageNo: 'likeUserPageNo', total: 'likeUserTotal'}
    },
    fetchUserList(pageNo) {
      if (!this.video.id || !this.isVideoOwner || this.userListLoading || this.userListLoadingMore) return;
      const config = this.userListConfig();
      const tab = this.activeTab;
      pageNo === 1 ? this.userListLoading = true : this.userListLoadingMore = true;
      this.$http({
        url: config.url,
        method: 'POST',
        params: {currentPage: pageNo, pageSize: 50},
        data: {videoId: this.video.id}
      }).then(page => {
        if (tab !== this.activeTab) return;
        const users = page.data || [];
        if (pageNo === 1) this[config.list] = users;
        else this[config.list].push(...users);
        this[config.pageNo] = pageNo;
        this[config.total] = page.total || 0
      }).finally(() => {
        this.userListLoading = false;
        this.userListLoadingMore = false;
        if (tab !== this.activeTab && this.activeTab !== 'comment' && !this.currentUserList.length) {
          this.fetchUserList(1)
        }
      })
    },
    loadMoreUserList() {
      if (this.userListLoadingMore || !this.currentUserHasMore) return;
      const config = this.userListConfig();
      this.fetchUserList(this[config.pageNo] + 1)
    },
    isMineUser(user) {
      return this.mine && this.mine.id != null && user && user.userId != null
        && String(this.mine.id) === String(user.userId)
    },
    isFollowingUser(user) {
      return Boolean(user && user.userId != null && this.followStore.isFollow(`${user.userId}:user`))
    },
    isFollowActioning(user) {
      return Boolean(user && this.followActioningKeys[`user:${user.userId}`])
    },
    toggleFollowUser(user) {
      if (!user || !user.userId || this.isMineUser(user) || this.isFollowActioning(user)) return;
      const follow = {targetId: user.userId, type: 'user'};
      const followed = this.isFollowingUser(user);
      const actionKey = `user:${user.userId}`;
      this.followActioningKeys[actionKey] = true;
      this.$http({
        url: followed ? `/follow/cancel?targetId=${user.userId}&type=user` : '/follow/add',
        method: followed ? 'DELETE' : 'POST',
        data: followed ? {} : follow
      }).then(savedFollow => {
        if (followed) this.followStore.removeFollow(follow);
        else this.followStore.addFollow(savedFollow || follow);
        uni.showToast({title: followed ? '已取消关注' : '关注成功', icon: 'none'})
      }).finally(() => {
        delete this.followActioningKeys[actionKey]
      })
    },
    toggleChildren(comment) {
      if (comment._showChildren) {
        comment._showChildren = false;
        return
      }
      comment._showChildren = true;
      if (!comment._children.length) this.fetchChildren(comment, 1)
    },
    fetchChildren(comment, pageNo) {
      if (comment._childLoading || comment._childLoadingMore) return;
      pageNo === 1 ? comment._childLoading = true : comment._childLoadingMore = true;
      this.request(pageNo, {videoId: this.video.id, topReplyCommentId: comment.id}).then(page => {
        const children = page.data || [];
        pageNo === 1 ? comment._children = children : comment._children.push(...children);
        comment._childPageNo = pageNo;
        comment._childTotal = page.total || 0;
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
      if (!this.video.id || this.commentActioning) return;
      this.commentActioning = true;
      const parent = this.replyingComment;
      this.$http({
        url: '/shortVideoComment/add',
        method: 'POST',
        data: {
          videoId: this.video.id,
          content,
          type,
          replyCommentId: parent ? parent.id : null,
          characterId: this.commentForm.characterId,
          avatarId: this.commentForm.characterAvatarId
        }
      }).then(created => {
        if (parent) {
          const top = String(parent.topReplyCommentId || 0) === '0' ? parent : this.commentList.find(item => String(item.id) === String(parent.topReplyCommentId));
          if (top) {
            top._children.push(created);
            top._showChildren = true;
            top.childCommentCount = (top.childCommentCount || 0) + 1
          }
        } else this.commentList.unshift(this.decorate(created));
        this.$emit('comment-count-change', 1);
        uni.showToast({title: '评论成功', icon: 'none'});
        this.$refs.commentBox.cancel();
        this.$refs.commentBox.clear();
        this.replyingComment = null;
        this.commentPlaceholder = '说点什么...'
      }).finally(() => {
        this.commentActioning = false
      })
    },
    likeComment(comment) {
      if (!comment || this.shortVideoStore.isCommentLiked(comment.id)) return;
      this.$http({url: `/shortVideoComment/addCommentLike/${comment.id}`, method: 'POST'}).then(() => {
        this.shortVideoStore.markCommentLiked(comment.id);
        comment.likeCount = (comment.likeCount || 0) + 1
      })
    },
    deleteComment(comment) {
      uni.showModal({
        title: '删除评论', content: '确定删除这条评论吗？', success: ({confirm}) => {
          if (!confirm) return;
          this.$http({url: '/shortVideoComment/delete', method: 'DELETE', data: {id: comment.id}}).then(() => {
            const top = this.commentList.find(item => String(item.id) === String(comment.topReplyCommentId));
            if (top) {
              top._children = top._children.filter(item => String(item.id) !== String(comment.id));
              top.childCommentCount = Math.max(0, (top.childCommentCount || 0) - 1)
            } else {
              this.commentList = this.commentList.filter(item => String(item.id) !== String(comment.id));
              this.commentTotal = Math.max(0, this.commentTotal - 1)
            }
            this.$emit('comment-count-change', -1)
          })
        }
      })
    },
    parse(content) {
      try {
        return JSON.parse(content)
      } catch (_) {
        return {}
      }
    },
    commentImage(content) {
      return this.parse(content).originUrl || ''
    },
    commentVoice(content) {
      return this.parse(content)
    },
    commentTextNodes(content) {
      return this.$emo.transformOriginal(content || '', 'emoji-small').replace(/\n/g, '<br>')
    },
    isCommentLiked(commentId) {
      return this.shortVideoStore.isCommentLiked(commentId)
    },
    isVideoAuthor(userId) {
      return this.video && this.video.userId != null && userId != null
        && String(this.video.userId) === String(userId)
    },
    formatCommentTime(value) {
      if (!value) return ''
      const diff = Date.now() - new Date(value.replace(/-/g, '/')).getTime()
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      return value.slice(5, 16)
    },
    previewCommentImage(content) {
      const url = this.parse(content).originUrl;
      if (url) uni.previewImage({urls: [url]})
    },
    playCommentVoice(content) {
      const voice = this.parse(content).voice;
      if (!voice) return;
      if (this.commentAudio) this.commentAudio.destroy();
      this.commentAudio = uni.createInnerAudioContext();
      this.commentAudio.src = voice;
      this.commentAudio.play()
    },
    getCommentCharacter(id) {
      this.clearCommentCharacter();
      if (!id) return;
      const targetId = String(id);
      this.$http({
        url: `/commentCharacter/getCommentCharacter?targetId=${id}&targetType=shortVideo`,
        method: 'get'
      }).then(res => {
        if (!res || String(this.video.id) !== targetId) return;
        Object.assign(this.commentForm, {
          characterAvatarId: res.avatarId,
          nickName: res.characterName,
          avatar: res.avatar,
          characterId: res.characterId
        })
      })
    },
    clearCommentCharacter() {
      Object.assign(this.commentForm, {characterAvatarId: null, characterId: null, nickName: '', avatar: ''})
    },
    showGroupTemplatesPopup() {
      if (!this.groupTemplates.length) this.queryGroupTemplateList();
      this.$refs.groupTemplateListRef.open()
    },
    async queryGroupTemplateList() {
      this.groupTemplates = await this.$http({url: '/templateGroup/list', method: 'get', params: ''})
    },
    chooseGroupTemplate(group) {
      this.$refs.groupTemplateListRef.cancel();
      if (group) {
        this.queryCharacterList(group.id);
        this.$refs.characterListRef.open()
      }
    },
    async queryCharacterList(id) {
      this.characters = await this.$http({url: `/templateCharacter/list/${id}`, method: 'get'})
    },
    chooseCharacter(character) {
      this.$refs.characterListRef.cancel();
      Object.assign(this.commentForm, {characterId: character.id, nickName: character.name, avatar: character.avatar})
    },
    async moreCharacterAvatars(character) {
      Object.assign(this.commentForm, {characterId: character.id, nickName: character.name, avatar: character.avatar});
      this.characterAvatars = await this.$http({url: `/characterAvatar/list/${character.id}`, method: 'get'});
      this.$refs.characterAvatarListRef.open()
    },
    chooseCharacterAvatar(avatar) {
      this.commentForm.avatar = avatar.avatar;
      this.commentForm.characterAvatarId = avatar.id;
      if (avatar.level !== 0) this.commentForm.nickName = avatar.name
    }
  }
}
</script>

<style scoped lang="scss">
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
  color: #333
}

.comment-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 92rpx;
  flex-shrink: 0;
  border-bottom: 1rpx solid #eee;
  font-size: 30rpx;
  font-weight: 600
}

.comment-panel-close {
  width: 92rpx;
  text-align: center
}

.comment-tabs {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28rpx;
  min-width: 0
}

.comment-tab {
  color: #777;
  font-size: 28rpx;
  font-weight: 400;
  white-space: nowrap
}

.comment-tab.active {
  color: #333;
  font-weight: 600
}

.comment-list {
  flex: 1;
  min-height: 0;
  padding: 0 26rpx;
  box-sizing: border-box
}

.comment-state {
  padding: 70rpx 0;
  color: #999;
  text-align: center;
  font-size: 26rpx
}

.comment-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f1f1f1
}

.user-item {
  display: flex;
  align-items: center;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f1f1f1
}

.user-avatar {
  flex-shrink: 0;
  border-radius: 50%;
  background: #e6e6e6
}

.user-name {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  margin-left: 18rpx;
  color: #333;
  font-size: 28rpx;
  text-overflow: ellipsis;
  white-space: nowrap
}

.follow-button {
  min-width: 112rpx;
  height: 54rpx;
  margin: 0 0 0 18rpx;
  padding: 0 16rpx;
  border: 1rpx solid #f23b54;
  border-radius: 28rpx;
  background: #f23b54;
  color: #fff;
  font-size: 24rpx;
  line-height: 52rpx
}

.follow-button.followed {
  border-color: #b8b8b8;
  background: #b8b8b8;
  color: #fff
}

.comment-main {
  display: flex;
  align-items: flex-start
}

.comment-avatar {
  flex-shrink: 0;
  border-radius: 50%;
  background: #e6e6e6
}

.comment-body {
  min-width: 0;
  flex: 1;
  margin-left: 18rpx
}

.comment-name {
  color: #5b7799;
  font-size: 26rpx;
  font-weight: 500
}

.comment-author {
  display: inline-block;
  margin-left: 10rpx;
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
  background: #f23b54;
  color: #fff;
  font-size: 20rpx;
  line-height: 1.2
}

.comment-time {
  margin-left: 14rpx;
  color: #aaa;
  font-size: 22rpx
}

.reply-to {
  margin-top: 7rpx;
  color: #8a8a8a;
  font-size: 23rpx
}

.comment-content {
  margin-top: 8rpx;
  color: #222;
  font-size: 28rpx;
  line-height: 1.45;
  word-break: break-all
}

.comment-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 8rpx
}

.comment-voice {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 9rpx 16rpx;
  border-radius: 8rpx;
  background: #f1f3f5;
  color: #555;
  font-size: 25rpx
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 28rpx;
  margin-top: 14rpx;
  color: #777;
  font-size: 23rpx
}

.comment-actions view {
  display: flex;
  align-items: center;
  gap: 5rpx
}

.delete-comment {
  color: #e25454
}

.child-entry {
  margin-top: 16rpx;
  color: #6687aa;
  font-size: 24rpx
}

.child-comments {
  margin-top: 10rpx;
  padding: 0 16rpx;
  border-radius: 8rpx;
  background: #f8f8f8
}

.child-comment {
  padding: 18rpx 0
}

.load-comments {
  padding: 24rpx 0 38rpx;
  color: #999;
  text-align: center;
  font-size: 24rpx
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
  font-size: 26rpx
}

.comment-character-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8rpx;
  margin-left: 16rpx
}

.comment-character-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  overflow: hidden
}
</style>
