import userStore from './userStore';
import localForage from 'localforage';

export default {
    state: {
        privateTalkMaxId: 0,
        notifyCount: 0,
        unreadUserList: [],
        lastTalks: [],
        groupsTalks: new Map(),
        regionTalks: new Map(),
        groupNotify: new Map(),
        regionNotify: new Map()
    },
    mutations: {
        initTalkInfo(state, talkInfo) {
            console.log("初始化动态信息", talkInfo);
            state.privateTalkMaxId = talkInfo.maxId || 0;
            state.unreadUserList = talkInfo.userList || [];
            state.lastTalks = talkInfo.talkList || [];
        },
        setUnreadTalkInfo(state, talkInfo) {
            state.privateTalkMaxId = talkInfo.maxId || state.privateTalkMaxId;
            state.unreadUserList = talkInfo.userList || [];
            state.lastTalks = talkInfo.talkList || [];
            state.notifyCount = talkInfo.notifyCount || 0;
            this.commit("saveTalkToStorage")
        },
        resetUnreadTalkInfo(state) {
            state.unreadUserList = [];
            state.lastTalks = [];
            state.notifyCount = 0;
            this.commit("saveTalkToStorage")
        },
        resetUnreadTalkNotify(state) {
            state.notifyCount = 0;
            this.commit("saveTalkToStorage")
        },
        addNewTalk(state, talk) {
            if (!state.unreadUserList.includes(talk.userId)) {
                state.unreadUserList.push(talk.userId);
            }
            state.lastTalks.unshift(talk);
            if (state.lastTalks.length > 2) {
                state.lastTalks = state.lastTalks.slice(0, 2);
            }

            this.commit("saveTalkToStorage");
        },
        addGroupTalk(state, talk) {
            const newMap = new Map(state.groupsTalks); // 创建副本
            if (!newMap.has(talk.groupId)) {
                newMap.set(talk.groupId, [talk]);
            } else {
                let talks = newMap.get(talk.groupId);
                talks.unshift(talk);
            }
            state.groupsTalks = newMap;
        },
        resetGroupTalk(state, groupId) {
            const newMap = new Map(state.groupsTalks); // 创建副本
            if (newMap.has(groupId)) {
                newMap.set(groupId, []);
                state.groupsTalks = newMap;
            }
        },
        addRegionTalk(state, talk) {
            const newMap = new Map(state.regionTalks); // 创建副本
            if (!newMap.has(talk.regionCode)) {
                newMap.set(talk.regionCode, [talk]);
            } else {
                let talks = newMap.get(talk.regionCode);
                talks.unshift(talk);
            }
            state.regionTalks = newMap;
        },
        resetRegionTalk(state, regionCode) {
            const newMap = new Map(state.regionTalks); // 创建副本
            if (newMap.has(regionCode)) {
                newMap.set(regionCode, []);
                state.regionTalks = newMap;
            }
        },
        addNotifyCount(state, msg) {
            state.notifyCount += 1;
            this.commit("saveTalkToStorage");
        },
        addGroupNotifyCount(state, talk) {
            const newMap = new Map(state.groupNotify); // 创建副本
            if (!newMap.has(talk.groupId)) {
                newMap.set(talk.groupId, 1);
            } else {
                let count = newMap.get(talk.groupId);
                newMap.set(talk.groupId, count + 1);
            }
            state.groupNotify = newMap;
        },
        resetGroupNotify(state, groupId) {
            const newMap = new Map(state.groupNotify);
            if (newMap.has(groupId)) {
                newMap.set(groupId, 0);
                state.groupNotify = newMap;
            }
        },
        addRegionNotifyCount(state, talk) {
            const newMap = new Map(state.regionNotify); // 创建副本
            if (!newMap.has(talk.regionCode)) {
                newMap.set(talk.regionCode, 1);
            } else {
                let count = newMap.get(talk.regionCode);
                newMap.set(talk.regionCode, count + 1);
            }
            state.regionNotify = newMap;
        },
        resetRegionNotify(state, regionCode) {
            const newMap = new Map(state.regionNotify);
            if (newMap.has(regionCode)) {
                newMap.set(regionCode, 0);
                state.regionNotify = newMap;
            }
        },
        saveTalkToStorage(state) {
            let userId = userStore.state.userInfo.id;
            let key = "talk-" + userId;

            let talkInfo = {
                maxId: state.privateTalkMaxId,
                userList: state.unreadUserList,
                talkList: state.lastTalks,
                notifyCount: state.notifyCount
            };

            localForage.setItem(key, talkInfo).then(() => {
                console.log("保存动态数据成功")
            }).catch((e) => {
                console.log("保存动态数据失败")
            })
        }
    },
    getters: {
        getGroupTalkList: (state, getters) =>  (groupId) => {
            return state.groupsTalks.get(groupId) || [];
        }
    },
    actions: {
        loadTalkInfo(context) {
            return new Promise((resolve, reject) => {
                let userId = userStore.state.userInfo.id;
                let key = "talk-" + userId;
                localForage.getItem(key).then((talkData) => {
                    if (!talkData) {
                        resolve();
                    } else {
                        context.commit("initTalkInfo", talkData);
                        resolve();
                    }
                }).catch((e) => {
                    console.log("加载动态数据失败")
                    reject();
                })
            })
        }
    }
}