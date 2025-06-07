<template>
	<el-container class="home-page">
		<el-aside width="5%" class="navi-bar">
			<div class="user-head-image" style="width: 100%">
        <head-image :name="$store.state.userStore.userInfo.nickName"
                    :url="$store.state.userStore.userInfo.headImage"
                    :size="60" @click.native="showSettingDialog=true">
        </head-image>
			</div>

			<el-menu class="menu-bar" text-color="#ddd" style="margin-top: 15px;width: 100%">
				<el-menu-item title="聊天">
					<router-link v-bind:to="'/home/chat'">
            <span>
              <svg class="icon svg-icon" aria-hidden="true">
                <use xlink:href="#icon-xiaoxi"></use>
              </svg>
            </span>
						<div v-show="unreadCount>0" class="unread-text">{{unreadCount}}</div>
					</router-link>
				</el-menu-item>
				<el-menu-item title="好友">
					<router-link v-bind:to="'/home/friend'">
            <span>
              <svg class="icon svg-icon" aria-hidden="true">
                <use xlink:href="#icon-friend"></use>
              </svg>
            </span>
					</router-link>
				</el-menu-item>
				<el-menu-item title="群聊">
					<router-link v-bind:to="'/home/group'">
            <span>
              <svg class="icon svg-icon" aria-hidden="true">
                <use xlink:href="#icon-group"></use>
              </svg>
            </span>
					</router-link>
				</el-menu-item>
        <el-menu-item title="经纬网">
          <router-link v-bind:to="'/home/regionGroup'">
              <span>
                <svg class="icon svg-icon" aria-hidden="true">
                  <use xlink:href="#icon-diqiu"></use>
                </svg>
              </span>
              <div v-show="unreadRegionCount>0" class="unread-text">{{unreadRegionCount}}</div>
          </router-link>
        </el-menu-item>
        <el-menu-item title="广场">
          <router-link v-bind:to="'/home/square'">
            <span>
              <svg class="icon svg-icon" aria-hidden="true">
               <use xlink:href="#icon-fenleiorguangchangorqitatianchong"></use>
              </svg>
            </span>
            <div v-show="unreadUserCount > 0 || notifyCount > 0" class="unread-text">{{unreadUserCount + notifyCount}}</div>
          </router-link>
        </el-menu-item>

				<el-menu-item title="设置" @click="showSetting()">
					<span>
            <svg class="icon svg-icon" aria-hidden="true">
              <use xlink:href="#icon-shezhi"></use>
            </svg>
          </span>
				</el-menu-item>
        <el-menu-item title="操作" @click="showOperation()">
          <span>
            <svg class="icon svg-icon" aria-hidden="true">
              <use xlink:href="#icon-operation"></use>
            </svg>
          </span>
        </el-menu-item>
        <el-menu-item @click="onExit()" title="退出">
          <span>
            <svg class="icon svg-icon" aria-hidden="true">
              <use xlink:href="#icon-tuichu"></use>
            </svg>
          </span>
        </el-menu-item>
			</el-menu>
		</el-aside>
		<el-main class="content-box">
			<router-view></router-view>
		</el-main>
		<setting :visible="showSettingDialog" @close="closeSetting()"></setting>
    <operation :visible="showOperationDialog" @close="closeOperation()"></operation>
    <user-info v-show="uiStore.userInfo.show" :pos="uiStore.userInfo.pos" :user="uiStore.userInfo.user"
               @close="$store.commit('closeUserInfoBox')"></user-info>
    <full-image :visible="uiStore.fullImage.show" :url="uiStore.fullImage.url"
                @close="$store.commit('closeFullImageBox')"></full-image>
    <rtc-private-video ref="rtcPrivateVideo"></rtc-private-video>
    <rtc-private-acceptor ref="rtcPrivateAcceptor"></rtc-private-acceptor>
    <rtc-group-video ref="rtcGroupVideo" ></rtc-group-video>
	</el-container>
</template>

<script>
	import HeadImage from '../components/common/HeadImage.vue';
	import Setting from '../components/setting/Setting.vue';
	import UserInfo from '../components/common/UserInfo.vue';
	import FullImage from '../components/common/FullImage.vue';
	import RtcPrivateVideo from '../components/rtc/RtcPrivateVideo.vue';
	import RtcPrivateAcceptor from '../components/rtc/RtcPrivateAcceptor.vue';
	import Operation from "@/components/operation/Operation";
  import RtcGroupVideo from '../components/rtc/RtcGroupVideo.vue';
  import SSETool from '@/utils/sse-util';

	export default {
		components: {
			HeadImage,
			Setting,
			UserInfo,
			FullImage,
      RtcPrivateVideo,
      RtcPrivateAcceptor,
      RtcGroupVideo,
      Operation
		},
		data() {
			return {
				showSettingDialog: false,
				showOperationDialog: false,
        lastPlayAudioTime: new Date()-1000,
        sse: null,
        messages: []
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
      initSSE() {
        this.sse = new SSETool({
          url: process.env.VUE_APP_SSE_URL,
          token: sessionStorage.getItem("accessToken"), // 从本地存储获取Token
          retryInterval: 5000,
          onOpen: () => {
            console.log('SSE连接成功');
          },
          onError: (err) => {
            console.error('SSE连接错误:', err);
          }
        });

        // 监听默认消息
        this.sse.on('message', (event) => {
          if (event.data === '') {
            console.log('收到心跳包，保持连接活跃');
            // 无需处理业务逻辑
          } else {
            // 正常业务处理
            this.messages.push(event.data);
          }
        });

        // 监听自定义事件
        this.sse.on('customEvent', (event) => {
          console.log('收到自定义事件:', event.data);
        });
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
      handlePrivateMessage(msg) {
        // 消息加载标志
        if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
          console.log("私聊记录加载......", JSON.parse(msg.content))
          this.$store.commit("loadingPrivateMsg", JSON.parse(msg.content))
          return;
        }
        // 消息已读处理，清空已读数量
        if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
          this.$store.commit("resetUnreadCount", {
            type: 'PRIVATE',
            targetId: msg.recvId
          })
          return;
        }
        // 消息回执处理,改消息状态为已读
        if (msg.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
          this.$store.commit("readedMessage", {
            friendId: msg.sendId
          })
          return;
        }
        // 标记这条消息是不是自己发的
        msg.selfSend = msg.sendId == this.$store.state.userStore.userInfo.id;
        // 单人webrtc 信令
        if (this.$msgType.isRtcPrivate(msg.type)) {
          this.$refs.rtcPrivateVideo.onRTCMessage(msg)
          return;
        }
        // 好友id
        let friendId = msg.selfSend ? msg.recvId : msg.sendId;
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
            msg.status != this.$enums.MESSAGE_STATUS.READED) {
          this.playAudioTip();
        }
      },
      handleGroupMessage(msg) {
        // 消息加载标志
        if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
          console.log("群聊记录加载......", JSON.parse(msg.content))
          this.$store.commit("loadingGroupMsg", JSON.parse(msg.content))
          return;
        }
        // 群聊有变更
        if (msg.type == this.$enums.MESSAGE_TYPE.TIP_TEXT && msg.groupChangeType && [1,2,3,5].includes(msg.groupChangeType)) {
          this.eventGroupChange(msg);
        }
        if (msg.type == this.$enums.MESSAGE_TYPE.WORD_VOICE && this.mine.autoPlay) {
          this.eventGroupPlayAudio(msg);
        }
        // 消息已读处理
        if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
          // 我已读对方的消息，清空已读数量
          let chatInfo = {
            type: 'GROUP',
            targetId: msg.groupId
          }
          this.$store.commit("resetUnreadCount", chatInfo)
          return;
        }
        // 消息回执处理
        if (msg.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
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
        msg.selfSend = msg.sendId == this.$store.state.userStore.userInfo.id;
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
        sessionStorage.removeItem("accessToken");
        location.href = "/";
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
          let url = require(`@/assets/audio/tip.wav`);
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
.navi-bar {
  background: white;
  padding: 10px;

  .user-head-image{
    /*转为flex弹性盒布局*/
    display: flex;
    /*主轴上的对齐方式为居中*/
    justify-content: center;
    /*交叉轴上对齐方式为居中*/
    align-items: center;
  }

  .el-menu {
    border: none;
    flex: 1;

    .el-menu-item {
      margin: 25px auto;

      .unread-text {
        position: absolute;
        line-height: 20px;
        background-color: #f56c6c;
        left: 36px;
        top: 7px;
        color: white;
        border-radius: 30px;
        padding: 0 5px;
        font-size: 10px;
        text-align: center;
        white-space: nowrap;
        border: 1px solid #f1e5e5;
      }
    }
  }
}

.menu-bar {
  background: inherit;

  .icon {
    display: block;
    height: 56px;
    line-height: 56px;
    font-size: 28px;
    margin: 10px auto;
    color: #333;
    -webkit-transition: font-size 0.25s linear, width 0.25s linear;
    -moz-transition: font-size 0.25s linear, width 0.25s linear;
    transition: font-size 0.25s linear, width 0.25s linear;
  }
}

.content-box {
  padding: 0;
  background-color: #FFFAFA;
  color: #333;
  text-align: center;
}
</style>
