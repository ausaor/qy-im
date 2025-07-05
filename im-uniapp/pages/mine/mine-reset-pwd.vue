<template>
  <view  class="page mine-reset-pwd">
    <nav-bar back>重置密码</nav-bar>
    <uni-card :is-shadow="false" is-full :border="false">
      <uni-forms ref="formRef" :model="formData" :rules="rules">
        <!-- 邮箱输入框 -->
        <uni-forms-item label="" name="email" label-width="0px">
          <uni-easyinput
              v-model="mine.email"
              disabled
              type="text"
          />
        </uni-forms-item>
        <uni-forms-item label="" name="newPassword" label-width="0px">
          <uni-easyinput
              v-model="formData.newPassword"
              placeholder="请输入新密码"
              type="text"
          />
        </uni-forms-item>
        <uni-forms-item label="" name="emailCode" label-width="0px">
          <view class="code-input">
            <uni-easyinput
                v-model="formData.emailCode"
                placeholder="请输入验证码"
                type="text"
            />
            <button
                class="send-btn"
                :class="{ disabled: isCounting }"
                :disabled="isCounting"
                @click="getEmailCode"
            >
              {{ countdownText }}
            </button>
          </view>
        </uni-forms-item>
      </uni-forms>

      <!-- 提交按钮 -->
      <button class="submit-btn" type="primary" @click="submitForm">确认</button>
    </uni-card>
  </view>
</template>

<script>
export default {
  name: "ming-reset-pwd",
  data() {
    return {
      formData: {
        emailCode: "",
        newPassword: ''
      },
      // 表单验证规则
      rules: {
        emailCode: {
          rules: [
            { required: true, errorMessage: '验证码不能为空' },
            { minLength: 4, maxLength: 6, errorMessage: '验证码格式不正确' }
          ]
        },
        newPassword: {
          rules: [
            { required: true, errorMessage: '新密码不能为空' },
            { minLength: 5, maxLength: 20, errorMessage: '密码长度在5~20个字符' }
          ]
        }
      },
      // 倒计时相关
      isCounting: false,
      countdown: 60,
      timer: null
    }
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
    countdownText() {
      return this.isCounting ? `${this.countdown}秒后重发` : "获取验证码"
    }
  },
  methods: {
    // 获取邮箱验证码
    async getEmailCode() {
      // 显示加载状态
      uni.showLoading({ title: '发送中...', mask: true })
      try {
        this.$http({
          url: "/user/getEmailCode?emailCategory=RESET_PASSWORD",
          method: "get",
        }).then(()=>{
          uni.showToast({ title: '验证码已发送', icon: 'success' })
          this.startCountdown()
        })
      } catch (e) {
        uni.showToast({ title: '发送失败: ' + e.message, icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },

    // 开始倒计时
    startCountdown() {
      this.isCounting = true
      this.countdown = 90
      this.timer = setInterval(() => {
        if (this.countdown <= 1) {
          clearInterval(this.timer)
          this.isCounting = false
          return
        }
        this.countdown--
      }, 1000)
    },

    // 提交表单
    async submitForm() {
      try {
        // 表单验证
        await this.$refs.formRef.validate()

        // 显示提交状态
        uni.showLoading({ title: '重置中...' })

        this.$http({
          url: "/user/resetPwd",
          method: 'post',
          data: this.formData
        }).then((data) => {
          uni.showToast({ title: '重置成功' })
          getApp().$vm.exit();
        })
      } catch (e) {
        uni.showToast({ title: '提交失败: ' + e.message, icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  },
  beforeDestroy() {
    // 清除定时器防止内存泄漏
    if (this.timer) clearInterval(this.timer)
  }
}
</script>

<style scoped lang="scss">
.code-input {
  display: flex;
  align-items: center;
}

.send-btn {
  margin-left: 20rpx;
  width: 220rpx;
  font-size: 26rpx;
  white-space: nowrap;
}

.send-btn.disabled {
  background-color: #eeeeee;
  color: #aaaaaa;
}

.submit-btn {
  margin-top: 60rpx;
}
</style>