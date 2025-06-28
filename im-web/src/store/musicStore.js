export default {
    state: {
        showFloatMusic: false,
        musics: []
    },
    mutations: {
        toggleFloatMusic(state, musics) {
            state.showFloatMusic = !state.showFloatMusic;
            if(state.showFloatMusic) {
                state.musics = musics;
            } else {
                state.musics = [];
            }
        },
        setMusicList(state, musics) {
            state.musics = musics;
        },
        clearMusicList(state) {
            state.musics = [];
        }
    }
}