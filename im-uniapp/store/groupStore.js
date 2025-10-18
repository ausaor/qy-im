import { defineStore } from 'pinia';
import http from '@/common/request';

export default defineStore('groupStore', {
	state: () => {
		return {
			groups: [],
			activeIndex: -1,
			groupRequests: [],
		}
	},
	actions: {
		setGroups(groups) {
			this.groups = groups;
		},
		setGroupRequests(groupRequests) {
			this.groupRequests = groupRequests;
		},
		activeGroup(index) {
			this.activeIndex = index;
		},
		addGroup(group) {
			this.groups.unshift(group);
		},
		addGroupRequest(groupRequest) {
			this.groupRequests.push(groupRequest);
		},
		removeGroup(groupId) {
			this.groups.forEach((g, index) => {
				if (g.id == groupId) {
					this.groups.splice(index, 1);
					if (this.activeIndex >= this.groups.length) {
						this.activeIndex = this.groups.length - 1;
					}
				}
			})
		},
		updateGroup(group) {
			let g = this.findGroup(group.id);
			Object.assign(g, group);
		},
		updateGroupRequest(groupRequest) {
			this.groupRequests.forEach((g, idx) => {
				if (g.id === groupRequest.id) {
					// 拷贝属性
					Object.assign(this.groupRequests[idx], groupRequest);
				}
			})
		},
		clear() {
			this.groups = [];
			this.activeGroup = -1;
		},
		loadGroup() {
			return new Promise((resolve, reject) => {
				http({
					url: '/group/list',
					method: 'GET'
				}).then((groups) => {
					this.setGroups(groups);
					resolve();
				}).catch((res) => {
					reject(res);
				})
			});
		}
	},
	getters: {
		findGroup: (state) => (id) => {
			return state.groups.find((g) => g.id == id);
		}
	}
})