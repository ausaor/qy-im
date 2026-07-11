export default {
	state: {
		// 已记录播放次数的视频 ID 集合
		playedVideoIds: [],
		// 已点赞的评论 ID 集合
		likedCommentIds: [],
		// 已关注的用户记录（格式：objectId:type）
		followedKeys: [],
		showFloat: false,
		character: {},
		shortVideoPublishType: '',
		floatPanelActiveTab: 'recom',
		showCharacterTab: false,
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

		/**
		 * 记录已关注（key 格式：objectId:type）
		 */
		markFollowed(state, key) {
			if (!state.followedKeys.includes(key)) {
				state.followedKeys.push(key)
			}
		},

		/**
		 * 移除关注记录
		 */
		unmarkFollowed(state, key) {
			const idx = state.followedKeys.indexOf(key)
			if (idx !== -1) {
				state.followedKeys.splice(idx, 1)
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

		/**
		 * 判断是否已关注（key 格式：objectId:type）
		 */
		isFollowed: (state) => (key) => {
			return state.followedKeys.includes(key)
		}
	}
}