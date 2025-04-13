<template>
  <view class="page region-chat-box">
    <nav-bar backHome more refresh @more="onShowMore" @refresh="refreshChat">{{ title }}</nav-bar>
    <view class="chat-main-box" :style="{height: chatMainHeight+'px'}">
      <view class="chat-msg" @click="switchChatTabBox('none')">
        <scroll-view class="scroll-box" scroll-y="true" upper-threshold="200" @scrolltoupper="onScrollToTop"
                     :scroll-into-view="'chat-item-' + scrollMsgIdx" :scroll-with-animation="true">
          <view v-if="chat" v-for="(msgInfo, idx) in chat.messages" :key="idx" class="message-wrapper"
                :data-highlight="scrollMsgIdx === msgInfo.id">
            <chat-message-item :ref="'message'+msgInfo.id" v-if="idx >= showMinIdx"
                               @call="onRtCall(msgInfo)" :showInfo="showInfo(msgInfo)"
                               @recall="onRecallMessage" @delete="onDeleteMessage" @copy="onCopyMessage"
                               @longPressHead="onLongPressHead(msgInfo)" @download="onDownloadFile"
                               @quote="quoteMessage" @scrollToMessage="scrollToTargetMsg"
                               @audioStateChange="onAudioStateChange" :id="'chat-item-' + idx" :msgInfo="msgInfo"
                               :groupMembers="regionGroupMembers" :myGroupMemberInfo="myGroupMemberInfo" :isOwner="regionGroup.leaderId === msgInfo.sendId">
            </chat-message-item>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: "region-chat-box",
  data() {
    return {
      chat: {},
      scrollMsgIdx: 0, // 滚动条定位为到哪条消息,
      regionGroup: {},
      myGroupMemberInfo: {},
      regionGroupMembers: [],
      chatTabBox: 'none',
      atUserIds: [],
      isReceipt: false, // 是否回执消息
      chatMainHeight: 0, // 聊天窗口高度
      keyboardHeight: 290, // 键盘高度
      windowHeight: 1000, // 窗口高度
      initHeight: 1000, // h5初始高度
      needScrollToBottom: false, // 需要滚动到底部
      showMinIdx: 0, // 下标小于showMinIdx的消息不显示，否则可能很卡
      reqQueue: [], // 请求队列
      isSending: false, // 是否正在发送请求
      isShowKeyBoard: false, // 键盘是否正在弹起
      editorCtx: null, // 编辑器上下文
      isEmpty: true, // 编辑器是否为空
      isFocus: false, // 编辑器是否焦点
      isReadOnly: false, // 编辑器是否只读
      playingAudio: null, // 当前正在播放的录音消息
      quoteMsgInfo: {
        msgInfo: null,
        quoteContent: '',
        show: false
      },
    }
  },
  methods: {
    switchChatTabBox(chatTabBox) {
      this.chatTabBox = chatTabBox;
      this.reCalChatMainHeight();
      if (chatTabBox != 'tools' && this.$refs.fileUpload) {
        this.$refs.fileUpload.hide()
      }
    },
    readedMessage() {
      if (this.unreadCount == 0) {
        return;
      }
      let url = `/message/regionGroup/readed?regionGroupId=${this.chat.targetId}`
      this.$http({
        url: url,
        method: 'PUT'
      }).then(() => {
        this.regionStore.resetRegionUnreadCount(this.chat)
        this.scrollToBottom();
      })
    },
    onScrollToTop() {
      if (this.showMinIdx == 0) {
        console.log("消息已滚动到顶部")
        return;
      }
      //  #ifndef H5
      // 防止滚动条定格在顶部，不能一直往上滚
      this.scrollToMsgIdx(this.showMinIdx);
      // #endif
      // 多展示20条信息
      this.showMinIdx = this.showMinIdx > 20 ? this.showMinIdx - 20 : 0;
    },
    scrollToBottom() {
      let size = this.messageSize;
      if (size > 0) {
        this.scrollToMsgIdx(size - 1);
      }
    },
    scrollToMsgIdx(idx) {
      // 如果scrollMsgIdx值没变化，滚动条不会移动
      if (idx == this.scrollMsgIdx && idx > 0) {
        this.$nextTick(() => {
          // 先滚动到上一条
          this.scrollMsgIdx = idx - 1;
          // 再滚动目标位置
          this.scrollToMsgIdx(idx);
        });
        return;
      }
      this.$nextTick(() => {
        this.scrollMsgIdx = idx;
      });
    },
    loadRegionGroup(groupId) {
      this.$http({
        url: `/region/group/find/${groupId}`,
        method: 'get'
      }).then((regionGroup) => {
        this.regionGroup = regionGroup;
        this.regionStore.updateRegionChatFromGroup(regionGroup);
        this.regionStore.updateRegionGroup(regionGroup);
      });

      this.loadRegionGroupMembers(groupId);
    },
    loadRegionGroupMembers(groupId) {
      this.$http({
        url: `/region/group/members/${groupId}`,
        method: 'get'
      }).then((groupMembers) => {
        this.myGroupMemberInfo = groupMembers.find((m) => m.userId === this.mine.id);
        this.regionGroupMembers = groupMembers;
      });
    },
    onShowMore() {

    },
    refreshChat() {

    },
    listenKeyBoard() {
      // #ifdef H5
      const userAgent = navigator.userAgent;
      const regex = /(macintosh|windows)/i;
      if (regex.test(userAgent)) {
        // 电脑端不需要弹出键盘
        console.log("userAgent:", userAgent)
        return;
      }
      if (uni.getSystemInfoSync().platform == 'ios') {
        // ios h5实现键盘监听
        window.addEventListener('focusin', this.focusInListener);
        window.addEventListener('focusout', this.focusOutListener);
        // 监听键盘高度，ios13以上开始支持
        if(window.visualViewport){
          window.visualViewport.addEventListener('resize', this.resizeListener);
        }
      } else {
        // 安卓h5实现键盘监听
        window.addEventListener('resize', this.resizeListener);
      }
      // #endif
      // #ifndef H5
      // app实现键盘监听
      uni.onKeyboardHeightChange(this.keyBoardListener);
      // #endif
    },
    focusInListener() {
      console.log("focusInListener")
      this.isShowKeyBoard = true;
      this.reCalChatMainHeight();
    },
    focusOutListener() {
      console.log("focusOutListener")
      this.isShowKeyBoard = false;
      this.reCalChatMainHeight();
    },
    resizeListener() {
      console.log("resize")
      let keyboardHeight = this.initHeight - window.innerHeight;
      this.isShowKeyBoard = keyboardHeight > 150;
      if (this.isShowKeyBoard) {
        this.keyboardHeight = keyboardHeight;
      }
      this.reCalChatMainHeight();
    },
    keyBoardListener(res) {
      this.isShowKeyBoard = res.height > 0;
      if (this.isShowKeyBoard) {
        this.keyboardHeight = res.height; // 获取并保存键盘高度
      }
      this.reCalChatMainHeight();
    },
    reCalChatMainHeight() {
      setTimeout(() => {
        let h = this.windowHeight;
        // 减去标题栏高度
        h -= 50;
        // 减去键盘高度
        if (this.isShowKeyBoard || this.chatTabBox != 'none') {
          console.log("减去键盘高度:", this.keyboardHeight)
          h -= this.keyboardHeight;
          this.scrollToBottom();
        }
        // #ifndef H5
        // h5需要减去状态栏高度
        h -= uni.getSystemInfoSync().statusBarHeight;
        // #endif
        this.chatMainHeight = h;
        console.log("窗口高度:", this.chatMainHeight)
        if (this.isShowKeyBoard || this.chatTabBox != 'none') {
          this.scrollToBottom();
        }
        // ios浏览器键盘把页面顶起后，页面长度不会变化，这里把页面拉到顶部适配一下
        // #ifdef H5
        if (uni.getSystemInfoSync().platform == 'ios') {
          // 不同手机需要的延时时间不一致，采用分段延时的方式处理
          const delays = [50, 100, 500];
          delays.forEach((delay) => {
            setTimeout(() => {
              uni.pageScrollTo({
                scrollTop: 0,
                duration: 10
              });
            }, delay);
          })
        }
        // #endif
      }, 30)
    },
    onRtCall(msgInfo) {
      if (msgInfo.type == this.$enums.MESSAGE_TYPE.ACT_RT_VOICE) {
        this.onPriviteVoice();
      } else if (msgInfo.type == this.$enums.MESSAGE_TYPE.ACT_RT_VIDEO) {
        this.onPriviteVideo();
      }
    },
    onPriviteVideo() {
      const friendInfo = encodeURIComponent(JSON.stringify(this.friend));
      uni.navigateTo({
        url: `/pages/chat/chat-private-video?mode=video&friend=${friendInfo}&isHost=true`
      })
    },
    onPriviteVoice() {
      const friendInfo = encodeURIComponent(JSON.stringify(this.friend));
      uni.navigateTo({
        url: `/pages/chat/chat-private-video?mode=voice&friend=${friendInfo}&isHost=true`
      })
    },
    onGroupVideo() {
      // 邀请成员发起通话
      let ids = [this.mine.id];
      this.$refs.selBox.init(ids, ids);
      this.$refs.selBox.open();
    },
    showInfo(msgInfo) {
      let showInfoObj = {
        showName: "",
        headImage: "",
        nickName: ""
      };
      if (this.$msgType.isNormal(msgInfo.type) || this.$msgType.isAction(msgInfo.type)) {
        let friend = this.friends.find((f) => f.id === msgInfo.sendId);
        if (friend) {
          if (friend.friendRemark) {
            showInfoObj.showName = friend.friendRemark;
          }
        }
        let member = this.regionGroupMembers.find((m) => m.userId == msgInfo.sendId);
        if (!showInfoObj.showName) {
          if (member) {
            showInfoObj.showName = member.aliasName;
          }
        }
        if (member) {
          showInfoObj.headImage = member.headImage;
        }
        if (!showInfoObj.showName) {
          if (msgInfo.sendNickName) {
            showInfoObj.showName = msgInfo.sendNickName;
          }
        }
        if (!showInfoObj.headImage) {
          if (msgInfo.sendUserAvatar) {
            showInfoObj.headImage = msgInfo.sendUserAvatar;
          }
        }
      }
      return showInfoObj;
    },
    onDeleteMessage(msgInfo) {
      uni.showModal({
        title: '删除消息',
        content: '确认删除消息?',
        success: (res) => {
          if (!res.cancel) {
            this.regionStore.deleteRegionMessage(msgInfo, this.chat);
            uni.showToast({
              title: "删除成功",
              icon: "none"
            })
          }
        }
      })
    },
    onRecallMessage(msgInfo) {
      uni.showModal({
        title: '撤回消息',
        content: '确认撤回消息?',
        success: (res) => {
          if (!res.cancel) {
            let url = `/message/regionGroup/recall/${msgInfo.id}`
            this.$http({
              url: url,
              method: 'DELETE'
            }).then(() => {
              msgInfo = JSON.parse(JSON.stringify(msgInfo));
              msgInfo.type = this.$enums.MESSAGE_TYPE.RECALL;
              msgInfo.content = '你撤回了一条消息';
              msgInfo.status = this.$enums.MESSAGE_STATUS.RECALL;
              this.regionStore.insertRegionMessage(msgInfo, this.chat);
            })
          }
        }
      })
    },
    onCopyMessage(msgInfo) {
      uni.setClipboardData({
        data: msgInfo.content,
        success: () => {
          uni.showToast({ title: '复制成功' });
        },
        fail: () => {
          uni.showToast({ title: '复制失败', icon: 'none' });
        }
      });
    },
    onLongPressHead(msgInfo) {
      if (!msgInfo.selfSend && this.atUserIds.indexOf(msgInfo.sendId) < 0) {
        this.atUserIds.push(msgInfo.sendId);
      }
    },
    onDownloadFile(msgInfo) {
      let url = JSON.parse(msgInfo.content).url;
      uni.downloadFile({
        url: url,
        success(res) {
          if (res.statusCode === 200) {
            var filePath = encodeURI(res.tempFilePath);
            uni.openDocument({
              filePath: filePath,
              showMenu: true
            });
          }
        },
        fail(e) {
          uni.showToast({
            title: "文件下载失败",
            icon: "none"
          })
        }
      });
    },
    quoteMessage(msgInfo) {
      console.log("引用消息", msgInfo);

      this.quoteMsgInfo.msgInfo =  msgInfo;
      this.quoteMsgInfo.quoteContent += (msgInfo.showName + "：");
      if (msgInfo.type === this.$enums.MESSAGE_TYPE.TEXT) {
        this.quoteMsgInfo.quoteContent += msgInfo.content;
      } else if (msgInfo.type === this.$enums.MESSAGE_TYPE.IMAGE) {
        this.quoteMsgInfo.quoteContent += "[图片]";
      } else if (msgInfo.type === this.$enums.MESSAGE_TYPE.VIDEO) {
        this.quoteMsgInfo.quoteContent += "[视频]";
      } else if (msgInfo.type === this.$enums.MESSAGE_TYPE.FILE) {
        this.quoteMsgInfo.quoteContent += "[文件]";
      } else if (msgInfo.type === this.$enums.MESSAGE_TYPE.AUDIO) {
        this.quoteMsgInfo.quoteContent += "[语音]" + JSON.parse(msgInfo.content).duration + '"';
      } else if (msgInfo.type === this.$enums.MESSAGE_TYPE.WORD_VOICE) {
        //this.quoteMsgInfo.quoteContent += "[语音台词]";
        this.quoteMsgInfo.quoteContent += JSON.parse(msgInfo.content).word;
      }
      this.quoteMsgInfo.show = true;
    },
    cancelQuote() {
      this.quoteMsgInfo.msgInfo = null;
      this.quoteMsgInfo.quoteContent = "";
      this.quoteMsgInfo.show = false;
    },
    scrollToTargetMsg(messageId) {
      this.scrollMsgIdx = messageId;
      setTimeout(() => {
        this.scrollMsgIdx = 0;
      }, 2000);
    },
    onAudioStateChange(state, msgInfo) {
      const playingAudio = this.$refs['message' + msgInfo.id][0]
      if (state == 'PLAYING' && playingAudio != this.playingAudio) {
        // 停止之前的录音
        this.playingAudio && this.playingAudio.stopPlayAudio();
        // 记录当前正在播放的消息
        this.playingAudio = playingAudio;
      }
    },
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
    messageSize() {
      if (!this.chat || !this.chat.messages) {
        return 0;
      }
      return this.chat.messages.length;
    },
    unreadCount() {
      if (!this.chat || !this.chat.unreadCount) {
        return 0;
      }
      return this.chat.unreadCount;
    },
    title() {
      let title = this.regionGroup.remark;
      let size = this.regionGroupMembers.filter(m => !m.quit).length;
      title += `(${size})`;
      return title;
    },
    friends() {
      return this.friendStore.friends;
    }
  },
  onLoad(options) {
    // 聊天数据
    this.chat = this.regionStore.findRegionChatById(options.regionGroupId);
    console.log("regionChat", this.chat)
    // 初始状态只显示20条消息
    let size = this.messageSize;
    this.showMinIdx = size > 20 ? size - 20 : 0;
    // 消息已读
    this.readedMessage();

    this.loadRegionGroup(options.regionGroupId);

    // 激活当前会话
    this.regionStore.activeRegionChat(options.regionGroupId);

    // 复位回执消息
    this.isReceipt = false;
    // 监听键盘高度
    this.listenKeyBoard();
    // 计算聊天窗口高度
    this.$nextTick(() => {
      this.windowHeight = uni.getSystemInfoSync().windowHeight;
      this.reCalChatMainHeight()
      // 兼容ios h5:禁止页面滚动
      // #ifdef H5
      this.initHeight = window.innerHeight;
      document.body.addEventListener('touchmove', function(e) {
        e.preventDefault();
      }, { passive: false });
      // #endif
    });
  }
}
</script>

<style scoped lang="scss">
.region-chat-box {
  $icon-color: rgba(0, 0, 0, 0.88);
  position: relative;
  background-color: #fafafa;

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes highlight {
    0%, 100% {
      background-color: transparent;
    }
    20%, 80% {
      background-color: rgba(79, 70, 229, 0.1);
    }
  }

  .header {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 60rpx;
    padding: 5px;
    background-color: #fafafa;
    line-height: 50px;
    font-size: $im-font-size-large;
    box-shadow: $im-box-shadow-lighter;
    z-index: 1;

    .btn-side {
      position: absolute;
      line-height: 60rpx;
      cursor: pointer;

      &.right {
        right: 30rpx;
      }
    }
  }

  .chat-main-box {
    // #ifdef H5
    top: $im-nav-bar-height;
    // #endif
    // #ifndef H5
    top: calc($im-nav-bar-height + var(--status-bar-height));
    // #endif
    position: fixed;
    width: 100%;
    display: flex;
    flex-direction: column;
    z-index: 2;

    .chat-msg {
      flex: 1;
      padding: 0;
      overflow: hidden;
      position: relative;
      background-color: white;

      .scroll-box {
        height: 100%;

        .message-wrapper {
          animation: fadeIn 0.3s ease;
          margin-bottom: 15px;
        }

        .message-wrapper[data-highlight="true"] {
          animation: highlight 2s ease;
        }
      }
    }

    .chat-at-bar {
      display: flex;
      align-items: center;
      padding: 0 10rpx;

      .icon-at {
        font-size: $im-font-size-larger;
        color: $im-color-primary;
        font-weight: bold;
      }

      .chat-at-scroll-box {
        flex: 1;
        width: 80%;

        .chat-at-items {
          display: flex;
          align-items: center;
          height: 70rpx;

          .chat-at-item {
            padding: 0 3rpx;
          }
        }
      }

    }



    .send-bar {
      display: flex;
      align-items: center;
      padding: 10rpx;
      border-top: $im-border solid 1px;
      background-color: $im-bg;
      min-height: 80rpx;
      margin-bottom: 14rpx;

      .iconfont {
        font-size: 60rpx;
        margin: 0 10rpx;
        color: $icon-color;
      }

      .chat-record {
        flex: 1;
      }

      .send-text {
        flex: 1;
        overflow: auto;
        padding: 14rpx 20rpx;
        background-color: #fff;
        border-radius: 8rpx;
        font-size: $im-font-size;
        box-sizing: border-box;
        margin: 0 10rpx;
        position: relative;

        .send-text-area {
          width: 100%;
          height: 100%;
          min-height: 40rpx;
          max-height: 200rpx;
          font-size: 30rpx;
        }

        .quote-message {
          background: #eee;
          padding: .15625rem;
          display: flex;
          align-items: center;
          border-radius: 10rpx;

          .quote-text {
            flex: 1;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            font-size: 20rpx;
            color: #909399;
          }
        }
      }

      .btn-send {
        margin: 5rpx;
      }
    }
  }

  .chat-tab-bar {
    position: fixed;
    bottom: 0;
    background-color: $im-bg;

    .chat-tools {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      height: 310px;
      padding: 40rpx;
      box-sizing: border-box;

      .chat-tools-item {
        width: 25%;
        padding: 16rpx;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        align-items: center;

        .tool-icon {
          padding: 26rpx;
          font-size: 54rpx;
          border-radius: 20%;
          background-color: white;
          color: $icon-color;

          &:active {
            background-color: $im-bg-active;
          }
        }

        .tool-name {
          height: 60rpx;
          line-height: 60rpx;
          font-size: 28rpx;
        }
      }
    }

    .chat-emotion {
      height: 310px;
      padding: 20rpx;
      box-sizing: border-box;

      .emotion-item-list {
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        align-content: center;

        .emotion-item {
          text-align: center;
          cursor: pointer;
          padding: 5px;
        }
      }
    }

    .character-emotion {
      height: 310px;
      width: 100%;
      padding: 10rpx;
      box-sizing: border-box;

      .emo-swiper {
        height: 310rpx;
        width: 100vw;

        .emo-swiper-item {
          height: 310rpx;
          width: 100vw;

          display: grid;
          grid-template-columns: repeat(6, 1fr);
          grid-auto-rows: 90rpx; /* 强制统一行高 */
          gap: 10rpx;

          .emotion-item {
            width: 90rpx;
            height: 90rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            aspect-ratio: 1; /* 宽高比 1:1 */
            object-fit: cover; /* 图片裁剪填充 */
          }
        }
      }
    }
  }
}
</style>