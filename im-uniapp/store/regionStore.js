import { defineStore } from 'pinia';
import http from '@/common/request';
import { MESSAGE_TYPE, MESSAGE_STATUS } from '@/common/enums.js';
import {processAtUsers} from "../common/common.js";
import useUserStore from './userStore';
import { v4 as uuidv4 } from 'uuid'

let cacheChats = [];
export default defineStore('regionStore', {
    state: () => {
        return {
            regionGroups: [],
            activeRegionIndex: -1,

            regionGroupMsgMaxId: 0,
            loadingRegionGroupMsg: false,
            regionChats: []
        }
    },
    actions: {
        setRegionGroups(regionGroups) {
            this.regionGroups= regionGroups;
        },
        addRegionGroup(group) {
            this.regionGroups.unshift(group);
        },
        removeRegionGroup(regionGroupId) {
            this.regionGroups.forEach((g, idx) => {
                if (g.id === regionGroupId) {
                    this.regionGroups.splice(idx, 1);
                }
            })
        },
        activeRegionGroup(index) {
            this.activeRegionIndex = index;
        },
        loadRegionGroup() {
            return new Promise((resolve, reject) => {
                http({
                    url: '/region/group/list',
                    method: 'GET'
                }).then((regionGroups) => {
                    this.setRegionGroups(regionGroups);
                    resolve();
                }).catch((res) => {
                    reject(res);
                })
            });
        },
        updateRegionGroup(regionGroup) {
            this.regionGroups.forEach((g, idx) => {
                if (g.id === regionGroup.id) {
                    // 拷贝属性
                    Object.assign(this.regionGroups[idx], regionGroup);
                }
            })
        },

        /* region chat start */
        removeRegionChat(regionGroupId) {
            let chats = this.curRegionChats;
            for (let idx in chats) {
                if (chats[idx].targetId === regionGroupId) {
                    chats[idx].delete = true;
                    chats[idx].stored = false;
                    this.regionSaveToStorage();
                    break;
                }
            }
        },

        activeRegionChat(regionGroupId) {
            let chats = this.curRegionChats;
            if (chats) {
                for (const idx in chats) {
                    if (chats[idx].targetId == regionGroupId) {
                        chats[idx].unreadCount = 0;
                    }
                }
            }
        },
        setLoadingRegionGroupMsg(loading) {
            this.loadingRegionGroupMsg = loading;
            if (!this.isRegionLoading()) {
                this.refreshRegionChats()
            }
        },
        initRegionChats(chatsData) {
            cacheChats = [];
            this.regionChats = [];
            for (let chat of chatsData.regionChats) {
                // 暂存至缓冲区
                chat.stored = false;
                cacheChats.push(JSON.parse(JSON.stringify(chat)));
                // 加载期间显示只前15个会话做做样子,一切都为了加快初始化时间
                if (this.regionChats.length < 15) {
                    this.regionChats.push(chat);
                }
            }
            this.regionGroupMsgMaxId = chatsData.regionGroupMsgMaxId || 0;
            // 防止图片一直处在加载中状态
            cacheChats.forEach((chat) => {
                chat.messages.forEach((msg) => {
                    if (msg.loadStatus == "loading") {
                        msg.loadStatus = "fail"
                    }
                })
            })
        },
        loadRegionChat(context) {
            return new Promise((resolve, reject) => {
                let userStore = useUserStore();
                let userId = userStore.userInfo.id;
                let chatsData = uni.getStorageSync("region-chats-app-" + userId)
                if (chatsData) {
                    if (chatsData.chatKeys) {
                        let time = new Date().getTime();
                        chatsData.regionChats = [];
                        chatsData.chatKeys.forEach(key => {
                            let chat = uni.getStorageSync(key);
                            if (chat) {
                                chatsData.regionChats.push(chat);
                            }
                        })
                    }
                    this.initRegionChats(chatsData);
                }
                resolve()
            })
        },
        refreshRegionChats() {
            if (!cacheChats) {
                return;
            }
            // 排序
            cacheChats.sort((chat1, chat2) => {
                return chat2.lastSendTime - chat1.lastSendTime;
            });
            // 将消息一次性装载回来
            this.regionChats = cacheChats;
            // 清空缓存，不再使用
            cacheChats = null;
            this.regionSaveToStorage();
        },
        regionSaveToStorage(state) {
            // 加载中不保存，防止卡顿
            if (this.isRegionLoading()) {
                return;
            }
            const userStore = useUserStore();
            let userId = userStore.userInfo.id;
            let key = "region-chats-app-" + userId;
            let chatKeys = [];
            // 按会话为单位存储，只存储有改动的会话
            this.regionChats.forEach((chat) => {
                let chatKey = `${key}-${chat.type}-${chat.targetId}`
                if (!chat.stored) {
                    if (chat.delete) {
                        uni.removeStorageSync(chatKey);
                    } else {
                        uni.setStorageSync(chatKey, chat);
                    }
                    chat.stored = true;
                }
                if (!chat.delete) {
                    chatKeys.push(chatKey);
                }
            })
            // 会话核心信息
            let chatsData = {
                regionGroupMsgMaxId: this.regionGroupMsgMaxId,
                chatKeys: chatKeys
            }
            uni.setStorageSync(key, chatsData)
            // 清理已删除的会话
            this.regionChats = this.regionChats.filter(chat => !chat.delete)
        },
        resetRegionUnreadCount(chatInfo) {
            let chats = this.curRegionChats;
            for (let idx in chats) {
                if (chats[idx].type == chatInfo.type &&
                    chats[idx].targetId == chatInfo.targetId) {
                    chats[idx].unreadCount = 0;
                    chats[idx].atMe = false;
                    chats[idx].atAll = false;
                    chats[idx].stored = false;
                    this.regionSaveToStorage();
                }
            }
        },
        updaterRegionMessage(msgInfo, chatInfo) {
            // 获取对方id或群id
            let chat = this.findRegionChat(chatInfo);
            let message = this.findRegionMessage(chat, msgInfo);
            if (message) {
                // 属性拷贝
                Object.assign(message, msgInfo);
                chat.stored = false;
                this.regionSaveToStorage();
            }
        },
        regionMoveTop(idx) {
            if (this.isRegionLoading()) {
                return;
            }
            let chats = this.curRegionChats;
            if (idx > 0) {
                let chat = chats[idx];
                chats.splice(idx, 1);
                chats.unshift(chat);
                chat.lastSendTime = new Date().getTime();
                chat.stored = false;
                this.regionSaveToStorage();
            }
        },
        openRegionChat(chatInfo) {
            let chats = this.curRegionChats;
            let chat = null;
            for (let idx in chats) {
                if (chats[idx].type == chatInfo.type &&
                    chats[idx].targetId === chatInfo.targetId) {
                    chat = chats[idx];
                    // 放置头部
                    this.regionMoveTop(idx)
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
                    stored: false
                };
                chats.unshift(chat);
                this.regionSaveToStorage();
            }
        },
        insertRegionMessage(msgInfo, chatInfo) {
            // 获取对方id或群id
            let type = chatInfo.type;
            // 记录消息的最大id
            this.regionGroupMsgMaxId = msgInfo.id;
            // 如果是已存在消息，则覆盖旧的消息数据
            let chat = this.findRegionChat(chatInfo);
            let message = this.findRegionMessage(chat, msgInfo);
            if (message) {
                Object.assign(message, msgInfo);
                // 撤回消息需要显示
                if (msgInfo.type == MESSAGE_TYPE.RECALL) {
                    chat.lastContent = msgInfo.content;
                }
                chat.stored = false;
                this.regionSaveToStorage();
                return;
            }
            // 会话列表内容
            if (msgInfo.type == MESSAGE_TYPE.IMAGE) {
                chat.lastContent = "[图片]";
            } else if (msgInfo.type == MESSAGE_TYPE.FILE) {
                chat.lastContent = "[文件]";
            } else if (msgInfo.type == MESSAGE_TYPE.AUDIO) {
                chat.lastContent = "[语音]";
            } else if (msgInfo.type == MESSAGE_TYPE.VIDEO) {
                chat.lastContent = "[视频]";
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
            if (!msgInfo.selfSend && msgInfo.status != MESSAGE_STATUS.READED &&
                msgInfo.type != MESSAGE_TYPE.TIP_TEXT) {
                chat.unreadCount++;
            }
            // 是否有人@我
            if (!msgInfo.selfSend && chat.type == "REGION-GROUP" && msgInfo.atUserIds &&
                msgInfo.status != MESSAGE_STATUS.READED) {
                const userStore = useUserStore();
                let userId = userStore.userInfo.id;
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
                    uid: uuidv4().split('-')[0],
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
                        console.log(`地区消息出现乱序,位置:${chat.messages.length},修正至:${insertPos}`);
                        break;
                    }
                }
            }
            if (insertPos == chat.messages.length) {
                // 这种赋值效率最高
                chat.messages[insertPos] = msgInfo;
            } else {
                chat.messages.splice(insertPos, 0, msgInfo);
            }
            chat.stored = false;
            this.regionSaveToStorage();
        },
        updateRegionChatFromGroup(group) {
            let chat = this.findRegionChatByGroup(group.id);
            if (chat && (chat.showName !== group.regionGroupName)) {
                chat.showName = group.regionGroupName;
                chat.stored = false;
                this.regionSaveToStorage();
            }
        },
        deleteRegionMessage(msgInfo, chatInfo) {
            // 获取对方id或群id
            let chat = this.findRegionChat(chatInfo);
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
            this.regionSaveToStorage();
        },
        /* region chat end */

        clear() {
            this.regionGroups = [];
            this.activeRegionGroup(-1);

            cacheChats = [];
            this.regionChats = [];
            this.regionGroupMsgMaxId = 0;
            this.loadingRegionGroupMsg = false;
        }
    },
    getters: {
        findRegionGroup: (state) => (id) => {
            return state.regionGroups.find((g) => g.id == id);
        },
        isRegionLoading: (state) => () => {
            return state.loadingRegionGroupMsg
        },
        findRegionChatIdx: (state) => (chat) => {
            let chats = state.curRegionChats;
            for (let idx in chats) {
                if (chats[idx].type == chat.type &&
                    chats[idx].targetId === chat.targetId) {
                    chat = state.regionChats[idx];
                    return idx;
                }
            }
        },
        findRegionChatById: (state) => (regionGroupId) => {
            let chats = state.curRegionChats;
            for (let idx in chats) {
                if (chats[idx].targetId == regionGroupId) {
                    return chats[idx];
                }
            }
        },
        findRegionChat: (state) => (chat) => {
            let chats = state.curRegionChats;
            let idx = state.findRegionChatIdx(chat);
            return chats[idx];
        },
        curRegionChats: (state) => {
            if (cacheChats && state.isRegionLoading()) {
                return cacheChats;
            }
            return state.regionChats;
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
        findRegionChatByGroup: (state) => (gid) => {
            let chats = state.curRegionChats;
            return chats.find(chat => chat.targetId === gid)
        },
    }
})