<template>
  <uni-popup ref="popup" type="bottom">
    <view class="member-list-box">
      <view class="search-bar">
        <uni-search-bar v-model="searchText" cancelButton="none" radius="100" placeholder="搜索"></uni-search-bar>
      </view>
      <view class="btns">
        <up-button text="取消" :custom-style="{width: '30%'}" @click="cancel"></up-button>
        <up-button type="primary" text="确定" :custom-style="{width: '30%'}" @click="confirm"></up-button>
      </view>
      <view class="member-list">
        <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
          <view v-for="(member, index) in members" v-show="!searchText || member.aliasName.includes(searchText) || member.nickName.includes(searchText)"
                :key="member.id">
            <view class="member-item" @click="chooseMember(member, index)" :class="{ checked: memberIndex === index}">
              <head-image :name="member.aliasName" :online="member.online"
                          :url="member.headImage"></head-image>
              <view class="member-name">{{ member.aliasName }}</view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </uni-popup>
</template>

<script>
import HeadImage from "../head-image/head-image.vue";

export default {
  name: "region-group-members",
  components: {HeadImage},
  props: {
    members: Array,
  },
  data() {
    return {
      searchText: "",
      member: {},
      memberIndex: -1,
    };
  },
  methods: {
    open() {
      this.$refs.popup.open();
    },
    cancel() {
      this.$refs.popup.close();
    },
    confirm() {
      this.$refs.popup.close();
      this.$emit("onConfirm", this.member);
    },
    chooseMember(member, index) {
      this.member = member;
      this.memberIndex = index;
    }
  }
}
</script>



<style scoped lang="scss">
.member-list-box {
  background-color: #fff;
  padding: 10rpx;

  .btns {
    display: flex;
  }

  .member-list {
    margin-top: 10rpx;
    overflow: hidden;

    .scroll-bar {
      height: 800rpx;

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
          flex: 1;
          padding-left: 20rpx;
          font-size: 30rpx;
          font-weight: 600;
          line-height: 60rpx;
          white-space: nowrap;
          overflow: hidden;
        }

        .member-info {
          display: flex;
          align-items: center;

          .nick-name {
            font-size: 30rpx;
            font-weight: 600;
            line-height: 60rpx;
            white-space: nowrap;
            margin-right: 20rpx;
          }
        }
      }

      .checked {
        background-color: $im-color-primary-light-9;
      }
    }
  }
}
</style>