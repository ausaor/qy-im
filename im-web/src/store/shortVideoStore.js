export default {
	state: {
		// 已记录播放次数的视频 ID 集合
		playedVideoIds: [],
		// 已点赞的评论 ID 集合
		likedCommentIds: []
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
		}
	}
}