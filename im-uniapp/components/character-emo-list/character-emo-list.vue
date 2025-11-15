<template>
  <uni-popup ref="popup" type="bottom">
    <view class="character-word-list-box">
      <view class="btns">
        <up-button text="取消" :custom-style="{width: '30%'}" @click="cancel"></up-button>
        <up-button type="primary" text="确定" :custom-style="{width: '30%'}" @click="confirm"></up-button>
      </view>
      <view class="cate-box">
        <view class="cate-item" :class="{actived: tabIndex===1}" @click="switchCate(1)">角色</view>
        <view class="cate-item" :class="{actived: tabIndex===2}" @click="switchCate(2)">群聊</view>
      </view>
      <view class="list">
        <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
          <view class="cate-box-list" v-show="tabIndex === 1">
            <view v-for="(item, index) in emos.character" :key="item.id" class="item" :class="{checked: activeIndex===index}" @click.stop="onSwitchChecked(item, index)">
              <view class="item-img">
                <image class="emo-img" :src="item.url" mode="aspectFit" lazy-load="true"></image>
              </view>
            </view>
          </view>
          <view class="cate-box-list" v-show="tabIndex === 2">
            <view v-for="(item, index) in emos.group" :key="item.id" class="item" :class="{checked: activeIndex===index}" @click.stop="onSwitchChecked(item, index)">
              <view class="item-img">
                <image class="emo-img" :src="item.url" mode="aspectFit" lazy-load="true"></image>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </uni-popup>
</template>

<script>
import SvgIcon from "../svg-icon/svg-icon.vue";

export default {
  name: "character-emo-list",
  components: {SvgIcon},
  props: {
    emos: {
      type: Object,
      default() {
        return {
          group: [],
          character: []
        }
      }
    }
  },
  data() {
    return {
      searchText: "",
      activeIndex: -1,
      activeItem: {},
      tabIndex: 1,
    };
  },
  methods: {
    open() {
      this.$refs.popup.open();
    },
    onSwitchChecked(data, index) {
      this.activeIndex = index;
      this.activeItem = data;
    },
    cancel() {
      this.$refs.popup.close();
    },
    confirm() {
      if (!this.activeItem.id) {
        uni.showToast({
          title: "您未选中数据",
          icon: 'none'
        });
        return;
      }
      this.$emit("confirm", this.activeItem);
      this.$refs.popup.close();
      this.activeIndex = -1;
      this.activeItem = {};
    },
    switchCate(index) {
      this.activeIndex = -1;
      this.tabIndex = index;
    },
  },
}
</script>

<style scoped lang="scss">
.character-word-list-box {
  background-color: #fff;
  padding: 10rpx;

  .btns {
    display: flex;
  }

  .cate-box {
    display: flex;
    justify-content: center;
    margin-top: 20rpx;
    margin-bottom: 20rpx;

    .cate-item {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 30%;
      padding: 10rpx;

      color: #323233;
      background-color: #fff;
      border-color: #ebedf0;
      border-width: 1px;
      border-style: solid;
    }

    .actived {
      background-color: orange;
      color: white;
    }
  }

  .list {
    overflow: hidden;

    .scroll-bar {
      height: 800rpx;

      .cate-box-list {
        display: grid;
        grid-template-columns: repeat(6, 1fr);
        gap: 20rpx;
        padding: 20rpx;
      }

      .item {
        aspect-ratio: 1/1;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 10rpx;
      }

      .item-img {
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
      }

      .emo-img {
        width: 90rpx;
        height: 90rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        aspect-ratio: 1; /* 宽高比 1:1 */
        object-fit: cover; /* 图片裁剪填充 */
      }

      .checked {
        background-color: $im-color-primary-light-9;
      }
    }
  }
}
</style>