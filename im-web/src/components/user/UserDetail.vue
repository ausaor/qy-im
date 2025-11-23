<template>
  <el-dialog
      title="个人资料"
      v-dialogDrag
      :visible.sync="visible"
      :close-on-click-modal="false"
      width="420px"
      center
      :before-close="onClose"
  >
    <div class="profile-container">
      <!-- 头像和基本信息 -->
      <div class="profile-header">
        <div class="avatar">
          <head-image :url="userInfo.headImage" :name="userInfo.nickName" :online="userInfo.online" :size="80"></head-image>
        </div>
        <div class="user-info">
          <h3 class="username">{{userInfo.nickName}}</h3>
          <p class="user-id">ID: {{userInfo.userName}}</p>
          <div class="online-status">
            <span class="status-indicator"></span>
            <span class="status-text">在线</span>
          </div>
        </div>
      </div>

      <!-- 详细信息列表 -->
      <div class="profile-details">
        <div class="detail-item">
          <i class="el-icon-user"></i>
          <span class="label">性别</span>
          <span class="value">{{userInfo.sex===0 ? "男":"女"}}</span>
        </div>

        <div class="detail-item">
          <i class="el-icon-message"></i>
          <span class="label">邮箱</span>
          <span class="value">{{userInfo.email}}</span>
        </div>

        <div class="detail-item signature">
          <div class="signature-label">
            <i class="el-icon-edit"></i>
            <span class="label">个性签名</span>
          </div>
          <div class="signature-value">{{userInfo.signature}}</div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import HeadImage from "@components/common/HeadImage.vue";

export default {
  name: "UserInfo",
  components: {
    HeadImage
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
    }
  },
  methods: {
    onClose() {
      this.$emit("close");
    }
  },
  computed: {
    userInfo() {
      return this.$store.state.userStore.userInfo;
    }
  }
}
</script>



<style scoped lang="scss">
.profile-container {
  padding: 10px 0;
  background: linear-gradient(135deg, #f8f9fa, #fff);
  border-radius: 12px;
  overflow: hidden;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 10px;
}

.avatar {
  margin-right: 15px;
}

.username {
  margin: 0 0 5px 0;
  font-size: 18px;
  font-weight: 600;
}

.user-id {
  margin: 0 0 5px 0;
  color: #666;
  font-size: 14px;
}

.online-status {
  display: flex;
  align-items: center;
}

.status-indicator {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #4cd964;
  margin-right: 5px;
}

.status-text {
  color: #666;
  font-size: 14px;
}

.profile-details {
  width: 100%;
  padding: 0 5px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all .3s ease;
  border-radius: 6px;
}

.detail-item:hover {
  background-color: #f4f4fc;
  transform: translateY(2px);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item i {
  color: #409eff;
  margin-right: 10px;
  width: 20px;
  text-align: center;
}

.label {
  color: #666;
  width: 60px;
  font-weight: 600;
}

.value {
  flex: 1;
  color: #333;
}

.signature {
  align-items: flex-start;
  flex-direction: column;
  padding: 16px 0;
}

.signature-label {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #555;
  font-size: 14px;
  margin-bottom: 12px;
}

.signature-value {
  width: 100%;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  font-style: italic;
  padding: 10px;
  border-radius: 12px;
  word-break: break-word;
  position: relative;
  box-sizing: border-box;
}
</style>