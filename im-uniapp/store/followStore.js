import { defineStore } from 'pinia';
import http from "../common/request";

export default defineStore('followStore', {
    state: () => {
        return {
            follows: [],
        }
    },
    actions: {
        setFollows(follows) {
            this.follows = follows;
        },
        removeFollow(follow) {
            const idx = this.follows.findIndex(f => f.targetId === follow.targetId && f.type === follow.type);
            if (idx !== -1) {
                this.follows.splice(idx, 1);
            }
        },
        addFollow(follow) {
            const idx = this.follows.findIndex(f => f.targetId === follow.targetId && f.type === follow.type);
            if (idx === -1) {
                this.follows.push(follow);
            }
        },
        clear() {
            this.follows = [];
        },
        loadFollows() {
            return new Promise((resolve, reject) => {
                http({
                    url: '/follow/list',
                    method: 'GET'
                }).then((follows) => {
                    this.setFollows(follows);
                    resolve()
                }).catch((res) => {
                    reject();
                })
            });
        }
    },
    getters: {
        isFollow: (state) => (key) => {
            return state.follows.some(f => f.targetId+ ':' + f.type === key);
        }
    }
})