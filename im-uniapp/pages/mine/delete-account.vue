<template>
  <view class="page delete-account">
    <nav-bar back>注销账号</nav-bar>
    <uni-card :is-shadow="false" is-full :border="false">
      <!-- 警告提示 -->
      <view class="warning-section">
        <uni-notice-bar 
          showIcon 
          backgroundColor="#fef0f0" 
          color="#f56c6c"
          text="警告：注销账号是不可逆操作！注销后您的所有数据将被永久删除，包括好友关系、群组信息等。请谨慎操作。"
        />
      </view>

      <!-- 注意事项 -->
      <view class="notice-section">
        <view class="notice-title">
          <text class="iconfont icon-warning"></text>
          <text>注意事项</text>
        </view>
        <view class="notice-list">
          <view class="notice-item">1. 注销后，您的账号将无法恢复</view>
          <view class="notice-item">2. 您的个人资料将不再可见</view>
          <view class="notice-item">3. 所有好友关系将被解除</view>
          <view class="notice-item">4. 您将退出所有加入的群组</view>
        </view>
      </view>

      <!-- 用户信息确认 -->
      <view class="user-confirm-section">
        <view class="confirm-text">当前登录账号：</view>
        <view class="user-info">
          <head-image :name="userInfo.nickName" :url="userInfo.headImage" :size="80"></head-image>
          <view class="user-detail">
            <view class="user-name">{{ userInfo.nickName }}</view>
            <view class="user-id">ID: {{ userInfo.userName }}</view>
          </view>
        </view>
      </view>

      <!-- 确认复选框 -->
      <view class="checkbox-section">
        <checkbox-group @change="onCheckboxChange">
          <label class="checkbox-label">
            <checkbox :checked="confirmed" color="#f56c6c" />
            <text class="checkbox-text">我已了解注销后果，确认要注销此账号</text>
          </label>
        </checkbox-group>
      </view>

      <!-- 注销按钮 -->
      <button 
        class="delete-btn" 
        type="warn" 
        :disabled="!confirmed"
        @click="confirmDeleteAccount"
      >
        确认注销账号
      </button>
    </uni-card>
  </view>
</template>

<script>
import NavBar from "../../components/nav-bar/nav-bar.vue";
import HeadImage from "../../components/head-image/head-image.vue";

export default {
  name: "delete-account",
  components: { NavBar, HeadImage },
  data() {
    return {
      confirmed: false
    };
  },
  computed: {
    userInfo() {
      return this.userStore.userInfo;
    }
  },
  methods: {
    // 复选框变化
    onCheckboxChange(e) {
      this.confirmed = e.detail.value.length > 0;
    },
    
    // 确认注销账号
    confirmDeleteAccount() {
      if (!this.confirmed) {
        uni.showToast({
          title: '请先确认了解注销后果',
          icon: 'none'
        });
        return;
      }

      uni.showModal({
        title: '最后确认',
        content: '此操作将永久注销您的账号，所有数据将被删除且无法恢复，是否继续？',
        confirmColor: '#f56c6c',
        success: (res) => {
          if (res.confirm) {
            this.deleteAccount();
          }
        }
      });
    },
    
    // 执行注销操作
    deleteAccount() {
      uni.showLoading({
        title: '注销中...',
        mask: true
      });

      this.$http({
        url: "/user/deleteAccount",
        method: "post"
      }).then(() => {
        uni.hideLoading();
        uni.showToast({
          title: '账号注销成功',
          icon: 'success',
          duration: 2000
        });
        
        // 延迟一下再退出，让用户看到成功提示
        setTimeout(() => {
          // 调用全局退出方法
          getApp().$vm.exit();
        }, 2000);
      }).catch((error) => {
        uni.hideLoading();
        uni.showToast({
          title: error.message || '账号注销失败，请重试',
          icon: 'none',
          duration: 2000
        });
      });
    }
  }
};
</script>

<style scoped lang="scss">
.delete-account {
  padding: 20rpx;

  .warning-section {
    margin-bottom: 30rpx;
  }

  .notice-section {
    background-color: #fdf6ec;
    border: 1px solid #faecd8;
    border-radius: 8rpx;
    padding: 24rpx;
    margin-bottom: 30rpx;

    .notice-title {
      display: flex;
      align-items: center;
      font-size: $im-font-size;
      font-weight: 600;
      color: #e6a23c;
      margin-bottom: 16rpx;

      .icon-warning {
        margin-right: 10rpx;
        font-size: 32rpx;
      }
    }

    .notice-list {
      .notice-item {
        font-size: $im-font-size-small;
        color: #e6a23c;
        line-height: 1.8;
        margin-bottom: 8rpx;
      }
    }
  }

  .user-confirm-section {
    background-color: #f5f7fa;
    border-radius: 8rpx;
    padding: 24rpx;
    margin-bottom: 30rpx;

    .confirm-text {
      font-size: $im-font-size-small;
      color: $im-text-color-light;
      margin-bottom: 16rpx;
    }

    .user-info {
      display: flex;
      align-items: center;

      .user-detail {
        margin-left: 20rpx;
        flex: 1;

        .user-name {
          font-size: $im-font-size;
          font-weight: 600;
          color: $im-text-color;
          margin-bottom: 8rpx;
        }

        .user-id {
          font-size: $im-font-size-small;
          color: $im-text-color-light;
        }
      }
    }
  }

  .checkbox-section {
    margin-bottom: 40rpx;
    padding: 20rpx 0;

    .checkbox-label {
      display: flex;
      align-items: flex-start;

      .checkbox-text {
        margin-left: 16rpx;
        font-size: $im-font-size-small;
        color: $im-text-color;
        line-height: 1.6;
      }
    }
  }

  .delete-btn {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    font-size: $im-font-size;
    font-weight: 600;
    border-radius: 8rpx;
    margin-top: 20rpx;
    transition: all 0.3s ease;

    &:active {
      transform: scale(0.98);
    }

    &[disabled] {
      opacity: 0.5;
    }
  }
}
</style>