<template>
  <div class="setting-container">
    <el-container>
      <el-aside width="200px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="notify">
            <i class="el-icon-setting"></i>
            <span slot="title">开关项</span>
          </el-menu-item>
          <el-menu-item index="modifyPwd">
            <i class="el-icon-key"></i>
            <span slot="title">修改密码</span>
          </el-menu-item>
          <el-menu-item index="resetPwd">
            <i class="el-icon-refresh"></i>
            <span slot="title">重置密码</span>
          </el-menu-item>
          <el-menu-item index="email">
            <i class="el-icon-message"></i>
            <span slot="title">邮箱绑定</span>
          </el-menu-item>
          <el-menu-item index="sourceCode">
            <i class="el-icon-document"></i>
            <span slot="title">项目源码</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main-content">
        <!-- 开关项 -->
        <div v-show="activeMenu === 'notify'" class="content-card">
          <div class="card-header">
            <h2>开关项设置</h2>
          </div>
          <div class="card-body">
            <el-row>
              <el-col :span="24" class="setting-item">
                <label>加我为好友需要验证：</label>
                <el-switch
                  v-model="userInfo.friendReview"
                  @change="changeFriendReview"
                  active-text="开启"
                  inactive-text="关闭">
                </el-switch>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24" class="setting-item">
                <label>邀我进群需要验证：</label>
                <el-switch
                  v-model="userInfo.groupReview"
                  @change="changeGroupReview"
                  active-text="开启"
                  inactive-text="关闭">
                </el-switch>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24" class="setting-item">
                <label>好友上线通知：</label>
                <el-switch
                  v-model="friendOnlineNotice"
                  active-text="开"
                  inactive-text="关">
                </el-switch>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24" class="setting-item">
                <label>上线通知好友：</label>
                <el-switch
                  v-model="onlineNoticeFriend"
                  active-text="开"
                  inactive-text="关">
                </el-switch>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24" class="setting-item">
                <label>消息提示音：</label>
                <el-switch
                  v-model="userInfo.soundPlay"
                  @change="changeSoundPlay"
                  active-text="开启"
                  inactive-text="关闭">
                </el-switch>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24" class="setting-item">
                <label>语音自动播放：</label>
                <el-switch
                  v-model="userInfo.autoPlay"
                  @change="changeAutoPlay"
                  active-text="开启"
                  inactive-text="关闭">
                </el-switch>
              </el-col>
            </el-row>
          </div>
        </div>

        <!-- 修改密码 -->
        <div v-show="activeMenu === 'modifyPwd'" class="content-card">
          <div class="card-header">
            <h2>修改密码</h2>
          </div>
          <div class="card-body">
            <el-form :model="pwdForm" status-icon :rules="rules" ref="pwdForm" label-width="100px">
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input type="password" v-model="pwdForm.oldPassword" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input type="password" v-model="pwdForm.newPassWord" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPwd">
                <el-input type="password" v-model="pwdForm.confirmPwd" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="modifyPassword('pwdForm')">确认修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 重置密码 -->
        <div v-show="activeMenu === 'resetPwd'" class="content-card">
          <div class="card-header">
            <h2>重置密码</h2>
          </div>
          <div class="card-body">
            <el-form :model="resetPwdForm" ref="resetPwdForm" :rules="resetPwdRules" label-width="100px">
              <el-form-item label="邮箱地址" prop="mail">
                <el-input type="text" :disabled="true" v-model="mine.email" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="验证码" prop="code">
                <div class="verify-wrapper">
                  <el-input 
                    type="text" 
                    maxlength="6" 
                    placeholder="验证码" 
                    v-model="resetPwdForm.emailCode"/>
                  <el-button 
                    class="verification-btn" 
                    :disabled="disabled" 
                    @click="getUserEmailCode">
                    {{validateBtn}}
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input type="password" v-model="resetPwdForm.newPassword" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="resetPwd('resetPwdForm')">确认重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 邮箱绑定 -->
        <div v-show="activeMenu === 'email'" class="content-card">
          <div class="card-header">
            <h2>邮箱绑定</h2>
          </div>
          <div class="card-body">
            <el-form :model="mailForm" ref="mailForm" :rules="mailRules" label-width="100px">
              <el-form-item label="邮箱地址" prop="mail">
                <el-input type="text" v-model="mailForm.email" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="验证码" prop="code">
                <div class="verify-wrapper">
                  <el-input 
                    type="text" 
                    maxlength="6" 
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
          </div>
        </div>

        <!-- 项目源码 -->
        <div v-show="activeMenu === 'sourceCode'" class="content-card">
          <div class="card-header">
            <h2>项目源码</h2>
          </div>
          <div class="card-body source-code">
            <p>你可以在以下链接获取本项目的完整源码：</p>
            <div class="source-link">
              <strong>Gitee:</strong>&nbsp;&nbsp;
              <a href="https://gitee.com/auraor/qy-im.git" target="_blank">
                https://gitee.com/auraor/qy-im.git
              </a>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "setting",
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
      if (value !== this.pwdForm.newPassWord) {
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
      activeMenu: 'notify',
      friendOnlineNotice: false,
      onlineNoticeFriend: false,
      pwdForm: {
        oldPassword: '',
        newPassWord: '',
        confirmPwd: ''
      },
      mailForm: {
        email: '',
        emailCode: ''
      },
      resetPwdForm: {
        emailCode: '',
        newPassword: ''
      },
      validateBtn: '获取验证码',
      disabled: false,
      rules: {
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
        }],
      },
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
        }],
      },
      userInfo: {}
    }
  },
  methods: {
    handleMenuSelect(key) {
      this.activeMenu = key;
    },
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
    bindMail(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.$http({
            url: "/user/bindEmail",
            method: 'post',
            data: this.mailForm
          }).then((data) => {
            this.$message.success("绑定成功!");
          })
        }
      });
    },
    getUserEmailCode() {
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

      this.$http({
        url: "/user/getEmailCode?emailCategory=RESET_PASSWORD",
        method: "get",
      }).then(()=>{
      })
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
          })
        }
      });
    },
    onExit() {
      this.$wsApi.close(3000);
      this.removeToken();
      location.href = "/";
    },
    removeToken() {
      sessionStorage.removeItem("accessToken");
      sessionStorage.removeItem("refreshToken");
    },
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
        this.$store.commit("setUserInfo",this.userInfo);
        this.$message.success("操作成功");
      })
    }
  },
  computed: {
    mine() {
      return this.$store.state.userStore.userInfo;
    },
  },
  mounted() {
    // 初始化用户信息
    let mine = this.$store.state.userStore.userInfo;
    this.userInfo = JSON.parse(JSON.stringify(mine));
  }
}
</script>

<style scoped lang="scss">
.setting-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 80px);
}

.sidebar {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: fit-content;
  
  .menu {
    border: none;
    
    .el-menu-item {
      height: 50px;
      line-height: 50px;
      
      &.is-active {
        background-color: #ecf5ff;
        color: #409eff;
        border-right: 3px solid #409eff;
      }
      
      i {
        margin-right: 10px;
        color: #909399;
      }
    }
  }
}

.main-content {
  padding: 0 20px;
  
  .content-card {
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow: hidden;
    
    .card-header {
      padding: 20px;
      border-bottom: 1px solid #ebeef5;
      
      h2 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }
    
    .card-body {
      padding: 20px;
      
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
        }
      }
      
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
      
      .source-code {
        p {
          color: #606266;
          margin-bottom: 20px;
        }
        
        .source-link {
          padding: 15px;
          background-color: #f4f4f5;
          border-radius: 4px;
          
          a {
            color: #409eff;
            text-decoration: none;
            
            &:hover {
              text-decoration: underline;
            }
          }
        }
      }
    }
  }
}

.el-row {
  margin-bottom: 15px;
}

.btn-orange {
  background-color: #ffa500;
  border-color: #ffa500;
  color: white;
}
</style>