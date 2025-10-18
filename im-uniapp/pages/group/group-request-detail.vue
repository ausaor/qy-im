<template>
  <view class="page group-request-detail">
    <nav-bar back>群聊请求详情</nav-bar>
    <view class="content">
      <view class="group-info">
        <head-image :url="request.groupHeadImage" :name="request.groupName" size="small"></head-image>
        <view class="info">
          <view class="group-name">
            <text>{{request.groupName}}</text>
            <text class="group-type">
              <uni-tag v-if="request.groupType===1" :circle="true" text="模板群聊" type="primary" size="mini" custom-style="background-color: rgb(0,47,167);border-color: rgb(0,47,167);" />
              <uni-tag v-if="request.groupType===2" :circle="true" text="多元角色群聊" type="primary" size="mini" custom-style="background-color: rgb(105,149,114);border-color: rgb(105,149,114);" />
              <uni-tag v-if="request.groupType===3" :circle="true" text="角色群聊" type="primary" size="mini" custom-style="background-color: rgb(144,0,33);border-color: rgb(144,0,33);" />
              <uni-tag v-if="request.groupType===4" :circle="true" text="模板角色群聊" type="primary" size="mini" custom-style="background-color: rgb(176,89,35);border-color: rgb(176,89,35);" />
            </text>
          </view>
          <view class="info-text">
            <view class="remark">{{request.remark}}</view>
          </view>
        </view>
      </view>
      <view class="user-info">
        <view>用户信息：</view>
        <head-image :url="request.userHeadImage" :name="request.userNickname" :size="60"></head-image>
        <view class="user-name">{{request.userNickname}}</view>
      </view>
      <view class="character-info" v-if="request.templateCharacterName">
        <view>角色信息：</view>
        <head-image :url="request.templateCharacterAvatar" :name="request.templateCharacterName" :size="60"></head-image>
        <view class="character-name">{{request.templateCharacterName}}</view>
      </view>
      <view class="launch-user">
        <view>发起用户：</view>
        <head-image :url="request.launchUserHeadImage" :name="request.launchUserNickname" :size="60"></head-image>
        <view class="user-name">{{request.launchUserNickname}}</view>
      </view>
      <view class="btn-group">
        <button size="mini" type="primary">同意</button>
        <button size="mini" type="warn">拒绝</button>
        <button size="mini" type="success">撤回</button>
        <button size="mini" type="primary">修改</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: "group-request-detail",
  data() {
    return {
      request: {}
    };
  },
  methods: {
    getRequestDetail(id) {
      this.$http({
        url: `/group/request/detail?id=${id}`,
        method: 'GET'
      }).then((data) => {
        this.request = data;
      }).catch(() => {

      });
    }
  },
  onLoad(options) {
    this.getRequestDetail(options.id)
  }
}
</script>

<style scoped lang="scss">
.group-request-detail {
  display: flex;
  flex-direction: column;
  background-color: #fff;

  .content {
    padding: 10px;

    .group-info {
      display: flex;
      align-items: center;

      .info {
        margin-left: 10px;
        flex-direction: column;
      }

      .group-name {
        flex: 1;
        font-size: .9375rem;
        white-space: nowrap;
        overflow: hidden;

        .group-type {
          margin-left: 10px;
        }
      }

      .info-text {
        display: flex;
        font-size: .8125rem;
        color: #909399;
        padding-top: .25rem;
        align-items: center;
        white-space: nowrap;
        max-width: 13.125rem;
        overflow: hidden;

        .remark {
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      padding-top: 10rpx;

      .user-name {
        margin-left: 14rpx;
      }
    }

    .character-info {
      display: flex;
      align-items: center;
      padding-top: 10rpx;

      .character-name {
        margin-left: 14rpx;
      }
    }

    .launch-user {
      display: flex;
      align-items: center;
      padding-top: 10rpx;

      .user-name {
        margin-left: 14rpx;
      }
    }

    .btn-group {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
  }
}
</style>