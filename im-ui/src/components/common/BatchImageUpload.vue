<template>
  <el-upload ref="uploader" class="uploader"
      :action="'#'"
      :http-request="onFileUpload"
      :headers="uploadHeaders"
      :before-upload="beforeUpload"
      :on-remove="handleRemove"
      :file-list="imageList"
      list-type="picture-card"
      :multiple="true"
  >
    <i class="el-icon-plus" />
  </el-upload>
</template>

<script>
export default {
  name: "BatchImageUpload",
  props: {
    action: {
      type: String,
      required: true
    },
    imageList: {
      type: Array,
      default: null
    },
    fileTypes: {
      type: Array,
      default: null
    },
    maxSize: {
      type: Number,
      default: null
    },
    showLoading: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      uploadHeaders: {"accessToken":sessionStorage.getItem('accessToken')}
    }
  },
  methods: {
    onFileUpload(file) {
      // 展示加载条
      if (this.showLoading) {
        this.loading = this.$loading({
          lock: true,
          text: '正在上传...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
      }

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
        this.$emit("success", data, file.file);
      }).catch((e) => {
        this.$emit("fail", e, file.file);
      }).finally(() => {
        this.loading && this.loading.close();
      })
    },
    handleSuccess(res, file) {
      if (res.code === 200) {
        this.$emit("success", res, file);
      }else{
        this.$message.error(res.message);
        this.$emit("fail", res, file);
      }
    },
    handleRemove(file) {
      this.$emit("remove", file);
    },
    handleError(err,file){
      this.$emit("fail", err, file);
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
      this.$emit("before", file);
      return true;
    },
    handleChange(info, fileList) {

    },
    clearImages () {
      this.$refs['uploader'].clearFiles();
    },
  },
  computed: {
    fileSizeStr() {
      if (this.maxSize > 1024 * 1024) {
        return Math.round(this.maxSize / 1024 / 1024) + "M";
      }
      if (this.maxSize > 1024) {
        return Math.round(this.maxSize / 1024) + "KB";
      }
      return this.maxSize + "B";
    }
  },
  mounted() {
    this.$nextTick(function() {
      this.$on('removeImages', function() {
        this.$refs['uploader'].clearFiles();
      });
    });
  }
}
</script>

<style scoped>

</style>