<template>
  <view class="page region-group-info">
    <nav-bar back home>群聊信息</nav-bar>
    <view v-if="!regionGroup.quit" class="group-members">
      <view class="member-items">
        <view v-for="(member, idx) in regionGroupMembers" :key="idx">
          <view class="member-item" v-if="idx < 9">
            <head-image :id="member.userId" :name="member.aliasName" :url="member.headImage" size="small"
                        :online="member.online"></head-image>
            <view class="member-name">
              <text>{{ member.aliasName }}</text>
            </view>
          </view>
        </view>
        <view class="view-btn" @click="toRegionGroupSpace">
          <svg-icon icon-class="shejiaotubiao-40" style="width: 86rpx;height: 86rpx;"></svg-icon>
        </view>
      </view>
      <view class="member-more" @click="onShowMoreMember()">{{ `查看全部群成员${regionGroupMembers.length}人` }}></view>
    </view>
    <view class="form">
      <view class="form-item">
        <view class="label" style="color: #3c9cff;" @click="chooseMember">选择成员</view>
        <view class="value" style="display: flex;align-items: center;">
          <head-image class="group-image" :name="choosedMember.aliasName" :url="choosedMember.headImage"
                      :size="60"></head-image>
          <text style="margin-left: 10rpx;">{{choosedMember.aliasName}}</text>
        </view>
      </view>
      <view class="form-item leader-transfer" v-if="myGroupMemberInfo.isLeader">
        <text @click="leaderTransfer">群主转移</text>
      </view>
      <view class="form-item leader-vote">
        <text>群主投票</text>
        <text>群主解除投票</text>
      </view>
      <view class="form-item">
        <view class="label">投票通知</view>
        <view class="value" style="display: flex;align-items: center;justify-content: space-between;">
          <text>关闭</text>
          <up-switch v-model="announce" activeColor="#13ce66" inactiveColor="#ff4949"></up-switch>
          <text>通知</text>
        </view>
      </view>
      <view class="form-item" v-if="myGroupMemberInfo.isLeader">
        <view class="label">全员禁言</view>
        <view class="value" style="display: flex;align-items: center;justify-content: space-between;">
          <text>关闭</text>
          <up-switch v-model="regionGroup.isBanned" activeColor="#13ce66" inactiveColor="#ff4949"></up-switch>
          <text>开启</text>
        </view>
      </view>
      <view class="form-item" v-if="myGroupMemberInfo.isLeader">
        <view class="label">禁言时长</view>
        <view class="value" style="display: flex;align-items: center;">
          <up-number-box :min="1" :max="720" v-model="bannedTime"></up-number-box>
          <text style="margin-left: 10rpx;">小时</text>
          <text style="color: #3c9cff;margin-left: 30rpx;">确认</text>
        </view>
      </view>
      <view class="form-item banned-msg" v-if="myGroupMemberInfo.isLeader">
        <text>用户禁言</text>
        <text>解除禁言</text>
      </view>
    </view>
    <region-group-members :members="regionGroupMembers" ref="membersPopup" @onConfirm="confirmChooseMember"></region-group-members>
    <up-modal :show="leaderTransferShow" title="群主转移" :content='leaderTransferContent' :showCancelButton="true"
              @confirm="confirmLeaderTransfer" @cancel="() => this.leaderTransferShow = false"></up-modal>
  </view>
</template>

<script>
import SvgIcon from "../../components/svg-icon/svg-icon.vue";
import HeadImage from "../../components/head-image/head-image.vue";
import RegionGroupMembers from "../../components/region-group-members/region-group-members.vue";

export default {
  name: "region-group-info",
  components: {RegionGroupMembers, HeadImage, SvgIcon},
  data() {
    return {
      regionGroupId: null,
      regionGroup: {},
      regionGroupMembers: [],
      myGroupMemberInfo: {},
      announce: false,
      bannedTime: 1,
      searchText: '',
      choosedMember: {},
      leaderTransferShow: false,
      leaderTransferContent: '',
    }
  },
  methods: {
    loadGroupInfo() {
      this.$http({
        url: `/region/group/find/${this.regionGroupId}`,
        method: 'GET'
      }).then((group) => {
        this.regionGroup = group;
        // 更新聊天页面的群聊信息
        this.regionStore.updateRegionChatFromGroup(group);
        // 更新聊天列表的群聊信息
        this.regionStore.updateRegionGroup(group);
      });
    },
    loadGroupMembers() {
      this.$http({
        url: `/region/group/members/${this.regionGroupId}`,
        method: "GET"
      }).then((members) => {
        this.regionGroupMembers = members.filter(m => !m.quit);
        this.myGroupMemberInfo = this.regionGroupMembers.find((m) => m.userId === this.mine.id);
      })
    },
    toRegionGroupSpace() {

    },
    onShowMoreMember() {

    },
    chooseMember() {
      this.$refs.membersPopup.open();
    },
    confirmChooseMember(member) {
      this.choosedMember = member;
    },
    leaderTransfer() {
      this.leaderTransferContent = `请确认是否将群主转移给群成员【${this.choosedMember.aliasName}】？`;
      this.leaderTransferShow = true;
    },
    confirmLeaderTransfer() {
      if (this.choosedMember == null || this.choosedMember.joinType === 0) {
        uni.showToast({
          title: "请先选择一位常驻成员",
          icon: 'none'
        })
        return;
      }
      if (this.myGroupMemberInfo.userId === this.choosedMember.userId) {
        uni.showToast({
          title: "不能选择自己",
          icon: 'none'
        })
        return;
      }
      let paramVO = {
        regionGroupId: this.regionGroup.id,
        userId: this.choosedMember.userId,
        joinType: this.choosedMember.joinType,
      }
      this.$http({
        url: '/region/group/leaderTransfer',
        method: 'post',
        data: paramVO
      }).then(() => {
        uni.showToast({
          title: "操作成功",
          icon: 'none'
        });
        this.leaderTransferShow = false;
      });
    },
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
  },
  onLoad(options) {
    this.regionGroupId = options.id;
    // 查询群聊信息
    this.loadGroupInfo(options.id);
    // 查询群聊成员
    this.loadGroupMembers(options.id)
  }
}
</script>

<style scoped lang="scss">
.region-group-info {
  .group-members {
    padding: 30rpx;
    background: white;

    .member-items {
      display: flex;
      align-items: center;
      flex-wrap: wrap;

      .member-item {
        width: 120rpx;
        display: flex;
        flex-direction: column;
        margin: 8rpx 2rpx;
        position: relative;
        align-items: center;
        padding-right: 5px;
        white-space: nowrap;

        .member-name {
          width: 100%;
          flex: 1;
          overflow: hidden;
          text-align: center;
          white-space: nowrap;
          padding-top: 8rpx;
          font-size: $im-font-size-smaller;
        }
      }

      .invite-btn {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 86rpx;
        height: 86rpx;
        margin: 10rpx;
        border: $im-border solid 2rpx;
        border-radius: 10%;
      }

      .view-btn {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 86rpx;
        height: 86rpx;
        margin: 10rpx;
        border: $im-border solid 2rpx;
        border-radius: 10%;
      }

      .upload-btn {
        width: 120rpx;
        display: flex;
        flex-direction: column;
        position: relative;
        align-items: center;
        white-space: nowrap;

        .upload-image {
          width: 86rpx;
          height: 86rpx;
          border: $im-border solid 2rpx;
          border-radius: 50%;

          display: flex;
          justify-content: center;
          align-items: center;

          .head-image {
            width: 86rpx;
            height: 86rpx;
            border-radius: 50%;
          }
        }
      }
    }

    .member-more {
      padding-top: 24rpx;
      text-align: center;
      font-size: $im-font-size-small;
      color: $im-text-color-lighter;
    }
  }

  .group-types {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10rpx;
    margin-bottom: 20rpx;

    .group-type-item {
      padding: 20rpx 20rpx;
      background-color: white;
      text-align: left;
      color: #35567f;
      font-size: 32rpx;
      font-weight: 600;
    }
  }

  .form {
    background-color: white;
    margin-top: 20rpx;
    padding: 10rpx 40rpx;

    .form-item {
      padding: 0 40rpx;
      display: flex;
      background: white;
      align-items: center;
      margin-top: 2rpx;

      .label {
        width: 220rpx;
        line-height: 100rpx;
        font-size: $im-font-size;
        white-space: nowrap;
      }

      .value {
        flex: 1;
        text-align: right;
        line-height: 100rpx;
        color: $im-text-color-lighter;
        font-size: $im-font-size-small;
        white-space: nowrap;
        overflow: hidden;
      }
    }

    .group-image {
      width: 60rpx;
      height: 60rpx;
      border-radius: 50%;
      border: 1px solid #ccc;
    }

    .leader-transfer {
      color: #3c9cff;
      line-height: 100rpx;
    }

    .leader-vote {
      display: flex;
      justify-content: space-between;
      color: #3c9cff;
      line-height: 100rpx;
    }

    .banned-msg {
      display: flex;
      justify-content: space-between;
      color: #3c9cff;
      line-height: 100rpx;
    }

    .group-edit {
      padding: 10rpx 40rpx 30rpx 40rpx	;
      text-align: center;
      background: white;
      font-size: $im-font-size-small;
      color: $im-text-color-lighter;
    }
  }

  .common-form {
    margin-top: 60rpx;
    width: 100%;

    .form-item {
      padding: 0 40rpx;
      display: flex;
      background: white;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10rpx;

      .label {
        line-height: 100rpx;
        font-size: $im-font-size;
        white-space: nowrap;
      }

      .value{
        white-space: nowrap;
      }

      .input {
        flex: 1;
        text-align: right;
        line-height: 100rpx;
        font-size: $im-font-size-small;
      }

      .group-image {
        width: 120rpx;
        height: 120rpx;
        border-radius: 50%;
        border: 1px solid #ccc;
      }
    }

    .btns {
      margin-top: 50rpx;
      display: flex;
      justify-content: center;
      gap: 60rpx;
      padding: 0 40rpx;
    }
  }
}
</style>