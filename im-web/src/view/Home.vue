<template>
	<div class="home-page">
    <div class="app-container" :class="{ fullscreen: isFullscreen }">
      <div class="navi-bar">
        <div class="navi-bar-box">
          <div class="top">
            <div class="user-head-image">
              <head-image :name="$store.state.userStore.userInfo.nickName"
                          :url="$store.state.userStore.userInfo.headImage"
                          :size="38" @click.native="showSettingDialog=true">
              </head-image>
            </div>
            <div class="menu">
              <router-link class="link" v-bind:to="'/home/chat'">
                <div class="menu-item">
                  <span>
                    <svg class="icon svg-icon" aria-hidden="true">
                      <use xlink:href="#icon-xiaoxi"></use>
                    </svg>
                  </span>
                  <div v-show="unreadCount>0" class="unread-text">{{unreadCount}}</div>
                </div>
              </router-link>
              <router-link class="link" v-bind:to="'/home/friend'">
                <div class="menu-item">
                  <span>
                    <svg class="icon svg-icon" aria-hidden="true">
                      <use xlink:href="#icon-friend"></use>
                    </svg>
                  </span>
                  <div v-show="friendRequestCount > 0" class="unread-text">{{friendRequestCount}}</div>
                </div>
              </router-link>
              <router-link class="link" v-bind:to="'/home/group'">
                <div class="menu-item">
                  <span>
                    <svg class="icon svg-icon" aria-hidden="true">
                      <use xlink:href="#icon-group"></use>
                    </svg>
                  </span>
                  <div v-show="groupActivity || groupRequestCount > 0 || joinGroupRequests > 0" class="unread-dot"></div>
                </div>
              </router-link>
              <router-link class="link" v-bind:to="'/home/regionGroup'">
                <div class="menu-item">
                  <span>
                    <svg class="icon svg-icon" aria-hidden="true">
                      <use xlink:href="#icon-diqiu"></use>
                    </svg>
                  </span>
                  <div v-show="unreadRegionCount>0" class="unread-text">{{unreadRegionCount}}</div>
                  <div v-show="regionGroupActivity" class="unread-dot"></div>
                </div>
              </router-link>
              <router-link class="link" v-bind:to="'/home/ai-chat'">
                <div class="menu-item">
                  <span>
                    <svg class="icon svg-icon" aria-hidden="true">
                      <use xlink:href="#icon-AIzhushou"></use>
                    </svg>
                  </span>
                </div>
              </router-link>
              <router-link class="link" v-bind:to="'/home/square'">
                <div class="menu-item">
                  <span>
                    <svg class="icon svg-icon" aria-hidden="true">
                     <use xlink:href="#icon-fenleiorguangchangorqitatianchong"></use>
                    </svg>
                  </span>
                  <div v-show="unreadUserCount > 0 || notifyCount > 0" class="unread-text">{{unreadUserCount + notifyCount}}</div>
                </div>
              </router-link>
            </div>
          </div>
          <div class="botoom">
<!--            <div class="bottom-item" @click="showSetting">
              <span>
                <svg class="icon svg-icon" aria-hidden="true">
                  <use xlink:href="#icon-shezhi"></use>
                </svg>
              </span>
            </div>-->
            <div class="bottom-item" @click="showOperation">
              <span>
                <svg class="icon svg-icon" aria-hidden="true">
                  <use xlink:href="#icon-shezhi"></use>
                </svg>
              </span>
            </div>
            <div class="bottom-item" @click="onExit()" title="退出">
              <span>
                <svg class="icon svg-icon" aria-hidden="true">
                  <use xlink:href="#icon-tuichu"></use>
                </svg>
              </span>
            </div>
          </div>
        </div>
      </div>
      <div class="content-box">
        <router-view></router-view>
      </div>
    </div>
    <user-detail :visible="showSettingDialog" @close="closeSetting()"></user-detail>
<!--		<setting :visible="showSettingDialog" @close="closeSetting()"></setting>-->
    <operation :visible="showOperationDialog" @close="closeOperation()"></operation>
    <user-info v-show="uiStore.userInfo.show" :pos="uiStore.userInfo.pos" :user="uiStore.userInfo.user"
               @close="$store.commit('closeUserInfoBox')"></user-info>
    <full-image :visible="uiStore.fullImage.show" :url="uiStore.fullImage.url"
                @close="$store.commit('closeFullImageBox')"></full-image>
    <rtc-private-video ref="rtcPrivateVideo"></rtc-private-video>
    <rtc-private-acceptor ref="rtcPrivateAcceptor"></rtc-private-acceptor>
    <rtc-group-video ref="rtcGroupVideo" ></rtc-group-video>
    <aplayer v-if="showFloatMusic"
            :music="musics[0]"
             :list="musics"
             :show-lrc="true"
             :theme="'#ff7e5f'"
             :autoplay="false"
             :float="true"
             :mutex="true"
             :shuffle="false"
             :preload="'auto'"
             :list-max-height="'300px'"
             :list-folded="true"
             :featured="true"/>
	</div>
</template>

<script>
	import HeadImage from '../components/common/HeadImage.vue';
	import Setting from '../components/setting/Setting.vue';
	import UserInfo from '../components/common/UserInfo.vue';
  import UserDetail from "@components/user/UserDetail.vue";
	import FullImage from '../components/common/FullImage.vue';
	import RtcPrivateVideo from '../components/rtc/RtcPrivateVideo.vue';
	import RtcPrivateAcceptor from '../components/rtc/RtcPrivateAcceptor.vue';
	import Operation from "@/components/operation/Operation";
  import RtcGroupVideo from '../components/rtc/RtcGroupVideo.vue';
  import Aplayer from 'vue-aplayer'

	export default {
		components: {
			HeadImage,
      UserInfo,
			Setting,
      UserDetail,
			FullImage,
      RtcPrivateVideo,
      RtcPrivateAcceptor,
      RtcGroupVideo,
      Operation,
      Aplayer
		},
		data() {
			return {
				showSettingDialog: false,
				showOperationDialog: false,
        lastPlayAudioTime: new Date()-1000,
        sse: null,
        messages: [],
        isFullscreen: true,
			}
		},
		methods: {
      init() {
        this.$eventBus.$on('openPrivateVideo', (rctInfo) => {
          // 进入单人视频通话
          this.$refs.rtcPrivateVideo.open(rctInfo);
        });
        this.$eventBus.$on('openGroupVideo', (rctInfo) => {
          // 进入多人视频通话
          this.$refs.rtcGroupVideo.open(rctInfo);
        });

        this.$store.dispatch("load").then(() => {
          // ws初始化
          this.$wsApi.connect(process.env.VUE_APP_WS_URL, sessionStorage.getItem("accessToken"));
          this.$wsApi.onConnect(()=>{
            // 加载离线消息
            this.pullPrivateOfflineMessage(this.$store.state.chatStore.privateMsgMaxId);
            this.pullGroupOfflineMessage(this.$store.state.chatStore.groupMsgMaxId);
            this.pullRegionGroupOfflineMessage(this.$store.state.regionGroupStore.regionGroupMsgMaxId);
            this.pullSystemOfflineMessage(this.$store.state.chatStore.systemMsgMaxSeqNo);
            this.pullOfflineTalks(this.$store.state.talkStore.privateTalkMaxId);
            this.pullFriendRequests();
            this.pullGroupRequests();
          });
          this.$wsApi.onMessage((cmd, msgInfo) => {
            if (cmd == 2) {
              // 关闭ws
              this.$wsApi.close(3000)
              // 异地登录，强制下线
              this.$alert("您已在其他地方登陆，将被强制下线", "强制下线通知", {
                confirmButtonText: '确定',
                callback: action => {
                  location.href = "/";
                }
              });
            } else if (cmd == 3) {
              // 插入私聊消息
              msgInfo.chatType = 'PRIVATE'
              this.handlePrivateMessage(msgInfo);
            } else if (cmd == 4) {
              // 插入群聊消息
              msgInfo.chatType = 'GROUP'
              this.handleGroupMessage(msgInfo);
            } else if (cmd == 5){
              // 处理系统消息
              msgInfo.chatType = 'SYSTEM'
              this.handleSystemMessage(msgInfo);
            } else if (cmd == 6) {
              // 处理动态消息
              this.handleTalkMessage(msgInfo);
            } else if (cmd == 9) {
              // 插入地区群聊消息
              msgInfo.chatType = 'REGION-GROUP'
              this.handleRegionGroupMessage(msgInfo);
            }
          });
          this.$wsApi.onClose((e) => {
            //console.log(e);
            if (e.code != 3000) {
              // 断线重连
              this.$message.error("连接断开，正在尝试重新连接...");
              this.$wsApi.reconnect(process.env.VUE_APP_WS_URL, sessionStorage.getItem("accessToken"));
            }
          });
        }).catch((e) => {
          console.log("初始化失败", e);
        })
      },
      pullPrivateOfflineMessage(minId) {
        console.log("拉取私聊记录start......")
        this.$store.commit("loadingPrivateMsg", true)
        this.$http({
          url: "/message/private/pullOfflineMessage?minId=" + minId,
          method: 'GET'
        }).catch(() => {
          console.log("拉取私聊记录end......")
          this.$store.commit("loadingPrivateMsg", false)
        })
      },
      pullGroupOfflineMessage(minId) {
        console.log("拉取群聊记录start......")
        this.$store.commit("loadingGroupMsg", true)
        this.$http({
          url: "/message/group/pullOfflineMessage?minId=" + minId,
          method: 'GET'
        }).catch(() => {
          console.log("拉取群聊记录end......")
          this.$store.commit("loadingGroupMsg", false)
        })
      },
      pullRegionGroupOfflineMessage(minId) {
        console.log("拉取地区群聊记录start......")
        this.$store.commit("loadingRegionGroupMsg", true);
        this.$http({
          url: "/message/regionGroup/pullOfflineMessage?minId=" + minId,
          method: 'GET'
        }).catch(() => {
          console.log("拉取地区群聊记录end......")
          this.$store.commit("loadingRegionGroupMsg", false);
        })
      },
      pullSystemOfflineMessage(minSeqNo) {
        console.log("拉取系统消息start......")
        this.$store.commit("loadingSystemMsg", true);
        this.$http({
          url: "/message/system/pullOfflineMessage?minSeqNo=" + minSeqNo,
          method: 'GET'
        }).catch(() => {
          console.log("拉取系统消息end......")
          this.$store.commit("loadingSystemMsg", false);
        })
      },
      pullOfflineTalks(minId) {
        this.$http({
          url: "/talk/pullOfflineTalks?minId=" + minId,
          method: 'GET'
        }).then((data) => {
          this.$store.commit("setUnreadTalkInfo", data)
        }).catch(() => {

        })
      },
      pullFriendRequests() {
        this.$http({
          url: "/friend/request/list",
          method: 'GET'
        }).then((data) => {
          this.$store.commit("setFriendRequest", data)
        }).catch(() => {
        })
      },
      pullGroupRequests() {
        this.$http({
          url: "/group/request/list",
          method: 'GET'
        }).then((data) => {
          this.$store.commit("setGroupRequests", data)
        }).catch(() => {
        })
      },
      handlePrivateMessage(msg) {
        // 标记这条消息是不是自己发的
        msg.selfSend = msg.sendId === this.mine.id;
        // 好友id
        let friendId = msg.selfSend ? msg.recvId : msg.sendId;
        // 消息加载标志
        if (msg.type === this.$enums.MESSAGE_TYPE.LOADING) {
          console.log("私聊记录加载......", JSON.parse(msg.content))
          this.$store.commit("loadingPrivateMsg", JSON.parse(msg.content))
          return;
        }
        // 消息已读处理，清空已读数量
        if (msg.type === this.$enums.MESSAGE_TYPE.READED) {
          this.$store.commit("resetUnreadCount", {
            type: 'PRIVATE',
            targetId: msg.recvId
          })
          return;
        }
        // 消息回执处理,改消息状态为已读
        if (msg.type === this.$enums.MESSAGE_TYPE.RECEIPT) {
          this.$store.commit("readedMessage", {
            friendId: msg.sendId
          })
          return;
        }
        // 好友申请类消息
        if (msg.type === this.$enums.MESSAGE_TYPE.FRIEND_REQUEST_ADD) {
          this.$store.commit("addFriendRequest", JSON.parse(msg.content));
          return;
        }
        if (msg.type === this.$enums.MESSAGE_TYPE.FRIEND_REQUEST_MODIFY) {
          this.$store.commit("updateFriendRequest", JSON.parse(msg.content));
          return;
        }

        // 新增好友
        if (msg.type === this.$enums.MESSAGE_TYPE.FRIEND_NEW) {
          this.$store.commit("addFriend", JSON.parse(msg.content));
          return;
        }
        // 删除好友
        if (msg.type === this.$enums.MESSAGE_TYPE.FRIEND_DEL) {
          this.$store.commit("removeFriend", friendId);
          return;
        }

        // 标记这条消息是不是自己发的
        msg.selfSend = msg.sendId === this.$store.state.userStore.userInfo.id;
        // 单人webrtc 信令
        if (this.$msgType.isRtcPrivate(msg.type)) {
          this.$refs.rtcPrivateVideo.onRTCMessage(msg)
          return;
        }
        this.loadFriendInfo(friendId).then((friend) => {
          this.insertPrivateMessage(friend, msg);
        })
      },
      insertPrivateMessage(friend, msg) {
        let chatInfo = {
          type: 'PRIVATE',
          targetId: friend.id,
          showName: friend.nickName,
          headImage: friend.headImage
        };
        // 打开会话
        this.$store.commit("openChat", chatInfo);
        // 插入消息
        this.$store.commit("insertMessage", msg);
        // 播放提示音
        if (!msg.selfSend && this.$msgType.isNormal(msg.type) &&
            msg.status !== this.$enums.MESSAGE_STATUS.READED) {
          this.playAudioTip();
        }
      },
      handleGroupMessage(msg) {
        // 消息加载标志
        if (msg.type === this.$enums.MESSAGE_TYPE.LOADING) {
          console.log("群聊记录加载......", JSON.parse(msg.content))
          this.$store.commit("loadingGroupMsg", JSON.parse(msg.content))
          return;
        }

        // --------------------------群聊申请类消息start--------------------------
        if (msg.type ===  this.$enums.MESSAGE_TYPE.GROUP_JOIN_REQUEST) {
          this.$store.commit("addGroupRequest", JSON.parse(msg.content));
          return;
        }
        if (msg.type ===  this.$enums.MESSAGE_TYPE.GROUP_JOIN_REQUEST_MODIFY) {
          this.$store.commit("updateGroupRequest", JSON.parse(msg.content));
          return;
        }
        // --------------------------群聊申请类消息end--------------------------

        // 新增群
        if (msg.type === this.$enums.MESSAGE_TYPE.GROUP_NEW) {
          this.$store.commit("addGroup", JSON.parse(msg.content));
          return;
        }
        // 删除群
        if (msg.type === this.$enums.MESSAGE_TYPE.GROUP_DEL) {
          this.$store.commit("removeGroup", msg.groupId);
          return;
        }

        // 群聊有变更
        if (msg.type === this.$enums.MESSAGE_TYPE.TIP_TEXT && msg.groupChangeType && [1,2,3,5].includes(msg.groupChangeType)) {
          this.eventGroupChange(msg);
        }
        if (msg.type === this.$enums.MESSAGE_TYPE.WORD_VOICE && this.mine.autoPlay) {
          this.eventGroupPlayAudio(msg);
        }
        // 消息已读处理
        if (msg.type === this.$enums.MESSAGE_TYPE.READED) {
          // 我已读对方的消息，清空已读数量
          let chatInfo = {
            type: 'GROUP',
            targetId: msg.groupId
          }
          this.$store.commit("resetUnreadCount", chatInfo)
          return;
        }
        // 消息回执处理
        if (msg.type === this.$enums.MESSAGE_TYPE.RECEIPT) {
          // 更新消息已读人数
          let msgInfo = {
            id: msg.id,
            groupId: msg.groupId,
            readedCount: msg.readedCount,
            receiptOk: msg.receiptOk,
            chatType: 'GROUP'
          };
          this.$store.commit("updateMessage", msgInfo)
          return;
        }
        // 标记这条消息是不是自己发的
        msg.selfSend = msg.sendId === this.$store.state.userStore.userInfo.id;
        // 群视频信令
        if (this.$msgType.isRtcGroup(msg.type)) {
          this.$nextTick(() => {
            this.$refs.rtcGroupVideo.onRTCMessage(msg);
          })
          return;
        }
        this.loadGroupInfo(msg.groupId).then((group) => {
          // 插入群聊消息
          this.insertGroupMessage(group, msg);
        })
      },
      insertGroupMessage(group, msg) {
        let chatInfo = {
          type: 'GROUP',
          targetId: group.id,
          showName: group.remark,
          headImage: group.headImage
        };
        // 打开会话
        this.$store.commit("openChat", chatInfo);
        // 插入消息
        this.$store.commit("insertMessage", msg);
        // 播放提示音
        if (!msg.selfSend && msg.type <= this.$enums.MESSAGE_TYPE.WORD_VOICE &&
            msg.status != this.$enums.MESSAGE_STATUS.READED) {
          this.playAudioTip();
        }
      },
      handleRegionGroupMessage(msg) {
        // 消息加载标志
        if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
          console.log("地区群聊记录加载......", JSON.parse(msg.content))
          this.$store.commit("loadingRegionGroupMsg", JSON.parse(msg.content))
          return;
        }
        // 群聊有变更
        if (msg.type == this.$enums.MESSAGE_TYPE.TIP_TEXT && msg.groupChangeType && [1,3].includes(msg.groupChangeType)) {
          this.eventRegionGroupChange(msg);
        }

        // 消息已读处理
        if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
          // 我已读对方的消息，清空已读数量
          let chatInfo = {
            type: 'REGION-GROUP',
            targetId: msg.regionGroupId
          }
          this.$store.commit("resetRegionUnreadCount", chatInfo)
          return;
        }
        // 消息回执处理
        if (msg.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
          // 更新消息已读人数
          let msgInfo = {
            id: msg.id,
            regionGroupId: msg.regionGroupId,
            readedCount: msg.readedCount,
            receiptOk: msg.receiptOk
          };
          this.$store.commit("updateRegionMessage", msgInfo)
          return;
        }
        // 标记这条消息是不是自己发的
        msg.selfSend = msg.sendId == this.$store.state.userStore.userInfo.id;
        // 群视频信令
        if (this.$msgType.isRtcGroup(msg.type)) {
          this.$nextTick(() => {
            this.$refs.rtcGroupVideo.onRTCMessage(msg);
          })
          return;
        }
        this.loadRegionGroupInfo(msg.regionGroupId).then((regionGroup) => {
          // 插入群聊消息
          this.insertRegionGroupMessage(regionGroup, msg);
        })
      },
      handleSystemMessage(msg){
        // 用户被封禁
        if (msg.type == this.$enums.MESSAGE_TYPE.USER_BANNED) {
          this.removeToken();
          this.$wsApi.close(3000);
          this.$alert("您的账号已被管理员封禁,原因:"+ msg.content, "账号被封禁", {
            confirmButtonText: '确定',
            callback: action => {
              this.onExit();
            }
          });
          return;
        }
        // 消息加载标志
        if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
          console.log("系统消息加载......", JSON.parse(msg.content))
          this.$store.commit("loadingSystemMsg", JSON.parse(msg.content))
          return;
        }
        // 消息已读处理
        if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
          // 我已读对方的消息，清空已读数量
          let chatInfo = {
            type: 'SYSTEM',
            targetId: msg.pusherId
          }
          this.$store.commit("resetUnreadCount", chatInfo)
          return;
        }
        this.insertSystemMessage(msg);
      },
      handleTalkMessage(msg) {
        if (msg.type === 1) {
          if (msg.talk.category === 'private') {
            this.$store.commit("addNewTalk", msg.talk);
          } else if (msg.talk.category === 'group') {
            this.$store.commit("addGroupTalk", msg.talk);
          } else if (msg.talk.category === 'region') {
            this.$store.commit("addRegionTalk", msg.talk);
          }
        } else if (msg.type === 2 ||  msg.type === 3) {
          if (msg.talk.category === 'private') {
            this.$store.commit("addNotifyCount", msg);
          } else if (msg.talk.category === 'group') {
            this.$store.commit("addGroupNotifyCount", msg.talk);
          } else if (msg.talk.category === 'region') {
            this.$store.commit("addRegionNotifyCount", msg.talk);
          }
        }
      },
      insertSystemMessage(msg) {
        let chatInfo = {
          type: 'SYSTEM',
          targetId: msg.pusherId,
          showName: msg.pusherName,
          headImage: msg.pusherHeadImage
        };
        // 打开会话
        this.$store.commit("openChat", chatInfo);

        // 插入消息
        this.$store.commit("insertMessage", msg);
        // 播放提示音
        if (msg.type <= this.$enums.MESSAGE_TYPE.RICHTEXT &&
            msg.status != this.$enums.MESSAGE_STATUS.READED) {
          this.playAudioTip();
        }
      },

      insertRegionGroupMessage(regionGroup, msg) {
        let chatInfo = {
          type: 'REGION-GROUP',
          targetId: regionGroup.id,
          showName: regionGroup.remark,
        };
        // 打开会话
        this.$store.commit("openRegionChat", chatInfo);
        // 插入消息
        msg.isLeader = msg.sendId === regionGroup.leaderId;
        this.$store.commit("insertRegionMessage", msg);
        if(!msg.selfSend && msg.type <= this.$enums.MESSAGE_TYPE.VIDEO
            && msg.status != this.$enums.MESSAGE_STATUS.READED){
          this.playAudioTip();
        }
      },
      onExit() {
        this.$wsApi.close(3000);
        this.removeToken();
        location.href = "/";
      },
      removeToken() {
        sessionStorage.removeItem("accessToken");
        sessionStorage.removeItem("refreshToken");
      },
      playAudioTip() {
        // 离线消息不播放铃声
        if(this.$store.getters.isLoading()){
          return;
        }
        // 防止过于密集播放
        if ((new Date().getTime() - this.lastPlayAudioTime > 1000) && this.mine.soundPlay) {
          this.lastPlayAudioTime = new Date().getTime();
          let audio = new Audio();
          let url = require(`@/assets/audio/tip.mp3`);
          audio.src = url;
          audio.play();
        }
      },
			showSetting() {
				this.showSettingDialog = true;
			},
      showOperation() {
			  this.showOperationDialog = true;
      },
			closeSetting() {
				this.showSettingDialog = false;
			},
      closeOperation() {
			  this.showOperationDialog = false;
      },
      loadFriendInfo(id) {
        return new Promise((resolve, reject) => {
          let friend = this.$store.state.friendStore.friends.find((f) => f.id == id);
          if (friend) {
            resolve(friend);
          } else {
            this.$http({
              url: `/friend/find/${id}`,
              method: 'get'
            }).then((friend) => {
              this.$store.commit("addFriend", friend);
              resolve(friend)
            })
          }
        });
      },
      loadGroupInfo(id) {
        return new Promise((resolve, reject) => {
          let group = this.$store.state.groupStore.groups.find((g) => g.id == id);
          if (group) {
            resolve(group);
          } else {
            this.$http({
              url: `/group/find/${id}`,
              method: 'get'
            }).then((group) => {
              resolve(group)
              this.$store.commit("addGroup", group);
            })
          }
        });
      },
      loadRegionGroupInfo(id) {
        return new Promise((resolve, reject) => {
          let regionGroup = this.$store.state.regionGroupStore.regionGroups.find((g) => g.id == id);
          if (regionGroup) {
            resolve(regionGroup);
          } else {
            this.$http({
              url: `/region/group/find/${id}`,
              method: 'get'
            }).then((regionGroup) => {
              resolve(regionGroup)
              this.$store.commit("addRegionGroup", regionGroup);
            })
          }
        });
      },
      eventGroupChange(msg) {
        console.log('group-change-event')
        this.$eventBus.$emit('group-change', msg);
      },
      eventRegionGroupChange(msg) {
        console.log('region-group-change-event')
        this.$eventBus.$emit('region-group-change', msg);
      },
      eventGroupPlayAudio(msg) {
        this.$eventBus.$emit('play-group-audio', msg);
      }
		},
		computed: {
      mine() {
        return this.$store.state.userStore.userInfo;
      },
			uiStore() {
				return this.$store.state.uiStore;
			},
      unreadCount() {
        let unreadCount = 0;
        let chats = this.$store.state.chatStore.chats;
        chats.forEach((chat) => {
          if(!chat.delete){
            unreadCount += chat.unreadCount
          }
        });
        return unreadCount;
      },
      unreadRegionCount() {
        let unreadCount = 0;
        let chats = this.$store.state.regionGroupStore.regionChats;
        if (chats) {
          chats.forEach((chat) => {
            if(!chat.delete){
              unreadCount += chat.unreadCount
            }
          });
        }
        return unreadCount;
      },
      unreadUserCount() {
        return this.$store.state.talkStore.unreadUserList.length;
      },
      notifyCount() {
        return this.$store.state.talkStore.notifyCount;
      },
      showFloatMusic() {
        return this.$store.state.musicStore.showFloatMusic;
      },
      musics() {
        return this.$store.state.musicStore.musics;
      },
      groupActivity() {
        let talkMap =this.$store.state.talkStore.groupsTalks;
        let notifyMap =this.$store.state.talkStore.groupNotify;
        for (const [key, value] of talkMap) {
          if (value.length > 0) {
            return true
          }
        }

        for (const [key, value] of notifyMap) {
          if (value > 0) {
            return true;
          }
        }
        return false;
      },
      regionGroupActivity() {
        let talkMap =this.$store.state.talkStore.regionTalks;
        let notifyMap =this.$store.state.talkStore.regionNotify;
        for (const [key, value] of talkMap) {
          if (value.length > 0) {
            return true
          }
        }

        for (const [key, value] of notifyMap) {
          if (value > 0) {
            return true;
          }
        }
        return false;
      },
      friendRequestCount() {
        return this.$store.state.friendStore.friendRequest.filter((r) => r.status === 1 && r.recvId === this.mine.id).length
      },
      groupRequestCount() {
        return this.$store.state.groupStore.groupRequests.filter((r) => r.userId === this.mine.id && r.status === 1 && r.type === 2).length
      },
      joinGroupRequests() {
        // 群组申请(当前用户是群主，待审核的加群申请)
        return this.$store.state.groupStore.groupRequests
            .filter((r) => r.groupOwnerId === this.mine.id && r.status === 1 && r.type === 1).length;
      },
		},
		watch: {
			unreadCount: {
				handler(newCount, oldCount) {
					let tip = newCount > 0 ? `${newCount}条未读` : "";
					this.$elm.setTitleTip(tip);
				},
				immediate: true
			}
		},
		mounted() {
      this.init();
      //this.initSSE();
		},
		unmounted() {
      this.$wsApi.close();
      // if (this.sse) {
      //   this.sse.close();
      // }
		}
	}
</script>

<style scoped lang="scss">
.home-page {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  overflow: hidden;

  .app-container {
    width: 62vw;
    height: 80vh;
    display: flex;
    min-height: 600px;
    min-width: 970px;
    position: absolute;
    border-radius: 4px;
    overflow: hidden;
    transition: 0.2s;

    &.fullscreen {
      transition: 0.2s;
      width: 100vw;
      height: 100vh;
    }
  }

  .navi-bar {
    --icon-font-size: 22px;
    --width: 60px;
    width: var(--width);
    background: white;
    padding-top: 20px;
    border-right: #cccccc solid 1px;

    .navi-bar-box {
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .botoom {
        margin-bottom: 30px;
      }
    }

    .user-head-image {
      display: flex;
      justify-content: center;
    }

    .menu {
      margin-top: 10px;
      padding-left: 5px;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .link {
        text-decoration: none;
      }

      .router-link-active .menu-item {
        color: #fff;
        /* 添加液态透明玻璃效果 */
        background: rgba(255, 255, 255, 0.3);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        border-radius: 10px;
        box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.2);
        border: 1px solid rgba(255, 255, 255, 0.18);
        /* 添加明显的左边竖条纹 */
        position: relative;
        
        &::before {
          content: '';
          position: absolute;
          left: -2px;
          top: 5%;
          width: 4px;
          height: 90%;
          background: linear-gradient(to bottom, #4facfe 0%, #00f2fe 100%);
          border-radius: 0 4px 4px 0;
        }
      }

      .link:not(.router-link-active) .menu-item:hover {
        color: #cccccc;
        /* 添加液态透明玻璃效果（仅鼠标悬停时） */
        background: rgba(255, 255, 255, 0.3);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        border-radius: 10px;
        box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.2);
      }

      .menu-item {
        position: relative;
        width: 50px;
        height: 50px;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-bottom: 10px;
        margin-top: 10px;
        transition: all 0.3s ease;
        z-index: 1;

        .icon {
          font-size: var(--icon-font-size);
          display: block;
          height: 25px;
          width: 25px;
          line-height: 25px;
          z-index: 2;
        }

        .unread-text {
          position: absolute;
          background-color: #f56c6c;
          left: 28px;
          top: 8px;
          color: white;
          border-radius: 30px;
          padding: 0 5px;
          font-size: 10px;
          text-align: center;
          white-space: nowrap;
          border: 1px solid #f1e5e5;
          z-index: 2;
        }

        .unread-dot {
          position: absolute;
          bottom: 1%;
          right: 16%;
          width: 12px;
          height: 12px;
          border-radius: 50%;
          background-color: #f56c6c;
          z-index: 2;
        }
      }
    }

    .bottom-item {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 50px;
      width: 100%;
      cursor: pointer;
      font-size: var(--icon-font-size);

      .icon {
        font-size: var(--icon-font-size);
        display: block;
        height: 25px;
        width: 25px;
        line-height: 25px;
      }

      &:hover {
        font-weight: 600;
        color: #cccccc;
      }
    }
  }

  .content-box {
    flex: 1;
    padding: 0;
    background-color: #fff;
  }
}

.aplayer {
  position: absolute;
  display: flex;
  flex-direction: column; /* 确保垂直排列 */
  height: min-content; /* 显式声明 */
  width: 450px;
  z-index: 99999;
  left: 0;
  bottom: 0;
}
</style>
