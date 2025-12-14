<template>
  <view class="page group-member">
    <nav-bar back :theme-index="18">群成员</nav-bar>
    <view class="nav-bar">
      <view class="nav-search">
        <uni-search-bar v-model="searchText" radius="100" cancelButton="none"
                        placeholder="输入昵称搜索"></uni-search-bar>
      </view>
    </view>
    <view class="member-items">
      <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
        <view v-for="(member, idx) in regionGroupMembers"
              v-show="!searchText || member.aliasName.includes(searchText)" :key="idx">
          <view class="member-item" @click="onShowUserInfo(member.userId)">
            <head-image :name="member.aliasName" :online="member.online"
                        :url="member.headImage"></head-image>
            <view class="member-name">{{ member.aliasName }}
              <uni-tag v-if="member.userId == regionGroup.ownerId" text="群主" size="small" circle type="error">
              </uni-tag>
              <uni-tag v-if="member.userId == userStore.userInfo.id" text="我" size="small" circle></uni-tag>
            </view>
            <view class="member-handle">
              <button type="info" plain v-if="isOwner && !isSelf(member.userId) && !member.isBanned" size="mini"
                      @click.stop="onMuteMember(member, idx)">禁言</button>
              <button type="success" plain v-if="isOwner && !isSelf(member.userId) && member.isBanned" size="mini"
                      @click.stop="onUnMuteMember(member, idx)">解除禁言</button>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 禁言时长输入弹窗 -->
    <uni-popup ref="mutePopup" type="dialog">
      <uni-popup-dialog mode="input" title="禁言时长(小时)" placeholder="请输入禁言时长(-1~60000)" :value="muteDuration"
                        type="number" :before-close="true" @close="onMuteCancel" @confirm="onMuteConfirm">
      </uni-popup-dialog>
    </uni-popup>
  </view>
</template>

<script>
export default {
  name: "region-group-member",
  data() {
    return {
      searchText: "",
      regionGroup: {},
      regionGroupMembers: [],
      currentMuteMember: null,
      currentMuteIndex: -1,
      muteDuration: ""
    }
  },
  methods: {
    onShowUserInfo(userId) {
      uni.navigateTo({
        url: "/pages/common/user-info?id=" + userId
      })
    },
    loadGroupInfo(id) {
      this.$http({
        url: `/region/group/find/${id}`,
        method: 'GET'
      }).then((group) => {
        this.regionGroup = group;
      });
    },
    loadGroupMembers(id) {
      this.$http({
        url: `/region/group/members/${id}`,
        method: "GET"
      }).then((members) => {
        this.regionGroupMembers = members.filter(m => !m.quit);
      })
    },
    isSelf(userId) {
      return this.userStore.userInfo.id == userId
    },
    onMuteMember(member, idx) {
      this.currentMuteMember = member;
      this.currentMuteIndex = idx;
      this.muteDuration = "";
      this.$refs.mutePopup.open();
    },
    onMuteCancel() {
      this.$refs.mutePopup.close();
    },
    onMuteConfirm(duration) {
      // 验证输入值
      const muteTime = parseInt(duration);
      if (isNaN(muteTime) || muteTime < -1 || muteTime > 60000) {
        uni.showToast({
          title: '请输入-1到60000之间的数值',
          icon: 'none'
        });
        return;
      }

      let param = {
        code: this.regionGroup.code,
        num: this.regionGroup.num,
        id: this.regionGroup.id,
        userId: this.currentMuteMember.userId,
        joinType: this.currentMuteMember.joinType,
        aliasName: this.currentMuteMember.aliasName,
        banDuration: muteTime,
        allBanned: false,
        banType: 'master'
      }

      // 发送禁言请求
      this.$http({
        url: '/region/group/banMsg',
        method: 'POST',
        data: param
      }).then(() => {
        uni.showToast({
          title: `已禁言成员${this.currentMuteMember.aliasName}`,
          icon: 'none'
        });

        // 更新本地数据
        this.regionGroupMembers[this.currentMuteIndex].isBanned = true;
        this.$refs.mutePopup.close();
      }).catch(() => {
        uni.showToast({
          title: '禁言操作失败',
          icon: 'none'
        });
      });
    },
    onUnMuteMember(member, idx) {
      let param = {
        code: this.regionGroup.code,
        num: this.regionGroup.num,
        id: this.regionGroup.id,
        userId: this.currentMuteMember.userId,
        joinType: this.currentMuteMember.joinType,
        aliasName: this.currentMuteMember.aliasName,
        banDuration: 1,
        allBanned: false,
        banType: 'master'
      }
      // 解除禁言逻辑
      this.$http({
        url: '/region/group/unBanMsg',
        method: 'POST',
        data: param
      }).then(() => {
        uni.showToast({
          title: `已解除成员${member.aliasName}的禁言`,
          icon: 'none'
        });

        // 更新本地数据
        this.regionGroupMembers[idx].isBanned = false;
      }).catch(() => {
        uni.showToast({
          title: '解除禁言操作失败',
          icon: 'none'
        });
      });
    }
  },
  computed: {
    isOwner() {
      return this.userStore.userInfo.id == this.regionGroup.ownerId;
    }
  },
  onLoad(options) {
    this.loadGroupInfo(options.id);
    this.loadGroupMembers(options.id);
  },
  onUnload() {

  }
}
</script>

<style scoped lang="scss">
.group-member {
  position: relative;
  display: flex;
  flex-direction: column;

  .member-items {
    position: relative;
    flex: 1;
    overflow: hidden;

    .member-item {
      height: 120rpx;
      display: flex;
      margin-bottom: 1rpx;
      position: relative;
      padding: 0 30rpx;
      align-items: center;
      background-color: white;
      white-space: nowrap;

      .member-name {
        display: flex;
        align-items: center;
        flex: 1;
        padding-left: 20rpx;
        font-size: $im-font-size;
        line-height: $im-font-size * 2;
        white-space: nowrap;
        overflow: hidden;

        .uni-tag {
          margin-left: 5rpx;
          min-width: 40rpx;
          border: 0;
          height: 30rpx;
          line-height: 30rpx;
          font-size: 20rpx;
          text-align: center;
        }
      }
    }

    .scroll-bar {
      height: 100%;
    }
  }
}
</style>