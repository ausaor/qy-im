<template>
  <el-dialog
    title="投诉"
    :visible.sync="dialogVisible"
    width="600px"
    :before-close="handleClose"
    :append-to-body="true"
    v-dialogDrag
    class="complaint-dialog"
  >
    <el-form :model="form" :rules="rules" ref="complaintForm" label-width="100px">
      <!-- 投诉对象 -->
      <el-form-item label="投诉对象">
        <span class="target-name">{{ targetName }}</span>
      </el-form-item>

      <!-- 投诉原因 -->
      <el-form-item label="投诉原因" prop="reason">
        <el-select v-model="form.reason" placeholder="请选择投诉原因" style="width: 300px">
          <el-option
            v-for="item in reasonOptions"
            :key="item"
            :label="item"
            :value="item"
          >
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 图片证据 -->
      <el-form-item label="图片证据">
        <batch-image-upload
          :action="imageUploadAction"
          :showLoading="true"
          :maxSize="maxImageSize"
          :fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/webp', 'image/gif']"
          :imageList="evidenceImageList"
          @success="handleUploadImageSuccess"
          @remove="handleRemoveImage"
          @before="handleBeforeUpload"
          ref="imageUploader"
        >
        </batch-image-upload>
        <div class="upload-tip">您上传了({{ evidenceImageList.length }}/9)</div>
      </el-form-item>

      <!-- 投诉内容 -->
      <el-form-item label="投诉内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="请输入投诉内容"
          maxlength="500"
          show-word-limit
        >
        </el-input>
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleCancel">取 消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确 认</el-button>
    </div>
  </el-dialog>
</template>

<script>
import BatchImageUpload from "@/components/common/BatchImageUpload.vue";

export default {
  name: "Complaint",
  components: {
    BatchImageUpload
  },
  props: {
    targetId: {
      type: Number,
    },
    targetName: {
      type: String,
      default: ''
    },
    targetType: {
      type: String,
      default: ''
    },
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      submitting: false,
      maxImageSize: 5 * 1024 * 1024, // 5MB
      evidenceImageList: [],
      form: {
        reason: "",
        content: ""
      },
      reasonOptions: [
        "对我造成骚扰",
        "疑似诈骗",
        "传播不良内容",
        "其他不正规行为"
      ],
      rules: {
        reason: [
          { required: true, message: "请选择投诉原因", trigger: "change" }
        ],
        content: [
          { required: true, message: "请输入投诉内容", trigger: "blur" },
          { min: 10, max: 500, message: "投诉内容长度在 10 到 500 个字符", trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      }
    },
    imageUploadAction() {
      return `/image/upload`;
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.resetForm();
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit("close");
    },
    handleCancel() {
      this.$emit("close");
    },
    resetForm() {
      this.form = {
        reason: "",
        content: ""
      };
      this.evidenceImageList = [];
      this.$refs.imageUploader && this.$refs.imageUploader.clearImages();
      this.$refs.complaintForm && this.$refs.complaintForm.resetFields();
    },
    handleUploadImageSuccess(data, file) {
      // 添加上传成功的图片URL到列表
      this.evidenceImageList.push({
        name: data.name,
        url: data.originUrl
      });
    },
    handleRemoveImage(file) {
      // 从列表中移除图片
      this.evidenceImageList = this.evidenceImageList.filter(
        item => item.url !== file.url
      );
    },
    handleBeforeUpload(file) {
      if (this.evidenceImageList.length >= 9) {
        this.$message.warning("最多只能上传9张图片");
        return false;
      }
      return true;
    },
    handleSubmit() {
      this.$refs.complaintForm.validate(valid => {
        if (valid) {
          this.submitting = true;
          const params = {
            targetId: this.targetId,
            targetName: this.targetName,
            targetType: this.targetType,
            reason: this.form.reason,
            content: this.form.content,
            evidenceImgList: this.evidenceImageList.map(item => item.url)
          };

          this.$http({
            url: "/complaint/submit",
            method: "post",
            data: params
          }).then(() => {
              this.$message.success("投诉提交成功");
              this.$emit("success");
              this.$emit("close");
          }).finally(() => {
              this.submitting = false;
          });
        }
      });
    }
  }
};
</script>

<style scoped lang="scss">
.complaint-dialog {
  ::v-deep .el-dialog__header {
    text-align: center;
    padding: 20px 20px 10px;
  }

  ::v-deep .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
  }

  ::v-deep .el-dialog__body {
    padding: 20px 30px 30px;
  }

  .target-name {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
  }

  .upload-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }

  ::v-deep .el-form-item__label {
    font-weight: 500;
  }

  .dialog-footer {
    text-align: center;
    padding-top: 10px;
  }
}
</style>