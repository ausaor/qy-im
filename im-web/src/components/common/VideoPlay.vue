<template>
  <el-dialog
      title="视频播放"
      v-dialogDrag
      :visible.sync="dialogVisible"
      :width="dialogWidth"
      :modal="false"
      :before-close="handleClose"
      class="tech-video-dialog">
    <div class="video-container" :style="containerStyle">
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
    },
    videoWidth: {
      type: Number,
      default: 0
    },
    videoHeight: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      dialogVisible: false
    }
  },
  computed: {
    containerStyle() {
      if (this.videoWidth > 0 && this.videoHeight > 0) {
        return {
          width: `calc(70vh * ${this.videoWidth} / ${this.videoHeight})`,
          maxWidth: '100%',
          maxHeight: '70vh',
          aspectRatio: `${this.videoWidth} / ${this.videoHeight}`,
          paddingTop: '0',
          margin: '0 auto'
        }
      }
      return {
        width: '640px',
        height: '360px',
        paddingTop: '0',
        maxWidth: '100%'
      }
    },
    dialogWidth() {
      if (this.videoWidth > 0 && this.videoHeight > 0) {
        const ratio = this.videoWidth / this.videoHeight
        // 横屏视频：宽弹窗
        if (ratio >= 1.5) return '60%'
        // 接近方形或略宽
        if (ratio >= 1.0) return '45%'
        // 竖屏视频：窄弹窗
        return '32%'
      }
      // 默认：容纳 640px 容器 + 弹窗内边距
      return '700px'
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
      this.$emit('close')
    },
    onPlayVideo() {
      this.dialogVisible = true
    }
  },
}
</script>

<style scoped>
.tech-video-dialog ::v-deep .el-dialog {
  max-height: 90vh;
  margin-top: 5vh !important;
  background: linear-gradient(135deg, rgba(30, 30, 46, 0.9), rgba(15, 15, 30, 0.95));
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3), 
              0 0 0 1px rgba(255, 255, 255, 0.05),
              0 0 20px rgba(64, 158, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
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
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  background: linear-gradient(45deg, #0f0f1e, #1e1e2e);
  border: 1px solid rgba(255, 255, 255, 0.05);

  .video-obj {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain; /* 保持比例填充，两侧留黑边 */
    /* object-fit: cover;   裁剪填充，不留黑边 */
    background: #000;
  }
}
</style>