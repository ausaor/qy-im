import http from '../api/httpRequest.js'

export default {

    state: {
        follows: [],
    },
    mutations: {
        setFollows(state, follows) {
            state.follows = follows;
        },
        updateFollow(state,follow){
            state.follows.forEach((f, index)=>{
                if(f.targetId===follow.targetId && f.type===follow.type){
                    // 拷贝属性
                    Object.assign(state.follows[index], follow);
                }
            })
        },
        removeFollow(state, follow) {
            const idx = state.follows.findIndex(f => f.targetId === follow.targetId && f.type === follow.type);
            if (idx !== -1) {
                state.follows.splice(idx, 1);
            }
        },
        addFollow(state, follow) {
            const idx = state.follows.findIndex(f => f.targetId === follow.targetId && f.type === follow.type);
            if (idx === -1) {
                state.follows.push(follow);
            }
        },
        clear(state) {
            state.follows = [];
        }
    },
    getters: {
        isFollowed: (state) => (key) => {
            return state.follows.some(f => f.targetId+ ':' + f.type === key);
        },
    },
    actions: {
        loadFollow(context) {
            return new Promise((resolve, reject) => {
                http({
                    url: '/follow/list',
                    method: 'GET'
                }).then((follows) => {
                    console.log("loadFollow")
                    context.commit("setFollows", follows);
                    resolve()
                }).catch((res) => {
                    reject();
                })
            });
        }
    }
}
