<template>
  <el-container style="min-height: 100vh;">
    <!-- 左侧导航栏 -->
    <el-aside width="200px" style="background-color: #f5f7fa; border-right: 1px solid #e4e7ed;">
      <el-menu
        default-active="1"
        class="el-menu-vertical-demo"
        @select="handleMenuSelect"
        background-color="#f5f7fa"
        text-color="#333"
        active-text-color="#1890ff"
      >
        <el-menu-item index="1">
          <i class="el-icon-user"></i>
          <span slot="title">个人资料</span>
        </el-menu-item>
        <el-menu-item index="2">
          <i class="el-icon-setting"></i>
          <span slot="title">用户设置</span>
        </el-menu-item>
        <el-menu-item index="3">
          <i class="el-icon-mobile-phone"></i>
          <span slot="title">绑定手机</span>
        </el-menu-item>
        <el-menu-item index="4">
          <i class="el-icon-message"></i>
          <span slot="title">绑定邮箱</span>
        </el-menu-item>
        <el-menu-item index="5">
          <i class="el-icon-lock"></i>
          <span slot="title">修改密码</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-main style="padding: 20px;">
      <!-- 1. 个人资料页面 -->
      <div v-if="activeTab === '1'">
        <el-card>
          <div slot="header" style="display: flex; justify-content: space-between; align-items: center;">
            <h3>个人资料</h3>
          </div>

          <div style="text-align: center; margin: 20px 0;">
            <file-upload  class="avatar-uploader"
                          :action="imageAction"
                          :showLoading="true"
                          :maxSize="maxSize"
                          @success="onUploadSuccess"
                          :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
              <head-image :url="userInfo.headImage" :name="userInfo.nickName" :size="60"></head-image>
            </file-upload>
            <div style="margin-top: 10px; font-size: 18px; font-weight: bold;">轻语</div>
            <div style="color: #666;">ID: qingyu</div>
          </div>

          <el-form ref="profileForm" :model="userInfo" label-width="100px" style="margin-top: 30px;">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="userInfo.nickName" maxlength="32"></el-input>
            </el-form-item>

            <el-form-item label="性别">
              <el-radio-group v-model="userInfo.sex">
                <el-radio :label="0">男</el-radio>
                <el-radio :label="1">女</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="手机号">
              <el-input v-model="userInfo.phone" disabled placeholder="未绑定手机号">
                <template slot="append">
                  <el-button type="text" @click="switchToTab('3')">去绑定</el-button>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input v-model="userInfo.email" disabled></el-input>
            </el-form-item>

            <el-form-item label="个性签名">
              <el-input
                v-model="userInfo.signature"
                type="textarea"
                rows="4"
                maxlength="64"
                placeholder="分享你的心情和想法..."
              ></el-input>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitProfile">提交</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 2. 用户设置页面 -->
      <div v-if="activeTab === '2'">
        <el-card>
          <div slot="header">
            <h3>用户设置</h3>
          </div>

          <el-card style="margin-bottom: 20px;">
            <div slot="header">
              <h4>隐私设置</h4>
            </div>

          </el-card>

          <el-card>
            <div slot="header">
              <h4>通知设置</h4>
            </div>
          </el-card>
        </el-card>
      </div>

      <!-- 3. 绑定手机页面 -->
      <div v-if="activeTab === '3'">
        <el-card>
          <div slot="header">
            <h3>绑定手机</h3>
          </div>

          <el-alert
            title="建议绑定手机号"
            message="绑定手机号后，您可以通过手机号快速重置密码，提高账户安全性"
            type="info"
            show-icon
            style="margin-bottom: 20px;"
          ></el-alert>
        </el-card>
      </div>

      <!-- 4. 绑定邮箱页面 -->
      <div v-if="activeTab === '4'">
        <el-card v-if="!emailBindSuccess">
          <div slot="header">
            <h3>绑定邮箱</h3>
          </div>
        </el-card>
      </div>

      <!-- 5. 修改密码页面 -->
      <div v-if="activeTab === '5'">
        <el-card>
          <div slot="header">
            <h3>修改密码</h3>
          </div>
        </el-card>
      </div>
    </el-main>
  </el-container>
</template>

<script>
import FileUpload from "@components/common/FileUpload.vue";
import HeadImage from "@components/common/HeadImage.vue";

export default {
  components: {
    HeadImage,
    FileUpload
  },
  data() {
    return {
      maxSize: 5*1024*1024,
      // 当前激活的标签页
      activeTab: '1',
    };
  },
  computed: {
    userInfo() {
      return this.$store.state.userStore.userInfo;
    },
    imageAction(){
      return `/image/upload`;
    }
  },
  methods: {
    onUploadSuccess(data, file) {
      this.userInfo.headImage = data.originUrl;
      this.userInfo.headImageThumb = data.thumbUrl;
    },
    // 处理菜单选择
    handleMenuSelect(key) {
      this.activeTab = key;
    },

    // 切换到指定标签页
    switchToTab(tab) {
      this.activeTab = tab;
    },

    // 提交个人资料
    submitProfile() {
      this.$refs.profileForm.validate((valid) => {
        if (valid) {
          this.$http({
            url: "/user/update",
            method: "put",
            data: this.userInfo
          }).then(()=>{
            this.$store.commit("setUserInfo", this.userInfo);
            this.$emit("close");
            this.$message.success('个人资料修改成功');
          })
        } else {
          this.$message.error('请完善个人资料信息');
          return false;
        }
      });
    }
  }
};
</script>

<style scoped lang="scss">
.avatar-uploader {

  .el-upload {
    border: 1px dashed #d9d9d9 !important;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }

  .el-upload:hover {
    border-color: #409EFF;
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }

  .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}

.el-card {
  margin-bottom: 20px;
}

.el-form {
  margin-top: 20px;
}
</style>