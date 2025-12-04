<template>
    <!-- 抽屉组件 -->
    <el-drawer
        :visible.sync="drawerVisible"
        direction="rtl"
        :with-header="false"
        :show-close="false"
        class="qq-message-drawer"
    >
      <!-- 自定义头部 -->
      <div class="drawer-header">
        <i class="el-icon-arrow-left back-btn" @click="drawerVisible = false"></i>
        <span class="header-title">空间消息</span>
        <i class="el-icon-view eye-icon"></i>
      </div>

      <!-- 消息列表 -->
      <div class="message-list">
        <div
            v-for="(message, index) in messages"
            :key="message.id"
            class="message-item"
            :class="{ 'message-divider': index < messages.length - 1 }"
        >
          <!-- 用户头部信息 -->
          <div class="message-header">
            <div class="user-info">
              <head-image :id="message.commentUserId" :size="45" :url="message.avatar" :name="message.nickname"></head-image>
              <div class="user-details">
                <span class="username" :class="{'character-name': message.commentCharacterId !== null}">{{ message.nickname }}</span>
                <div class="timestamp">{{ message.createTime }}</div>
              </div>
            </div>
            <div class="action-area">
              <span v-if="message.actionType===1" class="reply-btn" @click="handleShowCommentBox(message.talkComment, index)">回复</span>
              <i v-if="message.actionType===2" class="el-icon-thumb-up like-hand"></i>
            </div>
          </div>

          <!-- 消息内容 -->
          <div class="message-content">
            <!-- 点赞通知 -->
            <div v-if="message.actionType===2" class="like-notification">
              <svg class="icon svg-icon" aria-hidden="true">
                <use xlink:href="#icon-dianzan1"></use>
              </svg>
              <span class="like-text">赞了我</span>
            </div>
            <!-- 原始内容 -->
            <div class="original-text" v-if="message.actionType===1 && message.talkComment.type === $enums.MESSAGE_TYPE.TEXT"
                 v-html="$emo.transform(message.talkComment.content)"></div>
            <div class="original-text" v-if="message.actionType===1 && message.talkComment.type === $enums.MESSAGE_TYPE.WORD_VOICE">
              <span class="word">{{JSON.parse(message.talkComment.content).word}}</span>
              <span class="icon iconfont icon-xitongxiaoxi" style="color: orange;" @click.stop="playVoice(JSON.parse(message.talkComment.content))"></span>
            </div>
            <div class="original-text" v-if="message.actionType===1 && message.talkComment.type === $enums.MESSAGE_TYPE.IMAGE">
              <img :src="JSON.parse(message.talkComment.content).originUrl"/>
            </div>

            <div class="talk-content">
              <!-- 图片内容 -->
              <div class="media-section" v-if="message.talk.fileList && message.talk.fileList.length > 0">
                <img v-if="message.talk.fileList[0].fileType === 1" :src="message.talk.fileList[0].url" alt="动态图片" class="dynamic-image">
                <img v-if="message.talk.fileList[0].fileType === 2" class="video-image" :src="message.talk.fileList[0].coverUrl" loading="lazy"/>
                <span v-if="message.talk.fileList[0].fileType === 2" class="play-icon el-icon-video-play"></span>
                <svg v-if="message.talk.fileList[0].fileType === 3" class="icon svg-icon" aria-hidden="true">
                  <use xlink:href="#icon-yinpin"></use>
                </svg>
              </div>
              <!-- 引用框 -->
              <div class="talk-text-box">
                <span class="talk-author">{{message.talk.nickName}}：</span>
                <span v-html="$emo.transform(message.talk.content)"></span>
              </div>
            </div>
            <!-- 回复内容 -->
            <div v-if="message.replyTalkComment && message.replyTalkComment.length" class="replies-section">
              <div v-for="reply in message.replyTalkComment" :key="reply.id" class="reply-item">
                <span class="reply-author" :class="{'character-name': reply.characterId !== null}" @click="showUserInfo($event, reply.userId)">{{ reply.userNickname }}</span>
                <span v-if="reply.replyCommentId" style="margin: 0 10px;" class="reply-prefix">回复</span>
                <span v-if="reply.replyCommentId" class="reply-author" :class="{'character-name': reply.replyUserCharacterId !== null}" @click="showUserInfo($event, reply.replyUserId)">{{ reply.replyUserNickname }}</span>
                <span>：</span>
                <span v-if="reply.type === $enums.MESSAGE_TYPE.TEXT" v-html="$emo.transform(reply.content)" class="comment-content"></span>
                <span v-if="reply.type === $enums.MESSAGE_TYPE.WORD_VOICE" class="comment-content">
                  <span class="word">{{JSON.parse(reply.content).word}}</span>
                  <span class="icon iconfont icon-xitongxiaoxi" style="color: orange;" @click.stop="playVoice(JSON.parse(reply.content))"></span>
                </span>
                <span v-if="reply.type === $enums.MESSAGE_TYPE.IMAGE" class="comment-content">
                  <img :src="JSON.parse(reply.content).originUrl"/>
                </span>
              </div>
            </div>
            <input-box ref="contentInputBox" :character-id="message.talk.commentCharacterId" :placeholder="placeholder"
                       :width="'100%'" @send="(...args) => sayComment(message, ...args)"
            @sendWord="(...args) => sendCommentWord(message, ...args)"></input-box>
          </div>
        </div>
      </div>
      <pagination :totalPage="page.totalPage" :pageNo="page.pageNo" @changePage="handlePage"></pagination>
    </el-drawer>
</template>

<script>
import HeadImage from "@components/common/HeadImage.vue";
import Pagination from "@components/pagination/Pagination.vue";
import InputBox from "@components/common/InputBox.vue";

export default {
  name: 'QQMessageDrawer',
  components: {
    InputBox,
    Pagination,
    HeadImage
  },
  props: {
    category: {
      type: String,
      required: true
    },
    groupId: {
      type: Number,
      default: null,
    },
    regionCode: {
      type: String,
      default: null,
    }
  },
  data() {
    return {
      drawerVisible: false,
      page: {
        pageNo: 1,
        pageSize: 10,
        totalPage: 0,
      },
      messages: [],
      commentLastIndex: null,
      placeholder: "请输入内容",
      audio: null,
      audioSrc: '',
      playCommentId: null,
    }
  },
  methods: {
    show() {
      this.drawerVisible = true
    },
    handleClose() {
      this.drawerVisible = false
    },
    queryTalkNotify() {
      let params = {
        category: this.category,
        groupId: this.groupId,
        regionCode: this.regionCode
      };

      this.$http({
        url: `/talk-notify/pageQueryTalkNotify?pageNo=${this.page.pageNo}&pageSize=${this.page.pageSize}`,
        method: 'post',
        data: params
      }).then((data) => {
        this.messages.push(...data.data);
        this.page.totalPage = (data.total - 1) / this.page.pageSize + 1;
      }).finally(() => {

      })
    },
    sayComment(message, sendObj) {
      if (!sendObj) {
        return
      }
      let content = '';
      if (sendObj.type === this.$enums.MESSAGE_TYPE.IMAGE) {
        content = JSON.stringify(sendObj.content)
      } else {
        content = sendObj.content
      }
      let talk = message.talk;
      let params = {
        talkId: talk.id,
        content: content,
        userNickname: talk.commentCharacterName,
        characterId: talk.commentCharacterId,
        avatarId: talk.commentCharacterAvatarId,
        userAvatar: talk.commentCharacterAvatar,
        replyCommentId: message.commentId,
        type: sendObj.type
      }
      this.$http({
        url: "/talk/addTalkComment",
        method: 'post',
        data: params
      }).then((data) => {
        if (message.replyTalkComment && message.replyTalkComment.length) {
          message.replyTalkComment.push(data);
        } else {
          message.replyTalkComment = [data];
        }
        this.$message.success("评论成功");
        this.$refs.contentInputBox[this.commentLastIndex].hide();
      }).finally(() => {
        this.placeholder = '请输入内容';
      })
    },
    sendCommentWord(message, data) {
      if (!data) {
         return
      }
      let talk = message.talk;
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
        replyCommentId: message.commentId,
        type: this.$enums.MESSAGE_TYPE.WORD_VOICE
      }
      this.$http({
        url: "/talk/addTalkComment",
        method: 'post',
        data: params
      }).then((data) => {
        if (message.replyTalkComment && message.replyTalkComment.length) {
          message.replyTalkComment.push(data);
        } else {
          message.replyTalkComment = [data];
        }
        this.$message.success("评论成功");
        this.$refs.contentInputBox[this.commentLastIndex].hide();
      }).finally(() => {
        this.placeholder = '请输入内容';
      })
    },
    handleShowCommentBox(comment, index) {
      if (this.commentLastIndex != null && this.commentLastIndex != index) {
        this.$refs.contentInputBox[this.commentLastIndex].hide();
      }
      if (this.commentLastIndex == index) {
        if (this.$refs.contentInputBox[index].show) {
          this.$refs.contentInputBox[index].hide();
        } else {
          this.$refs.contentInputBox[index].view();
        }
      } else {
        this.$refs.contentInputBox[index].view();
      }
      this.commentLastIndex = index

      this.placeholder = "回复" + comment.userNickname + ":"
    },
    handlePage(pageNo) {
      this.page.pageNo = pageNo;
      this.queryTalkNotify();
    },
    playVoice(word) {
      if (!this.audio) {
        this.audio = new Audio();
        this.audioSrc = word.voice;
        this.audio.src = word.voice;
        this.audio.play();
      } else {
        if (word.voice === this.audioSrc) {
          this.audioSrc = '';
          this.audio.pause();
          this.audio = null;
        } else {
          this.audio.pause();
          this.audio = new Audio();
          this.audioSrc = word.voice;
          this.audio.src = word.voice;
          this.audio.play();
        }
      }
    },
    showUserInfo(e, userId) {
      if (userId && userId > 0) {
        this.$http({
          url: `/user/find/${userId}`,
          method: 'get'
        }).then((user) => {
          this.$store.commit("setUserInfoBoxPos", e);
          this.$store.commit("showUserInfoBox", user);
        })
      }
    },
  },
  watch: {
    drawerVisible(newValue) {
      if (newValue) {
        this.page.pageNo = 1;
        this.page.totalPage = 0;
        this.messages = [];
        this.queryTalkNotify();
      }
    }
  }
}
</script>

<style scoped lang="scss">
/* 抽屉样式重置 */
.qq-message-drawer >>> .el-drawer {
  background-color: #ffffff;
}

.qq-message-drawer >>> .el-drawer__body {
  padding: 0;
  height: 100vh;
  overflow-y: auto;
}

/* 头部样式 */
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  background-color: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn, .eye-icon {
  font-size: 20px;
  color: #333333;
  cursor: pointer;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #333333;
}

/* 消息列表 */
.message-list {
  background-color: #ffffff;
}

.message-item {
  padding: 20px;
  background-color: #ffffff;
}

.message-divider {
  border-bottom: 8px solid #f5f5f5;
}

/* 消息头部 */
.message-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: flex-start;
}

.user-avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
}

.user-details {
  margin-left: 8px;
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 16px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 2px;
  text-align: left;
}

.character-name {
  color: #f56c6c;
}

.timestamp {
  font-size: 12px;
  color: #999999;
}

.action-area {
  display: flex;
  align-items: center;
}

.reply-btn {
  color: #1890ff;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 8px;
}

.like-hand {
  font-size: 18px;
  color: #666666;
}

/* 消息内容 */
.message-content {
  margin-left: 57px;
}

.original-text {
  font-size: 16px;
  color: #333333;
  line-height: 1.5;
  margin-bottom: 12px;
  text-align: left;
  word-break: break-all;

  .icon {
    cursor: pointer;
  }

  img {
    max-width: 150px;
    max-height: 150px;
    object-fit: cover;
  }
}

.talk-content {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.talk-text-box {
  background-color: #f5f5f5;
  border-radius: 8px;
  padding-left: 12px;
  font-size: 14px;
  color: #666666;
  height: 90px;
  width: 100%;
  display: flex;
  justify-content: left;
  align-items: center;
}

.talk-author {
  color: #1890ff;
}

.media-section {
  display: flex;
  position: relative;

  .icon {
    width: 90px;
    height: 90px;
  }
}

.dynamic-image {
  width: 90px;
  height: 90px;
  border-radius: 8px;
  object-fit: cover;
  display: block;
}

.video-image {
  width: 90px;
  height: 90px;
  border-radius: 8px;
  object-fit: cover;
  display: block;
}

.play-icon {
  display: block;
  position: absolute;
  font-size: 40px;
  font-weight: 500;
  width: 40px;
  height: 40px;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  cursor: pointer;
  color: #ffffff;
}

.image-quote {
  background-color: #f5f5f5;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  color: #666666;
  display: inline-block;
}

.replies-section {
  font-size: 14px;
  color: #666666;
}

.reply-item {
  margin-bottom: 4px;
  display: flex;
  justify-content: left;
  align-items: flex-start;
  flex-wrap: nowrap;
}

.reply-author {
  font-weight: 500;
  flex-shrink: 0;
  cursor: pointer;
}

.reply-to-reply {
  margin-bottom: 8px;
}

.reply-prefix {
  font-weight: 500;
  flex-shrink: 0;
  color: #1890ff;
}

.comment-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  word-break: break-all;

  img {
    max-width: 150px;
    max-height: 150px;
    object-fit: cover;
  }
}

.icon {
  cursor: pointer;
}

.reply-input-placeholder {
  color: #cccccc;
  margin-top: 8px;
}

.like-notification {
  display: flex;
  align-items: center;
  background-color: #f0f8ff;
  padding: 8px 12px;
  border-radius: 6px;
  margin-top: 12px;
  margin-bottom: 12px;

  .icon {
    margin-right: 5px;
    display: block;
    height: 20px;
    line-height: 20px;
    font-size: 18px;
    -webkit-transition: font-size 0.25s linear, width 0.25s linear;
    -moz-transition: font-size 0.25s linear, width 0.25s linear;
    transition: font-size 0.25s linear, width 0.25s linear;
  }
}

.like-icon {
  color: #1890ff;
  font-size: 16px;
  margin-right: 6px;
}

.like-text {
  color: #1890ff;
  font-size: 14px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .drawer-header {
    padding: 12px 16px;
  }

  .message-item {
    padding: 16px;
  }

  .user-avatar {
    width: 40px;
    height: 40px;
  }

  .message-content {
    margin-left: 52px;
  }
}
</style>