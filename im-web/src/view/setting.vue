<template>
  <el-container style="min-height: 100vh;">
    <!-- 左侧导航栏 -->
    <el-aside width="200px" style="background-color: #f5f7fa; border-right: 1px solid #e4e7ed;">
      <el-menu
        :default-active="activeTab"
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
        <el-menu-item index="6">
          <i class="el-icon-key"></i>
          <span slot="title">重置密码</span>
        </el-menu-item>
        <el-menu-item index="7">
          <i class="el-icon-chat-line-round"></i>
          <span slot="title">消息气泡</span>
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
              <div class="avatar-container">
                <head-image :url="userInfo.headImage" :name="userInfo.nickName" :size="60"></head-image>
                <div class="camera-overlay">
                  <i class="el-icon-camera"></i>
                </div>
              </div>
            </file-upload>
            <div style="margin-top: 10px; font-size: 18px; font-weight: bold;">{{ userInfo.nickName }}</div>
            <div style="color: #666;">ID: {{userInfo.userName}}</div>
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
        <el-card class="setting-card">
          <div slot="header">
            <h3>用户设置</h3>
          </div>

          <el-card class="sub-card">
            <div slot="header">
              <h4>隐私设置</h4>
            </div>
            
            <div class="setting-item">
              <label>加我为好友需要验证：</label>
              <el-switch
                v-model="userInfo.friendReview"
                @change="changeFriendReview"
                active-text="开启"
                inactive-text="关闭">
              </el-switch>
            </div>
            
            <div class="setting-item">
              <label>邀我进群需要验证：</label>
              <el-switch
                v-model="userInfo.groupReview"
                @change="changeGroupReview"
                active-text="开启"
                inactive-text="关闭">
              </el-switch>
            </div>
          </el-card>

          <el-card class="sub-card">
            <div slot="header">
              <h4>通知设置</h4>
            </div>
            
            <div class="setting-item">
              <label>好友上线通知：</label>
              <el-switch
                v-model="friendOnlineNotice"
                active-text="开"
                inactive-text="关">
              </el-switch>
            </div>
            
            <div class="setting-item">
              <label>上线通知好友：</label>
              <el-switch
                v-model="onlineNoticeFriend"
                active-text="开"
                inactive-text="关">
              </el-switch>
            </div>
            
            <div class="setting-item">
              <label>消息提示音：</label>
              <el-switch
                v-model="userInfo.soundPlay"
                @change="changeSoundPlay"
                active-text="开启"
                inactive-text="关闭">
              </el-switch>
            </div>
            
            <div class="setting-item">
              <label>语音自动播放：</label>
              <el-switch
                v-model="userInfo.autoPlay"
                @change="changeAutoPlay"
                active-text="开启"
                inactive-text="关闭">
              </el-switch>
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
        <el-card>
          <div slot="header">
            <h3>绑定邮箱</h3>
          </div>
          
          <el-form :model="mailForm" ref="mailForm" :rules="mailRules" label-width="100px" class="email-bind-form">
            <el-form-item label="邮箱地址" prop="email">
              <el-input type="text" v-model="mailForm.email" autocomplete="off" placeholder="请输入邮箱地址"></el-input>
            </el-form-item>
            
            <el-form-item label="验证码" prop="emailCode">
              <div class="verify-wrapper">
                <el-input 
                  type="text" 
                  maxlength="6" 
                  suffix-icon="el-icon-lock" 
                  placeholder="验证码" 
                  v-model="mailForm.emailCode"/>
                <el-button 
                  class="verification-btn" 
                  :disabled="disabled" 
                  @click="sendVerificationCode">
                  {{validateBtn}}
                </el-button>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="bindMail('mailForm')">确认绑定</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 5. 修改密码页面 -->
      <div v-if="activeTab === '5'">
        <el-card>
          <div slot="header">
            <h3>修改密码</h3>
          </div>
          
          <el-form :model="pwdForm" status-icon :rules="pwdRules" ref="pwdForm" label-width="100px" class="password-form">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input type="password" v-model="pwdForm.oldPassword" autocomplete="off" show-password></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input type="password" v-model="pwdForm.newPassword" autocomplete="off" show-password></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPwd">
              <el-input type="password" v-model="pwdForm.confirmPwd" autocomplete="off" show-password></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="modifyPassword('pwdForm')">确认修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
      <!-- 6. 重置密码页面 -->
      <div v-if="activeTab === '6'">
        <el-card>
          <div slot="header">
            <h3>重置密码</h3>
          </div>
          
          <el-alert
            title="注意"
            description="重置密码将通过邮箱验证方式进行，请确保您的邮箱可以正常接收邮件"
            type="warning"
            show-icon
            style="margin-bottom: 20px;"
          ></el-alert>
          
          <el-form :model="resetPwdForm" ref="resetPwdForm" :rules="resetPwdRules" label-width="100px" class="reset-pwd-form">
            <el-form-item label="邮箱地址" prop="email">
              <el-input type="text" :disabled="true" v-model="userInfo.email" autocomplete="off"></el-input>
            </el-form-item>
            
            <el-form-item label="验证码" prop="emailCode">
              <div class="verify-wrapper">
                <el-input 
                  type="text" 
                  maxlength="6" 
                  suffix-icon="el-icon-lock" 
                  placeholder="验证码" 
                  v-model="resetPwdForm.emailCode"/>
                <el-button 
                  class="verification-btn" 
                  :disabled="resetDisabled" 
                  @click="getUserEmailCode">
                  {{resetValidateBtn}}
                </el-button>
              </div>
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input type="password" v-model="resetPwdForm.newPassword" autocomplete="off" show-password placeholder="请输入新密码"></el-input>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="resetPwd('resetPwdForm')">重置密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
      <div v-if="activeTab === '7'">
        <el-card>
          <div slot="header">
            <h3>消息气泡</h3>
          </div>
          <div class="bubble-selector">
            <div 
              v-for="(bubble, index) in chatBubbles" 
              :key="index" 
              class="bubble-option"
              :class="{ active: index === currentBubbleIndex }"
              @click="selectBubble(index)"
            >
              <div 
                class="bubble-preview" 
                :style="{ background: bubble.background, color: bubble.color }"
              >
                {{ bubble.name }}
              </div>
              <div class="bubble-name">{{ bubble.name }}</div>
            </div>
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
    let checkPassword = (rule, value, callback) => {
      if (value === '') {
        return callback(new Error('请输入密码'));
      }
      callback();
    };

    let checkConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        return callback(new Error('请输入密码'));
      }
      if (value !== this.pwdForm.newPassword) {
        return callback(new Error('两次密码输入不一致'));
      }
      callback();
    };

    let checkMail = (rule, value, callback) => {
      if (value === '') {
        return callback(new Error('请输入邮箱'));
      }
      const regEmail = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
      if (!regEmail.test(value)) {
        return callback(new Error('邮箱格式错误'));
      }
      callback();
    };

    let checkCode = (rule, value, callback) => {
      if (value === '') {
        return callback(new Error('请输入验证码'));
      }
      callback();
    };

    return {
      maxSize: 5*1024*1024,
      // 当前激活的标签页
      activeTab: '1',
      
      friendOnlineNotice: false,
      onlineNoticeFriend: false,
      
      // 本地userInfo副本，避免直接修改Vuex状态
      localUserInfo: {
        friendReview: false,
        groupReview: false,
        soundPlay: false,
        autoPlay: false
      },
      
      // 邮箱绑定相关数据
      mailForm: {
        email: '',
        emailCode: ''
      },
      validateBtn: '获取验证码',
      disabled: false,
      mailRules: {
        email: [{
          required: true,
          validator: checkMail,
          trigger: 'blur'
        }],
        emailCode: [{
          required: true,
          validator: checkCode,
          trigger: 'blur'
        }]
      },
      
      // 修改密码相关数据
      pwdForm: {
        oldPassword: '',
        newPassword: '',
        confirmPwd: ''
      },
      pwdRules: {
        oldPassword: [{
          required: true,
          validator: checkPassword,
          trigger: 'blur'
        }],
        newPassword: [{
          required: true,
          validator: checkPassword,
          trigger: 'blur'
        }],
        confirmPwd: [{
          required: true,
          validator: checkConfirmPassword,
          trigger: 'blur'
        }]
      },
      
      // 重置密码相关数据
      resetPwdForm: {
        emailCode: '',
        newPassword: ''
      },
      resetValidateBtn: '获取验证码',
      resetDisabled: false,
      resetPwdRules: {
        emailCode: [{
          required: true,
          validator: checkCode,
          trigger: 'blur'
        }],
        newPassword: [{
          required: true,
          validator: checkPassword,
          trigger: 'blur'
        }]
      }
    };
  },
  watch: {
    userInfo: {
      handler(newVal) {
        // 初始化开关状态
        if (newVal) {
          this.friendOnlineNotice = newVal.friendOnlineNotice || false;
          this.onlineNoticeFriend = newVal.onlineNoticeFriend || false;
          // 复制userInfo对象，避免直接修改Vuex状态
          this.localUserInfo = { ...newVal };
        }
      },
      immediate: true
    }
  },
  computed: {
    userInfo() {
      return this.$store.state.userStore.userInfo;
    },
    imageAction(){
      return `/image/upload`;
    },
    chatBubbles() {
      return this.$store.state.uiStore.chatBubble.bubbles;
    },
    currentBubbleIndex() {
      return this.$store.state.uiStore.chatBubble.currentBubbleIndex;
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
    
    // 邮箱绑定相关方法
    bindMail(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.$http({
            url: "/user/bindEmail",
            method: 'post',
            data: this.mailForm
          }).then((data) => {
            this.$message.success("绑定成功!");
            // 更新用户信息
            this.$store.state.userStore.userInfo.email = this.mailForm.email;
          }).catch(() => {
            this.$message.error("绑定失败，请重试");
          })
        }
      });
    },
    
    sendVerificationCode() {
      if (!this.mailForm.email) {
        this.$message.warning('请输入邮箱');
        return;
      }
      const regEmail = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
      if (!regEmail.test(this.mailForm.email)) {
        this.$message.warning('邮箱格式错误');
        return;
      }
      
      let time = 60;
      let timer = setInterval(() => {
        if(time === 0){
          clearInterval(timer);
          this.validateBtn = '获取验证码';
          this.disabled = false;
        }else{
          this.disabled = true;
          this.validateBtn = time + '秒后重试';
          time--;
        }
      }, 1000);

      this.getCode();
    },
    
    getCode() {
      let params = {
        toEmail: this.mailForm.email,
        category: 'BIND_EMAIL'
      }
      this.$http({
        url: "/email/getCode",
        method: "post",
        data: params
      }).then(()=>{
        this.$message.success("验证码已发送");
      }).catch(() => {
        this.$message.error("验证码发送失败");
      })
    },
    
    // 修改密码相关方法
    modifyPassword(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.$http({
            url: "/user/modifyPassword",
            method: 'post',
            data: this.pwdForm
          }).then((data) => {
            this.$message.success("修改成功!");
            this.$wsApi.closeWebSocket();
            sessionStorage.removeItem("token");
            location.href = "/";
          }).catch(() => {
          })
        }
      });
    },
    
    // 重置密码相关方法
    getUserEmailCode() {
      let time = 60;
      let timer = setInterval(() => {
        if(time === 0){
          clearInterval(timer);
          this.resetValidateBtn = '获取验证码';
          this.resetDisabled = false;
        }else{
          this.resetDisabled = true;
          this.resetValidateBtn = time + '秒后重试';
          time--;
        }
      }, 1000);

      this.$http({
        url: "/user/getEmailCode?emailCategory=RESET_PASSWORD",
        method: "get",
      }).then(()=>{
        this.$message.success("验证码已发送");
      }).catch(() => {
        this.$message.error("验证码发送失败");
      })
    },
    
    resetPwd(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.$http({
            url: "/user/resetPwd",
            method: 'post',
            data: this.resetPwdForm
          }).then((data) => {
            this.$message.success("重置成功!");
            this.onExit();
          }).catch(() => {
            this.$message.error("重置失败");
          })
        }
      });
    },
    
    onExit() {
      this.$wsApi.close(3000);
      sessionStorage.removeItem("accessToken");
      sessionStorage.removeItem("refreshToken");
      location.href = "/";
    },
    
    // 开关项相关方法 (从Operation.vue迁移过来)
    changeFriendReview(value) {
      this.userInfo.friendReview = value;
      this.updateUserInfo();
    },
    changeGroupReview(value) {
      this.userInfo.groupReview = value;
      this.updateUserInfo();
    },
    changeSoundPlay(value) {
      this.userInfo.soundPlay = value;
      this.updateUserInfo();
    },
    changeAutoPlay(value) {
      this.userInfo.autoPlay = value;
      this.updateUserInfo();
    },
    updateUserInfo() {
      this.$http({
        url: "/user/update",
        method: "put",
        data: this.userInfo
      }).then(()=>{
        this.$store.commit("setUserInfo", this.userInfo);
        this.$message.success("操作成功");
      })
    },
    selectBubble(index) {
      this.$store.commit("setChatBubbleIndex", index);
      this.$message.success("消息气泡设置已保存");
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
  
  .avatar-container {
    position: relative;
    display: inline-block;
    
    .camera-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.5);
      border-radius: 50%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      opacity: 0;
      transition: opacity 0.3s ease;
      
      .el-icon-camera {
        font-size: 24px;
        color: white;
        margin-bottom: 4px;
      }
      
      .upload-text {
        font-size: 12px;
        color: white;
      }
    }
    
    &:hover .camera-overlay {
      opacity: 1;
    }
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
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
  }
  
  ::v-deep .el-card__header {
    background-color: #f8f9fa;
    border-bottom: 1px solid #ebeef5;
    padding: 15px 20px;
    border-top-left-radius: 8px;
    border-top-right-radius: 8px;
    
    h3, h4 {
      margin: 0;
      color: #303133;
      font-weight: 600;
    }
    
    h3 {
      font-size: 18px;
    }
    
    h4 {
      font-size: 16px;
    }
  }
  
  ::v-deep .el-card__body {
    padding: 20px;
  }
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
  
  &:last-child {
    border-bottom: none;
  }
  
  label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }
  
  ::v-deep .el-switch {
    .el-switch__core {
      width: 40px !important;
    }
    
    &.is-checked .el-switch__core {
      border-color: #409eff;
      background-color: #409eff;
    }
  }
}

// 用户设置样式增强
.setting-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: none;
  
  &:hover {
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
  }
  
  ::v-deep .el-card__header {
    background: linear-gradient(120deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: 1px solid #e1e5eb;
    padding: 20px;
    border-top-left-radius: 12px;
    border-top-right-radius: 12px;
    
    h3, h4 {
      margin: 0;
      color: #212529;
      font-weight: 600;
    }
    
    h3 {
      font-size: 20px;
      letter-spacing: 0.5px;
    }
    
    h4 {
      font-size: 17px;
    }
  }
  
  ::v-deep .el-card__body {
    padding: 25px;
  }
}

.sub-card {
  margin-bottom: 25px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    border-color: #dcdfe6;
  }
  
  ::v-deep .el-card__header {
    background-color: #f8f9fa;
    border-bottom: 1px solid #ebeef5;
    padding: 16px 20px;
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
  }
  
  ::v-deep .el-card__body {
    padding: 20px;
  }
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 15px;
  border-bottom: 1px solid #f2f4f7;
  transition: all 0.2s ease;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background-color: #f8f9fa;
    border-radius: 6px;
    padding: 18px 20px;
    margin: 0 -5px;
  }
  
  label {
    font-size: 15px;
    color: #495057;
    font-weight: 500;
    margin-right: 15px;
  }
  
  ::v-deep .el-switch {
    .el-switch__core {
      width: 46px !important;
      height: 24px;
      border-radius: 12px;
    }
    
    .el-switch__core::after {
      width: 20px;
      height: 20px;
      border-radius: 50%;
    }
    
    &.is-checked .el-switch__core {
      border-color: #409eff;
      background-color: #409eff;
    }
    
    &.is-checked .el-switch__core::after {
      margin-left: -21px;
    }
    
    .el-switch__label {
      font-size: 13px;
      font-weight: 500;
      
      &.is-active {
        color: #409eff;
      }
    }
  }
}

// 左侧导航栏样式增强
.el-aside {
  box-shadow: 4px 0 12px rgba(29, 35, 41, 0.07);
  border-right: none !important;
  height: 100%;
  border-radius: 12px 0 0 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fa 100%);
}

::v-deep .el-menu {
  border-right: none !important;
  height: 100%;
  border-radius: 12px 0 0 12px;
}

::v-deep .el-menu-item {
  height: 56px;
  line-height: 56px;
  transition: all 0.3s;
  margin: 5px 10px;
  border-radius: 8px;
  padding-left: 25px !important;
  
  i {
    color: #6c757d;
    margin-right: 12px;
    font-size: 17px;
    width: 20px;
    text-align: center;
  }
  
  span {
    font-size: 15px;
    color: #495057;
  }
  
  &:hover {
    background: linear-gradient(120deg, #e3f2fd 0%, #bbdefb 100%);
    
    i, span {
      color: #1890ff;
    }
  }
  
  &.is-active {
    background: linear-gradient(120deg, #e3f2fd 0%, #bbdefb 100%);
    box-shadow: 0 2px 6px rgba(32, 119, 205, 0.15);
    
    i, span {
      color: #1890ff;
      font-weight: 500;
    }
  }
}

// 主内容区样式增强
.el-main {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7f1 100%);
  min-height: 100vh;
  padding: 25px !important;
}

// 页面标题样式
::v-deep .el-card__header h3, 
::v-deep .el-card__header h4 {
  position: relative;
  padding-left: 15px;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 3px;
    height: 22px;
    width: 5px;
    background: linear-gradient(to bottom, #409eff, #1890ff);
    border-radius: 3px;
  }
}

// 邮箱绑定样式
.email-bind-form {
  max-width: 500px;
  margin: 20px auto;
  
  .verify-wrapper {
    display: flex;
    gap: 10px;
    
    .el-input {
      flex: 1;
    }
    
    .verification-btn {
      width: 120px;
    }
  }
  
  .el-form-item {
    margin-bottom: 25px;
  }
}

// 修改密码样式
.password-form {
  max-width: 500px;
  margin: 20px auto;
    
  .el-form-item {
    margin-bottom: 25px;
  }
}

// 重置密码样式
.reset-pwd-form {
  max-width: 500px;
  margin: 20px auto;
    
  .verify-wrapper {
    display: flex;
    gap: 10px;
    
    .el-input {
      flex: 1;
    }
    
    .verification-btn {
      width: 120px;
    }
  }
    
  .el-form-item {
    margin-bottom: 25px;
  }
}

// 消息气泡样式
.bubble-selector {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 20px;
  padding: 20px;
}

.bubble-option {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
  border-radius: 8px;
  padding: 10px;
  
  &:hover {
    transform: translateY(-5px);
    background-color: #f5f7fa;
  }
  
  &.active {
    background-color: #e3f2fd;
    box-shadow: 0 2px 8px rgba(32, 119, 205, 0.2);
  }
}

.bubble-preview {
  padding: 12px 8px;
  border-radius: 12px;
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 14px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.bubble-name {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}
</style>