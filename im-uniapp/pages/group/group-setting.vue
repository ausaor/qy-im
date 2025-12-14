<template>
  <view class="page group-setting">
    <nav-bar back :theme-index="16">群管理</nav-bar>
    <view class="setting-box">
      <!-- 全局禁言开关 -->
      <view class="setting-item">
        <text class="setting-label">全局禁言</text>
        <text style="margin-right: 10rpx;">关闭</text>
        <up-switch v-model="group.isBanned" @change="doAllBanned"></up-switch>
        <text style="margin-left: 10rpx;">开启</text>
      </view>
      
      <!-- 禁言时长输入框 -->
      <view class="setting-item">
        <text class="setting-label">禁言时长(分钟)</text>
        <input 
          class="setting-input" 
          type="number" 
          v-model="bannedTime"
          placeholder="请输入禁言时长(-1~60000)" 
          @blur="validateBannedTime"
        />
        <text class="setting-hint">-1为永久禁言</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: "group-setting",
  data() {
    return {
      groupId: '',
      groupOwnerId: '',
      group: {},
      bannedTime: 10,
    }
  },
  onLoad(option) {
    this.groupId = option.groupId
    this.groupOwnerId = option.groupOwnerId
    // 页面加载后获取群组信息
    this.$nextTick(() => {
      this.loadGroupInfo();
    })
  },
  methods: {
    loadGroupInfo() {
      this.$http({
        url: `/group/find/${this.groupId}`,
        method: 'GET'
      }).then((group) => {
        this.group = group;
      });
    },
    doAllBanned(value) {
      let paramVO = {
        id: this.group.id,
        allBanned: value,
        banDuration: this.bannedTime,
        banType: 'master'
      }
      let url = "";
      if (value) {
        url = '/group/banMsg';
      } else {
        url = '/group/unBanMsg'
      }
      this.$http({
        url: url,
        method: 'post',
        data: paramVO
      }).then(() => {
        this.group.isBanned = value;
      }).catch((e) => {
        this.group.isBanned = !value;
      })
    },
    // 验证禁言时长输入
    validateBannedTime() {
      let time = parseInt(this.bannedTime);
      if (isNaN(time) || time < -1) {
        this.bannedTime = -1;
      } else if (time > 60000) {
        this.bannedTime = 60000;
      }
    }
  }
}
</script>

<style scoped lang="scss">
.setting-item {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #eee;
}

.setting-label {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.setting-input {
  width: 200rpx;
  height: 60rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  padding: 0 10rpx;
  text-align: right;
}

.setting-hint {
  margin-left: 20rpx;
  font-size: 24rpx;
  color: #999;
}
</style>