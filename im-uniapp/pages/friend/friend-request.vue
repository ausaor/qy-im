<template>
  <view class="page friend-request">
    <nav-bar back>新的朋友</nav-bar>
    <view class="tab-control">
      <view class="tabs">
        <view class="tab" :class="{active: curTab === 1}" @click.stop="curTab = 1">我收到的({{receivedFriendRequest.length}})</view>
        <view class="tab" :class="{active: curTab === 2}" @click.stop="curTab = 2">我发起的({{launchFriendRequest.length}})</view>
      </view>
    </view>
    <view class="content" v-if="curTab === 1">
      <view class="request-item" v-for="request in receivedFriendRequest" :key="request.id">
        <view class="friend-request-item">
          <head-image :url="request.sendHeadImage" :name="request.sendNickName" size="small"></head-image>
          <view class="request-info">
            <view class="friend-name">{{request.sendNickName}}</view>
            <view class="info-text">
              <view class="remark">{{request.remark}}</view>
            </view>
          </view>
          <view class="btn-group">
            <button size="mini" type="primary" @click.stop="toUserInfoPage(request.id, request.sendId)">查看</button>
          </view>
        </view>
      </view>
    </view>
    <view class="content" v-if="curTab === 2">
      <view class="request-item" v-for="request in launchFriendRequest" :key="request.id">
        <view class="friend-request-item">
          <head-image :url="request.recvHeadImage" :name="request.recvNickName" size="small"></head-image>
          <view class="request-info">
            <view class="friend-name">{{request.recvNickName}}</view>
            <view class="info-text">
              <view class="remark">{{request.remark}}</view>
            </view>
          </view>
          <view class="btn-group">
            <button size="mini" type="warn" @click.stop="recallFriendRequest(request.id)">撤回</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>

export default {
  name: "friend-request",
  data() {
    return {
      curTab: 1,
    }
  },
  methods: {
    recallFriendRequest(id) {
      this.$http({
        url: `/friend/request/recall?id=${id}`,
        method: "post",
      }).then(() => {

      })
    },
    toUserInfoPage(requestId, userId) {
      uni.navigateTo({
        url: `/pages/common/user-info?id=${userId}&requestId=${requestId}`
      })
    }
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
    receivedFriendRequest() {
      return this.friendStore.friendRequests.filter((r) => r.recvId === this.mine.id && r.status === 1)
    },
    launchFriendRequest() {
      return this.friendStore.friendRequests.filter((r) => r.sendId === this.mine.id && r.status === 1)
    },
  },
  onLoad(options) {

  }
}
</script>

<style scoped lang="scss">
.friend-request {
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
      .friend-request-item {
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

          .friend-name {
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

        .btn-group {
          margin-right: .9375rem;
        }
      }
    }
  }
}
</style>