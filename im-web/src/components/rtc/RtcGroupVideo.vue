<template>
	<div class="rtc-group-video-container">
		<el-dialog v-dialogDrag top="5vh" title="群聊通话" :close-on-click-modal="false" :close-on-press-escape="false"
				   :visible.sync="isShow" width="80%" :before-close="onQuit">
			<div class='rtc-group-video'>
				<!-- 视频网格布局 -->
				<div class="video-grid" :class="{'voice-mode': !hasVideo}">
					<!-- 每个用户的视频窗口 -->
					<div class="video-item" v-for="user in userInfos" :key="user.id">
						<div class="video-box" :class="{'mine': user.id === mine.id}">
							<!-- 头像 (未开启视频时显示) -->
							<head-image v-if="!user.isCamera" :name="user.aliasName || user.nickName" 
									   :url="user.headImage" :size="100" :isShowUserInfo="false">
								<div class="user-name">{{ user.aliasName || user.nickName }}</div>
							</head-image>
							
							<!-- 远端视频 (开启视频时显示) -->
							<video v-if="user.isCamera && user.id !== mine.id" 
								   ref="remoteVideos" 
								   autoplay 
								   playsinline></video>
							
							<!-- 本地视频 (自己的摄像头) -->
							<video v-if="user.isCamera && user.id === mine.id" 
								   ref="localVideo" 
								   autoplay 
								   playsinline 
								   muted></video>
							
							<!-- 用户信息标签 -->
							<div class="user-info-tag">
								<span>{{ user.aliasName || user.nickName }}</span>
								<span v-if="user.id === host?.id" class="host-tag">主持人</span>
							</div>
							
							<!-- 设备状态图标 -->
							<div class="device-status">
								<i v-if="!user.isMicroPhone" class="el-icon-microphone-off"></i>
								<i v-if="!user.isCamera" class="el-icon-video-camera"></i>
							</div>
						</div>
					</div>
				</div>
				
				<!-- 底部控制栏 -->
				<div class="control-bar">
					<div class="control-btn" @click="toggleMicroPhone()" :title="isMicroPhone ? '关闭麦克风' : '开启麦克风'">
						<i :class="isMicroPhone ? 'el-icon-microphone' : 'el-icon-microphone-off'"></i>
						<span>{{ isMicroPhone ? '麦克风' : '静音' }}</span>
					</div>
					
					<div class="control-btn" @click="toggleSpeaker()" :title="isSpeaker ? '关闭扬声器' : '开启扬声器'">
						<i :class="isSpeaker ? 'el-icon-headset' : 'el-icon-ringing'"></i>
						<span>{{ isSpeaker ? '扬声器' : '听筒' }}</span>
					</div>
					
					<div class="control-btn" @click="toggleCamera()" :title="isCamera ? '关闭摄像头' : '开启摄像头'"
					     v-if="mode === 'video'">
						<i :class="isCamera ? 'el-icon-video-camera' : 'el-icon-video-play'"></i>
						<span>{{ isCamera ? '摄像头' : '视频' }}</span>
					</div>
					
					<div class="control-btn" @click="showInviteBox()" title="邀请成员">
						<i class="el-icon-circle-plus-outline"></i>
						<span>邀请</span>
					</div>
					
					<div class="control-btn quit-btn" @click="onQuit()" title="退出通话">
						<i class="el-icon-switch-button"></i>
						<span>退出</span>
					</div>
				</div>
			</div>
		</el-dialog>
		
		<!-- 邀请成员对话框 -->
		<group-member-selector ref="inviteSel" :groupId="groupId" @complete="onInviteComplete"></group-member-selector>
    <rtc-group-join ref="rtcJoin"></rtc-group-join>
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
				
				isShow: false,
				groupId: null, // 群聊 ID
				mode: 'voice', // voice:语音 video:视频
				isHost: false, // 是否发起人
				inviterId: null, // 邀请人 ID
				host: null, // 主持人信息
				userInfos: [], // 所有用户信息
				
				// 设备状态
				isCamera: false, // 是否开启摄像头
				isMicroPhone: true, // 是否开启麦克风
				isSpeaker: true, // 是否使用扬声器
				
				// WebRTC 相关
				localStream: null, // 本地流
				peerConnections: new Map(), // 与每个用户的 peerConnection
				candidates: [], // 候选者队列
				
				// 定时器
				heartbeatTimer: null,
			}
		},
		computed: {
			mine() {
				return this.$store.state.userStore.userInfo;
			},
			hasVideo() {
				// 是否有人开启了视频
				return this.userInfos.some(u => u.isCamera);
			}
		},
		methods: {
			// 打开多人视频通话
			open(rtcInfo) {
				console.log("打开多人视频通话", rtcInfo);
				this.isShow = true;
				this.groupId = rtcInfo.groupId;
				this.mode = rtcInfo.mode || 'voice';
				this.isHost = rtcInfo.isHost;
				this.inviterId = rtcInfo.inviterId;
				this.host = rtcInfo.host;
				this.userInfos = rtcInfo.userInfos || [];
				
				// 初始化 WebRTC
				this.initRtc();
				
				// 启动心跳
				this.startHeartBeat();
				
				// 如果是主持人，发起呼叫
				if (this.isHost) {
					this.onCall();
				} else {
					// 被邀请者，接受通话
					this.onAccept();
				}
			},
			
			// 初始化 WebRTC
			initRtc() {
				const iceServers = this.$store.state.configStore.webrtc.iceServers;
				this.webrtc.init({ iceServers });
			},
			
			// 发起呼叫 (主持人)
			onCall() {
				// 打开通话设备
				this.openStream().then(() => {
					// 更新自己的状态
					this.updateMyUserInfo();
					
					// 向服务器发起 setup 请求
					//let userInfos = this.userInfos.filter(u => u.id !== this.mine.id);
					this.API.setup(this.groupId, this.userInfos).then(() => {
						console.log("发起群聊通话成功");
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
					}).catch((e) => {
						console.error("接受群聊通话失败", e);
						this.close();
					});
				}).catch(() => {
					this.$message.error("打开设备失败");
					this.close();
				});
			},
			
			// 打开摄像头/麦克风
			openStream() {
				return new Promise((resolve, reject) => {
					if (this.mode === 'video') {
						// 视频模式：默认不开启摄像头，点击按钮才开启
						if (this.isCamera) {
							this.camera.openVideo().then((stream) => {
								this.localStream = stream;
								this.$nextTick(() => {
									if (this.$refs.localVideo && this.$refs.localVideo[0]) {
										this.$refs.localVideo[0].srcObject = stream;
									}
								});
								resolve(stream);
							}).catch((e) => {
								console.error("打开摄像头失败", e);
								reject(e);
							});
						} else {
							// 只打开麦克风
							this.camera.openAudio().then((stream) => {
								this.localStream = stream;
								resolve(stream);
							}).catch((e) => {
								console.error("打开麦克风失败", e);
								reject(e);
							});
						}
					} else {
						// 语音模式：只打开麦克风
						this.camera.openAudio().then((stream) => {
							this.localStream = stream;
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
				let myInfo = this.userInfos.find(u => u.id === this.mine.id);
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
				// TODO: 切换音频输出设备
			},
			
			// 切换摄像头
			toggleCamera() {
				if (this.mode !== 'video') return;
				
				this.isCamera = !this.isCamera;
				this.updateDeviceToServer();
				
				if (this.isCamera) {
					// 开启摄像头
					this.openStream().then(() => {
						this.$nextTick(() => {
							if (this.$refs.localVideo && this.$refs.localVideo[0]) {
								this.$refs.localVideo[0].srcObject = this.localStream;
							}
						});
					});
				} else {
					// 关闭摄像头，保留麦克风
					if (this.localStream) {
						this.localStream.getVideoTracks().forEach(track => {
							track.stop();
						});
					}
				}
			},
			
			// 更新设备状态到服务器
			updateDeviceToServer() {
				this.API.device(this.groupId, this.isCamera, this.isMicroPhone).catch((e) => {
					console.error("更新设备状态失败", e);
				});
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
				console.log("收到群聊 RTC 消息", msg);
				
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
				let userInfos = JSON.parse(msg.content);
				this.userInfos = userInfos;
				this.host = userInfos.find(u => u.id === msg.sendId);
				
				// 显示加入通话对话框
				this.$refs.rtcJoin.open({
					host: this.host,
					userInfos: userInfos.filter(u => u.id !== msg.sendId),
          groupId: msg.groupId
				});
			},
			
			// 收到 accept 消息
			onRTCAccept(msg) {
				console.log("用户接受通话", msg.sendId);
				// 更新用户状态
				let user = this.userInfos.find(u => u.id === msg.sendId);
				if (user) {
					user.inChat = true;
				}
			},
			
			// 收到 reject 消息
			onRTCReject(msg) {
				console.log("用户拒绝通话", msg.sendId);
				// 从用户列表中移除
				this.userInfos = this.userInfos.filter(u => u.id !== msg.sendId);
			},
			
			// 收到 failed 消息
			onRTCFailed(msg) {
				let data = JSON.parse(msg.content);
				this.$message.error(data.reason || "通话失败");
				// 从用户列表中移除失败的用户
				if (data.userIds) {
					this.userInfos = this.userInfos.filter(u => !data.userIds.includes(u.id));
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
				this.userInfos = this.userInfos.filter(u => u.id !== msg.sendId);
			},
			
			// 收到 invite 消息
			onRTCInvite(msg) {
				let newUserInfos = JSON.parse(msg.content);
				// 添加新用户到列表
				newUserInfos.forEach(u => {
					if (!this.userInfos.find(existing => existing.id === u.id)) {
						this.userInfos.push(u);
					}
				});
				this.$message.success("有新用户加入通话");
			},
			
			// 收到 join 消息
			onRTCJoin(msg) {
				let userInfo = JSON.parse(msg.content);
				// 添加用户到列表
				if (!this.userInfos.find(u => u.id === userInfo.id)) {
					this.userInfos.push(userInfo);
				}
				this.$message.success(`${userInfo.aliasName} 加入通话`);
			},
			
			// 收到 offer 消息
			onRTCOffer(msg) {
				console.log("收到 offer", msg);
				// TODO: 处理 WebRTC offer
			},
			
			// 收到 answer 消息
			onRTCAnswer(msg) {
				console.log("收到 answer", msg);
				// TODO: 处理 WebRTC answer
			},
			
			// 收到 candidate 消息
			onRTCCandidate(msg) {
				console.log("收到 candidate", msg);
				// TODO: 处理 WebRTC candidate
			},
			
			// 收到 device 消息
			onRTCDevice(msg) {
				let deviceInfo = JSON.parse(msg.content);
				let user = this.userInfos.find(u => u.id === msg.sendId);
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
				this.isShow = false;
				this.camera.close();
				
				// 关闭所有 peerConnection
				this.peerConnections.forEach(pc => pc.close());
				this.peerConnections.clear();
				
				if (this.localStream) {
					this.localStream.getTracks().forEach(track => track.stop());
				}
				
				this.heartbeatTimer && clearInterval(this.heartbeatTimer);
				this.heartbeatTimer = null;
				
				// 重置数据
				this.userInfos = [];
				this.isCamera = false;
				this.isMicroPhone = true;
			}
		},
		beforeUnmount() {
			this.onQuit();
		}
	}
</script>

<style scoped lang="scss">
	.rtc-group-video-container {
		.rtc-group-video {
			min-height: 500px;
			display: flex;
			flex-direction: column;
			
			.video-grid {
				flex: 1;
				display: grid;
				grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
				gap: 10px;
				padding: 10px;
				background-color: #1a1a2e;
				
				&.voice-mode {
					grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
				}
				
				.video-item {
					position: relative;
					background-color: #16213e;
					border-radius: 8px;
					overflow: hidden;
					
					.video-box {
						position: relative;
						width: 100%;
						height: 200px;
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
	}
</style>
