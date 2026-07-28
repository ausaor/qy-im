import friendStore from "./friendStore";
import followStore from "./followStore";

export default {
	state: {
		// 已记录播放次数的视频 ID 集合
		playedVideoIds: [],
		// 已点赞的评论 ID 集合
		likedCommentIds: [],
		showFloat: false,
		character: {},
		template: {},
		shortVideoPublishType: '',
		floatPanelActiveTab: 'recom',
		showCharacterTab: false,
		showTemplateTab: false,
		starTabName: '星选',
		groupId: null,
		shortVideoMap: new Map(),
		shortVideoNotify: []
	},

	mutations: {
		/**
		 * 记录视频已播放（已调用接口增加播放次数）
		 */
		markPlayed(state, videoId) {
			if (!state.playedVideoIds.includes(videoId)) {
				state.playedVideoIds.push(videoId)
			}
		},

		/**
		 * 清空播放记录
		 */
		clearPlayed(state) {
			state.playedVideoIds = []
		},

		/**
		 * 记录评论已点赞
		 */
		markCommentLiked(state, commentId) {
			if (!state.likedCommentIds.includes(commentId)) {
				state.likedCommentIds.push(commentId)
			}
		},
		openShortVideoFloat(state) {
			state.showFloat = true
		},
		closeShortVideoFloat(state) {
			state.showFloat = false
		},
		setShortVideoCharacter(state, character) {
			state.character = character
		},
		clearShortVideoCharacter(state) {
			state.character = {}
		},
		setShortVideoTemplate(state, template) {
			state.template = template
		},
		clearShortVideoTemplate(state) {
			state.template = {}
		},
		setShortVideoPublishType(state, type) {
			state.shortVideoPublishType = type
		},
		clearShortVideoPublishType(state) {
			state.shortVideoPublishType = ''
		},
		setFloatPanelActiveTab(state, tab) {
			state.floatPanelActiveTab = tab
		},
		setShowCharacterTab(state, flag) {
			state.showCharacterTab = flag
		},
		setShowTemplateTab(state, flag) {
			state.showTemplateTab = flag
		},
		setStarTabName(state, name) {
			state.starTabName = name
		},
		setShortVideoGroupId(state, groupId) {
			state.groupId = groupId
		},
		addShortVideo(state, shortVideo) {
			const newMap = new Map(state.shortVideoMap); // 创建副本
			if (shortVideo.type !== 'user'
				&& !followStore.getters.isFollowed(shortVideo.objectId + ':' + shortVideo.type)
				&& friendStore.getters.isFriend(shortVideo.userId)) { // 视频类型不是用户发布，且未关注目标对象，但发布用户是好友
				if (!newMap.has(shortVideo.userId + '-user')) {
					newMap.set(shortVideo.userId + '-' + 'user', [shortVideo]);
				} else {
					let shortVideos = newMap.get(shortVideo.userId + '-user');
					shortVideos.unshift(shortVideo);
				}
				return
			}
			if (!newMap.has(shortVideo.objectId + '-' + shortVideo.type)) {
				newMap.set(shortVideo.objectId + '-' + shortVideo.type, [shortVideo]);
			} else {
				let shortVideos = newMap.get(shortVideo.objectId + '-' + shortVideo.type);
				shortVideos.unshift(shortVideo);
			}
			state.shortVideoMap = newMap;
		},
		resetShortVideo(state, key) {
			const newMap = new Map(state.shortVideoMap);
			if (newMap.has(key)) {
				newMap.set(key, []);
				state.shortVideoMap = newMap;
			}
		},
		addShortVideoNotify(state, msg) {
			state.shortVideoNotify.push(msg);
		},
		clearShortVideoNotify(state) {
			state.shortVideoNotify = [];
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
}