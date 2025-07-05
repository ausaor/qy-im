<template>
  <view  class="page mine-bind-email">
    <nav-bar back>绑定邮箱</nav-bar>
    <uni-card :is-shadow="false" is-full :border="false">
      <uni-forms ref="formRef" :model="formData" :rules="rules">
        <!-- 邮箱输入框 -->
        <uni-forms-item label="" name="email" label-width="0px">
          <uni-easyinput
              v-model="formData.email"
              placeholder="请输入邮箱地址"
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
                :disabled="isCounting || !isEmailValid"
                @click="getEmailCode"
            >
              {{ countdownText }}
            </button>
          </view>
        </uni-forms-item>
      </uni-forms>

      <!-- 提交按钮 -->
      <button class="submit-btn" type="primary" @click="submitForm">绑定邮箱</button>
    </uni-card>
  </view>
</template>

<script>
export default {
  name: "mind-bind-email",
  data() {
    return {
      formData: {
        email: "",
        emailCode: ""
      },
      // 表单验证规则
      rules: {
        email: {
          rules: [
            { required: true, errorMessage: '邮箱不能为空' },
            {
              validateFunction: (rule, value, data, callback) => {
                if (!/^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/.test(value)) {
                  callback('请输入有效的邮箱地址')
                }
                return true
              }
            }
          ]
        },
        emailCode: {
          rules: [
            { required: true, errorMessage: '验证码不能为空' },
            { minLength: 4, maxLength: 6, errorMessage: '验证码格式不正确' }
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
    // 计算属性：验证邮箱格式是否有效
    isEmailValid() {
      const emailRegex = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
      return emailRegex.test(this.formData.email)
    },
    countdownText() {
      return this.isCounting ? `${this.countdown}秒后重发` : "获取验证码"
    }
  },
  methods: {
    // 获取邮箱验证码
    async getEmailCode() {
      // 验证邮箱格式
      const emailValid = await this.$refs.formRef.validateField('email')
      if (!emailValid) return

      // 显示加载状态
      uni.showLoading({ title: '发送中...', mask: true })

      try {
        let params = {
          toEmail: this.formData.email,
          category: 'BIND_EMAIL'
        }
        this.$http({
          url: "/email/getCode",
          method: "post",
          data: params
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
        uni.showLoading({ title: '绑定中...' })

        this.$http({
          url: "/user/bindEmail",
          method: 'post',
          data: this.formData
        }).then((data) => {
          uni.showToast({ title: '绑定成功' })
          // 成功后的操作，如返回上一页
          uni.navigateBack()
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