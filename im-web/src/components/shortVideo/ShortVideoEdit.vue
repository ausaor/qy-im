<template>
  <el-dialog
    :title="isEdit ? '编辑短视频' : '发布短视频'"
    :visible.sync="visible"
    width="600px"
    :before-close="handleClose"
    :append-to-body="true"
    :close-on-click-modal="false"
  >
    <el-form
      :rules="rules"
      ref="formRef"
      label-width="100px"
      :model="form"
      class="form-box"
    >
      <el-form-item label="视频上传" prop="videoUrl">
        <VideoUpload
          ref="videoUploadRef"
          @upload-success="handleVideoUploadSuccess"
          @upload-remove="handleVideoRemove"
        />
      </el-form-item>

      <el-form-item label="可见范围" prop="scope">
        <el-select v-model="form.scope" placeholder="请选择可见范围">
          <el-option
            v-for="item in scopeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入视频标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="封面图片" prop="coverUrl">
        <ImageUpload
          ref="coverUploadRef"
          v-model="form.coverUrl"
          @upload-success="handleCoverUploadSuccess"
          @upload-remove="handleCoverRemove"
        />
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose" size="small">取消</el-button>
      <el-button
        type="primary"
        @click="submitForm"
        size="small"
        :loading="submitting"
      >
        {{ isEdit ? '更新' : '发布' }}
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import VideoUpload from '@/components/common/VideoUpload.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'

export default {
  name: 'ShortVideoEdit',
  components: {
    VideoUpload,
    ImageUpload,
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    videoId: {
      type: Number,
      default: null
    },
    type: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      submitting: false,
      form: {
        id: null,
        scope: 9,
        title: '',
        coverUrl: '',
        videoUrl: '',
        duration: 0,
        width: 0,
        height: 0,
        size: 0
      },
      rules: {
        videoUrl: [
          { required: true, message: '请上传视频', trigger: 'change' }
        ],
        scope: [
          { required: true, message: '请选择可见范围', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' }
        ],
        coverUrl: [
          { required: true, message: '请上传视频以生成封面', trigger: 'change' }
        ]
      },
      scopeOptions: [
        { value: 9, label: '公开' },
        { value: 3, label: '关注可见' },
        { value: 2, label: '好友可见' },
        { value: 1, label: '私密' }
      ]
    }
  },
  computed: {
    isEdit() {
      return this.videoId != null
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.resetForm()
        if (this.videoId != null) {
          this.loadVideoDetail()
        }
      }
    },
    videoId() {
      if (this.visible && this.videoId != null) {
        this.loadVideoDetail()
      }
    }
  },
  methods: {
    handleClose() {
      this.resetForm()
      this.$emit('close')
    },
    resetForm() {
      this.form = {
        id: null,
        scope: 9,
        title: '',
        coverUrl: '',
        videoUrl: '',
        duration: 0,
        width: 0,
        height: 0,
        size: 0
      }
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.clearValidate()
        }
      })
    },
    loadVideoDetail() {
      this.$http({
        url: `/shortVideo/detail/${this.videoId}`,
        method: 'get'
      }).then((data) => {
        this.form.id = data.id
        this.form.scope = data.scope
        this.form.title = data.title || ''
        this.form.coverUrl = data.coverUrl || ''
        this.form.videoUrl = data.videoUrl || ''
        this.form.duration = data.duration || 0
        this.form.width = data.width || 0
        this.form.height = data.height || 0
        this.form.size = data.size || 0
      }).catch(() => {
        this.$message.error('获取视频详情失败')
      })
    },
    handleVideoUploadSuccess(data) {
      this.form.videoUrl = data.videoUrl
      this.form.coverUrl = data.coverUrl
      if (data.size) {
        this.form.size = data.size
      }
      this.form.duration = data.duration
      this.form.width = data.width
      this.form.height = data.height
    },
    handleVideoRemove() {
      this.form.videoUrl = ''
      this.form.coverUrl = ''
      this.form.size = 0
    },
    handleCoverUploadSuccess(data) {
      this.form.coverUrl = data.originUrl
      this.form.width = data.width
    },
    handleCoverRemove() {
      this.form.coverUrl = ''
    },
    submitForm() {
      this.$refs.formRef.validate((valid) => {
        if (!valid) {
          return
        }
        this.submitting = true
        const isEdit = this.form.id != null
        const url = isEdit ? '/shortVideo/update' : '/shortVideo/add'
        const params = {
          type: this.type,
          scope: this.form.scope,
          title: this.form.title,
          coverUrl: this.form.coverUrl,
          videoUrl: this.form.videoUrl,
          duration: this.form.duration,
          width: this.form.width,
          height: this.form.height,
          size: this.form.size
        }
        if (isEdit) {
          params.id = this.form.id
        }
        this.$http({
          url: url,
          method: 'post',
          data: params
        }).then(() => {
          this.$message.success(isEdit ? '更新成功' : '发布成功')
          this.resetForm()
          this.$emit('refresh')
          this.handleClose()
        }).catch(() => {
          this.$message.error(isEdit ? '更新失败' : '发布失败')
        }).finally(() => {
          this.submitting = false
        })
      })
    }
  }
}
</script>

<style scoped lang="scss">
.form-box {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.dialog-footer {
  text-align: right;
}
</style>