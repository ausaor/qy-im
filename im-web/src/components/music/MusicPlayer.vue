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
              :src="currentSong.album"
              alt="专辑封面"
              class="album-cover"
          >
          <div class="album-spinner" :class="{ 'is-playing': isPlaying }"></div>
        </div>
      </div>

      <!-- 歌曲信息 -->
      <div class="song-info">
        <h3 class="song-title">{{ currentSong.title }}</h3>
        <p class="song-artist">{{ currentSong.artist }}</p>
      </div>

      <!-- 进度条 -->
      <div class="progress-container">
        <div class="time-display">
          <span>{{ currentTime }}</span>
          <span>{{ currentSong.duration }}</span>
        </div>
        <el-slider
            v-model="progress"
            :max="100"
            class="progress-slider"
            @change="handleProgressChange"
        ></el-slider>
      </div>

      <!-- 控制按钮 -->
      <div class="control-buttons">
        <div
            class="control-btn volume-btn el-icon-d-caret"
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
        <el-list class="song-list">
          <el-list-item
              v-for="(song, index) in songs"
              :key="song.id"
              class="song-item"
              :class="{ 'active-song': index === currentSongIndex }"
              @click="playSong(index)"
          >
            <div class="song-details">
              <div class="song-name">{{ song.title }}</div>
              <div class="song-singer">{{ song.artist }}</div>
            </div>
            <div class="song-actions">
              <span class="song-duration">{{ song.duration }}</span>
              <el-button
                  class="favorite-btn el-icon-star-off"
                  :class="{ 'is-favorite': song.favorite }"
                  @click.stop="toggleFavorite(index)"
              ></el-button>
            </div>
          </el-list-item>
        </el-list>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      // 歌曲数据
      songs: [
        {
          id: 1,
          title: "星空下的约定",
          artist: "梦乐团",
          duration: "3:45",
          album: "https://picsum.photos/id/65/300/300",
          favorite: true
        },
        {
          id: 2,
          title: "深海回响",
          artist: "海浪乐队",
          duration: "4:12",
          album: "https://picsum.photos/id/96/300/300",
          favorite: false
        },
        {
          id: 3,
          title: "城市微光",
          artist: "午夜行者",
          duration: "3:28",
          album: "https://picsum.photos/id/24/300/300",
          favorite: true
        },
        {
          id: 4,
          title: "时间旅行者",
          artist: "维度穿越者",
          duration: "5:03",
          album: "https://picsum.photos/id/42/300/300",
          favorite: false
        },
        {
          id: 5,
          title: "电子梦境",
          artist: "数字幽灵",
          duration: "3:57",
          album: "https://picsum.photos/id/60/300/300",
          favorite: false
        }
      ],

      // 播放器状态
      currentSongIndex: 0,
      isPlaying: true,
      playMode: 0, // 0-顺序，1-随机，2-单曲循环
      progress: 35,
      volume: 70,
      isMuted: false,
      lastVolume: 70,
      currentTime: "1:23",
      isPlaylistOpen: true,

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
      return this.songs[this.currentSongIndex];
    }
  },

  methods: {
    // 播放指定歌曲
    playSong(index) {
      this.currentSongIndex = index;
      this.isPlaying = true;
      this.progress = 0;
      this.updateCurrentTime();
    },

    // 切换播放/暂停
    togglePlay() {
      this.isPlaying = !this.isPlaying;
    },

    // 上一首
    prevSong() {
      if (this.playMode === 1) { // 随机模式
        const randomIndex = Math.floor(Math.random() * this.songs.length);
        this.currentSongIndex = randomIndex;
      } else {
        this.currentSongIndex = (this.currentSongIndex - 1 + this.songs.length) % this.songs.length;
      }
      this.playSong(this.currentSongIndex);
    },

    // 下一首
    nextSong() {
      if (this.playMode === 1) { // 随机模式
        const randomIndex = Math.floor(Math.random() * this.songs.length);
        this.currentSongIndex = randomIndex;
      } else {
        this.currentSongIndex = (this.currentSongIndex + 1) % this.songs.length;
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
      this.songs[index].favorite = !this.songs[index].favorite;
    },

    // 处理进度条变化
    handleProgressChange(val) {
      this.progress = val;
      this.updateCurrentTime();
    },

    // 处理音量变化
    handleVolumeChange(val) {
      this.volume = val;
      this.isMuted = val === 0;
    },

    // 切换静音
    toggleMute() {
      if (this.isMuted) {
        this.volume = this.lastVolume;
        this.isMuted = false;
      } else {
        this.lastVolume = this.volume;
        this.volume = 0;
        this.isMuted = true;
      }
    },

    // 切换循环模式
    toggleRepeat() {
      this.playMode = (this.playMode + 1) % 3;
    },

    // 更新当前播放时间
    updateCurrentTime() {
      const totalSeconds = this.parseTimeToSeconds(this.currentSong.duration);
      const currentSeconds = Math.floor(totalSeconds * (this.progress / 100));
      this.currentTime = this.formatTime(currentSeconds);
    },

    // 时间格式化工具
    parseTimeToSeconds(timeStr) {
      const [minutes, seconds] = timeStr.split(':').map(Number);
      return minutes * 60 + seconds;
    },

    formatTime(seconds) {
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${mins}:${secs < 10 ? '0' + secs : secs}`;
    },

    // 更新播放进度
    updateProgress() {
      if (!this.isPlaying) return;

      this.progress += 0.01;
      if (this.progress >= 100) {
        if (this.playMode === 2) { // 单曲循环
          this.progress = 0;
        } else {
          this.nextSong();
          this.progress = 0;
        }
      }

      this.updateCurrentTime();
    }
  },

  mounted() {
    // 启动进度更新定时器
    setInterval(this.updateProgress, 100);
  }
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
  font-size: 36px;
}

.control-btn:hover {
  transform: scale(1.1);
  filter: brightness(1.2);
  background: transparent;
  color: #2a3b4c;
}

.play-btn {
  width: 48px;
  height: 48px;
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