<template>
  <div class="image-upload">
    <div v-if="!imageUrl" class="upload-area">
      <el-upload
        class="image-uploader"
        action="#"
        :http-request="handleHttpRequest"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :accept="accept"
      >
        <div class="upload-trigger">
          <i class="el-icon-picture-outline"></i>
          <div class="upload-text">
            <span class="upload-title">点击上传图片</span>
            <span class="upload-tip">支持 JPG/PNG/GIF 格式</span>
          </div>
        </div>
      </el-upload>
    </div>
    <div v-else class="image-preview">
      <el-image
        :src="imageUrl"
        fit="cover"
        class="preview-image"
        :preview-src-list="[imageUrl]"
      />
      <div class="image-actions">
        <el-button
          type="danger"
          size="small"
          icon="el-icon-delete"
          @click="handleRemove"
          circle
        ></el-button>
        <span class="image-name">{{ imageName }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ImageUpload',
  props: {
    value: {
      type: String,
      default: ''
    },
    action: {
      type: String,
      default: '/image/upload'
    },
    maxSize: {
      type: Number,
      default: 10 * 1024 * 1024
    },
    accept: {
      type: String,
      default: 'image/*'
    }
  },
  data() {
    return {
      imageUrl: '',
      imageName: '',
      loading: null
    }
  },
  watch: {
    value: {
      immediate: true,
      handler(val) {
        if (val) {
          this.imageUrl = val
          this.imageName = this.getFileName(val)
        }
      }
    }
  },
  methods: {
    getFileName(url) {
      if (!url) return ''
      const parts = url.split('/')
      return parts[parts.length - 1] || ''
    },
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      if (!isImage) {
        this.$message.error('请上传图片格式的文件')
        return false
      }
      if (this.maxSize && file.size > this.maxSize) {
        this.$message.error(`图片大小不能超过 ${this.fileSizeStr}!`)
        return false
      }
      return true
    },
    handleHttpRequest(file) {
      this.loading = this.$loading({
        lock: true,
        text: '图片上传中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      const formData = new FormData()
      formData.append('file', file.file)
      this.$http({
        url: this.action,
        data: formData,
        method: 'post',
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then((data) => {
        this.imageUrl = data.originUrl || ''
        this.imageName = file.file.name
        this.$emit('input', this.imageUrl)
        this.$emit('upload-success', {
          originUrl: data.originUrl || '',
          thumbUrl: data.thumbUrl || '',
          name: data.name || file.file.name
        })
      }).catch(() => {
        this.$message.error('图片上传失败，请重试')
      }).finally(() => {
        this.loading && this.loading.close()
      })
    },
    handleRemove() {
      this.imageUrl = ''
      this.imageName = ''
      this.$emit('input', '')
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
.image-upload {
  width: 100%;

  .upload-area {
    .image-uploader {
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

  .image-preview {
    .preview-image {
      width: 100%;
      height: 200px;
      border-radius: 6px;
      border: 1px solid #ebeef5;
    }

    .image-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 8px;

      .image-name {
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
