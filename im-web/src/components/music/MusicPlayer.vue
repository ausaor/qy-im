<template>
  <div
      id="music-player"
      class="floating"
      :style="{ left: playerLeft + 'px', top: playerTop + 'px', position: 'fixed' }"
  >
    <!-- 拖拽区域 -->
    <div
        id="drag-handle"
        class="drag-handle"
    >
      <div class="player-title">音乐播放器</div>
      <div class="drag-icon">
        <i class="fa el-icon-rank"></i>
      </div>
    </div>

    <!-- 播放控制区域 -->
    <div class="control-panel">
      <!-- 专辑封面 -->
      <div class="album-container">
        <div class="album-wrapper">
          <img
              :src="currentSong?.cover"
              alt="专辑封面"
              class="album-cover"
          >
          <div class="album-spinner" :class="{ 'is-playing': isPlaying }"></div>
        </div>
      </div>

      <!-- 歌曲信息 -->
      <div class="song-info">
        <h3 class="song-title">{{ currentSong?.title }}</h3>
        <p class="song-artist">{{ currentSong?.artist }}</p>
      </div>

      <!-- 进度条 -->
      <div class="progress-container">
        <div class="time-display">
          <span>{{ formatTime(currentTime) }}</span>
          <span>{{ formatTime(duration) }}</span>
        </div>
        <el-slider
            v-model="currentTime"
            :max="duration"
            class="progress-slider"
            @change="handleProgressChange"
        ></el-slider>
      </div>

      <!-- 控制按钮 -->
      <div class="control-buttons">
        <div
            class="control-btn volume-btn"
            :class="isMuted ? 'el-icon-turn-off-microphone' : 'el-icon-microphone'"
            @click="toggleMute"
        ></div>

        <div
            class="control-btn prev-btn el-icon-d-arrow-left"
            @click="prevSong"
        ></div>

        <div
            :class="isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"
            class="control-btn play-btn"
            @click="togglePlay"
        >
        </div>

        <div
            class="control-btn next-btn el-icon-d-arrow-right"
            @click="nextSong"
        ></div>

        <div
            class="control-btn repeat-btn el-icon-refresh-right"
            @click="toggleRepeat"
        ></div>
      </div>

      <!-- 音量控制 -->
      <div class="volume-control">
        <el-slider
            v-model="volume"
            :max="100"
            class="volume-slider"
            @change="handleVolumeChange"
        ></el-slider>
      </div>

      <!-- 播放模式 -->
      <div class="play-modes">
        <el-button
            class="mode-btn"
            :class="{ active: playMode === 0 }"
            @click="setPlayMode(0)"
        >
          顺序播放
        </el-button>
        <el-button
            class="mode-btn"
            :class="{ active: playMode === 1 }"
            @click="setPlayMode(1)"
        >
          随机播放
        </el-button>
        <el-button
            class="mode-btn"
            :class="{ active: playMode === 2 }"
            @click="setPlayMode(2)"
        >
          单曲循环
        </el-button>
      </div>
    </div>

    <!-- 歌曲列表区域 -->
    <div class="playlist-panel">
      <div
          id="playlist-header"
          class="playlist-header"
          @click="togglePlaylist"
      >
        <div class="playlist-title">歌曲列表</div>
        <div class="toggle-icon">
          <i :class="isPlaylistOpen ? 'fa el-icon-arrow-up' : 'fa el-icon-arrow-down'"></i>
        </div>
      </div>

      <div
          id="playlist-container"
          class="playlist-container"
          v-if="isPlaylistOpen"
      >
        <div class="song-list">
          <div
              v-for="(song, index) in musics"
              :key="song?.id"
              class="song-item"
              :class="{ 'active-song': index === currentSongIndex }"
              @click="playSong(index)"
          >
            <div class="song-details">
              <div class="song-name">{{ song?.name }}</div>
              <div class="song-singer">{{ song?.singer }}</div>
            </div>
            <div class="song-actions">
              <span class="song-duration">{{ song?.duration }}</span>
              <el-button
                  class="favorite-btn el-icon-star-off"
                  :class="{ 'is-favorite': song?.favorite }"
                  @click.stop="toggleFavorite(index)"
              ></el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 隐藏的音频元素 -->
    <audio
        ref="audioPlayer"
        @timeupdate="updateTime"
        @loadedmetadata="updateDuration"
        @ended="onSongEnd"
    ></audio>
  </div>
</template>

<script>
export default {
  name: "MusicPlayer",
  props: {
    musics: {
      type: Array,
      default: () => []
    },
    showFloatMusic: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 播放器状态
      currentSongIndex: 0,
      isPlaying: false,
      playMode: 0, // 0-顺序，1-随机，2-单曲循环
      progress: 0,
      volume: 30,
      isMuted: false,
      lastVolume: 30,
      currentTime: 0,
      duration: 0,
      isPlaylistOpen: false,

      // 拖拽相关
      isDragging: false,
      offsetX: 0,
      offsetY: 0,
      playerLeft: '50%',
      playerTop: '50%'
    };
  },

  computed: {
    currentSong() {
      return this.musics[this.currentSongIndex];
    }
  },

  methods: {
    // 播放指定歌曲
    playSong(index) {
      this.currentSongIndex = index;
      this.$nextTick(() => {
        if (this.currentSong.url) {
          this.$refs.audioPlayer.src = this.currentSong.url
          this.$refs.audioPlayer.currentTime = 0
          this.$refs.audioPlayer.play()
          this.isPlaying = true
        }
      })
    },
    // 切换播放/暂停
    togglePlay() {
      if (this.isPlaying) {
        this.$refs.audioPlayer.pause()
      } else {
        this.$refs.audioPlayer.play()
      }
      this.isPlaying = !this.isPlaying;
    },
    // 上一首
    prevSong() {
      if (this.playMode === 1) { // 随机模式
        const randomIndex = Math.floor(Math.random() * this.musics.length);
        this.currentSongIndex = randomIndex;
      } else {
        this.currentSongIndex = (this.currentSongIndex - 1 + this.musics.length) % this.musics.length;
      }
      this.playSong(this.currentSongIndex);
    },
    // 下一首
    nextSong() {
      if (this.playMode === 1) { // 随机模式
        const randomIndex = Math.floor(Math.random() * this.musics.length);
        this.currentSongIndex = randomIndex;
      } else {
        this.currentSongIndex = (this.currentSongIndex + 1) % this.musics.length;
      }
      this.playSong(this.currentSongIndex);
    },
    // 切换播放列表显示
    togglePlaylist() {
      this.isPlaylistOpen = !this.isPlaylistOpen;
    },
    // 设置播放模式
    setPlayMode(mode) {
      this.playMode = mode;
    },
    // 切换收藏状态
    toggleFavorite(index) {
      this.musics[index].favorite = !this.musics[index].favorite;
    },
    // 处理进度条变化
    handleProgressChange(val) {
      this.$refs.audioPlayer.currentTime = val
    },
    // 处理音量变化
    handleVolumeChange(val) {
      this.volume = val;
      this.$refs.audioPlayer.volume = val / 100;
      // 如果调整音量时处于静音状态，则取消静音
      if (this.isMuted) {
        this.isMuted = false;
      }
    },
    // 切换静音
    toggleMute() {
      if (this.isMuted) { // 当前静音，需要恢复音量
        this.$refs.audioPlayer.volume = this.lastVolume / 100;
        this.volume = this.lastVolume;
        this.isMuted = false;
      } else { // 当前有音量，需要静音
        this.lastVolume = this.volume;
        this.$refs.audioPlayer.volume = 0;
        this.volume = 0;
        this.isMuted = true;
      }
    },
    // 切换循环模式
    toggleRepeat() {
      this.playMode = (this.playMode + 1) % 3;
    },
    formatTime(seconds) {
      if (!seconds) return '0:00'
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      return `${mins}:${secs.toString().padStart(2, '0')}`
    },
    updateTime() {
      this.currentTime = this.$refs.audioPlayer.currentTime
    },
    updateDuration() {
      this.duration = this.$refs.audioPlayer.duration
    },
    onSongEnd() {
      if (this.playMode === 2) {
        this.$refs.audioPlayer.currentTime = 0
        this.$refs.audioPlayer.play()
      } else {
        this.nextSong()
      }
    }
  },

  mounted() {
    // 设置初始音量
    if (this.$refs.audioPlayer) {
      this.$refs.audioPlayer.volume = this.volume / 100;
    }
  },
  watch: {
    showFloatMusic(val) {
      console.log("showFloatMusic:", val);
      if (val) {
        this.$nextTick(() => {
          if (this.musics.length > 0) {
            this.$refs.audioPlayer.src = this.musics[this.currentSongIndex].url;
          }
        })
      }
    }
  },
};
</script>

<style scoped lang="scss">
#music-player {
  width: 320px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  background: linear-gradient(135deg, #80d4ff 0%, #99f2d2 100%);
  transition: all 0.3s ease;
  z-index: 9999;
}

/* 拖拽区域样式 */
.drag-handle {
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: move;
}

.player-title {
  color: #2a3b4c;
  font-weight: bold;
}

.drag-icon {
  color: #2a3b4c;
  opacity: 0.7;
}

/* 控制区域样式 */
.control-panel {
  padding: 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 专辑封面样式 */
.album-container {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.album-wrapper {
  width: 192px;
  height: 192px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  position: relative;
}

.album-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-spinner {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4px solid transparent;
  border-top-color: rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}

.album-spinner.is-playing {
  animation: spin 3s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 歌曲信息样式 */
.song-info {
  text-align: center;
  margin-bottom: 16px;
}

.song-title {
  color: #2a3b4c;
  font-weight: bold;
  font-size: 18px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist {
  color: rgba(42, 59, 76, 0.8);
  font-size: 14px;
}

/* 进度条样式 */
.progress-container {
  margin-bottom: 8px;
}

.time-display {
  display: flex;
  justify-content: space-between;
  color: rgba(42, 59, 76, 0.7);
  font-size: 12px;
  margin-bottom: 4px;
}

.progress-slider {
  width: 100%;
}

/* 控制按钮样式 */
.control-buttons {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.control-btn {
  background: transparent;
  border: none;
  color: #2a3b4c;
  transition: all 0.2s ease;
  font-size: 24px;
}

.control-btn:hover {
  transform: scale(1.1);
  filter: brightness(1.2);
  background: transparent;
  color: #2a3b4c;
}

.play-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: white;
  color: #80d4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.play-btn:hover {
  color: #80d4ff;
  background: white;
}

/* 音量控制样式 */
.volume-control {
  margin-bottom: 8px;
}

.volume-slider {
  width: 100%;
}

/* 播放模式样式 */
.play-modes {
  display: flex;
  justify-content: center;
  gap: 16px;
  color: rgba(42, 59, 76, 0.7);
  font-size: 14px;
}

.mode-btn {
  background: transparent;
  border: none;
  padding: 4px 8px;
  border-radius: 12px;
  color: rgba(42, 59, 76, 0.7);
}

.mode-btn.active {
  background: rgba(255, 255, 255, 0.3);
  color: #2a3b4c;
}

.mode-btn:hover {
  background: transparent;
  color: #2a3b4c;
}

/* 播放列表样式 */
.playlist-panel {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.playlist-header {
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.playlist-title {
  color: #2a3b4c;
  font-weight: 600;
}

.toggle-icon {
  color: #2a3b4c;
}

.playlist-container {
  max-height: 400px;
  overflow-y: auto;
}

.song-list {
  margin: 0;
}

.song-item {
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.song-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(5px);
}

.song-item.active-song {
  background: rgba(255, 255, 255, 0.2);
}

.song-details {
  flex: 1;
  min-width: 0;
}

.song-name {
  color: #2a3b4c;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-singer {
  color: rgba(42, 59, 76, 0.7);
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.song-duration {
  color: rgba(42, 59, 76, 0.7);
  font-size: 14px;
}

.favorite-btn {
  background: transparent;
  border: none;
  color: #2a3b4c;
}

.favorite-btn.is-favorite {
  color: #ff4d4f;
}

.favorite-btn:hover {
  background: transparent;
  color: #ff4d4f;
}

/* 悬浮动画 */
.floating {
  animation: floating 6s ease-in-out infinite;
}

@keyframes floating {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
}

/* 自定义滑块样式 */
::v-deep .el-slider__rail {
  background-color: rgba(255, 255, 255, 0.3);
  height: 4px;
  border-radius: 2px;
}

::v-deep .el-slider__track {
  background-color: white;
  height: 4px;
  border-radius: 2px;
}

::v-deep .el-slider__handle {
  width: 14px;
  height: 14px;
  background-color: white;
  border: none;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);
  transition: all 0.2s ease;
}

::v-deep .el-slider__handle:hover {
  transform: scale(1.2);
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.3);
}
</style>