<template>
  <el-dialog
      title="视频播放"
      v-dialogDrag
      :visible.sync="dialogVisible"
      width="40%"
      :modal="false"
      :before-close="handleClose"
      class="tech-video-dialog">
    <div class="video-container">
<!--      <video-player
          ref="videoPlayer"
          :options="playerOptions"
          @ready="onPlayerReady"
      />-->
        <video ref="videoPlayer" class="video-obj" controls="controls" preload="none" :src="videoUrl" :poster="posterUrl"></video>
    </div>
  </el-dialog>
</template>

<script>

export default {
  name: "VideoPlay",
  components: {},
  props: {
    videoUrl: {
      type: String,
      default: ''
    },
    posterUrl: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      dialogVisible: false,
      // 视频配置项
      playerOptions: {
        autoplay: false,
        muted: true,
        preload: 'auto',
        language: 'en',
        // 屏幕自适应
        fluid: true,
        // 倍速
        playbackRates: [0.7, 1.0, 1.5, 2.0],
        // 视频
        sources: [
          {
            type: 'video/mp4',
            src: ''
          }
        ],
        // 封面
        poster: '',
        notSupportedMessage: '此视频暂无法播放，请稍后再试',
        // 控制界面
        controlBar: {
          // 剩余时间
          remainingTimeDisplay: false,
          // 全屏按钮
          fullscreenToggle: true,
          // 当前时间
          currentTimeDisplay: true,
          // 声音控制键
          volumeControl: false,
          // 暂停和播放键
          playToggle: true,
          // 进度条
          progressControl: true
        }
      }
    }
  },
  watch: {
    dialogVisible(val) {
      if (val) {
        // 弹窗显示时自动播放视频
        this.$nextTick(() => {
          const video = this.$refs.videoPlayer;
          if (video) {
            // 设置音量为50%
            video.volume = 0.5;
            // 尝试自动播放
            const playPromise = video.play();
            if (playPromise !== undefined) {
              playPromise.catch(error => {
                console.log('自动播放失败:', error);
              });
            }
          }
        });
      }
    }
  },
  methods: {
    handleClose(done) {
      this.dialogVisible = false
      this.playerOptions.sources[0].src = '';
      this.playerOptions.poster = '';
      this.$emit('close')
    },
    onPlayVideo() {
      this.playerOptions.sources[0].src = this.videoUrl;
      this.playerOptions.poster = this.posterUrl;
      this.dialogVisible = true
    },
    onPlayerReady(player) {
      console.log('播放器已加载', player)
    }
  },
}
</script>

<style scoped>
.tech-video-dialog ::v-deep .el-dialog {
  background: linear-gradient(135deg, rgba(30, 30, 46, 0.9), rgba(15, 15, 30, 0.95));
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3), 
              0 0 0 1px rgba(255, 255, 255, 0.05),
              0 0 20px rgba(64, 158, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.tech-video-dialog ::v-deep .el-dialog__header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.tech-video-dialog ::v-deep .el-dialog__title {
  color: #fff;
  font-weight: 600;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

.tech-video-dialog ::v-deep .el-dialog__headerbtn .el-dialog__close {
  color: #fff;
  font-weight: bold;
  font-size: 24px;
}

.tech-video-dialog ::v-deep .el-dialog__headerbtn:focus .el-dialog__close, 
.tech-video-dialog ::v-deep .el-dialog__headerbtn:hover .el-dialog__close {
  color: #409eff;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.7);
}

.tech-video-dialog ::v-deep .el-dialog__body {
  padding: 20px;
}

.video-container {
  position: relative;
  width: 100%; /* 容器宽度自适应父级 */
  height: 0; /* 高度由 padding 撇开 */
  padding-top: 56.25%;   /* 16:9 比例（9/16=56.25%） */
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  background: linear-gradient(45deg, #0f0f1e, #1e1e2e);
  border: 1px solid rgba(255, 255, 255, 0.05);

  .video-obj {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: contain; /* 保持比例填充，两侧留黑边 */
    /* object-fit: cover;   裁剪填充，不留黑边 */
    background: #000;
  }
}
</style>