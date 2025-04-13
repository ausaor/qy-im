<template>
  <view class="region-item" :class="active ? 'active' : ''">
    <!--rich-text中的表情包会屏蔽事件，所以这里用一个遮罩层捕获点击事件 -->
    <view class="mask" @tap="showChatBox()"></view>
    <view class="left">
      <head-image :url="regionGroup.headImage" :name="regionGroup.remark"></head-image>
    </view>
    <view class="right">
      <view class="region-name">
        <view class="region-name-text">
          <view>{{ regionGroup.remark }}</view>
          <uni-tag v-if="regionGroup.joinType===1" circle text="常驻" size="small" type="success"></uni-tag>
          <uni-tag v-if="regionGroup.joinType===0" circle text="临时" size="small" type="warning"></uni-tag>
        </view>
        <view class="chat-time">{{ chat.lastSendTime ? $date.toTimeText(chat.lastSendTime, true) : '' }}</view>
      </view>
      <view class="chat-content">
        <view class="chat-at-text">{{ atText }}</view>
        <view class="chat-send-name" v-if="isShowSendName">{{ chat.sendNickName + ':&nbsp;' }}</view>
        <rich-text class="chat-content-text" :nodes="chat.lastContent ? $emo.transform(chat.lastContent,'emoji-small') : ''"></rich-text>
        <uni-badge v-if="chat.unreadCount > 0" :max-num="99" :text="chat.unreadCount" />
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: "region-item",
  props: {
    regionGroup: {
      type: Object
    },
    index: {
      type: Number
    },
    active: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    showChatBox() {
      // 初始化期间进入会话会导致消息不刷新
      if(!getApp().$vm.isInit || this.regionStore.isRegionLoading()){
        uni.showToast({
          title: "正在初始化页面,请稍后...",
          icon: 'none'
        })
        return;
      }
      uni.navigateTo({
        url: "/pages/region-group/region-chat-box?regionGroupId=" + this.regionGroup.id
      })
    },
  },
  computed: {
    chat() {
      let chat = this.regionStore.findRegionChatByGroup(this.regionGroup.id);
      return chat ? chat : {};
    },
    atText() {
      if (this.chat.atMe) {
        return "[有人@我]"
      } else if (this.chat.atAll) {
        return "[@全体成员]"
      }
      return "";
    },
    isShowSendName() {
      if (!this.chat.sendNickName) {
        return false;
      }
      let size = this.chat.messages.length;
      if (size === 0) {
        return false;
      }
      // 只有普通消息需要显示名称
      let lastMsg = this.chat.messages[size - 1];
      return this.$msgType.isNormal(lastMsg.type)
    },
  },
}
</script>

<style scoped lang="scss">
.region-item {
  height: 96rpx;
  display: flex;
  margin-bottom: 2rpx;
  position: relative;
  padding: 18rpx 20rpx;
  align-items: center;
  background-color: white;
  white-space: nowrap;

  &:hover {
    background-color: $im-bg-active;
  }

  &.active {
    background-color: $im-bg-active;
  }

  .mask {
    position: absolute;
    width: 100%;
    height: 100%;
    left: 0;
    right: 0;
    z-index: 99;
  }

  .left {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100rpx;
    height: 100rpx;
  }

  .right {
    height: 100%;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding-left: 20rpx;
    text-align: left;
    overflow: hidden;

    .region-name {
      display: flex;

      .region-name-text {
        flex: 1;
        font-size: $im-font-size-large;
        white-space: nowrap;
        overflow: hidden;
        display: flex;
        align-items: center;

        .uni-tag {
          text-align: center;
          margin-left: 5rpx;
          border: 0;
          padding: 1px 5px;
          //opacity: 0.8;
        }
      }

      .chat-time {
        font-size: $im-font-size-smaller-extra;
        color: $im-text-color-lighter;
        text-align: right;
        white-space: nowrap;
        overflow: hidden;
      }
    }

    .chat-content {
      display: flex;
      font-size: $im-font-size-smaller;
      color: $im-text-color-lighter;
      padding-top: 8rpx;
      align-items: center;

      .chat-at-text {
        color: $im-color-danger;
      }

      .chat-send-name {
        font-size: $im-font-size-smaller;
      }

      .chat-content-text {
        flex: 1;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;

      }

    }
  }
}
</style>