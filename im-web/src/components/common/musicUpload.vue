<template>
  <!-- 音乐上传弹窗 -->
  <el-dialog
      title="上传新音乐"
      :visible.sync="dialogVisible"
      width="600px"
      custom-class="music-dialog"
      :before-close="resetForm"
  >
    <el-form :model="form" :rules="rules" ref="musicForm" label-width="100px">
      <!-- 歌曲名称 -->
      <el-form-item label="歌曲名称" prop="name">
        <el-input
            v-model="form.name"
            placeholder="请输入歌曲名称"
            class="custom-input"
        ></el-input>
      </el-form-item>

      <!-- 歌手 -->
      <el-form-item label="歌手">
        <el-input
            v-model="form.singer"
            placeholder="请输入歌手姓名或乐队名称"
        ></el-input>
      </el-form-item>

      <!-- 封面图片上传 -->
      <el-form-item label="封面图片">
        <el-upload
            class="cover-uploader"
            :action="'#'"
            :accept="fileTypes.join(',')"
            :http-request="onImageUpload"
            :before-upload="beforeUpload"
            :show-file-list="false">
          <img v-if="form.cover" :src="form.cover" class="cover">
          <i v-else class="el-icon-plus cover-uploader-icon"></i>
        </el-upload>
      </el-form-item>

      <!-- 歌曲文件上传 -->
      <el-form-item label="歌曲文件" prop="url">
        <el-upload
            class="upload-area"
            action="#"
            :on-change="handleMusicChange"
            :auto-upload="false"
            :limit="1"
            :file-list="fileList"
            accept="audio/*"
        >
          <i class="el-icon-plus"></i>
          <div class="el-upload__text">点击上传音乐文件</div>
          <div class="el-upload__tip">支持 MP3/WAV/FLAC 格式，大小不超过 10MB</div>
        </el-upload>
      </el-form-item>
    </el-form>

    <div class="dialog-footer">
      <button class="cancel-btn" @click="resetForm">取消</button>
      <button class="submit-btn" @click="submitForm">提交</button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'MusicUpload',
  props: {
    category: {
      type: String,
      required: true
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
  data() {
    return {
      dialogVisible: false,
      coverPreview: '',
      coverImage: [],
      fileList: [],
      form: {
        category: '',
        name: '',
        singer: '',
        url: '',
        cover: '',
        groupId: null,
        regionCode: null
      },
      rules: {
        name: [
          {required: true, message: '请输入歌曲名称', trigger: 'blur'}
        ],
        url: [
          {required: true, message: '请上传歌曲文件', trigger: 'change'}
        ]
      },
      uploadHeaders: {"accessToken":sessionStorage.getItem('accessToken')},
      fileTypes: ['image/jpeg', 'image/png', 'image/jpg'],
    }
  },
  methods: {
    open() {
      this.dialogVisible = true;
    },
    // 处理封面图片上传
    handleCoverChange(file) {
      const isImage = file.raw.type.includes('image');
      const isLt5M = file.raw.size / 1024 / 1024 < 5;

      if (!isImage) {
        this.$message.error('只能上传图片文件!');
        return false;
      }

      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB!');
        return false;
      }

      // 生成预览图
      this.coverPreview = URL.createObjectURL(file.raw);
      this.form.cover = file.name;

      return false;
    },

    // 处理音乐文件上传
    handleMusicChange(file) {
      const isAudio = file.raw.type.includes('audio');
      const isLt100M = file.raw.size / 1024 / 1024 < 100;

      if (!isAudio) {
        this.$message.error('只能上传音频文件!');
        return false;
      }

      if (!isLt100M) {
        this.$message.error('音频文件大小不能超过 100MB!');
        return false;
      }

      this.fileList = [file];
      this.form.url = file.name;

      return false;
    },

    // 格式化文件大小
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },

    // 提交表单
    async submitForm() {
      this.$refs.musicForm.validate(async (valid) => {
        if (valid) {
          // 模拟上传过程
          this.$message({
            message: '歌曲上传中...',
            type: 'info',
            duration: 2000
          });
          let musicFIle = await this.onMusicUpload(this.fileList[0])
          if (musicFIle) {
            this.form.groupId = this.groupId
            this.form.regionCode = this.regionCode
            this.form.category = this.category
            this.form.url = musicFIle.url
            this.$http({
              url: "/music/add",
              method: 'post',
              data: this.form
            }).then(data => {
              this.$message({
                message: '歌曲上传成功!',
                type: 'success',
                duration: 3000
              });
              this.form.cover = '';
              this.resetForm();
              this.$emit('add', data)
            })
          }
        } else {
          this.$message.error('请填写必填字段');
          return false;
        }
      });
    },

    // 重置表单
    resetForm() {
      this.$refs.musicForm.resetFields();
      this.coverPreview = '';
      this.fileList = [];
      this.dialogVisible = false;
    },
    onMusicUpload() {
      return new Promise((resolve, reject) => {
        let formData = new FormData()
        formData.append('file', this.fileList[0].raw)
        this.$http({
          url: '/audio/upload',
          data: formData,
          method: 'post',
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }).then((data) => {
          resolve(data)
        }).catch((e) => {
          this.$message.error('上传音乐文件失败');
          resolve(null);
        })
      })

    },
    onImageUpload(file) {
      let formData = new FormData()
      formData.append('file', file.file)
      this.$http({
        url: '/image/upload',
        data: formData,
        method: 'post',
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then((data) => {
        this.form.cover = data.originUrl
      }).catch((e) => {
      }).finally(() => {
      })
    },
    beforeUpload(file) {
      // 校验文件类型
      if(this.fileTypes && this.fileTypes.length > 0){
        let fileType = file.type;
        let t = this.fileTypes.find((t) => t.toLowerCase() === fileType);
        if (t === undefined) {
          this.$message.error(`文件格式错误，请上传以下格式的文件：${this.fileTypes.join("、")}`);
          return false;
        }
      }
      // 校验大小
      if (this.maxSize && file.size > this.maxSize) {
        this.$message.error(`文件大小不能超过 ${this.fileSizeStr}!`);
        return false;
      }
      return true;
    },
  }
}
</script>

<style scoped lang="scss">
/* 弹窗自定义样式 */
.form-title {
  color: #fff;
  text-align: center;
  font-size: 1.8rem;
  padding-bottom: 15px;
}

.cover-uploader {
  border: 1px dashed #606080;
  width: 178px;
  height: 178px;
}

.cover-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}


.cover-uploader .el-upload:hover {
  border-color: #409EFF;
}

::v-deep .el-form-item__content {
  display: flex;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.cover {
  width: 178px;
  height: 178px;
  display: block;
}

.cover-preview {
  width: 160px;
  height: 160px;
  border-radius: 10px;
  overflow: hidden;
  margin: 0 auto 20px;
  border: 2px dashed #606080;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1e1e32;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: none;
}

.cover-preview .placeholder {
  color: #606080;
  text-align: center;
  padding: 20px;
}

.upload-area {
  border: 1px dashed #606080;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.upload-area:hover {
  border-color: #2575fc;
}

.upload-area i {
  font-size: 40px;
  color: #606080;
  margin-bottom: 10px;
  display: block;
}

.el-upload__tip {
  color: #a0a0c0;
  font-size: 13px;
  margin-top: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 25px;
}

.submit-btn {
  background: linear-gradient(90deg, #6a11cb 0%, #2575fc 100%);
  border: none;
  padding: 12px 30px;
  font-size: 1rem;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover {
  background: linear-gradient(90deg, #7a1bdb 0%, #3585ff 100%);
  transform: translateY(-2px);
}

.cancel-btn {
  background: rgba(100, 100, 200, 0.2);
  border: none;
  padding: 12px 25px;
  font-size: 1rem;
  border-radius: 6px;
  color: #a0a0c0;
  cursor: pointer;
  margin-right: 15px;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: rgba(100, 100, 200, 0.3);
  color: white;
}
</style>