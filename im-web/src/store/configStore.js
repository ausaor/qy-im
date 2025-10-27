import http from '../api/httpRequest.js'

export default {
	state: {
		webrtc: {},
	},
	mutations: {
		setConfig(state, config) {
			state.webrtc = config.webrtc;
		},
		clear(state){
			state.webrtc = {};
		}
	},
	getters: {

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