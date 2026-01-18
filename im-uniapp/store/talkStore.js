import { defineStore } from 'pinia';
import useUserStore from './userStore';

export default defineStore('talkStore', {
    state: () => {
        return {
            privateTalkMaxId: 0,
            notifyCount: 0,
            unreadUserList: [],
            lastTalks: [],
            groupsTalks: new Map(),
            regionTalks: new Map(),
            groupNotify: new Map(),
            regionNotify: new Map(),
            characterNotify: new Map(),
            groupTemplateNotify: new Map(),
        }
    },
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
        addStarSpaceNotifyCount(talk) {
            if (talk.characterId) {
                const newMap = new Map(this.characterNotify); // 创建副本
                if (!newMap.has(talk.characterId)) {
                    newMap.set(talk.characterId, 1);
                } else {
                    let count = newMap.get(talk.characterId);
                    newMap.set(talk.characterId, count + 1);
                }
                this.characterNotify = newMap;
            } else if (talk.groupTemplateId) {
                const newMap = new Map(this.groupTemplateNotify); // 创建副本
                if (!newMap.has(talk.groupTemplateId)) {
                    newMap.set(talk.groupTemplateId, 1);
                } else {
                    let count = newMap.get(talk.groupTemplateId);
                    newMap.set(talk.groupTemplateId, count + 1);
                }
                this.groupTemplateNotify = newMap;
            }
        },
        resetGroupTemplateNotify(groupTemplateId) {
            const newMap = new Map(this.groupTemplateNotify);
            if (newMap.has(groupTemplateId)) {
                newMap.set(groupTemplateId, 0);
                this.groupTemplateNotify = newMap;
            }
        },
        resetCharacterNotify(characterId) {
            const newMap = new Map(this.characterNotify);
            if (newMap.has(characterId)) {
                newMap.set(characterId, 0);
                this.characterNotify = newMap;
            }
        },
        resetAllCharacterNotify(state) {
            this.characterNotify = new Map();
            this.groupTemplateNotify = new Map();
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
        getGroupTemplateNotifyCount: (state) => (groupTemplateId) => {
            return state.groupTemplateNotify.get(groupTemplateId) || 0;
        },
        getCharacterNotifyCount: (state) => (characterId) => {
            return state.characterNotify.get(characterId) || 0;
        },
        getTotalCharacterNotifyCount: (state)  => () => {
            let count = 0;
            state.characterNotify.forEach(value => {
                count += value;
            });
            state.groupTemplateNotify.forEach(value => {
                count += value;
            });
            return count;
        },
    },
})