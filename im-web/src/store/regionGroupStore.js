import http from '../api/httpRequest.js'
import { generateShortId } from '../utils/id-generator.js'
import { MESSAGE_TYPE, MESSAGE_STATUS } from "../api/enums.js"
import { processAtUsers } from '../api/common.js'
import userStore from './userStore';
import localForage from 'localforage';

/* 为了加速拉取离线消息效率，拉取时消息暂时存储到cacheChats,等
待所有离线消息拉取完成后，再统一放至vuex中进行渲染*/
let cacheChats = [];

export default {
	state: {
		regionGroups: [],
		activeRegionGroup: null,

		activeRegionChat: null,
		regionGroupMsgMaxId: 0,
		loadingRegionGroupMsg: false,
		regionChats: []
	},
	mutations: {
		setRegionGroups(state, regionGroups) {
			state.regionGroups = regionGroups;
		},
		activeRegionGroup(state, idx) {
			state.activeRegionGroup = state.regionGroups[idx];
		},
		addRegionGroup(state, regionGroup) {
			state.regionGroups.unshift(regionGroup);
		},
		removeRegionGroup(state, regionGroupId) {
			state.regionGroups.forEach((g, idx) => {
				if (g.id == regionGroupId) {
					state.regionGroups.splice(idx, 1);
				}
			})
			if (state.activeRegionGroup !=null && state.activeRegionGroup.id == regionGroupId) {
				state.activeRegionGroup = null;
			}
		},
		updateRegionGroup(state, regionGroup) {
			state.regionGroups.forEach((g, idx) => {
				if (g.id == regionGroup.id) {
					// 拷贝属性
					Object.assign(state.regionGroups[idx], regionGroup);
				}
			})
		},

		/*regionGroupChat start*/
		initRegionChats(state, chatsData) {
			state.regionChats = [];
			state.regionGroupMsgMaxId = chatsData.regionGroupMsgMaxId || 0;
			cacheChats = chatsData.regionChats || [];
			// 防止图片一直处在加载中状态
			cacheChats.forEach((chat) => {
				chat.messages.forEach((msg) => {
					if (msg.loadStatus == "loading") {
						msg.loadStatus = "fail"
					}
				})
			})
		},
		openRegionChat(state, chatInfo) {
			let chats = this.getters.findRegionChats();
			let chat = null;
			for (let idx in chats) {
				if (chats[idx].targetId === chatInfo.targetId) {
					chat = chats[idx];
					// 放置头部
					//this.commit("moveTop", idx)
					break;
				}
			}
			// 创建会话
			if (chat == null) {
				chat = {
					targetId: chatInfo.targetId,
					type: chatInfo.type,
					showName: chatInfo.showName,
					headImage: chatInfo.headImage,
					lastContent: "",
					lastSendTime: new Date().getTime(),
					unreadCount: 0,
					messages: [],
					atMe: false,
					atAll: false,
					stored: false,
					delete: false
				};
				chats.unshift(chat);
			}
		},
		activeRegionChat(state, idx) {
			let chats = this.getters.findRegionChats();
			state.activeRegionChat = chats[idx];
		},
		activeRegionChatById(state, id) {
			let chats = this.getters.findRegionChats();
			for (let idx in chats) {
				if (chats[idx].targetId === id) {
					state.activeRegionChat = chats[idx];
					break;
				}
			}
		},
		resetRegionUnreadCount(state, chatInfo) {
			let chats = this.getters.findRegionChats();
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId == chatInfo.targetId) {
					chats[idx].unreadCount = 0;
					chats[idx].atMe = false;
					chats[idx].atAll = false;
					chats[idx].stored = false;
					this.commit("regionSaveToStorage");
					break;
				}
			}
		},
		readedRegionMessage(state, pos) {
			let chat = this.getters.findChatByFriend(pos.friendId);
			chat.messages.forEach((m) => {
				if (m.id && m.selfSend && m.status < MESSAGE_STATUS.RECALL) {
					// pos.maxId为空表示整个会话已读
					if (!pos.maxId || m.id <= pos.maxId) {
						m.status = MESSAGE_STATUS.READED
						chat.stored = false;
					}
				}
			})
			this.commit("regionSaveToStorage");
		},
		removeRegionChat(state, idx) {
			let chats = this.getters.findRegionChats();
			if (chats[idx] == state.activeRegionChat) {
				state.activeRegionChat = null;
			}
			chats[idx].delete = true;
			chats[idx].stored = false;
			this.commit("regionSaveToStorage");
		},
		removeRegionGroupChat(state, regionGroupId) {
			let chats = this.getters.findRegionChats();
			for (let idx in chats) {
				if (chats[idx].targetId === regionGroupId) {
					this.commit("removeRegionChat", idx)
					break;
				}
			}
		},
		moveRegionTop(state, idx) {
			// 加载中不移动，很耗性能
			if (this.getters.isRegionLoading()) {
				return;
			}
			if (idx > 0) {
				let chats = this.getters.findRegionChats();
				let chat = chats[idx];
				chats.splice(idx, 1);
				chats.unshift(chat);
				chat.lastSendTime = new Date().getTime();
				chat.stored = false;
				this.commit("regionSaveToStorage");
			}
		},
		insertRegionMessage(state, msgInfo) {
			// 记录消息的最大id
			state.regionGroupMsgMaxId = msgInfo.id;
			// 如果是已存在消息，则覆盖旧的消息数据
			let chat = this.getters.findRegionChat(msgInfo);
			let message = this.getters.findRegionMessage(chat, msgInfo);
			if (message) {
				Object.assign(message, msgInfo);
				// 撤回消息需要显示
				if (msgInfo.type == MESSAGE_TYPE.RECALL) {
					chat.lastContent = msgInfo.content;
				}
				chat.stored = false;
				this.commit("regionSaveToStorage");
				return;
			}
			// 插入新的数据
			if (msgInfo.type == MESSAGE_TYPE.IMAGE) {
				chat.lastContent = "[图片]";
			} else if (msgInfo.type == MESSAGE_TYPE.FILE) {
				chat.lastContent = "[文件]";
			} else if (msgInfo.type == MESSAGE_TYPE.AUDIO) {
				chat.lastContent = "[语音]";
			} else if (msgInfo.type == MESSAGE_TYPE.ACT_RT_VOICE) {
				chat.lastContent = "[语音通话]";
			} else if (msgInfo.type == MESSAGE_TYPE.ACT_RT_VIDEO) {
				chat.lastContent = "[视频通话]";
			} else if (msgInfo.type == MESSAGE_TYPE.TEXT ||
				msgInfo.type == MESSAGE_TYPE.RECALL ||
				msgInfo.type == MESSAGE_TYPE.TIP_TEXT) {
				if (msgInfo.atUserIds && msgInfo.atUserIds.length > 0) {
					chat.lastContent = processAtUsers(msgInfo.content, msgInfo.atUserIds);
				} else {
					chat.lastContent = msgInfo.content;
				}
			}
			chat.lastSendTime = msgInfo.sendTime;
			chat.sendNickName = msgInfo.sendNickName;
			// 未读加1
			if (!msgInfo.selfSend && msgInfo.status != MESSAGE_STATUS.READED && msgInfo.type != MESSAGE_TYPE.TIP_TEXT) {
				chat.unreadCount++;
			}
			// 是否有人@我
			if (!msgInfo.selfSend && msgInfo.atUserIds &&
				msgInfo.status != MESSAGE_STATUS.READED) {
				let userId = userStore.state.userInfo.id;
				if (msgInfo.atUserIds.indexOf(userId) >= 0) {
					chat.atMe = true;
				}
				if (msgInfo.atUserIds.indexOf(-1) >= 0) {
					chat.atAll = true;
				}
			}
			// 间隔大于10分钟插入时间显示
			if (!chat.lastTimeTip || (chat.lastTimeTip < msgInfo.sendTime - 600 * 1000)) {
				chat.messages.push({
					uid: generateShortId(),
					sendTime: msgInfo.sendTime,
					type: MESSAGE_TYPE.TIP_TIME,
				});
				chat.lastTimeTip = msgInfo.sendTime;
			}
			// 根据id顺序插入，防止消息乱序
			let insertPos = chat.messages.length;
			// 防止 图片、文件 在发送方 显示 在顶端  因为还没存库，id=0
			if (msgInfo.id && msgInfo.id > 0) {
				for (let idx in chat.messages) {
					if (chat.messages[idx].id && msgInfo.id < chat.messages[idx].id) {
						insertPos = idx;
						console.log(`消息出现乱序,位置:${chat.messages.length},修正至:${insertPos}`);
						break;
					}
				}
			}
			chat.messages.splice(insertPos, 0, msgInfo);
			chat.stored = false;
			this.commit("regionSaveToStorage");
		},
		updateRegionMessage(state, msgInfo) {
			// 获取对方id或群id
			let chat = this.getters.findRegionChat(msgInfo);
			let message = this.getters.findRegionMessage(chat, msgInfo);
			if (message) {
				// 属性拷贝
				Object.assign(message, msgInfo);
				chat.stored = false;
				this.commit("regionSaveToStorage");
			}
		},
		deleteRegionMessage(state, msgInfo) {
			let chat = this.getters.findRegionChat(msgInfo);
			for (let idx in chat.messages) {
				// 已经发送成功的，根据id删除
				if (chat.messages[idx].id && chat.messages[idx].id == msgInfo.id) {
					chat.messages.splice(idx, 1);
					break;
				}
				// 正在发送中的消息可能没有id，根据发送时间删除
				if (msgInfo.selfSend && chat.messages[idx].selfSend &&
					chat.messages[idx].sendTime == msgInfo.sendTime) {
					chat.messages.splice(idx, 1);
					break;
				}
			}
			chat.stored = false;
			this.commit("regionSaveToStorage");
		},
		updateRegionChatFromGroup(state, group) {
			let chat = this.getters.findRegionChatByGroup(group.id);
			if (chat && (chat.showName != group.regionGroupName)) {
				chat.showName = group.regionGroupName;
				chat.stored = false;
				this.commit("regionSaveToStorage")
			}
		},
		loadingRegionGroupMsg(state, loading) {
			state.loadingRegionGroupMsg = loading;
			if (!this.getters.isRegionLoading()) {
				this.commit("refreshRegionChats")
			}
		},
		refreshRegionChats(state) {
			if(!cacheChats){
				return;
			}
			// 排序
			cacheChats.sort((chat1, chat2) => {
				return chat2.lastSendTime - chat1.lastSendTime;
			});
			// 将消息一次性装载回来
			state.regionChats = cacheChats;
			// 清空缓存,不再使用
			cacheChats = null;
			this.commit("regionSaveToStorage");
		},
		regionSaveToStorage: function (state) {
			// 加载中不保存，防止卡顿
			if (this.getters.isRegionLoading()) {
				return;
			}
			let userId = userStore.state.userInfo.id;
			let key = "region-chats-" + userId;
			let chatKeys = [];
			// 按会话为单位存储，
			state.regionChats.forEach((chat) => {
				// 只存储有改动的会话
				let chatKey = `${key}-${chat.type}-${chat.targetId}`;
				let findRegionGroup = this.getters.findRegionGroupById(chat.targetId);
				if (findRegionGroup === undefined || findRegionGroup === null) {
					chat.delete = true;
					localForage.removeItem(chatKey);
				}
				if (!chat.stored) {
					if (chat.delete) {
						localForage.removeItem(chatKey);
					} else {
						localForage.setItem(chatKey, chat);
					}
					chat.stored = true;
				}
				if (!chat.delete) {
					chatKeys.push(chatKey);
				}
			})
			// 会话核心信息
			let chatsData = {
				regionGroupMsgMaxId: state.regionGroupMsgMaxId,
				chatKeys: chatKeys
			}
			localForage.setItem(key, chatsData)
			// 清理已删除的会话
			state.regionChats = state.regionChats.filter(chat => !chat.delete)
		},
		/*regionGroupChat end*/

		clear(state) {
			state.regionGroups = [];
			state.activeRegionGroup = null;

			cacheChats = []
			state.regionChats = [];
			state.activeRegionChat = null;
		},
	},
	actions: {
		loadRegionGroup(context) {
			return new Promise((resolve, reject) => {
				http({
					url: '/region/group/list',
					method: 'GET'
				}).then((regionGroups) => {
					context.commit("setRegionGroups", regionGroups);
					console.log("loadRegionGroup")
					resolve();
				}).catch((res) => {
					reject(res);
				})
			});
		},
		loadRegionChat(context) {
			console.log("loadRegionChat")
			return new Promise((resolve, reject) => {
				let userId = userStore.state.userInfo.id;
				let key = "region-chats-" + userId;
				localForage.getItem(key).then((chatsData) => {
					if (!chatsData) {
						resolve();
					} else if (chatsData.chatKeys) {
						const promises = [];
						chatsData.chatKeys.forEach(key => {
							promises.push(localForage.getItem(key))
						})
						Promise.all(promises).then(chats => {
							chatsData.regionChats = chats.filter(o => o);
							context.commit("initRegionChats", chatsData);
							resolve();
						})
					}
				}).catch((e) => {
					console.log("加载消息失败")
					reject();
				})
			})
		}
	},
	getters: {
		isRegionLoading: (state) => () => {
			return state.loadingRegionGroupMsg;
		},
		findRegionChats: (state, getters) => () => {
			if(cacheChats && getters.isRegionLoading()){
				return cacheChats;
			}
			return state.regionChats;
		},
		findRegionChatIdx: (state, getters) => (chat) => {
			let chats = getters.findRegionChats();
			for (let idx in chats) {
				if (chats[idx].targetId === chat.targetId) {
					chat = chats[idx];
					return idx
				}
			}
		},
		findRegionChat: (state, getters) => (msgInfo) => {
			let chats = getters.findRegionChats();
			// 获取群id
			let targetId = msgInfo.regionGroupId;
			let chat = null;
			for (let idx in chats) {
				if (chats[idx].targetId === targetId) {
					chat = chats[idx];
					break;
				}
			}
			return chat;
		},
		findRegionChatByFriend: (state, getters) => (fid) => {
			let chats = getters.findRegionChats();
			return chats.find(chat => chat.targetId == fid)
		},
		findRegionChatByGroup: (state, getters) => (gid) => {
			let chats = getters.findRegionChats();
			return chats.find(chat => chat.targetId == gid)
		},
		findRegionMessage: (state) => (chat, msgInfo) => {
			if (!chat) {
				return null;
			}
			for (let idx in chat.messages) {
				// 通过id判断
				if (msgInfo.id && chat.messages[idx].id == msgInfo.id) {
					return chat.messages[idx];
				}
				// 正在发送中的消息可能没有id,只有tmpId
				if (msgInfo.tmpId && chat.messages[idx].tmpId &&
					chat.messages[idx].tmpId == msgInfo.tmpId) {
					return chat.messages[idx];
				}
			}
		},
		findRegionGroupById: (state, getters) => (gid) => {
			return state.regionGroups.find(item => item.id === gid);
		},
	}
}