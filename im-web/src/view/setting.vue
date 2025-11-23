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
</style>