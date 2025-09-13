import http from '../api/httpRequest.js'

export default {

	state: {
		groups: [],
		groupRequests: [],
		activeGroup: null,
	},
	mutations: {
		setGroups(state, groups) {
			state.groups = groups;
		},
		setGroupRequests(state, groupRequests) {
			state.groupRequests = groupRequests;
		},
		activeGroup(state, idx) {
			state.activeGroup = state.groups[idx];
		},
		clearActiveGroup(state) {
			state.activeGroup = null;
		},
		addGroup(state, group) {
			state.groups.unshift(group);
		},
		addGroupRequest(state, groupRequest) {
			state.groupRequests.push(groupRequest);
		},
		removeGroup(state, groupId) {
			state.groups.forEach((g, idx) => {
				if (g.id == groupId) {
					state.groups.splice(idx, 1);
				}
			})
			if (state.activeGroup && state.activeGroup.id == groupId) {
				state.activeGroup = null;
			}
		},
		updateGroup(state, group) {
			state.groups.forEach((g, idx) => {
				if (g.id == group.id) {
					// 拷贝属性
					Object.assign(state.groups[idx], group);
				}
			})
		},
		updateGroupRequest(state, groupRequest) {
			state.groupRequests.forEach((item, index)=>{
				if(item.id===groupRequest.id){
					// 拷贝属性
					Object.assign(state.groupRequests[index], groupRequest);
				}
			})
		},
		clear(state) {
			state.groups = [];
			state.activeGroup = null;
		}
	},
	actions: {
		loadGroup(context) {
			return new Promise((resolve, reject) => {
				http({
					url: '/group/list',
					method: 'GET'
				}).then((groups) => {
					context.commit("setGroups", groups);
					console.log("loadGroup")
					resolve();
				}).catch((res) => {
					reject(res);
				})
			});
		}
	},
	getters: {
		findGroupById: (state, getters) => (gid) => {
			return state.groups.find(item => item.id === gid);
		},
		getGroupRequests: (state) => () => {
			return state.groupRequests;
		},
	}
}