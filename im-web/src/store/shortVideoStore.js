export default {
	state: {
		// 已记录播放次数的视频 ID 集合
		playedVideoIds: []
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
		}
	},

	getters: {
		/**
		 * 判断视频是否已记录播放
		 */
		hasPlayed: (state) => (videoId) => {
			return state.playedVideoIds.includes(videoId)
		}
	}
}