import http from '../api/httpRequest.js'

export default {
	state: {
		webrtc: {},
		gaoDeMapKey: '',
	},
	mutations: {
		setConfig(state, config) {
			state.webrtc = config.webrtc;
			state.gaoDeMapKey = config.gaoDeMapKey;
		},
		clear(state){
			state.webrtc = {};
		}
	},
	getters: {
		getGaoDeMapKey: (state) => () => {
			return state.gaoDeMapKey
		},
	},
	actions:{
		loadConfig(context){
			return new Promise((resolve, reject) => {
				http({
					url: '/system/config',
					method: 'GET'
				}).then((config) => {
					context.commit("setConfig",config);
					resolve();
				}).catch((res)=>{
					reject(res);
				});
			})
		}
	}
	
}