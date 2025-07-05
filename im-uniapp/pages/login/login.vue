<template>
	<view class="login">
    <view class="container">
      <view class="title">{{title}}</view>
      <uni-forms :modelValue="loginForm" :rules="rules" validate-trigger="bind">
        <uni-forms-item name="userName">
          <uni-easyinput type="text" v-model="loginForm.userName" prefix-icon="person" placeholder="用户名" />
        </uni-forms-item>
        <uni-forms-item name="password">
          <uni-easyinput type="password" v-model="loginForm.password" prefix-icon="locked" placeholder="密码" />
        </uni-forms-item>
        <uni-forms-item name="code">
          <view style="display: flex;align-items: center;">
            <view style="width: 60%;">
              <uni-easyinput type="text" v-model="loginForm.code" placeholder="请输入验证码" />
            </view>
            <view style="width: 40%;display: flex;justify-content: right;">
              <u-image :src="codeUrl" width="100px" height="35px" @click="getCode"></u-image>
            </view>
          </view>
        </uni-forms-item>
        <button class="btn-submit" @click="submit" type="primary">登录</button>
        <view class="register-link">
          <text>没有账号，</text>
          <navigator class="link-text" url="/pages/register/register">
            前往注册
          </navigator>
        </view>
      </uni-forms>
    </view>
	</view>
</template>

<script>
import UImage from "../../uni_modules/uview-plus/components/u--image/u--image.vue";

export default {
  components: {UImage},
	data() {
		return {
      title: '青語',
      codeUrl: '',
			loginForm: {
				terminal: 1, // APP终端
				userName: '',
				password: '',
        code: '',
        uuid: '',
        loginType: 0,
			},
			rules: {
				userName: {
					rules: [{
						required: true,
						errorMessage: '请输入用户名',
					}]
				},
				password: {
					rules: [{
						required: true,
						errorMessage: '请输入密码',
					}]
				},
        code: {
          rules: [{
            required: true,
            errorMessage: '请输入验证码',
          }]
        },
			}
		}
	},
	methods: {
		submit() {
			this.$http({
				url: '/login',
				data: this.loginForm,
				method: 'POST'
			}).then(loginInfo => {
				console.log("登录成功,自动跳转到聊天页面...")
				uni.setStorageSync("userName", this.loginForm.userName);
				uni.setStorageSync("loginInfo", loginInfo);
				// 调用App.vue的初始化方法
				getApp().$vm.init()
				// 跳转到聊天页面   
				uni.switchTab({
					url: "/pages/chat/chat"
				})
			})
		},
    getCode() {
      this.$http({
        url: "/captchaImage",
        method: "get"
      }).then((result) => {
        this.codeUrl = "data:image/gif;base64," + result['img'];
        this.loginForm.uuid = result['uuid'];
      })
    }
	},

	onLoad() {
    this.title = Math.floor(Math.random()*2) === 0 ? '輕語' : '青語';
    this.getCode();
		this.loginForm.userName = uni.getStorageSync("userName");
		this.loginForm.password = uni.getStorageSync("password");
	}
}
</script>

<style lang="scss">
.login {
  display: grid;
  place-items: center; /* 同时水平+垂直居中 */
  height: 100vh;
  background: linear-gradient(180deg, #E8F5FF 0%, #F8FBFF 100%);

  .container {
    width: 100%;
    max-width: 600rpx;
    position: relative;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 20rpx;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
    padding: 40rpx 10px;

    .title {
      padding-bottom: 50rpx;
      color: #2b85e4;
      text-align: center;
      font-size: 60rpx;
      font-weight: 600;
      letter-spacing: 32rpx;
    }

    .uni-forms {
      padding: 50rpx;

      .btn-submit {
        margin-top: 80rpx;
        border-radius: 50rpx;
        background: linear-gradient(135deg, #2B85E4 0%, #1A6FD6 100%);
      }

      .register-link {
        display: flex;
        justify-content: center;
        margin-top: 30rpx;
        text-align: center;
        font-size: 28rpx;
        color: #666;

        .link-text {
          color: #2B85E4;
          margin: 0 4rpx;
        }
      }
    }
  }
}
</style>