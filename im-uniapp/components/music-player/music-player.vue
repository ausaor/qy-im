<template>
  <view class="music-player">
    <!-- 播放器容器，使用渐变色背景 -->
    <view class="player-container">
      <!-- 顶部内容区域 - 封面和播放控制 -->
      <view class="top-content">
        <!-- 封面图片区域，位于左上角 -->
        <view class="cover-wrapper" :class="{ playing: isPlaying }">
          <image :src="coverImgUrl" class="cover-image" mode="aspectFill" :alt="audioName || '音乐封面'"></image>
          <view class="cover-overlay"></view>
        </view>

        <!-- 右侧内容区域 -->
        <view class="player-content">
          <!-- 歌曲信息 -->
          <view class="song-info" v-if="audioName">
            <text class="song-name">{{ audioName }}</text>
          </view>

          <!-- 播放控制按钮 -->
          <view class="control-button">
            <view
                class="play-pause-btn"
                @click="togglePlay"
                :aria-label="isPlaying ? '暂停' : '播放'"
            >
              <uni-icons
                  custom-prefix="iconfont"
                  :type="isPlaying ? 'icon-yinpinzanting' : 'icon-yinpinbofang'"
                  size="48"
                  color="#ffffff"
              ></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部控制区域（进度条和音量控制，分上下两层） -->
      <view class="bottom-controls">
        <!-- 播放进度控制区域 -->
        <view class="progress-control">
          <text class="time current-time">{{ formatTime(currentTime) }}</text>
          <view class="progress-bar">
            <view class="progress-track" @click="handleProgressClick">
              <view class="progress-fill" :style="{ width: progress + '%' }"></view>
              <view
                  class="progress-thumb"
                  :style="{ left: progress + '%' }"
                  @touchstart="onDragStart"
                  @touchmove="onDragging"
                  @touchend="onDragEnd"
              ></view>
            </view>
          </view>
          <text class="time total-time">{{ formatTime(duration) }}</text>
        </view>

        <!-- 音量控制区域 -->
<!--        <view class="volume-control">
          <uni-icons
              :type="isMuted ? 'volume-off' : volumeIcon"
              size="28"
              color="#4a6fa5"
              @click="toggleMute"
              class="volume-icon"
          ></uni-icons>
          <view class="volume-bar">
            <view class="volume-track" @click="handleVolumeClick">
              <view class="volume-fill" :style="{ width: volume + '%' }"></view>
              <view
                  class="volume-thumb"
                  :style="{ left: volume + '%' }"
                  @touchstart="onVolumeDragStart"
                  @touchmove="onVolumeDragging"
                  @touchend="onVolumeDragEnd"
              ></view>
            </view>
          </view>
        </view>-->
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'MusicPlayer',
  props: {
    // 音乐封面图片URL
    coverImgUrl: {
      type: String,
      required: true,
      default: ''
    },
    // 音频文件URL
    audioUrl: {
      type: String,
      required: true,
      default: ''
    },
    // 音频名称（可选）
    audioName: {
      type: String,
      default: ''
    },
    audioDuration: {
      type: Number,
      default: 0
    },
    // 是否循环播放
    loop: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      innerAudioContext: null,
      // 播放状态
      isPlaying: false,
      // 当前播放时间（秒）
      currentTime: 0,
      // 音频总时长（秒）
      duration: 0,
      // 播放进度（百分比）
      progress: 0,
      // 是否正在拖动进度条
      isDragging: false,
      // 音量相关
      volume: 50, // 默认音量50%
      isMuted: false, // 是否静音
      isVolumeDragging: false, // 是否正在拖动音量滑块
      lastVolume: 80 // 静音前的音量，用于恢复
    };
  },
  computed: {
    // 根据音量值返回对应的音量图标
    volumeIcon() {
      if (this.volume > 60) {
        return 'volume-full';
      } else if (this.volume > 0) {
        return 'volume-medium';
      } else {
        return 'volume-off';
      }
    }
  },
  mounted() {
    // 初始化音频配置
    this.initAudioContext();
  },
  onLoad() {

  },
  watch: {
    // 监听音频URL变化
    audioUrl(newVal) {
      this.innerAudioContext.src = newVal;
      this.isPlaying = false;
      this.currentTime = 0;
      this.progress = 0;
    },
    // 监听循环状态变化
    loop(newVal) {
      this.innerAudioContext.loop = newVal;
    }
  },
  beforeDestroy() {
    // 页面销毁时停止播放并释放资源
    this.innerAudioContext.stop();
    this.innerAudioContext.destroy();
  },
  methods: {
    clearAudio() {
      if (this.innerAudioContext) {
        this.innerAudioContext.stop();
        this.innerAudioContext.destroy();
        this.innerAudioContext = null;
      }
    },
    stopAudio() {
      if (this.innerAudioContext) {
        this.innerAudioContext.stop();
        this.isPlaying = false;
      }
    },
    // 设置音频源
    initAudioContext() {
      if (!this.innerAudioContext) {
        this.innerAudioContext = uni.createInnerAudioContext();
        this.innerAudioContext.src = this.audioUrl;
      }
      // 初始化音频上下文
      // 设置初始配置
      this.innerAudioContext.loop = this.loop;
      this.innerAudioContext.volume = this.volume / 100;

      // 监听音频事件
      this.innerAudioContext.onPlay(() => {
        this.isPlaying = true;
      });

      this.innerAudioContext.onPause(() => {
        this.isPlaying = false;
      });

      this.innerAudioContext.onStop(() => {
        this.isPlaying = false;
      });

      this.innerAudioContext.onEnded(() => {
        this.isPlaying = false;
        this.currentTime = 0;
        this.progress = 0;
      });

      this.innerAudioContext.onTimeUpdate(() => {
        if (!this.isDragging) {
          this.currentTime = this.innerAudioContext.currentTime;
          this.progress = (this.currentTime / this.duration) * 100;
        }
      });

      this.innerAudioContext.onCanplay(() => {
        // 音频元数据加载完成，获取总时长
        this.duration = this.innerAudioContext.duration || this.audioDuration || 0;
      });

      this.innerAudioContext.onError((res) => {
        console.error('音频播放错误:', res.errMsg);
        console.error('错误码:', res.errCode);
      });
    },

    // 切换播放/暂停状态
    togglePlay() {
      if (this.isPlaying) {
        this.innerAudioContext.pause();
      } else {
        this.innerAudioContext.play();
      }
    },

    // 格式化时间（秒 -> mm:ss）
    formatTime(seconds) {
      if (isNaN(seconds) || seconds === 0 || !this.duration) return '00:00';

      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);

      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    },

    // 点击进度条调整播放位置
    handleProgressClick(e) {
      if (this.duration === 0 || this.isDragging) return;

      const rect = e.currentTarget.getBoundingClientRect();
      const clickPosition = (e.touches[0].clientX - rect.left) / rect.width;
      this.setPlayPosition(clickPosition);
    },

    // 开始拖动进度条
    onDragStart() {
      this.isDragging = true;
    },

    // 拖动进度条过程中
    onDragging(e) {
      if (this.duration === 0) return;

      const rect = e.currentTarget.parentElement.getBoundingClientRect();
      let position = (e.touches[0].clientX - rect.left) / rect.width;

      // 限制在0-1之间
      position = Math.max(0, Math.min(1, position));

      this.progress = position * 100;
      this.currentTime = position * this.duration;
    },

    // 结束拖动进度条
    onDragEnd() {
      this.isDragging = false;
      this.setPlayPosition(this.progress / 100);
    },

    // 设置播放位置
    setPlayPosition(position) {
      if (this.duration === 0) return;

      const newTime = position * this.duration;
      this.innerAudioContext.seek(newTime);
    },

    // 音量控制相关方法
    // 切换静音状态
    toggleMute() {
      this.isMuted = !this.isMuted;

      if (this.isMuted) {
        // 静音时保存当前音量
        this.lastVolume = this.volume;
        this.setVolume(0);
      } else {
        // 取消静音时恢复之前的音量
        this.setVolume(this.lastVolume);
      }
    },

    // 点击音量条调整音量
    handleVolumeClick(e) {
      if (this.isVolumeDragging) return;

      const rect = e.currentTarget.getBoundingClientRect();
      let volume = (e.touches[0].clientX - rect.left) / rect.width * 100;

      // 限制在0-100之间
      volume = Math.max(0, Math.min(100, volume));

      this.setVolume(volume);
    },

    // 开始拖动音量条
    onVolumeDragStart() {
      this.isVolumeDragging = true;
    },

    // 拖动音量条过程中
    onVolumeDragging(e) {
      const rect = e.currentTarget.parentElement.getBoundingClientRect();
      let volume = (e.touches[0].clientX - rect.left) / rect.width * 100;

      // 限制在0-100之间
      volume = Math.max(0, Math.min(100, volume));

      this.volume = volume;
      this.isMuted = volume === 0;
    },

    // 结束拖动音量条
    onVolumeDragEnd() {
      this.isVolumeDragging = false;
      this.setVolume(this.volume);
    },

    // 设置音量
    setVolume(volume) {
      this.volume = volume;
      this.isMuted = volume === 0;
      this.innerAudioContext.volume = volume / 100;

      // 记录当前音量作为最后一次非静音音量
      if (volume > 0) {
        this.lastVolume = volume;
      }
    }
  }
};
</script>

<style scoped>
/* 样式保持不变，与上一版本相同 */
.music-player {
  width: 100%;
  padding: 20rpx;
  box-sizing: border-box;
}

.player-container {
  background: linear-gradient(135deg, #b3e0ff 0%, #99e6e6 100%);
  border-radius: 30rpx;
  padding: 30rpx;
  box-shadow: 0 15rpx 35rpx rgba(0, 120, 255, 0.15);
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

/* 顶部内容区域 - 封面和播放控制 */
.top-content {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

/* 封面图片样式 - 左上角，180rpx */
.cover-wrapper {
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  overflow: hidden;
  position: relative;
  box-shadow: 0 10rpx 30rpx rgba(0, 120, 255, 0.2);
  flex-shrink: 0; /* 防止封面被压缩 */
}

/* 播放时添加旋转动画 */
.cover-wrapper.playing {
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
  pointer-events: none;
}

/* 右侧内容区域 */
.player-content {
  flex: 1; /* 占据剩余空间 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 15rpx;
}

/* 歌曲信息样式 */
.song-info {
  width: 100%;
  overflow: hidden;
  text-align: center;
  padding: 0 10rpx;
}

.song-name {
  font-size: 32rpx;
  color: #1a365d;
  font-weight: 500;
  text-overflow: ellipsis;
  overflow: hidden;
  display: inline-block;
  max-width: 100%;
}

/* 控制按钮样式 */
.control-button {
  display: flex;
  justify-content: center;
  padding-top: 10rpx;
}

.play-pause-btn {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: #4299e1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0;
  box-shadow: 0 5rpx 15rpx rgba(66, 153, 225, 0.4);
  transition: all 0.2s ease;
}

.play-pause-btn::after {
  border: none;
}

.play-pause-btn:active {
  transform: scale(0.95);
  box-shadow: 0 3rpx 10rpx rgba(66, 153, 225, 0.5);
}

/* 底部控制区域 */
.bottom-controls {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 15rpx;
  margin-top: 10rpx;
}

/* 进度条样式 */
.progress-control {
  width: 100%;
  display: flex;
  align-items: center;
}

.time {
  font-size: 24rpx;
  color: #4a6fa5;
  width: 100rpx;
}

.current-time {
  text-align: left;
  padding-right: 10rpx;
}

.total-time {
  text-align: right;
  padding-left: 10rpx;
}

.progress-bar {
  flex: 1;
  padding: 0 10rpx;
}

.progress-track {
  height: 8rpx;
  background-color: rgba(255, 255, 255, 0.4);
  border-radius: 4rpx;
  position: relative;
}

.progress-fill {
  height: 100%;
  background-color: #4299e1;
  border-radius: 4rpx;
  transition: width 0.1s ease;
}

.progress-thumb {
  position: absolute;
  width: 24rpx;
  height: 24rpx;
  background-color: #ffffff;
  border-radius: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
  z-index: 10;
}

/* 音量控制样式 */
.volume-control {
  width: 100%;
  display: flex;
  align-items: center;
  padding-left: 100rpx; /* 与进度条时间区域对齐 */
}

.volume-icon {
  width: 50rpx;
  height: 50rpx;
  padding-right: 10rpx;
  cursor: pointer;
}

.volume-bar {
  flex: 1;
  padding-right: 100rpx; /* 与进度条右侧时间对齐 */
}

.volume-track {
  height: 8rpx;
  background-color: rgba(255, 255, 255, 0.4);
  border-radius: 4rpx;
  position: relative;
}

.volume-fill {
  height: 100%;
  background-color: #4299e1;
  border-radius: 4rpx;
  transition: width 0.1s ease;
}

.volume-thumb {
  position: absolute;
  width: 20rpx;
  height: 20rpx;
  background-color: #ffffff;
  border-radius: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
  z-index: 10;
}
</style>
