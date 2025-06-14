<template>
  <view class="page region-group-info">
    <nav-bar back home @gotoHome="gotoHome">群聊信息</nav-bar>
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
      </view>
      <view class="member-more" @click="onShowMoreMember()">{{ `查看全部群成员${regionGroupMembers.length}人` }}></view>
    </view>
    <view class="form">
      <view class="form-item" @click="toRegionGroupSpace">
        <view class="form-item-left">
          <svg-icon icon-class="shejiaotubiao-40" style="width: 60rpx;height: 60rpx;"></svg-icon>
          <text style="margin-left: 10rpx;margin-right: 10rpx;">地区空间</text>
          <view v-show="unreadNotifyCount > 0" class="notify-count">{{unreadNotifyCount}}</view>
        </view>
        <view class="form-item-right">
          <text class="unread-talk-count" v-show="unreadTalkCount > 0">{{unreadTalkCount}}条新动态</text>
          <head-image v-for="(talk, index) in talkList" :key="index" :url="talk.avatar" :name="talk.nickName" :size="45"></head-image>
          <uni-icons type="right" size="20" color="#888888"></uni-icons>
        </view>
      </view>
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
        <text @click="voteLeader">群主投票</text>
        <text @click="voteRemoveLeader">群主解除投票</text>
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
          <up-switch v-model="regionGroup.isBanned" activeColor="#13ce66" inactiveColor="#ff4949" @change="doAllBanned"></up-switch>
          <text>开启</text>
        </view>
      </view>
      <view class="form-item" v-if="myGroupMemberInfo.isLeader">
        <view class="label">禁言时长</view>
        <view class="value" style="display: flex;align-items: center;">
          <up-number-box :min="1" :max="720" v-model="bannedTime"></up-number-box>
          <text style="margin-left: 10rpx;">小时</text>
        </view>
      </view>
      <view class="form-item banned-msg" v-if="myGroupMemberInfo.isLeader">
        <text @click="doBanned('banned')">成员禁言</text>
        <text @click="doBanned('unBanned')">解除禁言</text>
      </view>
    </view>
    <bar-group v-if="!regionGroup.quit">
      <btn-bar type="primary" title="发送消息" @tap="onSendMessage()"></btn-bar>
    </bar-group>
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
      this.talkStore.resetRegionTalk(this.regionGroup.code);
      this.talkStore.resetRegionNotify(this.regionGroup.code);
      uni.navigateTo({
        url: `/pages/activity/activity-space?category=region&section=my-region&regionGroupId=${this.regionGroup.id}&regionCode=${this.regionGroup.code}&spaceTitle=地区空间动态`
      })
    },
    onShowMoreMember() {
      uni.navigateTo({
        url: `/pages/region-group/region-group-member?id=${this.regionGroupId}`
      })
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
      if (!this.choosedMember.userId || this.choosedMember.joinType === 0) {
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
    voteLeader() {
      if (this.myGroupMemberInfo.joinType === 0) {
        uni.showToast({
          title: "您不是当前地区群聊常驻用户",
          icon: 'none'
        });
        return;
      }
      if (!this.choosedMember.userId || this.choosedMember.joinType === 0) {
        uni.showToast({
          title: "请先选择一位常驻成员",
          icon: 'none'
        })
        return;
      }
      let paramVO = {
        regionGroupId: this.regionGroup.id,
        userId: this.choosedMember.userId,
        joinType: this.choosedMember.joinType,
        announce: this.announce,
      }
      uni.showModal({
        title: '群主投票', // 标题
        content: `请确认是否投票给群成员【${this.choosedMember.aliasName}】？`,
        success: (res) => {
          if (res.confirm) {
            this.$http({
              url: '/region/group/vote',
              method: 'post',
              data: paramVO
            }).then(() => {
              uni.showToast({
                title: "操作成功",
                icon: 'none'
              });
            });
          }
        }
      });
    },
    voteRemoveLeader() {
      if (this.myGroupMemberInfo.joinType === 0) {
        uni.showToast({
          title: "您不是当前地区群聊常驻用户",
          icon: 'none'
        });
        return;
      }
      if (!this.choosedMember.userId || this.choosedMember.joinType === 0 || !this.choosedMember.isLeader) {
        uni.showToast({
          title: "请先选中群主",
          icon: 'none'
        })
        return;
      }
      let paramVO = {
        regionGroupId: this.regionGroup.id,
        userId: this.choosedMember.userId,
        joinType: this.choosedMember.joinType,
        announce: this.announce,
      }

      uni.showModal({
        title: '群主解除投票', // 标题
        content: `【群主解除投票】请确认是否投票解除群成员【${this.choosedMember.aliasName}】的群主身份？`,
        success: (res) => {
          if (res.confirm) {
            this.$http({
              url: '/region/group/voteRemove',
              method: 'post',
              data: paramVO
            }).then(() => {
              uni.showToast({
                title: "操作成功",
                icon: 'none'
              });
            });
          }
        }
      });
    },
    doAllBanned(value) {
      if (!this.myGroupMemberInfo.isLeader) {
        uni.showToast({
          title: "您不是当前地区群聊群主",
          icon: 'none'
        });
        return;
      }
      let paramVO = {
        code: this.regionGroup.code,
        num: this.regionGroup.num,
        id: this.regionGroup.id,
        allBanned: value,
        banDuration: this.bannedTime,
        banType: 'master'
      }
      let url = "";
      if (value) {
        url = '/region/group/banMsg';
      } else {
        url = '/region/group/unBanMsg'
      }
      this.$http({
        url: url,
        method: 'post',
        data: paramVO
      }).then(() => {
        this.regionGroup.isBanned = value;
        uni.showToast({
          title: "操作成功",
          icon: 'none'
        });
      }).catch((e) => {
        this.regionGroup.isBanned = !value;
      })
    },
    doBanned(type) {
      if (!this.myGroupMemberInfo.isLeader) {
        uni.showToast({
          title: "您不是当前地区群聊群主",
          icon: 'none'
        });
        return;
      }
      if (!this.choosedMember.userId) {
        uni.showToast({
          title: "请先选择群聊成员",
          icon: 'none'
        });
        return;
      }
      let paramVO = {
        code: this.regionGroup.code,
        num: this.regionGroup.num,
        id: this.regionGroup.id,
        userId: this.choosedMember.userId,
        joinType: this.choosedMember.joinType,
        aliasName: this.choosedMember.aliasName,
        banDuration: this.bannedTime,
        allBanned: false,
        banType: 'master'
      }
      let url = "";
      if (type === 'banned') {
        url = "/region/group/banMsg";
      } else {
        url = "/region/group/unBanMsg";
      }
      this.$http({
        url: url,
        method: 'post',
        data: paramVO
      }).then(() => {
        this.loadGroupMembers(this.regionGroup.id);
        uni.showToast({
          title: "操作成功",
          icon: 'none'
        });
      }).catch((e) => {
      })
    },
    onSendMessage() {
      let chat = {
        type: 'REGION-GROUP',
        targetId: this.regionGroup.id,
        showName: this.regionGroup.remark,
        headImage: this.regionGroup.headImage,
      };
      this.regionStore.openRegionChat(chat);
      uni.navigateTo({
        url: "/pages/region-group/region-chat-box?regionGroupId=" + this.regionGroup.id
      })
    },
    gotoHome() {
      uni.reLaunch({
        url: '/pages/region-group/region-group'
      })
    }
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
    talkList() {
      let talkMap =this.talkStore.regionTalks;
      let talks = talkMap.get(this.regionGroup.code)
      if (talks && talks.length > 2) {
        return talks.slice(0, 2);
      }
      return talks ? talks : [];
    },
    unreadTalkCount() {
      let talkMap =this.talkStore.regionTalks;
      let talks = talkMap.get(this.regionGroup.code);
      if (talks) {
        return talks.length;
      }
      return 0;
    },
    unreadNotifyCount() {
      let notifyMap =this.talkStore.regionNotify;
      let count = notifyMap.get(this.regionGroup.code);
      if (count) {
        return count;
      }
      return 0;
    }
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

      .form-item-left {
        display: flex;
        flex: 1;
        align-items: center;

        .notify-count {
          background-color: #f56c6c;
          color: white;
          font-size: 20rpx;
          font-weight: 500;
          width: 30rpx;
          height: 30rpx;
          line-height: 30rpx;
          border-radius: 50%;
          text-align: center;
        }
      }

      .form-item-right {
        display: flex;
        align-items: center;

        .unread-talk-count {
          margin-right: 10rpx;
          font-size: 20rpx;
          font-weight: 500;
          color: red;
        }
      }

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