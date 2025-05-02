<template>
  <el-dialog
      title="视频播放"
      :visible.sync="dialogVisible"
      width="40%"
      :modal="false"
      :before-close="handleClose">
    <div class="video-container">
<!--      <video-player
          ref="videoPlayer"
          :options="playerOptions"
          @ready="onPlayerReady"
      />-->
        <video class="video-obj" controls="controls" preload="none" :src="videoUrl" :poster="posterUrl"></video>
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
.video-container {
  position: relative;
  width: 100%; /* 容器宽度自适应父级 */
  height: 0; /* 高度由 padding 撑开 */
  padding-top: 56.25%;   /* 16:9 比例（9/16=56.25%） */

  .video-obj {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: contain; /* 保持比例填充，两侧留黑边 */
    /* object-fit: cover;   裁剪填充，不留黑边 */
  }
}
</style>