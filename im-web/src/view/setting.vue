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
            <el-switch
              v-model="privacySettings.friendVerification"
              active-text="加我为好友时需要验证"
              inactive-text="加我为好友时需要验证"
            ></el-switch>
          </el-card>

          <el-card>
            <div slot="header">
              <h4>通知设置</h4>
            </div>
            <el-switch
              v-model="notificationSettings.newMessageSound"
              active-text="新消息提示音"
              inactive-text="新消息提示音"
            ></el-switch>
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

          <el-form ref="phoneForm" :model="phoneForm" label-width="100px" style="max-width: 500px;">
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="phoneForm.phone"
                placeholder="请输入手机号码"
                maxlength="11"
                @input="handlePhoneInput"
              ></el-input>
            </el-form-item>

            <el-form-item label="验证码" prop="code">
              <el-row :gutter="10">
                <el-col :span="16">
                  <el-input
                    v-model="phoneForm.code"
                    placeholder="请输入验证码"
                    maxlength="6"
                  ></el-input>
                </el-col>
                <el-col :span="8">
                  <el-button
                    type="text"
                    @click="getPhoneCode"
                    :disabled="phoneCodeDisabled"
                  >
                    {{ phoneCodeText }}
                  </el-button>
                </el-col>
              </el-row>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="bindPhone">绑定</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 4. 绑定邮箱页面 -->
      <div v-if="activeTab === '4'">
        <el-card v-if="!emailBindSuccess">
          <div slot="header">
            <h3>绑定邮箱</h3>
          </div>

          <el-form ref="emailForm" :model="emailForm" label-width="100px" style="max-width: 500px;">
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="emailForm.email"
                placeholder="请输入邮箱地址"
              ></el-input>
            </el-form-item>

            <el-form-item label="验证码" prop="code">
              <el-row :gutter="10">
                <el-col :span="16">
                  <el-input
                    v-model="emailForm.code"
                    placeholder="请输入验证码"
                    maxlength="6"
                  ></el-input>
                </el-col>
                <el-col :span="8">
                  <el-button
                    type="text"
                    @click="getEmailCode"
                    :disabled="emailCodeDisabled"
                  >
                    {{ emailCodeText }}
                  </el-button>
                </el-col>
              </el-row>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="bindEmail">绑定</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <div v-if="emailBindSuccess" style="text-align: center; padding: 50px 0;">
          <i class="el-icon-circle-check" style="font-size: 50px; color: #4CAF50;"></i>
          <h3 style="margin-top: 20px; color: #4CAF50;">邮箱绑定成功</h3>
          <p style="color: #666; margin-top: 10px;">您的邮箱 {{ emailForm.email }} 已成功绑定</p>
          <el-button
            style="margin-top: 30px;"
            type="primary"
            @click="resetEmailBind"
          >
            重新绑定
          </el-button>
        </div>
      </div>

      <!-- 5. 修改密码页面 -->
      <div v-if="activeTab === '5'">
        <el-card>
          <div slot="header">
            <h3>修改密码</h3>
          </div>

          <el-form ref="passwordForm" :model="passwordForm" label-width="100px" style="max-width: 500px;">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
              ></el-input>
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
              ></el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请确认新密码"
              ></el-input>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="changePassword">提交</el-button>
            </el-form-item>
          </el-form>
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

      // 隐私设置
      privacySettings: {
        friendVerification: true
      },

      // 通知设置
      notificationSettings: {
        newMessageSound: true
      },

      // 手机绑定表单数据
      phoneForm: {
        phone: '',
        code: ''
      },

      // 手机验证码相关
      phoneCodeDisabled: false,
      phoneCodeText: '获取验证码',
      phoneCountdown: 60,

      // 邮箱绑定表单数据
      emailForm: {
        email: '3872642622@qq.com',
        code: ''
      },

      // 邮箱验证码相关
      emailCodeDisabled: false,
      emailCodeText: '获取验证码',
      emailCountdown: 60,
      emailBindSuccess: false,

      // 密码修改表单数据
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
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
    },

    // 处理手机号输入，只保留数字
    handlePhoneInput(value) {
      this.phoneForm.phone = value.replace(/[^\d]/g, '');
    },

    // 获取手机验证码
    getPhoneCode() {
      if (!this.phoneForm.phone) {
        this.$message.error('请输入手机号');
        return;
      }

      if (!/^1[3-9]\d{9}$/.test(this.phoneForm.phone)) {
        this.$message.error('请输入正确的手机号');
        return;
      }

      // 开始倒计时
      this.phoneCodeDisabled = true;
      this.phoneCodeText = `${this.phoneCountdown}s后重新获取`;

      const timer = setInterval(() => {
        this.phoneCountdown--;
        this.phoneCodeText = `${this.phoneCountdown}s后重新获取`;

        if (this.phoneCountdown <= 0) {
          clearInterval(timer);
          this.phoneCodeDisabled = false;
          this.phoneCodeText = '获取验证码';
          this.phoneCountdown = 60;
        }
      }, 1000);

      this.$message.success('验证码已发送，请注意查收');
    },

    // 绑定手机
    bindPhone() {
      if (!this.phoneForm.phone) {
        this.$message.error('请输入手机号');
        return;
      }

      if (!this.phoneForm.code) {
        this.$message.error('请输入验证码');
        return;
      }

      // 模拟绑定手机成功
      this.userInfo.phone = this.phoneForm.phone;
      this.$message.success('手机号绑定成功');
      this.switchToTab('1');
    },

    // 获取邮箱验证码
    getEmailCode() {
      if (!this.emailForm.email) {
        this.$message.error('请输入邮箱');
        return;
      }

      if (!/^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/.test(this.emailForm.email)) {
        this.$message.error('请输入正确的邮箱地址');
        return;
      }

      // 开始倒计时
      this.emailCodeDisabled = true;
      this.emailCodeText = `${this.emailCountdown}s后重新获取`;

      const timer = setInterval(() => {
        this.emailCountdown--;
        this.emailCodeText = `${this.emailCountdown}s后重新获取`;

        if (this.emailCountdown <= 0) {
          clearInterval(timer);
          this.emailCodeDisabled = false;
          this.emailCodeText = '获取验证码';
          this.emailCountdown = 60;
        }
      }, 1000);

      this.$message.success('验证码已发送到邮箱，请注意查收');
    },

    // 绑定邮箱
    bindEmail() {
      if (!this.emailForm.email) {
        this.$message.error('请输入邮箱');
        return;
      }

      if (!this.emailForm.code) {
        this.$message.error('请输入验证码');
        return;
      }

      // 模拟绑定邮箱成功
      this.userInfo.email = this.emailForm.email;
      this.emailBindSuccess = true;
      this.$message.success('邮箱绑定成功');
    },

    // 重置邮箱绑定状态
    resetEmailBind() {
      this.emailBindSuccess = false;
      this.emailForm.code = '';
    },

    // 修改密码
    changePassword() {
      if (!this.passwordForm.oldPassword) {
        this.$message.error('请输入原密码');
        return;
      }

      if (!this.passwordForm.newPassword) {
        this.$message.error('请输入新密码');
        return;
      }

      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.$message.error('两次输入的密码不一致');
        return;
      }

      // 模拟修改密码成功
      this.$message.success('密码修改成功，请重新登录');
      // 清空表单
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
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