<template>
  <view class="page group-request">
    <nav-bar back>新的群聊</nav-bar>
    <view class="tab-control">
      <view class="tabs">
        <view class="tab" :class="{active: curTab === 1}" @click.stop="curTab = 1">我收到的({{receivedGroupRequest.length}})</view>
        <view class="tab" :class="{active: curTab === 2}" @click.stop="curTab = 2">我发起的({{launchGroupRequest.length}})</view>
      </view>
    </view>
    <view class="content" v-if="curTab === 1">
      <view class="request-item" v-for="request in receivedGroupRequest" :key="request.id">
        <view class="group-request-item">
          <head-image :url="request.groupHeadImage" :name="request.groupName" size="small"></head-image>
          <view class="request-info">
            <view class="group-name">{{request.groupName}}</view>
            <view class="info-text">
              <view class="remark">{{request.remark}}</view>
            </view>
          </view>
          <view class="group-info">
            <uni-tag v-if="request.groupType===1" :circle="true" text="模板群聊" type="primary" size="mini" custom-style="background-color: rgb(0,47,167);border-color: rgb(0,47,167);" />
            <uni-tag v-if="request.groupType===2" :circle="true" text="多元角色群聊" type="primary" size="mini" custom-style="background-color: rgb(105,149,114);border-color: rgb(105,149,114);" />
            <uni-tag v-if="request.groupType===3" :circle="true" text="角色群聊" type="primary" size="mini" custom-style="background-color: rgb(144,0,33);border-color: rgb(144,0,33);" />
            <uni-tag v-if="request.groupType===4" :circle="true" text="模板角色群聊" type="primary" size="mini" custom-style="background-color: rgb(176,89,35);border-color: rgb(176,89,35);" />
          </view>
          <view class="btn-group">
            <button size="mini" type="primary" @click="viewGroupRequestDetail(request.id)">查看</button>
          </view>
        </view>
      </view>
    </view>
    <view class="content" v-if="curTab === 2">
      <view class="request-item" v-for="request in launchGroupRequest" :key="request.id">
        <view class="group-request-item">
          <head-image :url="request.groupHeadImage" :name="request.groupName" size="small"></head-image>
          <view class="request-info">
            <view class="group-name">{{request.groupName}}</view>
            <view class="info-text">
              <view class="remark">{{request.remark}}</view>
            </view>
          </view>
          <view class="group-info">
            <uni-tag v-if="request.groupType===1" :circle="true" text="模板群聊" type="primary" size="mini" custom-style="background-color: rgb(0,47,167);border-color: rgb(0,47,167);" />
            <uni-tag v-if="request.groupType===2" :circle="true" text="多元角色群聊" type="primary" size="mini" custom-style="background-color: rgb(105,149,114);border-color: rgb(105,149,114);" />
            <uni-tag v-if="request.groupType===3" :circle="true" text="角色群聊" type="primary" size="mini" custom-style="background-color: rgb(144,0,33);border-color: rgb(144,0,33);" />
            <uni-tag v-if="request.groupType===4" :circle="true" text="模板角色群聊" type="primary" size="mini" custom-style="background-color: rgb(176,89,35);border-color: rgb(176,89,35);" />
          </view>
          <view class="btn-group">
            <button size="mini" type="primary" @click="viewGroupRequestDetail(request.id)">查看</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: "group-request",
  data() {
    return {
      curTab: 1,
    }
  },
  methods: {
    viewGroupRequestDetail(id) {
      uni.navigateTo({
        url: `/pages/group/group-request-detail?id=${id}`
      });
    }
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
    receivedGroupRequest() {
      return this.groupStore.groupRequests.filter((r) => r.userId === this.mine.id && r.status === 1 && r.type === 2)
    },
    launchGroupRequest() {
      return this.groupStore.groupRequests.filter((r) => r.userId === this.mine.id && r.status === 1 && r.type === 1)
    },
  },
  onLoad(options) {

  }
}
</script>

<style scoped lang="scss">
.group-request {
  display: flex;
  flex-direction: column;
  background-color: #fff;

  .tab-control {
    padding: .625rem;

    .tabs {
      height: 3.125rem;
      display: flex;
      align-items: center;
      border-radius: .625rem;
      background-color: #f6f7fb;

      .tab {
        color: #909399;
        padding: .5625rem .9375rem;
        border-radius: .625rem;
        font-weight: 800 !important;
        flex: 1;
        text-align: center;
        margin: 0 .3125rem;
        font-size: 1.125rem;

        &.active {
          color: #000;
          background: #fff;
        }
      }
    }
  }

  .content {
    position: relative;
    flex: 1;

    .request-item {
      .group-request-item {
        height: 3.4375rem;
        display: flex;
        margin-bottom: .03125rem;
        position: relative;
        padding: .3125rem .3125rem .3125rem .625rem;
        align-items: center;
        background-color: #fff;
        white-space: nowrap;

        .request-info {
          flex: 1;
          display: flex;
          flex-direction: column;
          padding-left: .625rem;
          text-align: left;

          .group-name {
            font-size: .9375rem;
            white-space: nowrap;
            overflow: hidden;
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

        .group-info {
          margin-right: 18rpx;
        }

        .btn-group {
          margin-right: .9375rem;
        }
      }
    }
  }
}
</style>