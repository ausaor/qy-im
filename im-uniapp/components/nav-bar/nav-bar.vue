<template>
  <view class="im-nav-bar" :style="{background: navBarBackground}">
    <!-- #ifndef H5 -->
    <view style="height: var(--status-bar-height)"></view>
    <!-- #endif -->
    <view class="im-nav-bar-content">
      <view class="back" @click="handleBackClick" v-if="back">
        <uni-icons type="back" :size="iconFontSize" :color="'#ffffff'"></uni-icons>
      </view>
      <view class="back" @click="$emit('gotoHome')" v-if="backHome">
        <uni-icons type="back" :size="iconFontSize" :color="'#ffffff'"></uni-icons>
      </view>
      <view class="title" v-if="title">
        <slot></slot>
      </view>
      <view class="btn">
        <uni-badge v-show="unreadCount > 0" :text="unreadCount"/>
        <uni-icons type="home" @click="$emit('gotoHome')" v-if="home" :size="iconFontSize" :color="'#ffffff'"></uni-icons>
        <uni-icons class="btn-item" v-if="search" type="search" :size="iconFontSize" :color="'#ffffff'"
          @click="$emit('search')"></uni-icons>
        <uni-icons class="btn-item" v-if="refresh" type="refresh" :size="iconFontSize" :color="'#ffffff'"
                   @click="$emit('refresh')"></uni-icons>
        <uni-icons class="btn-item" v-if="add" type="plusempty" :size="iconFontSize" :color="'#ffffff'" @click="$emit('add')"></uni-icons>
        <uni-icons class="btn-item" v-if="more" type="more-filled" :size="iconFontSize" :color="'#ffffff'"
          @click="$emit('more')"></uni-icons>
      </view>
    </view>
  </view>
</template>

<script>

export default {
  name: "nav-bar",
  props: {
    home: {
      type: Boolean,
      default: false
    },
    backHome: {
      type: Boolean,
      default: false
    },
    back: {
      type: Boolean,
      default: false
    },
    title: {
      type: Boolean,
      default: true
    },
    search: {
      type: Boolean,
      default: false
    },
    add: {
      type: Boolean,
      default: false
    },
    more: {
      type: Boolean,
      default: false
    },
    refresh: {
      type: Boolean,
      default: false
    },
    iconFontSize: {
      type: Number,
      default: 24
    },
    unreadCount: {
      type: Number,
      default: 0
    },
    themeIndex: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {}
  },
  computed: {
    navBarBackground() {
      const themes = this.uiStore.navBar.themes;
      const index = this.themeIndex >= 0 && this.themeIndex < themes.length ? this.themeIndex : 0;
      return themes[index].background;
    },
    height() {

    }
  },
  methods: {
    handleBackClick() {
      uni.navigateBack({
        delta: 1
      })
    },
  }
}
</script>

<style scoped lang="scss">
.im-nav-bar {
  position: fixed;
  top: 0;
  width: 100%;
  color: white;
  border-bottom: 1px solid $im-border-light;
  font-size: $im-font-size-large;
  z-index: 99;

  .im-nav-bar-content {
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    height: $im-nav-bar-height;

    .title {
      color: white;
    }

    .back {
      position: absolute;
      left: 0;
      height: 100%;
      display: flex;
      align-items: center;
      padding: 12px;
      font-size: 22px;
      box-sizing: border-box;
    }

    .btn {
      position: absolute;
      right: 0;
      height: 100%;
      display: flex;
      padding: 12px;
      align-items: center;
      box-sizing: border-box;

      .btn-item {
        margin-left: 8px;
      }
    }
  }

}
</style>