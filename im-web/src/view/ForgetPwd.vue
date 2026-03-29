<template>
  <div class="forget-pwd-container">
    <div class="forget-pwd-card">
      <div class="card-header">
        <div class="header-icon">
          <i class="el-icon-lock"></i>
        </div>
        <h2 class="title">忘记密码</h2>
        <p class="subtitle">通过邮箱验证重置您的密码</p>
      </div>

      <el-alert
        title="温馨提示"
        description="请输入您绑定的邮箱地址，我们将发送验证码到该邮箱"
        type="info"
        show-icon
        :closable="false"
        class="tip-alert"
      ></el-alert>

      <el-form 
        :model="resetForm" 
        ref="resetForm" 
        :rules="resetRules" 
        label-width="120px" 
        class="reset-form"
      >
        <el-form-item label="邮箱地址" prop="email">
          <div class="input-wrapper">
            <i class="el-icon-message input-icon"></i>
            <el-input 
              type="text" 
              v-model="resetForm.email" 
              autocomplete="off" 
              placeholder="请输入绑定的邮箱地址"
              class="custom-input"
            ></el-input>
          </div>
        </el-form-item>

        <el-form-item label="验证码" prop="emailCode">
          <div class="input-wrapper">
            <i class="el-icon-key input-icon"></i>
            <el-input 
              type="text" 
              maxlength="6" 
              placeholder="请输入验证码"
              v-model="resetForm.emailCode"
              class="custom-input"
            />
            <el-button 
              class="code-btn" 
              :disabled="codeDisabled" 
              @click="getEmailCode"
              :loading="codeLoading"
            >
              {{ codeBtnText }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <div class="input-wrapper">
            <i class="el-icon-lock input-icon"></i>
            <el-input 
              type="password" 
              v-model="resetForm.newPassword" 
              autocomplete="off" 
              show-password
              placeholder="请输入新密码（6-20 位）"
              class="custom-input"
            ></el-input>
          </div>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <div class="input-wrapper">
            <i class="el-icon-lock input-icon"></i>
            <el-input 
              type="password" 
              v-model="resetForm.confirmPassword" 
              autocomplete="off" 
              show-password
              placeholder="请再次输入新密码"
              class="custom-input"
            ></el-input>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            @click="submitReset" 
            class="submit-btn"
            :loading="submitLoading"
          >
            <i class="el-icon-check"></i> 重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <div class="back-login">
        <el-link type="primary" @click="goBack">
          <i class="el-icon-back"></i> 返回登录
        </el-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ForgetPwd",
  data() {
    // 验证邮箱
    const validateEmail = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('请输入邮箱地址'));
      }
      const regEmail = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
      if (!regEmail.test(value)) {
        return callback(new Error('邮箱格式不正确'));
      }
      callback();
    };

    // 验证密码
    const validatePassword = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('请输入新密码'));
      }
      if (value.length < 6 || value.length > 20) {
        return callback(new Error('密码长度必须在 6-20 位之间'));
      }
      callback();
    };

    // 验证确认密码
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('请再次输入新密码'));
      }
      if (value !== this.resetForm.newPassword) {
        return callback(new Error('两次输入的密码不一致'));
      }
      callback();
    };

    return {
      // 表单数据
      resetForm: {
        email: '',
        emailCode: '',
        newPassword: ''
      },
      
      // 表单验证规则
      resetRules: {
        email: [
          { required: true, validator: validateEmail, trigger: 'blur' }
        ],
        emailCode: [
          { required: true, message: '请输入验证码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, validator: validatePassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      
      // 验证码按钮状态
      codeDisabled: false,
      codeBtnText: '获取验证码',
      codeLoading: false,
      codeTimer: null, // 保存定时器引用
      
      // 提交按钮状态
      submitLoading: false
    };
  },
  methods: {
    // 获取邮箱验证码
    getEmailCode() {
      if (!this.resetForm.email) {
        this.$message.warning('请输入邮箱');
        return;
      }
      const regEmail = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
      if (!regEmail.test(this.resetForm.email)) {
        this.$message.warning('邮箱格式错误');
        return;
      }
      this.codeLoading = true;

      this.$http({
        url: "/email/getCode",
        method: "post",
        data: {
          toEmail: this.resetForm.email,
          category: 'RESET_PASSWORD'
        }
      }).then(() => {
        this.$message.success("验证码已发送到您的邮箱，请注意查收");
        this.startCountdown();
      }).catch(() => {
        this.$message.error("验证码发送失败，请重试");
      }).finally(() => {
        this.codeLoading = false;
      });
    },

    // 倒计时
    startCountdown() {
      // 先清理之前的定时器
      if (this.codeTimer) {
        clearInterval(this.codeTimer);
        this.codeTimer = null;
      }

      let time = 60;
      this.codeDisabled = true;
      
      this.codeTimer = setInterval(() => {
        if (time === 0) {
          clearInterval(this.codeTimer);
          this.codeTimer = null;
          this.codeBtnText = '获取验证码';
          this.codeDisabled = false;
        } else {
          this.codeBtnText = `${time}秒后重试`;
          time--;
        }
      }, 1000);
    },

    // 提交重置密码
    submitReset() {
      this.$refs.resetForm.validate((valid) => {
        if (!valid) {
          return;
        }

        this.submitLoading = true;

        this.$http({
          url: "/email/resetPwd",
          method: 'post',
          data: {
            email: this.resetForm.email,
            emailCode: this.resetForm.emailCode,
            newPassword: this.resetForm.newPassword
          }
        }).then(() => {
          this.$message.success("密码重置成功！");
          
          // 延迟跳转到登录页
          setTimeout(() => {
            this.goBack();
          }, 1500);
        }).catch(() => {
          this.$message.error("密码重置失败，请重试");
        }).finally(() => {
          this.submitLoading = false;
        });
      });
    },

    // 返回登录页
    goBack() {
      this.$router.push('/login');
    }
  },
  beforeDestroy() {
    // 清理验证码定时器
    if (this.codeTimer) {
      clearInterval(this.codeTimer);
      this.codeTimer = null;
    }
  }
}
</script>



<style scoped lang="scss">
.forget-pwd-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.forget-pwd-card {
  width: 100%;
  max-width: 520px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 50px 40px;
  backdrop-filter: blur(10px);
  animation: slideIn 0.5s ease-out;

  @keyframes slideIn {
    from {
      opacity: 0;
      transform: translateY(-30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.card-header {
  text-align: center;
  margin-bottom: 35px;

  .header-icon {
    width: 80px;
    height: 80px;
    margin: 0 auto 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);

    i {
      font-size: 36px;
      color: white;
    }
  }

  .title {
    font-size: 28px;
    font-weight: 600;
    color: #2d3748;
    margin: 0 0 10px 0;
    letter-spacing: 1px;
  }

  .subtitle {
    font-size: 14px;
    color: #718096;
    margin: 0;
  }
}

.tip-alert {
  margin-bottom: 30px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);

  ::v-deep .el-alert__content {
    p {
      font-size: 13px;
    }
  }
}

.reset-form {
  margin-top: 30px;

  .input-wrapper {
    display: flex;
    align-items: center;
    position: relative;

    .input-icon {
      position: absolute;
      left: 15px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 18px;
      color: #a0aec0;
      z-index: 1;
    }

    .custom-input {
      flex: 1;

      ::v-deep .el-input__inner {
        padding-left: 45px;
        height: 48px;
        border-radius: 10px;
        border: 2px solid #e2e8f0;
        font-size: 14px;
        transition: all 0.3s;
        background-color: #f8f9fa;

        &:hover {
          border-color: #cbd5e0;
        }

        &:focus {
          border-color: #667eea;
          background-color: white;
          box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
      }
    }

    .code-btn {
      margin-left: 12px;
      height: 48px;
      padding: 0 20px;
      border-radius: 10px;
      font-size: 14px;
      font-weight: 500;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      color: white;
      transition: all 0.3s;
      white-space: nowrap;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
      }

      &:disabled {
        background: linear-gradient(135deg, #cbd5e0 0%, #a0aec0 100%);
        cursor: not-allowed;
      }
    }
  }

  ::v-deep .el-form-item {
    margin-bottom: 25px;

    .el-form-item__label {
      font-size: 15px;
      font-weight: 500;
      color: #4a5568;
      line-height: 48px;
    }
  }
}

.submit-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
  }

  &:active {
    transform: translateY(0);
  }
}

.back-login {
  text-align: center;
  margin-top: 25px;
  padding-top: 25px;
  border-top: 1px solid #e2e8f0;

  ::v-deep .el-link {
    font-size: 14px;
    font-weight: 500;

    i {
      margin-right: 5px;
    }
  }
}

// 响应式设计
@media screen and (max-width: 600px) {
  .forget-pwd-card {
    padding: 35px 25px;
  }

  .card-header {
    .header-icon {
      width: 60px;
      height: 60px;

      i {
        font-size: 28px;
      }
    }

    .title {
      font-size: 24px;
    }
  }

  .reset-form {
    .input-wrapper {
      .code-btn {
        padding: 0 15px;
        font-size: 13px;
      }
    }
  }
}
</style>