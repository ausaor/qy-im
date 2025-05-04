<template>
  <el-dialog class="setting" title="操作" :visible.sync="visible"  width="500px" :before-close="handleClose">
    <el-container>
      <el-tabs v-moel="activeTab" tab-position="left" style="height: 360px;" @tab-click="handleTabClick">
        <el-tab-pane label="消息通知" name="notify">
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
              <el-input type="text" v-model="resetPwdForm.mail" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="verify-wrapper">
                <el-input type="text" maxlength="6" suffix-icon="el-icon-lock" placeholder="验证码" v-model="resetPwdForm.code"/>
                <el-button class="btn-orange" :disabled="disabled" @click="sendVerificationCode">{{validateBtn}}</el-button>
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
              <el-input type="text" v-model="mailForm.mail" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="verify-wrapper">
                <el-input type="text" maxlength="6" suffix-icon="el-icon-lock" placeholder="验证码" v-model="mailForm.code"/>
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
        mail: '',
        code: ''
      },
      resetPwdForm: {
        mail: '',
        code: '',
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
        mail: [{
          required: true,
          validator: checkMail,
          trigger: 'blur'
        }],
        code: [{
          required: true,
          validator: checkCode,
          trigger: 'blur'
        }]
      },
      resetPwdRules: {
        mail: [{
          required: true,
          validator: checkMail,
          trigger: 'blur'
        }],
        code: [{
          required: true,
          validator: checkCode,
          trigger: 'blur'
        }],
        newPassword: [{
          required: true,
          validator: checkPassword,
          trigger: 'blur'
        }],
      }
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

      });
    },
    sendVerificationCode() {
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

    },
    resetPwd() {

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