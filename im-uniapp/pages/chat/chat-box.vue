<template>
	<view class="page chat-box" id="chatBox" :chatidx="chatIdx">
		<nav-bar backHome more @more="onShowMore" @gotoHome="gotoHome" :unread-count="groupUnreadCount">{{ title }}</nav-bar>
		<view class="chat-main-box" :style="{height: chatMainHeight+'px'}">
			<view class="chat-msg" @click="switchChatTabBox('none')">
				<scroll-view ref="messagesContainer" class="scroll-box" scroll-y="true" upper-threshold="200" @scroll="onScroll"
                     @scrolltoupper="onScrollToTop" @scrolltolower="onScrollToBottom"
					:scroll-into-view="'chat-item-' + scrollMsgIdx" :scroll-top="scrollTop">
          <view v-if="chat" class="chat-wrap">
            <view v-for="(msgInfo, idx) in chat.messages" :key="msgInfo.id ? msgInfo.id : msgInfo.uid"
                  class="message-wrapper" :class="{active: targetMsgId === msgInfo.id}">
              <chat-message-item :ref="'message'+msgInfo.id" v-if="idx >= showMinIdx"
                                 @call="onRtCall(msgInfo)" :showInfo="showInfo(msgInfo)"
                                 @recall="onRecallMessage" @delete="onDeleteMessage" @copy="onCopyMessage"
                                 @longPressHead="onLongPressHead(msgInfo)" @download="onDownloadFile"
                                 @quote="quoteMessage" @scrollToMessage="scrollToTargetMsg" @playVideo="playVideo"
                                 @audioStateChange="onAudioStateChange" :id="'chat-item-' + idx" :msgInfo="msgInfo"
                                 :groupMembers="groupMembers" :myGroupMemberInfo="myGroupMemberInfo" :isOwner="group.ownerId === msgInfo.sendId">
              </chat-message-item>
            </view>
          </view>
				</scroll-view>
        <view v-if="!isInBottom" class="scroll-to-bottom" @click="onClickToBottom">
          {{ newMessageSize > 0 ?  newMessageSize+'条新消息' :'回到底部'}}
        </view>
			</view>
			<view v-if="atUserIds.length > 0" class="chat-at-bar" @click="openAtBox()">
				<view class="iconfont icon-at">:&nbsp;</view>
				<scroll-view v-if="atUserIds.length > 0" class="chat-at-scroll-box" scroll-x="true" scroll-left="120">
					<view class="chat-at-items">
						<view v-for="m in atUserItems" class="chat-at-item" :key="m.userId">
							<head-image :name="m.aliasName" :url="m.headImage" size="minier"></head-image>
						</view>
					</view>
				</scroll-view>
			</view>
      <character-word-list ref="characterWordList" :words="words" @confirm="chooseCharacterWord"></character-word-list>
			<view class="send-bar">
				<view v-if="!showRecord" class="iconfont icon-voice-circle" @click="onRecorderInput()"></view>
				<view v-else class="iconfont icon-keyboard" @click="onKeyboardInput()"></view>
				<chat-record v-if="showRecord" class="chat-record" @send="onSendRecord"></chat-record>
				<view v-else class="send-text">
					<editor id="editor" class="send-text-area" :placeholder="isReceipt ? '[回执消息]' : ''"
						:read-only="isReadOnly" @focus="onEditorFocus" @blur="onEditorBlur" @ready="onEditorReady" @input="onTextInput">
					</editor>
          <view class="quote-message" v-if="quoteMsgInfo.show">
            <view class="quote-text">{{quoteContent}}</view>
            <uni-icons type="clear" size="20" color="#888888" @click="cancelQuote"></uni-icons>
          </view>
				</view>
				<view v-if="chat && chat.type == 'GROUP'" class="iconfont icon-at" @click="openAtBox()"></view>
				<view class="iconfont icon-icon_emoji" @click="onShowEmoChatTab()"></view>
				<view v-if="isEmpty" class="iconfont icon-add" @click="onShowToolsChatTab()">
				</view>
				<button v-if="!isEmpty || atUserIds.length" class="btn-send" type="primary"
					@touchend.prevent="sendTextMessage()" size="mini">发送</button>
			</view>
		</view>
		<view class="chat-tab-bar">
			<view v-if="chatTabBox == 'tools'" class="chat-tools" :style="{height: keyboardHeight+'px'}">
				<view class="chat-tools-item">
					<file-upload ref="fileUpload" :onBefore="onUploadFileBefore" :onSuccess="onUploadFileSuccess"
						:onError="onUploadFileFail">
            <svg-icon class="tool-icon" icon-class="icon-wenjian"></svg-icon>
					</file-upload>
					<view class="tool-name">文件</view>
				</view>
				<view class="chat-tools-item">
					<image-upload :maxCount="9" sourceType="album" :onBefore="onUploadImageBefore"
						:onSuccess="onUploadImageSuccess" :onError="onUploadImageFail">
            <svg-icon class="tool-icon" icon-class="tupian"></svg-icon>
					</image-upload>
					<view class="tool-name">相册</view>
				</view>
        <view class="chat-tools-item">
          <video-upload sourceType="album" :onBefore="onUploadVideoBefore" :onSuccess="onUploadVideoSuccess" :onError="onUploadVideoFail">
            <svg-icon class="tool-icon" icon-class="mv"></svg-icon>
          </video-upload>
          <view class="tool-name">视频</view>
        </view>
				<view class="chat-tools-item" @click="onRecorderInput()">
          <svg-icon class="tool-icon" icon-class="yuyin"></svg-icon>
					<view class="tool-name">语音消息</view>
				</view>
				<view v-if="chat.type == 'GROUP'" class="chat-tools-item" @click="switchReceipt()">
          <svg-icon class="tool-icon" :class="isReceipt ? 'active' : ''" icon-class="yihuizhi"></svg-icon>
					<view class="tool-name">回执消息</view>
				</view>
				<!-- #ifndef MP-WEIXIN -->
				<!-- 音视频不支持小程序 -->
				<view v-if="chat.type == 'PRIVATE'" class="chat-tools-item" @click="onPriviteVideo()">
          <svg-icon class="tool-icon" icon-class="shipin"></svg-icon>
					<view class="tool-name">视频通话</view>
				</view>
				<view v-if="chat.type == 'PRIVATE'" class="chat-tools-item" @click="onPriviteVoice()">
          <svg-icon class="tool-icon" icon-class="yuyintonghua"></svg-icon>
					<view class="tool-name">语音通话</view>
				</view>
				<view v-if="chat.type == 'GROUP'" class="chat-tools-item" @click="onGroupVideo()">
          <svg-icon class="tool-icon" icon-class="yuyintonghua"></svg-icon>
					<view class="tool-name">语音通话</view>
				</view>
        <view v-if="chat.type == 'GROUP' && myGroupMemberInfo.isTemplate" class="chat-tools-item" @click="onCharacterWord()">
          <svg-icon class="tool-icon" icon-class="minganci"></svg-icon>
          <view class="tool-name">角色台词</view>
        </view>
        <view v-if="chat.type == 'GROUP' && myGroupMemberInfo.isTemplate" class="chat-tools-item" @click="onShowCharacterEmo()">
          <svg-icon class="tool-icon" icon-class="biaoqing"></svg-icon>
          <view class="tool-name">角色表情</view>
        </view>
				<!-- #endif -->
			</view>
			<scroll-view v-if="chatTabBox === 'emo'" class="chat-emotion" scroll-y="true"
				:style="{height: keyboardHeight+'px'}">
				<view class="emotion-item-list">
					<image class="emotion-item emoji-large" :title="emoText" :src="$emo.textToPath(emoText)"
						v-for="(emoText, i) in $emo.emoTextList" :key="i" @click="selectEmoji(emoText)" mode="aspectFit"
						lazy-load="true"></image>
				</view>
			</scroll-view>
      <scroll-view v-if="chatTabBox === 'characterEmo'" class="character-emotion" scroll-y="true"
				:style="{height: keyboardHeight+'px'}">
        <swiper class="emo-swiper" :indicator-dots="true">
          <swiper-item v-if="characterEmos.group.length !== 0" class="emo-swiper-item">
            <image class="emotion-item" :src="emo.url"
                   v-for="(emo, i) in characterEmos.group" :key="i" @click="selectCharacterEmoji(emo)" mode="aspectFit"
                   lazy-load="true"></image>
          </swiper-item>
          <swiper-item v-if="characterEmos.character.length !== 0" class="emo-swiper-item">
            <image class="emotion-item" :src="emo.url"
                   v-for="(emo, i) in characterEmos.character" :key="i" @click="selectCharacterEmoji(emo)" mode="aspectFit"
                   lazy-load="true"></image>
          </swiper-item>
        </swiper>
      </scroll-view>
		</view>
		<!-- @用户时选择成员 -->
		<chat-at-box ref="atBox" :ownerId="group.ownerId" :members="groupMembers"
			@complete="onAtComplete"></chat-at-box>
    <!-- 视频弹窗组件 -->
    <video-play
        v-if="viewVideo"
        :video-url="videoSrc"
        :cover-url="videoCoverImage"
        :visible.sync="viewVideo"
        @close="handleVideoClose"
        @error="handleVideoError"
    ></video-play>
		<!-- 群语音通话时选择成员 -->
		<!-- #ifndef MP-WEIXIN -->
		<group-member-selector ref="selBox" :members="groupMembers" :maxSize="configStore.webrtc.maxChannel"
			@complete="onInviteOk"></group-member-selector>
		<group-rtc-join ref="rtcJoin" :groupId="group.id"></group-rtc-join>
		<!-- #endif -->
	</view>
</template>

<script>
import characterWordList from "../../components/character-word-list/character-word-list.vue";
import VideoUpload from "../../components/video-upload/video-upload.vue";
import SvgIcon from "../../components/svg-icon/svg-icon.vue";
import VideoPlay from "../../components/video-play/video-play.vue";

export default {
  components: {
    VideoPlay,
    SvgIcon,
    VideoUpload,
    characterWordList
  },
	data() {
		return {
      chatIdx: null,
			chat: {},
			friend: {},
			group: {},
			groupMembers: [],
      groupMembersMap: new Map(),
      myGroupMemberInfo: {}, // 我的群聊成员信息
			isReceipt: false, // 是否回执消息
			scrollMsgIdx: 0, // 滚动条定位为到哪条消息
      targetMsgId: null,
			chatTabBox: 'none',
			showRecord: false,
			chatMainHeight: 0, // 聊天窗口高度
			keyboardHeight: 290, // 键盘高度
			windowHeight: 1000, // 窗口高度
			initHeight: 1000, // h5初始高度
			atUserIds: [],
			showMinIdx: 0, // 下标小于showMinIdx的消息不显示，否则可能很卡
			reqQueue: [], // 请求队列
			isSending: false, // 是否正在发送请求
			isShowKeyBoard: false, // 键盘是否正在弹起 
			editorCtx: null, // 编辑器上下文
			isEmpty: true, // 编辑器是否为空
			isFocus: false, // 编辑器是否焦点
			isReadOnly: false, // 编辑器是否只读
			playingAudio: null, // 当前正在播放的录音消息
      playAudios: [],
      playingAudioState: 'STOP',
      words: {
        group: [],
        character: []
      },
      characterEmos: {
        group: [],
        character: []
      },
      quoteMsgInfo: {
        msgInfo: null,
        quoteContent: '',
        show: false
      },
      videoSrc: '',
      videoCoverImage: '',
      viewVideo: false,
      isInBottom: true, // 滚动条是否在底部
      newMessageSize: 0, // 滚动条不在底部时新的消息数量
      scrollTop: 0, // 用于ios h5定位滚动条
      scrollViewHeight: 0, // 滚动条总长度
      currentScrollTop: 0,          // 当前滚动位置
      lastScrollTop: 0,      // 上一次滚动位置
      scrollDirection: null, // 滚动方向：'up' 或 'down'
      timer: null, // 防抖计时器
		}
	},
	methods: {
		onRecorderInput() {
			this.showRecord = true;
			this.switchChatTabBox('none');
		},
		onKeyboardInput() {
			this.showRecord = false;
			this.switchChatTabBox('none');
		},
		onSendRecord(data) {
			// 检查是否被封禁
			if (this.isBanned) {
				this.showBannedTip();
				return;
			}
			let msgInfo = {
				content: JSON.stringify(data),
				type: this.$enums.MESSAGE_TYPE.AUDIO,
				receipt: this.isReceipt
			}
			// 填充对方id
			this.fillTargetId(msgInfo, this.chat.targetId);
			this.sendMessageRequest(msgInfo).then((m) => {
				m.selfSend = true;
				this.chatStore.insertMessage(m, this.chat);
				// 会话置顶
				this.moveChatToTop();
				// 滚动到底部
				this.scrollToBottom();
				this.isReceipt = false;

			})
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
		onInviteOk(ids) {
			if (ids.length < 2) {
				return;
			}
			let users = [];
			ids.forEach(id => {
				let m = this.groupMembers.find(m => m.userId == id);
				// 只取部分字段,压缩url长度
				users.push({
					id: m.userId,
					nickName: m.aliasName,
					headImage: m.headImage,
					isCamera: false,
					isMicroPhone: true
				})
			})
			const groupId = this.group.id;
			const inviterId = this.mine.id;
			const userInfos = encodeURIComponent(JSON.stringify(users));
			uni.navigateTo({
				url: `/pages/chat/chat-group-video?groupId=${groupId}&isHost=true
						&inviterId=${inviterId}&userInfos=${userInfos}`
			})
		},
		moveChatToTop() {
			let chatIdx = this.chatStore.findChatIdx(this.chat);
			this.chatStore.moveTop(chatIdx);
		},
		switchReceipt() {
			this.isReceipt = !this.isReceipt;
		},
		openAtBox() {
			this.$refs.atBox.init(this.atUserIds);
			this.$refs.atBox.open();
		},
		onAtComplete(atUserIds) {
			this.atUserIds = atUserIds;
		},
		onLongPressHead(msgInfo) {
			if (!msgInfo.selfSend && this.chat.type == "GROUP" && this.atUserIds.indexOf(msgInfo.sendId) < 0) {
				this.atUserIds.push(msgInfo.sendId);
			}
		},
		headImage(msgInfo) {
			if (this.chat.type == 'GROUP') {
				let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
				return member ? member.headImage : "";
			} else {
				return msgInfo.selfSend ? this.mine.headImage : this.chat.headImage
			}
		},
		showName(msgInfo) {
			if (this.chat.type == 'GROUP') {
				let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
				return member ? member.aliasName : "";
			} else {
				return msgInfo.selfSend ? this.mine.nickName : this.chat.showName
			}
		},
    showInfo(msgInfo) {
      let showInfoObj = {
        showName: "",
        headImage: "",
        nickName: "",
        characterNum: null,
        quoteShowName: '',
      };
      if (this.chat.type == 'GROUP') {
        //let friend = this.friends.find((f) => f.id === msgInfo.sendId);
        let friend = this.friendsMap.get(msgInfo.sendId);
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
        //let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
        let member = this.groupMembersMap.get(msgInfo.sendId);
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
        if (msgInfo.quoteMsg) {
          //let member2 = this.groupMembers.find((m) => m.userId == msgInfo.quoteMsg.sendId);
          let member2 = this.groupMembersMap.get(msgInfo.quoteMsg.sendId);
          showInfoObj.quoteShowName = member2 ? member2.aliasName : "";
        }

        if (!showInfoObj.showName) {
          if (msgInfo.sendNickName) {
            showInfoObj.showName = msgInfo.sendNickName;
          }
        }
        if (!showInfoObj.headImage) {
          if (msgInfo.sendUserAvatar && this.group.groupType !== 0) {
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
		sendTextMessage() {
			this.editorCtx.getContents({
				success: (e) => {
					// 清空编辑框数据
					this.editorCtx.clear();
					// 检查是否被封禁
					if (this.isBanned) {
						this.showBannedTip();
						return;
					}
					let sendText = "";
					e.delta.ops.forEach((op) => {
						if (op.insert.image) {
							// emo表情
							sendText += `#${op.attributes.alt};`
						} else(
							// 文字
							sendText += op.insert
						)
					})
					if (!sendText.trim() && this.atUserIds.length == 0) {
						return uni.showToast({
							title: "不能发送空白信息",
							icon: "none"
						});
					}
					let receiptText = this.isReceipt ? "【回执消息】" : "";
					let atText = this.createAtText();
					let msgInfo = {
						content: receiptText + sendText + atText,
						atUserIds: this.atUserIds,
						receipt: this.isReceipt,
						type: 0
					}
					// 清空@成员和回执标记
					this.atUserIds = [];
					this.isReceipt = false;
					// 填充对方id
					this.fillTargetId(msgInfo, this.chat.targetId);
					this.sendMessageRequest(msgInfo).then((m) => {
						m.selfSend = true;
						this.chatStore.insertMessage(m, this.chat);
						// 会话置顶
						this.moveChatToTop();
					}).finally(() => {
						// 滚动到底部
						this.scrollToBottom();
					});
				}
			})

		},
    chooseCharacterWord(data) {
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
        type: this.$enums.MESSAGE_TYPE.WORD_VOICE,
        receipt: this.isReceipt
      }
      // 填充对方id
      this.fillTargetId(msgInfo, this.chat.targetId);
      this.sendMessageRequest(msgInfo).then((m) => {
        m.selfSend = true;
        this.chatStore.insertMessage(m, this.chat);
        // 会话置顶
        this.moveChatToTop();
        // 滚动到底部
        this.scrollToBottom();
        this.isReceipt = false;
      })
    },
		createAtText() {
			let atText = "";
			this.atUserIds.forEach((id) => {
				if (id == -1) {
					atText += ` @全体成员`;
				} else {
					let member = this.groupMembers.find((m) => m.userId == id);
					if (member) {
						atText += ` @${member.aliasName}#{${member.userId}}`;
					}
				}
			})
			return atText;
		},
		fillTargetId(msgInfo, targetId) {
			if (this.chat.type == "GROUP") {
				msgInfo.groupId = targetId;
			} else {
				msgInfo.recvId = targetId;
			}
		},
		scrollToBottom() {
			let size = this.messageSize;
			if (size > 0) {
				this.scrollToMsgIdx(size - 1);
			}
		},
    holdingScrollBar(scrollViewHeight) {
      // 内容高度
      const query = uni.createSelectorQuery().in(this);
      setTimeout(() => {
        query.select('.chat-wrap').boundingClientRect();
        query.exec(data => {
          this.scrollTop = data[0].height - scrollViewHeight;
          if(this.scrollTop < 10){
            // 未渲染完成，重试一次
            this.holdingScrollBar();
          }
        });
      }, 50)
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
		onShowEmoChatTab() {
			this.showRecord = false;
			this.switchChatTabBox('emo')
		},
		onShowToolsChatTab() {
			this.showRecord = false;
			this.switchChatTabBox('tools')
		},
		switchChatTabBox(chatTabBox) {
			this.chatTabBox = chatTabBox;
			this.reCalChatMainHeight();
			if (chatTabBox != 'tools' && this.$refs.fileUpload) {
				this.$refs.fileUpload.hide()
			}
		},
		selectEmoji(emoText) {
			let path = this.$emo.textToPath(emoText)
			// 先把键盘禁用了，否则会重新弹出键盘
			this.isReadOnly = true;
			this.isEmpty = false;
			this.$nextTick(() => {
				this.editorCtx.insertImage({
					src: path,
					alt: emoText,
					extClass: 'emoji-small',
					nowrap: true,
					complete: () => {
						this.isReadOnly = false;
						this.editorCtx.blur();
					}
				});
			})
		},
    onUploadVideoBefore(file) {
      // 检查是否被封禁
      if (this.isBanned) {
        this.showBannedTip();
        return;
      }
      let data = {
        videoUrl: file.tempFilePath,
      }
      let msgInfo = {
        id: 0,
        tmpId: this.generateId(),
        fileId: file.uid,
        sendId: this.mine.id,
        content: JSON.stringify(data),
        sendTime: new Date().getTime(),
        selfSend: true,
        type: this.$enums.MESSAGE_TYPE.VIDEO,
        readedCount: 0,
        loadStatus: "loading",
        status: this.$enums.MESSAGE_STATUS.UNSEND
      }
      // 填充对方id
      this.fillTargetId(msgInfo, this.chat.targetId);
      // 插入消息
      this.chatStore.insertMessage(msgInfo, this.chat);
      // 会话置顶
      this.moveChatToTop();
      // 借助file对象保存
      file.msgInfo = msgInfo;
      // 滚到最低部
      this.scrollToBottom();
      return true;
    },
    onUploadVideoSuccess(file, res) {
      let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
      msgInfo.content = JSON.stringify(res.data);
      msgInfo.receipt = this.isReceipt
      this.sendMessageRequest(msgInfo).then((m) => {
        msgInfo.loadStatus = 'ok';
        msgInfo.id = m.id;
        this.isReceipt = false;
        this.chatStore.insertMessage(msgInfo, this.chat);
      })
    },
    onUploadVideoFail(file, err) {
      let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
      msgInfo.loadStatus = 'fail';
      this.chatStore.insertMessage(msgInfo, this.chat);
    },
		onUploadImageBefore(file) {
			// 检查是否被封禁
			if (this.isBanned) {
				this.showBannedTip();
				return;
			}
			let data = {
				originUrl: file.path,
				thumbUrl: file.path
			}
			let msgInfo = {
				id: 0,
				tmpId: this.generateId(),
				fileId: file.uid,
				sendId: this.mine.id,
				content: JSON.stringify(data),
				sendTime: new Date().getTime(),
				selfSend: true,
				type: this.$enums.MESSAGE_TYPE.IMAGE,
				readedCount: 0,
				loadStatus: "loading",
				status: this.$enums.MESSAGE_STATUS.UNSEND
			}
			// 填充对方id
			this.fillTargetId(msgInfo, this.chat.targetId);
			// 插入消息
			this.chatStore.insertMessage(msgInfo, this.chat);
			// 会话置顶
			this.moveChatToTop();
			// 借助file对象保存
			file.msgInfo = msgInfo;
			// 滚到最低部
			this.scrollToBottom();
			return true;
		},
		onUploadImageSuccess(file, res) {
			let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
			msgInfo.content = JSON.stringify(res.data);
			msgInfo.receipt = this.isReceipt
			this.sendMessageRequest(msgInfo).then((m) => {
				msgInfo.loadStatus = 'ok';
				msgInfo.id = m.id;
				this.isReceipt = false;
				this.chatStore.insertMessage(msgInfo, this.chat);
			})
		},
		onUploadImageFail(file, err) {
			let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
			msgInfo.loadStatus = 'fail';
			this.chatStore.insertMessage(msgInfo, this.chat);
		},
		onUploadFileBefore(file) {
			// 检查是否被封禁
			if (this.isBanned) {
				this.showBannedTip();
				return;
			}
			let data = {
				name: file.name,
				size: file.size,
				url: file.path
			}
			let msgInfo = {
				id: 0,
				tmpId: this.generateId(),
				sendId: this.mine.id,
				content: JSON.stringify(data),
				sendTime: new Date().getTime(),
				selfSend: true,
				type: this.$enums.MESSAGE_TYPE.FILE,
				readedCount: 0,
				loadStatus: "loading",
				status: this.$enums.MESSAGE_STATUS.UNSEND
			}
			// 填充对方id
			this.fillTargetId(msgInfo, this.chat.targetId);
			// 插入消息
			this.chatStore.insertMessage(msgInfo, this.chat);
			// 会话置顶
			this.moveChatToTop();
			// 借助file对象保存
			file.msgInfo = msgInfo;
			// 滚到最低部
			this.scrollToBottom();
			return true;
		},
		onUploadFileSuccess(file, res) {
			let data = {
				name: file.name,
				size: file.size,
				url: res.data
			}
			let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
			msgInfo.content = JSON.stringify(data);
			msgInfo.receipt = this.isReceipt
			this.sendMessageRequest(msgInfo).then((m) => {
				msgInfo.loadStatus = 'ok';
				msgInfo.id = m.id;
				this.isReceipt = false;
				this.chatStore.insertMessage(msgInfo, this.chat);
			})
		},
		onUploadFileFail(file, res) {
			let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
			msgInfo.loadStatus = 'fail';
			this.chatStore.insertMessage(msgInfo, this.chat);
		},
		onDeleteMessage(msgInfo) {
			uni.showModal({
				title: '删除消息',
				content: '确认删除消息?',
				success: (res) => {
					if (!res.cancel) {
						this.chatStore.deleteMessage(msgInfo, this.chat);
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
						let url = `/message/${this.chat.type.toLowerCase()}/recall/${msgInfo.id}`
						this.$http({
							url: url,
							method: 'DELETE'
						}).then(() => {
							msgInfo = JSON.parse(JSON.stringify(msgInfo));
							msgInfo.type = this.$enums.MESSAGE_TYPE.RECALL;
							msgInfo.content = '你撤回了一条消息';
							msgInfo.status = this.$enums.MESSAGE_STATUS.RECALL;
							this.chatStore.insertMessage(msgInfo, this.chat);
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
    onScroll(e) {
      // 记录当前滚动条高度
      this.scrollViewHeight = e.detail.scrollHeight;
      //console.log('scrollViewHeight', this.scrollViewHeight);

      // 清除之前的计时器
      if (this.timer) clearTimeout(this.timer)

      // 设置防抖（100ms内只执行一次）
      this.timer = setTimeout(() => {
        const currentScrollTop = e.detail.scrollTop
        // 计算滚动距离
        const scrollDistance = Math.abs(currentScrollTop - this.lastScrollTop);
        // 设置滚动距离阈值
        const scrollThreshold = 400;

        // 判断滚动方向
        if (currentScrollTop < this.lastScrollTop) {
          this.scrollDirection = 'up'
          // 判断滚动距离是否超过阈值
          if (scrollDistance > scrollThreshold) {
            this.isInBottom = false;
            console.log('向上快速滚动，滚动距离:', scrollDistance)
            // 这里可以添加向上快速滚动的自定义逻辑
          } else {
            console.log('向上缓慢滚动，滚动距离:', scrollDistance)
          }
        } else if (currentScrollTop > this.lastScrollTop) {
          this.scrollDirection = 'down'
          // 判断滚动距离是否超过阈值
          if (scrollDistance > scrollThreshold) {
            console.log('向下快速滚动，滚动距离:', scrollDistance)
            // 这里可以添加向下快速滚动的自定义逻辑
          } else {
            console.log('向下缓慢滚动，滚动距离:', scrollDistance)
          }
        }
        // 更新滚动位置记录
        this.lastScrollTop = currentScrollTop
        this.currentScrollTop = currentScrollTop
      }, 100)
    },
    onClickToBottom() {
      this.scrollToBottom();
      // 有些设备滚到底部时会莫名触发滚动到顶部的事件
      // 所以这里延迟100s保证能准确设置底部标志
      setTimeout(() => {
        this.isInBottom = true;
        this.newMessageSize = 0;
      }, 100)
    },
		onScrollToTop() {
      console.log("onScrollToTop")
      if (this.showMinIdx > 0) {
        //  #ifndef H5
        // 防止滚动条定格在顶部，不能一直往上滚
        this.scrollToMsgIdx(this.showMinIdx);
        // #endif

        // #ifdef H5
        // 防止滚动条定格在顶部，不能一直往上滚，h5采用scroll-top定位
        this.holdingScrollBar(this.scrollViewHeight);
        // #endif

        // 多展示20条信息
        this.showMinIdx = this.showMinIdx > 20 ? this.showMinIdx - 20 : 0;
      }
      // 清除底部标识
      if (this.showMinIdx > 0) {
        this.isInBottom = false;
      }
		},
    onScrollToBottom(e) {
      console.log("onScrollToBottom")
      // 设置底部标识
      this.isInBottom = true;
      this.newMessageSize = 0;
    },
		onShowMore() {
			if (this.chat.type == "GROUP") {
				uni.navigateTo({
					url: "/pages/group/group-info?id=" + this.group.id
				})
			} else {
				uni.navigateTo({
					url: "/pages/common/user-info?id=" + this.friend.id
				})
			}
		},
		onTextInput(e) {
			this.isEmpty = e.detail.html == '<p><br></p>'
		},
		onEditorReady() {
      this.$nextTick(()=>{
        const query = uni.createSelectorQuery().in(this);
        query.select('#editor').context((res) => {
          this.editorCtx = res.context
        }).exec()
      })
		},
		onEditorFocus(e) {
			this.isFocus = true;
			this.scrollToBottom()
			this.switchChatTabBox('none')

		},
		onEditorBlur(e) {
			this.isFocus = false;
		},
		onAudioStateChange(state, msgInfo) {
      this.playingAudioState = state;
			const playingAudio = this.$refs['message' + msgInfo.id][0]
			if (state == 'PLAYING' && playingAudio != this.playingAudio) {
				// 停止之前的录音
				this.playingAudio && this.playingAudio.stopPlayAudio();
				// 记录当前正在播放的消息
				this.playingAudio = playingAudio;
			} else if (state === 'STOP') {
        if (this.playAudios.length > 0) {
          const msg = this.playAudios.shift();
          this.$refs[`message${msg.id}`][0].onPlayAudio(JSON.parse(msg.content).voice);
        }
      } else if (state === 'PAUSE') {
        this.playAudios = [];
      }
		},
		loadReaded(fid) {
			this.$http({
				url: `/message/private/maxReadedId?friendId=${fid}`,
				method: 'get'
			}).then((id) => {
				this.chatStore.readedMessage({
					friendId: fid,
					maxId: id
				});
			});
		},
		readedMessage() {
			if (this.unreadCount == 0) {
				return;
			}
			let url = ""
			if (this.chat.type == "GROUP") {
				url = `/message/group/readed?groupId=${this.chat.targetId}`
			} else {
				url = `/message/private/readed?friendId=${this.chat.targetId}`
			}
			this.$http({
				url: url,
				method: 'PUT'
			}).then(() => {
				this.chatStore.resetUnreadCount(this.chat)
				this.scrollToBottom();
			})
		},
		loadGroup(groupId) {
			this.$http({
				url: `/group/find/${groupId}`,
				method: 'GET'
			}).then((group) => {
				this.group = group;
				this.chatStore.updateChatFromGroup(group);
				this.groupStore.updateGroup(group);
			});

			this.$http({
				url: `/group/members/${groupId}`,
				method: 'GET'
			}).then((groupMembers) => {
        this.groupMembers = groupMembers;
        this.groupMembersMap = groupMembers.reduce((map, member) => {
          map.set(member.userId, member);
          return map;
        }, new Map()); // 初始值为一个空Map

        this.myGroupMemberInfo = this.groupMembersMap.get(this.mine.id);
			});
		},
		loadFriend(friendId) {
			// 获取对方最新信息
			this.$http({
				url: `/user/find/${friendId}`,
				method: 'GET'
			}).then((friend) => {
				this.friend = friend;
				this.chatStore.updateChatFromFriend(friend);
				this.friendStore.updateFriend(friend);
			})
		},
		rpxTopx(rpx) {
			// rpx转换成px
			let info = uni.getSystemInfoSync()
			let px = info.windowWidth * rpx / 750;
			return Math.floor(rpx);
		},
		sendMessageRequest(msgInfo) {
			return new Promise((resolve, reject) => {
				// 请求入队列，防止请求"后发先至"，导致消息错序
				this.reqQueue.push({ msgInfo, resolve, reject });
				this.processReqQueue();
			})
		},
		processReqQueue() {
			if (this.reqQueue.length && !this.isSending) {
				this.isSending = true;
				const reqData = this.reqQueue.shift();
        if (this.chat.type === "GROUP") {
          reqData.msgInfo.characterId = this.myGroupMemberInfo.templateCharacterId;
          reqData.msgInfo.version = this.group.version;
        }
        if (this.quoteMsgInfo.msgInfo) {
          reqData.msgInfo.quoteId = this.quoteMsgInfo.msgInfo.id;
          this.quoteMsgInfo.msgInfo = null;
          this.quoteMsgInfo.quoteContent = '';
          this.quoteMsgInfo.show = false;
        }
				this.$http({
					url: this.messageAction,
					method: 'post',
					data: reqData.msgInfo
				}).then((res) => {
					reqData.resolve(res)
				}).catch((e) => {
					reqData.reject(e)
				}).finally(() => {
					this.isSending = false;
					// 发送下一条请求
					this.processReqQueue();
				})
			}
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
		listenKeyBoard() {
      // #ifdef H5
      if (navigator.platform == "Win32") {
        // 电脑端不需要弹出键盘
        console.log("navigator.platform:", navigator.platform)
        return;
      }
      if (uni.getSystemInfoSync().platform == 'ios') {
        // ios h5实现键盘监听
        window.addEventListener('focusin', this.focusInListener);
        window.addEventListener('focusout', this.focusOutListener);
        // 监听键盘高度，ios13以上开始支持
        if (window.visualViewport) {
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
		unListenKeyboard() {
			// #ifdef H5
			window.removeEventListener('resize', this.resizeListener);
			window.removeEventListener('focusin', this.focusInListener);
			window.removeEventListener('focusout', this.focusOutListener);
			// #endif
			// #ifndef H5
			uni.offKeyboardHeightChange(this.keyBoardListener);
			// #endif
		},
		keyBoardListener(res) {
			this.isShowKeyBoard = res.height > 0;
			if (this.isShowKeyBoard) {
				this.keyboardHeight = res.height; // 获取并保存键盘高度
			}
			this.reCalChatMainHeight();
		},
		resizeListener() {
      let keyboardHeight = this.initHeight - window.innerHeight;
      // 兼容部分ios浏览器
      if (window.visualViewport && uni.getSystemInfoSync().platform == 'ios') {
        keyboardHeight = this.initHeight - window.visualViewport.height;
      }
      console.log("resizeListener:", window.visualViewport.height)
      this.isShowKeyBoard = keyboardHeight > 150;
      if (this.isShowKeyBoard) {
        this.keyboardHeight = keyboardHeight;
      }
      this.reCalChatMainHeight();
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
		showBannedTip() {
			let msgInfo = {
				tmpId: this.generateId(),
				sendId: this.mine.id,
				sendTime: new Date().getTime(),
				type: this.$enums.MESSAGE_TYPE.TIP_TEXT
			}
			if (this.chat.type == "PRIVATE") {
				msgInfo.recvId = this.mine.id
				msgInfo.content = "该用户已被管理员封禁,原因:" + this.friend.reason
			} else {
				msgInfo.groupId = this.group.id;
				msgInfo.content = "本群聊已被管理员封禁,原因:" + this.group.reason
			}
			this.chatStore.insertMessage(msgInfo, this.chat);
		},
		generateId() {
			// 生成临时id 
			return String(new Date().getTime()) + String(Math.floor(Math.random() * 1000));
		},
    onCharacterWord() {
      this.queryCharacterWord(this.myGroupMemberInfo.templateCharacterId).then((data) => {
        if (data.group.length === 0 && data.character.length === 0) {
          uni.showToast({
            title: "该角色未配置语音台词",
            icon: "none"
          })
        } else {
          this.switchChatTabBox('none');
          this.words.group = data.group;
          this.words.character = data.character;
          this.$refs.characterWordList.open();
        }
      })
    },
    onShowCharacterEmo() {
      this.queryCharacterEmo(this.myGroupMemberInfo.templateCharacterId).then((data) => {
        if (data.group.length === 0 && data.character.length === 0) {
          uni.showToast({
            title: "该角色未配置表情包",
            icon: "none"
          })
        } else {
          this.characterEmos.group = data.group;
          this.characterEmos.character = data.character;
          this.showRecord = false;
          this.switchChatTabBox('characterEmo')
        }
      })
    },
    queryCharacterWord(characterId) {
      return new Promise((resolve, reject) => {
        this.$http({
          url: `/character/word/publishedWord?characterId=${characterId}`,
          method: "get",
        }).then((data) => {
          resolve(data)
        })
      });
    },
    queryCharacterEmo(characterId) {
      return new Promise((resolve, reject) => {
        this.$http({
          url: `/character/emo/publishedEmo?characterId=${characterId}`,
          method: "get",
        }).then((data) => {
          resolve(data)
        })
      });
    },
    selectCharacterEmoji(emo) {
      let content = {
        id: emo.id,
        templateGroupId: emo.templateGroupId,
        characterId: emo.characterId,
        characterName: emo.characterName,
        name: emo.name,
        originUrl: emo.url
      }
      let msgInfo = {
        sendId: this.mine.id,
        content: JSON.stringify(content),
        sendTime: new Date().getTime(),
        selfSend: true,
        type: this.$enums.MESSAGE_TYPE.IMAGE,
        readedCount: 0,
        loadStatus: "loading",
        receipt: this.isReceipt,
        status: this.$enums.MESSAGE_STATUS.UNSEND
      }

      // 填充对方id
      this.fillTargetId(msgInfo, this.chat.targetId);

      this.sendMessageRequest(msgInfo).then((m) => {
        msgInfo.loadStatus = 'ok';
        msgInfo.id = m.id;
        this.isReceipt = false;
        this.chatStore.insertMessage(msgInfo, this.chat);
      })

      // 会话置顶
      this.moveChatToTop();

      // 滚到最低部
      this.scrollToBottom();
    },
    refreshChat() {
      this.loadGroup(this.chat.targetId)
    },
    gotoHome() {
      uni.reLaunch({
        url: '/pages/chat/chat'
      })
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
        this.quoteMsgInfo.quoteContent += "[音频]" + (JSON.parse(msgInfo.content).duration ? JSON.parse(msgInfo.content).duration + '"' : JSON.parse(msgInfo.content).originalName);
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
      this.scrollMsgIdx = this.findIdxByMessageId(messageId);
      this.targetMsgId = messageId;
      console.log("scrollToTargetMsg", messageId)
      // 移除之前的高亮
      setTimeout(() => {
        this.scrollMsgIdx = null;
        this.targetMsgId = null;
      }, 2000);
    },
    findIdxByMessageId(messageId) {
      return this.chat.messages.findIndex(item => item.id == messageId);
    },
    handleGroupChangeEvent(msg) {
      console.log("handleGroupChangeEvent", msg);
      if (msg.chatType === 'GROUP' && this.group.id === msg.groupId && msg.groupChangeType) {
        this.loadGroup(this.group.id);
      }
    },
    handleGroupAudioEvent(msg) {
      console.log("handleGroupAudioEvent", msg);
      if (msg.chatType === 'GROUP' && this.group.id === msg.groupId) {
        this.playAudios.push(msg);
        setTimeout(() => {
          if (this.playingAudioState === 'STOP') {
            if (this.$refs[`message${msg.id}`][0]) {
              this.playAudios.shift();
              this.$refs[`message${msg.id}`][0].onPlayAudio(JSON.parse(msg.content).voice);
            }
          }
        }, 2000);
      }
    },
    playVideo(data) {
      this.videoSrc = data.videoUrl;
      this.videoCoverImage = data.coverImageUrl;
      this.viewVideo = true;
    },
    // 视频弹窗关闭回调
    handleVideoClose() {
      console.log('Video popup closed');
      this.videoSrc = "";
      this.videoCoverImage = "";
      this.viewVideo = false;
    },
    // 视频错误回调
    handleVideoError(e) {
      console.error('Video error in parent component:', e)
      this.videoSrc = "";
      this.videoCoverImage = "";
      this.viewVideo = false;
    }
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		title() {
			if (!this.chat) {
				return "";
			}
			let title = this.chat.showName;
			if (this.chat.type == "GROUP") {
				let size = this.groupMembers.filter(m => !m.quit).length;
				title += `(${size})`;
			}
			return title;
		},
		messageAction() {
			return `/message/${this.chat.type.toLowerCase()}/send`;
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
		isBanned() {
			return (this.chat.type == "PRIVATE" && this.friend.isBanned) ||
				(this.chat.type == "GROUP" && this.group.isBanned)
		},
		atUserItems() {
			let atUsers = [];
			this.atUserIds.forEach((id) => {
				if (id == -1) {
					atUsers.push({
						id: -1,
            aliasName: "全体成员"
					})
					return;
				}
				let member = this.groupMembers.find((m) => m.userId == id);
				if (member) {
					atUsers.push(member);
				}
			})
			return atUsers;
		},
    friends() {
      return this.friendStore.friends;
    },
    friendsMap() {
      return this.friends.reduce((map, item) => {
        map[item.id] = item;
        return map;
      }, new Map());
    },
    quoteContent() {
      return this.$commonUtil.processAtUsers(this.quoteMsgInfo.quoteContent, this.quoteMsgInfo.msgInfo?.atUserIds || []);
    },
    groupRequestUnreadCount() {
      // 当前用户是群主，待审核的加群申请
      return this.chat.type === "GROUP" && this.group?.ownerId === this.mine.id ? this.groupStore.groupRequests
          .filter((r) => r.groupId === this.group.id && r.groupOwnerId === this.mine.id && r.status === 1 && r.type === 1).length : 0;
    },
    unreadTalkCount() {
      if (this.chat.type === "GROUP") {
        return this.talkStore.groupsTalks.get(this.group.id)?.length || 0;
      }
      return 0;
    },
    unreadNotifyCount() {
      if (this.chat.type === "GROUP") {
        return this.talkStore.groupNotify.get(this.group.id) || 0;
      }
      return 0;
    },
    groupUnreadCount() {
      return this.unreadTalkCount + this.unreadNotifyCount + this.groupRequestUnreadCount;
    }
	},
	watch: {
		messageSize: function(newSize, oldSize) {
      console.log("newSize", newSize);
      console.log("oldSize", oldSize);
      // 接收到新消息
      if (newSize > oldSize && oldSize > 0) {
        let lastMessage = this.chat.messages[newSize - 1];
        if (this.$msgType.isNormal(lastMessage.type)) {
          if (this.isInBottom || lastMessage.selfSend) {
            // 收到消息,则滚动至底部
            this.scrollToBottom();
          } else {
            // 若滚动条不在底部，说明用户正在翻历史消息，此时滚动条不能动，同时增加新消息提示
            this.newMessageSize++;
          }
        }
      }
		},
		unreadCount: {
			handler(newCount, oldCount) {
				if (newCount > 0) {
					// 消息已读
					this.readedMessage()
				}
			}
		},
	},
	onLoad(options) {
    uni.$on('group-change-event', this.handleGroupChangeEvent);
    uni.$on('group-audio-event', this.handleGroupAudioEvent);
    this.chatIdx = options.chatIdx;
		// 聊天数据
		this.chat = this.chatStore.chats[options.chatIdx];
		// 初始状态只显示20条消息
		let size = this.messageSize;
		this.showMinIdx = size > 20 ? size - 20 : 0;
		// 消息已读
		this.readedMessage()
		// 加载好友或群聊信息
		if (this.chat.type == "GROUP") {
			this.loadGroup(this.chat.targetId);
		} else {
			this.loadFriend(this.chat.targetId);
			this.loadReaded(this.chat.targetId)
		}
		// 激活当前会话
		this.chatStore.activeChat(options.chatIdx);
		// 复位回执消息
		this.isReceipt = false;
    // 清空底部标志
    this.isInBottom = true;
    this.newMessageSize = 0;
		// 监听键盘高度
    this.listenKeyBoard();
    // 计算聊天窗口高度
    this.windowHeight = uni.getSystemInfoSync().windowHeight;
    this.screenHeight = uni.getSystemInfoSync().screenHeight;
    this.reCalChatMainHeight();
    this.$nextTick(() => {
      // 上面获取的windowHeight可能不准，重新计算一次聊天窗口高度
      this.windowHeight = uni.getSystemInfoSync().windowHeight;
      this.reCalChatMainHeight();
      this.scrollToBottom();
      // #ifdef H5
      this.initHeight = window.innerHeight;
      // 兼容ios的h5:禁止页面滚动
      const chatBox = document.getElementById('chatBox')
      chatBox.addEventListener('touchmove', e => {
        e.preventDefault()
      }, { passive: false });
      // #endif
    });
	},
	onUnload() {
    console.log('chat-box-onUnload')
    uni.$off('group-change-event', this.handleGroupChangeEvent) // 清理监听
    uni.$off('group-audio-event', this.handleGroupAudioEvent) // 清理监听
		this.unListenKeyboard();
	},
  onHide() {
    console.log('chat-box-onHide')
    uni.$off('group-change-event', this.handleGroupChangeEvent) // 清理监听
    uni.$off('group-audio-event', this.handleGroupAudioEvent) // 清理监听
  },
}
</script>

<style lang="scss" scoped>
.chat-box {
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
          //animation: fadeIn 0.3s ease;
          margin-bottom: 15px;
        }

        .active {
          background-color: rgba(79, 70, 229, 0.1);
        }

        //.message-wrapper[data-highlight="true"] {
        //  animation: highlight 2s ease;
        //}
			}

      .scroll-to-bottom {
        position: absolute;
        right: 30rpx;
        bottom: 30rpx;
        font-size: $im-font-size;
        color: $im-color-primary;
        font-weight: 600;
        background: white;
        padding: 10rpx 30rpx;
        border-radius: 25rpx;
        box-shadow: $im-box-shadow-dark;
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