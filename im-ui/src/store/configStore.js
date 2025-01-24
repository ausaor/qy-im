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
	actions:{
		loadConfig(context){
			return new Promise((resolve, reject) => {
				http({
					url: '/system/config',
					method: 'GET'
				}).then((config) => {
					console.log("系统配置",config)
					context.commit("setConfig",config);
					resolve();
				}).catch((res)=>{
					reject(res);
				});
			})
		}
	}
	
}