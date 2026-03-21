<template>
	<div class="rtc-group-video-container">
		<el-dialog v-dialogDrag top="5vh" title="群聊通话" :close-on-click-modal="false" :close-on-press-escape="false"
				   :visible.sync="isShow" width="fit-content" :before-close="onQuit">
			<div class='rtc-group-video'>
				<!-- 视频网格布局 -->
				<div class="video-grid" :class="[{...gridStyle}]">
					<!-- 每个用户的视频窗口 -->
					<div class="video-item" v-for="user in userInfos" :key="user.id">
						<div class="video-box" :class="{'mine': user.id == mine.id}">
							<!-- 头像 (未开启视频时显示) -->
							<head-image v-if="!user.isCamera"
									  :name="user.aliasName || user.nickName" 
									  :url="user.headImage" 
									  :size="100"
									  :isShowUserInfo="false">
							</head-image>
										
							<!-- 用户视频 (开启视频时显示) -->
							<video v-show="user.isCamera"
								   :id="`video${user.id}`"
								   :data-user-id="user.id"
								   autoplay 
								   playsinline></video>
							<!-- 用户信息标签 -->
							<div class="user-info-tag">
								<span>{{ user.aliasName || user.nickName }}</span>
								<span v-if="user.id == host?.id" class="host-tag">主持人</span>
							</div>
										
							<!-- 设备状态图标 -->
							<div class="device-status">
								<i :class="user.isMicroPhone ? 'el-icon-microphone' : 'el-icon-turn-off-microphone turn-off'"></i>
							</div>
						</div>
					</div>
				</div>
			</div>
      <!-- 底部控制栏 -->
      <div class="control-bar" slot="footer">
        <div class="control-btn" @click="toggleMicroPhone()" :title="isMicroPhone ? '关闭麦克风' : '开启麦克风'">
          <i :class="isMicroPhone ? 'el-icon-microphone' : 'el-icon-turn-off-microphone'"></i>
        </div>

        <div class="control-btn" @click="toggleSpeaker()" :title="isSpeaker ? '关闭扬声器' : '开启扬声器'">
          <i class="icon iconfont" :class="isSpeaker ? 'icon-yangshengqi' : 'icon-guanbiyangshengqi'"></i>
        </div>

        <div class="control-btn" @click="toggleCamera()" :title="isCamera ? '关闭摄像头' : '开启摄像头'">
          <i :class="isCamera ? 'el-icon-video-camera-solid' : 'el-icon-video-camera'"></i>
        </div>

        <div class="control-btn" @click="showInviteBox()" title="邀请成员">
          <i class="el-icon-circle-plus-outline"></i>
        </div>

        <div class="control-btn quit-btn" @click="onQuit()" title="退出通话">
          <i class="el-icon-switch-button"></i>
        </div>
      </div>
		</el-dialog>
		
		<!-- 邀请成员对话框 -->
		<group-member-selector ref="inviteSel" :groupId="groupId" @complete="onInviteComplete"></group-member-selector>
    <rtc-group-join ref="rtcJoin" v-if="showJoinDialog" :groupId="groupId" :userInfos="userInfos" :host="host" @accept="onJoinAccept" @reject="onJoinReject"></rtc-group-join>
	</div>
</template>

<script>
	import HeadImage from '@/components/common/HeadImage';
	import GroupMemberSelector from '@/components/group/GroupMemberSelector.vue';

	import ImWebRtc from '@/api/webrtc';
	import ImCamera from '@/api/camera';
	import RtcGroupApi from '@/api/rtcGroupApi';
  import RtcGroupJoin from "@components/rtc/RtcGroupJoin.vue";

	export default {
		name: "rtcGroupVideo",
		components: {
      RtcGroupJoin,
			HeadImage,
			GroupMemberSelector
		},
		data() {
			return {
				camera: new ImCamera(), // 摄像头和麦克风
				webrtc: new ImWebRtc(), // webrtc相关
				API: new RtcGroupApi(), // API
        audio: new Audio(), // 呼叫音频
				isShow: false,
				groupId: null, // 群聊 ID
				isHost: false, // 是否发起人
				inviterId: null, // 邀请人 ID
				host: null, // 主持人信息
				userInfos: [], // 所有用户信息
				showJoinDialog: false, // 是否显示加入通话对话框
				
				// 设备状态
				isCamera: false, // 是否开启摄像头
				isMicroPhone: true, // 是否开启麦克风
				isSpeaker: true, // 是否使用扬声器
				
				// WebRTC 相关
				localStream: null, // 本地流
				peerConnections: new Map(), // 与每个用户的 peerConnection
				candidates: new Map(), // 候选者队列
				
				// 定时器
				heartbeatTimer: null,
        waitTimer:  null, // 呼叫等待计时
        joinMidway: false, // 是否中途加入(用户主动进入)
			}
		},
		computed: {
			mine() {
				return this.$store.state.userStore.userInfo;
			},
			gridStyle() {
				// 根据用户数量返回不同的 class
				const count = this.userInfos.length;
				if (count === 2) {
					return {'two-users-mode': true};
				} else if (count === 4) {
					return {'four-users-mode': true};
				}
				return {};
			},
			configuration() {
				const iceServers = this.$store.state.configStore.webrtc.iceServers;
				return {
					iceServers: iceServers
				}
			},
		},
		methods: {
      initAudio() {
        let url = require(`@/assets/audio/call.wav`);
        this.audio.src = url;
        this.audio.loop = true;
      },
			// 打开多人视频通话
			open(rtcInfo) {
				console.log("打开多人视频通话", rtcInfo);
        if (!this.checkDevEnable()) {
          this.close();
          return;
        }
				this.isShow = true;
				this.groupId = rtcInfo.groupId;
				this.isHost = rtcInfo.isHost;
				this.inviterId = rtcInfo.inviterId;
				this.host = rtcInfo.host;
				this.userInfos = rtcInfo.userInfos || [];
        console.log("用户信息", this.userInfos);
        this.joinMidway = rtcInfo.joinMidway;
        this.isCamera = false;
        this.isMicroPhone = true;

				// 初始化 WebRTC
				this.initRtc();
				
				// 启动心跳
				this.startHeartBeat();
				
				// 如果是主持人，发起呼叫
				if (this.isHost) {
					this.onCall();
				} else if (this.joinMidway) {
          // 中途加入
          this.onJoin();
				} else {
          // 被邀请者，接受通话
          this.onAccept();
        }
			},
			
			// 初始化 WebRTC
			initRtc() {
        this.webrtc.init(this.configuration)
			},

      checkDevEnable() {
        // 检测摄像头
        if (!this.camera.isEnable()) {
          this.$message.error("访问摄像头失败");
          return false;
        }
        // 检测webrtc
        if (!this.webrtc.isEnable()) {
          this.$message.error("初始化RTC失败，原因可能是: 1.服务器缺少ssl证书 2.您的设备不支持WebRTC");
          return false;
        }
        return true;
      },
					
			// 创建与对方的 peerConnection
			createPeerConnection(userId) {
				if (this.peerConnections.has(userId)) {
					return this.peerConnections.get(userId);
				}
							
				const peerConnection = new RTCPeerConnection(this.webrtc.configuration);
				this.peerConnections.set(userId, peerConnection);
				
				// 初始化该用户的 candidate 缓存队列
				if (!this.candidates.has(userId)) {
					this.candidates.set(userId, []);
				}
							
				// 添加本地流到 peerConnection
				if (this.localStream) {
					this.localStream.getTracks().forEach(track => {
						peerConnection.addTrack(track, this.localStream);
					});
				}
							
				// 监听候选者
				peerConnection.onicecandidate = (event) => {
					if (event.candidate) {
						// 发送 candidate 给对方
						this.API.candidate(this.groupId, userId, JSON.stringify(event.candidate)).catch((e) => {
							console.error("发送 candidate 失败", e);
						});
					}
				};
							
				// 监听连接状态
				peerConnection.oniceconnectionstatechange = () => {
					const state = peerConnection.iceConnectionState;
					console.log(`peerConnection 状态 [${userId}]:`, state);
								
					// 处理不同的连接状态
					switch (state) {
						case 'connected':
							console.log(`[${userId}] 连接成功`);
							break;
						case 'disconnected':
							console.warn(`[${userId}] 连接断开，尝试恢复...`);
							// 可以尝试重新建立连接
							setTimeout(() => {
								if (peerConnection.iceConnectionState === 'disconnected') {
									console.log(`[${userId}] 仍然断开，创建新的 offer`);
									this.sendOfferToUser(userId);
								}
							}, 2000);
							break;
						case 'failed':
							console.error(`[${userId}] 连接失败，将发送 candidate`);
							// 连接失败，尝试重启 ICE
							peerConnection.restartIce();
							break;
						case 'closed':
							console.log(`[${userId}] 连接已关闭`);
							break;
					}
				};
							
				// 接收远端流
				peerConnection.ontrack = (e) => {
					console.log("收到远端流", userId, e.streams[0]);
					// 找到对应用户的信息
					let user = this.userInfos.find(u => u.id == userId);
					if (user) {
						// 通过 userId 查找对应的 video 元素
						const videoEl = document.getElementById(`video${userId}`);
						if (videoEl) {
							videoEl.srcObject = e.streams[0];
						} else {
							console.warn(`未找到用户 ${userId} 的视频元素`);
						}
					}
				};
							
				return peerConnection;
			},
			
			// 发起呼叫 (主持人)
			onCall() {
				// 打开通话设备
				this.openStream().then(() => {
					// 更新自己的状态
					this.updateMyUserInfo();
										
					// 向服务器发起 setup 请求
					this.API.setup(this.groupId, this.userInfos).then(() => {
            console.log("发起群聊通话成功");
            this.audio.play();
            // 30s没有人进入自动取消
            this.waitTimer = setTimeout(() => {
              this.API.cancel(this.groupId).then(() => {
                this.$message.success("已取消呼叫,通话结束")
                this.close();
              });
            }, 30000)
					}).catch((e) => {
						console.error("发起群聊通话失败", e);
						this.$message.error("发起通话失败");
						this.close();
					});
				}).catch(() => {
					this.$message.error("打开设备失败");
					this.close();
				});
			},
			
			// 接受通话 (被邀请者)
			onAccept() {
				// 打开通话设备
				this.openStream().then(() => {
					// 更新自己的状态
					this.updateMyUserInfo();
											
					// 向服务器发送 accept 请求
					this.API.accept(this.groupId).then(() => {
						console.log("接受群聊通话成功");
						// 为每个用户创建 peerConnection，等待接收主持人的 offer
						this.userInfos.filter(u => u.id != this.mine.id).forEach(user => {
							this.createPeerConnection(user.id);
						});
					}).catch((e) => {
						console.error("接受群聊通话失败", e);
						this.close();
					});
				}).catch(() => {
					this.$message.error("打开设备失败");
					this.close();
				});
			},

      onJoin() {
        // 打开通话设备
        this.openStream().then(() => {
          // 更新自己的状态
          this.updateMyUserInfo();

          // 为每个用户创建 peerConnection，等待接收主持人的 offer
          this.userInfos.filter(u => u.id != this.mine.id).forEach(user => {
            this.createPeerConnection(user.id);
            this.sendOfferToUser(user.id);
          });
        }).catch(() => {
          this.$message.error("打开设备失败");
          this.close();
        });
      },
			
			// 打开摄像头/麦克风
			openStream() {
				return new Promise((resolve, reject) => {
					if (this.isCamera) {
						// 视频模式且开启了摄像头：打开视频和音频
						this.camera.openVideo().then((stream) => {
							this.localStream = stream;
							this.$nextTick(() => {
                const videoEl = document.getElementById(`video${this.mine.id}`)
                videoEl.srcObject = stream;
                videoEl.muted = true;
							});
							resolve(stream);
						}).catch((e) => {
							console.error("打开摄像头失败", e);
							reject(e);
						});
					} else {
						// 语音模式或关闭摄像头：只打开麦克风
						this.camera.openAudio().then((stream) => {
							this.localStream = stream;
							// 语音模式下不需要绑定到 video 元素
              this.$nextTick(() => {
                const videoEl = document.getElementById(`video${this.mine.id}`)
                videoEl.srcObject = stream;
                videoEl.muted = true;
              });
							resolve(stream);
						}).catch((e) => {
							console.error("打开麦克风失败", e);
							reject(e);
						});
					}
				});
			},
			
			// 更新自己的用户信息
			updateMyUserInfo() {
				let myInfo = this.userInfos.find(u => u.id == this.mine.id);
				if (!myInfo) {
					myInfo = {
						id: this.mine.id,
						aliasName: this.mine.nickName,
						nickName: this.mine.nickName,
						headImage: this.mine.headImage,
						isCamera: this.isCamera,
						isMicroPhone: this.isMicroPhone
					};
					this.userInfos.push(myInfo);
				} else {
					myInfo.isCamera = this.isCamera;
					myInfo.isMicroPhone = this.isMicroPhone;
				}
			},
			
			// 切换麦克风
			toggleMicroPhone() {
				this.isMicroPhone = !this.isMicroPhone;
        this.userInfos.find(u => u.id == this.mine.id).isMicroPhone = this.isMicroPhone;
				this.updateDeviceToServer();
						
				// 控制本地流的音轨
				if (this.localStream) {
					this.localStream.getAudioTracks().forEach(track => {
						track.enabled = this.isMicroPhone;
					});
				}
			},
					
			// 切换扬声器
			toggleSpeaker() {
				this.isSpeaker = !this.isSpeaker;
				// 切换所有远端视频的音频输出设备
				this.userInfos.filter(u => u.isCamera && u.id != this.mine.id).forEach(user => {
					const videoEl = document.getElementById(`video${user.id}`);
					if (videoEl && videoEl.srcObject) {
						// 检查浏览器是否支持 setSinkId
						if (typeof videoEl.setSinkId === 'function') {
							// 使用默认设备（空字符串），实际项目中可以根据需求设置具体的 deviceId
							// 这里主要通过 muted 属性来控制是否静音
							videoEl.muted = !this.isSpeaker;
						} else {
							// 不支持 setSinkId 的浏览器，给出提示
							console.warn('浏览器不支持 setSinkId API');
						}
					}
				});
				this.$message.success(this.isSpeaker ? '已开启扬声器' : '已关闭扬声器');
			},
								
		// 切换摄像头
		toggleCamera() {
			const newCameraState = !this.isCamera;
			
			// 立即更新 UI 状态
			this.isCamera = newCameraState;
			this.updateMyUserInfo();
			// 立即通知服务器和其他用户
			this.updateDeviceToServer();

			// 根据新的摄像头状态更新本地流
			if (this.localStream) {
				// 停止所有现有轨道
				this.localStream.getTracks().forEach(track => track.stop());
			}

			this.openStream().then(() => {
				// 等待 DOM 更新完成后绑定视频流
				this.$nextTick(() => {
					if (this.isCamera) {
            const videoEl = document.getElementById(`video${this.mine.id}`)
						videoEl.srcObject = this.localStream;
						videoEl.muted = true;
						console.log("已设置本地视频流到 video 元素");
					} else {
						// 语音模式，只保留音频流
            const videoEl = document.getElementById(`video${this.mine.id}`)
            videoEl.srcObject = this.localStream;
            videoEl.muted = true;
					}
					// 更新所有 peerConnection 的发送轨道
					this.updateLocalTracksInPeerConnections();
				});
			}).catch((e) => {
				console.error("切换摄像头失败", e);
				// 回滚状态
				this.isCamera = !newCameraState;
				this.updateMyUserInfo();
				this.updateDeviceToServer();
			});
		},
			
			// 更新设备状态到服务器
			updateDeviceToServer() {
				this.API.device(this.groupId, this.isCamera, this.isMicroPhone).catch((e) => {
					console.error("更新设备状态失败", e);
				});
			},
					
			// 更新所有 peerConnection 中的本地轨道
			updateLocalTracksInPeerConnections() {
				console.log('开始更新所有 peerConnection 中的本地轨道');
						
				this.peerConnections.forEach((pc, userId) => {
					console.log(`为用户 [${userId}] 更新本地轨道`);
							
					// 获取当前连接的发送器
					const senders = pc.getSenders();
							
					// 策略 1: 优先使用 replaceTrack 方式 (不会改变 m-section)
					if (this.localStream) {
						const videoTrack = this.localStream.getVideoTracks()[0];
						const audioTrack = this.localStream.getAudioTracks()[0];
								
						senders.forEach(sender => {
							if (sender.track) {
								if (sender.track.kind === 'video') {
									// 替换视频轨道
									if (videoTrack) {
										sender.replaceTrack(videoTrack).then(() => {
											console.log(`成功替换视频轨道给用户 [${userId}]`);
										}).catch((e) => {
											console.error(`替换视频轨道失败 [${userId}]`, e);
										});
									} else {
										// 关闭摄像头时，用静音轨道替换
										const silentVideoTrack = this.createSilentVideoTrack();
										sender.replaceTrack(silentVideoTrack).then(() => {
											console.log(`替换为静音视频轨道给用户 [${userId}]`);
										}).catch((e) => {
											console.error(`替换静音视频轨道失败 [${userId}]`, e);
										});
									}
								} else if (sender.track.kind === 'audio') {
									// 替换音频轨道
									if (audioTrack) {
										sender.replaceTrack(audioTrack).then(() => {
											console.log(`成功替换音频轨道给用户 [${userId}]`);
										}).catch((e) => {
											console.error(`替换音频轨道失败 [${userId}]`, e);
										});
									}
								}
							}
						});
								
						// 检查是否有遗漏的轨道类型，如果有则添加
						const hasVideoSender = senders.some(s => s.track && s.track.kind === 'video');
						const hasAudioSender = senders.some(s => s.track && s.track.kind === 'audio');
								
						if (!hasVideoSender && videoTrack) {
							pc.addTrack(videoTrack, this.localStream);
							console.log(`添加新的视频轨道给用户 [${userId}]`);
						}
						if (!hasAudioSender && audioTrack) {
							pc.addTrack(audioTrack, this.localStream);
							console.log(`添加新的音频轨道给用户 [${userId}]`);
						}
					}
							
					// 重新创建 offer 并发送
					pc.createOffer().then((offer) => {
						return pc.setLocalDescription(offer);
					}).then(() => {
						console.log(`发送新的 offer 给用户 [${userId}] (轨道已更新)`);
						this.API.offer(this.groupId, userId, JSON.stringify(pc.localDescription)).catch((e) => {
							console.error(`发送 offer 失败 [${userId}]`, e);
						});
					}).catch((e) => {
						console.error(`创建 offer 失败 [${userId}]`, e);
						// 如果 createOffer 失败，尝试重启 ICE 或重建连接
						if (e.name === 'OperationError') {
							console.warn(`[${userId}] SDP 协商失败，尝试重启 ICE`);
							pc.restartIce();
						}
					});
				});
			},
					
			// 创建静音视频轨道 (黑屏)
			createSilentVideoTrack() {
				const canvas = document.createElement('canvas');
				canvas.width = 640;
				canvas.height = 480;
				const ctx = canvas.getContext('2d');
				// 填充黑色背景
				ctx.fillStyle = '#000000';
				ctx.fillRect(0, 0, canvas.width, canvas.height);
						
				// 从 canvas 捕获视频流
				const stream = canvas.captureStream(30); // 30 FPS
				return stream.getVideoTracks()[0];
			},
			
			// 显示邀请框
			showInviteBox() {
				let ids = this.userInfos.map(u => u.id);
				let maxChannel = this.$store.state.configStore.webrtc.maxChannel;
				this.$refs.inviteSel.open(maxChannel, ids, ids);
			},
			
			// 邀请完成回调
			onInviteComplete(members) {
				let newUserInfos = members.map(m => ({
					id: m.userId,
					aliasName: m.aliasName,
					headImage: m.headImage,
					isCamera: false,
					isMicroPhone: true
				}));
				
				this.API.invite(this.groupId, newUserInfos).then(() => {
					this.$message.success("邀请成功");
				}).catch((e) => {
					console.error("邀请失败", e);
				});
			},
			
			// 启动心跳
			startHeartBeat() {
				this.heartbeatTimer && clearInterval(this.heartbeatTimer);
				this.heartbeatTimer = setInterval(() => {
					this.API.heartbeat(this.groupId).catch(() => {});
				}, 15000);
			},
			
			// 处理 RTC 消息
			onRTCMessage(msg) {
				//console.log("收到群聊 RTC 消息", msg);
				
				// RTC 信令处理
				switch (msg.type) {
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_SETUP:
						this.onRTCSetup(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_ACCEPT:
						this.onRTCAccept(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_REJECT:
						this.onRTCReject(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_FAILED:
						this.onRTCFailed(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_CANCEL:
						this.onRTCCancel(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_QUIT:
						this.onRTCQuit(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_INVITE:
						this.onRTCInvite(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_JOIN:
						this.onRTCJoin(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_OFFER:
						this.onRTCOffer(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_ANSWER:
						this.onRTCAnswer(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_CANDIDATE:
						this.onRTCCandidate(msg);
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_DEVICE:
						this.onRTCDevice(msg);
						break;
				}
			},
			
			// 收到 setup 消息 (被邀请者)
			onRTCSetup(msg) {
        console.log("收到 setup 邀请消息", msg);
				let userInfos = JSON.parse(msg.content);
				this.userInfos = userInfos;
				this.host = userInfos.find(u => u.id == msg.sendId);
        this.groupId= msg.groupId;
				
				// 显示加入通话对话框
				this.showJoinDialog = true;
        this.startHeartBeat();
        this.audio.play();
			},
			
			// 收到 accept 消息
			onRTCAccept(msg) {
        console.log("用户接受通话", msg.sendId);
        // 清理定时器
        this.waitTimer && clearTimeout(this.waitTimer);
        this.waitTimer = null;

        this.audio.pause();
				// 更新用户状态
				let user = this.userInfos.find(u => u.id == msg.sendId);
				if (user) {
					user.inChat = true;
				}
						
				// 如果还没有创建 peerConnection，则创建
				if (!this.peerConnections.has(msg.sendId)) {
					const peerConnection = this.createPeerConnection(msg.sendId);
					// 创建并发送 offer
					const offerParam = {};
					offerParam.offerToRecieveAudio = 1;
					offerParam.offerToRecieveVideo = 1;
					peerConnection.createOffer(offerParam).then((offer) => {
						return peerConnection.setLocalDescription(offer);
					}).then(() => {
						this.API.offer(this.groupId, msg.sendId, JSON.stringify(peerConnection.localDescription)).catch((e) => {
							console.error("发送 offer 失败", e);
						});
					}).catch((e) => {
						console.error("创建 offer 失败", e);
					});
				} else {
					// 如果已经存在 peerConnection（可能是被邀请的用户），重新发送 offer
					console.log(`用户 ${msg.sendId} 已存在 peerConnection，重新发送 offer`);
					this.sendOfferToUser(msg.sendId);
				}
			},
			
			// 收到 reject 消息
			onRTCReject(msg) {
				console.log("用户拒绝通话", msg.sendId);
				// 从用户列表中移除
				this.userInfos = this.userInfos.filter(u => u.id != msg.sendId);
				
				// 关闭对应的 peerConnection
				const peerConnection = this.peerConnections.get(msg.sendId);
				if (peerConnection) {
					peerConnection.close();
					this.peerConnections.delete(msg.sendId);
				}
				
				// 清理 candidate 缓存
				this.candidates.delete(msg.sendId);
			},
			
			// 收到 failed 消息
			onRTCFailed(msg) {
				let data = JSON.parse(msg.content);
				this.$message.error(data.reason || "通话失败");
				// 从用户列表中移除失败的用户
				if (data.userIds) {
					data.userIds.forEach(userId => {
						// 从用户列表中移除
						this.userInfos = this.userInfos.filter(u => u.id != userId);
						
						// 关闭对应的 peerConnection
						const peerConnection = this.peerConnections.get(userId);
						if (peerConnection) {
							peerConnection.close();
							this.peerConnections.delete(userId);
						}
						
						// 清理 candidate 缓存
						this.candidates.delete(userId);
					});
				}
			},
			
			// 收到 cancel 消息
			onRTCCancel(msg) {
				this.$message.success("通话已取消");
				this.close();
			},
			
			// 收到 quit 消息
			onRTCQuit(msg) {
				console.log("用户退出通话", msg.sendId);
				// 从用户列表中移除
				this.userInfos = this.userInfos.filter(u => u.id != msg.sendId);
						
				// 关闭与该用户的 peerConnection
				const peerConnection = this.peerConnections.get(msg.sendId);
				if (peerConnection) {
					peerConnection.close();
					this.peerConnections.delete(msg.sendId);
				}
						
				// 清理该用户的 candidate 缓存
				this.candidates.delete(msg.sendId);
			},
			
			// 收到 invite 消息
			onRTCInvite(msg) {
				let newUserInfos = JSON.parse(msg.content);
				// 添加新用户到列表
				newUserInfos.forEach(u => {
					if (!this.userInfos.find(existing => existing.id == u.id)) {
						this.userInfos.push(u);
					}
				});
				this.$message.success("有新用户加入通话");
						
				// 新邀请的用户创建 peerConnection
        newUserInfos.forEach(u => {
          if (u.id != this.mine.id && !this.peerConnections.has(u.id)) {
            this.createPeerConnection(u.id);
          }
        });
			},
			
			// 收到 join 消息
			onRTCJoin(msg) {
				let userInfo = JSON.parse(msg.content);
				// 添加用户到列表
				if (!this.userInfos.find(u => u.id == userInfo.id)) {
					this.userInfos.push(userInfo);
				}
				this.$message.success(`${userInfo.aliasName} 加入通话`);
						
				// 为加入的用户创建 peerConnection
				if (userInfo.id != this.mine.id) {
          this.createPeerConnection(userInfo.id);
				}
			},
					
			// 向指定用户发送 offer
			sendOfferToUser(userId) {
				const peerConnection = this.peerConnections.get(userId);
				if (!peerConnection) {
					console.warn(`peerConnection 不存在 [${userId}]`);
					return;
				}
						
				const offerParam = {};
				offerParam.offerToRecieveAudio = 1;
				offerParam.offerToRecieveVideo = 1;
						
				peerConnection.createOffer(offerParam).then((offer) => {
					return peerConnection.setLocalDescription(offer);
				}).then(() => {
					this.API.offer(this.groupId, userId, JSON.stringify(peerConnection.localDescription)).catch((e) => {
						console.error(`发送 offer 给 ${userId} 失败`, e);
					});
				}).catch((e) => {
					console.error(`创建 offer 给 ${userId} 失败`, e);
				});
			},
			
			// 收到 offer 消息
			onRTCOffer(msg) {
				console.log("收到 offer", msg);
				let data = JSON.parse(msg.content);
				// 如果还没有创建 peerConnection，则创建
				if (!this.peerConnections.has(msg.sendId)) {
					this.createPeerConnection(msg.sendId);
				}
									
				const peerConnection = this.peerConnections.get(msg.sendId);
				
				// 检查是否已经设置了 remoteDescription
				if (peerConnection.remoteDescription) {
					console.log(`remoteDescription 已存在 [${msg.sendId}],需要重新创建 peerConnection`);
					// 关闭旧的连接并创建新的
					peerConnection.close();
					this.peerConnections.delete(msg.sendId);
					// 创建新的 peerConnection
					const newPeerConnection = this.createPeerConnection(msg.sendId);
					// 设置新的 remoteDescription
					newPeerConnection.setRemoteDescription(new RTCSessionDescription(data)).then(() => {
						console.log("设置远端描述成功", msg.sendId);
						// 处理缓存的 candidate
						this.processCachedCandidates(msg.sendId);
						// 创建 answer
          const offerParam = {};
          offerParam.offerToRecieveAudio = 1;
          offerParam.offerToRecieveVideo = 1;
						return newPeerConnection.createAnswer();
					}).then((answer) => {
						// 设置本地 SDP (answer)
						return newPeerConnection.setLocalDescription(answer);
					}).then(() => {
						// 发送 answer 给对方
						this.API.answer(this.groupId, msg.sendId, JSON.stringify(newPeerConnection.localDescription)).catch((e) => {
							console.error("发送 answer 失败", e);
						});
					}).catch((e) => {
						console.error("处理 offer 失败", e);
					});
				} else {
					// 第一次设置 remoteDescription
					peerConnection.setRemoteDescription(new RTCSessionDescription(data)).then(() => {
						console.log("设置远端描述成功", msg.sendId);
						// 处理缓存的 candidate
						this.processCachedCandidates(msg.sendId);
						// 创建 answer
          const offerParam = {};
          offerParam.offerToRecieveAudio = 1;
          offerParam.offerToRecieveVideo = 1;
						return peerConnection.createAnswer();
					}).then((answer) => {
						// 设置本地 SDP (answer)
						return peerConnection.setLocalDescription(answer);
					}).then(() => {
						// 发送 answer 给对方
						this.API.answer(this.groupId, msg.sendId, JSON.stringify(peerConnection.localDescription)).catch((e) => {
							console.error("发送 answer 失败", e);
						});
					}).catch((e) => {
						console.error("处理 offer 失败", e);
					});
				}
			},
					
			// 收到 answer 消息
			onRTCAnswer(msg) {
				console.log("收到 answer", msg);
				let data = JSON.parse(msg.content);
				const peerConnection = this.peerConnections.get(msg.sendId);
				if (peerConnection) {
					// 检查当前连接状态
					if (peerConnection.connectionState === 'closed' || 
					    peerConnection.iceConnectionState === 'closed') {
						console.warn(`[${msg.sendId}] 连接已关闭，忽略 answer`);
						return;
					}
					
					// 设置远端 SDP (answer)
					peerConnection.setRemoteDescription(new RTCSessionDescription(data)).then(() => {
						console.log("设置远端描述成功", msg.sendId);
						// 处理缓存的 candidate
						this.processCachedCandidates(msg.sendId);
					}).catch((e) => {
						console.error("设置 answer 失败", e);
						// 如果是 m-line 不匹配错误，重新创建 peerConnection
						if (e.message.includes('m-lines') || e.message.includes('order')) {
							console.log(`[${msg.sendId}] m-line 不匹配，重新创建连接`);
							peerConnection.close();
							this.peerConnections.delete(msg.sendId);
							const newPeerConnection = this.createPeerConnection(msg.sendId);
							// 重新发送 offer
							this.sendOfferToUser(msg.sendId);
						}
					});
				}
			},
					
			// 收到 candidate 消息
			onRTCCandidate(msg) {
				//console.log("收到 candidate", msg);
				let data = JSON.parse(msg.content);
				const peerConnection = this.peerConnections.get(msg.sendId);
						
				if (!peerConnection) {
					console.warn(`peerConnection 不存在 [${msg.sendId}]`);
					return;
				}
						
				// 如果还没有设置远端描述，先缓存 candidate
				if (!peerConnection.remoteDescription) {
					console.log(`远端描述未设置，缓存 candidate [${msg.sendId}]`);
					if (!this.candidates.has(msg.sendId)) {
						this.candidates.set(msg.sendId, []);
					}
					this.candidates.get(msg.sendId).push(data);
					return;
				}
						
				// 已设置远端描述，直接添加 candidate
				if (data) {
					peerConnection.addIceCandidate(new RTCIceCandidate(data)).then(() => {
						console.log("添加 candidate 成功", msg.sendId);
					}).catch((e) => {
						console.error("添加 candidate 失败", e);
					});
				}
			},
			
			// 处理缓存的 candidate
			processCachedCandidates(userId) {
				const cached = this.candidates.get(userId);
				if (cached && cached.length > 0) {
					console.log(`处理缓存的 candidate [${userId}], 数量：${cached.length}`);
					const peerConnection = this.peerConnections.get(userId);
					if (!peerConnection) return;
							
					// 逐个添加缓存的 candidate
					cached.forEach(candidateData => {
						peerConnection.addIceCandidate(new RTCIceCandidate(candidateData)).catch((e) => {
							console.error(`添加缓存的 candidate 失败 [${userId}]`, e);
						});
					});
							
					// 清空缓存
					this.candidates.set(userId, []);
				}
			},
						
			// 收到 device 消息
			onRTCDevice(msg) {
				let deviceInfo = JSON.parse(msg.content);
				let user = this.userInfos.find(u => u.id == msg.sendId);
				if (user) {
					user.isCamera = deviceInfo.isCamera;
					user.isMicroPhone = deviceInfo.isMicroPhone;
				}
			},
			
			// 退出通话
			onQuit() {
				if (this.isHost) {
					// 主持人退出，取消通话
					this.API.cancel(this.groupId).then(() => {
						this.$message.success("已取消通话");
						this.close();
					});
				} else {
					// 普通用户退出
					this.API.quit(this.groupId).then(() => {
						this.$message.success("已退出通话");
						this.close();
					});
				}
			},
			
			// 关闭
			close() {
        this.showJoinDialog = false;
        this.isShow = false;
				this.camera.close();
        this.audio.pause();

				// 关闭所有 peerConnection
				this.peerConnections.forEach(pc => pc.close());
				this.peerConnections.clear();
        this.candidates.clear();
				
				if (this.localStream) {
					this.localStream.getTracks().forEach(track => track.stop());
				}
				
				this.heartbeatTimer && clearInterval(this.heartbeatTimer);
        this.waitTimer && clearTimeout(this.waitTimer);
				this.heartbeatTimer = null;
        this.waitTimer = null;
				
				// 重置数据
				this.userInfos = [];
				this.isCamera = false;
				this.isMicroPhone = true;
			},
      onJoinAccept(rtcInfo) {
        this.showJoinDialog = false;
        this.waitTimer && clearTimeout(this.waitTimer);
        this.waitTimer = null;
        this.audio.pause();
        this.open(rtcInfo)
      },
      onJoinReject(groupId) {
        this.API.reject(groupId).then(() => {
          console.log(`拒绝加入群组通话 [${groupId}]`)
        })
        this.close();
      },
		},
    created() {
      // 监听页面刷新事件
      window.addEventListener('beforeunload', () => {
        this.onQuit();
      });
    },
    mounted() {
      // 初始化音频文件
      this.initAudio();
    },
		beforeUnmount() {
			this.onQuit();
		}
	}
</script>

<style scoped lang="scss">
	.rtc-group-video-container {
		.rtc-group-video {
			display: flex;
			flex-direction: column;

			.video-grid {
				display: grid;
				grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
				gap: 15px;
				padding: 15px;
				background-color: #1a1a2e;
				height: fit-content;

				// 2 个用户时：2 列 1 行
				&.two-users-mode {
					grid-template-columns: repeat(2, 1fr);
				}

				// 4 个用户时：2 列 2 行
				&.four-users-mode {
					grid-template-columns: repeat(2, 1fr);
				}

				.video-item {
					position: relative;
					background-color: #16213e;
					border-radius: 8px;
					overflow: hidden;
					display: flex;
					align-items: center;
					justify-content: center;
					aspect-ratio: 1 / 1;

					.video-box {
						position: relative;
						width: 300px;
						height: 300px;
						display: flex;
						align-items: center;
						justify-content: center;

						&.mine {
							border: 2px solid #4facfe;
						}

						video {
							width: 100%;
							height: 100%;
							object-fit: cover;
						}

						.user-name {
							position: absolute;
							bottom: 10px;
							left: 50%;
							transform: translateX(-50%);
							color: white;
							font-size: 14px;
							text-shadow: 0 2px 4px rgba(0,0,0,0.5);
						}

						.user-info-tag {
							position: absolute;
							top: 10px;
							left: 10px;
							color: white;
							font-size: 12px;
							background: rgba(0,0,0,0.5);
							padding: 4px 8px;
							border-radius: 4px;

							.host-tag {
								margin-left: 5px;
								background: #ff6b6b;
								padding: 2px 6px;
								border-radius: 3px;
								font-size: 10px;
							}
						}

						.device-status {
							position: absolute;
							top: 10px;
							right: 10px;
							color: white;
							font-size: 16px;

							i {
								margin-left: 5px;
							}
						}

            .turn-off {
              color: red;
            }
					}
				}
			}
		}

    .control-bar {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 20px;
      padding: 20px;
      background-color: #f5f5f5;

      .control-btn {
        display: flex;
        flex-direction: column;
        align-items: center;
        cursor: pointer;
        padding: 10px;
        border-radius: 8px;
        transition: all 0.3s;

        &:hover {
          background-color: #e0e0e0;
        }

        i {
          font-size: 24px;
          margin-bottom: 5px;
        }

        span {
          font-size: 12px;
        }

        &.quit-btn {
          color: #ff6b6b;

          &:hover {
            background-color: #ffeaea;
          }
        }
      }
    }
	}
</style>
