import { defineStore } from 'pinia';
import followStore from "./followStore";
import friendStore from "./friendStore";

export default defineStore('shortVideoStore', {
    state: () => {
        return {
            // 已记录播放次数的视频 ID 集合
            playedVideoIds: [],
            // 已点赞的评论 ID 集合
            likedCommentIds: [],
            shortVideoMap: new Map(),
            shortVideoNotify: [],
            objectShortVideos: [],// 目标对象的短视频
        }
    },
    actions: {
        setObjectShortVideos(objectShortVideos) {
            this.objectShortVideos = objectShortVideos;
        },
        clearObjectShortVideos() {
            this.objectShortVideos = [];
        },
        /**
         * 记录视频已播放（已调用接口增加播放次数）
         */
        markPlayed(videoId) {
            if (!this.playedVideoIds.includes(videoId)) {
                this.playedVideoIds.push(videoId)
            }
        },
        /**
         * 清空播放记录
         */
        clearPlayed() {
            this.playedVideoIds = []
        },
        /**
         * 记录评论已点赞
         */
        markCommentLiked(commentId) {
            if (!this.likedCommentIds.includes(commentId)) {
                this.likedCommentIds.push(commentId)
            }
        },
        addShortVideo(shortVideo) {
            const newMap = new Map(this.shortVideoMap); // 创建副本
            // 调用 hook 获取其他 store 实例（getter 为实例上的扁平属性，如 follow.isFollow(key)）
            const follow = followStore();
            const friend = friendStore();
            const isFriend = friend.isFriend(shortVideo.userId);
            const isFollow = follow.isFollow(shortVideo.objectId + ':' + shortVideo.type);
            if (shortVideo.type !== 'user' && !isFollow && isFriend) { // 视频类型不是用户发布，且未关注目标对象，但发布用户是好友
                if (!newMap.has(shortVideo.userId + '-user')) {
                    newMap.set(shortVideo.userId + '-' + 'user', [shortVideo]);
                } else {
                    let shortVideos = newMap.get(shortVideo.userId + '-user');
                    shortVideos.unshift(shortVideo);
                }
                this.shortVideoMap = newMap;
                return
            }
            if (!newMap.has(shortVideo.objectId + '-' + shortVideo.type)) {
                newMap.set(shortVideo.objectId + '-' + shortVideo.type, [shortVideo]);
            } else {
                let shortVideos = newMap.get(shortVideo.objectId + '-' + shortVideo.type);
                shortVideos.unshift(shortVideo);
            }
            this.shortVideoMap = newMap;
        },
        resetShortVideo(state, key) {
            const newMap = new Map(this.shortVideoMap);
            if (newMap.has(key)) {
                newMap.set(key, []);
                this.shortVideoMap = newMap;
            }
        },
        clearFriendShortVideos() {
            const friends = friendStore().friends;
            const newMap = new Map(this.shortVideoMap);
            friends.forEach((friend) => newMap.delete(`${friend.id}-user`));
            this.shortVideoMap = newMap;
        },
        clearFollowShortVideos() {
            const follows = followStore().follows;
            const newMap = new Map(this.shortVideoMap);
            follows.forEach((follow) => newMap.delete(`${follow.targetId}-${follow.type}`));
            this.shortVideoMap = newMap;
        },
        addShortVideoNotify(msg) {
            this.shortVideoNotify.push(msg);
        },
        clearShortVideoNotify() {
            this.shortVideoNotify = [];
        }
    },
    getters: {
        /**
         * 判断视频是否已记录播放
         */
        hasPlayed: (state) => (videoId) => {
            return state.playedVideoIds.includes(videoId)
        },

        /**
         * 判断评论是否已点赞
         */
        isCommentLiked: (state) => (commentId) => {
            return state.likedCommentIds.includes(commentId)
        },
        getShortVideoNotifyCount: (state) =>  () => {
            return state.shortVideoNotify.length;
        },
    }
})
