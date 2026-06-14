<template>
  <el-dialog
    title="动态详情"
    v-dialogDrag
    :visible.sync="dialogVisible"
    width="750px"
    :before-close="handleClose"
    :append-to-body="true"
    class="talk-detail-dialog"
  >
    <div v-loading="loading" class="talk-detail" element-loading-text="加载中...">
      <!-- 用户信息 -->
      <div class="detail-header">
        <head-image :url="detail.avatar" :name="detail.nickName" :id="detail.userId" :size="50"></head-image>
        <div class="header-info">
          <div class="nickname" :class="{ 'character-name': detail.characterId !== null }">
            {{ detail.nickName }}
          </div>
          <div class="meta">
            <span class="time"><i class="el-icon-time"></i> {{ formatDateTime(detail.createTime) }}</span>
            <span v-if="detail.address" class="address"><i class="el-icon-location-outline"></i> {{ detail.address }}</span>
          </div>
          <div class="meta-status" v-if="detail.statusName">
            <el-tag :type="getStatusTagType(detail.status)" effect="dark" size="mini">
              {{ detail.statusName || detail.status }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 动态内容 -->
      <div class="detail-content" v-highlight v-html="$emo.transform(detail.content)"></div>

      <!-- 动态文件 -->
      <div v-if="detail.fileList && detail.fileList.length > 0" class="detail-files">
        <!-- 图片 -->
        <div v-if="imageFiles.length > 0" :class="['file-image-grid', imageGridClass]">
          <div v-for="(file, idx) in imageFiles" :key="'img-' + idx" class="image-item">
            <img :src="file.url" />
          </div>
        </div>

        <!-- 视频 -->
        <div v-if="videoFile" class="file-video" @click="playVideo(videoFile.url, videoFile.coverUrl)">
          <img :src="videoFile.coverUrl" class="video-cover" />
          <span class="play-icon-overlay el-icon-video-play"></span>
        </div>

        <!-- 音频 -->
        <div v-if="audioFile" class="file-audio">
          <vue-audio :audio-source="audioFile.url"></vue-audio>
        </div>
      </div>

      <!-- 点赞 -->
      <div v-if="detail.talkStarVOS && detail.talkStarVOS.length" class="detail-likes">
        <svg class="icon svg-icon" aria-hidden="true">
          <use xlink:href="#icon-dianzan1"></use>
        </svg>
        <span v-for="(star, idx) in detail.talkStarVOS" :key="'star-' + idx" class="star-user">
          {{ star.nickname }}<span v-if="idx < detail.talkStarVOS.length - 1">，</span>
        </span>
      </div>

      <!-- 评论 -->
      <div v-if="detail.talkCommentVOS && detail.talkCommentVOS.length" class="detail-comments">
        <div class="comments-title">评论 ({{ detail.talkCommentVOS.length }})</div>
        <div v-for="(comment, idx) in detail.talkCommentVOS" :key="'comment-' + idx" class="comment-item">
          <head-image class="comment-avatar" :url="comment.userAvatar" :name="comment.userNickname" :size="28" />
          <div class="comment-body">
            <span class="comment-username" :class="{ 'character-name': comment.characterId !== null }">
              {{ comment.userNickname }}
            </span>
            <template v-if="comment.replyCommentId">
              <span class="comment-reply-label"> 回复 </span>
              <head-image class="comment-reply-avatar" :url="comment.replyUserAvatar" :name="comment.replyUserNickname" :size="28" />
              <span class="comment-username" :class="{ 'character-name': comment.replyUserCharacterId !== null }">
                {{ comment.replyUserNickname }}
              </span>
            </template>
            <span>：</span>
            <!-- 文字评论 -->
            <span v-if="comment.type === 0" class="comment-text" v-html="$emo.transform(comment.content)"></span>
            <!-- 语音台词评论 -->
            <span v-if="comment.type === 5" class="comment-text">
              <span class="word">{{ JSON.parse(comment.content).word }}</span>
              <span class="icon iconfont icon-xitongxiaoxi voice-icon" @click.stop="playVoice(JSON.parse(comment.content))"></span>
            </span>
            <!-- 图片评论 -->
            <span v-if="comment.type === 1" class="comment-text">
              <img :src="JSON.parse(comment.content).originUrl" />
            </span>
          </div>
        </div>
      </div>
    </div>
    <video-play ref="videoPlay" :videoUrl="videoUrl" :posterUrl="posterUrl" @close="closeVideoPlay"></video-play>
  </el-dialog>
</template>

<script>
import HeadImage from "@/components/common/HeadImage";
import VideoPlay from "@/components/common/VideoPlay";

export default {
  name: "TalkDetail",
  components: { HeadImage, VideoPlay },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    talkId: {
      type: Number,
      default: null
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      detail: {},
      videoUrl: '',
      posterUrl: '',
      audio: null,
      audioSrc: ''
    };
  },
  computed: {
    imageFiles() {
      if (!this.detail.fileList) return [];
      return this.detail.fileList.filter(f => f.fileType === 1);
    },
    videoFile() {
      if (!this.detail.fileList) return null;
      return this.detail.fileList.find(f => f.fileType === 2) || null;
    },
    audioFile() {
      if (!this.detail.fileList) return null;
      return this.detail.fileList.find(f => f.fileType === 3) || null;
    },
    imageGridClass() {
      const count = this.imageFiles.length;
      if (count === 0) return '';
      if (count === 1) return 'cols-1';
      if (count === 2 || count === 4) return 'cols-2';
      return 'cols-3';
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val;
      if (val && this.talkId) {
        this.fetchDetail();
      }
    },
    dialogVisible(val) {
      if (!val) {
        this.$emit('update:visible', false);
      }
    }
  },
  methods: {
    fetchDetail() {
      this.loading = true;
      this.detail = {};
      this.$http({
        url: `/talk/getTalkDetailById/${this.talkId}`,
        method: 'get'
      }).then((data) => {
        this.detail = data || {};
      }).catch(() => {
        this.$message.error('获取动态详情失败');
      }).finally(() => {
        this.loading = false;
      });
    },

    handleClose() {
      this.dialogVisible = false;
      this.stopAudio();
    },

    formatDateTime(date) {
      if (!date) return '';
      if (typeof date === 'string') {
        date = new Date(date);
      }
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },

    getStatusTagType(status) {
      const map = {
        '0': 'info',
        '1': 'warning',
        '2': 'success',
        '3': 'danger'
      };
      return map[status] || 'info';
    },

    previewImage(index) {
      const urls = this.imageFiles.map(f => f.url);
      this.$imagePreview({
        images: urls,
        index: index
      });
    },

    playVideo(videoUrl, coverUrl) {
      this.videoUrl = videoUrl;
      this.posterUrl = coverUrl;
      this.$refs.videoPlay.onPlayVideo();
    },

    closeVideoPlay() {
      this.videoUrl = '';
      this.posterUrl = '';
    },

    playVoice(word) {
      this.stopAudio();
      this.audio = new Audio();
      this.audioSrc = word.voice;
      this.audio.src = word.voice;

      const handleEnded = () => {
        this.stopAudio();
      };

      this.audio.addEventListener('ended', handleEnded);
      this.audio.play().catch(err => {
        console.error('音频播放失败:', err);
        this.stopAudio();
      });
    },

    stopAudio() {
      if (this.audio) {
        this.audio.pause();
        this.audio = null;
        this.audioSrc = '';
      }
    }
  },
  beforeDestroy() {
    this.stopAudio();
  }
};
</script>

<style scoped lang="scss">
.talk-detail-dialog {
  ::v-deep .el-dialog {
    border-radius: 12px;
    overflow: hidden;
  }

  ::v-deep .el-dialog__body {
    padding: 20px 24px;
    max-height: 70vh;
    overflow-y: auto;
  }
}

.talk-detail {
  .detail-header {
    display: flex;
    align-items: flex-start;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 16px;

    .header-info {
      margin-left: 12px;
      flex: 1;

      .nickname {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }

      .character-name {
        color: #f56c6c;
      }

      .meta {
        font-size: 13px;
        color: #909399;
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        align-items: center;

        i {
          margin-right: 2px;
        }
      }

      .meta-status {
        margin-top: 6px;
      }
    }
  }

  .detail-content {
    font-size: 15px;
    line-height: 1.8;
    color: #303133;
    white-space: pre-wrap;
    word-break: break-word;
    margin-bottom: 16px;

    ::v-deep img {
      vertical-align: middle;
    }
  }

  .detail-files {
    margin-bottom: 16px;
  }

  .file-image-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .image-item {
      overflow: hidden;
      border-radius: 8px;
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: scale(1.02);
      }

      img {
        display: block;
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    &.cols-1 {
      .image-item {
        max-width: 400px;
        width: 100%;
        border-radius: 10px;

        img {
          width: 100%;
          height: auto;
          object-fit: contain;
          max-height: 400px;
        }
      }
    }

    &.cols-2 {
      .image-item {
        width: calc(50% - 4px);
        aspect-ratio: 1 / 1;

        img {
          height: 100%;
          object-fit: cover;
        }
      }
    }

    &.cols-3 {
      .image-item {
        width: calc(33.333% - 6px);
        aspect-ratio: 1 / 1;

        img {
          height: 100%;
          object-fit: cover;
        }
      }
    }
  }

  .file-video {
    position: relative;
    display: inline-block;
    cursor: pointer;
    border-radius: 8px;
    overflow: hidden;
    max-width: 400px;
    width: 100%;

    .video-cover {
      width: 100%;
      display: block;
      aspect-ratio: 16 / 9;
      object-fit: cover;
    }

    .play-icon-overlay {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 60px;
      color: #fff;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
      transition: transform 0.2s, font-size 0.2s;
    }

    &:hover .play-icon-overlay {
      font-size: 70px;
    }
  }

  .file-audio {
    max-width: 500px;
    margin-top: 8px;
  }

  .detail-likes {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-top: 1px solid #f0f0f0;

    .svg-icon {
      width: 18px;
      height: 18px;
      margin-right: 6px;
      flex-shrink: 0;
    }

    .star-user {
      font-size: 14px;
      color: #576b95;
    }
  }

  .detail-comments {
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;

    .comments-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 12px;
    }

    .comment-item {
      display: flex;
      margin-bottom: 10px;

      .comment-avatar {
        flex-shrink: 0;
        margin-right: 8px;
      }

      .comment-reply-avatar {
        display: inline-flex;
        vertical-align: middle;
      }

      .comment-body {
        flex: 1;
        font-size: 14px;
        line-height: 1.6;
        color: #606266;

        .comment-username {
          color: #576b95;
          font-weight: 500;
          vertical-align: middle;

          &.character-name {
            color: #f56c6c;
          }
        }

        .comment-reply-label {
          color: #909399;
          vertical-align: middle;
        }

        .comment-text {
          word-break: break-word;

          img {
            max-width: 180px;
            max-height: 180px;
            border-radius: 6px;
            object-fit: cover;
            margin-top: 4px;
          }

          .word {
            cursor: pointer;
          }

          .voice-icon {
            color: orange;
            margin-left: 4px;
            cursor: pointer;
            font-size: 16px;
          }
        }
      }
    }
  }
}
</style>
