<template>
  <uni-popup ref="popup" type="bottom">
    <view class="character-word-list-box">
      <view class="btns">
        <up-button text="取消" :custom-style="{width: '30%'}" @click="cancel"></up-button>
        <up-button type="primary" text="确定" :custom-style="{width: '30%'}" @click="confirm"></up-button>
      </view>
      <view class="cate-box">
        <view class="cate-item" :class="{actived: wordIndex===1}" @click="switchCate(1)">角色</view>
        <view class="cate-item" :class="{actived: wordIndex===2}" @click="switchCate(2)">群聊</view>
      </view>
      <view class="list">
        <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
          <view class="cate-box-list" v-show="wordIndex === 1">
            <view v-for="(item, index) in words.character" :key="item.id" class="item" :class="{checked: activeIndex===index}" @click.stop="onSwitchChecked(item, index)">
              <view class="item-box">
                <view class="item-name">{{item.word}}</view>
                <view class="item-voice" @click.stop="onPlayWordVoice(item.voice)">
                  <svg-icon :icon-class="'xitongxiaoxi'"></svg-icon>
                </view>
              </view>
            </view>
          </view>
          <view class="cate-box-list" v-show="wordIndex === 2">
            <view v-for="(item, index) in words.group" :key="item.id" class="item" :class="{checked: activeIndex===index}" @click.stop="onSwitchChecked(item, index)">
              <view class="item-box">
                <view class="item-name">{{item.word}}</view>
                <view class="item-voice" @click.stop="onPlayWordVoice(item.voice)">
                  <svg-icon :icon-class="'xitongxiaoxi'"></svg-icon>
                </view>
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
  name: "character-word-list",
  components: {SvgIcon},
  props: {
    words: {
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
      wordIndex: 1,
      audioContext: null,
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
      this.wordIndex = index;
    },
    onPlayWordVoice(url) {
      // 创建音频上下文
      this.audioContext = uni.createInnerAudioContext();
      // 设置音频源
      this.audioContext.src = url;
      // 监听音频播放结束事件
      this.audioContext.onEnded(() => {
        console.log('音频播放结束');
      });
      // 监听音频播放错误事件
      this.audioContext.onError((res) => {
        console.log('音频播放出错:', res.errMsg);
      });
      this.audioContext.play();
    }
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
      width: 30%;
      height: 40rpx;
      text-align: center;
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

      .item-box {
        height: 60rpx;
        margin-bottom: 10rpx;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding-left: 20rpx;
        padding-right: 20rpx;
      }

      .checked {
        background-color: $im-color-primary-light-9;
      }
    }
  }
}
</style>