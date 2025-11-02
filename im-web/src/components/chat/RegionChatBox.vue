<template>
  <div class="chat-box" @click="closeRefBox()" @mousemove="readedMessage()">
    <el-container>
      <el-header height="60px" style="text-align: center">
        <span class="region-group-name">{{title}}</span>
        <span class="btn-side">
<!--          <span title="刷新群聊信息" style="color: greenyellow;margin-right: 10px" class="el-icon-refresh" @click="loadRegionGroup(chat.targetId)"></span>-->
          <span title="群聊信息" class="el-icon-more" @click="showSide=!showSide"></span>
        </span>

      </el-header>
      <el-main style="padding: 0;">
        <el-container>
          <el-container class="content-box">
            <el-main class="im-chat-main" id="chatScrollBox" @scroll="onScroll">
              <div class="im-chat-box">
                <ul>
                  <li v-for="(msgInfo, idx) in chat.messages" :key="msgInfo.id ? msgInfo.id : msgInfo.uid" :ref="`message-${msgInfo.id}`"
                      :data-highlight="highlightedMessageId === msgInfo.id" class="message-wrapper">
                    <chat-message-item
                        v-if="idx>=showMinIdx"
                        @call="onCall(msgInfo.type)"
                        :mine="msgInfo.sendId == mine.id"
                        :showInfo="showInfo(msgInfo)"
                        :msgInfo="msgInfo"
                        :isOwner="regionGroup.leaderId === msgInfo.sendId"
                        :myGroupMemberInfo="myGroupMemberInfo"
                        :groupMembers="regionGroupMembers"
                        @delete="deleteMessage"
                        @recall="recallMessage"
                        @quote="quoteMessage"
                        @playVideo="playVideo"
                        @scrollToMessage="scrollToTargetMsg"
                        @audioStateChange="onAudioStateChange">
                    </chat-message-item>
                  </li>
                </ul>
              </div>
            </el-main>
            <div v-if="!isInBottom" class="scroll-to-bottom" @click="scrollToBottom">
              {{ newMessageSize > 0 ? newMessageSize + '条新消息' : '回到底部' }}
            </div>
            <el-footer height="240px" class="im-chat-footer">
              <div class="chat-tool-bar">
                <div title="表情" ref="emotion" @click.stop="showEmotionBox()">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-fabiaoqing"></use>
                  </svg>
                </div>
                <div title="发送图片">
                  <file-upload :action="'/image/upload'" :maxSize="5*1024*1024" :fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/webp','image/gif']"
                               @before="onImageBefore" @success="onImageSuccess" @fail="onImageFail">
                    <svg class="icon svg-icon" aria-hidden="true">
                      <use xlink:href="#icon-tupian"></use>
                    </svg>
                  </file-upload>
                </div>
                <div title="发送视频">
                  <file-upload :action="'/video/upload'" :maxSize="10*1024*1024" :fileTypes="['video/mp4', 'video/ogg', 'video/flv', 'video/avi','video/wmv', 'video/rmvb', 'video/mov']"
                               @before="onVideoBefore" @success="onVideoSuccess" @fail="onVideoFail">
                    <svg class="icon svg-icon" aria-hidden="true">
                      <use xlink:href="#icon-mv"></use>
                    </svg>
                  </file-upload>
                </div>
                <div title="发送音频">
                  <file-upload :action="'/audio/upload'" :maxSize="10*1024*1024" :fileTypes="['audio/mpeg', 'audio/mp3', 'audio/ape', 'audio/wav', 'audio/flac','audio/m4a', 'audio/kgm', 'audio/ncm', 'audio/mgg']"
                               @before="onAudioBefore" @success="onAudioSuccess" @fail="onAudioFail">
                    <svg class="icon svg-icon" aria-hidden="true">
                      <use xlink:href="#icon-yinpin"></use>
                    </svg>
                  </file-upload>
                </div>
                <div title="发送文件">
                  <file-upload ref="fileUpload" :action="'/file/upload'" :maxSize="10 * 1024 * 1024"
                               @before="onFileBefore" @success="onFileSuccess" @fail="onFileFail">
                    <svg class="icon svg-icon" aria-hidden="true">
                      <use xlink:href="#icon-icon-wenjian"></use>
                    </svg>
                  </file-upload>
                </div>
                <div title="回执消息" v-show="isGroupOwner && memberSize <= 50" :class="isReceipt ? 'chat-tool-active' : ''" @click="onSwitchReceipt">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-yihuizhi"></use>
                  </svg>
                </div>
                <div title="发送语音" @click="showRecordBox()">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-yuyin"></use>
                  </svg>
                </div>
                <div title="语音通话" v-show="regionGroup.joinType===1" @click="onGroupVideo()">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-yuyintonghua"></use>
                  </svg>
                </div>
                <div title="聊天记录" v-show="regionGroup.joinType===1" @click="showHistoryBox()">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-lishijilu"></use>
                  </svg>
                </div>
              </div>
              <div class="send-content-area">
                <ChatInput :ownerId="regionGroup.leaderId" ref="chatInputEditor" :groupMembers="regionGroupMembers"
                           @submit="sendMessage" :quote-message="quoteMsgInfo" @removeQuoteMsg="cancelQuote"/>
                <div class="send-btn-area">
                  <el-button type="primary" size="small" @click="notifySend()">发送</el-button>
                </div>
              </div>
            </el-footer>
          </el-container>
          <el-aside class="chat-group-side-box" width="300px" v-show="showSide">
            <chat-region-group-side :regionGroup="regionGroup" @reload="loadRegionGroup(regionGroup.id)">
            </chat-region-group-side>
          </el-aside>
        </el-container>
      </el-main>
      <emotion ref="emoBox" @emotion="onEmotion"></emotion>
      <chat-record :visible="showRecord" @close="closeRecordBox" @send="onSendRecord"></chat-record>
      <region-chat-history :visible="showHistory" :chat="chat" :regionGroup="regionGroup" :groupMembers="regionGroupMembers" :myGroupMemberInfo="myGroupMemberInfo"
                           @close="closeHistoryBox"></region-chat-history>
      <video-play ref="videoPlay" :videoUrl="videoUrl" :posterUrl="posterUrl" @close="closeVideoPlay"></video-play>
    </el-container>
  </div>
</template>

<script>
import ChatInput from "./ChatInput";
import FileUpload from "../common/FileUpload.vue";
import Emotion from "../common/Emotion.vue";
import ChatRecord from "@/components/chat/ChatRecord";
import ChatMessageItem from "./ChatMessageItem.vue";
import RegionChatHistory from "@/components/chat/RegionChatHistory";
import ChatRegionGroupSide from "@/components/chat/ChatRegionGroupSide";
import VideoPlay from "../common/VideoPlay.vue";

export default {
  name: "RegionChatBox",
  components: {
    VideoPlay,
    ChatInput,
    FileUpload,
    Emotion,
    ChatRecord,
    ChatMessageItem,
    RegionChatHistory,
    ChatRegionGroupSide,
  },
  props: {
    chat: {
      type: Object
    }
  },
  data() {
    return {
      regionGroup: {},
      friend: {},
      regionGroupMembers: [],
      regionGroupMembersMap: new Map(),
      myGroupMemberInfo: {}, // 我的群聊成员信息
      sendImageUrl: "",
      sendImageFile: "",
      placeholder: "",
      isReceipt: false,
      showRecord: false, // 是否显示语音录制弹窗
      showSide: false, // 是否显示群聊信息栏
      showHistory: false, // 是否显示历史聊天记录
      lockMessage: false, // 是否锁定发送
      showMinIdx: 0, // 下标低于showMinIdx的消息不显示，否则页面会很卡
      friends: [],
      reqQueue: [],
      isSending: false,
      quoteMsgInfo: {
        msgInfo: null,
        show: false
      },
      highlightedMessageId: null,
      videoUrl: '',
      posterUrl: '',
      isInBottom: false, // 滚动条是否在底部
      newMessageSize: 0, // 滚动条不在底部时新的消息数量
      lastScrollTop: 0,
    }
  },
  created() {
    this.friends = this.$store.state.friendStore.friends;

    // 监听事件
    this.$eventBus.$on('region-group-change', (msg) => {
      if (msg.chatType === 'REGION-GROUP' && this.regionGroup.id === msg.regionGroupId && msg.groupChangeType) {
        this.loadRegionGroupMembers(this.regionGroup.id);
      }
    });
  },
  beforeDestroy() {
    // 组件销毁时移除监听，避免内存泄漏
    console.log('RegionChatBox beforeDestroy');
    this.$eventBus.$off('region-group-change');
  },
  methods: {
    moveChatToTop() {
      let chatIdx = this.$store.getters.findChatIdx(this.chat);
      this.$store.commit("moveTop", chatIdx);
    },
    closeRefBox() {
      //this.$refs.emoBox.close();
    },
    onCall(type){
      if(type == this.$enums.MESSAGE_TYPE.ACT_RT_VOICE){
        this.showPrivateVideo('voice');
      }else if(type == this.$enums.MESSAGE_TYPE.ACT_RT_VIDEO){
        this.showPrivateVideo('video');
      }
    },
    onSwitchReceipt() {
      this.isReceipt = !this.isReceipt;
      this.refreshPlaceHolder();
    },
    onImageSuccess(data, file) {
      let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
      msgInfo.content = JSON.stringify(data);
      msgInfo.receipt = this.isReceipt;
      this.sendMessageRequest(msgInfo).then((m) => {
        msgInfo.loadStatus = 'ok';
        msgInfo.id = m.id;
        this.isReceipt = false;
        this.$store.commit("insertRegionMessage", msgInfo);
      })
    },
    onImageFail(e, file) {
      let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
      msgInfo.loadStatus = 'fail';
      this.$store.commit("insertRegionMessage", msgInfo);
    },
    onImageBefore(file) {
      let url = URL.createObjectURL(file);
      let data = {
        originUrl: url,
        thumbUrl: url
      }
      let msgInfo = {
        id: 0,
        tmpId: this.generateId(),
        fileId: file.uid,
        sendId: this.mine.id,
        content: JSON.stringify(data),
        sendTime: new Date().getTime(),
        selfSend: true,
        type: 1,
        readedCount: 0,
        loadStatus: "loading",
        joinType: this.regionGroup.joinType,
        status: this.$enums.MESSAGE_STATUS.UNSEND
      }
      // 填充对方id
      this.fillTargetId(msgInfo, this.chat.targetId);
      // 插入消息
      this.$store.commit("insertRegionMessage", msgInfo);
      // 会话置顶
      //this.moveChatToTop();
      // 滚动到底部
      this.scrollToBottom();
      // 借助file对象保存
      file.msgInfo = msgInfo;
    },
    onVideoFail(e, file) {
      let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
      msgInfo.loadStatus = 'fail';
      this.$store.commit("insertRegionMessage", msgInfo);
    },
    onVideoSuccess(data, file) {
      let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
      msgInfo.content = JSON.stringify(data);
      msgInfo.receipt = this.isReceipt;
      this.sendMessageRequest(msgInfo).then((m) => {
        msgInfo.loadStatus = 'ok';
        msgInfo.id = m.id;
        this.isReceipt = false;
        this.$store.commit("insertRegionMessage", msgInfo);
      })
    },
    onVideoBefore(file) {
      let url = URL.createObjectURL(file);
      let data = {
        videoUrl: url,
      }
      let msgInfo = {
        id: 0,
        tmpId: this.generateId(),
        fileId: file.uid,
        sendId: this.mine.id,
        content: JSON.stringify(data),
        sendTime: new Date().getTime(),
        selfSend: true,
        type: 4,
        readedCount: 0,
        loadStatus: "loading",
        status: this.$enums.MESSAGE_STATUS.UNSEND,
        joinType: this.regionGroup.joinType,
      }
      // 填充对方id
      this.fillTargetId(msgInfo, this.chat.targetId);
      // 插入消息
      this.$store.commit("insertRegionMessage", msgInfo);
      // 会话置顶
      this.moveChatToTop();
      // 滚动到底部
      this.scrollToBottom();
      // 借助file对象保存
      file.msgInfo = msgInfo;
    },
    onAudioFail(e, file) {
      let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
      msgInfo.loadStatus = 'fail';
      this.$store.commit("insertRegionMessage", msgInfo);
    },
    onAudioSuccess(data, file) {
      let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
      msgInfo.content = JSON.stringify(data);
      msgInfo.receipt = this.isReceipt;
      this.sendMessageRequest(msgInfo).then((m) => {
        msgInfo.loadStatus = 'ok';
        msgInfo.id = m.id;
        this.isReceipt = false;
        this.$store.commit("insertRegionMessage", msgInfo);
      })
    },
    onAudioBefore(file) {
      let url = URL.createObjectURL(file);
      let data = {
        url: url,
      }
      let msgInfo = {
        id: 0,
        tmpId: this.generateId(),
        fileId: file.uid,
        sendId: this.mine.id,
        content: JSON.stringify(data),
        sendTime: new Date().getTime(),
        selfSend: true,
        type: 3,
        readedCount: 0,
        loadStatus: "loading",
        status: this.$enums.MESSAGE_STATUS.UNSEND,
        joinType: this.regionGroup.joinType,
      }
      // 填充对方id
      this.fillTargetId(msgInfo, this.chat.targetId);
      // 插入消息
      this.$store.commit("insertRegionMessage", msgInfo);
      // 会话置顶
      this.moveChatToTop();
      // 滚动到底部
      this.scrollToBottom();
      // 借助file对象保存
      file.msgInfo = msgInfo;
    },
    onFileSuccess(url, file) {
      let data = {
        name: file.name,
        size: file.size,
        url: url
      }
      let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
      msgInfo.content = JSON.stringify(data);
      msgInfo.receipt = this.isReceipt
      this.$http({
        url: this.messageAction,
        method: 'post',
        data: msgInfo
      }).then((id) => {
        msgInfo.loadStatus = 'ok';
        msgInfo.id = id;
        this.isReceipt = false;
        this.$store.commit("insertRegionMessage", msgInfo);
      })
    },
    onFileFail(res, file) {
      let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
      msgInfo.loadStatus = 'fail';
      this.$store.commit("insertRegionMessage", msgInfo);
    },
    onFileBefore(file) {
      let url = URL.createObjectURL(file);
      let data = {
        name: file.name,
        size: file.size,
        url: url
      }
      let msgInfo = {
        id: 0,
        tmpId: this.generateId(),
        sendId: this.mine.id,
        content: JSON.stringify(data),
        sendTime: new Date().getTime(),
        selfSend: true,
        type: 2,
        readedCount: 0,
        loadStatus: "loading",
        joinType: this.regionGroup.joinType,
        status: this.$enums.MESSAGE_STATUS.UNSEND
      }
      // 填充对方id
      this.fillTargetId(msgInfo, this.chat.targetId);
      // 插入消息
      this.$store.commit("insertRegionMessage", msgInfo);
      // 会话置顶
      //this.moveChatToTop();
      // 滚动到底部
      this.scrollToBottom();
      // 借助file对象透传
      file.msgInfo = msgInfo;
    },
    onCloseSide() {
      this.showSide = false;
    },
    onScrollToTop() {
      // 多展示10条信息
      this.showMinIdx = this.showMinIdx > 10 ? this.showMinIdx - 10 : 0;
    },
    onScroll(e) {
      let scrollElement = e.target
      let scrollTop = scrollElement.scrollTop
      if (scrollTop < 30) { // 在顶部,不滚动的情况
        // 多展示20条信息
        this.showMinIdx = this.showMinIdx > 20 ? this.showMinIdx - 20 : 0;
        this.isInBottom = false;
      }
      // 判断滚动方向（向上滚动）
      if (scrollTop < this.lastScrollTop - 300) {
        console.log("向上滚动 ↑");
        // 这里可以添加自定义逻辑
        this.isInBottom = false;
      }

      // 更新上次滚动位置
      this.lastScrollTop = scrollTop;
      // 滚到底部
      if (scrollTop + scrollElement.clientHeight >= scrollElement.scrollHeight - 30) {
        this.isInBottom = true;
        this.newMessageSize = 0;
      }
    },
    throttle(fn, delay) {
      let timer = null;
      return function (...args) {
        if (!timer) {
          timer = setTimeout(() => {
            fn.apply(this, args);
            timer = null;
          }, delay);
        }
      };
    },
    showEmotionBox() {
      let width = this.$refs.emotion.offsetWidth;
      let left = this.$elm.fixLeft(this.$refs.emotion);
      let top = this.$elm.fixTop(this.$refs.emotion);
      this.$refs.emoBox.open({
        x: left + width / 2,
        y: top
      })

    },
    onEmotion(emoText) {
      this.$refs.chatInputEditor.insertEmoji(emoText);
    },
    showRecordBox() {
      this.showRecord = true;
    },
    closeRecordBox() {
      this.showRecord = false;
    },
    showPrivateVideo(mode) {
      let rtcInfo = {
        mode: mode,
        isHost: true,
        friend: this.friend,
      }
      // 通过home.vue打开单人视频窗口
      this.$eventBus.$emit("openPrivateVideo", rtcInfo);
    },
    onGroupVideo() {
      // 邀请成员发起通话
      let ids = [this.mine.id];
      let maxChannel = this.$store.state.configStore.webrtc.maxChannel;
      this.$refs.rtcSel.open(maxChannel, ids, ids);
    },
    onInviteOk(members) {
      if (members.length < 2) {
        return;
      }
      let userInfos = [];
      members.forEach(m => {
        userInfos.push({
          id: m.userId,
          nickName: m.showNickName,
          headImage: m.headImage,
          isCamera: false,
          isMicroPhone: true
        })
      })
      let rtcInfo = {
        isHost: true,
        groupId: this.group.id,
        inviterId: this.mine.id,
        userInfos: userInfos
      }
      // 通过home.vue打开多人视频窗口
      this.$eventBus.$emit("openGroupVideo", rtcInfo);
    },
    showHistoryBox() {
      this.showHistory = true;
    },
    closeHistoryBox() {
      this.showHistory = false;
    },
    onSendRecord(data) {
      let msgInfo = {
        content: JSON.stringify(data),
        type: 3,
        receipt: this.isReceipt,
        joinType: this.regionGroup.joinType,
      }
      // 填充对方id
      this.fillTargetId(msgInfo, this.chat.targetId);
      this.sendMessageRequest(msgInfo).then((m) => {
        m.selfSend = true;
        this.$store.commit("insertRegionMessage", m);
        // 会话置顶
        //this.moveChatToTop();
        // 保持输入框焦点
        this.$refs.chatInputEditor.focus();
        // 滚动到底部
        this.scrollToBottom();
        // 关闭录音窗口
        this.showRecord = false;
        this.isReceipt = false;
        this.refreshPlaceHolder();
      })
    },
    fillTargetId(msgInfo, targetId) {
      msgInfo.regionGroupId = targetId;
    },
    notifySend() {
      this.$refs.chatInputEditor.submit();
    },
    async sendMessage(fullList) {
      this.resetEditor();
      this.readedMessage();
      let sendText = this.isReceipt ? "【回执消息】" : "";
      let promiseList = [];
      for (let i = 0; i < fullList.length; i++) {
        let msg = fullList[i];
        switch (msg.type) {
          case "text":
            await this.sendTextMessage(sendText + msg.content,msg.atUserIds);
            break;
          case "image":
            await this.sendImageMessage(msg.content.file);
            break;
          case "file":
            await this.sendFileMessage(msg.content.file);
            break;
        }

      }
    },
    sendImageMessage(file) {
      return new Promise((resolve,reject)=>{
        this.onImageBefore(file);
        let formData = new FormData()
        formData.append('file', file)
        this.$http.post("/image/upload", formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }).then((data) => {
          this.onImageSuccess(data, file);
          resolve();
        }).catch((res) => {
          this.onImageFail(res, file);
          reject();
        })
        this.$nextTick(() => this.$refs.chatInputEditor.focus());
        this.scrollToBottom();
      });
    },
    sendTextMessage(sendText,atUserIds) {
      return new Promise((resolve,reject)=>{
        if (!sendText.trim()) {
          reject();
        }
        let msgInfo = {
          content: sendText,
          type: 0
        }
        // 填充对方id
        this.fillTargetId(msgInfo, this.chat.targetId);
        // 被@人员列表
        msgInfo.atUserIds = atUserIds;
        msgInfo.receipt = this.isReceipt;
        msgInfo.joinType = this.regionGroup.joinType;
        this.lockMessage = true;
        this.sendMessageRequest(msgInfo).then((m) => {
          m.selfSend = true;
          this.$store.commit("insertRegionMessage", m);
          // 会话置顶
          //this.moveChatToTop();
        }).finally(() => {
          // 解除锁定
          this.scrollToBottom();
          this.isReceipt = false;
          resolve();
        });
      });
    },
    sendFileMessage(file) {
      return new Promise((resolve,reject)=>{
        let check = this.$refs.fileUpload.beforeUpload(file);
        if (check) {
          this.$refs.fileUpload.onFileUpload({ file });
        }
      })
    },
    deleteMessage(msgInfo) {
      this.$confirm('确认删除消息?', '删除消息', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.commit("deleteRegionMessage", msgInfo);
      });
    },
    recallMessage(msgInfo) {
      this.$confirm('确认撤回消息?', '撤回消息', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let url = `/message/regionGroup/recall/${msgInfo.id}`
        this.$http({
          url: url,
          method: 'delete'
        }).then(() => {
          this.$message.success("消息已撤回");
          msgInfo = JSON.parse(JSON.stringify(msgInfo));
          msgInfo.type = 10;
          msgInfo.content = '你撤回了一条消息';
          msgInfo.status = this.$enums.MESSAGE_STATUS.RECALL;
          this.$store.commit("insertRegionMessage", msgInfo);
        })
      });
    },
    quoteMessage(msgInfo) {
      console.log("引用消息", msgInfo);

      this.quoteMsgInfo.msgInfo =  msgInfo;
      this.quoteMsgInfo.show = true;
    },
    cancelQuote() {
      this.quoteMsgInfo.msgInfo = null;
      this.quoteMsgInfo.show = false;
    },
    readedMessage() {
      if (this.chat.unreadCount == 0) {
        return;
      }
      this.$store.commit("resetRegionUnreadCount", this.chat)
      let url = `/message/regionGroup/readed?regionGroupId=${this.chat.targetId}`
      this.$http({
        url: url,
        method: 'put'
      }).then(() => {})
    },
    loadRegionGroup(groupId) {
      this.$http({
        url: `/region/group/find/${groupId}`,
        method: 'get'
      }).then((regionGroup) => {
        this.regionGroup = regionGroup;
        this.$store.commit("updateRegionChatFromGroup", regionGroup);
        this.$store.commit("updateRegionGroup", regionGroup);
      });

      this.loadRegionGroupMembers(groupId);
    },
    loadRegionGroupMembers(groupId) {
      this.$http({
        url: `/region/group/members/${groupId}`,
        method: 'get'
      }).then((groupMembers) => {
        this.regionGroupMembers = groupMembers;
        this.regionGroupMembersMap = groupMembers.reduce((map, member) => {
          map.set(member.userId, member);
          return map;
        }, new Map()); // 初始值为一个空Map
        this.myGroupMemberInfo = this.regionGroupMembersMap.get(this.mine.id);
      });
    },
    showName(msgInfo) {
      if (this.$msgType.isNormal(msgInfo.type) || this.$msgType.isAction(msgInfo.type)) {
        let friend = this.friends.find((f) => f.id === msgInfo.sendId);
        if (friend && friend.friendRemark) {
          return friend.friendRemark;
        }

        let member = this.regionGroupMembers.find((m) => m.userId == msgInfo.sendId);
        if (member) {
          return member.aliasName;
        }
        if (msgInfo.sendNickName) {
          return msgInfo.sendNickName;
        }
        return "";
      }
      return "";
    },
    headImage(msgInfo) {
      if (this.$msgType.isNormal(msgInfo.type) || this.$msgType.isAction(msgInfo.type)) {
        let friend = this.friends.find((f) => f.id === msgInfo.sendId);
        if (friend && friend.friendHeadImage) {
          return friend.friendHeadImage;
        }

        let member = this.regionGroupMembers.find((m) => m.userId == msgInfo.sendId);
        if (member && member.headImage) {
          return member.headImage;
        }
        if (msgInfo.sendUserAvatar) {
          return msgInfo.sendUserAvatar
        }
        return "";
      }
      return "";
    },
    showInfo(msgInfo) {
      let showInfoObj = {
        showName: "",
        headImage: "",
        nickName: "",
        quoteShowName: '',
        role: '',
      };
      if (this.$msgType.isNormal(msgInfo.type) || this.$msgType.isAction(msgInfo.type)) {
        //let friend = this.friends.find((f) => f.id === msgInfo.sendId);
        let friend = this.friendsMap.get(msgInfo.sendId);
        if (friend) {
          showInfoObj.role = friend.role;
          if (friend.friendRemark) {
            showInfoObj.showName = friend.friendRemark;
          }
        }
        //let member = this.regionGroupMembers.find((m) => m.userId == msgInfo.sendId);
        let member = this.regionGroupMembersMap.get(msgInfo.sendId);
        if (msgInfo.quoteMsg) {
          //let member2 = this.regionGroupMembers.find((m) => m.userId == msgInfo.quoteMsg.sendId);
          let member2 = this.regionGroupMembersMap.get(msgInfo.quoteMsg.sendId);
          showInfoObj.quoteShowName = member2 ? member2.aliasName : "";
        }
        if (!showInfoObj.showName) {
          if (member) {
            showInfoObj.showName = member.aliasName;
          }
        }
        if (member) {
          showInfoObj.role = member.role;
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
    resetEditor() {
      this.$nextTick(() => {
        this.$refs.chatInputEditor.clear();
        this.$refs.chatInputEditor.focus();
      });
    },
    scrollToBottom() {
      this.$nextTick(() => {
        let div = document.getElementById("chatScrollBox");
        div.scrollTop = div.scrollHeight;
      });
    },
    refreshPlaceHolder() {
      if (this.isReceipt) {
        this.placeholder = "【回执消息】"
      } else if (this.$refs.editBox && this.$refs.editBox.innerHTML) {
        this.placeholder = ""
      } else {
        this.placeholder = "聊点什么吧~";
      }
    },
    sendMessageRequest(msgInfo){
      return  new Promise((resolve,reject)=>{
        // 请求入队列，防止请求"后发先至"，导致消息错序
        this.reqQueue.push({msgInfo,resolve,reject});
        this.processReqQueue();
      })
    },
    processReqQueue(){
      if (this.reqQueue.length && !this.isSending) {
        this.isSending = true;
        const reqData = this.reqQueue.shift();
        if (this.quoteMsgInfo.msgInfo) {
          reqData.msgInfo.quoteId = this.quoteMsgInfo.msgInfo.id;
          this.quoteMsgInfo.msgInfo = null;
          this.quoteMsgInfo.show = false;
        }
        this.$http({
          url: this.messageAction,
          method: 'post',
          data: reqData.msgInfo
        }).then((res)=>{
          reqData.resolve(res)
        }).catch((e)=>{
          reqData.reject(e)
        }).finally(()=>{
          this.isSending = false;
          // 发送下一条请求
          this.processReqQueue();
        })
      }
    },
    generateId(){
      // 生成临时id
      return String(new Date().getTime()) + String(Math.floor(Math.random() * 1000));
    },
    scrollToTargetMsg(messageId) {
      const element = this.$refs[`message-${messageId}`][0];
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center' });

        // 高亮显示源消息
        this.highlightedMessageId = messageId;
        setTimeout(() => {
          this.highlightedMessageId = null;
        }, 2000);
      }
    },
    playVideo(data) {
      this.videoUrl = data.videoUrl;
      this.posterUrl = data.coverImageUrl;
      this.$refs.videoPlay.onPlayVideo()
    },
    closeVideoPlay() {
      this.videoUrl = '';
      this.posterUrl = '';
    },
    onAudioStateChange(state, msgInfo) {
      const playingAudio = this.$refs['message-item-' + msgInfo.id][0]
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
      return this.$store.state.userStore.userInfo;
    },
    title() {
      let title = this.regionGroup.regionGroupName;
      let size = this.regionGroupMembers.filter(m => !m.quit).length;
      title += `(${size})`;
      return title;
    },
    messageAction() {
      return `/message/regionGroup/send`;
    },
    unreadCount() {
      return this.chat.unreadCount;
    },
    messageSize() {
      if (!this.chat || !this.chat.messages) {
        return 0;
      }
      return this.chat.messages.length;
    },
    memberSize() {
      return this.regionGroupMembers.filter(m => !m.quit).length;
    },
    isGroupOwner() {
      return this.regionGroup.leaderId === this.mine.id;
    },
    friendsMap() {
      return this.$store.state.friendStore.friends.reduce((map, friend) => {
        map[friend.id] = friend;
        return map;
      }, new Map())
    }
  },
  watch: {
    chat: {
      handler(newChat, oldChat) {
        if (newChat.targetId > 0) {
          this.loadRegionGroup(this.chat.targetId);
          // 滚到底部
          this.scrollToBottom();
          this.showSide = false;
          this.isInBottom = true;
          // 消息已读
          this.readedMessage()
          // 初始状态只显示30条消息
          let size = this.chat.messages.length;
          this.showMinIdx = size > 30 ? size - 30 : 0;
          // 重置输入框
          this.resetEditor();
          // 复位回执消息
          this.isReceipt = false;
        }
      },
      immediate: true
    },
    messageSize: {
      handler(newSize, oldSize) {
        if (newSize > oldSize) {
          if (this.isInBottom) {
            // 拉至底部
            this.scrollToBottom();
          } else {
            // 增加新消息提醒
            this.newMessageSize++;
          }
        }
      }
    },
    regionGroup: {
      handler(newRegionGroup, oldRegionGroup) {
      }
    }
  },
  mounted() {
    let div = document.getElementById("chatScrollBox");
    div.addEventListener('scroll', this.throttle(this.onScroll, 100));
  },
}
</script>

<style scoped lang="scss">
.chat-box {
  position: relative;
  width: 100%;
  background: #fff;
  border: var(--border-color) solid 1px;

  .region-group-name {
    color: orange;
  }

  .el-header {
    padding: 5px;
    background-color: white;
    line-height: 50px;
    font-size: 20px;
    font-weight: 600;
    border-bottom: 1px solid #EBEEF5;

    .btn-side {
      position: absolute;
      right: 20px;
      line-height: 60px;
      font-size: 22px;
      cursor: pointer;
    }
  }

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

  .content-box {
    .im-chat-main {
      padding: 0;
      border: var(--border-color) solid 1px;
      .im-chat-box {
        >ul {
          padding: 0px 20px 20px 20px;

          li {
            list-style-type: none;
          }

          .message-wrapper {
            animation: fadeIn 0.3s ease;
            margin-bottom: 15px;
          }

          .message-wrapper[data-highlight="true"] {
            animation: highlight 2s ease;
          }
        }
      }
    }
  }

  .scroll-to-bottom {
    text-align: right;
    position: absolute;
    right: 20px;
    bottom: 250px;
    color: #2830d3;
    font-size: 14px;
    font-weight: 600;
    background: #eee;
    padding: 5px 15px;
    border-radius: 15px;
    cursor: pointer;
    z-index: 99;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .im-chat-footer {
    display: flex;
    flex-direction: column;
    padding: 0;
    border: var(--border-color) solid 1px;

    .chat-tool-bar {

      display: flex;
      position: relative;
      width: 100%;
      height: 40px;
      text-align: left;
      box-sizing: border-box;
      border: var(--border-color) solid 1px;
      padding: 2px;

      >div {
        font-size: 22px;
        cursor: pointer;
        color: #333333;
        line-height: 34px;
        width: 34px;
        height: 34px;
        text-align: center;
        border-radius: 3px;

        &:hover {
          color: black;
        }

        &.chat-tool-active {
          background: #ddd;
        }
      }

      .icon {
        font-size: 22px;
        height: 38px;
      }
    }

    .send-content-area {
      position: relative;
      display: flex;
      flex-direction: column;
      height: 100%;
      background-color: white !important;

      .send-text-area {
        box-sizing: border-box;
        padding: 5px;
        width: 100%;
        flex: 1;
        resize: none;
        font-size: 16px;
        color: black;
        outline-color: rgba(83, 160, 231, 0.61);
        text-align: left;
        line-height: 30px;

        &:before {
          content: attr(placeholder);
          color: gray;
        }

        .at {
          color: blue;
          font-weight: 600;
        }

        .receipt {
          color: darkblue;
          font-size: 15px;
          font-weight: 600;
        }

        .emo {
          width: 30px;
          height: 30px;
          vertical-align: bottom;
        }
      }

      .send-image-area {
        text-align: left;
        border: #53a0e7 solid 1px;

        .send-image-box {
          position: relative;
          display: inline-block;

          .send-image {
            max-height: 180px;
            border: 1px solid #ccc;
            border-radius: 2%;
            margin: 2px;
          }

          .send-image-close {
            position: absolute;
            padding: 3px;
            right: 7px;
            top: 7px;
            color: white;
            cursor: pointer;
            font-size: 15px;
            font-weight: 600;
            background-color: #aaa;
            border-radius: 50%;
            border: 1px solid #ccc;
          }
        }

      }

      .send-btn-area {
        padding: 10px;
        position: absolute;
        bottom: 0;
        right: 0;
      }
    }
  }

  .chat-group-side-box {
    border: var(--border-color) solid 1px;
    animation: rtl-drawer-in .3s 1ms;
  }
}
</style>