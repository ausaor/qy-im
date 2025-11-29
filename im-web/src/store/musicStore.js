import http from '../api/httpRequest.js'

export default {
    state: {
        showFloatMusic: false,
        musics: [],
        publicMusics: [],
    },
    mutations: {
        toggleFloatMusic(state) {
            state.showFloatMusic = !state.showFloatMusic;
        },
        setMusicList(state, musics) {
            state.musics = musics;
        },
        setPublicMusics(state, musics) {
            state.publicMusics = musics;
        },
        playPublicMusic(state) {
            state.musics = state.publicMusics;
        },
        clearMusicList(state) {
            state.musics = [];
        }
    },
    actions: {
        loadPublicMusics(context) {
            return new Promise((resolve, reject) => {
                http({
                    url: '/music/list',
                    method: 'POST',
                    data: {
                        category: 'public'
                    }
                }).then((result) => {
                    context.commit("setPublicMusics", result);
                    context.commit("setMusicList", result);
                    resolve()
                }).catch((res) => {
                    reject();
                })
            })
        }
    }
}