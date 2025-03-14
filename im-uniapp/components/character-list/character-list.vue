<template>
  <uni-popup ref="popup" type="bottom">
    <view class="character-list-box">
      <view class="choosed-character">
        <character-avatar :size="70" :url="activeCharacter.avatar" :name="activeCharacter.name"></character-avatar>
        <view class="character-name">{{ activeCharacter.name }}</view>
      </view>
      <view class="btns">
        <up-button text="取消" :custom-style="{width: '30%'}" @click="cancel"></up-button>
        <up-button type="primary" text="确定" :disabled="characters.length === 0" :custom-style="{width: '30%'}" @click="confirm"></up-button>
      </view>
      <view class="search-bar">
        <uni-search-bar v-model="searchText" cancelButton="none" radius="100" placeholder="搜索"></uni-search-bar>
      </view>
      <view class="character-list">
        <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
          <view v-for="(character, index) in characters" v-show="character.name.includes(searchText)" :key="character.id"
                class="character-item" :class="{checked: activeIndex===index}" @click="onSwitchChecked(character, index)">
            <view class="character-info">
              <character-avatar :size="70" :url="character.avatar" :name="character.name"></character-avatar>
              <view class="character-name">{{ character.name }}</view>
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
  name: "character-list",
  components: {CharacterAvatar},
  props: {
    characters: Array,
  },
  data() {
    return {
      searchText: "",
      activeIndex: -1,
      activeCharacter: {}
    };
  },
  methods: {
    open() {
      this.$refs.popup.open();
    },
    onSwitchChecked(character, index) {
      this.activeIndex = index;
      this.activeCharacter = character;
    },
    cancel() {
      this.$refs.popup.close();
    },
    confirm() {
      if (!this.activeCharacter.id) {
        uni.showToast({
          title: "请选择一个角色",
          icon: 'none'
        });
        return;
      }
      this.$emit("confirm", this.activeCharacter);
      this.$refs.popup.close();
      this.activeIndex = -1;
      this.activeCharacter = {};
    }
  },
}
</script>

<style scoped lang="scss">
.character-list-box {
  background-color: #fff;
  padding: 10rpx;

  .choosed-character {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20rpx;

    .character-name {
      margin-left: 20rpx;
    }
  }

  .btns {
    display: flex;
  }

  .character-list {
    overflow: hidden;

    .scroll-bar {
      height: 800rpx;

      .character-item {
        height: 80rpx;
        margin-bottom: 10rpx;
        display: flex;
        justify-content: space-between;

        .character-info {
          display: flex;
          align-items: center;

          .character-name {
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