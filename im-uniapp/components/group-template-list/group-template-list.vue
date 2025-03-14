<template>
  <uni-popup ref="popup" type="bottom">
    <view class="template-list-box">
      <view class="choosed-template">
        <character-avatar :size="70" :url="activeGroupTemplate.avatar" :name="activeGroupTemplate.groupName"></character-avatar>
        <view class="template-name">{{ activeGroupTemplate.groupName }}</view>
      </view>
      <view class="btns">
        <up-button text="取消" :custom-style="{width: '30%'}" @click="cancel"></up-button>
        <up-button type="primary" text="确定" :disabled="groupTemplates.length === 0" :custom-style="{width: '30%'}" @click="confirm"></up-button>
      </view>
      <view class="search-bar">
        <uni-search-bar v-model="searchText" cancelButton="none" radius="100" placeholder="搜索"></uni-search-bar>
      </view>
      <view class="template-list">
        <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
          <view v-for="(template, index) in groupTemplates" v-show="template.groupName.includes(searchText)" :key="template.id"
                class="template-item" :class="{checked: activeIndex===index}" @click="onSwitchChecked(template, index)">
            <view class="template-info">
              <character-avatar :size="70" :url="template.avatar" :name="template.groupName"></character-avatar>
              <view class="template-name">{{ template.groupName }}</view>
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
  name: "group-template-list",
  components: {CharacterAvatar},
  props: {
    groupTemplates: Array,
  },
  data() {
    return {
      searchText: "",
      activeIndex: -1,
      activeGroupTemplate: {}
    }
  },
  methods: {
    open() {
      this.$refs.popup.open();
    },
    onSwitchChecked(template, index) {
      this.activeIndex = index;
      this.activeGroupTemplate = template;
    },
    cancel() {
      this.$refs.popup.close();
    },
    confirm() {
      if (!this.activeGroupTemplate.id) {
        uni.showToast({
          title: "请选择一个群聊模板",
          icon: 'none'
        });
        return;
      }
      this.$emit("confirm", this.activeGroupTemplate);
      this.$refs.popup.close();
      this.activeIndex = -1;
      this.activeGroupTemplate = {};
    }
  },
}
</script>

<style scoped lang="scss">
.template-list-box {
  background-color: #fff;
  padding: 10rpx;

  .choosed-template {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20rpx;

    .template-name {
      margin-left: 20rpx;
    }
  }

  .btns {
    display: flex;
  }

  .template-list {
    overflow: hidden;

    .scroll-bar {
      height: 800rpx;

      .template-item {
        height: 80rpx;
        margin-bottom: 10rpx;
        display: flex;
        justify-content: space-between;

        .template-info {
          display: flex;
          align-items: center;

          .template-name {
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