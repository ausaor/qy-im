<template>
  <div class="video-upload">
    <div v-if="!videoUrl" class="upload-area">
      <el-upload
        class="video-uploader"
        action="#"
        :http-request="onFileUpload"
        :show-file-list="false"
        :before-upload="beforeUpload"
        accept="video/*"
      >
        <div class="upload-trigger">
          <i class="el-icon-video-camera"></i>
          <div class="upload-text">
            <span class="upload-title">点击上传视频</span>
            <span class="upload-tip">支持 MP4/AVI/MOV 格式</span>
          </div>
        </div>
      </el-upload>
    </div>
    <div v-else class="video-preview">
      <video :src="videoUrl" controls class="preview-video"></video>
      <div class="video-actions">
        <el-button type="danger" size="small" icon="el-icon-delete" @click="handleRemove" circle></el-button>
        <span class="video-name">{{ videoName }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "VideoUpload",
  props: {
    action: {
      type: String,
      default: '/video/upload'
    },
    maxSize: {
      type: Number,
      default: 100 * 1024 * 1024
    },
    fileTypes: {
      type: Array,
      default: () => ['video/mp4', 'video/avi', 'video/mov']
    }
  },
  data() {
    return {
      videoUrl: '',
      coverUrl: '',
      videoName: '',
      loading: null
    }
  },
  methods: {
    onFileUpload(file) {
      this.loading = this.$loading({
        lock: true,
        text: '视频上传中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      let formData = new FormData()
      formData.append('file', file.file)
      this.$http({
        url: this.action,
        data: formData,
        method: 'post',
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then((data) => {
        this.videoUrl = data.videoUrl || ''
        this.coverUrl = data.coverUrl || ''
        this.videoName = file.file.name
        this.$emit('upload-success', {
          videoUrl: this.videoUrl,
          coverUrl: this.coverUrl,
          width: data.width,
          height: data.height,
          duration: data.duration,
          name: file.file.name,
          size: file.file.size
        })
      }).catch(() => {
        this.$message.error('视频上传失败，请重试')
      }).finally(() => {
        this.loading && this.loading.close()
      })
    },
    beforeUpload(file) {
      if (this.fileTypes && this.fileTypes.length > 0) {
        let fileType = file.type
        let t = this.fileTypes.find((t) => t.toLowerCase() === fileType)
        if (!t) {
          this.$message.error(`视频格式错误，请上传以下格式的视频：${this.fileTypes.join('、')}`)
          return false
        }
      }
      if (this.maxSize && file.size > this.maxSize) {
        this.$message.error(`视频大小不能超过 ${this.fileSizeStr}!`)
        return false
      }
      this.$emit('before', file)
      return true
    },
    handleRemove() {
      this.videoUrl = ''
      this.coverUrl = ''
      this.videoName = ''
      this.$emit('upload-remove')
    }
  },
  computed: {
    fileSizeStr() {
      if (this.maxSize > 1024 * 1024) {
        return Math.round(this.maxSize / 1024 / 1024) + 'M'
      }
      if (this.maxSize > 1024) {
        return Math.round(this.maxSize / 1024) + 'KB'
      }
      return this.maxSize + 'B'
    }
  }
}
</script>

<style scoped lang="scss">
.video-upload {
  width: 100%;

  .upload-area {
    .video-uploader {
      width: 100%;

      .upload-trigger {
        width: 100%;
        height: 160px;
        border: 2px dashed #dcdfe6;
        border-radius: 8px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: border-color 0.3s ease;

        &:hover {
          border-color: #409eff;
        }

        i {
          font-size: 40px;
          color: #c0c4cc;
          margin-bottom: 10px;
        }

        .upload-text {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 4px;

          .upload-title {
            font-size: 14px;
            color: #606266;
          }

          .upload-tip {
            font-size: 12px;
            color: #c0c4cc;
          }
        }
      }
    }
  }

  .video-preview {
    .preview-video {
      width: 100%;
      max-height: 300px;
      border-radius: 6px;
      background: #000;
    }

    .video-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 8px;

      .video-name {
        font-size: 13px;
        color: #909399;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}
</style>