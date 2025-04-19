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
  </view>
</template>

<script>
import SvgIcon from "../../components/svg-icon/svg-icon.vue";

export default {
  name: "region-group-info",
  components: {SvgIcon},
  data() {
    return {
      regionGroupId: null,
      regionGroup: {},
      regionGroupMembers: [],
      myGroupMemberInfo: {},
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

    }
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
    margin-top: 20rpx;

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