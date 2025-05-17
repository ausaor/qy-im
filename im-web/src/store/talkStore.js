import userStore from './userStore';
import localForage from 'localforage';

export default {
    state: {
        privateTalkMaxId: 0,
        notifyCount: 0,
        unreadUserList: [],
        lastTalks: [],
        groupsTalkMaxIdMap: new Map(),
        regionTalkMaxIdMap: new Map(),
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