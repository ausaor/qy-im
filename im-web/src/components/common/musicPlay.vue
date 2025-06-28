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
        @closed="handleClose"
    >
      <div class="player-container">
        <!-- 头部 -->
        <div class="player-header">
          <div class="logo">
            <i class="el-icon-headset"></i>
            <span>QY MUSIC</span>
          </div>
          <div v-if="showUpload" class="upload-btn" @click="showUploadDialog()">
            <i class="el-icon-upload2"></i>
          </div>
<!--          <el-button
              type="text"
              @click="drawerVisible = false"
              class="close-btn"
          >
            <i class="el-icon-close"></i>
          </el-button>-->
        </div>

        <!-- 当前播放歌曲 -->
        <div class="current-song">
          <div class="song-cover">
            <img v-if="currentSong.cover" :src="currentSong.cover" :alt="currentSong.name" />
            <svg v-else class="icon svg-icon" :class="{'rotate-active': isPlaying}" aria-hidden="true">
              <use xlink:href="#icon-music"></use>
            </svg>
          </div>
          <div class="song-info">
            <div class="song-name">
              <h3>{{ currentSong.name }}</h3>
              <div class="like-overlay">
                <el-button
                    type="text"
                    @click="toggleLike(currentSong)"
                    class="like-btn"
                    :class="{ liked: currentSong.liked }"
                >
                  <i class="el-icon-star-on"></i>
                </el-button>
              </div>
            </div>
            <p>{{ currentSong.singer }}</p>
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
                <img v-if="song.cover" :src="song.cover" :alt="song.name" />
                <svg v-else class="icon svg-icon" aria-hidden="true">
                  <use xlink:href="#icon-music"></use>
                </svg>
                <div class="play-indicator" v-if="currentSongIndex === index && isPlaying">
                  <i class="el-icon-video-play"></i>
                </div>
              </div>

              <div class="song-item-info">
                <div class="song-name">{{ song.name }}</div>
                <div class="song-artist">{{ song.singer }}</div>
              </div>

              <div class="song-item-actions">
                <span class="duration">{{ formatTime(song.duration) }}</span>
                <span class="play-count" v-if="song.playCount > 0">
                  <i class="el-icon-video-play"></i>
                  <span>{{ song.playCount }}</span>
                </span>
                <el-button
                    type="text"
                    @click.stop="toggleLike(song)"
                    class="like-btn-small"
                    :class="{ liked: song.liked }"
                >
                  <i class="el-icon-star-on"></i>
                  <span v-if="song.likeCount > 0">{{ song.likeCount }}</span>
                </el-button>
                <el-popconfirm
                    v-if="song.isOwner"
                    confirm-button-text='确认'
                    cancel-button-text='取消'
                    icon="el-icon-info"
                    icon-color="red"
                    title="确认删除当前歌曲吗？"
                    @confirm="delSong(song)"
                >
                  <el-button slot="reference" size="mini" type="text" @click.stop class="delete-btn-small">删除</el-button>
                </el-popconfirm>
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
      <music-upload ref="musicUploadRef" :category="category" :group-id="groupId" :region-code="regionCode" @add="addMusic"></music-upload>
    </el-drawer>
</template>

<script>
import MusicUpload from "@components/common/musicUpload.vue";

export default {
  name: 'MusicPlayer',
  props: {
    category: {
      type: String,
      default: true
    },
    section: {
      type: String,
      default: true
    },
    showUpload: {
      type: Boolean,
      default: true
    },
    friendId: {
      type: Number,
      default: null
    },
    groupId: {
      type: Number,
      default: null
    },
    regionCode: {
      type: String,
      default: null
    }
  },
  components: {
    MusicUpload
  },
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
      playlist: []
    }
  },

  computed: {
    currentSong() {
      return this.playlist[this.currentSongIndex] || {};
    },

    playModeIcon() {
      return this.playMode === 'random' ? 'el-icon-sort' : 'el-icon-menu'
    },

    playModeText() {
      return this.playMode === 'random' ? '随机播放' : '顺序播放'
    }
  },
  watch: {
    drawerVisible(newValue) {
      if (newValue) {
        this.listMusic();
      }
    }
  },

  methods: {
    handleClose() {
      if (this.isPlaying) {
        this.$refs.audioPlayer.pause()
        this.isPlaying = false
      }
    },
    show() {
      this.drawerVisible = true
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
        if (this.currentSong.url) {
          this.$refs.audioPlayer.src = this.currentSong.url
          this.$refs.audioPlayer.play()
          this.isPlaying = true
          this.increasePlayCount();
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

    toggleLike(music) {
      let params = {
        musicId: music.id
      }
      let url;
      if (music.liked) {
        url = '/music/cancelLike'
      } else {
        url = '/music/like'
      }
      this.$http({
        url: url,
        method: 'POST',
        data: params
      }).then(res => {
        music.liked = !music.liked;
        music.likeCount += music.liked ? 1 : -1
        this.$message.success('操作成功');
      })
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
    },
    showUploadDialog() {
      this.$refs.musicUploadRef.open();
    },
    addMusic(data) {
      this.playlist.push(data);
    },
    listMusic() {
      let params = {
        category: this.category,
        section: this.section,
        friendId: this.friendId,
        groupId: this.groupId,
        regionCode: this.regionCode,
      }
      this.$http({
        url: `/music/list`,
        method: "post",
        data: params
      }).then((data) => {
        this.playlist = data
        this.$nextTick(() => {
          if (this.playlist.length > 0) {
            this.$refs.audioPlayer.src = this.playlist[0].url;
          }
        })
      })
    },
    delSong(song) {
      let params = {
        id: song.id
      }
      this.$http({
        url: `/music/delete`,
        method: "delete",
        data: params
      }).then((data) => {
        this.$message.success("删除成功");
        this.playlist = this.playlist.filter(item => item.id !== song.id)
      })
    },
    increasePlayCount() {
      this.$http({
        url: `/music/increasePlayCount/${this.currentSong.id}`,
        method: "post",
      }).then((data) => {
      })
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

.upload-btn {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #00bcd4;
  cursor: pointer;
}

.close-btn {
  color: #666;
  font-size: 18px;
}

/* 当前歌曲区域 */
/* 定义旋转动画 */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

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

.song-cover .icon {
  width: 80px;
  height: 80px;
}

.rotate-active {
  animation: spin 5s linear infinite; /* 持续2秒，匀速，无限循环 */

  /* 确保元素以自身中心旋转（默认行为，无需额外设置） */
  transform-origin: center; /* 可选：明确指定旋转中心 */

  /* 可选：添加平滑旋转效果 */
  backface-visibility: hidden;
}

.like-overlay {
  font-size: 18px;
}

.like-btn {
  color: white;
  font-size: 18px;
}

.like-btn.liked {
  color: #ff4757;
}

.song-info h3 {
  margin: 0;
  font-size: 18px;
}

.song-info .song-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.song-info p {
  margin: 5px 0 0 0;
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

.song-item-cover .icon {
  width: 45px;
  height: 45px;
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

.play-count {
  font-size: 18px;
  opacity: 0.7;
  min-width: 35px;
  color: #666;
}

.play-count span {
  font-size: 18px;
  margin-left: 2px;
}

.like-btn-small {
  padding: 4px;
  font-size: 18px;
  color: #666;
}

.like-btn-small.liked {
  color: #ff4757;
}

.like-btn-small span {
  font-size: 18px;
  margin-left: 2px;
}

.playlist-item.active .like-btn-small {
  color: rgba(255, 255, 255, 0.8);
}

.playlist-item.active .like-btn-small.liked {
  color: #ff4757;
}

.delete-btn-small {
  font-size: 12px;
  margin-left: 2px;
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