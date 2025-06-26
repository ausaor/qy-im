<template>
    <!-- 音乐播放器抽屉 -->
    <el-drawer
        title=""
        :visible.sync="drawerVisible"
        direction="rtl"
        size="40%"
        :with-header="false"
        :show-close="false"
        class="music-drawer"
    >
      <div class="player-container">
        <!-- 头部 -->
        <div class="player-header">
          <div class="logo">
            <i class="el-icon-headset"></i>
            <span>QY MUSIC</span>
          </div>
          <el-button
              type="text"
              @click="drawerVisible = false"
              class="close-btn"
          >
            <i class="el-icon-close"></i>
          </el-button>
        </div>

        <!-- 当前播放歌曲 -->
        <div class="current-song">
          <div class="song-cover">
            <img :src="currentSong.cover" :alt="currentSong.name" />
            <div class="like-overlay">
              <el-button
                  type="text"
                  @click="toggleLike(currentSong.id)"
                  class="like-btn"
                  :class="{ liked: currentSong.liked }"
              >
                <i class="el-icon-star-off"></i>
              </el-button>
            </div>
          </div>
          <div class="song-info">
            <h3>{{ currentSong.name }}</h3>
            <p>{{ currentSong.artist }}</p>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-section">
          <span class="time">{{ formatTime(currentTime) }}</span>
          <el-slider
              v-model="currentTime"
              :max="duration"
              @change="seekTo"
              class="progress-slider"
          ></el-slider>
          <span class="time">{{ formatTime(duration) }}</span>
        </div>

        <!-- 控制按钮 -->
        <div class="controls">
          <div class="volume-control">
            <i class="el-icon-voice"></i>
            <el-slider
                v-model="volume"
                :max="100"
                @change="changeVolume"
                class="volume-slider"
            ></el-slider>
          </div>

          <div class="playback-controls">
            <el-button
                type="text"
                @click="togglePlayMode"
                class="control-btn"
                :title="playModeText"
            >
              <i :class="playModeIcon"></i>
            </el-button>

            <el-button
                type="text"
                @click="previousSong"
                class="control-btn"
            >
              <i class="el-icon-d-arrow-left"></i>
            </el-button>

            <el-button
                type="primary"
                @click="togglePlay"
                class="play-btn"
                circle
            >
              <i :class="isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"></i>
            </el-button>

            <el-button
                type="text"
                @click="nextSong"
                class="control-btn"
            >
              <i class="el-icon-d-arrow-right"></i>
            </el-button>

            <el-button
                type="text"
                @click="toggleRepeat"
                class="control-btn"
                :class="{ active: isRepeat }"
            >
              <i class="el-icon-refresh"></i>
            </el-button>
          </div>
        </div>

        <!-- 播放列表 -->
        <div class="playlist-section">
          <div class="playlist-header">
            <h4>播放列表</h4>
            <span class="song-count">{{ playlist.length }}首歌曲</span>
          </div>

          <div class="playlist">
            <div
                v-for="(song, index) in playlist"
                :key="song.id"
                class="playlist-item"
                :class="{ active: currentSongIndex === index }"
                @click="playSong(index)"
            >
              <div class="song-item-cover">
                <img :src="song.cover" :alt="song.name" />
                <div class="play-indicator" v-if="currentSongIndex === index && isPlaying">
                  <i class="el-icon-video-play"></i>
                </div>
              </div>

              <div class="song-item-info">
                <div class="song-name">{{ song.name }}</div>
                <div class="song-artist">{{ song.artist }}</div>
              </div>

              <div class="song-item-actions">
                <span class="duration">{{ song.duration }}</span>
                <el-button
                    type="text"
                    @click.stop="toggleLike(song.id)"
                    class="like-btn-small"
                    :class="{ liked: song.liked }"
                >
                  <i class="el-icon-star-off"></i>
                  <span v-if="song.likeCount > 0">{{ song.likeCount }}</span>
                </el-button>
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
    </el-drawer>
</template>

<script>
export default {
  name: 'MusicPlayer',
  data() {
    return {
      drawerVisible: false,
      isPlaying: false,
      currentTime: 0,
      duration: 0,
      volume: 50,
      currentSongIndex: 0,
      playMode: 'sequence', // sequence, random
      isRepeat: false,

      playlist: [
        {
          id: 1,
          name: '夏日微风',
          artist: '林晓风',
          duration: '3:45',
          cover: 'http://localhost:8300/image/01de69620c3bda155b59b87bebd0a50b.jpg',
          src: 'http://localhost:8300/audio/0d5fbca7252cc2adfbb7e85eeaa3fef2.mp3', // 实际项目中应该是真实的音频文件路径
          liked: true,
          likeCount: 1024
        },
        {
          id: 2,
          name: '城市星光',
          artist: '陈夜明',
          duration: '4:20',
          cover: 'http://localhost:8300/image/01de69620c3bda155b59b87bebd0a50b.jpg',
          src: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
          liked: true,
          likeCount: 856
        },
        {
          id: 3,
          name: '雨后彩虹',
          artist: '张雨菲',
          duration: '3:15',
          cover: 'http://localhost:8300/image/01de69620c3bda155b59b87bebd0a50b.jpg',
          src: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
          liked: false,
          likeCount: 432
        },
        {
          id: 4,
          name: '山间小径',
          artist: '王远山',
          duration: '5:10',
          cover: 'http://localhost:8300/image/01de69620c3bda155b59b87bebd0a50b.jpg',
          src: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
          liked: false,
          likeCount: 678
        },
        {
          id: 5,
          name: '海边日落',
          artist: '李海燕',
          duration: '4:35',
          cover: 'http://localhost:8300/image/01de69620c3bda155b59b87bebd0a50b.jpg',
          src: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
          liked: true,
          likeCount: 1205
        }
      ]
    }
  },

  computed: {
    currentSong() {
      return this.playlist[this.currentSongIndex] || {};;
    },

    playModeIcon() {
      return this.playMode === 'random' ? 'el-icon-sort' : 'el-icon-menu'
    },

    playModeText() {
      return this.playMode === 'random' ? '随机播放' : '顺序播放'
    }
  },

  methods: {
    show() {
      this.drawerVisible = true
      this.$nextTick(() => {
        this.$refs.audioPlayer.src = this.playlist[this.currentSongIndex].src;
      })
    },
    togglePlay() {
      if (this.isPlaying) {
        this.$refs.audioPlayer.pause()
      } else {
        this.$refs.audioPlayer.play()
      }
      this.isPlaying = !this.isPlaying
    },

    playSong(index) {
      this.currentSongIndex = index
      this.$nextTick(() => {
        if (this.currentSong.src) {
          this.$refs.audioPlayer.src = this.currentSong.src
          this.$refs.audioPlayer.play()
          this.isPlaying = true
        }
      })
    },

    previousSong() {
      if (this.playMode === 'random') {
        this.currentSongIndex = Math.floor(Math.random() * this.playlist.length)
      } else {
        this.currentSongIndex = this.currentSongIndex > 0
            ? this.currentSongIndex - 1
            : this.playlist.length - 1
      }
      this.playSong(this.currentSongIndex)
    },

    nextSong() {
      if (this.playMode === 'random') {
        this.currentSongIndex = Math.floor(Math.random() * this.playlist.length)
      } else {
        this.currentSongIndex = this.currentSongIndex < this.playlist.length - 1
            ? this.currentSongIndex + 1
            : 0
      }
      this.playSong(this.currentSongIndex)
    },

    onSongEnd() {
      if (this.isRepeat) {
        this.$refs.audioPlayer.currentTime = 0
        this.$refs.audioPlayer.play()
      } else {
        this.nextSong()
      }
    },

    togglePlayMode() {
      this.playMode = this.playMode === 'sequence' ? 'random' : 'sequence'
      this.$message.success(`已切换到${this.playModeText}`)
    },

    toggleRepeat() {
      this.isRepeat = !this.isRepeat
      this.$message.success(this.isRepeat ? '已开启单曲循环' : '已关闭单曲循环')
    },

    toggleLike(songId) {
      const song = this.playlist.find(s => s.id === songId)
      if (song) {
        song.liked = !song.liked
        song.likeCount += song.liked ? 1 : -1
      }
    },

    seekTo(value) {
      this.$refs.audioPlayer.currentTime = value
    },

    changeVolume(value) {
      this.$refs.audioPlayer.volume = value / 100
    },

    updateTime() {
      this.currentTime = this.$refs.audioPlayer.currentTime
    },

    updateDuration() {
      this.duration = this.$refs.audioPlayer.duration
    },

    formatTime(seconds) {
      if (!seconds) return '0:00'
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      return `${mins}:${secs.toString().padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.music-player {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.main-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}

.open-player-btn {
  padding: 15px 30px;
  font-size: 16px;
  border-radius: 25px;
  background: linear-gradient(45deg, #00bcd4, #2196f3);
  border: none;
  box-shadow: 0 4px 15px rgba(0, 188, 212, 0.3);
}

.open-player-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 188, 212, 0.4);
}

/* 抽屉样式 */
.music-drawer >>> .el-drawer {
  background: linear-gradient(180deg, #ffffff 0%, #f0f9ff 100%);
}

.player-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.player-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #00bcd4;
}

.logo i {
  margin-right: 8px;
  font-size: 24px;
}

.close-btn {
  color: #666;
  font-size: 18px;
}

/* 当前歌曲区域 */
.current-song {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  border-radius: 15px;
  color: white;
}

.song-cover {
  position: relative;
  margin-right: 15px;
}

.song-cover img {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  object-fit: cover;
}

.like-overlay {
  position: absolute;
  top: 5px;
  right: 5px;
}

.like-btn {
  color: white;
  font-size: 18px;
}

.like-btn.liked {
  color: #ff4757;
}

.song-info h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.song-info p {
  margin: 0;
  opacity: 0.8;
  font-size: 14px;
}

/* 进度条区域 */
.progress-section {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.time {
  font-size: 12px;
  color: #666;
  min-width: 35px;
}

.progress-slider {
  flex: 1;
  margin: 0 15px;
}

.progress-slider >>> .el-slider__runway {
  background-color: #e1f5fe;
}

.progress-slider >>> .el-slider__bar {
  background: linear-gradient(90deg, #00bcd4, #2196f3);
}

/* 控制区域 */
.controls {
  margin-bottom: 30px;
}

.volume-control {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.volume-control i {
  color: #00bcd4;
  margin-right: 10px;
}

.volume-slider {
  flex: 1;
  max-width: 120px;
}

.volume-slider >>> .el-slider__runway {
  background-color: #e1f5fe;
}

.volume-slider >>> .el-slider__bar {
  background: linear-gradient(90deg, #00bcd4, #2196f3);
}

.playback-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
}

.control-btn {
  color: #666;
  font-size: 20px;
  padding: 8px;
}

.control-btn:hover,
.control-btn.active {
  color: #00bcd4;
}

.play-btn {
  width: 50px;
  height: 50px;
  background: linear-gradient(45deg, #00bcd4, #2196f3);
  border: none;
  font-size: 20px;
}

.play-btn:hover {
  transform: scale(1.05);
}

/* 播放列表区域 */
.playlist-section {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e1f5fe;
}

.playlist-header h4 {
  margin: 0;
  color: #333;
}

.song-count {
  color: #666;
  font-size: 12px;
}

.playlist {
  flex: 1;
  overflow-y: auto;
}

.playlist-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 8px;
}

.playlist-item:hover {
  background: linear-gradient(90deg, #e1f5fe, #f0f9ff);
}

.playlist-item.active {
  background: linear-gradient(90deg, #00bcd4, #2196f3);
  color: white;
}

.song-item-cover {
  position: relative;
  margin-right: 12px;
}

.song-item-cover img {
  width: 45px;
  height: 45px;
  border-radius: 6px;
  object-fit: cover;
}

.play-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 16px;
}

.song-item-info {
  flex: 1;
  min-width: 0;
}

.song-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-artist {
  font-size: 12px;
  opacity: 0.7;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.duration {
  font-size: 12px;
  opacity: 0.7;
  min-width: 35px;
}

.like-btn-small {
  padding: 4px;
  font-size: 14px;
  color: #666;
}

.like-btn-small.liked {
  color: #ff4757;
}

.like-btn-small span {
  font-size: 10px;
  margin-left: 2px;
}

.playlist-item.active .like-btn-small {
  color: rgba(255, 255, 255, 0.8);
}

.playlist-item.active .like-btn-small.liked {
  color: #ff4757;
}

/* 滚动条样式 */
.playlist::-webkit-scrollbar {
  width: 4px;
}

.playlist::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.playlist::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #00bcd4, #2196f3);
  border-radius: 2px;
}

.playlist::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #0097a7, #1976d2);
}
</style>