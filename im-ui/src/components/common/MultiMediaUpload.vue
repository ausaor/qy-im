<template>
  <div class="media-container">
    <div class="media-list" v-for="(item, index) in mediaFileList" :key="index">
      <el-image
          class="media-file image-file"
          v-if="item.fileType == IMAGE_TYPE || fileType == IMAGE_TYPE"
          :src="item.url"
      ></el-image>
      <video
          class="media-file video-file"
          v-if="item.fileType == VIDEO_TYPE || fileType == VIDEO_TYPE"
          :src="item.url"
          controls
      ></video>
      <vue-audio class="media-file audio-file"
                 v-if="item.fileType == AUDIO_TYPE || fileType == AUDIO_TYPE"
                 :width="210"
                 :audio-source="item.url"
      ></vue-audio>
<!--      <audio
          class="media-file audio-file"
          v-if="item.fileType == AUDIO_TYPE || fileType == AUDIO_TYPE"
          :src="item.url"
          controls
      ></audio>-->
      <i
          class="el-icon-zoom-in zoom-in-file"
          v-if="item.fileType == IMAGE_TYPE || fileType == IMAGE_TYPE"
          @click="handlePreview(item)"
      ></i>
      <i
          class="el-icon-delete-solid delete-file"
          @click="handleDelete(item)"
      ></i>
    </div>
    <el-upload
        ref="upload"
        action="#"
        list-type="picture-card"
        :multiple="multiple"
        :limit="limit"
        :accept="accept"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="handleChange"
        :on-exceed="handleExceed"
        :class="isExceed"
    >
      <i class="el-icon-plus"></i>
    </el-upload>
    <image-viewer
        style="z-index: 9999"
        v-if="showViewer"
        :on-close="closePreview"
        :url-list="previewImages"
        :initial-index="initialIndex"
    >
    </image-viewer>
  </div>
</template>

<script>
const IMAGE_TYPE = 1; // 图片
const VIDEO_TYPE = 2; // 视频
const AUDIO_TYPE = 3; // 音频
import imageViewer from "element-ui/packages/image/src/image-viewer";

export default {
  name: "MultiMediaUpload",
  props: {
    fileList: {
      type: Array,
      default: () => [],
    },
    // 指定文件类型
    fileType: {
      type: Number
    },
    // 是否允许多选
    multiple: {
      type: Boolean,
      default: false,
    },
    // 数量限制
    limit: {
      type: Number,
      default: 9,
    },
    // 限制可选上传文件类型
    accept: {
      type: String,
      default: "image/*,video/*,audio/*",
    },
    // 大小限制(MB)
    fileSize: {
      type: [Number, String],
    },
  },
  components: {imageViewer},
  data() {
    return {
      IMAGE_TYPE,
      VIDEO_TYPE,
      AUDIO_TYPE,
      mediaFileList: [],
      deleteFileList: [],
      showViewer: false,
      initialIndex: 0,
    };
  },
  computed: {
    // 超出限制数量隐藏上传框
    isExceed() {
      return this.mediaFileList.length >= this.limit ? "hide" : "";
    },
    // 预览图片集合
    previewImages() {
      return this.mediaFileList
          .filter((item) => item.fileType === IMAGE_TYPE)
          .map((item) => item.url);
    },
  },
  methods: {
    handleChange(file, fileList) {
      if (this.mediaFileList.length >= this.limit) {
        this.$message.warning(`已超出上传数量,最多只能上传 ${this.limit} 个`);
        return
      }
      if (this.handleCheckFile(file)) {
        let fileItem = {
          fileType: this.handleMediaFileType(file),
          url: file.url,
          raw: file.raw,
        };
        this.mediaFileList.push(fileItem);
        this.$emit("update:fileList", this.mediaFileList);
      } else {
        const index = fileList.indexOf(file);
        if (index !== -1) {
          fileList.splice(index, 1);
        }
        this.$emit("update:fileList", fileList);
      }
    },
    // 超出上传数量提示
    handleExceed() {
      this.$message.warning(`已超出上传数量,最多只能上传 ${this.limit} 个`);
    },
    // 图片预览
    handlePreview(file) {
      // 预览图片索引，若没有则始终从第一张开始预览
      this.initialIndex = this.previewImages.findIndex(
          (item) => item === file.url
      );
      this.showViewer = true;
    },
    // 关闭图片预览
    closePreview() {
      this.showViewer = false;
    },
    // 删除文件
    handleDelete(item) {
      const deleteObj = {
        text: "确定删除吗, 是否继续",
        methods: () => {
          if (!item.raw) {
            this.deleteFileList.push(item); // 删除已上传图片
          }
          const filterFiles = this.mediaFileList.indexOf(item);
          if (filterFiles !== -1) {
            this.mediaFileList.splice(filterFiles, 1);
          }
          this.$emit("update:fileList", this.mediaFileList);
          this.$emit("deleteFile", this.deleteFileList); // 若需要删除数据
          // 更新upload上传数量,不然触发上传数量提示
          this.$nextTick(() => {
            this.$refs.upload.clearFiles();
          });
        },
      };
      this.$commonUtil.isConfirm(deleteObj);
    },
    // 校验文件
    handleCheckFile(file) {
      let typeTip = "";
      let sizeTip = "";
      if (this.accept) {
        const fileType = file.raw.type;
        const fileExtension = `.${file.name.split(".").pop()}`;
        const acceptedTypes = this.accept.split(",").map((type) => type.trim());
        const isType = acceptedTypes.some((accept) => {
          // 校验指定格式
          if (accept.startsWith(".")) {
            return accept === fileExtension;
          }
          // 校验图片、视频、音频格式
          const regex = new RegExp(`^${accept.replace("*", ".*")}$`);
          return regex.test(fileType);
        });
        if (!isType) {
          typeTip = `文件格式不正确, 请上传${this.accept}格式文件`;
        }
      }
      if (this.fileSize) {
        const isSize = file.size / 1024 / 1024 < this.fileSize;
        if (!isSize) {
          sizeTip = `上传文件大小不能超过 ${this.fileSize} MB`;
        }
      }
      if (typeTip || sizeTip) {
        this.$message.warning(`${typeTip} ${sizeTip}`);
        return false;
      }
      return true;
    },
    // 回显文件类型
    handleMediaFileType(file) {
      const typeMap = {
        image: IMAGE_TYPE,
        video: VIDEO_TYPE,
        audio: AUDIO_TYPE,
      };
      const fileType = file.raw.type.split("/")[0];
      return typeMap[fileType] || this.fileType;
    },
    handleFile() {
      this.mediaFileList = this.fileList;
    },
  },
  created() {
    //this.handleFile();
  },
  beforeDestroy() {
  },
  watch: {
    fileList: {
      handler(newFileList, oldFileList) {
        this.mediaFileList = newFileList;
      }
    }
  }
};
</script>

<style lang='scss' scoped>
.media-container {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
}

.media-list {
  position: relative;
  width: 32%;
}

.media-list:hover .delete-file,
.media-list:hover .zoom-in-file {
  display: inline-block;
}

.media-file {
  width: 100%;
  height: 148px;
  border: 1px dashed #c0ccda;
  border-radius: 5px;
  margin-right: 10px;
}

// .image-file {}
.video-file {
  object-fit: fill;
}

// .audio-file {}
.delete-file {
  cursor: pointer;
  width: 20px;
  height: 20px;
  display: none;
  font-size: 24px;
  color: #f56c6c;
  position: absolute;
  top: 5%;
  right: 15%;
}

.zoom-in-file {
  cursor: pointer;
  width: 20px;
  height: 20px;
  display: none;
  font-size: 24px;
  color: #009ad6;
  position: absolute;
  top: 5%;
  right: 38%;
}

::v-deep .hide .el-upload--picture-card {
  display: none;
}
</style>