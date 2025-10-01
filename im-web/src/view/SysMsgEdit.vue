<template>
  <div class="message-form-container">
    <div class="form-header">
      <h2>{{ isEdit ? '编辑消息' : '新增消息' }}</h2>
      <div class="back">
        <el-button @click="handleBack" type="text">返回</el-button>
      </div>
    </div>

    <el-form
        ref="messageForm"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="message-form"
    >
      <!-- 消息类型 -->
      <el-form-item label="消息类型" prop="type">
        <el-select
            v-model="form.type"
            placeholder="请选择消息类型"
            @change="handleTypeChange"
            style="width: 300px"
        >
          <el-option
              v-for="item in messageTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>

      <!-- 标题 -->
      <el-form-item label="标题" prop="title">
        <el-input
            v-model="form.title"
            placeholder="请输入消息标题"
            style="width: 500px"
        />
      </el-form-item>

      <!-- 封面图片 -->
      <el-form-item label="封面图片" prop="coverUrl">
        <el-upload
            class="cover-uploader"
            action="/api/image/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :accept="imgTypes.join(',')"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
        >
          <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-image">
          <i v-else class="el-icon-plus cover-uploader-icon"></i>
        </el-upload>
        <div class="upload-tip">建议尺寸：750x400px，支持jpg、png格式</div>
      </el-form-item>

      <!-- 简述 -->
      <el-form-item label="简述" prop="intro">
        <el-input
            v-model="form.intro"
            type="textarea"
            :rows="3"
            placeholder="请输入消息简述"
            style="width: 500px"
        />
      </el-form-item>

      <!-- 内容 - 根据消息类型显示不同的编辑器 -->
      <el-form-item label="内容" prop="content">
        <!-- 文字、外部链接 - 文本域 -->
        <el-input
            v-if="form.type === 0 || form.type === 5"
            v-model="form.content"
            type="textarea"
            :rows="6"
            :placeholder="form.type === 0 ? '请输入文字内容' : '请输入外部链接地址'"
            style="width: 600px"
        />

        <!-- 富文本编辑器 -->
        <div v-else-if="form.type === 9" class="quill-editor-container">
          <quill-editor
              ref="quillEditor"
              v-model="form.content"
              :options="editorOptions"
              style="min-height: 300px"
          />
        </div>

        <!-- 图片上传 - 最多9张 -->
        <div v-else-if="form.type === 1" class="image-upload-container">
          <el-upload
              action="/api/image/upload"
              :headers="uploadHeaders"
              list-type="picture-card"
              :file-list="imageList"
              :on-success="handleImageSuccess"
              :on-remove="handleImageRemove"
              :before-upload="beforeImageUpload"
              :limit="9"
              :on-exceed="handleImageExceed"
          >
            <i class="el-icon-plus"></i>
          </el-upload>
          <div class="upload-tip">最多可上传9张图片，支持jpg、png格式</div>
        </div>

        <!-- 文件上传 - 最多1个 -->
        <div v-else-if="form.type === 2" class="file-upload-container">
          <el-upload
              action="/api/file/upload"
              :headers="uploadHeaders"
              :file-list="fileList"
              :on-success="handleFileSuccess"
              :on-remove="handleFileRemove"
              :before-upload="beforeFileUpload"
              :limit="1"
              :on-exceed="handleFileExceed"
          >
            <el-button size="small" type="primary">
              <i class="el-icon-upload"></i> 选择文件
            </el-button>
          </el-upload>
          <div class="upload-tip">支持常见文档格式，单个文件不超过50MB</div>
        </div>

        <!-- 音频上传 - 最多1个 -->
        <div v-else-if="form.type === 3" class="audio-upload-container">
          <el-upload
              action="/api/audio/upload"
              :headers="uploadHeaders"
              :file-list="audioList"
              :on-success="handleAudioSuccess"
              :on-remove="handleAudioRemove"
              :before-upload="beforeAudioUpload"
              :accept="audioTypes.join(',')"
              :limit="1"
              :on-exceed="handleAudioExceed"
          >
            <el-button size="small" type="primary">
              <i class="el-icon-microphone"></i> 选择音频
            </el-button>
          </el-upload>
          <div class="upload-tip">支持mp3、wav、m4a格式，单个文件不超过10MB</div>

          <!-- 音频播放器 -->
          <div v-if="audioList.length > 0" class="audio-player">
            <audio controls :src="audioList[0].url" style="width: 100%; margin-top: 10px;"></audio>
          </div>
        </div>

        <!-- 视频上传 - 最多1个 -->
        <div v-else-if="form.type === 4" class="video-upload-container">
          <el-upload
              action="/api/video/upload"
              :headers="uploadHeaders"
              :file-list="videoList"
              :on-success="handleVideoSuccess"
              :on-remove="handleVideoRemove"
              :before-upload="beforeVideoUpload"
              :accept="videoTypes.join(',')"
              :limit="1"
              :on-exceed="handleVideoExceed"
          >
            <el-button size="small" type="primary">
              <i class="el-icon-video-camera"></i> 选择视频
            </el-button>
          </el-upload>
          <div class="upload-tip">支持mp4、avi、mov格式，单个文件不超过50MB</div>

          <!-- 视频播放器 -->
          <div v-if="videoList.length > 0" class="video-player">
            <video
                controls
                :poster="videoList[0].coverUrl"
                :src="videoList[0].url"
                style="width: 100%; max-width: 600px; margin-top: 10px;"
            ></video>
          </div>
        </div>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item class="form-actions">
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '更新' : '保存' }}
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
// 引入富文本quill-editor相关组件依赖
import { quillEditor, Quill } from 'vue-quill-editor'
//import { container, QuillWatch } from 'quill-image-extend-module'
import ImageResize from 'quill-image-resize-module' // 引用，调整图片大小
Quill.register('modules/imageResize', ImageResize)
// import {ImageDrop} from 'quill-image-drop-module'// 引用，粘贴图片
// Quill.register('modules/imageDrop', ImageDrop) //粘贴图片

export default {
  name: 'MessageForm',
  components: {
    quillEditor
  },
  data() {
    return {
      imgTypes: ['image/jpeg', 'image/png', 'image/jpg'],
      audioTypes: ['audio/mp3', 'audio/wav', 'audio/m4a'],
      videoTypes: ['video/mp4', 'video/avi', 'video/mov'],
      isEdit: false,
      loading: false,
      form: {
        id: null,
        type: 0,
        title: '',
        coverUrl: '',
        intro: '',
        content: ''
      },
      messageTypes: [
        { value: 0, label: '文字' },
        { value: 1, label: '图片' },
        { value: 2, label: '文件' },
        { value: 3, label: '音频' },
        { value: 4, label: '视频' },
        { value: 5, label: '外部链接' },
        { value: 9, label: '富文本' }
      ],
      rules: {
        type: [
          { required: true, message: '请选择消息类型', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { min: 1, max: 50, message: '标题长度在1到50个字符', trigger: 'blur' }
        ],
        intro: [
          { max: 100, message: '简述不能超过100个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入内容', trigger: 'blur' }
        ]
      },
      editorOptions: {
        theme: 'snow',
        modules: {
          // 调整图片大小
          imageResize: {
            displayStyles: {
              backgroundColor: 'black',
              border: 'none',
              color: 'white'
            },
            modules: [ 'Resize', 'DisplaySize', 'Toolbar' ]
          },
          toolbar: {
            container: [
              ['bold', 'italic', 'underline', 'strike'],
              ['blockquote', 'code-block'],
              [{ 'header': 1 }, { 'header': 2 }],
              [{ 'list': 'ordered'}, { 'list': 'bullet' }],
              [{ 'script': 'sub'}, { 'script': 'super' }],
              [{ 'indent': '-1'}, { 'indent': '+1' }],
              [{ 'direction': 'rtl' }],
              [{ 'size': ['small', false, 'large', 'huge'] }],
              [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
              [{ 'color': [] }, { 'background': [] }],
              [{ 'font': [] }],
              [{ 'align': [] }],
              ['clean'],
              ['link', 'image', 'video']
            ],
            handlers: {
              image: this.handleQuillImageUpload,
              video: this.handleQuillVideoUpload
            }
          }
        }
      },
      imageList: [],
      fileList: [],
      audioList: [],
      videoList: []
    }
  },
  computed: {
    uploadHeaders() {
      return {
        'accessToken': `${this.getAccessToken()}`
      }
    }
  },
  mounted() {
    if (this.$route.query.id) {
      this.isEdit = true
      this.loadMessageData(this.$route.query.id)
    }
  },
  methods: {
    getAccessToken() {
      return sessionStorage.getItem('accessToken') || ''
    },

    handleQuillImageUpload() {
      const input = document.createElement('input')
      input.setAttribute('type', 'file')
      input.setAttribute('accept', 'image/*')
      input.click()

      input.onchange = () => {
        const file = input.files[0]
        if (file) {
          const formData = new FormData()
          formData.append('file', file)
          this.$http({
            url: '/image/upload',
            method: 'POST',
            data: formData,
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          }).then(result => {
            if (result.originUrl) {
              const quill = this.$refs.quillEditor.quill
              const range = quill.getSelection()
              quill.insertEmbed(range.index, 'image', result.originUrl)
            }
          })
        }
      }
    },

    handleQuillVideoUpload() {
      const input = document.createElement('input')
      input.setAttribute('type', 'file')
      input.setAttribute('accept', 'video/*')
      input.click()

      input.onchange = () => {
        const file = input.files[0]
        if (file) {
          const formData = new FormData()
          formData.append('file', file)

          this.$http({
            url: '/video/upload',
            method: 'POST',
            data: formData,
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          }).then(result => {
            if (result.videoUrl) {
              const quill = this.$refs.quillEditor.quill
              const range = quill.getSelection()
              quill.insertEmbed(range.index, 'video', result.videoUrl)
            }
          })
        }
      }
    },

    handleTypeChange(type) {
      this.form.content = ''
      this.imageList = []
      this.fileList = []
      this.audioList = []
      this.videoList = []
    },

    // 封面图片上传成功
    handleCoverSuccess(response, file) {
      this.form.coverUrl = response.data.originUrl
    },

    // 封面图片上传前验证
    beforeCoverUpload(file) {
      const isImage = file.type.indexOf('image/') === 0
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('只能上传图片格式的文件!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('上传图片大小不能超过 2MB!')
        return false
      }
      return true
    },

    // 图片上传成功
    handleImageSuccess(response, file, fileList) {
      const imgItem = {
        url: response.data.originUrl,
        name: file.name,
        originName: response.data.name,
        type: 'image',
        size: file.size
      }
      this.imageList.push(imgItem)
      this.updateContentFromList('image')
    },

    // 图片移除
    handleImageRemove(file, fileList) {
      this.imageList = fileList
      this.updateContentFromList('image')
    },

    // 图片上传前验证
    beforeImageUpload(file) {
      const isImage = file.type.indexOf('image/') === 0
      const isLt5M = file.size / 1024 / 1024 < 5

      if (!isImage) {
        this.$message.error('只能上传图片格式的文件!')
        return false
      }
      if (!isLt5M) {
        this.$message.error('上传图片大小不能超过 5MB!')
        return false
      }
      return true
    },

    // 图片数量超限
    handleImageExceed() {
      this.$message.warning('最多只能上传9张图片')
    },

    // 文件上传成功
    handleFileSuccess(response, file, fileList) {
      const fileItem = {
        url: response.data,
        name: file.name,
        type: 'file',
        size: file.size
      }
      this.fileList.push(fileItem)
      this.updateContentFromList('file')
    },

    // 文件移除
    handleFileRemove(file, fileList) {
      this.fileList = fileList
      this.updateContentFromList('file')
    },

    // 文件上传前验证
    beforeFileUpload(file) {
      const isLt50M = file.size / 1024 / 1024 < 50

      if (!isLt50M) {
        this.$message.error('上传文件大小不能超过 50MB!')
        return false
      }
      return true
    },

    handleFileExceed() {
      this.$message.warning('最多只能上传1个文件')
    },

    handleAudioSuccess(response, file, fileList) {
      const audioItem = {
        url: response.data.url,
        originalName: response.data.originalName,
        name: file.name,
        type: 'audio',
        size: file.size
      }
      this.audioList.push(audioItem)
      this.updateContentFromList('audio')
    },

    handleAudioRemove(file, fileList) {
      this.audioList = fileList
      this.updateContentFromList('audio')
    },

    beforeAudioUpload(file) {
      const isAudio = file.type.indexOf('audio/') === 0
      const isLt10M = file.size / 1024 / 1024 < 10

      if (!isAudio) {
        this.$message.error('只能上传音频格式的文件!')
        return false
      }
      if (!isLt10M) {
        this.$message.error('上传音频大小不能超过 100MB!')
        return false
      }
      return true
    },

    handleAudioExceed() {
      this.$message.warning('最多只能上传1个音频文件')
    },

    handleVideoSuccess(response, file, fileList) {
      const videoItem = {
        url: response.data.videoUrl,
        coverUrl: response.data.coverUrl,
        name: file.name,
        type: 'video',
        size: file.size
      }
      this.videoList.push(videoItem)
      this.updateContentFromList('video')
    },

    handleVideoRemove(file, fileList) {
      this.videoList = fileList
      this.updateContentFromList('video')
    },

    beforeVideoUpload(file) {
      const isVideo = file.type.indexOf('video/') === 0
      const isLt50M = file.size / 1024 / 1024 < 50

      if (!isVideo) {
        this.$message.error('只能上传视频格式的文件!')
        return false
      }
      if (!isLt50M) {
        this.$message.error('上传视频大小不能超过 500MB!')
        return false
      }
      return true
    },

    handleVideoExceed() {
      this.$message.warning('最多只能上传1个视频文件')
    },

    updateContentFromList(type) {
      let list = []
      switch (type) {
        case 'image':
          list = this.imageList
          this.form.content = this.buildImgContent(list)
          break
        case 'file':
          list = this.fileList
          this.form.content = this.buildFileContent(list)
          break
        case 'audio':
          list = this.audioList
          this.form.content = this.buildAudioContent(list)
          break
        case 'video':
          list = this.videoList
          this.form.content = this.buildVideoContent(list)
          break
      }
    },
    buildImgContent(list) {
      return JSON.stringify(list.map(item => ({
        name: item.name,
        originalName: item.originalName,
        url: item.url,
        size: item.size,
        type: item.type
      })))
    },
    buildVideoContent(list) {
      return JSON.stringify(list.map(item => ({
        name: item.name,
        url: item.url,
        coverUrl: item.coverUrl,
        size: item.size,
        type: item.type
      })))
    },
    buildAudioContent(list) {
      return JSON.stringify(list.map(item => ({
        name: item.name,
        originalName: item.originalName,
        url: item.url,
        size: item.size,
        type: item.type
      })))
    },
    buildFileContent(list) {
      return JSON.stringify(list.map(item => ({
        name: item.name,
        url: item.url,
        size: item.size,
        type: item.type
      })))
    },

    async loadMessageData(id) {
      try {
        const response = await this.$http.get(`/message/system/get?id=${id}`)
        const data = response

        this.form = { ...data }

        if (data.content && [1, 2, 3, 4].includes(data.type)) {
          const contentData = JSON.parse(data.content)
          switch (data.type) {
            case 1:
              this.imageList = contentData
              break
            case 2:
              this.fileList = contentData
              break
            case 3:
              this.audioList = contentData
              break
            case 4:
              this.videoList = contentData
              break
          }
        }
      } catch (error) {
        this.$message.error('加载数据失败')
      }
    },

    handleSubmit() {
      this.$refs.messageForm.validate(async (valid) => {
        if (valid) {
          this.loading = true
          try {
            const url = this.isEdit ? `/message/system/modify` : '/message/system/save'
            this.$http({
              url: url,
              method: "post",
              data: this.form
            }).then((data) => {
              this.$message.success(this.isEdit ? '更新成功' : '保存成功')
            })
          } catch (error) {
            this.$message.error(this.isEdit ? '更新失败' : '保存失败')
          } finally {
            this.loading = false
          }
        }
      })
    },

    handleCancel() {
      this.$router.push('/home/square/sysMsg')
    },

    handleReset() {
      this.$refs.messageForm.resetFields()
      this.imageList = []
      this.fileList = []
      this.audioList = []
      this.videoList = []
    },

    handleBack() {
      this.$router.push('/home/square/sysMsg')
    },
  }
}
</script>

<style scoped>
.message-form-container {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-width: 1000px;
  margin: 0 auto;
}

.form-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
}

.back {
  font-size: 16px;
  cursor: pointer;
}

.form-header h2 {
  margin: 0;
  color: #333;
  font-size: 20px;
  font-weight: 500;
}

.message-form {
  margin-top: 24px;
}

.cover-uploader {
  display: inline-block;
}

.cover-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 200px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-uploader .el-upload:hover {
  border-color: #409EFF;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.cover-image {
  max-width: 300px;
  max-height: 200px;
  width:auto;
  height:auto;
  object-fit: contain; /* 保持比例，完整显示图片 */
  border-radius: 8px;
}

.upload-tip {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

.quill-editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.image-upload-container,
.file-upload-container,
.audio-upload-container,
.video-upload-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
  background: #fafafa;
}

.audio-player,
.video-player {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
}

.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
}

.form-actions .el-button {
  margin-right: 16px;
}

/* 富文本编辑器样式调整 */
.quill-editor-container .ql-editor {
  min-height: 200px;
}

/* 上传组件样式调整 */
.el-upload-list--picture-card .el-upload-list__item {
  width: 100px;
  height: 100px;
}

.el-upload--picture-card {
  width: 100px;
  height: 100px;
  line-height: 100px;
}
</style>