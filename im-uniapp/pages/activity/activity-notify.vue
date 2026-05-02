<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-left" @tap="goBack">
        <uni-icons class="back-icon" type="back" size="24"/>
      </view>
      <view class="header-title">空间消息</view>
      <view class="header-right">
        <uni-icons class="back-icon" type="eye" size="24"/>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
        class="message-list"
        scroll-y="true"
        refresher-enabled="true"
    >
      <view class="message-item" v-for="(item, index) in messageList" :key="index">
        <!-- 用户信息 -->
        <view class="user-info">
          <head-image :url="item.avatar" :name="item.nickname" size="small" :id="item.commentUserId"></head-image>
          <view class="user-meta">
            <text class="username">{{ item.nickname }}</text>
            <text class="time">{{ item.createTime }}</text>
          </view>
          <view class="reply-btn" v-if="item.actionType===1" @tap="replyMessage(item)">
            <text>回复</text>
          </view>
        </view>

        <!-- 消息内容 -->
        <view class="message-content">
          <rich-text class="content-text" v-if="item.actionType===1 && item.talkComment.type === $enums.MESSAGE_TYPE.TEXT"
                     :nodes="nodesText(item.talkComment.content)"></rich-text>
          <view class="content-text" v-if="item.actionType===1 && item.talkComment.type === $enums.MESSAGE_TYPE.IMAGE">
            <image class="comment-image" :src="JSON.parse(item.talkComment.content).originUrl" mode="aspectFill"></image>
          </view>
          <view class="content-text" v-if="item.actionType===1 && item.talkComment.type === $enums.MESSAGE_TYPE.WORD_VOICE">
            <text>{{JSON.parse(item.talkComment.content).word}}</text>
            <svg-icon :icon-class="'xitongxiaoxi'" @click.stop="playVoice(JSON.parse(item.talkComment.content))" />
          </view>
          <!-- 互动信息 -->
          <view class="interaction-info" v-if="item.actionType===2">
            <view class="like-info">
              <text class="like-icon">👍</text>
              <text class="like-text">赞了我</text>
            </view>
          </view>

          <!-- 图片内容 -->
          <view class="media-wrapper">
            <view class="media-section" v-if="item.talk.fileList && item.talk.fileList.length > 0">
              <image v-if="item.talk.fileList[0].fileType === 1" class="content-image" :src="item.talk.fileList[0].url" mode="aspectFill"></image>
              <image v-if="item.talk.fileList[0].fileType === 2" class="content-image" :src="item.talk.fileList[0].coverUrl" mode="aspectFill"></image>
              <svg-icon v-if="item.talk.fileList[0].fileType === 3" :icon-class="'yinpin'" class="media-icon"></svg-icon>
              <text v-if="item.talk.fileList[0].fileType === 2" class="play-icon iconfont icon-play"></text>
            </view>
            <view class="image-caption">
              <text class="caption-prefix">{{item.talk.nickName}}：</text>
              <rich-text class="caption-text" :nodes="nodesText(item.talk.content)"></rich-text>
            </view>
          </view>

          <!-- 回复内容 -->
          <view class="replies" v-if="item.replyTalkComment && item.replyTalkComment.length > 0">
            <view class="reply-item" v-for="(reply, rIndex) in item.replyTalkComment" :key="rIndex">
              <text class="reply-username">{{ reply.userNickname }}</text>
              <text v-if="reply.replyCommentId" style="margin: 0 5rpx;color: #1890ff;font-size: 28rpx;">回复</text>
              <text class="reply-username" v-if="reply.replyCommentId">{{ reply.replyUserNickname }}</text>
              <text>：</text>
              <rich-text v-if="reply.type === $enums.MESSAGE_TYPE.TEXT"
                         class="reply-content" :nodes="nodesText(reply.content)"></rich-text>

              <image v-if="reply.type === $enums.MESSAGE_TYPE.IMAGE"
                     class="reply-image" :src="JSON.parse(reply.content).originUrl" mode="aspectFill"></image>
              <view v-if="reply.type === $enums.MESSAGE_TYPE.WORD_VOICE" class="reply-content">
                <text>{{JSON.parse(reply.content).word}}</text>
                <svg-icon :icon-class="'xitongxiaoxi'" @click.stop="playVoice(JSON.parse(reply.content))" />
              </view>
            </view>
          </view>

          <!-- 回复输入框 -->
          <view class="reply-input-box" v-if="item.actionType===1">
            <text class="reply-to" @click="replyMessage(item)">回复{{ item.nickname }}:</text>
          </view>
        </view>
      </view>

      <!-- 分页组件 -->
      <pagination 
        :total-page="page.totalPage" 
        :page-no="page.pageNo"
        @loadMore="onLoadMore">
      </pagination>
    </scroll-view>
    <comment-box  ref="commentBox" @submit="submitComment" @sendWord="sendCommentWord" @sendEmo="sendCommentCharacterEmo" @sendImg="sendCommentImg" :comment-placeholder="commentPlaceholder"
                  :character-id="curMessage?.talk?.commentCharacterId"></comment-box>
  </view>
</template>

<script>
import CommentBox from "../../components/comment-box/comment-box.vue";
import HeadImage from "../../components/head-image/head-image.vue";
import SvgIcon from "../../components/svg-icon/svg-icon.vue";
import Pagination from "../../components/pagination/pagination.vue";

export default {
  components: {SvgIcon, HeadImage, CommentBox, Pagination},
  data() {
    return {
      category: null,
      groupId: null,
      regionCode: null,
      page: {
        pageNo: 1,
        pageSize: 10,
        totalPage: 0,
      },
      commentPlaceholder:  '说点什么...',
      curMessage: {},
      messageList: [],
      wordAudioContext: null,
      playingVoice: null,
      voicePlayState: 'STOP', // 'PLAYING', 'PAUSE', 'STOP'
    }
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    onLoadMore(callback) {
      if (this.page.pageNo >= this.page.totalPage) {
        // 没有更多数据
        uni.showToast({
          title: '已经到底了',
          icon: 'none'
        })
        callback && callback();
        return
      }

      this.page.pageNo += 1;
      this.queryTalkNotify().finally(() => {
        callback && callback();
      });
    },
    replyMessage(item) {
      this.curMessage = item;
      this.commentPlaceholder = `回复${item.nickname}：`;
      this.$refs.commentBox.open();
    },
    queryTalkNotify() {
      let params = {
        category: this.category,
        groupId: this.groupId,
        regionCode: this.regionCode
      };

      return this.$http({
        url: `/talk-notify/pageQueryTalkNotify?pageNo=${this.page.pageNo}&pageSize=${this.page.pageSize}`,
        method: 'post',
        data: params
      }).then((data) => {
        if (this.page.pageNo === 1) {
          this.messageList = data.data;
        } else {
          this.messageList.push(...data.data);
        }
        this.page.totalPage = Math.ceil(data.total / this.page.pageSize);
      }).finally(() => {

      })
    },
    nodesText(content) {
      let color = '';
      let text = this.$url.replaceURLWithHTMLLinks(content, color)
      return this.$emo.transform(text, 'emoji-small').replace(/\n/g, '<br>');
    },
    submitComment(sendText) {
      let talk = this.curMessage.talk;
      let params = {
        talkId: talk.id,
        content: sendText,
        userNickname: talk.commentCharacterName,
        characterId: talk.commentCharacterId,
        avatarId: talk.commentCharacterAvatarId,
        userAvatar: talk.commentCharacterAvatar,
        replyCommentId: this.curMessage.commentId,
        type: this.$enums.MESSAGE_TYPE.TEXT
      }
      this.$http({
        url: "/talk/addTalkComment",
        method: 'post',
        data: params
      }).then((data) => {
        if (this.curMessage.replyTalkComment && this.curMessage.replyTalkComment.length) {
          this.curMessage.replyTalkComment.push(data);
        } else {
          this.curMessage.replyTalkComment = [data];
        }
      }).finally(() => {
        this.$refs.commentBox.cancel();
        this.commentPlaceholder = '说点什么...';
      })
    },
    sendCommentWord(data) {
      let talk = this.curMessage.talk;
      let content = JSON.stringify({
        id: data.id,
        templateGroupId: data.templateGroupId,
        characterId: data.characterId,
        characterName: data.characterName,
        word: data.word,
        voice: data.voice
      })
      let params = {
        talkId: talk.id,
        content: content,
        userNickname: talk.commentCharacterName,
        characterId: talk.commentCharacterId,
        avatarId: talk.commentCharacterAvatarId,
        userAvatar: talk.commentCharacterAvatar,
        replyCommentId: this.curMessage.commentId,
        type: this.$enums.MESSAGE_TYPE.WORD_VOICE
      }
      this.$http({
        url: "/talk/addTalkComment",
        method: 'post',
        data: params
      }).then((data) => {
        if (this.curMessage.replyTalkComment && this.curMessage.replyTalkComment.length) {
          this.curMessage.replyTalkComment.push(data);
        } else {
          this.curMessage.replyTalkComment = [data];
        }
      }).finally(() => {
        this.$refs.commentBox.cancel();
        this.commentPlaceholder = '说点什么...';
      })
    },
    sendCommentCharacterEmo(data) {
      let talk = this.curMessage.talk;
      let content = JSON.stringify({
        originUrl: data.url,
        id: data.id,
        templateGroupId: data.templateGroupId,
        characterId: data.characterId,
        characterName: data.characterName,
        name: data.name,
      })
      let params = {
        talkId: talk.id,
        content: content,
        userNickname: talk.commentCharacterName,
        characterId: talk.commentCharacterId,
        avatarId: talk.commentCharacterAvatarId,
        userAvatar: talk.commentCharacterAvatar,
        replyCommentId: this.curMessage.commentId,
        type: this.$enums.MESSAGE_TYPE.IMAGE
      }
      this.$http({
        url: "/talk/addTalkComment",
        method: 'post',
        data: params
      }).then((data) => {
        if (this.curMessage.replyTalkComment && this.curMessage.replyTalkComment.length) {
          this.curMessage.replyTalkComment.push(data);
        } else {
          this.curMessage.replyTalkComment = [data];
        }
      }).finally(() => {
        this.$refs.commentBox.cancel();
        this.commentPlaceholder = '说点什么...';
      })
    },
    sendCommentImg(data) {
      let talk = this.curMessage.talk;
      let content = JSON.stringify({
        originUrl: data.url,
        name: data.name,
      })
      let params = {
        talkId: talk.id,
        content: content,
        userNickname: talk.commentCharacterName,
        characterId: talk.commentCharacterId,
        avatarId: talk.commentCharacterAvatarId,
        userAvatar: talk.commentCharacterAvatar,
        replyCommentId: this.curMessage.commentId,
        type: this.$enums.MESSAGE_TYPE.IMAGE
      }
      this.$http({
        url: "/talk/addTalkComment",
        method: 'post',
        data: params
      }).then((data) => {
        if (this.curMessage.replyTalkComment && this.curMessage.replyTalkComment.length) {
          this.curMessage.replyTalkComment.push(data);
        } else {
          this.curMessage.replyTalkComment = [data];
        }
      }).finally(() => {
        this.$refs.commentBox.cancel();
        this.commentPlaceholder = '说点什么...';
      })
    },
    playVoice(word) {
      // 如果点击的是同一个音频，则切换播放/暂停状态
      if (this.playingVoice && this.playingVoice.voice === word.voice) {
        if (this.voicePlayState === 'PLAYING') {
          this.wordAudioContext.pause();
          this.voicePlayState = 'PAUSE';
          return;
        } else if (this.voicePlayState === 'PAUSE') {
          this.wordAudioContext.play();
          this.voicePlayState = 'PLAYING';
          return;
        }
      }

      // 停止当前播放的音频
      if (this.wordAudioContext) {
        this.wordAudioContext.stop();
      }

      // 创建音频上下文
      this.wordAudioContext = uni.createInnerAudioContext();
      // 设置音频源
      this.wordAudioContext.src = word.voice;

      // 监听音频播放结束事件
      this.wordAudioContext.onEnded(() => {
        console.log('音频播放结束');
        this.voicePlayState = 'STOP';
        this.playingVoice = null;
      });

      // 监听音频播放错误事件
      this.wordAudioContext.onError((res) => {
        console.log('音频播放出错:', res.errMsg);
        this.voicePlayState = 'STOP';
        this.playingVoice = null;
      });

      // 开始播放新音频
      this.wordAudioContext.play();
      this.voicePlayState = 'PLAYING';
      this.playingVoice = word;
    }
  },
  onLoad(options) {
    this.category = options.category;
    this.groupId = options.groupId;
    this.regionCode = options.regionCode;
    this.queryTalkNotify();
  },
}
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f6f6f6;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 30rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #eeeeee;
  position: fixed;
  width: 100%;
  top: 0;
  z-index: 100;
}

.header-left, .header-right {
  width: 80rpx;
  display: flex;
  align-items: center;
}

.header-title {
  flex: 1;
  text-align: center;
  font-size: 34rpx;
  font-weight: 500;
  color: #333333;
}

.message-list {
  flex: 1;
  margin-top: 90rpx;
}

.message-item {
  background-color: #ffffff;
  padding: 20rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #f0f0f0;
}

.user-meta {
  flex: 1;
}

.username {
  font-size: 30rpx;
  font-weight: 500;
  color: #333333;
  margin-bottom: 6rpx;
  display: block;
}

.time {
  font-size: 24rpx;
  color: #999999;
}

.reply-btn {
  padding: 10rpx 24rpx;
  border-radius: 30rpx;
}

.reply-btn text {
  font-size: 26rpx;
  color: #1890ff;
}

.message-content {

}

.content-text {
  font-size: 30rpx;
  color: #333333;
  line-height: 1.5;
  margin-bottom: 16rpx;
}

.comment-image {
  max-width: 150rpx;
  max-height: 150rpx;
}

.media-wrapper {
  display: flex;
  position: relative;
  margin: 16rpx 0;
  width: 100%;
  border-radius: 12rpx;
  background-color: #f5f5f5;
  overflow: hidden;
}

.media-section {
  position: relative;
}

.content-image {
  width: 180rpx;
  height: 180rpx;
}

.media-icon {
  width: 180rpx;
  height: 180rpx;
}

.image-caption {
  flex: 1;
  padding: 16rpx;
  display: flex;
  align-items: center;
}

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 100rpx;
  color: white;
}

.caption-prefix {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.caption-text {
  font-size: 24rpx;
}

.replies {
  background-color: #f8f8f8;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-top: 16rpx;
}

.reply-item {
  margin-bottom: 12rpx;
  display: flex;
  align-items: flex-start;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-username {
  font-size: 28rpx;
  margin-right: 8rpx;
  flex-shrink: 0;
  white-space: nowrap;
}

.reply-content {
  font-size: 28rpx;
  color: #333333;
  word-wrap: break-word;
  word-break: break-all;
  flex: 1;
  min-width: 0;
}

.reply-image {
  max-width: 150rpx;
  max-height: 150rpx;
}

.reply-input-box {
  background-color: #f8f8f8;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-top: 16rpx;
}

.reply-to {
  font-size: 28rpx;
  color: #999999;
}

.interaction-info {
  margin-top: 16rpx;
  display: flex;
  align-items: center;
}

.like-info {
  display: flex;
  align-items: center;
  width: 100%;
  background-color: #f0f8ff;
}

.like-icon {
  font-size: 32rpx;
  margin-right: 8rpx;
}

.like-text {
  font-size: 28rpx;
  color: #666666;
}

.loading-more {
  text-align: center;
  padding: 20rpx 0;
}

.loading-more text {
  font-size: 26rpx;
  color: #999999;
}

.tab-bar {
  display: flex;
  height: 100rpx;
  background-color: #000000;
  border-top: 1rpx solid #333333;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-icon {
  font-size: 40rpx;
  color: #888888;
}

.tab-icon.active {
  color: #ffffff;
}
</style>