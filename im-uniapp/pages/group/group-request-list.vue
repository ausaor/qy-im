<template>
  <view class="page group-request">
    <nav-bar back>群聊请求</nav-bar>
    <view class="tab-control">
      <view class="tabs">
        <view class="tab" v-if="isOwner" :class="{active: curTab === 1}" @click.stop="curTab = 1">申请中的({{joinGroupRequests.length}})</view>
        <view class="tab" :class="{active: curTab === 2}" @click.stop="curTab = 2">邀请中的({{inviteGroupRequests.length}})</view>
      </view>
    </view>
    <view class="content" v-if="curTab === 1">
      <view class="request-item" v-for="request in joinGroupRequests" :key="request.id">
        <view class="group-request-item">
          <head-image :url="request.userHeadImage" :name="request.userNickname" size="small"></head-image>
          <view class="request-info">
            <view class="user-name">{{request.userNickname}}</view>
            <view class="info-text">
              <view class="remark">{{request.remark}}</view>
            </view>
          </view>
          <view class="btn-group">
            <button size="mini" type="primary" @click="viewGroupRequestDetail(request.id, '1')">查看</button>
          </view>
        </view>
      </view>
    </view>
    <view class="content" v-if="curTab === 2">
      <view class="request-item" v-for="request in inviteGroupRequests" :key="request.id">
        <view class="group-request-item">
          <head-image :url="request.userHeadImage" :name="request.userNickname" size="small"></head-image>
          <view class="request-info">
            <view class="user-name">{{request.userNickname}}</view>
            <view class="info-text">
              <view class="remark">{{request.remark}}</view>
            </view>
          </view>
          <view class="btn-group">
            <button size="mini" type="primary" @click="viewGroupRequestDetail(request.id, '2')">查看</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: "group-request-list",
  data() {
    return {
      curTab: 1,
      groupId: null,
      groupOwnerId: null,
    }
  },
  methods: {
    viewGroupRequestDetail(id, type) {
      uni.navigateTo({
        url: `/pages/group/group-request-detail?id=${id}&type=${type}`
      });
    }
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
    isOwner() {
      return this.groupOwnerId === this.mine.id;
    },
    joinGroupRequests() {
      // 群组申请(当前用户是群主，待审核的加群申请)
      return this.groupOwnerId === this.mine.id ? this.groupStore.groupRequests
          .filter((r) => r.groupOwnerId === this.mine.id && r.status === 1 && r.type === 1 && r.groupId === this.groupId) : [];
    },
    inviteGroupRequests() {
      // 群组邀请(当前用户是群成员，正在邀请中的数据)
      return this.groupId ? this.groupStore.groupRequests
          .filter((r) => r.status === 1 && r.type === 2 && r.groupId === this.groupId && (r.launchUserId === this.mine.id || r.groupOwnerId === this.mine.id)) : [];
    },
  },
  onLoad(options) {
    this.groupId = Number(options.groupId);
    this.groupOwnerId = Number(options.groupOwnerId);
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

          .user-name {
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