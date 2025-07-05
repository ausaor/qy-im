<template>
  <view  class="page mine-bind-email">
    <nav-bar back>消息通知</nav-bar>
    <uni-card :is-shadow="false" is-full :border="false">
      <view class="form-item">
        <view class="label">消息提示音</view>
        <view style="display: flex;align-items: center;gap: 10rpx;">
          <text style="color: #8f939c">关闭</text>
          <up-switch v-model="userInfo.soundPlay" activeColor="#13ce66" inactiveColor="#ff4949" @change="changeSoundPlay"></up-switch>
          <text style="color: #3cc51f">开启</text>
        </view>
      </view>
      <view class="form-item">
        <view class="label">语音自动播放</view>
        <view style="display: flex;align-items: center;gap: 10rpx;">
          <text style="color: #8f939c">关闭</text>
          <up-switch v-model="userInfo.autoPlay" activeColor="#13ce66" inactiveColor="#ff4949" @change="changeAutoPlay"></up-switch>
          <text style="color: #3cc51f">开启</text>
        </view>
      </view>
    </uni-card>
  </view>
</template>

<script>
export default {
  name: "mine-msg-notice",
  computed: {
    userInfo() {
      return this.userStore.userInfo;
    },
  },
  methods: {
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
      }).then(() => {
        this.userInfo.setUserInfo(this.userInfo);
        uni.showToast({
          title: "修改成功",
          icon: 'none'
        })
      })
    }
  }
}
</script>

<style scoped lang="scss">
.form-item {
  margin-bottom: 20rpx;
}
</style>