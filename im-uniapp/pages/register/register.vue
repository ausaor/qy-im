<template>
	<view class="register">
    <view class="container">
      <view class="title">欢迎注册</view>
      <uni-forms  ref="form" :modelValue="dataForm" :rules="rules" validate-trigger="bind" label-width="80px">
        <uni-forms-item name="userName" label="用户名">
          <uni-easyinput type="text" v-model="dataForm.userName" placeholder="用户名" />
        </uni-forms-item>
        <uni-forms-item name="nickName" label="昵称">
          <uni-easyinput type="text" v-model="dataForm.nickName" placeholder="昵称" />
        </uni-forms-item>
        <uni-forms-item name="password" label="密码">
          <uni-easyinput type="password" v-model="dataForm.password" placeholder="密码" />
        </uni-forms-item>
        <uni-forms-item name="corfirmPassword" label="确认密码">
          <uni-easyinput type="password" v-model="dataForm.corfirmPassword" placeholder="确认密码" />
        </uni-forms-item>
        <uni-forms-item name="code" label="验证码">
          <view style="display: flex;align-items: center;">
            <view style="width: 50%;">
              <uni-easyinput type="text" v-model="dataForm.code" placeholder="验证码" />
            </view>
            <view style="width: 50%;display: flex;justify-content: right;">
              <u-image :src="codeUrl" width="100px" height="35px" @click="getCode"></u-image>
            </view>
          </view>
        </uni-forms-item>
        <button class="btn-submit" @click="submit" type="primary">注册</button>
        <view class="login-link">
          <navigator class="link-text" url="/pages/login/login">
            返回登录页面
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
      codeUrl: '',
			dataForm: {
				userName: '',
				nickName: '',
				password: '',
				corfirmPassword: '',
        code: '',
        uuid: '',
			},
			rules: {
				userName: {
					rules: [{
						required: true,
						errorMessage: '请输入用户名',
					}]
				},
				nickName: {
					rules: [{
						required: true,
						errorMessage: '请输入昵称',
					}]
				},
				password: {
					rules: [{
						required: true,
						errorMessage: '请输入密码',
					}]
				},
				corfirmPassword: {
					rules: [{
						required: true,
						errorMessage: '请输入确认密码',
					}, {
						validateFunction: (rule, value, data, callback) => {
							console.log("validateFunction")
							if (data.password != value) {
								callback('两次密码输入不一致')
							}
							return true;
						}
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
  onLoad(options) {
    this.getCode();
  },
	methods: {
		submit() {
			this.$refs.form.validate().then(() => {
				this.$http({
					url: '/register',
					data: this.dataForm,
					method: 'POST'
				}).then(() => {
					uni.showToast({
						title: "注册成功",
						icon: 'none'
					})
          uni.navigateTo({
            url: '/pages/login/login',
            success: function () {
              console.log('跳转成功');
            },
            fail: function (err) {
              console.log('跳转失败', err);
            }
          });
				})
			})
		},
    getCode() {
      this.$http({
        url: "/captchaImage",
        method: "get"
      }).then((result) => {
        this.codeUrl = "data:image/gif;base64," + result['img'];
        this.dataForm.uuid = result['uuid'];
      })
    }
	}
}
</script>

<style lang="scss">
.register {
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
    padding: 40rpx 10rpx;

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

      .login-link {
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