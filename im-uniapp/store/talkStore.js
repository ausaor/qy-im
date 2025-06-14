import { defineStore } from 'pinia';
import useUserStore from './userStore';

export default defineStore('talkStore', {
    state: () => ({
        privateTalkMaxId: 0,
        notifyCount: 0,
        unreadUserList: [],
        lastTalks: [],
        groupsTalks: new Map(),
        regionTalks: new Map(),
        groupNotify: new Map(),
        regionNotify: new Map()
    }),
    actions: {
        initTalkInfo(talkInfo) {
            console.log("初始化动态信息", talkInfo);
            this.privateTalkMaxId = talkInfo.maxId || 0;
            this.unreadUserList = talkInfo.userList || [];
            this.lastTalks = talkInfo.talkList || [];
        },
        setUnreadTalkInfo(talkInfo) {
            this.privateTalkMaxId = talkInfo.maxId || this.privateTalkMaxId;
            this.unreadUserList = talkInfo.userList || [];
            this.lastTalks = talkInfo.talkList || [];
            this.notifyCount = talkInfo.notifyCount || 0;
            this.saveTalkToStorage();
        },
        resetUnreadTalkInfo() {
            this.unreadUserList = [];
            this.lastTalks = [];
            this.notifyCount = 0;
            this.saveTalkToStorage();
        },
        resetUnreadTalkNotify() {
            this.notifyCount = 0;
            this.saveTalkToStorage();
        },
        resetTalkList() {
            this.unreadUserList = [];
            this.lastTalks = [];
            this.saveTalkToStorage();
        },
        addNewTalk(talk) {
            this.privateTalkMaxId = talk.id;
            if (!this.unreadUserList.includes(talk.userId)) {
                this.unreadUserList.push(talk.userId);
            }
            this.lastTalks.unshift(talk);
            if (this.lastTalks.length > 2) {
                this.lastTalks = this.lastTalks.slice(0, 2);
            }

            this.saveTalkToStorage();
        },
        addGroupTalk(talk) {
            const newMap = new Map(this.groupsTalks); // 创建副本
            if (!newMap.has(talk.groupId)) {
                newMap.set(talk.groupId, [talk]);
            } else {
                let talks = newMap.get(talk.groupId);
                talks.unshift(talk);
            }
            this.groupsTalks = newMap;
        },
        resetGroupTalk(groupId) {
            const newMap = new Map(this.groupsTalks); // 创建副本
            if (newMap.has(groupId)) {
                newMap.set(groupId, []);
                this.groupsTalks = newMap;
            }
        },
        addRegionTalk(talk) {
            const newMap = new Map(this.regionTalks); // 创建副本
            if (!newMap.has(talk.regionCode)) {
                newMap.set(talk.regionCode, [talk]);
            } else {
                let talks = newMap.get(talk.regionCode);
                talks.unshift(talk);
            }
            this.regionTalks = newMap;
        },
        resetRegionTalk(regionCode) {
            const newMap = new Map(this.regionTalks); // 创建副本
            if (newMap.has(regionCode)) {
                newMap.set(regionCode, []);
                this.regionTalks = newMap;
            }
        },
        addNotifyCount() {
            this.notifyCount += 1;
            this.saveTalkToStorage();
        },
        addGroupNotifyCount(talk) {
            const newMap = new Map(this.groupNotify); // 创建副本
            if (!newMap.has(talk.groupId)) {
                newMap.set(talk.groupId, 1);
            } else {
                let count = newMap.get(talk.groupId);
                newMap.set(talk.groupId, count + 1);
            }
            this.groupNotify = newMap;
        },
        resetGroupNotify(groupId) {
            const newMap = new Map(this.groupNotify);
            if (newMap.has(groupId)) {
                newMap.set(groupId, 0);
                this.groupNotify = newMap;
            }
        },
        addRegionNotifyCount(talk) {
            const newMap = new Map(this.regionNotify); // 创建副本
            if (!newMap.has(talk.regionCode)) {
                newMap.set(talk.regionCode, 1);
            } else {
                let count = newMap.get(talk.regionCode);
                newMap.set(talk.regionCode, count + 1);
            }
            this.regionNotify = newMap;
        },
        resetRegionNotify(regionCode) {
            const newMap = new Map(this.regionNotify);
            if (newMap.has(regionCode)) {
                newMap.set(regionCode, 0);
                this.regionNotify = newMap;
            }
        },
        saveTalkToStorage() {
            let userStore = useUserStore();
            let userId = userStore.userInfo.id;
            let key = "talk-" + userId;

            let talkInfo = {
                maxId: this.privateTalkMaxId,
                userList: this.unreadUserList,
                talkList: this.lastTalks,
                notifyCount: this.notifyCount
            };

            uni.setStorageSync(key, talkInfo);
        },
        loadTalkInfo(context) {
            return new Promise((resolve, reject) => {
                let userStore = useUserStore();
                let userId = userStore.userInfo.id;
                let key = "talk-" + userId;
                let talkData = uni.getStorageSync(key);
                if (talkData) {
                    this.initTalkInfo(talkData);
                }
                resolve();
            })
        }
    },
    getters: {

    },
})