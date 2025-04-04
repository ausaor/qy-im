<template>
  <div class="chat-box" @click="closeRefBox()" @mousemove="readedMessage()">
    <el-container>
      <el-header height="60px">
        <span>{{title}}</span>
        <span class="btn-side" v-show="this.chat.type=='GROUP'">
          <span title="刷新群聊信息" style="color: greenyellow;margin-right: 10px" class="el-icon-refresh" @click="loadGroup(chat.targetId)"></span>
          <span title="群聊信息" class="el-icon-more" @click="showSide=!showSide"></span>
        </span>

      </el-header>
      <el-main style="padding: 0;">
        <el-container>
          <el-container class="content-box">
            <el-main class="im-chat-main" id="chatScrollBox" @scroll="onScroll">
              <div class="im-chat-box">
                <ul>
                  <li v-for="(msgInfo,idx) in chat.messages" :key="msgInfo.id ? msgInfo.id : msgInfo.uid" :ref="`message-${msgInfo.id}`"
                      :data-highlight="highlightedMessageId === msgInfo.id" class="message-wrapper">
                    <chat-message-item
                        v-if="idx>=showMinIdx"
                        @call="onCall(msgInfo.type)"
                        :mine="msgInfo.sendId == mine.id"
                        :showInfo="showInfo(msgInfo)"
                        :msgInfo="msgInfo"
                        :isOwner="group.ownerId === msgInfo.sendId"
                        :myGroupMemberInfo="myGroupMemberInfo"
                        :groupMembers="groupMembers"
                        @delete="deleteMessage"
                        @recall="recallMessage"
                        @quote="quoteMessage"
                        @scrollToMessage="scrollToTargetMsg">
                    </chat-message-item>
                  </li>
                </ul>
              </div>
            </el-main>
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
                <div title="回执消息" v-show="chat.type == 'GROUP'" :class="isReceipt ? 'chat-tool-active' : ''" @click="onSwitchReceipt">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-yihuizhi"></use>
                  </svg>
                </div>
                <div title="发送语音" @click="showRecordBox()">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-yuyin"></use>
                  </svg>
                </div>
                <div title="语音通话" v-show="chat.type == 'PRIVATE'" @click="showPrivateVideo('voice')">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-yuyintonghua"></use>
                  </svg>
                </div>
                <div title="语音通话" v-show="chat.type == 'GROUP'" @click="onGroupVideo()">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-yuyintonghua"></use>
                  </svg>
                </div>
                <div title="视频通话" v-show="chat.type=='PRIVATE'" @click="showPrivateVideo('video')">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-shipin"></use>
                  </svg>
                </div>
                <div title="角色台词" ref="characterWord" v-show="chat.type == 'GROUP' && myGroupMemberInfo.isTemplate" @click.stop="showWordBox()">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-minganci"></use>
                  </svg>
                </div>
                <div title="角色表情包" ref="characterEmo" v-show="chat.type == 'GROUP' && myGroupMemberInfo.isTemplate" @click.stop="showCharacterEmoBox()">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-biaoqing"></use>
                  </svg>
                </div>
                <div title="聊天记录" @click="showHistoryBox()">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-lishijilu"></use>
                  </svg>
                </div>
              </div>
              <div class="send-content-area">
                <ChatInput :ownerId="group.ownerId" ref="chatInputEditor" :group-members="groupMembers"
                           :show-nick-name="group.groupType !== 0" :quote-message="quoteMsgInfo"
                           @submit="sendMessage" @removeQuoteMsg="cancelQuote"/>
                <div class="send-btn-area">
                  <el-button type="primary" size="small" @click="notifySend()">发送</el-button>
                </div>
              </div>
            </el-footer>
          </el-container>
          <el-aside class="chat-group-side-box" width="300px" v-show="showSide">
            <chat-group-side :group="group" :groupMembers="groupMembers" @reload="loadGroup(group.id)"
                             :myGroupMemberInfo="myGroupMemberInfo"
                             :friends="friends"
                             @change="modifyMyGroupMember">
            </chat-group-side>
          </el-aside>
        </el-container>
      </el-main>
      <emotion ref="emoBox" @emotion="onEmotion"></Emotion>
      <character-emotion ref="characterEmoBox" :emos="emos" @emotion="onCharacterEmotion"></character-emotion>
      <character-word ref="wordBox" :words="words" @send="sendWordVoice"></character-word>
      <chat-record :visible="showRecord" @close="closeRecordBox" @send="onSendRecord"></chat-record>
      <group-member-selector ref="rtcSel" :groupId="group.id" @complete="onInviteOk"></group-member-selector>
      <rtc-group-join ref="rtcJoin" :groupId="group.id"></rtc-group-join>
      <chat-history :visible="showHistory" :chat="chat" :friend="friend" :group="group" :groupMembers="groupMembers" @close="closeHistoryBox"></chat-history>
    </el-container>
  </div>
</template>

<script>
	import ChatGroupSide from "./ChatGroupSide.vue";
	import ChatMessageItem from "./ChatMessageItem.vue";
	import FileUpload from "../common/FileUpload.vue";
	import Emotion from "../common/Emotion.vue";
	import ChatRecord from "@/components/chat/ChatRecord";
	import ChatHistory from "./ChatHistory.vue";
  import ChatAtBox from "./ChatAtBox.vue"
  import ChatInput from "./ChatInput";
  import GroupMemberSelector from "../group/GroupMemberSelector.vue"
  import RtcGroupJoin from "../rtc/RtcGroupJoin.vue"
  import CharacterWord from "@/components/common/CharacterWord";
  import CharacterEmotion from "@/components/common/CharacterEmotion";

	export default {
		name: "chatPrivate",
		components: {
			ChatMessageItem,
			FileUpload,
			ChatGroupSide,
			Emotion,
      ChatRecord,
			ChatHistory,
      ChatAtBox,
      ChatInput,
      GroupMemberSelector,
      RtcGroupJoin,
      CharacterWord,
      CharacterEmotion,
		},
		props: {
			chat: {
				type: Object
			}
		},
		data() {
			return {
				friend: {},
				group: {},
				groupMembers: [],
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
        words: {
          group: [],
          character: []
        },
        emos: {
				  group: [],
          character: []
        },
        quoteMsgInfo: {
          msgInfo: null,
          show: false
        },
        highlightedMessageId: null,
			}
		},
    created() {
      this.friends = this.$store.state.friendStore.friends;
    },
    methods: {
      moveChatToTop() {
        let chatIdx = this.$store.getters.findChatIdx(this.chat);
        this.$store.commit("moveTop", chatIdx);
      },
      closeRefBox() {
        //this.$refs.emoBox.close();
        // this.$refs.atBox.close();
        //this.$refs.wordBox.close();
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
        msgInfo.chatType = this.chat.type;
        this.sendMessageRequest(msgInfo).then((m) => {
          msgInfo.loadStatus = 'ok';
          msgInfo.id = m.id;
          this.isReceipt = false;
          this.$store.commit("insertMessage", msgInfo);
        })
      },
      onImageFail(e, file) {
        let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
        msgInfo.loadStatus = 'fail';
        msgInfo.chatType = this.chat.type;
        this.$store.commit("insertMessage", msgInfo);
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
          status: this.$enums.MESSAGE_STATUS.UNSEND,
          chatType: this.chat.type,
        }
        // 填充对方id
        this.fillTargetId(msgInfo, this.chat.targetId);
        // 插入消息
        this.$store.commit("insertMessage", msgInfo);
        // 会话置顶
        this.moveChatToTop();
        // 滚动到底部
        this.scrollToBottom();
        // 借助file对象保存
        file.msgInfo = msgInfo;
      },
      onVideoFail(e, file) {
        let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
        msgInfo.loadStatus = 'fail';
        msgInfo.chatType = this.chat.type;
        this.$store.commit("insertMessage", msgInfo);
      },
      onVideoSuccess(data, file) {
        let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
        msgInfo.content = JSON.stringify(data);
        msgInfo.receipt = this.isReceipt;
        msgInfo.chatType = this.chat.type;
        this.sendMessageRequest(msgInfo).then((m) => {
          msgInfo.loadStatus = 'ok';
          msgInfo.id = m.id;
          this.isReceipt = false;
          this.$store.commit("insertMessage", msgInfo);
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
          chatType: this.chat.type,
        }
        // 填充对方id
        this.fillTargetId(msgInfo, this.chat.targetId);
        // 插入消息
        this.$store.commit("insertMessage", msgInfo);
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
        msgInfo.chatType = this.chat.type;
        this.$store.commit("insertMessage", msgInfo);
      },
      onAudioSuccess(data, file) {
        let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
        msgInfo.content = JSON.stringify(data);
        msgInfo.receipt = this.isReceipt;
        msgInfo.chatType = this.chat.type;
        this.sendMessageRequest(msgInfo).then((m) => {
          msgInfo.loadStatus = 'ok';
          msgInfo.id = m.id;
          this.isReceipt = false;
          this.$store.commit("insertMessage", msgInfo);
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
          chatType: this.chat.type,
        }
        // 填充对方id
        this.fillTargetId(msgInfo, this.chat.targetId);
        // 插入消息
        this.$store.commit("insertMessage", msgInfo);
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
          msgInfo.chatType = this.chat.type;
					this.$store.commit("insertMessage", msgInfo);
				})
			},
      onFileFail(res, file) {
				let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
				msgInfo.loadStatus = 'fail';
        msgInfo.chatType = this.chat.type;
				this.$store.commit("insertMessage", msgInfo);
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
          status: this.$enums.MESSAGE_STATUS.UNSEND,
          chatType: this.chat.type,
				}
				// 填充对方id
				this.fillTargetId(msgInfo, this.chat.targetId);
				// 插入消息
				this.$store.commit("insertMessage", msgInfo);
        // 会话置顶
        this.moveChatToTop();
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
        }
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
      showWordBox() {
        this.getCharacterWord().then((data) => {
          this.words.group = data.group;
          this.words.character = data.character;
          let width = this.$refs.characterWord.offsetWidth;
          let left = this.$elm.fixLeft(this.$refs.characterWord);
          let top = this.$elm.fixTop(this.$refs.characterWord);
          this.$refs.wordBox.open({
            x: left + width / 2,
            y: top
          })
        });
      },
      showCharacterEmoBox() {
        this.getCharacterEmo().then((data) => {
          this.emos.group = data.group;
          this.emos.character = data.character;
          let width = this.$refs.characterEmo.offsetWidth;
          let left = this.$elm.fixLeft(this.$refs.characterEmo);
          let top = this.$elm.fixTop(this.$refs.characterEmo);
          this.$refs.characterEmoBox.open({
            x: left + width / 2,
            y: top
          })
        })
      },
      getCharacterWord() {
        return new Promise((resolve, reject) => {
          this.$http({
            url: `/character/word/publishedWord?characterId=${this.myGroupMemberInfo.templateCharacterId}`,
            method: "get",
          }).then((data) => {
            resolve(data)
          })
          // let regionGroup = this.$store.state.regionGroupStore.regionGroups.find((g) => g.id == id);
          // if (regionGroup) {
          //   resolve(regionGroup);
          // } else {
          //
          // }
        });

      },
      getCharacterEmo() {
        return new Promise((resolve, reject) => {
          this.$http({
            url: `/character/emo/publishedEmo?characterId=${this.myGroupMemberInfo.templateCharacterId}`,
            method: "get",
          }).then((data) => {
            resolve(data)
          })
        });
      },
      onEmotion(emoText) {
        this.$refs.chatInputEditor.insertEmoji(emoText);
      },
      onCharacterEmotion(emo) {
        this.$refs.chatInputEditor.insertImage(emo);
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
      sendWordVoice(data) {
        let content = {
          id: data.id,
          templateGroupId: data.templateGroupId,
          characterId: data.characterId,
          characterName: data.characterName,
          word: data.word,
          voice: data.voice
        }
        let msgInfo = {
          content: JSON.stringify(content),
          type: 5,
          receipt: this.isReceipt
        }
        // 填充对方id
        this.fillTargetId(msgInfo, this.chat.targetId);
        this.sendMessageRequest(msgInfo).then((m) => {
          m.selfSend = true;
          m.chatType = this.chat.type;
          this.$store.commit("insertMessage", m);
          // 会话置顶
          this.moveChatToTop();
          // 保持输入框焦点
          this.$refs.chatInputEditor.focus();
          // 滚动到底部
          this.scrollToBottom();
          this.isReceipt = false;
          this.refreshPlaceHolder();
        })
      },
      sendExistsImage(obj) {
        return new Promise((resolve,reject)=>{
          let data = obj.imgObj
          let content = {
            id: data.id,
            templateGroupId: data.templateGroupId,
            characterId: data.characterId,
            characterName: data.characterName,
            name: data.name,
            originUrl: data.url
          }
          let msgInfo = {
            content: JSON.stringify(content),
            type: 1,
            receipt: this.isReceipt
          }
          // 填充对方id
          this.fillTargetId(msgInfo, this.chat.targetId);
          this.sendMessageRequest(msgInfo).then((m) => {
            m.selfSend = true;
            m.chatType = this.chat.type;
            this.$store.commit("insertMessage", m);
            // 会话置顶
            this.moveChatToTop();
            // 保持输入框焦点
            this.$refs.chatInputEditor.focus();
          }).finally(() => {
            // 解除锁定
            this.scrollToBottom();
            this.isReceipt = false;
            this.refreshPlaceHolder();
            resolve();
          })
        });
      },
      onSendRecord(data) {
        let msgInfo = {
          content: JSON.stringify(data),
          type: 3,
          receipt: this.isReceipt
        }
        // 填充对方id
        this.fillTargetId(msgInfo, this.chat.targetId);
        this.sendMessageRequest(msgInfo).then((m) => {
          m.selfSend = true;
          m.chatType = this.chat.type;
          this.$store.commit("insertMessage", m);
          // 会话置顶
          this.moveChatToTop();
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
				if (this.chat.type == "GROUP") {
					msgInfo.groupId = targetId;
				} else {
					msgInfo.recvId = targetId;
				}
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
              if (msg.content.exists) {
                await this.sendExistsImage(msg.content);
              } else {
                await this.sendImageMessage(msg.content.file);
              }
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
          if (this.chat.type == "GROUP") {
            msgInfo.atUserIds = atUserIds;
            msgInfo.receipt = this.isReceipt;
          }
          this.lockMessage = true;
          this.sendMessageRequest(msgInfo).then((m) => {
            m.selfSend = true;
            m.chatType = this.chat.type;
            this.$store.commit("insertMessage", m);
            // 会话置顶
            this.moveChatToTop();
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
          this.$store.commit("deleteMessage", msgInfo);
        });
      },
      recallMessage(msgInfo) {
        this.$confirm('确认撤回消息?', '撤回消息', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let url = `/message/${this.chat.type.toLowerCase()}/recall/${msgInfo.id}`
          this.$http({
            url: url,
            method: 'delete'
          }).then(() => {
            this.$message.success("消息已撤回");
            msgInfo = JSON.parse(JSON.stringify(msgInfo));
            msgInfo.type = 10;
            msgInfo.content = '你撤回了一条消息';
            msgInfo.status = this.$enums.MESSAGE_STATUS.RECALL;
            msgInfo.chatType = this.chat.type;
            this.$store.commit("insertMessage", msgInfo);
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
        this.$store.commit("resetUnreadCount", this.chat)
        if (this.chat.type == "GROUP") {
          var url = `/message/group/readed?groupId=${this.chat.targetId}`
        } else {
          url = `/message/private/readed?friendId=${this.chat.targetId}`
        }
        this.$http({
          url: url,
          method: 'put'
        }).then(() => {})
      },
      loadReaded(fId) {
        this.$http({
          url: `/message/private/maxReadedId?friendId=${fId}`,
          method: 'get'
        }).then((id) => {
          this.$store.commit("readedMessage", {
            friendId: fId,
            maxId: id
          });
        });
      },
			loadGroup(groupId) {
				this.$http({
					url: `/group/find/${groupId}`,
					method: 'get'
				}).then((group) => {
					this.group = group;
					this.$store.commit("updateChatFromGroup", group);
					this.$store.commit("updateGroup", group);
				});

				this.$http({
					url: `/group/members/${groupId}`,
					method: 'get'
				}).then((groupMembers) => {
          this.myGroupMemberInfo = groupMembers.find((m) => m.userId === this.mine.id);
					this.groupMembers = groupMembers;
				});
			},
			loadFriend(friendId) {
				// 获取对方最新信息
				this.$http({
					url: `/user/find/${friendId}`,
					method: 'get'
				}).then((friend) => {
					this.friend = friend;
					this.$store.commit("updateChatFromFriend", friend);
					this.$store.commit("updateFriend", friend);
				})
			},
			showName(msgInfo) {
				if (this.chat.type == 'GROUP') {
				  // 是普通群聊
				  if (this.group.groupType === 0) {
            let friend = this.friends.find((f) => f.id === msgInfo.sendId);
            if (friend && friend.friendRemark) {
              return friend.friendRemark;
            }
          }
					let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
					return member ? member.aliasName : "";
				} else {
					return msgInfo.sendId == this.mine.id ? this.mine.nickName : this.chat.showName
				}
			},
      nickName(msgInfo) {
        if (this.chat.type === 'GROUP') {
          let friend = this.friends.find((f) => f.id === msgInfo.sendId);
          if (friend && friend.friendRemark) {
            return friend.friendRemark;
          }
          let member = this.groupMembers.find((m) => m.userId === msgInfo.sendId);
          return member ? member.nickName : "";
        } else {
          return "";
        }
      },
			headImage(msgInfo) {
				if (this.chat.type == 'GROUP') {
					let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
					return member ? member.headImage : "";
				} else {
					return msgInfo.sendId == this.mine.id ? this.mine.headImage : this.chat.headImage
				}
			},
      showInfo(msgInfo) {
        console.log("showInfo")
        let showInfoObj = {
          showName: "",
          headImage: "",
          nickName: "",
          characterNum: null,
          quoteShowName: '',
        };
        if (this.chat.type == 'GROUP') {
          let friend = this.friends.find((f) => f.id === msgInfo.sendId);
          if (friend) {
            if (friend.friendRemark) {
              showInfoObj.nickName = friend.friendRemark;
            } else if (friend.friendNickName) {
              showInfoObj.nickName = friend.friendNickName;
            }
          }
          // 是普通群聊
          if (this.group.groupType === 0) {
            if (friend) {
              if (friend.friendRemark) {
                showInfoObj.showName = friend.friendRemark;
              } else if (friend.friendNickName) {
                showInfoObj.showName = friend.friendNickName;
              }
            }
          }
          let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
          if (msgInfo.quoteMsg) {
            let member2 = this.groupMembers.find((m) => m.userId == msgInfo.quoteMsg.sendId);
            showInfoObj.quoteShowName = member2 ? member2.aliasName : "";
          }
          if (member) {
            showInfoObj.characterNum = member.characterNum;
            showInfoObj.headImage = member.headImage;
            if (!showInfoObj.showName) {
              showInfoObj.showName = member.aliasName;
            }
            if (!showInfoObj.nickName) {
              showInfoObj.nickName = member.nickName;
            }
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
        } else {
          msgInfo.sendId == this.mine.id ? showInfoObj.showName = this.mine.nickName : showInfoObj.showName = this.chat.showName;
          msgInfo.sendId == this.mine.id ? showInfoObj.headImage = this.mine.headImage : showInfoObj.headImage = this.chat.headImage;
          if (msgInfo.quoteMsg) {
            msgInfo.quoteMsg.sendId == this.mine.id ? showInfoObj.quoteShowName = this.mine.nickName : showInfoObj.quoteShowName = this.chat.showName;
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
      modifyMyGroupMember(showNickName) {
        this.myGroupMemberInfo.showNickName = showNickName;
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
      }
		},
		computed: {
			mine() {
				return this.$store.state.userStore.userInfo;
			},
			title() {
        let title = "";
				if (this.chat.type == "GROUP") {
          title = this.chat.showName;
          let size = this.groupMembers.filter(m => !m.quit).length;
					title += `(${size})`;
				} else if (this.chat.type == "PRIVATE") {
          title = this.chat.friendRemark ? this.chat.friendRemark : this.chat.showName;
        }
				return title;
			},
			messageAction() {
				return `/message/${this.chat.type.toLowerCase()}/send`;
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
		},
		watch: {
			chat: {
        handler(newChat, oldChat) {
          if (newChat.targetId > 0 && (!oldChat || newChat.type != oldChat.type ||
              newChat.targetId != oldChat.targetId)) {
            if (this.chat.type == "GROUP") {
              this.loadGroup(this.chat.targetId);
            } else {
              this.groupMembers = [];
              this.loadFriend(this.chat.targetId);
              // 加载已读状态
              this.loadReaded(this.chat.targetId)
            }
            // 滚到底部
            this.scrollToBottom();
            this.showSide = false;
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
            // 拉至底部
            this.scrollToBottom();
          }
        }
      }
		},
    mounted() {
      let div = document.getElementById("chatScrollBox");
      div.addEventListener('scroll', this.onScroll)
    },
	}
</script>

<style lang="scss">
	.chat-box {
    position: relative;
    width: 100%;
    background: #fff;
		border: var(--border-color) solid 1px;

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

				&:hover {
					font-size: 30px;
				}
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
