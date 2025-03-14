<template>
  <uni-popup ref="popup" type="bottom">
    <view class="member-list-box">
      <view class="search-bar">
        <uni-search-bar v-model="searchText" cancelButton="none" radius="100" placeholder="搜索"></uni-search-bar>
      </view>
      <view class="member-list">
        <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
          <view v-for="member in members" v-show="!searchText || member.aliasName.includes(searchText) || member.nickName.includes(searchText)"
                :key="member.id">
            <view class="member-item">
              <head-image :name="member.aliasName" :online="member.online"
                          :url="member.headImage"></head-image>
              <view class="member-name">{{ member.aliasName }}</view>
              <view class="member-info" v-if="member.isTemplate">
                <view class="nick-name">{{member.nickName}}</view>
                <head-image :name="member.nickName" :url="member.userAvatar" :online="member.online">
                </head-image>
              </view>
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
  name: "group-member-list",
  components: {HeadImage},
  props: {
    members: Array,
  },
  data() {
    return {
      searchText: "",
    };
  },
  methods: {
    open() {
      this.$refs.popup.open();
    },
  }
}
</script>



<style scoped lang="scss">
.member-list-box {
  background-color: #fff;
  padding: 10rpx;

  .member-list {
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
    }
  }
}
</style>