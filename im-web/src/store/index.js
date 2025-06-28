import Vue from 'vue';
import Vuex from 'vuex';
import chatStore from './chatStore.js';
import friendStore from './friendStore.js';
import userStore from './userStore.js';
import groupStore from './groupStore.js';
import regionGroupStore from './regionGroupStore.js';
import configStore from './configStore.js';
import uiStore from './uiStore.js';
import talkStore from './talkStore.js';
import musicStore from "./musicStore.js";
// import VuexPersistence from 'vuex-persist'
//
//
// const vuexLocal = new VuexPersistence({
//     storage: window.localStorage,
// 	modules: ["userStore","chatStore"]
// })

Vue.use(Vuex)

export default new Vuex.Store({
	modules: {chatStore,friendStore,userStore,groupStore,regionGroupStore,configStore,uiStore,talkStore,musicStore},
	state: {},
	//plugins: [vuexLocal.plugin],
	mutations: {
	},
	actions: {
		load(context) {
			return this.dispatch("loadUser").then(() => {
				const promises = [];
				promises.push(this.dispatch("loadFriend"));
				promises.push(this.dispatch("loadGroup"));
				promises.push(this.dispatch("loadRegionGroup"));
				promises.push(this.dispatch("loadChat"));
				promises.push(this.dispatch("loadRegionChat"));
				promises.push(this.dispatch("loadTalkInfo"));
				promises.push(this.dispatch("loadConfig"));
				return Promise.all(promises);
			})
		},
		unload(context){
			context.commit("clear");
		}
	},
	strict: process.env.NODE_ENV !== 'production'
})
