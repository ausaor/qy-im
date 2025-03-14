<template>
  <uni-popup ref="popup" type="bottom">
    <view class="character-avatar-list-box">
      <view class="choosed-character-avatar">
        <character-avatar :size="70" :url="activeAvatar.avatar" :name="activeAvatar.name"></character-avatar>
        <view class="character-avatar-name">{{ activeAvatar.name }}</view>
      </view>
      <view class="btns">
        <up-button text="取消" :custom-style="{width: '30%'}" @click="cancel"></up-button>
        <up-button type="primary" text="确定" :disabled="characterAvatars.length === 0" :custom-style="{width: '30%'}" @click="confirm"></up-button>
      </view>
      <view class="search-bar">
        <uni-search-bar v-model="searchText" cancelButton="none" radius="100" placeholder="搜索"></uni-search-bar>
      </view>
      <view class="character-avatar-list">
        <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
          <view v-for="(item, index) in characterAvatars" v-show="item.name.includes(searchText)" :key="item.id"
                class="character-avatar-item" :class="{checked: activeIndex===index}" @click="onSwitchChecked(item, index)">
            <view class="character-avatar-info">
              <character-avatar :size="70" :url="item.avatar" :name="item.name"></character-avatar>
              <view class="character-avatar-name">{{ item.name }}</view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </uni-popup>
</template>

<script>
import CharacterAvatar from "../character-avatar/character-avatar.vue";

export default {
  name: "character-avatar-list",
  components: {CharacterAvatar},
  props: {
    characterAvatars: Array,
  },
  data() {
    return {
      searchText: "",
      activeIndex: -1,
      activeAvatar: {},
    };
  },
  methods: {
    open() {
      this.$refs.popup.open();
    },
    onSwitchChecked(character, index) {
      this.activeIndex = index;
      this.activeAvatar = character;
    },
    cancel() {
      this.$refs.popup.close();
    },
    confirm() {
      if (!this.activeAvatar.id) {
        uni.showToast({
          title: "请选择一个角色头像",
          icon: 'none'
        });
        return;
      }
      this.$emit("confirm", this.activeAvatar);
      this.$refs.popup.close();
      this.activeIndex = -1;
      this.activeAvatar = {};
    }
  },
}
</script>

<style scoped lang="scss">
.character-avatar-list-box {
  background-color: #fff;
  padding: 10rpx;

  .choosed-character-avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20rpx;

    .character-avatar-name {
      margin-left: 20rpx;
    }
  }

  .btns {
    display: flex;
  }

  .character-avatar-list {
    overflow: hidden;

    .scroll-bar {
      height: 800rpx;

      .character-avatar-item {
        height: 80rpx;
        margin-bottom: 10rpx;
        display: flex;
        justify-content: space-between;

        .character-avatar-info {
          display: flex;
          align-items: center;

          .character-avatar-name {
            margin-left: 20rpx;
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