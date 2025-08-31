<template>
  <el-dialog class="setting" title="操作" :visible.sync="visible"  width="500px" :before-close="handleClose">
    <el-container>
      <el-tabs v-moel="activeTab" tab-position="left" style="height: 360px;" @tab-click="handleTabClick">
        <el-tab-pane label="开关项" name="notify">
          <el-row>
            <el-col :span="24"><label>加我为好友需要验证：</label><el-switch
                v-model="userInfo.friendReview"
                @change="changeFriendReview"
                active-text="开启"
                inactive-text="关闭">
            </el-switch></el-col>
          </el-row>
          <el-row>
            <el-col :span="24"><label>好友上线通知：</label><el-switch
                v-model="friendOnlineNotice"
                active-text="开"
                inactive-text="关">
            </el-switch></el-col>
          </el-row>
          <el-row>
            <el-col :span="24"><label>上线通知好友：</label><el-switch
                v-model="onlineNoticeFriend"
                active-text="开"
                inactive-text="关">
            </el-switch></el-col>
          </el-row>
          <el-row>
            <el-col :span="24"><label>消息提示音：</label><el-switch
                v-model="userInfo.soundPlay"
                @change="changeSoundPlay"
                active-text="开启"
                inactive-text="关闭">
            </el-switch></el-col>
          </el-row>
          <el-row>
            <el-col :span="24"><label>语音自动播放：</label><el-switch
                v-model="userInfo.autoPlay"
                @change="changeAutoPlay"
                active-text="开启"
                inactive-text="关闭">
            </el-switch></el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="修改密码" name="modifyPwd">
          <el-form :model="pwdForm" status-icon :rules="rules" ref="pwdForm" label-width="80px">
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
              <el-button type="success" @click="modifyPassword('pwdForm')">确认</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="重置密码" name="resetPwd">
          <el-form :model="resetPwdForm" ref="resetPwdForm" :rules="resetPwdRules" label-width="80px">
            <el-form-item label="邮箱地址" prop="mail">
              <el-input type="text" :disabled="true" v-model="mine.email" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="verify-wrapper">
                <el-input type="text" maxlength="6" suffix-icon="el-icon-lock" placeholder="验证码" v-model="resetPwdForm.emailCode"/>
                <el-button class="btn-orange" :disabled="disabled" @click="getUserEmailCode">{{validateBtn}}</el-button>
              </div>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input type="password" v-model="resetPwdForm.newPassword" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="resetPwd('resetPwdForm')">确认</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="邮箱绑定" name="email">
          <el-form :model="mailForm" ref="mailForm" :rules="mailRules" label-width="80px" class="content">
            <el-form-item label="邮箱地址" prop="mail">
              <el-input type="text" v-model="mailForm.email" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="verify-wrapper">
                <el-input type="text" maxlength="6" suffix-icon="el-icon-lock" placeholder="验证码" v-model="mailForm.emailCode"/>
                <el-button class="btn-orange" :disabled="disabled" @click="sendVerificationCode">{{validateBtn}}</el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="bindMail('mailForm')">确认</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="项目源码" name="sourceCode">
          <div>
            Gitee&nbsp;&nbsp;<a href="https://gitee.com/auraor/qy-im.git" target="_blank">https://gitee.com/auraor/qy-im.git</a>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-container>
  </el-dialog>
</template>

<script>
export default {
  name: "Operation",
  props: {
    visible: {
      type: Boolean
    }
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
      friendOnlineNotice: false,
      onlineNoticeFriend: false,
      activeTab: 'notify',
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
    handleClose() {
      this.$emit("close");
    },
    handleTabClick(tab, event) {
      this.activeTab = tab.name;
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
  watch: {
    visible: function(newData, oldData) {
      // 深拷贝
      if (newData) {
        let mine = this.$store.state.userStore.userInfo;
        this.userInfo = JSON.parse(JSON.stringify(mine));
      }
    }
  }
}
</script>

<style scoped lang="scss">
.verify-wrapper {
  display: flex;
  justify-content: space-between;
}

.el-row {
  margin-bottom: 15px;
}
</style>